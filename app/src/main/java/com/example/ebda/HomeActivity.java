package com.example.ebda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout namajLayout;
    private SharedPreferences namajPreferences;
    boolean namajDataFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        namajLayout = findViewById(R.id.namajLayout);
        Button editNamajButton = findViewById(R.id.editNamajButton);

        namajPreferences = getSharedPreferences("NamajPreferences", MODE_PRIVATE);

        displayNamajCards();

        String[] namajTitles = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Jummah"};

        for (String namajTitle : namajTitles) {
            String startTimeKey = "startTime_" + namajTitle;
            String finishTimeKey = "finishTime_" + namajTitle;

            // Check if namaj data is available in SharedPreferences
            if (!namajPreferences.getString(startTimeKey, "").isEmpty() &&
                    !namajPreferences.getString(finishTimeKey, "").isEmpty()) {
                namajDataFound = true;
                break; // Exit the loop as soon as namaj data is found
            }
        }

        editNamajButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCardActivity();
            }
        });

        // Add warning notification button functionality
        Button warningNotificationButton = findViewById(R.id.warningNotificationButton);
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean("warningNotificationsEnabled", true);
        
        // Set initial button text based on current state
        updateWarningButtonText(warningNotificationButton, notificationsEnabled);
        
        // In the warningNotificationButton onClick listener
        warningNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle notification state
                boolean currentState = preferences.getBoolean("warningNotificationsEnabled", true);
                boolean newState = !currentState;
                
                // Save the new state
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("warningNotificationsEnabled", newState);
                editor.apply();
                
                // Update button text
                updateWarningButtonText(warningNotificationButton, newState);
                
                // Show feedback to user
                String message = newState ? "Warning notifications enabled" : "Warning notifications disabled";
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                
                // Restart the service to apply changes if any prayer is active
                if (isAnyPrayerActive()) {
                    stopForegroundService();
                    startForegroundService();
                }
            }
        });
    }

    // Helper method for warning notification button
    private void updateWarningButtonText(Button button, boolean enabled) {
        if (enabled) {
            button.setText("Disable Warning Notifications");
        } else {
            button.setText("Enable Warning Notifications");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }

    public void goToCardActivity() {
        Intent intent = new Intent(this, EditNamajActivity.class);
        startActivity(intent);
    }
    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, PrayerForegroundService.class);
        stopService(serviceIntent);

        startService(serviceIntent);
        Toast.makeText(HomeActivity.this, "Prayer silent system activated!", Toast.LENGTH_SHORT).show();

    }

    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, PrayerForegroundService.class);
        stopService(serviceIntent);
        Toast.makeText(HomeActivity.this, "Prayer silent system deactivated!", Toast.LENGTH_SHORT).show();

    }
    public void refresh() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);


    }
    private void displayNamajCards() {
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat sdf24hr = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat sdf12hr = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // 12-hour format
    
        // Clear existing cards
        namajLayout.removeAllViews();
    
        // List of prayer titles
        String[] prayerTitles = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Jummah"};
    
        for (String prayerTitle : prayerTitles) {
            String startTimeKey = "startTime_" + prayerTitle;
            String finishTimeKey = "finishTime_" + prayerTitle;
            String activeKey = "active_" + prayerTitle;
            String deactivateKey = "deactivate_" + prayerTitle;
    
            // Check if prayer data is available in SharedPreferences
            if (!namajPreferences.getString(startTimeKey, "").isEmpty() &&
                !namajPreferences.getString(finishTimeKey, "").isEmpty()) {
    
                View cardView = inflater.inflate(R.layout.prayer_card, namajLayout, false);
    
                TextView titleTextView = cardView.findViewById(R.id.cardTitleTextView);
                TextView timeTextView = cardView.findViewById(R.id.cardTimeTextView);
                Switch activeSwitch = cardView.findViewById(R.id.activeSwitch);
    
                String startTime = namajPreferences.getString(startTimeKey, "");
                String finishTime = namajPreferences.getString(finishTimeKey, "");
                boolean isActive = namajPreferences.getBoolean(activeKey, true);
                boolean isDeactivated = namajPreferences.getBoolean(deactivateKey, false);
    
                // Format times for display
                String displayStartTime = "";
                String displayFinishTime = "";
                try {
                    Date startDate = sdf24hr.parse(startTime);
                    Date finishDate = sdf24hr.parse(finishTime);
                    if (startDate != null && finishDate != null) {
                        displayStartTime = sdf12hr.format(startDate);
                        displayFinishTime = sdf12hr.format(finishDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
    
                titleTextView.setText(prayerTitle);
                timeTextView.setText(String.format("%s - %s", displayStartTime, displayFinishTime));
    
                // Set switch state and text based on active and deactivate status
                activeSwitch.setChecked(!isDeactivated && isActive);
                activeSwitch.setText(isDeactivated ? "Activate" : "Deactivate");
    
                // Set switch listener to update active/deactivate status
                final String finalPrayerTitle = prayerTitle;
                activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // Stop the service first
                        stopForegroundService();
    
                        // Update only this prayer's deactivation status
                        SharedPreferences.Editor editor = namajPreferences.edit();
                        editor.putBoolean(deactivateKey, !isChecked);
                        editor.apply();
    
                        // Update switch text
                        activeSwitch.setText(isChecked ? "Deactivate" : "Activate");
    
                        if (isChecked) {
                            Toast.makeText(HomeActivity.this, finalPrayerTitle + " activated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomeActivity.this, finalPrayerTitle + " deactivated", Toast.LENGTH_SHORT).show();
                        }
    
                        // Restart the service if any prayer is still active
                        if (isAnyPrayerActive()) {
                            startForegroundService();
                        }
                    }
                });
    
                namajLayout.addView(cardView);
            }
        }
    }
    
    // Helper method to check if any prayer is active
    private boolean isAnyPrayerActive() {
        String[] prayerTitles = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Jummah"};
        
        for (String prayerTitle : prayerTitles) {
            String activeKey = "active_" + prayerTitle;
            String deactivateKey = "deactivate_" + prayerTitle;
            
            boolean isActive = namajPreferences.getBoolean(activeKey, false);
            boolean isDeactivated = namajPreferences.getBoolean(deactivateKey, true);
            
            if (isActive && !isDeactivated) {
                return true;
            }
        }
        
        return false;
    }
    private void deleteNamajPreferences(String namajTitle) {
        // Delete SharedPreferences values associated with the given namaj title
        SharedPreferences.Editor editor = namajPreferences.edit();
        editor.remove("startTime_" + namajTitle);
        editor.remove("finishTime_" + namajTitle);
        editor.apply();
    }
}
