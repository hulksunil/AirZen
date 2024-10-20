package com.example.airzen;

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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    private TextView currentTemp,currentHumidity,currenteCo2;

    private SlimChart slimChart;

    private final int [] AQI_Index = new int[]{50, 100, 150, 200, 300};

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

        temperatureSVG = findViewById(R.id.tempSVG);
        humiditySVG = findViewById(R.id.humiditySVG);
        eCO2SVG = findViewById(R.id.eco2SVG);


        currentTemp = findViewById(R.id.currentTemp);
        currentHumidity = findViewById(R.id.currentHumidity);
        currenteCo2 = findViewById(R.id.currenteCo2);

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
     * @param myRef
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
     * @param myRef
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

                    currentTemp.setText(value.getTemperature()+""+getString(R.string.degreesC));
                    setTemperatureSVG(value.getTemperature());

                    currenteCo2.setText(getString(R.string.ppm,value.getCo2()));
                    setEcos2SVG(value.getCo2());

                    DecimalFormat df = new DecimalFormat("#.##");

                    currentHumidity.setText(df.format(value.getHumidity())+""+getString(R.string.percent));
                    setHumiditySVG(value.getHumidity());
                }
                else{
                    currentTemp.setText(getString(R.string.error));
                    currentHumidity.setText(getString(R.string.error));
                    currenteCo2.setText(getString(R.string.error));
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

    /*public void setTemperatureSVG(double currentTemp){
        if(currentTemp > 25.00){
            temperatureSVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.thermometer_red));
        }
        else{
            temperatureSVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.thermometer_blue));
        }
    }*/

    public void setTemperatureSVG(double currentTemp){
        if(currentTemp > 35.00){
            temperatureSVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.thermometer_red));
        }
        else if(currentTemp < 35.00 && currentTemp >= 25.00){
            temperatureSVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.thermometer_orange));
        }
        else if(currentTemp < 25.00 && currentTemp >= 15.00){
            temperatureSVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.thermometer_green));
        }
        else{
            temperatureSVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.thermometer_blue));
        }
    }

    private void slimChartInit(int number){
        final float[] stats = new float[2]; // The rings
        int[] colors = new int[2];//the colors in the rings

        if(number <= 50){ //Good green
            colors[1] = Color.rgb(0, 255, 0);

        } else if (number <= 100) { // Moderate yellow
            colors[1] = Color.rgb(255, 255, 0);

        } else if (number <=150) {//Unhealthy for sensitive groups orange
            colors[1] = Color.rgb(255, 165, 0);
        }
        else if(number <= 200){//Unhealthy red
            colors[1] = Color.rgb(255, 0, 0);
        }
        else if(number <= 300) {//Very unhealthy purple
            colors[1] = Color.rgb(128, 0, 128);
        } else {// 301 and greater brown
            colors[1] = Color.rgb(128, 0, 0);
        }

        colors[0]=Color.rgb(107, 107, 107);//grey outline ring

        stats[0] = 100;//This will be a grey circle to provide an outline
        stats[1] = (float) ((number /500.0)*100);
        //Max AQI is 500 but it doesnt look at good

        slimChart.setStats(stats);

        slimChart.setColors(colors);
        slimChart.setText(""+number);

        slimChart.setStrokeWidth(9);
    }

    public void setHumiditySVG(double currentHumidity){
        if(currentHumidity >= 70 || currentHumidity < 25){
            humiditySVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.humidity_red));
        }
        else if((currentHumidity >= 60 && currentHumidity < 70) || (currentHumidity >= 25 && currentHumidity < 30)){
            humiditySVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.humidity_orange));
        }
        else if(currentHumidity >= 30 && currentHumidity < 60){
            humiditySVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.humidity_green));
        }
    }

    public void setEcos2SVG(int currentCO2){
        if(currentCO2 > 2500){
            eCO2SVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.co2_red));
        } else if (currentCO2 > 1500 && currentCO2 < 2500) {
            eCO2SVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.co2_orange));
        }
        else {
            eCO2SVG.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.co2_green));
        }
    }
}

