package com.example.airzen;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private SharedPreferences sharedPreferences;
    private Double idealTemp = 0.0;
    private Double idealHumidity = 0.0;
    private Double idealCO2 = 0.0;
    private Double idealDust = 0.0;
    private Double idealVOC = 0.0;


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

        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        if (sharedPreferences.contains("profileName")) {
            idealTemp = Double.valueOf((sharedPreferences.getString("ideal_Temp", "")));
            idealHumidity = Double.valueOf((sharedPreferences.getString("ideal_Humidity", "")));
            idealCO2 = Double.valueOf((sharedPreferences.getString("ideal_CO2", "").trim()));
            idealDust = Double.valueOf((sharedPreferences.getString("ideal_Dust_Density", "")));
            idealVOC = Double.valueOf((sharedPreferences.getString("ideal_VOC", "")));
        }

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
     *
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        finish();
        return super.onSupportNavigateUp();
    }

    /**
     * Inflates the menu
     *
     * @param menu The options menu in which you place your items.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.additional_information_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Adds the functionality to the learn more menu item.
     *
     * @param item The menu item that was selected.
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
        int desiredChildCount = 50; // Number of children to retrieve

        // Reference to the "pastValues" node
        DatabaseReference pastValuesDataRef = myRef.child("pastValues");

        // Query to get the desired number of latest children
        Query query = pastValuesDataRef.orderByKey().limitToLast(desiredChildCount); // Order by key or timestamp

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seriesData.clear();
                if (pastValues.get().isEmpty()) { // prevents the map from updating everytime there is new data
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        SensorData value = childSnapshot.getValue(SensorData.class);
                        pastValues.get().add(value);
                    }
                    displayGraph(graphToDisplay);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
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
                trimTextBoxes();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void displayGraph(String message) {
        try {
            switch (message) {
                case "tempTile":
                    if (!pastValues.get().isEmpty()) tempGraph();
                    else hideGraph();
                    pageTitle.setText(getString(R.string.temp));
                    currentValueLbl.setText(getString(R.string.temp));
                    break;
                case "humidityTile":
                    if (!pastValues.get().isEmpty()) humidityGraph();
                    else hideGraph();
                    pageTitle.setText(getString(R.string.humidity));
                    currentValueLbl.setText(getString(R.string.humidity));
                    break;
                case "eCO2Tile":
                    if (!pastValues.get().isEmpty()) eCO2Graph();
                    else hideGraph();
                    pageTitle.setText(getString(R.string.eco2));
                    currentValueLbl.setText(getString(R.string.eco2));
                    break;
                case "dustTile":
                    if (!pastValues.get().isEmpty()) dustDensityGraph();
                    else hideGraph();
                    pageTitle.setText(getString(R.string.dust));
                    currentValueLbl.setText(getString(R.string.dust));
                    break;
                case "vocTile":
                    if (!pastValues.get().isEmpty()) VOCGraph();
                    else hideGraph();
                    pageTitle.setText(getString(R.string.voc));
                    currentValueLbl.setText(getString(R.string.voc));
                    break;
                default:
                    notYetImplemented();
                    break;
            }
        } catch (SetException setException) {
            Log.e("SetException", setException.getMessage());
            finish();
        }
    }

    /**
     * Class to store the data to be displayed on the graph
     */
    private static class SensorPlotValue extends ValueDataEntry {
        SensorPlotValue(String time, Number primarySensorData) {
            super(time, primarySensorData);
        }
    }

    //temperature graph function
    private void tempGraph() throws SetException {
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        ArrayList<SensorData> sensorDataValues = pastValues.get();
        for (SensorData sensorData : sensorDataValues) {
            String time = parseSensorDataTimestamp(sensorData);

            seriesData.add(new SensorPlotValue(time, sensorData.getTemperature()));
        }

        Set set = getSet("Temperature");
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Cartesian cartesian = initCartesianGraph(series1Mapping, "Temperature", "#FF0000");

        anyChartView.setChart(cartesian);
    }

    private void humidityGraph() throws SetException {
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        ArrayList<SensorData> sensorDataValues = pastValues.get();
        for (SensorData sensorData : sensorDataValues) {
            String time = parseSensorDataTimestamp(sensorData);
            seriesData.add(new SensorPlotValue(time, sensorData.getHumidity()));
        }

        Log.i("SensorLength", "" + seriesData.size());

        Set set = getSet("Humidity");
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Cartesian cartesian = initCartesianGraph(series1Mapping, "Humidity", "#326da8");

        anyChartView.setChart(cartesian);
    }

    private void eCO2Graph() throws SetException {
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        ArrayList<SensorData> sensorDataValues = pastValues.get();
        for (SensorData sensorData : sensorDataValues) {
            String time = parseSensorDataTimestamp(sensorData);
            seriesData.add(new SensorPlotValue(time, sensorData.getCo2()));
        }

        Log.i("SensorLength", "" + seriesData.size());

        Set set = getSet("eCO2");
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Cartesian cartesian = initCartesianGraph(series1Mapping, "CO2", "#32a83a");

        anyChartView.setChart(cartesian);
    }

    private void dustDensityGraph() {

        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        ArrayList<SensorData> sensorDataValues = pastValues.get();
        for (SensorData sensorData : sensorDataValues) {
            String time = parseSensorDataTimestamp(sensorData);
            seriesData.add(new SensorPlotValue(time, sensorData.getDustDensity()));
        }

        Log.i("SensorLength", "" + seriesData.size());
        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Cartesian cartesian = initCartesianGraph(series1Mapping, "DustDensity", "#fcba03");

        anyChartView.setChart(cartesian);
    }


    private void VOCGraph() throws SetException {
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

//        Log.i("AlexRules", "" + pastValues.get());

        ArrayList<SensorData> sensorDataValues = pastValues.get();
        for (SensorData sensorData : sensorDataValues) {
            seriesData.add(new SensorPlotValue(sensorData.getTimestamp(), sensorData.getVOC()));
        }

        Log.i("SensorLength", "" + seriesData.size());
        Set set = getSet("VOC");
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'primarySensorData' }");

        Cartesian cartesian = initCartesianGraph(series1Mapping, "VOC", "#32a83a");

        anyChartView.setChart(cartesian);
    }

    private @NonNull Set getSet(String from) throws SetException {
        Set set = null;
        try {
            set = Set.instantiate();
        } catch (NullPointerException npe) {
            throw new SetException("Set Exception " + from);
        }
        return set;
    }

    private void temperatureWarning(Double currentTemperature) {
        idealPref(currentTemperature, idealTemp, getString(R.string.temp));

        if (currentTemperature > 24 || currentTemperature < 22) {
            additionalInfo.append(getString(R.string.Temperature_workRange));
        }

        if (currentTemperature < 20 || currentTemperature > 22) {
            warnings.append(getString(R.string.Temperature_asthma));
        }

        if (currentTemperature < 15) {
            additionalInfo.append(getString(R.string.Temperature_sleepTempLow));
        } else if (currentTemperature > 21) {
            additionalInfo.append(getString(R.string.Temperature_sleepTempHigh));
        }

        infoBoxesVisibility();
    }

    private void humidityWarning(Double currentHumidity) {
        idealPref(currentHumidity, idealHumidity, getString(R.string.humidity));

        if (currentHumidity < 30 || currentHumidity > 50) {
            warnings.append(getString(R.string.Humidity_asthma));
        }

        if (currentHumidity < 30) {
            warnings.append(getString(R.string.Humidity_low));
        }

        if (currentHumidity > 55) {
            warnings.append(getString(R.string.Humidity_high));
        }

        if (currentHumidity < 30 || currentHumidity > 35) {
            additionalInfo.append(getString(R.string.Humidity_winter));
        }

        if (currentHumidity > 50) {
            additionalInfo.append(getString(R.string.Humidity_summer));
        }

        if (currentHumidity >= 60){
            warnings.append(getString(R.string.Humidity_indoor));
        }

        infoBoxesVisibility();
    }

    private void co2Warning(int currentCo2) {
        idealPref(currentCo2, idealCO2, getString(R.string.eco2));

        if (currentCo2 > 1000) {
            warnings.append(getString(R.string.CO2_longExposure));
        }

        if (currentCo2 > 1000 && currentCo2 < 2500) {
            warnings.append(getString(R.string.CO2_drowsiness));
        }

        if (currentCo2 > 2500) {
            warnings.append(getString(R.string.CO2_healthRisk));
        }

        if (currentCo2 > 2000 && currentCo2 < 5000) {
            warnings.append(getString(R.string.CO2_concentration));
        }

        if (currentCo2 <= 1000) {
            additionalInfo.append(getString(R.string.CO2_ventilation));
        }
        infoBoxesVisibility();
    }

    private void dustWarning(double currentDust) {
        infoBoxesVisibility();
        //TODO
    }

    private void vocWarning(double currentVOC) {
        infoBoxesVisibility();
        //TODO
    }

    private void notYetImplemented() {
        anyChartView.setVisibility(View.GONE);
        findViewById(R.id.progress_bar).setVisibility(View.GONE);

        warningsBox.setVisibility(View.GONE);
        additionalInfoBox.setVisibility(View.GONE);
    }


    /**
     * Parses the timestamp from the sensor data into a more readable format (dd-MMM HH:mm:ss) (cuts out the year) (24-hour clock)
     * @param sensorData
     * @return
     */
    private static String parseSensorDataTimestamp(SensorData sensorData) {
        String time;
        try {
            // Parse the string into a LocalDateTime object
            LocalDateTime dateTime = LocalDateTime.parse(sensorData.getTimestamp());

            // Define the desired output format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");

            // Format the LocalDateTime into the desired format
            time = dateTime.format(formatter);
        } catch (Exception e) {
            time = sensorData.getTimestamp();
        }
        return time;
    }


    /**
     * Initializes the cartesian graph with the given parameters
     *
     * @param series1Mapping    The mapping of the data to the graph
     * @param sensorReadingType The type of sensor reading (Temperature, VOC, CO2, Dust, Humidity, etc.)
     * @param lineColor         The color of the line on the graph (HEX value)
     * @return
     */
    @NonNull
    private static Cartesian initCartesianGraph(Mapping series1Mapping, String sensorReadingType, String lineColor) {
        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Your AirZen " + sensorReadingType + " Historical Data");

        Line series1 = cartesian.line(series1Mapping);
        series1.color(lineColor);
        series1.name(sensorReadingType);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        //TODO some stying

//        cartesian.dataArea().background().enabled(true);
//        cartesian.dataArea().background().fill("#ffd54f 0.2");
//
//        cartesian.background().enabled(true);
//        cartesian.background().fill("#3a56b0");

        return cartesian;
    }

    private void hideGraph() {
        anyChartView.setVisibility(View.GONE);
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }


    /**
     * This method will compare the current metric and the ideal metric
     * and display a suggestion to do something about it
     * This also empties out the text fields to prevent duplication or stacking of information
     * due to new data coming in
     * @param current metric that the user is looking at
     * @param ideal the ideal metric that the user defined in their profile
     * @param metric the metric the user clicked on
     * @return
     */
    private void idealPref(double current, double ideal, String metric) {
        boolean isIdeal = ideal >= current;

        warnings.setText("");
        additionalInfo.setText("");

        if (isIdeal) {
            switch (metric) {
                case "Temperature":
                    warnings.append(getString(R.string.idealTempSurpass));
                    break;
                case "Humidity":
                    warnings.append(getString(R.string.idealHumiditySurpass));
                    break;
                case "CO₂":
                case "VOC":
                    warnings.append(getString(R.string.idealGasSurpass,metric.toLowerCase()));
                    break;
                case "Dust Density":
                    warnings.append(getString(R.string.idealDustSurpass));
                    break;
            }
        }
    }

    /**
     * As redundant as this looks, it is useful. This will trim the ends of the text boxes so that
     * there is not a new line under the last Warning or Additional information provided to the user
     */
    private void trimTextBoxes(){
        warnings.setText(warnings.getText().toString().trim());
        additionalInfo.setText(additionalInfo.getText().toString().trim());
    }

    /**
     * All pages need to configure when the box is being visible or not, therefore there is a centralized
     * way of doing it in a lot cleaner way
     */
    private void infoBoxesVisibility() {
        if(warnings.getText().length() > 0) warningsBox.setVisibility(View.VISIBLE);
        else warningsBox.setVisibility(View.GONE);


        if(additionalInfo.getText().length() > 0) additionalInfoBox.setVisibility(View.VISIBLE);
        else additionalInfoBox.setVisibility(View.GONE);
    }


    class SetException extends Exception {
        public SetException(String s) {
            super(s);// Call constructor of parent Exception
        }
    }
}