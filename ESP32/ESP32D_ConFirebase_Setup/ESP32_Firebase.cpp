#include "ESP32_Firebase.h"
#include "addons/TokenHelper.h" //For token generation
#include "addons/RTDBHelper.h" //For realtime database payload helping
#include "BME680.h"
#include "SensorData.h"
#include <time.h>


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
unsigned long timeDelay = 5000; //Every 5 seconds, send more data to the database.

//We have to connect to the firebase before we can send data
void connectFB() {

  //We first have to assign the URL and API key:
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  //Now we can call the firebase signUp method.
  //This singUp is using our current authentication method, which is "anonymous user". 

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

// Function to generate a timestamp as a unique ID
String generateTimestamp() {
    time_t now = time(nullptr);  // Get the current time
    struct tm *timeinfo = localtime(&now);  // Convert to local time
    char buffer[20];
    strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", timeinfo);  // Format: 2024-11-09T15:45:00
    return String(buffer);
}

//Now we need a function that can send values to the database:
void sendFB(const SensorData &data) {
    String currentDataPath = "/sensorData/current";  // Path for current readings
    String pastValuesPath = "/sensorData/pastValues";  // Path for historical data

    // Generate a timestamp-based unique ID
    String timestamp = generateTimestamp();

    if (Firebase.ready() && (millis() - sendDataPrevMillis > timeDelay || sendDataPrevMillis == 0)) {
        sendDataPrevMillis = millis();

        // Update the current data
        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/temperature", data.temperature)) {
            Serial.println("Temperature saved to current node.");
        } else {
            Serial.println("Failed to save temperature: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/humidity", data.humidity)) {
            Serial.println("Humidity saved to current node.");
        } else {
            Serial.println("Failed to save humidity: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/pressure", data.pressure)) {
            Serial.println("Pressure saved to current node.");
        } else {
            Serial.println("Failed to save pressure: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/gas", data.gas)) {
            Serial.println("Gas saved to current node.");
        } else {
            Serial.println("Failed to save gas: " + FB.errorReason());
        }

        if (Firebase.RTDB.setFloat(&FB, currentDataPath + "/altitude", data.altitude)) {
            Serial.println("Altitude saved to current node.");
        } else {
            Serial.println("Failed to save altitude: " + FB.errorReason());
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
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/pressure", data.pressure)) {
            Serial.println("Failed to save pressure to pastValues: " + FB.errorReason());
        }
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/gas", data.gas)) {
            Serial.println("Failed to save gas to pastValues: " + FB.errorReason());
        }
        if (!Firebase.RTDB.setFloat(&FB, pastValuesPath + "/" + timestamp + "/altitude", data.altitude)) {
            Serial.println("Failed to save altitude to pastValues: " + FB.errorReason());
        }
        if (!Firebase.RTDB.setString(&FB, pastValuesPath + "/" + timestamp + "/timestamp", timestamp)) {
            Serial.println("Failed to save timestamp to pastValues: " + FB.errorReason());
    } }else {
        Serial.println("Firebase not ready or time delay not met.");
    }
}

//--------------------------------------------------------------------------------------------------------------------
//Second Version of sendFB with current.
// void sendFB(const SensorData &data) {
//     String dataPath = "/sensorData/current";

//     if (Firebase.ready() && (millis() - sendDataPrevMillis > timeDelay || sendDataPrevMillis == 0)) {
//         sendDataPrevMillis = millis();

//         // Sending all data from SensorData object
//         if (Firebase.RTDB.setFloat(&FB, dataPath + "/temperature", data.temperature)) {
//             Serial.println("Temperature saved to: " + FB.dataPath());
//         } else {
//             Serial.println("Failed: " + FB.errorReason());
//         }

//         if (Firebase.RTDB.setFloat(&FB, dataPath + "/humidity", data.humidity)) {
//             Serial.println("Humidity saved to: " + FB.dataPath());
//         } else {
//             Serial.println("Failed: " + FB.errorReason());
//         }

