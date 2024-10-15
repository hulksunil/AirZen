package com.example.airzen.models;

public class SensorData {
    private int co2;
    private int aqi;
    private int temperature;

    public SensorData(int co2, int aqi, int temperature) {
        this.co2 = co2;
        this.aqi = aqi;
        this.temperature = temperature;
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

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "co2=" + co2 +
                ", aqi=" + aqi +
                ", temperature=" + temperature +
                '}';
    }
}
