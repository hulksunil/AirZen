  #include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"
#include "SensorData.h"
#include <time.h>
#include "GP2Y1010AU0F_DustSensor.h"


extern void checkIaqSensorStatus(void);
extern Bsec iaqSensor;


const char* ntpServer = "pool.ntp.org";  // NTP server
const long gmtOffset_sec = -18000;            // Adjust for your timezone (e.g., GMT+0)
const int daylightOffset_sec = 0;     // Daylight saving time offset (if applicable)


void setup() {
  Serial.begin(115200);
  analogSetPinAttenuation(34, ADC_11db); //Increasing the attenuation of the ADC PIN.
  pinMode(34, INPUT); // GPIO 34 (the ADC PIN for the analog reading of VO) is set as an input.
  pinMode(13, OUTPUT); // GPIO 13 (the LED PIN for the IR LED with diode pulse) is set as an output.
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
    // Call the iaqSensor.run() method once in the loop to update sensor data
    if (iaqSensor.run()) {
    SensorData sensorData(
        BMETemp(),      // Get temperature reading
        BMEHumidity(),  // Get humidity reading
        BMEco2(),  // Get CO2 reading
        BMEVOC(),       // Get VOC reading
        readDensity(),  // Get Dust reading
         BMEAQI()      // Get AQI
    );
    
    // Call sendFB with the SensorData object
    sendFB(sensorData);
    delay(10000);  // Add a delay as needed to control data transmission frequency
    }
    else {
       checkIaqSensorStatus();
    }
}
