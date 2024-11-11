package com.example.airzen.models;

public class SensorData {
    private int co2;
    private int aqi;
    private double humidity;
    private double pressure;
    private double temperature;
    private double voc;
    private String timestamp;

    public SensorData(int co2, int aqi, double humidity, double pressure, double temperature, double voc, String timestamp) {
        this.co2 = co2;
        this.aqi = aqi;
        this.humidity = humidity;
        this.pressure = pressure;
        this.temperature = temperature;
        this.voc = voc;
        this.timestamp = timestamp;
    }

    public SensorData() {
    }

    public int getCo2() {
        return co2;
    }

    public void setCo2(int co2) {
        this.co2 = co2;
    }

    public int getAqi() {
        return aqi;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getVOC() {
        return voc;
    }

    public void setVOC(double voc) {
        this.voc = voc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "co2=" + co2 +
                ", aqi=" + aqi +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", temperature=" + temperature +
                ", voc=" + voc +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
