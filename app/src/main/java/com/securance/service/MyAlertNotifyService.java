package com.securance.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.securance.MainActivity;
import com.securance.R;
import com.securance.TrackMapActivity;

// https://stackoverflow.com/questions/15758980/android-service-needs-to-run-always-never-pause-or-stop
public class MyAlertNotifyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something useful
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        runWebService();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void runWebService() {
        Log.d("runWebService", "--> Start");
        try {
            FirebaseApp.initializeApp(getApplicationContext());

            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

            // store app title to 'app_title' node
            //mFirebaseInstance.getReference("app_title").setValue("Realtime Database");

            // app_title change listener
            mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String appTitle = dataSnapshot.getValue(String.class);
                    Log.d("FirebaseDatabase -->", "App title updated -->" + appTitle);
                    if (appTitle != null && appTitle.length() > 3) {
                        sendMyNotification(appTitle);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e("FirebaseDatabase -->", "Failed to read app title value.", error.toException());
                }
            });
            Log.d("runWebService", "--> Stop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // https://github.com/pritamkhose/NotificationOreo/blob/master/app/src/main/java/com/pritam/emergency/HomeActivity.java
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    NotificationChannel notificationChannel;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "1";

    private void sendMyNotification(String appTitle) {
        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, TrackMapActivity.class);
        Context context = getApplicationContext();
        intent.putExtra("title", appTitle);
//        intent.putExtra("message", message);
//        intent.putExtra("data", "data");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/speech.mp3");
       // Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.speech);

        mNotifyManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(context, null);
        mBuilder.setContentTitle("Help Needed!")
                .setContentText(appTitle)
                .setSmallIcon(R.mipmap.ic_launcher) // use png https://stackoverflow.com/questions/25317659/how-to-fix-android-app-remoteserviceexception-bad-notification-posted-from-pac
                .setOnlyAlertOnce(true)
                //.setSound(soundUri)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "SecuRance Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
        else {
            mBuilder.setContentTitle("Help Needed!")
                    .setContentText(appTitle)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark))
                    .setAutoCancel(true)
                    //.setSound(soundUri)
                    .setVibrate(new long[]{0, 1000, 500, 1000, 500, 1000})
                    .setLights(Color.RED, 3000, 3000)
                    .setContentIntent(pendingIntent);
        }

        mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

        startService(new Intent(getApplicationContext(), SoundService.class));


    }


}
