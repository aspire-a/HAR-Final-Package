This file is the BLE version of our HAR Prototype. This file was designed run on 16gb Raspberry Pi 5

1. Install a compatible Python interpreter
   ----------------------------------------
   • This script requires Python 3.8 or newer.

2. (Optional but recommended) Create and activate a virtual environment
   --------------------------------------------------------------------
    $ python3 -m venv venv
    $ source venv/bin/activate

3. Upgrade pip
   -------------
   $ pip install --upgrade pip

4. Install required Python packages
   ---------------------------------
   The only third-party package needed is **bleak**.
     $ pip install bleak

5. Ensure RPI's Bluetooth is turned on
   ---------------------------------------------
   • Make sure BlueZ is installed and running.

6. Run the script
   ----------------
   From the directory containing your `.py` file:
     $ python your_script_name.py