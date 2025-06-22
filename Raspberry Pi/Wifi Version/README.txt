WIFI version of our HAR Prototype which uses Flask Server with HTTP requests. This is designed for Raspberry Pi 5. Before Running Create a PSA2 locked Hotspot On Wifi and connect Raspberry Pi to its own hotspot. This will broadcast the hotspot. Make the name of the Hotspot HAR with password being "harhar". If you want to change the name and password change the ESP codes to connect to the new hotspot.

1. Python Interpreter
   ------------------
   • Requires Python 3.8 or newer  

2. (Recommended) Create & Activate Virtual Environment
   ---------------------------------------------------
     $ python3 -m venv venv  
     $ source venv/bin/activate  

3. Upgrade pip
   -------------
     $ pip install --upgrade pip

4. Install Dependencies
   --------------------
   The app uses these third-party packages:
     • Flask  
     • APScheduler  
     • Gunicorn
     • gevent

   Install them with:
     $ pip install Flask APScheduler gunicorn gevent


6. Initialize CSV Files & Start Server
   ------------------------------------
   From the directory containing 'main.py':
     $ python main.py  
