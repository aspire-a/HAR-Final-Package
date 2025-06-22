Script to match the sensor and label data based on timestamp. Copy the files genetaed by the main python code of RPI5 inside this folder(all of the csv files) then run the program which will create the categorized versions of the csv files.

1. Python Interpreter
   ------------------
   • Requires Python 3.6 or newer  

2. (Optional) Create & Activate Virtual Environment
   ------------------------------------------------
     $ python3 -m venv venv  
     $ source venv/bin/activate  

3. Upgrade pip (if using a venv)
   ------------------------------
     $ pip install --upgrade pip  


4. Prepare Your CSV Files
   ------------------------
   • Place your existing esp1.csv … esp5.csv and activity.csv in the same folder as the script.  
   • Ensure that activity.csv has the headers:
     ```
     activity_label,activity_start_date,activity_start_time,activity_end_date,activity_end_time,height
     ```

5. Run the Script
   ----------------
   From the directory containing merge.py (or whatever you name the file):
     $ python merge.py  

   • It will read each esp<i>.csv and activity.csv.  
   • For each ESP file it finds, it will create categorized_esp<i>.csv with two new columns:  
    activity_label and height.

6. Output
   --------
   • categorized_esp1.csv, …, categorized_esp5.csv (only for the ESP files that exist).  
   • Each row whose timestamp falls within an activity window (excluding the first and last 2 s) will be copied along with its matching activity label and height.