package com.securance.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// https://examples.javacodegeeks.com/android/core/activity/android-start-service-boot-example/
// https://stackoverflow.com/questions/4562734/android-starting-service-at-boot-time
public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, AndroidServiceStartOnBoot.class);
            context.startService(serviceIntent);
        }
    }
}