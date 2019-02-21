package com.securance.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
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

        if(isMyServiceRunning(MyAlertNotifyService.class) == false){
            getApplicationContext().startService((new Intent(getApplicationContext(), MyAlertNotifyService.class)));
        }
    }

    // https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}