package com.example.airzen.models;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.airzen.MainActivity;
import com.example.airzen.R;

public class AssetConfigure {

    /**
     * This will return an svg that represents the current levels of the temp
     *
     * @param currentTemp this will determine the specific SVG to return
     * @param context     this is the current context where this method is being called
     * @return a Drawable to be displayed for the specific temperature reading
     */
    public static Drawable setTemperatureSVG(double currentTemp, Context context) {
        NotificationHelper.notifyTemperatureIfBeyondThreshold((Activity) context, currentTemp);

        if (currentTemp >= 35.00) {
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_red);
        } else if (currentTemp < 35.00 && currentTemp >= 25.00) {
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_orange);
        } else if (currentTemp < 25.00 && currentTemp >= 15.00) {
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_green);
        } else {
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_blue);
        }

    }

    /**
     * This will return an svg that represents the current levels of the humidity
     *
     * @param currentHumidity this will determine the specific SVG to return
     * @param context         this is the current context where this method is being called
     * @return a Drawable to be displayed for the specific humidity reading
     */
    public static Drawable setHumiditySVG(double currentHumidity, Context context) {
        NotificationHelper.notifyHumidityIfBeyondThreshold((Activity) context, currentHumidity);

        if (currentHumidity >= 70 || currentHumidity < 25) {
            return AppCompatResources.getDrawable(context, R.drawable.humidity_red);
        } else if ((currentHumidity >= 60) || (currentHumidity >= 25 && currentHumidity < 30)) {
            return AppCompatResources.getDrawable(context, R.drawable.humidity_orange);

        } else /*if(currentHumidity >= 30)*/ {
            return AppCompatResources.getDrawable(context, R.drawable.humidity_green);
        }
    }

    /**
     * This will return an svg that represents the current levels of the co2
     *
     * @param currentCO2 this will determine the specific SVG to return
     * @param context    this is the current context where this method is being called
     * @return a Drawable to be displayed for the specific co2 reading
     */
    public static Drawable setEcos2SVG(int currentCO2, Context context) {
        NotificationHelper.notifyCo2IfBeyondThreshold((Activity) context, currentCO2);

        if (currentCO2 > 2500) {
            return AppCompatResources.getDrawable(context, R.drawable.co2_red);
        } else if (currentCO2 > 1500 && currentCO2 < 2500) {
            return AppCompatResources.getDrawable(context, R.drawable.co2_orange);
        } else {
            return AppCompatResources.getDrawable(context, R.drawable.co2_green);
        }
    }

    /**
     * This will return an svg that represents the current levels of the VOC
     *
     * @param currentVOC this will determine the specific SVG to return
     * @param context    this is the current context where this method is being called
     * @return a Drawable to be displayed for the specific VOC reading
     */
    public static Drawable setVOCSVG(double currentVOC, Context context) {
        NotificationHelper.notifyVocIfBeyondThreshold((Activity) context, currentVOC);
        if (currentVOC > 2.2) {
            return AppCompatResources.getDrawable(context, R.drawable.voc_red);
        } else if (currentVOC > 0.66 && currentVOC < 2.2) {
            return AppCompatResources.getDrawable(context, R.drawable.voc_orange);
        } else {
            return AppCompatResources.getDrawable(context, R.drawable.voc_green);
        }
    }

}
