package com.example.airzen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.airzen.models.NotificationHelper;
import com.example.airzen.models.AssetConfigure;
import com.example.airzen.models.SensorData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.slimchart.SlimChart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private static final String DATABASE_READ_TAG = "DATABASE_READ_TAG";
    private ListView listOfItems; // (FOR TESTING PURPOSES) (remove later)

    protected ConstraintLayout tempTile, humidityTile, eCO2Tile, vocTile;
    private ImageView temperatureSVG, humiditySVG, eCO2SVG, vocSVG;
    private TextView currentTemp, currentHumidity, currentCo2, currentVOC, currentDust;

    private SlimChart slimChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tempTile = findViewById(R.id.tempTile);
        humidityTile = findViewById(R.id.humidityTile);
        eCO2Tile = findViewById(R.id.eCO2Tile);
        vocTile = findViewById(R.id.vocTile);

        temperatureSVG = findViewById(R.id.tempSVG);
        humiditySVG = findViewById(R.id.humiditySVG);
        eCO2SVG = findViewById(R.id.eco2SVG);
        vocSVG = findViewById(R.id.vocSVG);

        currentTemp = findViewById(R.id.currentTemp);
        currentHumidity = findViewById(R.id.currentHumidity);
        currentCo2 = findViewById(R.id.currenteCo2);
        currentDust = findViewById(R.id.currentDust);
        currentVOC = findViewById(R.id.currentVOC);

        slimChart = findViewById(R.id.slimChart);

        readFirebaseSensorData();
        NotificationHelper.createNotificationChannel(this);
        if(!NotificationHelper.isPostNotificationsPermissionGranted(this)){
            NotificationHelper.requestPermissions(this);
        }


    }


    /**
     * Connects to the Firebase database and reads the sensor data
     * from the "current" and "pastValues" node in the database and the
     * This method is called when the activity is created.
     * The data is read anytime the "current" data changes and whenever a new child value is given to the "pastValues" node.
     * The data is then logged to the console.
     * NOTE: CHECK LOGCAT ON BOTTOM LEFT
     */
    private void readFirebaseSensorData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // gets the default instance (us-central)
        DatabaseReference myRef = database.getReference("sensorData"); // gets the reference to the database that we want to read/write to
        readCurrentData(myRef);
//        readPastData(myRef);
    }



    /**
     * Reads the current data from the database anytime the "current" node changes
     *
     * @param myRef the reference of the database
     */
    private void readCurrentData(DatabaseReference myRef) {
        // write data to the database (for testing purposes)
        DatabaseReference currentDataRef = myRef.child("current");

        // read data anytime the "current data" changes
        currentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SensorData value = dataSnapshot.getValue(SensorData.class);
                if (value != null) {
                    slimChartInit(value.getAqi());

                    DecimalFormat df = new DecimalFormat("#.##");

                    currentTemp.setText(df.format(value.getTemperature()) + "" + getString(R.string.degreesC));
                    temperatureSVG.setImageDrawable(AssetConfigure.setTemperatureSVG(value.getTemperature(), MainActivity.this));

                    currentCo2.setText(getString(R.string.ppm, value.getCo2()));
                    eCO2SVG.setImageDrawable(AssetConfigure.setEcos2SVG(value.getCo2(), MainActivity.this));

                    currentVOC.setText(df.format(value.getVOC()) + "ppm");
                    vocSVG.setImageDrawable(AssetConfigure.setVOCSVG(value.getVOC(), MainActivity.this));

                    currentHumidity.setText(df.format(value.getHumidity()) + "" + getString(R.string.percent));
                    humiditySVG.setImageDrawable(AssetConfigure.setHumiditySVG(value.getHumidity(), MainActivity.this));

                    currentDust.setText(df.format(value.getDustDensity()) + "" + getString(R.string.ug));
                } else {
                    currentTemp.setText(getString(R.string.error));
                    currentHumidity.setText(getString(R.string.error));
                    currentCo2.setText(getString(R.string.error));
                    currentVOC.setText(getString(R.string.error));
                }

                Log.d(DATABASE_READ_TAG, "Current value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(DATABASE_READ_TAG, "Failed to read value.", error.toException());
            }
        });
    }

    //https://cdn-shop.adafruit.com/product-files/3660/BME680.pdf
    private void slimChartInit(int iaqi) {
        NotificationHelper.notifyAqiIfBeyondThreshold(this, iaqi);

        final float[] stats = new float[2]; // The rings
        int[] colors = new int[2];//the colors in the rings

        if (iaqi <= 50) { // Excellent
            colors[1] = Color.rgb(0, 255, 0);
        } else if (iaqi >= 51 && iaqi <= 100) { // Good
            colors[1] = Color.rgb(146, 208, 80);
        } else if (iaqi >= 101 && iaqi <= 150) { // Lightly polluted
            colors[1] = Color.rgb(255, 255, 0);
        } else if (iaqi >= 151 && iaqi <= 200) { // Moderately Polluted
            colors[1] = Color.rgb(255, 165, 0);
        } else if (iaqi >= 201 && iaqi <= 250) { // Heavily Polluted
            colors[1] = Color.rgb(255, 0, 0);
        } else if (iaqi >= 251 && iaqi <= 350) { //Severely Polluted
            colors[1] = Color.rgb(128, 0, 128);
        } else { // > 351 Extremely Polluted
            colors[1] = Color.rgb(128, 0, 0);
        }


        colors[0] = Color.rgb(107, 107, 107);//grey outline ring

        stats[0] = 100;//This will be a grey circle to provide an outline
        stats[1] = (float) (iaqi / 500.0) * 100;

        slimChart.setStats(stats);

        slimChart.setColors(colors);
        slimChart.setText("" + iaqi);

        slimChart.setStrokeWidth(9);
    }

    public void openGraphActivity(View view) {
        Intent intent = new Intent(MainActivity.this, GraphActivity.class);
        String longID = view.getResources().getResourceName(view.getId());
        String ID = longID.replace("com.example.airzen:id/", "");
        Log.i("openSecondActivity", ID);
        String tileID = "Not Implemented";
        switch (ID) {
            case "tempTile": {
                tileID = "tempTile";
                break;
            }
            case "humidityTile": {
                tileID = "humidityTile";
                break;
            }
            case "eCO2Tile": {
                tileID = "eCO2Tile";
                break;
            }
            case "dustTile": {
                tileID = "dustTile";
                break;
            }
            case "vocTile": {
                tileID = "vocTile";
                break;
            }
        }
        intent.putExtra("TILE_ID", tileID);
        startActivity(intent);
    }
}

