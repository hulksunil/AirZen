#include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"

void setup() {
  Serial.begin(115200);
  delay(5000);

  //Connect to the WIFI:
  WIFIstart();

  //Connect to Firebase
  connectFB();

  //Confirm the BME sensor is connected
  BME_Start();

}

//In the loop, we'll send the sensor data to the database.
void loop() {
  
 float temperature;
 float humidity;
 float pressure;
 float gas;
 float altitude;
 
  //The we'll call the firebase function and get the latest sensor readings.
  sendFB(temperature, humidity, pressure, gas, altitude);
}

