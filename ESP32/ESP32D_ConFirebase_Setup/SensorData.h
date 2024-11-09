// SensorData.h
#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H
//Provides an object that stores all the data read by the sensor
class SensorData {
public:
    float temperature;
    float humidity;
    float pressure;
    float gas;
    float altitude;

    SensorData(float temp = 0, float hum = 0, float pres = 0, float g = 0, float alt = 0)
        : temperature(temp), humidity(hum), pressure(pres), gas(g), altitude(alt) {}
};

#endif // SENSOR_DATA_H