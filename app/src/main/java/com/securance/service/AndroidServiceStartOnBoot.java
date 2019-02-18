package com.securance.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;

public class AndroidServiceStartOnBoot extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // here you can add whatever you want this service to do
        Log.d("ServiceStartOnBoot-->" ,(new Date()).toString());
    }

}