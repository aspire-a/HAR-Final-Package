Adjust the I2C address for QMC5883 change it for HMC5883 or QMC5883

Install Arduino IDE
• Download and install Arduino IDE 1.8.19 or newer from https://www.arduino.cc/en/software

Download ESP drivers for your computer

Add ESP32 board support
a. Open Arduino IDE → File → Preferences
b. In the “Additional Boards Manager URLs” field add:
'https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json'
c. Click OK
d. Tools → Board → Boards Manager…
e. Search for esp32 and install esp32 by Espressif Systems

Select your ESP32 board
• Tools → Board → ESP32 Arduino → select ESP32 Dev Module (or your specific board)

Install required libraries
a. Sketch → Include Library → Manage Libraries…
b. In the Library Manager search for and install:
• MPU6050 by Electronic Cats
• DFRobot QMC5883 by DFRobot
(BLE support is included with the ESP32 core)

Wire your sensors and ESP32
• MPU6050 #1: SDA → GPIO 21, SCL → GPIO 22, AD0 → GND, VCC → 3.3 V, GND → GND
• MPU6050 #2: AD0 → 3.3 V (SDA/SCL shared with MPU6050 #1)
• QMC5883 compass: SDA → GPIO 21, SCL → GPIO 22, VCC → 3.3 V, GND → GND

Configure UUIDs
• In the sketch, update SERVICE_UUID and CHARACTERISTIC_UUID to match your mobile app

Upload the sketch
a. Connect your ESP32 via USB
b. Tools → Port → select the correct COM port
c. Click the Upload button (→)

Monitor output
• Tools → Serial Monitor
• Set baud rate to 115200 to view connection status and sensor readings