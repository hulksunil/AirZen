package com.example.airzen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.TooltipPositionMode;
import com.example.airzen.models.SensorData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GraphActivity extends AppCompatActivity {

    protected AnyChartView anyChartView;
    protected AtomicReference<ArrayList<SensorData>> pastValues;
    private ArrayList<DataEntry> seriesData = new ArrayList<>();
    private String graphToDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setBackgroundColor(0);

        Intent intent = getIntent();
        graphToDisplay = intent.getStringExtra("TILE_ID");

        Log.i("graphDisplay", graphToDisplay);

        readFirebaseSensorData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void readFirebaseSensorData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // gets the default instance (us-central)
        DatabaseReference myRef = database.getReference("sensorData"); // gets the reference to the database that we want to read/write to
        readPastData(myRef);
    }

    private void readPastData(DatabaseReference myRef) {
        pastValues = new AtomicReference<>(new ArrayList<>());

        // Reference to the "pastValues" node
        DatabaseReference pastValuesDataRef = myRef.child("pastValues");

        // Listen for new data being added
        pastValuesDataRef.addChildEventListener(new ChildEventListener() {

            //!! We only care about new data being added
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                SensorData value = snapshot.getValue(SensorData.class);
                pastValues.get().add(value);

                if (pastValues.get().size() == snapshot.getChildrenCount()) {
                    Log.i("PastValues", "" + pastValues.get());
                    displayGraph(graphToDisplay);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {

                // Handle child changed if needed
                //This will be used to monitor when there is a new past value in the db
                // and can live update the specific graph
                //snapshot will need to be reassigned to the pastValues class var

                Log.i("PastValuesChanged", "" + snapshot.getValue(SensorData.class));
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
            }
        });
    }

    public void displayGraph(String message) {
        switch (message) {
            case "tempTile":
                tempGraph();
                break;
            case "humidityTile":
                humidityGraph();
                break;
            case "eCO2Tile":
                eCO2Graph();
                break;
        }
    }

    private class SensorPlotValue extends ValueDataEntry {
        SensorPlotValue(String time, Number primarySensorData) {
            super(time, primarySensorData);
        }
    }


    //temperature graph function
    private void tempGraph() {
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Your AirZen Temperature Historical Data Which Is Super Important");

        Log.i("AlexRules", "" + pastValues.get());

        for (int i = 0; i < pastValues.get().size(); i++) {
            seriesData.add(new SensorPlotValue(pastValues.get().get(i).getTimestamp(), pastValues.get().get(i).getTemperature()));
            Log.i("pastValues", "" + pastValues.get().get(i));
        }

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.color("#FF0000");
        series1.name("Temperature");

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

//        cartesian.dataArea().background().enabled(true);
//        cartesian.dataArea().background().fill("#ffd54f 0.2");
//
//        cartesian.background().enabled(true);
//        cartesian.background().fill("#3a56b0");

        anyChartView.setChart(cartesian);
    }

    private void humidityGraph() {

        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Your AirZen Humidity Historical Data Which Is Super Important");

        Log.i("AlexRules", "" + pastValues.get());

        for (int i=0; i<pastValues.get().size(); i++){
            seriesData.add(new SensorPlotValue(pastValues.get().get(i).getTimestamp(), pastValues.get().get(i).getHumidity()));
        }

        Log.i("SensorLength", "" + seriesData.size());

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.color("#326da8");
        series1.name("Humidity");


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);


//        cartesian.dataArea().background().enabled(true);
//        cartesian.dataArea().background().fill("#ffd54f 0.2");
//
//
//        cartesian.background().enabled(true);
//        cartesian.background().fill("#3a56b0");

        anyChartView.setChart(cartesian);
    }

    private void eCO2Graph() {

        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Your AirZen eCO2 Historical Data Which Is Super Important");

        Log.i("AlexRules", "" + pastValues.get());

        for (int i=0; i<pastValues.get().size(); i++){
            seriesData.add(new SensorPlotValue(pastValues.get().get(i).getTimestamp(), pastValues.get().get(i).getCo2()));
        }

        Log.i("SensorLength", "" + seriesData.size());

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.color("#32a83a");
        series1.name("CO2");


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);


//        cartesian.dataArea().background().enabled(true);
//        cartesian.dataArea().background().fill("#ffd54f 0.2");
//
//
//        cartesian.background().enabled(true);
//        cartesian.background().fill("#3a56b0");

        anyChartView.setChart(cartesian);
    }


}