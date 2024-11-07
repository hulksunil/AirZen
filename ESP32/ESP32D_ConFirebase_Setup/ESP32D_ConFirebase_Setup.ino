#include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"

extern void checkIaqSensorStatus(void);
extern Bsec iaqSensor;

void setup() {
  Serial.begin(115200);
  delay(5000);

  // Connect to the WiFi
  WIFIstart();

  // Connect to Firebase
  connectFB();

  // Initialize the BME680 sensor
  BME_Start();
}

void loop() {
  // Call the iaqSensor.run() method once in the loop to update sensor data
  if (iaqSensor.run()) {
    // Call the functions to get sensor data
    float temperature = BMETemp();  // Get the temperature
    float humidity = BMEHumidity();  // Get the humidity
    float co2 = BMEco2();  // Get the CO2 equivalent
    float gas = BMEVOC();  // Get the VOC equivalent (breath gas)
    float aqi = BMEAQI();  // Get the IAQ (Indoor Air Quality)
    float altitude = BMEAltitude();  // Get the altitude based on pressure

    // Now that we have the sensor data, pass it to the sendFB function
    sendFB(temperature, humidity, co2, gas, aqi, altitude);
  } else {
    checkIaqSensorStatus();
  }

  // Add a small delay to avoid flooding the Serial monitor and Firebase
  delay(1000);
}
