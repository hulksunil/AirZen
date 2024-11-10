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

    protected ConstraintLayout tempTile, humidityTile, eCO2Tile;
    private ImageView temperatureSVG,humiditySVG,eCO2SVG;

    private TextView currentTemp,currentHumidity, currentCo2;

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

        temperatureSVG = findViewById(R.id.tempSVG);
        humiditySVG = findViewById(R.id.humiditySVG);
        eCO2SVG = findViewById(R.id.eco2SVG);

        currentTemp = findViewById(R.id.currentTemp);
        currentHumidity = findViewById(R.id.currentHumidity);
        currentCo2 = findViewById(R.id.currenteCo2);

        slimChart= findViewById(R.id.slimChart);

        readFirebaseSensorData();
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
     * Reads the past data from the database
     * The data is read anytime a new child value is given to the "pastValues" node.
     *
     * @param myRef the reference of the database
     */
    private void readPastData(DatabaseReference myRef) {
        Log.d(DATABASE_READ_TAG, "Reading PAST VALUE data from the database");
        AtomicReference<ArrayList<SensorData>> pastValues = new AtomicReference<>(new ArrayList<>());

        // Reference to the "pastValues" node
        DatabaseReference pastValuesDataRef = myRef.child("pastValues");

        // Listen for new data being added
        pastValuesDataRef.addChildEventListener(new ChildEventListener() {

            //!! We only care about new data being added
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                SensorData value = snapshot.getValue(SensorData.class);
                pastValues.get().add(value);
                Log.d(DATABASE_READ_TAG, "New PAST VALUE added: " + snapshot.getKey() + " : " + value);

                // FOR TESTING PURPOSES (remove later)
                listOfItems.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, pastValues.get()));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle child changed if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle child removed if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle child moved if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DATABASE_READ_TAG, "Error listening for new data", error.toException());
            }
        });
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
                if(value != null){
                    slimChartInit(value.getAqi());

                    DecimalFormat df = new DecimalFormat("#.##");

                    currentTemp.setText(df.format(value.getTemperature())+""+getString(R.string.degreesC));
                    temperatureSVG.setImageDrawable(AssetConfigure.setTemperatureSVG(value.getTemperature(),MainActivity.this));

                    currentCo2.setText(getString(R.string.ppm,value.getCo2()));
                    eCO2SVG.setImageDrawable(AssetConfigure.setEcos2SVG(value.getCo2(),MainActivity.this));

                    currentHumidity.setText(df.format(value.getHumidity())+""+getString(R.string.percent));
                    humiditySVG.setImageDrawable(AssetConfigure.setHumiditySVG(value.getHumidity(),MainActivity.this));
                }
                else{
                    currentTemp.setText(getString(R.string.error));
                    currentHumidity.setText(getString(R.string.error));
                    currentCo2.setText(getString(R.string.error));
                }

                Log.d(DATABASE_READ_TAG, "Current value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(DATABASE_READ_TAG, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * (FOR TESTING PURPOSES) (remove later)
     * Called when the button is clicked
     * Writes a new SensorData object to the database in the "pastValues" node
     *
     * @param view
     */
    public void onButtonClicked(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // gets the default instance (us-central)
        DatabaseReference myRef = database.getReference("sensorData"); // gets the reference to the database that we want to read/write to

        // Do something in response to button click
        DatabaseReference pastValuesDataRef = myRef.child("pastValues");
        DatabaseReference newNode = pastValuesDataRef.push();
        newNode.setValue(new SensorData(100, 200, 50.43, 1000.23, 19.29, LocalDateTime.now().toString()));
    }

//https://atmotube.com/atmocube-support/indoor-air-quality-index-iaqi#:~:text=IAQI%20Categories%20and%20Breakpoint%20Table
    private void slimChartInit(int iaqi){
        final float[] stats = new float[2]; // The rings
        int[] colors = new int[2];//the colors in the rings

        if(iaqi <= 50){ // Excellent
            colors[1] = Color.rgb(0, 255, 0);
        }
        else if (iaqi >= 51 && iaqi <= 100) { // Good
            colors[1] = Color.rgb(146, 208, 80);
        }
        else if (iaqi >= 101 && iaqi <= 150) { // Lightly polluted
            colors[1] = Color.rgb(255, 255, 0);
        }
        else if (iaqi >= 151 && iaqi <= 200) { // Moderately Polluted
            colors[1] = Color.rgb(255, 165, 0);
        }
        else if (iaqi >= 201 && iaqi <= 250) { // Heavily Polluted
            colors[1] = Color.rgb(255, 0, 0);
        }
        else if (iaqi >= 251 && iaqi <= 350) { //Severely Polluted
            colors[1] = Color.rgb(128, 0, 128);
        }
        else { // > 351 Extremely Polluted
            colors[1] = Color.rgb(128, 0, 0);
        }


        colors[0]=Color.rgb(107, 107, 107);//grey outline ring

        stats[0] = 100;//This will be a grey circle to provide an outline
        stats[1] = (float) (iaqi/500.0)*100;

        slimChart.setStats(stats);

        slimChart.setColors(colors);
        slimChart.setText(""+iaqi);

        slimChart.setStrokeWidth(9);
    }

    public void openGraphActivity(View view){
        Intent intent = new Intent(MainActivity.this, GraphActivity.class);
        String longID = view.getResources().getResourceName(view.getId());
        String ID = longID.replace("com.example.airzen:id/", "");
        Log.i("openSecondActivity",ID);
        switch (ID) {
            case "tempTile": {
                String tileID = "tempTile";
                intent.putExtra("TILE_ID", tileID);
                break;
            }
            case "humidityTile": {
                String tileID = "humidityTile";
                intent.putExtra("TILE_ID", tileID);
                break;
            }
            case "eCO2Tile": {
                String tileID = "eCO2Tile";
                intent.putExtra("TILE_ID", tileID);
                break;
            }
        }
        startActivity(intent);
    }
}

