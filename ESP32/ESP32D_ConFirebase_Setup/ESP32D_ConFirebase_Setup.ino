#include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"
#include "SensorData.h"
#include <time.h>
#include "GP2Y1010AU0F_DustSensor.h"


extern void checkIaqSensorStatus(void);
extern Bsec iaqSensor;


//For the calibration. This is used with the BSEC_SAMPLE_RATE_LP, so the values will be read every 3 seconds. 
//We use millis here for a simple timer on when to end the function.
unsigned long startTime;
const unsigned long calibrationTime = 5.5 * 60 * 1000; // 5 minutes in milliseconds

//For the Firebase readings. We use the millis here to tell Arduino how often to send the data to Firebase. This should be "unblocked"; no interuptions to the sensor code.
unsigned long lastRead;
const unsigned long ReadSpace = 10 * 1000; // 10 seconds in milliseconds



const char* ntpServer = "pool.ntp.org";  // NTP server
const long gmtOffset_sec = -18000;            // Adjust for your timezone (e.g., GMT+0)
const int daylightOffset_sec = 0;     // Daylight saving time offset (if applicable)


void setup() {
  Serial.begin(115200);
  analogSetPinAttenuation(34, ADC_11db); //Increasing the attenuation of the ADC PIN.
  pinMode(34, INPUT); // GPIO 34 (the ADC PIN for the analog reading of VO) is set as an input.
  pinMode(13, OUTPUT); // GPIO 13 (the LED PIN for the IR LED with diode pulse) is set as an output.
  delay(3000);
  

  // Connect to the WiFi
  WIFIstart();

 // Set up NTP for time synchronization
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Time synchronized.");

  //Connect to Firebase
  connectFB();
  
  // Initialize the BME680 sensor
  BME_Start();

  //Creating our initial start time for calibration
  startTime = millis();

  Serial.println("Starting the calibration period. Will take about 5min 30 sec");

while (millis() - startTime < calibrationTime) {
  if (iaqSensor.run()) {
  BMETemp();
  BMEHumidity();
  BMEco2();
  BMEVOC();
  BMEAQI();
 // BMEIQA_ACCURACY();
  }
  
}
  Serial.println("The calibration step is complete. Going to send data to Firebase at longer intervals");

}


void loop() {
    // Create and populate a SensorData object
    // Call the iaqSensor.run() method once in the loop to update sensor data
    if (iaqSensor.run()) {

      float temp = BMETemp();
      float hum = BMEHumidity();
      float co2 = BMEco2();
      float voc = BMEVOC();
      float IAQ = BMEAQI();
     // float acc = BMEIQA_ACCURACY();

    //Creating our timer for sending the BME680 data
    if(millis() - lastRead >= ReadSpace) {

      lastRead = millis(); //Resetting the timer.

      Serial.println("__________________________");
      Serial.println("Proper data block to be sent");
  
      SensorData sensorData(
        temp,      // Get temperature reading
        hum,  // Get humidity reading
        co2,  // Get CO2 reading
        voc,       // Get VOC reading
        IAQ,  // Get Dust reading
       // acc     // Get AQI
       readDensity()
    );

       Serial.println("Data block finsihed");
       Serial.println("__________________________");
    
    // Call sendFB with the SensorData object
    sendFB(sensorData);
   // delay(10000);  // Add a delay as needed to control data transmission frequency
    }
    else {
       checkIaqSensorStatus();
    }
}
}
