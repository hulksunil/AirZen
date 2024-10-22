#include "BME680.h"
#define SEALEVELPRESSURE_HPA (1013.25)

//Here is our functions for our BME680 sensor. We'll call these functions to send the data to firebase.

Adafruit_BME680 myBME; //We need to create a BME object to store/send data to firebase

//Starting the BME sensor. If it's not detected, then a message will appear.
void BME_Start(){

  if(!myBME.begin(0x76)) { //If the BME680 is not detected
    Serial.println("Could not find the BME sensor.");
    while(1); //While true, this loop will run infinitely.
  }

  else if(myBME.begin(0x76)) { //If the BME680 is detected
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

 float BMEGas() {
  float gas = myBME.gas_resistance() / 1000.0;
  Serial.print("Gas Resistance = ");
  Serial.println(gas);
 // Serial.println(" kOhms");
  return gas;
 }

 float BMEAltitude() {
  float altitude = myBME.readAltitude(SEALEVELPRESSURE_HPA);
  Serial.print("Altitude = ");
  Serial.println(altitude);
 // Serial.println(" m");
  return altitude;
 }






