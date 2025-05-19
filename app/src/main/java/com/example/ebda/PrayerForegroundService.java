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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PrayerForegroundService extends Service {
    private static final int NOTIFICATION_ID = 12345678; // Unique ID for the notification
    private static final String CHANNEL_ID = "PrayerChannel"; // Notification channel ID
    private static final CharSequence CHANNEL_NAME = "PrayerChannel"; // Notification channel name

    private Handler handler;
    private Runnable runnable;
    private SharedPreferences namajPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        namajPreferences = getSharedPreferences("NamajPreferences", Context.MODE_PRIVATE);

        // Create a notification channel
        createNotificationChannel();

        // Create and show notification immediately (required for Android 12+)
        Notification notification = createNotification("Prayer time silent mode activated!");
        startForeground(NOTIFICATION_ID, notification);

        // Start a handler to run the runnable every second
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                checkPrayerTimes();
                handler.postDelayed(this, 1000); // Repeat every second
            }
        };
        handler.post(runnable);
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

        checkPrayerTime("Fajr", currentTime);
        checkPrayerTime("Dhuhr", currentTime);
        checkPrayerTime("Asr", currentTime);
        checkPrayerTime("Maghrib", currentTime);
        checkPrayerTime("Isha", currentTime);
        checkPrayerTime("Jummah", currentTime);
    }

    private void checkPrayerTime(String prayerName, String currentTime) {
        SharedPreferences.Editor editor = namajPreferences.edit();

        String startTimeKey = "startTime_" + prayerName;
        String finishTimeKey = "finishTime_" + prayerName;
        String startTime = namajPreferences.getString(startTimeKey, "");
        String finishTime = namajPreferences.getString(finishTimeKey, "");

        // Check if the current time is within the range of the prayer time
        if (currentTime.equals(startTime)) {
            // Start time reached, set the device to silent mode
            if (checkNotificationPolicyAccess(this)) {
                // Permission granted, change the ringer mode
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                // Show a notification
                createNotification(prayerName + " prayer time. Device set to silent mode.");
            }
        }
        if (currentTime.equals(finishTime)) {
            if (checkNotificationPolicyAccess(this)) {
                // End time reached, set the device to normal mode
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                // Show a notification
                createNotification(prayerName + " prayer time ended. Device set to normal mode.");
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
