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
        Button startServicesButton = findViewById(R.id.startServices);
        Button stopServicesButton = findViewById(R.id.stopServices);
        namajPreferences = getSharedPreferences("NamajPreferences", MODE_PRIVATE);

        displayNamajCards();

        String[] namajTitles = {"ফজর", "যোহর", "আসর", "মাগরিব", "এশা", "জুমা"};


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

// Check if no namaj data is found, then stop the foreground service
        if (!namajDataFound) {
            stopForegroundService();
            startServicesButton.setVisibility(View.GONE);
            stopServicesButton.setVisibility(View.GONE);
        }


        editNamajButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCardActivity();
            }
        });
        startServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startForegroundService();
            }
        });
        stopServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopForegroundService();
            }
        });


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

        // List of prayer titles
        String[] prayerTitles = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Jummah"};

        for (String prayerTitle : prayerTitles) {
            String startTimeKey = "startTime_" + prayerTitle;
            String finishTimeKey = "finishTime_" + prayerTitle;

            // Check if prayer data is available in SharedPreferences
            if (!namajPreferences.getString(startTimeKey,"").isEmpty() && !namajPreferences.getString(finishTimeKey,"").isEmpty()) {

                String startTime = namajPreferences.getString(startTimeKey, "");
                String finishTime = namajPreferences.getString(finishTimeKey, "");

                try {
                    // Convert start time and finish time to 12-hour format
                    Date startTime24hr = sdf24hr.parse(startTime);
                    Date finishTime24hr = sdf24hr.parse(finishTime);
                    String startTime12hr = sdf12hr.format(startTime24hr);
                    String finishTime12hr = sdf12hr.format(finishTime24hr);

                    // Inflate the namaj card layout
                    View namajCard = inflater.inflate(R.layout.card_layouts, namajLayout, false);

                    // Set namaj title
                    TextView titleTextView = namajCard.findViewById(R.id.titleTextView);
                    titleTextView.setText(prayerTitle);

                    // Set start time (in 12-hour format)
                    TextView startTimeTextView = namajCard.findViewById(R.id.startTimeTextView);
                    startTimeTextView.setText(startTime12hr);

                    // Set finish time (in 12-hour format)
                    TextView finishTimeTextView = namajCard.findViewById(R.id.finishTimeTextView);
                    finishTimeTextView.setText(" -  " + finishTime12hr);

                    Button closeButton = namajCard.findViewById(R.id.close);
                    // Set tag for the close button to the namaj title
                    closeButton.setTag(prayerTitle);
                    namajLayout.addView(namajCard);
                    namajLayout.setVisibility(View.VISIBLE);
                    // Set click listener for the close button
                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Retrieve the namaj title from the tag
                            String namajTitleToDelete = (String) v.getTag();
                            deleteNamajPreferences(namajTitleToDelete);
                            refresh();
                        }
                    });
                    // Add the namaj card to the layout
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void deleteNamajPreferences(String namajTitle) {
        // Delete SharedPreferences values associated with the given namaj title
        SharedPreferences.Editor editor = namajPreferences.edit();
        editor.remove("startTime_" + namajTitle);
        editor.remove("finishTime_" + namajTitle);
        editor.apply();
    }
}
