package com.example.airzen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView userName = findViewById(R.id.userName);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String profileName = sharedPreferences.getString("profileName", "Default Name");

        // Set the profile name to the TextView
        userName.setText(profileName);

        findViewById(R.id.accountSettings).setOnClickListener(v -> {
            // Create intent to start UserSettings activity
            Intent intent = new Intent(ProfileActivity.this, UserSettingsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Sets up the toolbar for the data activity by enabling the back button
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Create an intent to navigate back to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the activity stack to avoid returning to the current activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish the current activity
        return true;
    }
}