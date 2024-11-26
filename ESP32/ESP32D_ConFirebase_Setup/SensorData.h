// SensorData.h
#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H
//Provides an object that stores all the data read by the sensor
class SensorData {
public:
    float temperature;
    float humidity;
    float co2;
    float voc; //gas
    float aqi;
    float dustDensity;

    SensorData(float temp = 0, float hum = 0, float co2 = 0, float v = 0, float iaqi =0, float dustdensity =0)
        : temperature(temp), humidity(hum), co2(co2), voc(v), aqi(iaqi), dustDensity(dustdensity) {}
};

#endif // SENSOR_DATA_H