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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
                // Check notification permission before saving
                if (checkAndRequestNotificationPermission()) {
                    saveData();
                    Toast.makeText(EditNamajActivity.this, "Data Saved!", Toast.LENGTH_SHORT).show();
                    goToHomeActivity();
                    startForegroundService();
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
    // Method to setup each edit card
    private void setupEditCard(final String namajTitle, int cardLayoutId, int startTimeEditTextId,
                               int finishTimeEditTextId, int titleTextViewId/*, int shareButtonId*/) {
        RelativeLayout cardView = findViewById(cardLayoutId);
        final EditText startTimeEditText = cardView.findViewById(startTimeEditTextId);
        final EditText finishTimeEditText = cardView.findViewById(finishTimeEditTextId);


        // Retrieve existing data and populate EditText fields
        String startTimeKey = "startTime_" + namajTitle;
        String finishTimeKey = "finishTime_" + namajTitle;
        String startTime = namajPreferences.getString(startTimeKey, "");
        String finishTime = namajPreferences.getString(finishTimeKey, "");
        startTimeEditText.setText(startTime);
        finishTimeEditText.setText(finishTime);



        startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(startTimeEditText);
            }
        });

        finishTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(finishTimeEditText);
            }
        });


    }

    // Method to save data for each prayer
    private void saveData() {
        SharedPreferences.Editor editor = namajPreferences.edit();
        
        fajrStartTime = startTimeEditText1.getText().toString();
        fajrFinishTime = finishTimeEditText1.getText().toString();
        editor.putString("Fajr", fajrTitle);
        editor.putString("startTime_Fajr", fajrStartTime);
        editor.putString("finishTime_Fajr", fajrFinishTime);
        
        dhuhrStartTime = startTimeEditText2.getText().toString();
        dhuhrFinishTime = finishTimeEditText2.getText().toString();
        editor.putString("Dhuhr", dhuhrTitle);
        editor.putString("startTime_Dhuhr", dhuhrStartTime);
        editor.putString("finishTime_Dhuhr", dhuhrFinishTime);
        
        asrStartTime = startTimeEditText3.getText().toString();
        asrFinishTime = finishTimeEditText3.getText().toString();
        editor.putString("Asr", asrTitle);
        editor.putString("startTime_Asr", asrStartTime);
        editor.putString("finishTime_Asr", asrFinishTime);
        
        maghribStartTime = startTimeEditText4.getText().toString();
        maghribFinishTime = finishTimeEditText4.getText().toString();
        editor.putString("Maghrib", maghribTitle);
        editor.putString("startTime_Maghrib", maghribStartTime);
        editor.putString("finishTime_Maghrib", maghribFinishTime);
        
        ishaStartTime = startTimeEditText5.getText().toString();
        ishaFinishTime = finishTimeEditText5.getText().toString();
        editor.putString("Isha", ishaTitle);
        editor.putString("startTime_Isha", ishaStartTime);
        editor.putString("finishTime_Isha", ishaFinishTime);
        
        jummahStartTime = startTimeEditText6.getText().toString();
        jummahFinishTime = finishTimeEditText6.getText().toString();
        editor.putString("Jummah", jummahTitle);
        editor.putString("startTime_Jummah", jummahStartTime);
        editor.putString("finishTime_Jummah", jummahFinishTime);

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
}