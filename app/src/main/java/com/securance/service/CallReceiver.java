package com.securance.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.CallLog;
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
    ArrayList<String> lastCallNoList;

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

        lastCallNoList = getLastCallDetails(ctx);

        SharedPrefUtils sh = new SharedPrefUtils(ctx);
        String EmergencyNo = sh.getSharedPrefString("EmergencyNo");
        checkCondtion(EmergencyNo);

        ArrayList<HashMap<String, Object>> aList = sh.getSharedPrefArrayList("ContactList");
        if (aList != null && aList.size() > 0) {

            String ph = "";
            for (int i = 0; i < aList.size(); i++) {
                ph = aList.get(i).get("Phone").toString();
                checkCondtion(ph);
            }
        }

        if (countCall > 3) {
            disableSlient(ctx);
        }
    }

    int countCall = 0;
    private Boolean checkCondtion(String EmergencyNo) {
        boolean b = false;
        if (EmergencyNo != null && EmergencyNo.length() > 9) {
            String eno = EmergencyNo;
            if (eno.startsWith("+91")) {
                eno = eno.replace("+91", "");
            }
            if (eno.startsWith("0")) {
                eno = eno.substring(1);
            }
            if (oldno.equals(EmergencyNo) || sno.equals(eno)) {
                for (int i = 0; i < lastCallNoList.size(); i++) {
                    EmergencyNo = lastCallNoList.get(i);
                    eno = EmergencyNo;
                    if (eno.startsWith("+91")) {
                        eno = eno.replace("+91", "");
                    }
                    if (eno.startsWith("0")) {
                        eno = eno.substring(1);
                    }
                    if (oldno.equals(EmergencyNo) || sno.equals(eno)) {
                        b = true;
                        countCall = countCall + 1;
                    }
                }

            }
        }
        return b;
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

    // https://stackoverflow.com/questions/42436808/get-last-call-details-from-call-log
    // https://stackoverflow.com/questions/7480132/getting-the-call-logs-of-incoming-and-outgoing-calls-in-android-programmatically
    public ArrayList<String> getLastCallDetails(Context context) {

        Date cureentDayTime = new Date();

        ArrayList<HashMap<String, Object>> callDetails = new ArrayList<>();
        ArrayList<String> phNumberList = new ArrayList<>();
        Uri contacts = CallLog.Calls.CONTENT_URI;
        try {

            @SuppressLint("MissingPermission")
            Cursor managedCursor = context.getContentResolver().query(contacts, null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 10;");

            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int incomingtype = managedCursor.getColumnIndex(String.valueOf(CallLog.Calls.INCOMING_TYPE));

            while (managedCursor.moveToNext()) {
                String callType = "";
                String phNumber = managedCursor.getString(number);
                //String callerName = getContactName(context, phNumber);
                //int dircode = Integer.parseInt(callType);
                   /* switch (incomingtype) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            callType = "OUTGOING";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            callType = "INCOMING";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            callType = "MISSED";
                            break;
                    }*/
                String callDuration = managedCursor.getString(duration);
                String callDate = managedCursor.getString(date);
                Date callDayTime = new Date(Long.valueOf(callDate));

                HashMap<String, Object> hm = new HashMap<>();
                if (incomingtype == -1) {
                    callType = "incoming";
                    long diff = cureentDayTime.getTime() - callDayTime.getTime();
//                        long diffmsec = d2.getTime() - d1.getTime();
//                        long diffSeconds = diff / 1000;
                    long diffMinutes = diff / (60 * 1000);
//                        long diffHours = diff / (60 * 60 * 1000);
                    if (diffMinutes < 15.1) {
                        phNumberList.add(phNumber);
//                            hm.put("select", true);
                    } else {
//                            hm.put("select", false);
                    }
                } else {
                    callType = "outgoing";
                }

                //hm.setCallerName(callerName);
//                    hm.put("phNumber", phNumber);
//                    hm.put("callDuration", callDuration);
//                    hm.put("callType", callType);
//                    hm.put("callDayTime", callDayTime.toString());
//                    callDetails.add(hm);
            }
            managedCursor.close();

//            HashMap<String, Object> hm = new HashMap<>();
//            hm.put("phNumberList", phNumberList);
//            callDetails.add(hm);

            Log.d("callDetails -->", phNumberList.toString());
        } catch (SecurityException e) {
            Log.e("Security Exception", "User denied call log permission");
            e.printStackTrace();
        }

        //return callDetails;
        return phNumberList;

    }

}

