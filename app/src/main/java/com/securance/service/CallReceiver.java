package com.securance.service;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import com.securance.util.SharedPrefUtils;

import java.util.Date;

// https://stackoverflow.com/questions/15563921/how-to-detect-incoming-calls-in-an-android-device
// https://stackoverflow.com/questions/22503489/incoming-and-outgoing-calls-in-the-broadcastreceiver

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        String s = number +"\t"+ start.toString();
        Log.d("CallReceiver -->", s);

        SharedPrefUtils sh = new SharedPrefUtils(ctx);
        String EmergencyNo = sh.getSharedPrefString("EmergencyNo");
        if(EmergencyNo != null && EmergencyNo.length() > 9 ) {
            String sno = number;
            String eno = EmergencyNo;
            if(sno.startsWith("+91")){
                sno =  sno.replace("+91","");
            }
            if(sno.startsWith("0")){
                sno =  sno.substring(1);
            }
            if(eno.startsWith("+91")){
                eno =  eno.replace("+91","");
            }
            if(eno.startsWith("0")){
                eno =  eno.substring(1);
            }

            if (number.equals(EmergencyNo) || sno.equals(eno)) {
                disableSlient(ctx);
            }

        }
    }

    private void disableSlient(Context ctx) {
        // https://stackoverflow.com/questions/11699603/is-it-possible-to-turn-off-the-silent-mode-programmatically-in-android

        AudioManager AUDIOMANAGER = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
//        if (AUDIOMANAGER.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
//                AUDIOMANAGER.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
        if (AUDIOMANAGER.getRingerMode() != AudioManager.RINGER_MODE_NORMAL ) {
            //Change Ringer mode previous mode:
            AUDIOMANAGER.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

}

