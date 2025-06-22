import csv
from datetime import datetime, timedelta
import os

def parse_datetime(date_str, time_str):
    return datetime.strptime(f"{date_str} {time_str}", "%Y-%m-%d %H:%M:%S")

def merge_esp_with_activity(esp_csv_path, activity_csv_path, categorized_csv_path):
    activities = []
    with open(activity_csv_path, mode="r", newline="") as f_act:
        reader = csv.DictReader(f_act)
        for row in reader:
            try:
                start_dt = parse_datetime(row["activity_start_date"], row["activity_start_time"])
                end_dt   = parse_datetime(row["activity_end_date"], row["activity_end_time"])
                label    = row["activity_label"]
                height   = row["height"]
                activities.append((start_dt, end_dt, label, height))
            except Exception as e:
                print(f"Error parsing activity row in {activity_csv_path}: {e}")
                continue

    with open(esp_csv_path, mode="r", newline="") as f_esp, \
         open(categorized_csv_path, mode="w", newline="") as f_out:

        reader_esp = csv.DictReader(f_esp)
        fieldnames = reader_esp.fieldnames + ["activity_label", "height"]
        writer_out = csv.DictWriter(f_out, fieldnames=fieldnames)
        writer_out.writeheader()

        for row in reader_esp:
            try:
                sensor_dt = parse_datetime(row["Date"], row["Time"])
            except Exception as e:
                print(f"Error parsing datetime in {esp_csv_path}: {e}")
                continue

            matched_label = None
            matched_height = None

            for (start_dt, end_dt, label, height) in activities:
                excluded_start = start_dt + timedelta(seconds=2)
                excluded_end   = end_dt - timedelta(seconds=2)

                if excluded_start > excluded_end:
                    continue

                if excluded_start <= sensor_dt <= excluded_end:
                    matched_label = label
                    matched_height = height
                    break

            if matched_label is not None:
                row["activity_label"] = matched_label
                row["height"] = matched_height
                writer_out.writerow(row)

def main():
    activity_csv_path = "activity.csv"
    if not os.path.exists(activity_csv_path):
        print("Error: activity.csv not found.")
        return

    for i in range(1, 6):
        esp_csv = f"esp{i}.csv"
        categorized_csv = f"categorized_esp{i}.csv"

        if not os.path.exists(esp_csv):
            print(f"Warning: {esp_csv} not found. Skipping.")
            continue

        print(f"Processing {esp_csv} -> {categorized_csv}")
        merge_esp_with_activity(esp_csv, activity_csv_path, categorized_csv)

if __name__ == "__main__":
    main()
