Adjust the SSID and password to match with the RPI's hotspot

Arduino IDE
• Requires Arduino IDE 1.8.19 or newer
• Download: https://www.arduino.cc/en/software

Download necesarry drivers for ESP32 From the internet

Add ESP32 board support
a. Arduino → File → Preferences
b. In Additional Boards Manager URLs add:
'https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json'
c. Click OK
d. Tools → Board → Boards Manager…
e. Search for esp32 and install esp32 by Espressif Systems

Select board & port
• Tools → Board → ESP32 Dev Module (or your specific ESP32)
• Tools → Port → select your ESP32’s COM port

Install required libraries
• Wire (built-in)
• MPU6050 by Electronic Cats
• DFRobot QMC5883 by DFRobot
• WiFi (built-in)
• HTTPClient (built-in)

(Use Sketch → Include Library → Manage Libraries…)

Configure user settings
• In the sketch, set your Wi-Fi credentials:
const char* WIFI_SSID = 'your-ssid';
const char* WIFI_PASS = 'your-password';
• Set DEVICE_ID (1–5) to match server endpoint /espX/data.
• Set PI_HOST to your Raspberry Pi’s IP address (default '192.168.4.1').
• Adjust SAMPLE_PERIOD (ms) as needed.

Wire sensors & ESP32
• SDA → GPIO 21, SCL → GPIO 22
• MPU6050 #1: AD0 → GND, VCC → 3.3 V, GND → GND
• MPU6050 #2: AD0 → 3.3 V (SDA/SCL shared)
• QMC5883 compass: VCC → 3.3 V, GND → GND

Upload & run
Connect ESP32 via USB

Open Serial Monitor at 115200 baud

Operation
• On boot, ESP32 connects to Wi-Fi and initializes sensors.
• In loop(), it reads both MPU6050s and the QMC5883, builds a JSON string,
then sends an HTTP POST to:
http://<PI_HOST>:5000/esp<DEVICE_ID>/data
• It prints POST status and the JSON payload to Serial Monitor.