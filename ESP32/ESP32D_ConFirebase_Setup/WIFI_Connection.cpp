#include "WIFI_Connection.h"

//Here is the function to set up our wifi connection
//The code here was partially used and modified based on the current link:
//https://randomnerdtutorials.com/esp32-esp8266-firebase-bme280-rtdb/

//Library used for setting up the web config portal (ESP32 AP):
//WiFiManager by tzapu
//Yoube Tutorial on ESP32 WifiManaer was used. Link: https://www.youtube.com/watch?v=VnfX9YJbaU8&ab_channel=DroneBotWorkshop

//IMPORTANT NOTE: The user chooses their ssid and enters their password for their chosen network. 
//The next time the ESP32 is powered on, it will connect to the WIFI network automatically. If you don't want this or need to troubleshoot, uncomment wm.resetSettings().

void WIFIstart() {

    WiFi.mode(WIFI_STA); // explicitly set mode, esp defaults to STA+AP
   
    Serial.begin(115200);
    Serial.println("Starting the WiFI connection process");
    delay(3000);
    
    //Object to represent the WifiManager.
    WiFiManager wm;
   
    // The resetSettings() erases the stored credentials (IP address and network) on the WiFiManager object. This is for testing only; the final product will NOT have this.
    wm.resetSettings();

    // Automatically connect using saved credentials,
    // if connection fails, it starts an access point with the specified name ("AutoConnectAP"),

    bool res; //This object will be true if the access point parameter conditions are met.
    res = wm.autoConnect("AutoConnectAP","password123"); // Our password protected access point. The user will recieve the password when they buy the product.

    if(!res) { //If the res object hasn't been validated/credentials are wrong:
      Serial.println("Failed to connect");
      //We'll try to connect again every 3 seconds, in case there is a connection delay.
      ESP.restart(); //Restarting the entire ESP32
      delay(3000);
    } 
    else {
      Serial.println("The ESP32 AP connection was sucessful");
    }

    //Once connected to the AutoConnectAP, the user need to open their browser and enter: http://192.168.4.1
    //This will bring them to the portal.
   
    // The user has chosen their network SSID and entered their password. Waiting for a connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
}
   //If the network chosen by the user is established:
    Serial.println("Connected to WIFI");
    Serial.println(WiFi.localIP());  // Prints the IP address
    delay(10000);

}