//         if (Firebase.RTDB.setFloat(&FB, dataPath + "/pressure", data.pressure)) {
//             Serial.println("Pressure saved to: " + FB.dataPath());
//         } else {
//             Serial.println("Failed: " + FB.errorReason());
//         }

//         if (Firebase.RTDB.setFloat(&FB, dataPath + "/gas", data.gas)) {
//             Serial.println("Gas saved to: " + FB.dataPath());
//         } else {
//             Serial.println("Failed: " + FB.errorReason());
//         }

//         if (Firebase.RTDB.setFloat(&FB, dataPath + "/altitude", data.altitude)) {
//             Serial.println("Altitude saved to: " + FB.dataPath());
//         } else {
//             Serial.println("Failed: " + FB.errorReason());
//         }
//     }
//}

// ---------------------------------------------------------------------------------------------------------
//First version of sendFB
// void sendFB(float temperature, float humidity, float pressure, float gas, float altitude) { //We want to send these five variables to the firebase.
//   String dataPath = "/sensorData/current"; //We need a path defined for where the data will be sent.

// //Check to proceed:
// if(Firebase.ready() && (millis() - sendDataPrevMillis > timeDelay || sendDataPrevMillis == 0)){
//   sendDataPrevMillis = millis(); //Marks the current time when a transfer of data occurs

// //Temperature capture
//   if(Firebase.RTDB.setFloat(&FB,dataPath + "/temperature", BMETemp())) {   //Firebase Object, Database node path (if the path doesn't exist, it will be created automatically), value we want to pass. 
//    // Serial.print(BMETemp()); //Printing the captured value
//     Serial.print(" --Succcessfully saved to: " + FB.dataPath());
//     Serial.println();

//   } else { //If it failed to aquire the data:
//     Serial.println("Failed: " +FB.errorReason());
// }

// //Humidity capture
//   if(Firebase.RTDB.setFloat(&FB,dataPath + "/humidity", BMEHumidity())) {   //Firebase Object, Database node path (if the path doesn't exist, it will be created automatically), value we want to pass. 
//     //Serial.print(BMEHumidity()); //Printing the captured value
//     Serial.print(" --Succcessfully saved to: " + FB.dataPath());
//     Serial.println();

//   } else { //If it failed to aquire the data:
//     Serial.println("Failed: " +FB.errorReason());
// }

// //Pressure capture
//   if(Firebase.RTDB.setFloat(&FB,dataPath + "/pressure", BMEPressure())) {   //Firebase Object, Database node path (if the path doesn't exist, it will be created automatically), value we want to pass. 
//    // Serial.print(BMEPressure()); //Printing the captured value
//     Serial.print(" --Succcessfully saved to: " + FB.dataPath());
//     Serial.println();

//   } else { //If it failed to aquire the data:
//     Serial.println("Failed: " +FB.errorReason());
//   }

// //Gas Resistance capture
//   if(Firebase.RTDB.setFloat(&FB,dataPath + "/gas", BMEGas())) {   //Firebase Object, Database node path (if the path doesn't exist, it will be created automatically), value we want to pass. 
//    // Serial.print(BMEGas()); //Printing the captured value
//     Serial.print(" --Succcessfully saved to: " + FB.dataPath());
//     Serial.println();

//   } else { //If it failed to aquire the data:
//     Serial.println("Failed: " +FB.errorReason());
//   }

//   //Altitude capture
//   if(Firebase.RTDB.setFloat(&FB,dataPath + "/altitude", BMEAltitude())) {   //Firebase Object, Database node path (if the path doesn't exist, it will be created automatically), value we want to pass. 
//    // Serial.print(BMEAltitude()); //Printing the captured value
//     Serial.print(" --Succcessfully saved to: " + FB.dataPath());
//     Serial.println();

//   } else { //If it failed to aquire the data:
//     Serial.println("Failed: " +FB.errorReason());
//   }
// }
//}