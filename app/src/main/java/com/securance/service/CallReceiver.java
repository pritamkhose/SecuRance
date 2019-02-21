package com.securance.service;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import com.securance.util.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// https://stackoverflow.com/questions/15563921/how-to-detect-incoming-calls-in-an-android-device
// https://stackoverflow.com/questions/22503489/incoming-and-outgoing-calls-in-the-broadcastreceiver

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    String sno, oldno;

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        String s = number + "\t" + start.toString();
        Log.d("CallReceiver -->", s);

        oldno = number;
        sno = number;
        if (sno.startsWith("+91")) {
            sno = sno.replace("+91", "");
        }
        if (sno.startsWith("0")) {
            sno = sno.substring(1);
        }

        SharedPrefUtils sh = new SharedPrefUtils(ctx);
        String EmergencyNo = sh.getSharedPrefString("EmergencyNo");
        if (EmergencyNo != null && EmergencyNo.length() > 9) {
            checkCondtion(EmergencyNo, ctx);
        }
        ArrayList<HashMap<String, Object>> aList = sh.getSharedPrefArrayList("ContactList");
        if (aList != null && aList.size() > 0) {

            String ph = "";
            for (int i = 0; i < aList.size(); i++) {
                ph = aList.get(i).get("Phone").toString();
                if (ph != null && ph.length() > 0) {
                    checkCondtion(ph, ctx);
                }
            }
        }
    }

    private void checkCondtion(String EmergencyNo, Context ctx) {
        String eno = EmergencyNo;
        if (eno.startsWith("+91")) {
            eno = eno.replace("+91", "");
        }
        if (eno.startsWith("0")) {
            eno = eno.substring(1);
        }

        if (oldno.equals(EmergencyNo) || sno.equals(eno)) {
            disableSlient(ctx);
        }
    }

    private void disableSlient(Context ctx) {
        // https://stackoverflow.com/questions/11699603/is-it-possible-to-turn-off-the-silent-mode-programmatically-in-android

        AudioManager AUDIOMANAGER = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
//        if (AUDIOMANAGER.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
//                AUDIOMANAGER.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
        if (AUDIOMANAGER.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            //Change Ringer mode previous mode:
            AUDIOMANAGER.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

}

