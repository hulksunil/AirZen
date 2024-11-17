package com.example.airzen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class UserSettings extends AppCompatActivity {

    private TextInputLayout profileNameInput;
    private TextInputEditText profileNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initializing views
        Button saveButton = findViewById(R.id.saveButton);
        profileNameInput = findViewById(R.id.profileName);
        profileNameEditText = (TextInputEditText) profileNameInput.getEditText();

        saveButton.setOnClickListener(v -> {
            // Get the profile name entered by the user
            if (profileNameEditText != null) {
                String profileName = profileNameEditText.getText().toString().trim();
                // Check if the profile name is not empty
                if (!profileName.isEmpty()) {
                    saveProfileNameToPreferences(profileName); // Save input to SharedPreferences
                    Toast.makeText(this, "Profile name saved successfully!", Toast.LENGTH_SHORT).show();
                    // Start ProfileActivity
                    Intent intent = new Intent(UserSettings.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    // Show error message if the input is empty
                    Toast.makeText(this, "Please enter a profile name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveProfileNameToPreferences(String profileName) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profile_name", profileName);
        editor.apply(); // Commit changes asynchronously
    }

}