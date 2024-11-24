package com.example.airzen.models;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.airzen.R;


public  class NotificationHelper {

    // Android only allows requesting permissions twice and then stops asking so we use this to redirect the user to settings page after 2 requests
    private static final int MAX_TIMES_ALLOWED_TO_REQUEST_PERMISSIONS = 2;


    // Flags to track if notification has been sent
    // This is to prevent multiple notifications from being sent for the same event (e.g. temperature exceeding threshold)
    // the flag is reset when the value goes below the threshold
    private static boolean tempNotificationSent = false;
    private static boolean humNotificationSent = false;
    private static boolean co2NotificationSent = false;
    private static boolean vocNotificationSent = false;
    private static boolean dustNotificationSent = false;
    private static boolean aqiNotificationSent = false;

    private final static double TEMPERATURE_UPPER_THRESHOLD = 35.00;
    private final static double HUMIDITY_UPPER_THRESHOLD = 70;
    private final static double HUMIDITY_LOWER_THRESHOLD = 25;
    private final static double CO2_UPPER_THRESHOLD = 2500;
    private final static double VOC_UPPER_THRESHOLD = 2.2;
    private final static double DUST_UPPER_THRESHOLD = 100;
    private final static double AQI_UPPER_THRESHOLD = 100;

    public static void requestPermissions(Activity context){
        int timesRequested = context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getInt("timesRequested", 0);
        timesRequested++;

        if(timesRequested > MAX_TIMES_ALLOWED_TO_REQUEST_PERMISSIONS){
            Toast.makeText(context, "Please enable notifications from settings", Toast.LENGTH_SHORT).show();
            // redirect the user to actual settings notifications page since android doesn't allow requesting permissions more than 2 times
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
            return;
        }
        context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).edit().putInt("timesRequested", timesRequested).apply();

        ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
    }



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

    public static boolean isPostNotificationsPermissionGranted(Activity context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean areNotificationsEnabled(Activity context) {
        return context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("areNotificationsEnabled", false);
    }


    private static void notifyUserOfHighThreshold(Activity context, String measurement) {
        // Create the notification
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.app_logo_notification_icon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "your_channel_id")
                .setSmallIcon(R.drawable.app_logo_notification_icon)  // Replace with your app's icon
                .setLargeIcon(icon)
                .setContentTitle("Environmental Alert")
                .setContentText(measurement+ " value has exceeded threshold limits")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

//        // if the app does not have the permission to post notifications, request it
        if (!isPostNotificationsPermissionGranted(context)) {
            requestPermissions(context);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    public static void notifyTemperatureIfBeyondThreshold(Activity context, double temperature)  {
        if(!areNotificationsEnabled(context)){
            return;
        }

        if(!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasTempNotificationEnabled", false)){
            return;
        }

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
        if(!areNotificationsEnabled(context)){
            return;
        }

        if(!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasHumidityNotificationEnabled", false)){
            return;
        }


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
        if(!areNotificationsEnabled(context)){
            return;
        }

        if(!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasCO2NotificationEnabled", false)){
            return;
        }

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
        if(!areNotificationsEnabled(context)){
            return;
        }

        if(!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasVOCNotificationEnabled", false)){
            return;
        }

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
        if(!areNotificationsEnabled(context)){
            return;
        }

        if(!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasDustNotificationEnabled", false)){
            return;
        }

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
        if(!areNotificationsEnabled(context)){
            return;
        }

        if(!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasIAQINotificationEnabled", false)){
            return;
        }

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
