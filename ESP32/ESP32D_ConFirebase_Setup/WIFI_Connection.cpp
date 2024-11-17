#include "WIFI_Connection.h"

//Here is the function to set up our wifi connection
//The code here was partially used and modified based on the current link:
//https://randomnerdtutorials.com/esp32-esp8266-firebase-bme280-rtdb/

//Our credentials (for now):
const char* ssid = "BELL075";  
const char* password = ""; 

void WIFIstart() {
  delay(1000); //We will add some delay, as this reduces the risk or errors (from testing previously).
  //WIFI connection begins
  WiFi.begin(ssid,password);
  Serial.println("Connecting to the WIFI network");

// Waiting for a connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
}

//Once a connection is established:
Serial.println("Connected to WIFI");
Serial.println(WiFi.localIP());  // Prints the IP address
delay(1000);
}