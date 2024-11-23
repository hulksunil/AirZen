package com.example.airzen.models;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.airzen.R;


public  class NotificationHelper {
    // Flag to track if notification has been sent
    // This is to prevent multiple notifications from being sent for the same event (e.g. temperature exceeding threshold)
    // the flag is reset when the value goes below the threshold
    private static boolean tempNotificationSent = false;
    private static boolean humNotificationSent = false;
    private static boolean co2NotificationSent = false;
    private static boolean vocNotificationSent = false;
    private static boolean dustNotificationSent = false;
    private static boolean aqiNotificationSent = false;


    public static void requestPermissions(Activity context){
        ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
    }

    private final static double TEMPERATURE_UPPER_THRESHOLD = 35.00;
    private final static double HUMIDITY_UPPER_THRESHOLD = 70;
    private final static double HUMIDITY_LOWER_THRESHOLD = 25;
    private final static double CO2_UPPER_THRESHOLD = 2500;
    private final static double VOC_UPPER_THRESHOLD = 2.2;
    private final static double DUST_UPPER_THRESHOLD = 100;
    private final static double AQI_UPPER_THRESHOLD = 100;


    public static void createNotificationChannel(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "your_channel_id";
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private static void notifyUserOfHighThreshold(Activity context, String measurement) {
        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "your_channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // Replace with your app's icon
                .setContentTitle("Environmental Alert")
                .setContentText(measurement+ " value exceeded threshold limits")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // if the app does not have the permission to post notifications, request it
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(context);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // TOOO: fix the threshold values

    public static void notifyTemperatureIfBeyondThreshold(Activity context, double temperature)  {
        if (temperature >= TEMPERATURE_UPPER_THRESHOLD) {
            if(tempNotificationSent){
                return;
            }
            notifyUserOfHighThreshold(context, "Temperature");
            tempNotificationSent = true;
        }else {
            tempNotificationSent = false;
        }
    }

    public static void notifyHumidityIfBeyondThreshold(Activity context, double humidity)  {
        if (humidity >= HUMIDITY_UPPER_THRESHOLD || humidity < HUMIDITY_LOWER_THRESHOLD) {
            if(humNotificationSent){
                return;
            }
            notifyUserOfHighThreshold(context, "Humidity");
            humNotificationSent = true;
        }else {
            humNotificationSent = false;
        }
    }

    public static void notifyCo2IfBeyondThreshold(Activity context, double co2)  {
        if (co2 >= CO2_UPPER_THRESHOLD) {
            if(co2NotificationSent){
                return;
            }
            notifyUserOfHighThreshold(context, "CO2");
            co2NotificationSent = true;
        }else {
            co2NotificationSent = false;
        }
    }

    public static void notifyVocIfBeyondThreshold(Activity context, double voc)  {
        if (voc >= VOC_UPPER_THRESHOLD) {
            if(vocNotificationSent){
                return;
            }
            notifyUserOfHighThreshold(context, "VOC");
            vocNotificationSent = true;
        }else {
            vocNotificationSent = false;
        }
    }

    public static void notifyDustIfBeyondThreshold(Activity context, double dust)  {
        if (dust > DUST_UPPER_THRESHOLD) {
            if(dustNotificationSent){
                return;
            }
            notifyUserOfHighThreshold(context, "Dust");
            dustNotificationSent = true;
        }else {
            dustNotificationSent = false;
        }
    }



    public static void notifyAqiIfBeyondThreshold(Activity context, double aqi)  {
        if (aqi > AQI_UPPER_THRESHOLD) {
            if(aqiNotificationSent){
                return;
            }
            notifyUserOfHighThreshold(context, "AQI");
            aqiNotificationSent = true;
        }else {
            aqiNotificationSent = false;
        }
    }


}
