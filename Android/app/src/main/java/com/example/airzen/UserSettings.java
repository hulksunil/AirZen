package com.example.airzen;

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

    private TextInputLayout idealTempInput;
    private TextInputEditText idealTempEditText;

    private TextInputLayout idealHumidityInput;
    private TextInputEditText idealHumidityEditText;

    private TextInputLayout idealCO2Input;
    private TextInputEditText idealCO2EditText;

    private TextInputLayout idealDustDensityInput;
    private TextInputEditText idealDustDensityEditText;

    private TextInputLayout idealVOCInput;
    private TextInputEditText idealVOCEditText;

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

        idealTempInput = findViewById(R.id.userIdealTemp);
        idealTempEditText = (TextInputEditText) idealTempInput.getEditText();


        idealHumidityInput = findViewById(R.id.userIdealHumidity);
        idealHumidityEditText = (TextInputEditText) idealHumidityInput.getEditText();


        idealCO2Input = findViewById(R.id.userIdealCO2);
        idealCO2EditText = (TextInputEditText) idealCO2Input.getEditText();


        idealDustDensityInput = findViewById(R.id.userIdealDustDensity);
        idealDustDensityEditText = (TextInputEditText) idealDustDensityInput.getEditText();


        idealVOCInput = findViewById(R.id.userIdealVOC);
        idealVOCEditText = (TextInputEditText) idealVOCInput.getEditText();

        loadPreferences();

        saveButton.setOnClickListener(v -> {
            // Get the profile name entered by the user
            boolean allInputsValid = true;

            if (profileNameEditText != null) {
                String profileName = profileNameEditText.getText().toString().trim();
                // Check if the profile name is not empty
                if (!profileName.isEmpty()) {
                    allInputsValid = true;
                    saveProfileNameToSharedPreferences(profileName);
                } else {
                    // Show error message if the input is empty
                    Toast.makeText(this, "Please enter a profile name", Toast.LENGTH_SHORT).show();
                    allInputsValid = false;
                }
            }

            String idealTemp = idealTempEditText.getText().toString().trim();
            String idealHumidity = idealHumidityEditText.getText().toString().trim();
            String idealCO2 = idealCO2EditText.getText().toString().trim();
            String idealDustDensity = idealDustDensityEditText.getText().toString().trim();
            String idealVOC = idealVOCEditText.getText().toString().trim();

            if (!idealTemp.isEmpty() && !isValidDecimal(idealTemp)) {
                Toast.makeText(this, "Please enter a valid temperature (decimal number)", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else if(!isValidRange(idealTemp,0,35)){
                Toast.makeText(this, "Temperature must be between 0°C and 35°C", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else{
                saveIdealTempToSharedPreferences(idealTemp);
            }

            if (!idealHumidity.isEmpty() && !isValidDecimal(idealHumidity)) {
                Toast.makeText(this, "Please enter a valid humidity (decimal number)", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else if(!isValidRange(idealHumidity, 30, 55)){
                Toast.makeText(this, "Humidity must be between 30% and 55%", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else{
                saveIdealHumidityToSharedPreferences(idealHumidity);
            }

            if (!idealCO2.isEmpty() && !isValidDecimal(idealCO2)) {
                Toast.makeText(this, "Please enter a valid CO2 level (decimal number)", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else if (!isValidRange(idealCO2, 400, 1000)){
                Toast.makeText(this, "CO2 level must be between 400 and 1000 ppm", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else{
                saveIdealCO2ToSharedPreferences(idealCO2);
            }

            if (!idealDustDensity.isEmpty() && !isValidDecimal(idealDustDensity)) {
                Toast.makeText(this, "Please enter a valid dust density (decimal number)", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else if(!isValidRange(idealDustDensity, 0, 150)){
                Toast.makeText(this, "Dust Density level must be between 0 and 150 μg/m³", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else {
                saveIdealDustDensityToSharedPreferences(idealDustDensity);
            }

            if (!idealVOC.isEmpty() && !isValidDecimal(idealVOC)) {
                Toast.makeText(this, "Please enter a valid VOC level (decimal number)", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else if(!isValidRange(idealVOC, 0, 0.5)){
                Toast.makeText(this, "VOC level must be between 0 and 0.5 ppm", Toast.LENGTH_SHORT).show();
                allInputsValid = false;
            }
            else {
                saveIdealVOCToSharedPreferences(idealVOC);
            }

            // If all inputs are valid, save them to SharedPreferences
            if (allInputsValid) {
                //savePreferences();
                Toast.makeText(this, "All inputs saved successfully!", Toast.LENGTH_SHORT).show();

                finish(); // Close the activity
            }
        });
    }

    private boolean isValidDecimal(String value) {
        try {
            // Attempt to parse the string as a double
            Double.parseDouble(value);
            return true; // If parsing succeeds, it's a valid decimal
        } catch (NumberFormatException e) {
            return false; // If parsing fails, it's not valid
        }
    }

    private boolean isValidRange(String value, double min, double max) {
        try {
            double doubleValue = Double.parseDouble(value);
            return doubleValue >= min && doubleValue <= max;
        } catch (NumberFormatException e) {
            return false; // Invalid input (not a number)
        }
    }

    private void saveProfileNameToSharedPreferences(String profileName) {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the data
        editor.putString("profileName", profileName );

        // Commit changes
        editor.apply(); // Alternatively, use .commit() for synchronous save
    }

    private void saveIdealTempToSharedPreferences(String idealTemp) {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the data
        editor.putString("ideal_Temp", idealTemp);

        // Commit changes
        editor.apply(); // Alternatively, use .commit() for synchronous save
    }

    private void saveIdealHumidityToSharedPreferences(String idealHumidity) {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the data
        editor.putString("ideal_Humidity", idealHumidity);

        // Commit changes
        editor.apply(); // Alternatively, use .commit() for synchronous save
    }

    private void saveIdealCO2ToSharedPreferences(String idealCO2) {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the data
        editor.putString("ideal_CO2", idealCO2);

        // Commit changes
        editor.apply(); // Alternatively, use .commit() for synchronous save
    }

    private void saveIdealDustDensityToSharedPreferences(String idealDustDensity) {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the data
        editor.putString("ideal_Dust_Density", idealDustDensity);

        // Commit changes
        editor.apply(); // Alternatively, use .commit() for synchronous save
    }

    private void saveIdealVOCToSharedPreferences(String idealVOC) {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the data
        editor.putString("ideal_VOC", idealVOC);

        // Commit changes
        editor.apply(); // Alternatively, use .commit() for synchronous save
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        // Check if the EditText fields are initialized and set their values
        if (profileNameEditText != null) {
            profileNameEditText.setText(sharedPreferences.getString("profileName", ""));
        }
        if (idealTempEditText != null) {
            idealTempEditText.setText(sharedPreferences.getString("ideal_Temp", ""));
        }
        if (idealHumidityEditText != null) {
            idealHumidityEditText.setText(sharedPreferences.getString("ideal_Humidity", ""));
        }
        if (idealCO2EditText != null) {
            idealCO2EditText.setText(sharedPreferences.getString("ideal_CO2", ""));
        }
        if (idealDustDensityEditText != null) {
            idealDustDensityEditText.setText(sharedPreferences.getString("ideal_Dust_Density", ""));
        }
        if (idealVOCEditText != null) {
            idealVOCEditText.setText(sharedPreferences.getString("ideal_VOC", ""));
        }
    }

}