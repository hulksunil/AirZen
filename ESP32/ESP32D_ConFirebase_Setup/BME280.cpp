#include "BME280.h"

//Here is our functions for our BME280 sensor. We'll call these functions to send the data to firebase.

Adafruit_BME280 myBME; //We need to create a BME object to store/send data to firebase

//Starting the BME sensor. If it's not detected, then a message will appear.
void BME_Start(){

  if(!myBME.begin(0x76)) { //If the BME280 is not detected
    Serial.println("Could not find the BME sensor.");
    while(1); //While true, this loop will run infinitely.
  }

  else if(myBME.begin(0x76)) { //If the BME280 is detected
    Serial.println("Connected to the BME sensor!");
    delay(1000); //Wait 1 second afterwards.
  }
}

//The other functions will each represent 1 parameter the sensor offers. 
//The libraries already give packaged functions that read the data. To represent the data, we'll use float.

float BMETemp() { 
  float temperature = myBME.readTemperature();
  Serial.print("Temperature = ");
  Serial.println(temperature);
 // Serial.println(" °C");
  return temperature;
}


float BMEHumidity() { 
  float humidity = myBME.readHumidity();
  Serial.print("Humidity = ");
  Serial.println(humidity);
 // Serial.println(" %");
   return humidity;
}

 float BMEPressure() {
  float pressure = myBME.readPressure() / 100.0F;
  Serial.print("Pressure = ");
  Serial.println(pressure);
 // Serial.println(" %");
  return pressure;
 }




