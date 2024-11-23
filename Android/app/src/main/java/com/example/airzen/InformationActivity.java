package com.example.airzen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.airzen.models.AssetConfigure;

public class InformationActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinearLayout temperatureInfo, humidityInfo, co2Info,dustInfo,vocInfo;
    private TextView notYetImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_information);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        temperatureInfo = findViewById(R.id.temperatureInfo);
        humidityInfo = findViewById(R.id.humidityInfo);
        co2Info = findViewById(R.id.co2Info);
        //dustInfo
        vocInfo = findViewById(R.id.vocInfo);


        notYetImpl = findViewById(R.id.todo);

        informationToDisplay(getIntent().getStringExtra(getString(R.string.clickedMetric)));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void informationToDisplay(String clickedMetric) {
        switch (clickedMetric) {
            case "Temperature":
                temperatureInfo.setVisibility(View.VISIBLE);
                break;
            case "Humidity":
                humidityInfo.setVisibility(View.VISIBLE);
                break;
            case "CO₂":
                co2Info.setVisibility(View.VISIBLE);
                break;
            case "VOC":
                vocInfo.setVisibility(View.VISIBLE);
                break;
            case "Dust Density":
                break;
            default:
                notYetImpl.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }
}