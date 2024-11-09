#include <WiFi.h>
#include "WIFI_Connection.h"
#include "BME680.h"
#include "ESP32_Firebase.h"
#include "GP2Y1010AU0F_DustSensor.h"

void setup() {
  Serial.begin(19200);
  //We should also set the attenuation level of the GPIO 34 ADC PIN to read higher voltages safely. 
  analogSetPinAttenuation(34, ADC_11db); //Increasing the attenuation of the ADC PIN.
  pinMode(34, INPUT); // GPIO 34 (the ADC PIN for the analog reading of VO) is set as an input.
  pinMode(13, OUTPUT); // GPIO 13 (the LED PIN for the IR LED with diode pulse) is set as an output.
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
 float dustDensity;
 
  //The we'll call the firebase function and get the latest sensor readings.
  sendFB(temperature, humidity, pressure, gas, altitude, dustDensity);
}