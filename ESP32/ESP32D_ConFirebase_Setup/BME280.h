#ifndef BME280_H
#define BME280_H

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>


void BME_Start();
float BMETemp();
float BMEHumidity();
float BMEPressure();

#endif // BME280_H
