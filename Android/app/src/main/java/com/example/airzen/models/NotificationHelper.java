package com.example.airzen.models;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
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


public class NotificationHelper {

    // Android only allows requesting permissions twice and then stops asking so we use this to redirect the user to settings page after 2 requests
    public static final int MAX_TIMES_ALLOWED_TO_REQUEST_PERMISSIONS = 2;
    private static final String ENVIRONMENTAL_ALERTS_GROUP = "environmental_alerts_group";


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

    public static void requestPermissions(Activity context) {
        int timesRequested = context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getInt("timesRequested", 0);
        timesRequested++;

        if (timesRequested > MAX_TIMES_ALLOWED_TO_REQUEST_PERMISSIONS) {
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
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Create the group
            NotificationChannelGroup environmentalGroup = new NotificationChannelGroup(
                    ENVIRONMENTAL_ALERTS_GROUP,
                    "Environmental Threshold Alerts"
            );

            // Register the group
            if (notificationManager != null) {
                notificationManager.createNotificationChannelGroup(environmentalGroup);
            }


            // Create the channels
            // Temperature channel
            NotificationChannel temperatureChannel = new NotificationChannel(
                    "temperature_alerts",
                    "Temperature Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            temperatureChannel.setDescription("Alerts when temperature exceeds the threshold.");
            temperatureChannel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);

            // Humidity channel
            NotificationChannel humidityChannel = new NotificationChannel(
                    "humidity_alerts",
                    "Humidity Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            humidityChannel.setDescription("Alerts for humidity exceeding or falling below the thresholds.");
            humidityChannel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);

            // CO2 channel
            NotificationChannel co2Channel = new NotificationChannel(
                    "co2_alerts",
                    "CO2 Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            co2Channel.setDescription("Alerts for CO2 exceeding the threshold.");
            co2Channel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);

            // VOC channel
            NotificationChannel vocChannel = new NotificationChannel(
                    "voc_alerts",
                    "VOC Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            vocChannel.setDescription("Alerts for VOC exceeding the threshold.");
            vocChannel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);

            // Dust channel
            NotificationChannel dustChannel = new NotificationChannel(
                    "dust_alerts",
                    "Dust Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            dustChannel.setDescription("Alerts for Dust exceeding the threshold.");
            dustChannel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);

            // IAQI channel
            NotificationChannel iaqiChannel = new NotificationChannel(
                    "iaqi_alerts",
                    "IAQI Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            iaqiChannel.setDescription("Alerts for IAQI exceeding the threshold.");
            iaqiChannel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);

            NotificationChannel generalAlertsChannel = new NotificationChannel(
                    "general_alerts",
                    "General Alerts",
                    NotificationManager.IMPORTANCE_LOW // Adjust importance as needed
            );
            generalAlertsChannel.setDescription("General notifications related to environmental alerts.");
            generalAlertsChannel.setGroup(ENVIRONMENTAL_ALERTS_GROUP);


            // Register the channels
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(temperatureChannel);
                notificationManager.createNotificationChannel(humidityChannel);
                notificationManager.createNotificationChannel(co2Channel);
                notificationManager.createNotificationChannel(vocChannel);
                notificationManager.createNotificationChannel(dustChannel);
                notificationManager.createNotificationChannel(iaqiChannel);
                notificationManager.createNotificationChannel(generalAlertsChannel);
            }
        }
    }

    public static boolean isPostNotificationsPermissionGranted(Activity context) {
        SharedPreferences notification = context.getSharedPreferences("notificationPreferences", MODE_PRIVATE);
        boolean isFirstTimeNotificationsEnabled = context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("isFirstTimeEnabled", true);
        if (isFirstTimeNotificationsEnabled) {
            notification.edit().putBoolean("isFirstTimeEnabled", false).apply();

            notification.edit().putBoolean("hasTempNotificationEnabled", true).putBoolean("hasHumidityNotificationEnabled", true).putBoolean("hasCO2NotificationEnabled", true).putBoolean("hasVOCNotificationEnabled", true).putBoolean("hasDustNotificationEnabled", true).putBoolean("hasIAQINotificationEnabled", true).apply();

        }
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean areNotificationsEnabled(Activity context) {
        return context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("areNotificationsEnabled", false);
    }

    private static int getNotificationId(String channel_id) {
        switch (channel_id) {
            case "temperature_alerts":
                return 1;
            case "humidity_alerts":
                return 2;
            case "co2_alerts":
                return 3;
            case "voc_alerts":
                return 4;
            case "dust_alerts":
                return 5;
            case "iaqi_alerts":
                return 6;
            default:
                return 0;
        }
    }

    /**
     * Notify the user of a high threshold
     * We have separate channels for each measurement so that the user can receive multiple notifications for different measurements at the same time
     * @param context
     * @param message
     * @param channel_id
     */
    private static void sendEnvironmentalNotificationToUser(Activity context, String message, String channel_id) {
        // Create the notification
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo_notification_icon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id).setSmallIcon(R.drawable.app_logo_notification_icon)  // Replace with your app's icon
                .setLargeIcon(icon).setContentTitle("Environmental Alert").setContentText(message + " ").setGroup(ENVIRONMENTAL_ALERTS_GROUP).setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(getNotificationId(channel_id), builder.build());

        createOrUpdateSummaryNotification(context, notificationManager);

    }

    private static void createOrUpdateSummaryNotification(Activity context, NotificationManagerCompat notificationManager) {
        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, "general_alerts")
                .setSmallIcon(R.drawable.app_logo_notification_icon)
                .setContentTitle("Environmental Alerts Summary")
                .setContentText("Multiple threshold alerts detected.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(ENVIRONMENTAL_ALERTS_GROUP)
                .setGroupSummary(true);

        // Use a constant ID for the summary notification
        notificationManager.notify(9999, summaryBuilder.build());
    }

    public static void notifyTemperatureIfBeyondThreshold(Activity context, double temperature) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasTempNotificationEnabled", false)) {
            return;
        }

        if (temperature >= TEMPERATURE_UPPER_THRESHOLD) {
            if (tempNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "Temperature value has exceeded threshold limits of "+TEMPERATURE_UPPER_THRESHOLD+"°C", "temperature_alerts");
            tempNotificationSent = true;
        } else {
            tempNotificationSent = false;
        }
    }

    public static void notifyHumidityIfBeyondThreshold(Activity context, double humidity) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasHumidityNotificationEnabled", false)) {
            return;
        }


        if (humidity >= HUMIDITY_UPPER_THRESHOLD || humidity < HUMIDITY_LOWER_THRESHOLD) {
            if (humNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "Humidity value has exceeded threshold limits of "+HUMIDITY_LOWER_THRESHOLD + "% to "+HUMIDITY_UPPER_THRESHOLD+"%","humidity_alerts");
            humNotificationSent = true;
        } else {
            humNotificationSent = false;
        }
    }

    public static void notifyCo2IfBeyondThreshold(Activity context, double co2) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasCO2NotificationEnabled", false)) {
            return;
        }

        if (co2 >= CO2_UPPER_THRESHOLD) {
            if (co2NotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "CO2 levels have exceeded threshold limits of "+CO2_UPPER_THRESHOLD+"ppm", "co2_alerts");
            co2NotificationSent = true;
        } else {
            co2NotificationSent = false;
        }
    }

    public static void notifyVocIfBeyondThreshold(Activity context, double voc) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasVOCNotificationEnabled", false)) {
            return;
        }

        if (voc >= VOC_UPPER_THRESHOLD) {
            if (vocNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "VOC value has exceeded threshold limits of "+VOC_UPPER_THRESHOLD+"ppm", "voc_alerts");
            vocNotificationSent = true;
        } else {
            vocNotificationSent = false;
        }
    }

    public static void notifyDustIfBeyondThreshold(Activity context, double dust) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasDustNotificationEnabled", false)) {
            return;
        }

        if (dust > DUST_UPPER_THRESHOLD) {
            if (dustNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "Dust density levels have exceeded threshold limits of "+DUST_UPPER_THRESHOLD+"ug/m³", "dust_alerts");
            dustNotificationSent = true;
        } else {
            dustNotificationSent = false;
        }
    }


    public static void notifyAqiIfBeyondThreshold(Activity context, double aqi) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasIAQINotificationEnabled", false)) {
            return;
        }

        if (aqi > AQI_UPPER_THRESHOLD) {
            if (aqiNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "IAQI level has gone beyond threshold limit of "+AQI_UPPER_THRESHOLD, "iaqi_alerts");
            aqiNotificationSent = true;
        } else {
            aqiNotificationSent = false;
        }
    }


}
