package com.securance.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.securance.R;
import com.securance.TrackMapActivity;

// https://www.text2speech.org/
// https://github.com/pritamkhose/FCMNotifcation
// https://firebase.google.com/docs/cloud-messaging/android/first-message?authuser=1#retrieve-the-current-registration-token
// https://console.firebase.google.com/u/1/project/securance-94e24/notification
// https://github.com/pratikbutani/Firebase-FCMDemo

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {

        sendMyNotification(message.getNotification().getTitle(),message.getNotification().getBody());
    }
    //https://stackoverflow.com/questions/6464080/how-to-play-mp3-file-in-raw-folder-as-notification-sound-alert-in-android


    private void sendMyNotification(String title, String message) {

        if(title == null) {
            title  ="SecureRance Notification";
        }
        if(message == null) {
            message  ="No message";
        }
        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, TrackMapActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("data", "data");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/speech");

        //Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(new long[]{500, 500, 500, 500, 500, 500})
                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }


}