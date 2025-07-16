package com.example.ebda;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PrayerForegroundService extends Service {
    private static final int NOTIFICATION_ID = 12345678; // Unique ID for the notification
    private static final String CHANNEL_ID = "PrayerChannel"; // Notification channel ID
    private static final CharSequence CHANNEL_NAME = "PrayerChannel"; // Notification channel name

    private static final String WARNING_CHANNEL_ID = "PrayerWarningChannel";
    private static final CharSequence WARNING_CHANNEL_NAME = "Prayer Warning Channel";
    private static final int WARNING_NOTIFICATION_ID = 87654321;

    // Track which prayers have had warnings shown
    private final Set<String> warningsShown = new HashSet<>();
    private SharedPreferences namajPreferences;
    
    // Add these two declarations
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        namajPreferences = getSharedPreferences("NamajPreferences", Context.MODE_PRIVATE);

        // Create notification channels
        createNotificationChannel();
        createWarningNotificationChannel();

        // Create and show notification immediately (required for Android 12+)
        Notification notification = createNotification("Prayer time silent mode activated!");
        startForeground(NOTIFICATION_ID, notification);

        // Start a handler to run the runnable every second
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                checkPrayerTimes();
                checkWarningTimes();
                handler.postDelayed(this, 1000); // Repeat every second
            }
        };
        handler.post(runnable);
    }

    private void createWarningNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    WARNING_CHANNEL_ID,
                    WARNING_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Use HIGH importance for warning
            );
            channel.setDescription("Prayer time warning notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});
            channel.setShowBadge(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void checkWarningTimes() {
        // Check if warning notifications are enabled
        SharedPreferences appPrefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean warningNotificationsEnabled = appPrefs.getBoolean("warningNotificationsEnabled", true);
        
        if (!warningNotificationsEnabled) {
            return;
        }
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(calendar.getTime());
        
        // Check regular prayers
        checkWarningTime("Fajr", currentTime, calendar);
        checkWarningTime("Dhuhr", currentTime, calendar);
        checkWarningTime("Asr", currentTime, calendar);
        checkWarningTime("Maghrib", currentTime, calendar);
        checkWarningTime("Isha", currentTime, calendar);
        
        // Check Jummah prayer only on Friday
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            checkWarningTime("Jummah", currentTime, calendar);
        }
    }

    private void checkWarningTime(String prayerTitle, String currentTime, Calendar calendar) {
        String startTimeKey = "startTime_" + prayerTitle;
        String activeKey = "active_" + prayerTitle;
        String deactivateKey = "deactivate_" + prayerTitle;
        
        String startTime = namajPreferences.getString(startTimeKey, "");
        boolean isActive = namajPreferences.getBoolean(activeKey, true);
        boolean isDeactivated = namajPreferences.getBoolean(deactivateKey, false);
        
        // Skip if prayer is not active or deactivated or no start time
        if (!isActive || isDeactivated || startTime.isEmpty()) {
            return;
        }
        
        try {
            // Calculate warning time (5 minutes before prayer start)
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date prayerStartDate = sdf.parse(startTime);
            
            if (prayerStartDate != null) {
                Calendar prayerStartCal = Calendar.getInstance();
                prayerStartCal.setTime(prayerStartDate);
                prayerStartCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                prayerStartCal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                prayerStartCal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                
                // Calculate warning time (5 minutes before)
                Calendar warningCal = (Calendar) prayerStartCal.clone();
                warningCal.add(Calendar.MINUTE, -5);
                
                // Format warning time
                String warningTime = sdf.format(warningCal.getTime());
                
                // Check if current time matches warning time and we haven't shown this warning today
                String warningKey = prayerTitle + "_" + calendar.get(Calendar.DAY_OF_YEAR);
                if (currentTime.equals(warningTime) && !warningsShown.contains(warningKey)) {
                    showWarningNotification(prayerTitle, startTime);
                    warningsShown.add(warningKey);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showWarningNotification(String prayerTitle, String startTime) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, WARNING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Prayer Time Warning")
                .setContentText(prayerTitle + " prayer will start at " + startTime + " (in 5 minutes).")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 250, 500});
    
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(WARNING_NOTIFICATION_ID + prayerTitle.hashCode(), builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Service will be explicitly started and stopped
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding needed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the runnable from the handler when service is destroyed
        handler.removeCallbacks(runnable);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW // Use LOW importance to avoid sound
            );
            channel.setDescription("Prayer time notifications");
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String message) {
        // Create an intent to open the app when notification is tapped
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Prayer Times")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true);

        return builder.build();
    }

    private void checkPrayerTimes() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(calendar.getTime());
        
        // Check regular prayers
        checkPrayerTime("Fajr", currentTime);
        checkPrayerTime("Dhuhr", currentTime);
        checkPrayerTime("Asr", currentTime);
        checkPrayerTime("Maghrib", currentTime);
        checkPrayerTime("Isha", currentTime);
        
        // Check Jummah prayer only on Friday (Calendar.FRIDAY is 6)
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            checkPrayerTime("Jummah", currentTime);
        }
    }

    private void checkPrayerTime(String prayerTitle, String currentTime) {
        String startTimeKey = "startTime_" + prayerTitle;
        String finishTimeKey = "finishTime_" + prayerTitle;
        String activeKey = "active_" + prayerTitle;
        String deactivateKey = "deactivate_" + prayerTitle;
        
        SharedPreferences preferences = getSharedPreferences("NamajPreferences", MODE_PRIVATE);
        String startTime = preferences.getString(startTimeKey, "");
        String finishTime = preferences.getString(finishTimeKey, "");
        boolean isActive = preferences.getBoolean(activeKey, true);
        boolean isDeactivated = preferences.getBoolean(deactivateKey, false);
        
        // Only check time if prayer is active and not deactivated
        if (!startTime.isEmpty() && !finishTime.isEmpty() && isActive && !isDeactivated) {
            // Check if the current time is within the range of the prayer time
            if (currentTime.equals(startTime)) {
                // Start time reached, set the device to silent mode
                if (checkNotificationPolicyAccess(this)) {
                    // Permission granted, change the ringer mode
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        
                    // Show a notification
                    createNotification(prayerTitle + " prayer time. Device set to silent mode.");
                }
            }
            if (currentTime.equals(finishTime)) {
                if (checkNotificationPolicyAccess(this)) {
                    // End time reached, set the device to normal mode
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    // Show a notification
                    createNotification(prayerTitle + " prayer time ended. Device set to normal mode.");
                }
            }
        }
    }

    private boolean checkNotificationPolicyAccess(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return false; // Permission not granted
            }
        }
        return true; // Permission granted or SDK version is lower than M
    }
}
