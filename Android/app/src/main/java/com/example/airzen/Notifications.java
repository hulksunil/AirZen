package com.example.airzen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.airzen.models.NotificationHelper;

public class Notifications extends AppCompatActivity {

    private SwitchCompat generalNotificationsSwitch, iaqiSwitch, temperatureSwitch, humiditySwitch, co2Switch, vocSwitch, dustSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initToolbar();
        initSwitches();
        handleSwitches();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences notification = getSharedPreferences("notificationPreferences", MODE_PRIVATE);
        boolean arePostNotificationsPermissionGranted = NotificationHelper.isPostNotificationsPermissionGranted(this);
        Log.i("MYNOTIFICATIONLOGS", "onResume: postNotificationsPermissionGranted: " + arePostNotificationsPermissionGranted);

        if(!arePostNotificationsPermissionGranted) {
            generalNotificationsSwitch.setChecked(false);
        } else {


            boolean hasGeneralNotification = notification.getBoolean("areNotificationsEnabled", true);
            generalNotificationsSwitch.setChecked(hasGeneralNotification);
        }
    }




    /**
     * Sets up the toolbar for the data activity ny setting the title and enabling the back button
     */
    private void initToolbar() {
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void initSwitches() {
        generalNotificationsSwitch = findViewById(R.id.switch_notifications);
        iaqiSwitch = findViewById(R.id.switch_iaqi);
        temperatureSwitch = findViewById(R.id.switch_temp);
        humiditySwitch = findViewById(R.id.switch_humidity);
        co2Switch = findViewById(R.id.switch_co2);
        vocSwitch = findViewById(R.id.switch_voc);
        dustSwitch = findViewById(R.id.switch_dust);

        SharedPreferences notification = getSharedPreferences("notificationPreferences", MODE_PRIVATE);
        boolean areNotificationsEnabled = notification.getBoolean("areNotificationsEnabled", NotificationHelper.isPostNotificationsPermissionGranted(this));
        generalNotificationsSwitch.setChecked(areNotificationsEnabled);
        if(!areNotificationsEnabled){
            iaqiSwitch.setChecked(false);
            temperatureSwitch.setChecked(false);
            humiditySwitch.setChecked(false);
            co2Switch.setChecked(false);
            vocSwitch.setChecked(false);
            dustSwitch.setChecked(false);

            iaqiSwitch.setEnabled(false);
            temperatureSwitch.setEnabled(false);
            humiditySwitch.setEnabled(false);
            co2Switch.setEnabled(false);
            vocSwitch.setEnabled(false);
            dustSwitch.setEnabled(false);
        } else {
            iaqiSwitch.setChecked(notification.getBoolean("hasIAQINotificationEnabled", false));
            temperatureSwitch.setChecked(notification.getBoolean("hasTempNotificationEnabled", false));
            humiditySwitch.setChecked(notification.getBoolean("hasHumidityNotificationEnabled", false));
            co2Switch.setChecked(notification.getBoolean("hasCO2NotificationEnabled", false));
            vocSwitch.setChecked(notification.getBoolean("hasVOCNotificationEnabled", false));
            dustSwitch.setChecked(notification.getBoolean("hasDustNotificationEnabled", false));
        }
    }

    private void handleSwitches() {
        SharedPreferences notificationPreferences = getSharedPreferences("notificationPreferences", MODE_PRIVATE);
        generalNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                // if they're permissions aren't granted, request them
                if (!NotificationHelper.isPostNotificationsPermissionGranted(this)) {
                    NotificationHelper.requestPermissions(this);
                    // cancel the switch change
                    generalNotificationsSwitch.setChecked(false);
                    return;
                }
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("areNotificationsEnabled", true).apply();
                iaqiSwitch.setChecked(notificationPreferences.getBoolean("hasIAQINotificationEnabled", false));
                temperatureSwitch.setChecked(notificationPreferences.getBoolean("hasTempNotificationEnabled", false));
                humiditySwitch.setChecked(notificationPreferences.getBoolean("hasHumidityNotificationEnabled", false));
                co2Switch.setChecked(notificationPreferences.getBoolean("hasCO2NotificationEnabled", false));
                vocSwitch.setChecked(notificationPreferences.getBoolean("hasVOCNotificationEnabled", false));
                dustSwitch.setChecked(notificationPreferences.getBoolean("hasDustNotificationEnabled", false));

                iaqiSwitch.setEnabled(true);
                temperatureSwitch.setEnabled(true);
                humiditySwitch.setEnabled(true);
                co2Switch.setEnabled(true);
                vocSwitch.setEnabled(true);
                dustSwitch.setEnabled(true);


            } else {
                // The switch is disabled
                notificationPreferences.edit().putBoolean("areNotificationsEnabled", false).apply();
                iaqiSwitch.setChecked(false);
                temperatureSwitch.setChecked(false);
                humiditySwitch.setChecked(false);
                co2Switch.setChecked(false);
                vocSwitch.setChecked(false);
                dustSwitch.setChecked(false);

                iaqiSwitch.setEnabled(false);
                temperatureSwitch.setEnabled(false);
                humiditySwitch.setEnabled(false);
                co2Switch.setEnabled(false);
                vocSwitch.setEnabled(false);
                dustSwitch.setEnabled(false);
            }
        });


        iaqiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("hasIAQINotificationEnabled", true).apply();
            } else {
                if(generalNotificationsSwitch.isChecked()) {
                // The switch is disabled
                notificationPreferences.edit().putBoolean("hasIAQINotificationEnabled", false).apply();
                }
            }
        });

        temperatureSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("hasTempNotificationEnabled", true).apply();
            } else {
                if (generalNotificationsSwitch.isChecked()) {
                    // The switch is disabled
                    notificationPreferences.edit().putBoolean("hasTempNotificationEnabled", false).apply();
                }
            }
        });

        humiditySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("hasHumidityNotificationEnabled", true).apply();
            } else {
                if(generalNotificationsSwitch.isChecked()) {
                    // The switch is disabled
                    notificationPreferences.edit().putBoolean("hasHumidityNotificationEnabled", false).apply();
                }
            }
        });

        co2Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("hasCO2NotificationEnabled", true).apply();
            } else {
                if(generalNotificationsSwitch.isChecked()) {
                    // The switch is disabled
                    notificationPreferences.edit().putBoolean("hasCO2NotificationEnabled", false).apply();
                }
            }
        });

        vocSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("hasVOCNotificationEnabled", true).apply();
            } else {
                if(generalNotificationsSwitch.isChecked()) {
                    // The switch is disabled
                    notificationPreferences.edit().putBoolean("hasVOCNotificationEnabled", false).apply();
                }
            }
        });

        dustSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The switch is enabled/checked
                notificationPreferences.edit().putBoolean("hasDustNotificationEnabled", true).apply();
            } else {
                if(generalNotificationsSwitch.isChecked()) {
                    // The switch is disabled
                    notificationPreferences.edit().putBoolean("hasDustNotificationEnabled", false).apply();
                }
            }
        });
    }


}