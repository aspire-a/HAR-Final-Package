/*********************************************************************
 *  ESP32-WROOM-32D  |  Wi-Fi Telemetry Client (HTTP POST, JSON)
 *  Uses: WiFi.h, HTTPClient.h, Wire.h, MPU6050.h, DFRobot_QMC5883.h
 *********************************************************************/

#include <WiFi.h>
#include <HTTPClient.h>
#include <esp_timer.h>


#include <Wire.h>
#include <MPU6050.h>
#include <DFRobot_QMC5883.h>

/* ------------ user settings -------------------------------------- */
const char* WIFI_SSID = "HAR";
const char* WIFI_PASS = "harharhar";

#define DEVICE_ID 3                  // 1-5 ─ makes endpoint /espX/data
const char* PI_HOST = "192.168.4.1"; // AP default; change if different
const uint16_t SAMPLE_PERIOD = 100;

/* ------------ sensors -------------------------------------------- */
MPU6050 mpu1(0x68);      // AD0 → GND
MPU6050 mpu2(0x69);      // AD0 → VCC
DFRobot_QMC5883 compass(&Wire, /*I2C*/QMC5883_ADDRESS);

/* ------------ helpers -------------------------------------------- */
void connectWiFi()
{
  if (WiFi.status() == WL_CONNECTED) return;

  Serial.printf("\nConnecting to \"%s\"  ", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  uint32_t retry = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print('.');
    if (++retry > 20) {             // ≈10 s timeout
      Serial.println("\nWi-Fi failed, rebooting");
      ESP.restart();
    }
  }

  Serial.printf("\nWi-Fi OK  |  IP: %s\n", WiFi.localIP().toString().c_str());
}

bool postJSON(const String& payload)
{
  connectWiFi();                    // auto-reconnect if needed

  HTTPClient http;
  String url = String("http://") + PI_HOST + ":5000/esp" + DEVICE_ID + "/data";

  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  int code = http.POST(payload);    // blocking - few ms on LAN
  http.end();

  if (code == 200) {
    Serial.println("POST OK");
    return true;
  }
  Serial.printf("POST failed (%d)\n", code);
  return false;
}

/* ------------ Arduino lifecycle ---------------------------------- */
void setup()
{
  Serial.begin(115200);
  Wire.begin(21, 22);               // SDA, SCL on many ESP32 dev-boards

  connectWiFi();

  /* Init sensors */
  mpu1.initialize();
  mpu2.initialize();
  if (!mpu1.testConnection() || !mpu2.testConnection() || !compass.begin()) {
    Serial.println("Sensor init error - halt");
    for (;;) delay(1000);
  }
  float decl = (4.0 + 26.0 / 60.0) / (180.0 / PI);
  compass.setDeclinationAngle(decl);

  Serial.println("Setup complete");
}

void loop()
{
  /* ------------ read sensors ---------------- */
  int16_t ax1, ay1, az1, gx1, gy1, gz1;
  int16_t ax2, ay2, az2, gx2, gy2, gz2;

  mpu1.getMotion6(&ax1, &ay1, &az1, &gx1, &gy1, &gz1);
  mpu2.getMotion6(&ax2, &ay2, &az2, &gx2, &gy2, &gz2);

  sVector_t mag = compass.readRaw();
  compass.getHeadingDegrees();      // populates mag.HeadingDegress

  /* ------------ build JSON ------------------- */
  String json = String("{") +
      "\"mpu1\":{\"ax\":" + String(ax1) +
      ",\"ay\":" + String(ay1) +
      ",\"az\":" + String(az1) +
      ",\"gx\":" + String(gx1) +
      ",\"gy\":" + String(gy1) +
      ",\"gz\":" + String(gz1) + "}," +
      "\"mpu2\":{\"ax\":" + String(ax2) +
      ",\"ay\":" + String(ay2) +
      ",\"az\":" + String(az2) +
      ",\"gx\":" + String(gx2) +
      ",\"gy\":" + String(gy2) +
      ",\"gz\":" + String(gz2) + "}," +
      "\"HMCx\":" + String(mag.XAxis) +
      ",\"HMCy\":" + String(mag.YAxis) +
      ",\"HMCz\":" + String(mag.ZAxis) +
      ",\"Heading\":" + String(mag.HeadingDegress) +
      "}";

  /* ------------ push to Raspberry Pi --------- */
  postJSON(json);
  Serial.println(json);

  delay(SAMPLE_PERIOD);
}
