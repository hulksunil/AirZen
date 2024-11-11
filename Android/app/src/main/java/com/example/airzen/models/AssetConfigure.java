package com.example.airzen.models;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.airzen.MainActivity;
import com.example.airzen.R;

public class AssetConfigure {

    public static Drawable setTemperatureSVG(double currentTemp, Context context){
        NotificationHelper.notifyTemperatureIfBeyondThreshold((Activity) context, currentTemp);

        if(currentTemp >= 35.00){
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_red);
        }
        else if(currentTemp < 35.00 && currentTemp >= 25.00){
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_orange);
        }
        else if(currentTemp < 25.00 && currentTemp >= 15.00){
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_green);
        }
        else{
            return AppCompatResources.getDrawable(context, R.drawable.thermometer_blue);
        }

    }

    public static Drawable setHumiditySVG(double currentHumidity, Context context){
        NotificationHelper.notifyHumidityIfBeyondThreshold((Activity) context, currentHumidity);

        if(currentHumidity >= 70 || currentHumidity < 25){
            return AppCompatResources.getDrawable(context,R.drawable.humidity_red);
        }
        else if((currentHumidity >= 60) || (currentHumidity >= 25 && currentHumidity < 30)){
            return AppCompatResources.getDrawable(context,R.drawable.humidity_orange);

        }
        else /*if(currentHumidity >= 30)*/{
            return AppCompatResources.getDrawable(context,R.drawable.humidity_green);
        }
    }

    public static Drawable setEcos2SVG(int currentCO2, Context context){
        NotificationHelper.notifyCo2IfBeyondThreshold((Activity) context, currentCO2);

        if(currentCO2 > 2500){
            return AppCompatResources.getDrawable(context,R.drawable.co2_red);
        } else if (currentCO2 > 1500 && currentCO2 < 2500) {
            return AppCompatResources.getDrawable(context,R.drawable.co2_orange);
        }
        else {
            return AppCompatResources.getDrawable(context,R.drawable.co2_green);
        }
    }

    public static Drawable setVOCSVG(double currentVOC, Context context){
        if(currentVOC > 2.2){
            return AppCompatResources.getDrawable(context,R.drawable.voc_red);
        } else if (currentVOC > 0.66 && currentVOC < 2.2) {
            return AppCompatResources.getDrawable(context,R.drawable.voc_orange);
        }
        else {
            return AppCompatResources.getDrawable(context,R.drawable.voc_green);
        }
    }

}
