Files Included
• activity.csv
• categorized_esp1.csv … categorized_esp5.csv

activity.csv
• Description: Logs each activity session as labeled via the mobile app.
• Columns:
– activity_label — Name of the activity (e.g., walking, sitting)
– activity_start_date — Start date in YYYY-MM-DD
– activity_start_time — Start time in HH:MM:SS
– activity_end_date — End date in YYYY-MM-DD
– activity_end_time — End time in HH:MM:SS
– height — User’s height (in cm)

categorized_espX.csv (for X=1…5)
• Description: Raw sensor readings from ESP X merged with the matching activity label and user height (rows only appear when the timestamp falls within a labeled activity, excluding the first/last 2 s).
• Columns (in order):

mpu1_ax, mpu1_ay, mpu1_az — Accelerometer X, Y, Z from the first MPU6050

mpu1_gx, mpu1_gy, mpu1_gz — Gyroscope X, Y, Z from the first MPU6050

mpu2_ax, mpu2_ay, mpu2_az — Accelerometer X, Y, Z from the second MPU6050

mpu2_gx, mpu2_gy, mpu2_gz — Gyroscope X, Y, Z from the second MPU6050

HMC_x, HMC_y, HMC_z — Magnetometer X, Y, Z from the QMC5883

Heading_degrees — Computed heading (°) from the magnetometer

Date — Measurement date in YYYY-MM-DD

Time — Measurement time in HH:MM:SS

activity_label — Matched activity name (copied from activity.csv)

height — User’s height (copied from activity.csv)

How to Use
Position this entire Dataset folder alongside your analysis or training scripts.

Inspect activity.csv to see all labeled intervals.

Load categorized_espX.csv for each ESP to get time-aligned sensor data with activity labels.

Train models or perform analyses directly on the merged files—you already have both raw channels (18 sensor channels + timestamp) and ground-truth labels (activity & height).