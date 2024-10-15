package com.example.airzen;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {


    private static final String DATABASE_READ_TAG = "DATABASE_READ_TAG";

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
     * <p>
     * NOTE: CHECK LOGCAT ON BOTTOM LEFT
     */
    private void readFirebaseSensorData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // gets the default instance (us-central)
        DatabaseReference myRef = database.getReference("sensorData"); // gets the reference to the database that we want to read/write to

        readCurrentData(myRef);

        readPastData(myRef);

    }

    private void readPastData(DatabaseReference myRef) {
        Log.d(DATABASE_READ_TAG, "Reading PAST VALUE data from the database");
        AtomicReference<ArrayList<SensorData>> pastValues = new AtomicReference<>(new ArrayList<>());

        // Reference to the "pastValues" node
        DatabaseReference pastValuesDataRef = myRef.child("pastValues");

        // Listen for new data being added
        pastValuesDataRef.addChildEventListener(new ChildEventListener() {

            //!! We only care about new data being added
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,  String previousChildName) {
                SensorData value = snapshot.getValue(SensorData.class);
                pastValues.get().add(value);
                Log.d(DATABASE_READ_TAG, "New PAST VALUE added: " + snapshot.getKey());

                Log.d(DATABASE_READ_TAG, "Value is: " + value);
                Log.d(DATABASE_READ_TAG, "CO2: " + value.getCo2());
                Log.d(DATABASE_READ_TAG, "AQI: " + value.getAqi());
                Log.d(DATABASE_READ_TAG, "Temperature: " + value.getTemperature());

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
            public void onChildMoved(@NonNull DataSnapshot snapshot,  String previousChildName) {
                // Handle child moved if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DATABASE_READ_TAG, "Error listening for new data", error.toException());
            }
        });
    }

    private static void readCurrentData(DatabaseReference myRef) {
        // write data to the database (for testing purposes)
        DatabaseReference currentDataRef = myRef.child("current");

        // read data anytime the "current data" changes
        currentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SensorData value = dataSnapshot.getValue(SensorData.class);
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

