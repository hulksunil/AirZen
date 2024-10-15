package com.example.airzen;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.airzen.models.SensorData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    private static final String DATABASE_READ_TAG = "DATABASE_READ_MainActivity";

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

        readFirebaseSensorData();
    }


    /**
     * Connects to the Firebase database and reads the sensor data
     * from the "current" node in the database.
     * This method is called when the activity is created.
     * The data is read anytime the "current" data changes.
     * The data is then logged to the console.
     *
     * NOTE: CHECK LOGCAT ON BOTTOM LEFT
     */
    private void readFirebaseSensorData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // gets the default instance (us-central)
        DatabaseReference myRef = database.getReference("sensorData"); // gets the reference to the database that we want to read/write to

        // write data to the database (for testing purposes)
        DatabaseReference currentDataRef = myRef.child("current");
        DatabaseReference pastValuesDataRef = myRef.child("pastValues");

        // read data anytime the "current data" changes
        currentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SensorData value =  dataSnapshot.getValue(SensorData.class);
                Log.d(DATABASE_READ_TAG, "Value is: " + value);
                Log.d(DATABASE_READ_TAG, "CO2: " + value.getCo2());
                Log.d(DATABASE_READ_TAG, "AQI: " + value.getAqi());
                Log.d(DATABASE_READ_TAG, "Temperature: " + value.getTemperature());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(DATABASE_READ_TAG, "Failed to read value.", error.toException());
            }
        });

    }
}

