//WARNING:
//We must be careful with the sensor's analog output!
//The ADC pins are only rated for 3.3V. So, we should scale down the voltage to 3.0V (safe margin) before it reaches GPIO 34. 
//This was done using a voltage divider with the following resistances: R1 = 2.2kOhms , R2 = 3.3kOhms

#include <Arduino.h>
#include "GP2Y1010AU0F_DustSensor.h"

const int sensorPin = 34; //Connected to the VO wire on the sensor. Provides the sensor output readings for dust density
const int ledPin = 13; //Connected to the LED wire on the sensor. Switches and controls the IR sensor inside the GP2Y1010AU0F
const int adcMax = 4095; //This is the standard Max ADC value for an ESP32. Confirmed by testing using analogRead(PIN).
const float Vcc = 5.0; //The voltage of the power supplied to the sensor, which is 5V. 

float readDensity() {

  digitalWrite(ledPin, LOW); //Switch on the IR LED (LOW symbolizes that the IR LED is ON).
  delayMicroseconds(280); //As per the manufacturers datasheet, we should wait at leat 0.28ms before reading the Vo output of the sensor.
  int adc = analogRead(sensorPin); //We read the voltage with our ADC PIN 34. However, this gives an ADC value, NOT a voltage. We have to convert to Vo.
  float v0 = (Vcc * adc / adcMax) / 0.6; //Converting the analog reading to a voltage. Adjust from voltage divider (3/5 = 0.6)

 
  //The manufacturer shows a graph that indicates the linear range of the dust density readings: 0 to 0.5 mg/m^3 and 0 - 3.7V.
  //However, this is a typical graph only, and does not guarantee an accurate calculation. 
  //The min and max voltages were found with the current sensor, and a new linear fitting was made, altering the formula slightly to match the read values.

  float density = (0.103 * v0 - 0.0414) * 1000 ; // Now we can calculate the dust density ug/m^3)
  delayMicroseconds(40); //The extra delay is to account for the remaining amount of time in the pulse width (Total time = 320us. 280 + 40 = 320)
  digitalWrite(ledPin, HIGH);
  delay(1500); //To avoid overlap issues, a delay is included.

  //Accounting for negative values:
  if (density < 0) {
    density = 0;
  }
  Serial.print("Dust Density = ");
  Serial.println(density);
  return density;

}

