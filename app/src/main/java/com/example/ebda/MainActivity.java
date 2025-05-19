package com.example.ebda;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnGoToCardActivity;
    Button btnOurMission;
    private SharedPreferences namajPreferences;
    private static final int REQUEST_CODE_NOTIFICATION_POLICY_ACCESS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        namajPreferences = getSharedPreferences("NamajPreferences", MODE_PRIVATE);
        
        // List of prayer titles
        String[] prayerTitles = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Jummah"};

        for (String prayerTitle : prayerTitles) {
            String startTimeKey = "startTime_" + prayerTitle;
            String finishTimeKey = "finishTime_" + prayerTitle;

            // Check if prayer data is available in SharedPreferences
            if (!namajPreferences.getString(startTimeKey,"").isEmpty() && !namajPreferences.getString(finishTimeKey,"").isEmpty()) {
                goToHomeActivity();
            }
        }

        btnGoToCardActivity = findViewById(R.id.buttonGoToCardActivity);
        btnOurMission = findViewById(R.id.buttonOurMission);

        btnGoToCardActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestNotificationPolicyAccess(MainActivity.this)) {
                    goToCardActivity();
                }
                else {
                    showPermissionDialog();
                }
            }
        });
        
        // Add click listener for Our Mission button
        btnOurMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToOurMissionActivity();
            }
        });
        
        checkAndRequestNotificationPolicyAccess(MainActivity.this);
    }

    public void goToCardActivity() {
        Intent intent = new Intent(this, EditNamajActivity.class);
        startActivity(intent);
    }
    
    public void goToOurMissionActivity() {
        Intent intent = new Intent(this, OurMissionActivity.class);
        startActivity(intent);
    }
    
    public void goToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private boolean checkAndRequestNotificationPolicyAccess(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                // Permission not granted, show dialog to request it
                showPermissionDialog();
                return false;
            }
        }
        return true;
    }
    
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important Permission Required");
        builder.setMessage("This app needs access to Do Not Disturb settings to manage prayer time notifications properly. Without this permission, silent mode during prayers won't work correctly.\n\nOn the next screen, please find 'Ybaadah' in the list and toggle the switch to ON position.");
        builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestNotificationPolicyAccess();
            }
        });
        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save that user has denied permission
                SharedPreferences permPrefs = getSharedPreferences("PermissionPreferences", MODE_PRIVATE);
                permPrefs.edit().putBoolean("dnd_permission_denied", true).apply();
                
                // Show a toast explaining the consequences
                Toast.makeText(MainActivity.this,
                        "Some features will be limited. You can grant permission later in settings.", 
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void requestNotificationPolicyAccess() {
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_POLICY_ACCESS);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_NOTIFICATION_POLICY_ACCESS) {
            NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
                    notificationManager.isNotificationPolicyAccessGranted()) {
                // Permission granted
                Toast.makeText(this, "Thank you! All features are now available.", 
                        Toast.LENGTH_SHORT).show();
                
                // Clear the denied flag if it was set before
                SharedPreferences permPrefs = getSharedPreferences("PermissionPreferences", MODE_PRIVATE);
                permPrefs.edit().putBoolean("dnd_permission_denied", false).apply();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if user previously denied permission and show dialog again
        SharedPreferences permPrefs = getSharedPreferences("PermissionPreferences", MODE_PRIVATE);
        boolean permissionPreviouslyDenied = permPrefs.getBoolean("dnd_permission_denied", false);
        
        if (permissionPreviouslyDenied) {
            NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    
            // Only show if permission is still not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
                    !notificationManager.isNotificationPolicyAccessGranted()) {
                showPermissionReminderDialog();
            } else {
                // User must have granted permission through settings
                permPrefs.edit().putBoolean("dnd_permission_denied", false).apply();
            }
        }
    }
    
    private void showPermissionReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Still Required");
        builder.setMessage("This app needs access to Do Not Disturb settings to manage prayer time notifications properly. Without this permission, silent mode during prayers won't work correctly.\n\nOn the next screen, please find 'Ybaadah' in the list and toggle the switch to ON position.");
        builder.setPositiveButton("Yes, Grant Access", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestNotificationPolicyAccess();
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
