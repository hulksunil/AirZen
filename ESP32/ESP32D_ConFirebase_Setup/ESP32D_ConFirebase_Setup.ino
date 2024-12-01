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
const unsigned long calibrationTime = 5.5 * 60 * 1000; // 5 minutes and 30 seconds

//For the Firebase readings. We use the millis here to tell Arduino how often to send the data to Firebase. This should be "unblocked"; no interuptions to the sensor code.
unsigned long lastRead;
const unsigned long ReadSpace = 10 * 1000; // 10 seconds



const char* ntpServer = "pool.ntp.org";  // NTP server
const long gmtOffset_sec = -18000;            // Adjust for your timezone (e.g., GMT+0)
const int daylightOffset_sec = 0;     // Daylight saving time offset (if applicable)


void setup() {
  Serial.begin(115200);
  analogSetPinAttenuation(34, ADC_11db); //Increasing the attenuation of the ADC PIN. This should cover the min/max voltage rage from the dust sensor.
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
  
  //Initialize the BME680 sensor
  BME_Start();

  //Creating our initial start time for calibration
  startTime = millis();

  Serial.println("Starting the calibration period. Will take about 5min 30 sec");
  //The calibration will run now. The C02, VOC, and IAQ values will become non-constant after approximately 4 minutes and 45 seconds. This was tested multiple times to be sure.
  //The accuracy was also tested multiple times before (using the iaqsensor.iaqaccuracy function). The accuracy always changed from 0 to 1, which indicated the values were moderately calibrated.
  
while (millis() - startTime < calibrationTime) {
  if (iaqSensor.run()) {
  BMETemp();
  BMEHumidity();
  BMEco2();
  BMEVOC();
  BMEAQI();

  }
  
}
  Serial.println("The calibration step is complete. Going to send data to Firebase at longer intervals");

}

//Now that the values are calibrated, we can send the data to Firebase. However, the BSEC library requires the values to still run every 3 seconds, or we loose the calibarted data.
//The solution is to continue outputting values every 3 seconds, but only sending the data to Firebase every 10 seconds.
//The reason we chosen 10 seconds was because the current and past values directories have time to get populated with new data. When the time was less, we sometimes got token errors.

void loop() {
    // Create and populate a SensorData object
    // Call the iaqSensor.run() method once in the loop to update sensor data every 3 seconds (done automatically becuase of the BSEC_SAMPLE_RATE_LP).
    //We'll store this data in local variables, which can be sent to Firebase everytime the timer reaches 10 seconds.
    if (iaqSensor.run()) {

      float temp = BMETemp();
      float hum = BMEHumidity();
      float co2 = BMEco2();
      float voc = BMEVOC();
      float IAQ = BMEAQI();
     

    //Creating our timer for sending the BME680 data
    if(millis() - lastRead >= ReadSpace) { //If the space between readings exceed 10 seconds, reset the timer and send the values to Firebase.

      lastRead = millis(); //Resetting the timer.

      Serial.println("__________________________");
      Serial.println("Proper data block to be sent");
  
      SensorData sensorData(
        temp, //Get the temperature reading
        hum,  //Get the humidity reading
        co2,  //Get the CO2 reading
        voc,  //Get the VOC reading
        IAQ,  //Get the IAQ reading
      
       readDensity() //Get the Dust reading
    );

       Serial.println("Data block finsihed");
       Serial.println("__________________________");
    
    // Call sendFB with the SensorData object
    sendFB(sensorData);
   
    }
    else {
       checkIaqSensorStatus();
    }
}
}
