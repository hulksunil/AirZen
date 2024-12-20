#include "ESP32_Firebase.h"
#include "addons/TokenHelper.h" //For token generation
#include "addons/RTDBHelper.h" //For realtime database payload helping
#include "BME680.h"
#include "SensorData.h"
#include <time.h>
#include "GP2Y1010AU0F_DustSensor.h"

//The Firebase code structure had two main resources used to help understand how Firebase works. The first was the tutorial resources for databases. 
//The second was a Youtube link giving a breakdown on how FIrebase works: https://www.youtube.com/watch?v=aO92B-K4TnQ&t=916s&ab_channel=EducationisLife%28joedgoh%29

//The two pieces (not One Piece sorry Moussa) that we need to find/connect to our database.
#define API_KEY "AIzaSyBCrpkYZPiBYCCGg_C9HSNXUEbpUf1j5AU"
#define DATABASE_URL "https://airzen-c1ded-default-rtdb.firebaseio.com/"

//Defining our Firebase objects:
FirebaseData FB; //This will handle the data operations (reading, writing, etc). This is our main object used.
FirebaseAuth auth; //Handles authentication
FirebaseConfig config; //Handles configuration

//We'll have a signup boolean that's by default false.
boolean signUpOK = false;

//Setting up timer variables.
unsigned long sendDataPrevMillis = 0; //Stores the time when the last data chunk was sent.
unsigned long timeDelay = 10000; //Every 10 seconds, send more data to the database.

//We have to connect to the firebase before we can send data
void connectFB() {

  //We first have to assign the URL and API key:
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  //Now we can call the firebase signUp method.
  //This signUp is using our current authentication method, which is "anonymous user" (AU). 

  if(Firebase.signUp(&config, &auth, "", "")) { //The last two arguments are empty strings, denoting AU signup.
    Serial.println("The signup was successful, new anonymous user created.");
    signUpOK = true;
  } else { //If the signUp fails
    Serial.printf("%s\n", config.signer.signupError.message.c_str()); //Error message from the library
  }

  config.token_status_callback = tokenStatusCallback; //When the system needs to update a token status, it can use this function.
  //We can now initialize the firebase library using our configuration and authentication settings
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

//Function to generate a timestamp as a unique ID. This will be for the pastValues directory.
String generateTimestamp() {
    time_t now = time(nullptr);  // Get the current time
    struct tm *timeinfo = localtime(&now);  // Convert to local time
    char buffer[20];
    strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", timeinfo);  // Format: 2024-11-09T15:45:00
    return String(buffer);
}

//Now we need a function that can send values to the database:
void sendFB(const SensorData &data) { //Using the SensorData object to send all the float parameters to Firebase.
    String currentDataPath = "/sensorData/current";  //Path for current readings. If the path doesn't exist, it will be created for us.
    String pastValuesPath = "/sensorData/pastValues";  //Path for historical data. If the path doesn't exist, it will be created for us.

    // Generate a timestamp-based unique ID
    String timestamp = generateTimestamp();

    //The data can be sent using the Firebase.RTDB.setFloat() function (we can pick from a list of variables, but we're using Float).
    //First, we check to make sure the timer is past the 10 second mark. 
    if (Firebase.ready() && (millis() - sendDataPrevMillis > timeDelay || sendDataPrevMillis == 0)) {
        sendDataPrevMillis = millis(); //Marks the current time before the transfer of data occurs

        // Update the current data
        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/temperature", data.temperature)) {  //Firebase Object, Database node path (if the path doesn't exist, it will be created automatically), value we want to pass. 
            Serial.println("Temperature saved to current node.");
        } else { //If it failed to aquire the data:
            Serial.println("Failed to save temperature: " + FB.errorReason());
        }
        //We then repeat for all of the parameters we want to send to the database.
        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/humidity", data.humidity)) {
            Serial.println("Humidity saved to current node.");
        } else {
            Serial.println("Failed to save humidity: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/co2", data.co2)) {
            Serial.println("CO2 saved to current node.");
        } else {
            Serial.println("Failed to save CO2: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/voc", data.voc)) {
            Serial.println("VOC saved to current node.");
        } else {
            Serial.println("Failed to save VOC: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/aqi", data.aqi)) {
            Serial.println("AQI saved to current node.");
        } else {
            Serial.println("Failed to save AQI: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/dustDensity", data.dustDensity)) {
            Serial.println("Dust Density saved to current node.");
        } else {
            Serial.println("Failed to save Dust Density: " + FB.errorReason());
        }
       

        // Save the same data to the pastValues node with timestamp
        if (Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/temperature", data.temperature)) {
            Serial.println("Temperature saved to pastValues.");
        } else {
            Serial.println("Failed to save temperature to pastValues: " + FB.errorReason());
        }

        // Repeat for other fields:
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/humidity", data.humidity)) {
            Serial.println("Failed to save humidity to pastValues: " + FB.errorReason());
        }
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/co2", data.co2)) {
            Serial.println("Failed to save CO2 to pastValues: " + FB.errorReason());
        }
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/voc", data.voc)) {
            Serial.println("Failed to save VOC to pastValues: " + FB.errorReason());
        }
     
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/dustDensity", data.dustDensity)) {
            Serial.println("Failed to save dust Density to pastValues: " + FB.errorReason());
        }
     
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/aqi", data.aqi)) {
            Serial.println("Failed to save AQI to pastValues: " + FB.errorReason());
        }
        if (!Firebase.RTDB.setString(&FB, pastValuesPath + "/" + timestamp + "/timestamp", timestamp)) {
            Serial.println("Failed to save timestamp to pastValues: " + FB.errorReason());
    } } 
     else {
        Serial.println("Firebase not ready or time delay not met.");
    }
}


