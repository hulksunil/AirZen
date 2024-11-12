#ifndef BME680_H
#define BME680_H

#include "bsec.h"


float BMETemp();
float BMEHumidity();
float BMEco2();
float BMEVOC();
float BMEAQI();
void BME_Start();

#endif // BME680_H