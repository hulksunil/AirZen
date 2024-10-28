#ifndef BME680_H
#define BME680_H

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME680.h>


void BME_Start();
float BMETemp();
float BMEHumidity();
float BMEPressure();
float BMEGas();
float BMEAltitude();

#endif // BME680_H