package com.example.airzen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.example.airzen.models.AssetConfigure;
import com.example.airzen.models.SensorData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class GraphActivity extends AppCompatActivity {

    protected AnyChartView anyChartView;
    protected AtomicReference<ArrayList<SensorData>> pastValues;
    private ArrayList<DataEntry> seriesData = new ArrayList<>();
    private String graphToDisplay;
    private Toolbar toolbar;
    private TextView pageTitle;
    private SensorData currentReadings;
    private TextView currentValueLbl;
    private ImageView currentReadSvg;
    private TextView currentRead;
    private TextView warnings;
    private ConstraintLayout warningsBox;
    private TextView additionalInfo;
    private ConstraintLayout additionalInfoBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_graph);

        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setBackgroundColor(0);

        Intent intent = getIntent();
        graphToDisplay = intent.getStringExtra("TILE_ID");

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        pageTitle = findViewById(R.id.toolbarTextView);

        currentValueLbl = findViewById(R.id.selectedCurrentValue);
        currentReadSvg = findViewById(R.id.selectedSVG);
        currentRead = findViewById(R.id.currentRead);

        warnings = findViewById(R.id.warningTextView);
        warningsBox = findViewById(R.id.warningsBox);

        additionalInfo = findViewById(R.id.additionalInfoTextView);
        additionalInfoBox = findViewById(R.id.additionalInfoBox);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        assert graphToDisplay != null;
        Log.i("graphDisplay", graphToDisplay);

        readFirebaseSensorData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Sets up the toolbar for the data activity by enabling the back button
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }

    /**
     * Inflates the menu
     * @param menu The options menu in which you place your items.
     *
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.additional_information_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Adds the functionality to the learn more menu item.
     * @param item The menu item that was selected.
     *
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.learnMoreMenu) {
            Intent learnMore = new Intent(GraphActivity.this, InformationActivity.class);
            learnMore.putExtra(getString(R.string.clickedMetric), pageTitle.getText());
            startActivity(learnMore);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Connects to the Firebase database and reads the sensor data from the "current" and "pastValues" node in the database
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
     * @param myRef the reference of the database
     */
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
                Log.i("PastValuesCount", "" + snapshot.getChildrenCount());

                // If all the data has been read, display the graph
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

    private void readCurrentData(DatabaseReference myRef) {
        DatabaseReference currentDataRef = myRef.child("current");

        // read data anytime the "current data" changes
        currentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentReadings = dataSnapshot.getValue(SensorData.class);

                DecimalFormat df = new DecimalFormat("#.##");
                switch (graphToDisplay) {
                    case "tempTile":
                        currentReadSvg.setImageDrawable(AssetConfigure.setTemperatureSVG(currentReadings.getTemperature(), GraphActivity.this));
                        currentRead.setText(df.format(currentReadings.getTemperature()) + "" + getString(R.string.degreesC));
                        temperatureWarning(currentReadings.getTemperature());
                        break;
                    case "humidityTile":
                        currentReadSvg.setImageDrawable(AssetConfigure.setHumiditySVG(currentReadings.getHumidity(), GraphActivity.this));
                        currentRead.setText(df.format(currentReadings.getHumidity()) + "" + getString(R.string.percent));
                        humidityWarning(currentReadings.getHumidity());
                        break;
                    case "eCO2Tile":
                        currentReadSvg.setImageDrawable(AssetConfigure.setEcos2SVG(currentReadings.getCo2(), GraphActivity.this));
                        currentRead.setText(getString(R.string.ppm, currentReadings.getCo2()));
                        co2Warning(currentReadings.getCo2());
                        break;
                    case "dustTile":
                        currentReadSvg.setImageDrawable(getDrawable(R.drawable.dust_icon));
                        currentRead.setText(df.format(currentReadings.getDustDensity()) + "" + getString(R.string.ug));
                        dustWarning(currentReadings.getDustDensity());
                        break;
                    case "vocTile":
                        currentReadSvg.setImageDrawable(AssetConfigure.setVOCSVG(currentReadings.getVOC(), GraphActivity.this));
                        currentRead.setText(df.format(currentReadings.getVOC()) + "ppm");
                        vocWarning(currentReadings.getVOC());
                        break;
                    default:
                        currentRead.setText(getString(R.string.NA));
                        notYetImplemented();
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void displayGraph(String message) {
        switch (message) {
            case "tempTile":
                tempGraph();
                pageTitle.setText(getString(R.string.temp));
                currentValueLbl.setText(getString(R.string.temp));
                break;
            case "humidityTile":
                humidityGraph();
                pageTitle.setText(getString(R.string.humidity));
                currentValueLbl.setText(getString(R.string.humidity));
                break;
            case "eCO2Tile":
                eCO2Graph();
                pageTitle.setText(getString(R.string.eco2));
                currentValueLbl.setText(getString(R.string.eco2));
                break;
            case "dustTile":
                currentValueLbl.setText(getString(R.string.dust));
                anyChartView.setVisibility(View.GONE);
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                break;
            case "vocTile":
                VOCGraph();
                pageTitle.setText(getString(R.string.voc));
                currentValueLbl.setText(getString(R.string.voc));
                break;
            default:
                notYetImplemented();
                break;
        }
    }

    private static class SensorPlotValue extends ValueDataEntry {
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

        for (int i = 0; i < pastValues.get().size(); i++) {
            //TODO Parse the timestamp to obtain just the time so it can look better on the graph
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

        //TODO some stying

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


        for (int i = 0; i < pastValues.get().size(); i++) {
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

        for (int i = 0; i < pastValues.get().size(); i++) {
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

    private void VOCGraph() {

        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Your AirZen VOC Historical Data Which Is Super Important");

        Log.i("AlexRules", "" + pastValues.get());

        for (int i = 0; i < pastValues.get().size(); i++) {
            seriesData.add(new SensorPlotValue(pastValues.get().get(i).getTimestamp(), pastValues.get().get(i).getVOC()));
        }

        Log.i("SensorLength", "" + seriesData.size());

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.color("#32a83a");
        series1.name("VOC");


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

    private void temperatureWarning(Double currentTemperature) {
        boolean warningSet = false;
        boolean additionalSet = false;

        additionalInfo.setText(getString(R.string.additionalInfo));
        warnings.setText(getString(R.string.warning));

        if (currentTemperature > 24 || currentTemperature < 22) {
            additionalInfo.append(getString(R.string.Temperature_workRange));
            additionalSet = true;
        }

        if (currentTemperature < 20 || currentTemperature > 22) {
            warnings.append(getString(R.string.Temperature_asthma));
            warningSet = true;
        }

        if (currentTemperature < 15) {
            additionalInfo.append(getString(R.string.Temperature_sleepTempLow));
            additionalSet = true;
        } else if (currentTemperature > 21) {
            additionalInfo.append(getString(R.string.Temperature_sleepTempHigh));
            additionalSet = true;
        }

        if (!warningSet) {
            warningsBox.setVisibility(View.GONE);
        }

        if (!additionalSet) {
            additionalInfoBox.setVisibility(View.GONE);
        }
    }

    private void humidityWarning(Double currentHumidity) {
        boolean warningSet = false;
        boolean additionalSet = false;

        additionalInfo.setText(getString(R.string.additionalInfo));
        warnings.setText(getString(R.string.warning));

        if (currentHumidity < 30 || currentHumidity > 50) {
            warnings.append(getString(R.string.Humidity_indoor));
            warnings.append(getString(R.string.Humidity_asthma));
            warningSet = true;
        }

        if (currentHumidity < 30) {
            warnings.append(getString(R.string.Humidity_low));
            warningSet = true;
        }

        if (currentHumidity > 55) {
            warnings.append(getString(R.string.Humidity_high));
            warningSet = true;
        }

        if (currentHumidity < 30 || currentHumidity > 35) {
            additionalInfo.append(getString(R.string.Humidity_winter));
            additionalSet = true;
        }

        if (currentHumidity > 50) {
            additionalInfo.append(getString(R.string.Humidity_summer));
            additionalSet = true;
        }

        if (!warningSet) {
            warningsBox.setVisibility(View.GONE);
        }

        if (!additionalSet) {
            additionalInfoBox.setVisibility(View.GONE);
        }
    }

    private void co2Warning(int currentCo2) {
        boolean warningSet = false;
        boolean additionalSet = false;

        additionalInfo.setText(getString(R.string.additionalInfo));
        warnings.setText(getString(R.string.warning));

        if (currentCo2 > 1000) {
            warnings.append(getString(R.string.CO2_ventilation));
            warnings.append(getString(R.string.CO2_longExposure));
            warningSet = true;
        }

        if (currentCo2 > 1000 && currentCo2 < 2500) {
            warnings.append(getString(R.string.CO2_drowsiness));
            warningSet = true;
        }

        if (currentCo2 > 2500) {
            warnings.append(getString(R.string.CO2_healthRisk));
            warningSet = true;
        }

        if (currentCo2 > 2000 && currentCo2 < 5000) {
            warnings.append(getString(R.string.CO2_concentration));
            warningSet = true;
        }

        if (currentCo2 <= 1000) {
            additionalInfo.append(getString(R.string.CO2_ventilation));
            additionalSet = true;
        }

        if (!warningSet) {
            warningsBox.setVisibility(View.GONE);
        }

        if (!additionalSet) {
            additionalInfoBox.setVisibility(View.GONE);
        }
    }

    private void dustWarning(double currentDust) {
        warningsBox.setVisibility(View.GONE);
        additionalInfoBox.setVisibility(View.GONE);
    }

    private void vocWarning(double currentVOC) {
        warningsBox.setVisibility(View.GONE);
        additionalInfoBox.setVisibility(View.GONE);
    }

    private void notYetImplemented() {
        anyChartView.setVisibility(View.GONE);
        findViewById(R.id.progress_bar).setVisibility(View.GONE);

        warningsBox.setVisibility(View.GONE);
        additionalInfoBox.setVisibility(View.GONE);
    }

}