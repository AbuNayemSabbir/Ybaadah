package com.example.ebda;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
// Add this import if not already present
import android.widget.RelativeLayout;
// Remove or comment out this import if it's no longer needed
// import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Locale;

public class EditNamajActivity extends AppCompatActivity {

    private SharedPreferences namajPreferences;
    private static final int NUMBER_OF_CARDS = 6;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private String fajrTitle, dhuhrTitle, asrTitle, maghribTitle, ishaTitle, jummahTitle;
    private String fajrStartTime, fajrFinishTime, dhuhrStartTime, dhuhrFinishTime,
            asrStartTime, asrFinishTime, maghribStartTime, maghribFinishTime,
            ishaStartTime, ishaFinishTime, jummahStartTime, jummahFinishTime;
    // Add active status variables for each prayer
    private boolean fajrActive, dhuhrActive, asrActive, maghribActive, ishaActive, jummahActive;

    private EditText startTimeEditText1, finishTimeEditText1;
    private EditText startTimeEditText2, finishTimeEditText2;
    private EditText startTimeEditText3, finishTimeEditText3;
    private EditText startTimeEditText4, finishTimeEditText4;
    private EditText startTimeEditText5, finishTimeEditText5;
    private EditText startTimeEditText6, finishTimeEditText6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_namaj);

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted
                        Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        // Permission denied
                        Toast.makeText(this, "Notification permission is required for proper functioning", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // The EditText views are still accessible with the same IDs
        startTimeEditText1 = findViewById(R.id.startTimeEditText1);
        finishTimeEditText1 = findViewById(R.id.finishTimeEditText1);

        startTimeEditText2 = findViewById(R.id.startTimeEditText2);
        finishTimeEditText2 = findViewById(R.id.finishTimeEditText2);

        startTimeEditText3 = findViewById(R.id.startTimeEditText3);
        finishTimeEditText3 = findViewById(R.id.finishTimeEditText3);

        startTimeEditText4 = findViewById(R.id.startTimeEditText4);
        finishTimeEditText4 = findViewById(R.id.finishTimeEditText4);

        startTimeEditText5 = findViewById(R.id.startTimeEditText5);
        finishTimeEditText5 = findViewById(R.id.finishTimeEditText5);

        startTimeEditText6 = findViewById(R.id.startTimeEditText6);
        finishTimeEditText6 = findViewById(R.id.finishTimeEditText6);

        namajPreferences = getSharedPreferences("NamajPreferences", MODE_PRIVATE);

        // Setup edit cards for each prayer
        setupEditCard("Fajr", R.id.editCard1, R.id.startTimeEditText1, R.id.finishTimeEditText1, R.id.titleTextView1);
        setupEditCard("Dhuhr", R.id.editCard2, R.id.startTimeEditText2, R.id.finishTimeEditText2, R.id.titleTextView2);
        setupEditCard("Asr", R.id.editCard3, R.id.startTimeEditText3, R.id.finishTimeEditText3, R.id.titleTextView3);
        setupEditCard("Maghrib", R.id.editCard4, R.id.startTimeEditText4, R.id.finishTimeEditText4, R.id.titleTextView4);
        setupEditCard("Isha", R.id.editCard5, R.id.startTimeEditText5, R.id.finishTimeEditText5, R.id.titleTextView5);
        setupEditCard("Jummah", R.id.editCard6, R.id.startTimeEditText6, R.id.finishTimeEditText6, R.id.titleTextView6);

        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if all time fields are properly filled
                if (validateTimeInputs()) {
                    // Check notification permission before saving
                    if (checkAndRequestNotificationPermission()) {
                        saveData();
                        Toast.makeText(EditNamajActivity.this, "Data Saved!", Toast.LENGTH_SHORT).show();
                        goToHomeActivity();
                        startForegroundService();
                    }
                } else {
                    Toast.makeText(EditNamajActivity.this, "Please fill both start and end times for each prayer", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean checkAndRequestNotificationPermission() {
        // For Android 13 (API 33) and above, we need to request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Show explanation if needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationPermissionDialog();
                    return false;
                } else {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    return false;
                }
            }
        }
        return true;
    }

    private void showNotificationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Permission Required");
        builder.setMessage("This app needs notification permission to show prayer time notifications. Please grant this permission for the app to function properly.");
        builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(EditNamajActivity.this, "Notification permission is required", Toast.LENGTH_LONG).show();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void startForegroundService() {
        try {
            Intent serviceIntent = new Intent(this, PrayerForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting service: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void goToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
    // Method to setup each edit card
    private void setupEditCard(String prayerName, int cardId, int startTimeEditTextId, int finishTimeEditTextId, int titleTextViewId) {
        // Get references to the views
        // Change this line in the setupEditCard method
        RelativeLayout cardView = findViewById(cardId);
        TextView titleTextView = findViewById(titleTextViewId);
        EditText startTimeEditText = findViewById(startTimeEditTextId);
        EditText finishTimeEditText = findViewById(finishTimeEditTextId);

        // Set the title text
        if (titleTextView != null) {
            titleTextView.setText(prayerName);
        }

        // Set up the start time edit text
        if (startTimeEditText != null) {
            // Get the saved start time or use a default value
            String startTimeKey = "startTime_" + prayerName;
            String savedStartTime = namajPreferences.getString(startTimeKey, "");
            startTimeEditText.setText(savedStartTime);

            // Set up the click listener to show the time picker dialog
            startTimeEditText.setOnClickListener(v -> showTimePickerDialog(startTimeEditText));
        }

        // Set up the finish time edit text
        if (finishTimeEditText != null) {
            // Get the saved finish time or use a default value
            String finishTimeKey = "finishTime_" + prayerName;
            String savedFinishTime = namajPreferences.getString(finishTimeKey, "");
            finishTimeEditText.setText(savedFinishTime);

            // Set up the click listener to show the time picker dialog
            finishTimeEditText.setOnClickListener(v -> showTimePickerDialog(finishTimeEditText));
        }
    }

    // Method to save data for each prayer
    private void saveData() {
        SharedPreferences.Editor editor = namajPreferences.edit();

        // Get existing active status for each prayer
        boolean existingFajrDeactivated = namajPreferences.getBoolean("deactivate_Fajr", false);
        boolean existingDhuhrDeactivated = namajPreferences.getBoolean("deactivate_Dhuhr", false);
        boolean existingAsrDeactivated = namajPreferences.getBoolean("deactivate_Asr", false);
        boolean existingMaghribDeactivated = namajPreferences.getBoolean("deactivate_Maghrib", false);
        boolean existingIshaDeactivated = namajPreferences.getBoolean("deactivate_Isha", false);
        boolean existingJummahDeactivated = namajPreferences.getBoolean("deactivate_Jummah", false);

        fajrStartTime = startTimeEditText1.getText().toString();
        fajrFinishTime = finishTimeEditText1.getText().toString();
        editor.putString("Fajr", "Fajr");
        editor.putString("startTime_Fajr", fajrStartTime);
        editor.putString("finishTime_Fajr", fajrFinishTime);
        // Set active status to true if both start and finish times are provided
        fajrActive = !fajrStartTime.isEmpty() && !fajrFinishTime.isEmpty();
        editor.putBoolean("active_Fajr", fajrActive);
        // Preserve the user's deactivation choice
        editor.putBoolean("deactivate_Fajr", existingFajrDeactivated);

        dhuhrStartTime = startTimeEditText2.getText().toString();
        dhuhrFinishTime = finishTimeEditText2.getText().toString();
        editor.putString("Dhuhr", "Dhuhr");
        editor.putString("startTime_Dhuhr", dhuhrStartTime);
        editor.putString("finishTime_Dhuhr", dhuhrFinishTime);
        dhuhrActive = !dhuhrStartTime.isEmpty() && !dhuhrFinishTime.isEmpty();
        editor.putBoolean("active_Dhuhr", dhuhrActive);
        editor.putBoolean("deactivate_Dhuhr", existingDhuhrDeactivated);

        asrStartTime = startTimeEditText3.getText().toString();
        asrFinishTime = finishTimeEditText3.getText().toString();
        editor.putString("Asr", "Asr");
        editor.putString("startTime_Asr", asrStartTime);
        editor.putString("finishTime_Asr", asrFinishTime);
        asrActive = !asrStartTime.isEmpty() && !asrFinishTime.isEmpty();
        editor.putBoolean("active_Asr", asrActive);
        editor.putBoolean("deactivate_Asr", existingAsrDeactivated);

        maghribStartTime = startTimeEditText4.getText().toString();
        maghribFinishTime = finishTimeEditText4.getText().toString();
        editor.putString("Maghrib", "Maghrib");
        editor.putString("startTime_Maghrib", maghribStartTime);
        editor.putString("finishTime_Maghrib", maghribFinishTime);
        maghribActive = !maghribStartTime.isEmpty() && !maghribFinishTime.isEmpty();
        editor.putBoolean("active_Maghrib", maghribActive);
        editor.putBoolean("deactivate_Maghrib", existingMaghribDeactivated);

        ishaStartTime = startTimeEditText5.getText().toString();
        ishaFinishTime = finishTimeEditText5.getText().toString();
        editor.putString("Isha", "Isha");
        editor.putString("startTime_Isha", ishaStartTime);
        editor.putString("finishTime_Isha", ishaFinishTime);
        ishaActive = !ishaStartTime.isEmpty() && !ishaFinishTime.isEmpty();
        editor.putBoolean("active_Isha", ishaActive);
        editor.putBoolean("deactivate_Isha", existingIshaDeactivated);

        jummahStartTime = startTimeEditText6.getText().toString();
        jummahFinishTime = finishTimeEditText6.getText().toString();
        editor.putString("Jummah", "Jummah");
        editor.putString("startTime_Jummah", jummahStartTime);
        editor.putString("finishTime_Jummah", jummahFinishTime);
        jummahActive = !jummahStartTime.isEmpty() && !jummahFinishTime.isEmpty();
        editor.putBoolean("active_Jummah", jummahActive);
        editor.putBoolean("deactivate_Jummah", existingJummahDeactivated);

        editor.apply();
    }

    // Method to show time picker dialog
    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        editText.setText(formattedTime);
                    }
                },
                hour,
                minute,
                android.text.format.DateFormat.is24HourFormat(this)
        );

        timePickerDialog.show();
    }

    // Add this new method to validate time inputs
    private boolean validateTimeInputs() {
        boolean isValid = isPairValid(startTimeEditText1.getText().toString(), finishTimeEditText1.getText().toString());

        // Check each pair of time inputs
        if (!isPairValid(startTimeEditText2.getText().toString(), finishTimeEditText2.getText().toString())) isValid = false;
        if (!isPairValid(startTimeEditText3.getText().toString(), finishTimeEditText3.getText().toString())) isValid = false;
        if (!isPairValid(startTimeEditText4.getText().toString(), finishTimeEditText4.getText().toString())) isValid = false;
        if (!isPairValid(startTimeEditText5.getText().toString(), finishTimeEditText5.getText().toString())) isValid = false;
        if (!isPairValid(startTimeEditText6.getText().toString(), finishTimeEditText6.getText().toString())) isValid = false;

        return isValid;
    }

    private boolean isPairValid(String startTime, String finishTime) {
        // Either both must be filled or both must be empty
        return (startTime.isEmpty() && finishTime.isEmpty()) ||
                (!startTime.isEmpty() && !finishTime.isEmpty());
    }
}