#ifndef ESP32_FIREBASE_H
#define ESP32_FIREBASE_H

#include <Firebase_ESP_Client.h>


// Function declarations
void connectFB();
//void sendFB(float temperature, float humidity, float pressure, float gas, float altitude);
void sendFB(const SensorData &data);
#endif // ESP32_FIREBASE_H