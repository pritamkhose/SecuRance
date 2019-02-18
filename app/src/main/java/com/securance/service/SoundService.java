package com.securance.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.securance.R;

// https://stackoverflow.com/questions/21043059/play-background-sound-in-android-applications

public class SoundService extends Service {
    MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        audioManager();
        player = MediaPlayer.create(this, R.raw.speech); //select music file // rockon
        player.setLooping(true); //set looping
        player.setVolume(100,100);
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        player.start();
        return Service.START_NOT_STICKY;
    }

    public void onDestroy() {
        player.stop();
        player.release();
        stopSelf();
        super.onDestroy();
    }

    private void audioManager() {
        // https://stackoverflow.com/questions/40925722/how-to-increase-and-decrease-the-volume-programmatically-in-android
        AudioManager audioManager = (AudioManager)  getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 90, 0);
    }

}