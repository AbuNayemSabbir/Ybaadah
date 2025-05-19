package com.example.ebda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Start your service here
            Intent serviceIntent = new Intent(context, PrayerForegroundService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }


}
