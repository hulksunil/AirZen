#ifndef ESP32_FIREBASE_H
#define ESP32_FIREBASE_H

#include <Firebase_ESP_Client.h>
#include "SensorData.h"


// Function declarations
void connectFB();

void sendFB(const SensorData &data);
#endif // ESP32_FIREBASE_H