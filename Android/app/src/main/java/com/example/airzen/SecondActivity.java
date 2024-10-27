package com.example.airzen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        Intent intent = getIntent();
        String message = intent.getStringExtra("TILE_ID");

        /*Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String message = bundle.getString("TILE_ID");*/

        Log.i("graphDisplay", message);

        displayGraph(message);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

public void displayGraph(String message) {
        switch (message) {
        case "tempTile":
            Toast.makeText(this, "TEMP GRAPH GOES HERE", Toast.LENGTH_LONG).show();
            break;
        case "humidityTile":
            Toast.makeText(this, "HUMIDITY GRAPH GOES HERE", Toast.LENGTH_LONG).show();
            break;
        case "eCO2Tile":
            Toast.makeText(this, "CO2 GRAPH GOES HERE", Toast.LENGTH_LONG).show();
            break;
    }
}


}