#include <EEPROM.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

ESP8266WebServer server(80);

const char* ap_ssid = "ESP_AP_1";
const char* ap_password = "";

const char* mqtt_server = "e5dfb536a0a1462aa9e5c9a81159c869.s1.eu.hivemq.cloud";  // HiveMQ broker address
const int mqtt_port = 8883;                                                       // Secure MQTT port
const char* mqtt_user = "controller_esp_1";                                       // MQTT username
const char* mqtt_password = "Controller_esp_1";                                   // MQTT password

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 0, 60000);  // NTP client, 60-second update interval

WiFiClientSecure espClient;
PubSubClient client(espClient);

String deviceId;  // To store the device ID from EEPROM

void startAPMode();
void handleConfig();
void handleId();
void reconnectMQTT();
void publishSensorData();
String readStringFromEEPROM(int startAddress, int length);
void writeStringToEEPROM(int startAddress, const String& data);

void setup() {
  Serial.begin(115200);
  EEPROM.begin(512);

  writeStringToEEPROM(1, "252");

  // Initialize the MQTT client
  client.setServer(mqtt_server, mqtt_port);
  espClient.setInsecure();  // Use this for testing purposes

  // Read stored Wi-Fi credentials and device ID
  String storedSSID = readStringFromEEPROM(100, 32);
  String storedPassword = readStringFromEEPROM(200, 32);
  deviceId = readStringFromEEPROM(1, 32);

  if (storedSSID.length() > 0 && storedPassword.length() > 0) {
    

    WiFi.begin(storedSSID.c_str(), storedPassword.c_str());

    unsigned long startAttemptTime = millis();

    while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 10000) {  // 10 seconds timeout
      delay(500);
    }
    if (WiFi.status() == WL_CONNECTED) {
      // Attempt to connect to the MQTT broker
      timeClient.begin();  // Start NTP client
      reconnectMQTT();
    } else {
      startAPMode();
    }
  } else {
    startAPMode();
  }
}

unsigned long previousMillis = 0;  // stores the last time the values were printed
const long interval = 10000;        // interval to print values (10000 milliseconds or 10 seconds)

void loop() {
  server.handleClient();

  unsigned long currentMillis = millis();
  if (WiFi.status() == WL_CONNECTED) {
    if (currentMillis - previousMillis >= interval) {
      previousMillis = currentMillis;
      publishSensorData();
    }

    client.loop();  // Ensure MQTT client is running
  }
}

void startAPMode() {
  WiFi.softAP(ap_ssid, ap_password);

  // Set up the web server routes
  server.on("/config", HTTP_POST, handleConfig);
  server.on("/id", HTTP_POST, handleId);
  server.begin();
}

void handleConfig() {
  if (server.hasArg("ssid") && server.hasArg("password")) {
    String ssid = server.arg("ssid");
    String pass = server.arg("password");

    if (ssid.length() > 0 && pass.length() > 0) {
      writeStringToEEPROM(100, ssid);
      writeStringToEEPROM(200, pass);
      EEPROM.commit();

      String response = "Credentials saved! Restarting ESP...";
      server.send(200, "text/plain", response);

      delay(3000);

      // Try connecting to the new Wi-Fi network
      WiFi.begin(ssid.c_str(), pass.c_str());

      unsigned long startAttemptTime = millis();

      while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 10000) {  // 10 seconds timeout
        delay(500);
      }

      if (WiFi.status() == WL_CONNECTED) {
        timeClient.begin();  // Start NTP client after Wi-Fi is connected
        reconnectMQTT();  // Try connecting to MQTT after connecting to Wi-Fi
      } else {
      }

      ESP.restart();  // Restart the ESP to apply the new settings
    } else {
      server.send(400, "text/plain", "Invalid SSID or Password");
    }
  } else {
    server.send(400, "text/plain", "SSID and Password not provided");
  }
}

void handleId() {
  if (server.hasArg("id")) {
    String idS = server.arg("id");

    if (idS.length() > 0) {
      writeStringToEEPROM(1, idS);
      EEPROM.commit();

      String response = "Id saved!";
      server.send(200, "text/plain", response);

      delay(3000);
      ESP.restart();  // Restart the ESP to apply the new settings
    } else {
      server.send(400, "text/plain", "Invalid id");
    }
  } else {
    server.send(400, "text/plain", "Id not provided");
  }
}

void publishSensorData() {
  if (!client.connected()) {
    reconnectMQTT();
  }

  // Create a JSON document
  StaticJsonDocument<1024> doc;

  doc["id"] = deviceId;

  timeClient.update();

  long timestamp = timeClient.getEpochTime();  // Get real-time timestamp
  doc["timestamp"] = timestamp;

  JsonArray temperatures = doc.createNestedArray("temperatures");

  // Clear any existing data in the serial buffer
  while (Serial.available() > 0) {
    Serial.read();
  }

  // Request temperature data from the PCB
  Serial.println("REQ_TEMPS");  // Send a command to the PCB to request temperature data

  // Wait for the response from the PCB
  unsigned long startMillis = millis();
  while (Serial.available() == 0) {
    if (millis() - startMillis > 5000) {  // 5 seconds timeout
      return;
    }
  }

  // Read the temperature data from the PCB
  String tempData = Serial.readStringUntil('\n');  // the data is sent as a single line

  // Parse the temperature data (CSV format from PCB: "23.4,24.5,22.8,...")
  int commaIndex = -1;
  for (int i = 0; i < 16; i++) {
    commaIndex = tempData.indexOf(',');
    float tempValue;

    if (commaIndex == -1 && i == 15) {
      // This is the last value (no comma at the end)
      tempValue = tempData.toFloat();
    } else {
      tempValue = tempData.substring(0, commaIndex).toFloat();
      tempData = tempData.substring(commaIndex + 1);
    }

    temperatures.add(tempValue);
  }

  // Serialize JSON document to a string
  char buffer[512];
  size_t n = serializeJson(doc, buffer);

  // Publish the JSON string to the MQTT topic
  client.publish("esp8266/temperature", buffer, n);
}


void reconnectMQTT() {
  int retries = 0;
  while (!client.connected() && retries < 5) {
    // Attempt to connect
    if (client.connect(deviceId.c_str(), mqtt_user, mqtt_password)) {
    } else {
      delay(5000);
      retries++;
    }
  }
}

String readStringFromEEPROM(int startAddress, int length) {
  char data[length + 1];  // Buffer to hold the string, +1 for null terminator
  for (int i = 0; i < length; i++) {
    data[i] = EEPROM.read(startAddress + i);
  }
  data[length] = '\0';  // Null-terminate the string
  return String(data);
}

void writeStringToEEPROM(int startAddress, const String& data) {
  for (int i = 0; i < data.length(); i++) {
    EEPROM.write(startAddress + i, data[i]);
  }
  EEPROM.write(startAddress + data.length(), '\0');  // Null-terminate the string in EEPROM
  EEPROM.commit();
}
