import os
import csv
from datetime import datetime
from flask import Flask, request, jsonify, abort
import logging

from apscheduler.schedulers.background import BackgroundScheduler

app = Flask(__name__)

CSV_HEADERS = [
    "mpu1_ax", "mpu1_ay", "mpu1_az", "mpu1_gx", "mpu1_gy", "mpu1_gz",
    "mpu2_ax", "mpu2_ay", "mpu2_az", "mpu2_gx", "mpu2_gy", "mpu2_gz",
    "HMC_x", "HMC_y", "HMC_z", "Heading_degrees",
    "Date", "Time"
]

ACTIVITY_HEADERS = [
    "activity_label", "activity_start_date", "activity_start_time",
    "activity_end_date", "activity_end_time", "height"
]

last_seen = {i: datetime.utcnow() for i in range(1, 6)}


def init_csv_files() -> None:
    for i in range(1, 6):
        fn = f"esp{i}.csv"
        if not os.path.exists(fn):
            with open(fn, "w", newline="") as f:
                csv.writer(f).writerow(CSV_HEADERS)

    if not os.path.exists("activity.csv"):
        with open("activity.csv", "w", newline="") as f:
            csv.writer(f).writerow(ACTIVITY_HEADERS)


def append_data(device_id: int, payload: dict) -> None:
    now = datetime.now()
    row = [
        payload.get("mpu1", {}).get("ax", "N/A"),
        payload.get("mpu1", {}).get("ay", "N/A"),
        payload.get("mpu1", {}).get("az", "N/A"),
        payload.get("mpu1", {}).get("gx", "N/A"),
        payload.get("mpu1", {}).get("gy", "N/A"),
        payload.get("mpu1", {}).get("gz", "N/A"),

        payload.get("mpu2", {}).get("ax", "N/A"),
        payload.get("mpu2", {}).get("ay", "N/A"),
        payload.get("mpu2", {}).get("az", "N/A"),
        payload.get("mpu2", {}).get("gx", "N/A"),
        payload.get("mpu2", {}).get("gy", "N/A"),
        payload.get("mpu2", {}).get("gz", "N/A"),

        payload.get("HMCx", "N/A"),
        payload.get("HMCy", "N/A"),
        payload.get("HMCz", "N/A"),
        payload.get("Heading", "N/A"),

        now.strftime("%Y-%m-%d"),
        now.strftime("%H:%M:%S")
    ]
    with open(f"esp{device_id}.csv", "a", newline="") as f:
        csv.writer(f).writerow(row)


def last_row(filename: str):
    try:
        with open(filename, "rb") as f:
            f.seek(-2, os.SEEK_END)  # jump before final newline
            while f.read(1) != b'\n':
                f.seek(-2, os.SEEK_CUR)  # scan backwards
            return f.readline().decode().strip().split(",")
    except (OSError, IndexError):  # file too small or missing
        return None


@app.route("/esp<int:device_id>/data", methods=["POST"])
def ingest(device_id):
    if device_id not in range(1, 6):
        abort(404, "Unknown device_id (must be 1-5).")

    try:
        payload = request.get_json(force=True)
    except Exception:
        abort(400, "Invalid or missing JSON body.")

    append_data(device_id, payload)

    last_seen[device_id] = datetime.utcnow()

    return jsonify({"status": "ok"}), 200


@app.route("/esp<int:device_id>/latest", methods=["GET"])
def latest(device_id):
    row = last_row(f"esp{device_id}.csv")
    if row is None:
        abort(404, "No data yet.")
    # Re-shape to the same JSON structure the mobile app expects
    j = {
        "MPU1": dict(zip(["ax", "ay", "az", "gx", "gy", "gz"], row[0:6])),
        "MPU2": dict(zip(["ax", "ay", "az", "gx", "gy", "gz"], row[6:12])),
        "HMC": {"x": row[12], "y": row[13], "z": row[14], "degrees": row[15]},
        "Timestamps": {"date": row[16], "time": row[17]}
    }
    return jsonify(j)


@app.route("/data", methods=["GET"])
def latest_all():
    out = {}
    for i in range(1, 6):
        row = last_row(f"esp{i}.csv")
        if row:
            out[f"ESP{i}"] = {
                "MPU1": dict(zip(["ax", "ay", "az", "gx", "gy", "gz"], row[0:6])),
                "MPU2": dict(zip(["ax", "ay", "az", "gx", "gy", "gz"], row[6:12])),
                "HMC": {"x": row[12], "y": row[13], "z": row[14], "degrees": row[15]},
                "Timestamps": {"date": row[16], "time": row[17]}
            }
    return jsonify(out)


@app.route("/activity", methods=["POST"])
def label_activity():
    data = request.get_json(force=True)
    if not data or not all(k in data for k in ACTIVITY_HEADERS):
        abort(400, "Body must contain: " + ", ".join(ACTIVITY_HEADERS))

    with open("activity.csv", "a", newline="") as f:
        csv.writer(f).writerow([
            data["activity_label"],
            data["activity_start_date"],
            data["activity_start_time"],
            data["activity_end_date"],
            data["activity_end_time"],
            data["height"]
        ])
    return jsonify({"status": "saved"}), 200


def _check_idle():
    now = datetime.utcnow()
    for i, ts in last_seen.items():
        if ts and (now - ts).total_seconds() > 10:
            print(
                f"ESP{i} hasn't sent data for >10 s (last at {ts.isoformat()}Z)"
            )

logging.getLogger("werkzeug").setLevel(logging.WARNING)

if __name__ == "__main__":
    init_csv_files()

    scheduler = BackgroundScheduler(daemon=True)
    scheduler.add_job(_check_idle, "interval", seconds=2)
    scheduler.start()

    # Development: threaded server on all interfaces.
    # Production:  `$ pip install gunicorn gevent` then
    #   $ gunicorn -k gevent -w 2 -b 0.0.0.0:5000 app:app
    app.run(host="0.0.0.0", port=5000, threaded=True)
