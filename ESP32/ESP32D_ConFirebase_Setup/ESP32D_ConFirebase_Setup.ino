#include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"
#include "SensorData.h"
#include <time.h>

const char* ntpServer = "pool.ntp.org";  // NTP server
const long gmtOffset_sec = -18000;            // Adjust for your timezone (e.g., GMT+0)
const int daylightOffset_sec = 0;     // Daylight saving time offset (if applicable)
void setup() {
  Serial.begin(115200);
  delay(5000);

  //Connect to the WIFI:
  WIFIstart();

 // Set up NTP for time synchronization
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Time synchronized.");
  
  //Connect to Firebase
  connectFB();

  //Confirm the BME sensor is connected
  BME_Start();

}

//In the loop, we'll send the sensor data to the database.
void loop() {
    // Create and populate a SensorData object
    SensorData sensorData(
        BMETemp(),      // Get temperature reading
        BMEHumidity(),  // Get humidity reading
        BMEPressure(),  // Get pressure reading
        BMEGas(),       // Get gas reading
        BMEAltitude()   // Get altitude reading
    );

    // Call sendFB with the SensorData object
    sendFB(sensorData);
    delay(5000);  // Add a delay as needed to control data transmission frequency
}


// void loop() {
  
//  float temperature;
//  float humidity;
//  float pressure;
//  float gas;
//  float altitude;
 
//   //The we'll call the firebase function and get the latest sensor readings.
//   sendFB(temperature, humidity, pressure, gas, altitude);
// }