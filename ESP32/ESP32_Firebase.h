#ifndef ESP32_FIREBASE_H
#define ESP32_FIREBASE_H

#include <Firebase_ESP_Client.h>


// Function declarations
void connectFB();
void sendFB(float temperature, float humidity, float co2, float voc, float aqi, float altitude);

#endif // ESP32_FIREBASE_H