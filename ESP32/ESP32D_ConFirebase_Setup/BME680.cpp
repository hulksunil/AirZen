#include "BME680.h"
#define SEALEVELPRESSURE_HPA (101325)

Bsec iaqSensor;  // Create an instance of Bsec for handling BME680 sensor data.

void checkIaqSensorStatus(void) {
  if (iaqSensor.bsecStatus != BSEC_OK) {
    if (iaqSensor.bsecStatus < BSEC_OK) {
      Serial.print("BSEC error code: ");
      Serial.println(iaqSensor.bsecStatus);
    } else {
      Serial.print("BSEC warning code: ");
      Serial.println(iaqSensor.bsecStatus);
    }
  }

  if (iaqSensor.bme68xStatus != BME68X_OK) {
    if (iaqSensor.bme68xStatus < BME68X_OK) {
      Serial.print("BME680 error code: ");
      Serial.println(iaqSensor.bme68xStatus);
    } else {
      Serial.print("BME680 warning code: ");
      Serial.println(iaqSensor.bme68xStatus);
    }
  }
}

// Initialize the sensor and configure it for the required virtual sensors.
void BME_Start() {
  // Initialize the sensor with I2C address (0x77).
  iaqSensor.begin(0x77, Wire);

  // Print the BSEC library version.
  Serial.print("BSEC library version: ");
  Serial.print(iaqSensor.version.major);
  Serial.print(".");
  Serial.print(iaqSensor.version.minor);
  Serial.print(".");
  Serial.print(iaqSensor.version.major_bugfix);
  Serial.print(".");
  Serial.println(iaqSensor.version.minor_bugfix);

  // Check sensor status.
  checkIaqSensorStatus();

  // Define the virtual sensors we want to use.
  bsec_virtual_sensor_t sensorList[6] = {
    BSEC_OUTPUT_IAQ,
    BSEC_OUTPUT_CO2_EQUIVALENT,
    BSEC_OUTPUT_BREATH_VOC_EQUIVALENT,  // VOC Equivalent sensor
    BSEC_OUTPUT_SENSOR_HEAT_COMPENSATED_TEMPERATURE, // (Now unused)
    BSEC_OUTPUT_SENSOR_HEAT_COMPENSATED_HUMIDITY,    // Humidity sensor
    BSEC_OUTPUT_RAW_PRESSURE  // Pressure sensor added
  };

  // Update the sensor subscription with the required sensors.
  iaqSensor.updateSubscription(sensorList, 6, BSEC_SAMPLE_RATE_LP); //This sample rate outputs new values every 3 seconds.
  checkIaqSensorStatus();
}

// Function to read and return temperature from the sensor.
float BMETemp() {
  float temperature = iaqSensor.temperature;
  Serial.print("Temperature: ");
  Serial.println(temperature, 2);
  // Serial.println(" °C");
  return temperature;
}

// Function to read and return humidity from the sensor.
float BMEHumidity() {
  float humidity = iaqSensor.humidity;
  Serial.print("Humidity: ");
  Serial.println(humidity, 2);
  //Serial.println(" %");
  return humidity;
}

// Function to read and return CO2 equivalent from the sensor.
float BMEco2() {
  float co2 = iaqSensor.co2Equivalent;
  Serial.print("CO2 Equivalent: ");
  Serial.println(co2, 2);
  // Serial.println(" ppm");
  return co2;
}

// Function to read and return VOC equivalent from the sensor.
float BMEVOC() {
  float voc = iaqSensor.breathVocEquivalent;
  Serial.print("VOC Equivalent: ");
  Serial.println(voc, 2);
  // Serial.println(" ppb");
  return voc;
}

// Function to read and return IAQ (Indoor Air Quality) from the sensor.
float BMEAQI() {
  float aqi = iaqSensor.iaq;
  Serial.print("IAQ (Indoor Air Quality): ");
  Serial.println(aqi, 2);
  // Serial.println(" ");
  return aqi;
}

float BMEIQA_ACCURACY() {
  float acc = iaqSensor.iaqAccuracy;
  Serial.print("IAQ Accuracy: ");
  Serial.println(acc, 2);
  // Serial.println(" ");
  return acc;
}
