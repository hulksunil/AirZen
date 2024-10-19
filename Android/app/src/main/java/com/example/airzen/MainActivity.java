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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {


    private static final String DATABASE_READ_TAG = "DATABASE_READ_TAG";
    private ListView listOfItems; // (FOR TESTING PURPOSES) (remove later)

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

        listOfItems = findViewById(R.id.listOfItems); // (FOR TESTING PURPOSES) (remove later)

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

        readPastData(myRef);
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
    private static void readCurrentData(DatabaseReference myRef) {
        // write data to the database (for testing purposes)
        DatabaseReference currentDataRef = myRef.child("current");

        // read data anytime the "current data" changes
        currentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SensorData value = dataSnapshot.getValue(SensorData.class);
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

}

