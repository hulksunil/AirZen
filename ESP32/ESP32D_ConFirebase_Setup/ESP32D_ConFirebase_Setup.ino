#include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"
#include "SensorData.h"
#include <time.h>
#include "GP2Y1010AU0F_DustSensor.h"

const char* ntpServer = "pool.ntp.org";  // NTP server
const long gmtOffset_sec = -18000;            // Adjust for your timezone (e.g., GMT+0)
const int daylightOffset_sec = 0;     // Daylight saving time offset (if applicable)


void setup() {
  Serial.begin(19200);
  //We should also set the attenuation level of the GPIO 34 ADC PIN to read higher voltages safely. 
  analogSetPinAttenuation(34, ADC_11db); //Increasing the attenuation of the ADC PIN.
  pinMode(34, INPUT); // GPIO 34 (the ADC PIN for the analog reading of VO) is set as an input.
  pinMode(13, OUTPUT); // GPIO 13 (the LED PIN for the IR LED with diode pulse) is set as an output.
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
        BMEAltitude(),   // Get altitude reading
        readDensity()   // Get Dust reading
    );

    // Call sendFB with the SensorData object
    sendFB(sensorData);
    delay(10000);  // Add a delay as needed to control data transmission frequency
}
