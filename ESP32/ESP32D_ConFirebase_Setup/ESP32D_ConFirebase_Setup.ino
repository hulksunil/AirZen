  #include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"
<<<<<<< HEAD
#include "SensorData.h"
#include <time.h>
#include "GP2Y1010AU0F_DustSensor.h"
=======

extern void checkIaqSensorStatus(void);
extern Bsec iaqSensor;
>>>>>>> 3be74e68763a3ad6e874cae6669b9e719c22f96e

const char* ntpServer = "pool.ntp.org";  // NTP server
const long gmtOffset_sec = -18000;            // Adjust for your timezone (e.g., GMT+0)
const int daylightOffset_sec = 0;     // Daylight saving time offset (if applicable)


void setup() {
  Serial.begin(115200);
  delay(5000);

  // Connect to the WiFi
  WIFIstart();

 // Set up NTP for time synchronization
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Time synchronized.");

  //Connect to Firebase
  connectFB();
  
  // Initialize the BME680 sensor
  BME_Start();
}

void loop() {
    // Create and populate a SensorData object
    SensorData sensorData(
        BMETemp(),      // Get temperature reading
        BMEHumidity(),  // Get humidity reading
        BMEPressure(),  // Get pressure reading
        BMEGas(),       // Get gas reading
        BMEAltitude(),   // Get altitude reading
        readDensity()   // Get Dust reading
    );

    // Call sendFB with the SensorData object
    sendFB(sensorData);
    delay(10000);  // Add a delay as needed to control data transmission frequency

//   // Call the iaqSensor.run() method once in the loop to update sensor data
//   if (iaqSensor.run()) {
//     // Call the functions to get sensor data
//     float temperature = BMETemp();  // Get the temperature
//     float humidity = BMEHumidity();  // Get the humidity
//     float co2 = BMEco2();  // Get the CO2 equivalent
//     float voc = BMEVOC();  // Get the VOC equivalent (breath gas)
//     float aqi = BMEAQI();  // Get the IAQ (Indoor Air Quality)
//     float altitude = BMEAltitude();  // Get the altitude based on pressure

//     // Now that we have the sensor data, pass it to the sendFB function
//     sendFB(temperature, humidity, co2, voc, aqi, altitude);
//   } else {
//     checkIaqSensorStatus();
//   }

//   // Add a small delay to avoid flooding the Serial monitor and Firebase
//   delay(1000);
// >>>>>>> 3be74e68763a3ad6e874cae6669b9e719c22f96e
}
