package com.example.airzen.models;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import com.example.airzen.MainActivity;
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

    private final static double TEMPERATURE_UPPER_THRESHOLD = 27.00;
    private final static double HUMIDITY_UPPER_THRESHOLD = 70;
    private final static double HUMIDITY_LOWER_THRESHOLD = 25;
    private final static double CO2_UPPER_THRESHOLD = 2500;
    private final static double VOC_UPPER_THRESHOLD = 2.2;
    private final static double DUST_UPPER_THRESHOLD = 150;
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
     *
     * @param context
     * @param message
     * @param channel_id
     */
    @SuppressLint("MissingPermission")
    private static void sendEnvironmentalNotificationToUser(Activity context, String message, String channel_id) {
        PendingIntent activityToGoToWhenClicked = createIntentForNotification(context);


        // Create the notification
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo_notification_icon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(R.drawable.app_logo_notification_icon)  // Replace with your app's icon
                .setLargeIcon(icon).setContentTitle("Environmental Alert")
                .setContentText(message + " ").setGroup(ENVIRONMENTAL_ALERTS_GROUP)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(activityToGoToWhenClicked) // Set the pending intent
                .setAutoCancel(true); // Dismiss notification when tapped;

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(getNotificationId(channel_id), builder.build());

        createOrUpdateSummaryNotification(context, notificationManager);

    }

    /**
     * Create or update the summary notification
     * This notification will be shown when multiple threshold alerts are detected
     *
     * @param context
     * @param notificationManager
     */
    @SuppressLint("MissingPermission")
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

    private static PendingIntent createIntentForNotification(Activity context) {
        // Create the intent to open the app
        Intent intent = new Intent(context, MainActivity.class); // Replace with your main activity class
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Wrap the intent in a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0, // Request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE for API 31+
        );
        return pendingIntent;
    }

    public static void notifyTemperatureIfBeyondThreshold(Activity context, double temperature) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        if (!context.getSharedPreferences("notificationPreferences", MODE_PRIVATE).getBoolean("hasTempNotificationEnabled", false)) {
            return;
        }

        double idealTemp =  Double.valueOf((context.getSharedPreferences("UserPreferences",MODE_PRIVATE).getString("ideal_Temp", "-1")));
        double tempToCheck = idealTemp;
        if(idealTemp==-1) {
            tempToCheck = TEMPERATURE_UPPER_THRESHOLD;
        }

        if (temperature >=  tempToCheck) {
            if (tempNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "Temperature value has exceeded threshold limits of " + tempToCheck + "°C", "temperature_alerts");
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

        double humidtyDifference = HUMIDITY_UPPER_THRESHOLD - HUMIDITY_LOWER_THRESHOLD;
        double idealHumidty =  Double.valueOf((context.getSharedPreferences("UserPreferences",MODE_PRIVATE).getString("ideal_Humidity", "-1")));
        double humidtyToCheckUpper = idealHumidty+humidtyDifference/2;
        double humidtyToCheckLower = idealHumidty-humidtyDifference/2;

        if(idealHumidty==-1) {
            humidtyToCheckUpper = HUMIDITY_UPPER_THRESHOLD;
            humidtyToCheckLower = HUMIDITY_LOWER_THRESHOLD;
        }

        if (humidity >= humidtyToCheckUpper || humidity < humidtyToCheckLower) {
            if (humNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "Humidity value has exceeded threshold limits of " + humidtyToCheckLower + "% to " + humidtyToCheckUpper + "%", "humidity_alerts");
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

        double idealCO2 =  Double.valueOf((context.getSharedPreferences("UserPreferences",MODE_PRIVATE).getString("ideal_CO2", "-1")));
        double co2ToCheck = idealCO2;
        if(idealCO2==-1) {
            co2ToCheck = CO2_UPPER_THRESHOLD;
        }

        if (co2 >= co2ToCheck) {
            if (co2NotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "CO2 levels have exceeded threshold limits of " + co2ToCheck + "ppm", "co2_alerts");
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

        double idealVOC =  Double.valueOf((context.getSharedPreferences("UserPreferences",MODE_PRIVATE).getString("ideal_VOC", "-1")));
        double vocToCheck = idealVOC;
        if(idealVOC==-1) {
            vocToCheck = VOC_UPPER_THRESHOLD;
        }

        if (voc >= vocToCheck) {
            if (vocNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "VOC value has exceeded threshold limits of " + vocToCheck + "ppm", "voc_alerts");
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

        double idealDust =  Double.valueOf((context.getSharedPreferences("UserPreferences",MODE_PRIVATE).getString("ideal_Dust_Density", "-1")));
        double dustToCheck = idealDust;
        if(idealDust==-1) {
            dustToCheck = DUST_UPPER_THRESHOLD;
        }

        if (dust > dustToCheck) {
            if (dustNotificationSent) {
                return;
            }
            sendEnvironmentalNotificationToUser(context, "Dust density levels have exceeded threshold limits of " + dustToCheck + "ug/m³", "dust_alerts");
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
            sendEnvironmentalNotificationToUser(context, "IAQI level has gone beyond threshold limit of " + AQI_UPPER_THRESHOLD, "iaqi_alerts");
            aqiNotificationSent = true;
        } else {
            aqiNotificationSent = false;
        }
    }


}
