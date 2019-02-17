package com.securance.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.securance.TrackMapActivity;

// https://stackoverflow.com/questions/30029978/how-to-detect-device-power-button-press-twice-in-android-programmatically
// https://stackoverflow.com/questions/9162705/android-how-to-listen-for-volume-button-events

// check https://www.codeproject.com/Questions/1112843/How-to-wakeup-device-by-volume-key-using-backgroun
public class VolumeChangeReceiver  extends BroadcastReceiver {

    Integer i = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
            int newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
            int oldVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0);
            if (newVolume != oldVolume) {
               if(i == 5) {
                   i=1;
                   Intent i = new Intent();
                   i.setClass(context, TrackMapActivity.class);
                   i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   context.startActivity(i);
                }
                i=i+1;
            }
        }
    }
}

