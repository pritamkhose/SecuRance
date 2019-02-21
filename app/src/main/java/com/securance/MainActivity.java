package com.securance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.CardView;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.securance.service.MyAlertNotifyService;
import com.securance.service.SoundService;
import com.securance.util.AppConstant;
import com.securance.util.GPSTracker;
import com.securance.util.SharedPrefUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity implements LocationListener {

    String LatLong = "18.5699098,73.7733724";
    public static final int PERMISSION_REQUEST_CODE = 200;
    double latitude = 0, longitude = 0;
    LocationManager locationManager;
    String mprovider;
    SharedPrefUtils sh;
    Boolean isPanic = false;
//    TextView tv_location;

    //http://alexzh.com/tutorials/android-battery-status-use-broadcastreceiver/
    private BroadcastReceiver mReceiver;
    private Boolean isBatterySMSsend = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sh = new SharedPrefUtils(this);

        String Username = sh.getSharedPrefString("Username");
        if(!(Username != null && Username.length() > 0)) {
            Random r = new Random( System.currentTimeMillis() );
            int no = ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
            sh.saveSharedPrefString("Username", "User "+no);
        }

        // tv_location = ((TextView) findViewById(R.id.location));

        mReceiver = new BatteryBroadcastReceiver();
        getApplicationContext().startService((new Intent(getApplicationContext(), MyAlertNotifyService.class)));

        ((CardView) findViewById(R.id.btn_panic)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                panicCall();
            }
        });

        ((CardView) findViewById(R.id.btn_sms)).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                //makeCall();
                SendCallSMS();
            }
        });

        ((CardView) findViewById(R.id.btn_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ContactListActivity.class));
            }
        });

        ((CardView) findViewById(R.id.btn_place)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), PlacesActivity.class);
                intent.putExtra("LatLong", LatLong);
                startActivity(intent);
            }
        });

        ((CardView) findViewById(R.id.btn_track)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), TrackMapActivity.class);
                intent.putExtra("LatLong", LatLong);
                startActivity(intent);
            }
        });
    }

    private void panicCall() {
        if (!isPanic) {
            ((ImageView) findViewById(R.id.panicimg)).setImageResource(R.mipmap.stop);
            isPanic = true;
            //start service and play music
            Intent intent = new Intent(MainActivity.this, SoundService.class);
            intent.putExtra("ringerLoop", "ringerLoop");
            startService(intent);
            if (isNetworkAvaiable()) {
                notifyFirebase(true);
            } else {
                SendCallSMS();
            }
        } else {
            ((ImageView) findViewById(R.id.panicimg)).setImageResource(R.mipmap.panic);
            isPanic = false;
            //stop service and stop music
            stopService(new Intent(MainActivity.this, SoundService.class));
            if (isNetworkAvaiable()) {
                notifyFirebase(false);
            }
        }


    }



    private void SendCallSMS() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            ArrayList<HashMap<String, Object>> aList = sh.getSharedPrefArrayList("ContactList");
            if (aList != null && aList.size() > 0) {

                String ph = "";
                for (int i = 0; i < aList.size(); i++) {
                    ph = aList.get(i).get(getResources().getString(R.string.et_phone)).toString();
                    String msg = getSavedLocation();
                    if (msg != null && msg.length() > 0) {
                        sendSMS(ph, msg);
                    } else {
                        Toast.makeText(MainActivity.this, "Acquiring Current Location", Toast.LENGTH_LONG).show();
                        getGPSLocation();
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "To use these feature, Kindly Add Contact List No.", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getBaseContext(), ContactListActivity.class);
                i.putExtra("openPopup", "openPopup");
                startActivity(i);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void makeCall() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            String s = sh.getSharedPrefString("EmergencyNo");
            if (s != null && s.length() > 0) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + s));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "To use these feature, Kindly set Emergency No.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getBaseContext(), ContactListActivity.class));
            }
        }
    }


    private void disableSlient() {
        // https://stackoverflow.com/questions/11699603/is-it-possible-to-turn-off-the-silent-mode-programmatically-in-android

        AudioManager AUDIOMANAGER = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        if (AUDIOMANAGER.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                AUDIOMANAGER.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {

            //Change Ringer mode previous mode:
            AUDIOMANAGER.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }


    private String getSavedLocation() {
        String loc = "My IMEI - " + getDeviceInfo() + " & Location is ";
        String s = sh.getSharedPrefString("Lat");
        boolean b = false;
        if (s != null && s.length() > 0) {
            b = true;
            loc = loc + s + "Lat. ";
        }
        s = sh.getSharedPrefString("Long");
        if (s != null && s.length() > 0) {
            loc = loc + s + "Long. ";
        }
        s = sh.getSharedPrefString("Addr");
        if (s != null && s.length() > 0) {
            loc = loc + "Addr.- " + s + " ";
        }
        if (b) {
            return loc;
        } else {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }
        if (isGooglePlayServicesAvailable()) {
            GPSsettingsrequest();
        } else {
            showSettingsAlert();
        }

        getGPSLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contact_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_exit: {
                finishAffinity();
            }
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean checkPermission() {
        // mandatory for device build compileSdkVersion < 23
        return ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    CALL_PHONE,
                    SEND_SMS,
                    ACCESS_FINE_LOCATION,
                    READ_PHONE_STATE
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean per0 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean per1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean per2 = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean per3 = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (per0 && per1 && per2 && per3) {
                        //Snackbar.make(view, "Permission Granted, Now you can access location data and camera.", Snackbar.LENGTH_LONG).show();
                    } else {
                        // Toast.makeText(this, "As Permission Denied,\nApplication unable work", Toast.LENGTH_LONG).show();
                        showMessageOKCancel("You need to allow access for All the permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermission();
                                    }
                                });

                    }
                }
                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            if(phoneNo.startsWith("+91")){
                phoneNo =  phoneNo.replace("+91","");
            }
            if(phoneNo.startsWith("0")){
                phoneNo =  phoneNo.substring(1);
            }
            phoneNo = "+91" + phoneNo;

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            // Toast.makeText(getApplicationContext(),ex.getMessage().toString(),  Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Something went wrong !", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void getGPSLocation() {

        GPSTracker gps = new GPSTracker(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            sh.saveSharedPrefString("Lat", String.valueOf(latitude));
            sh.saveSharedPrefString("Long", String.valueOf(longitude));
            getAddress(gps.getLocation());

            //tv_location.setText("Lat - " + String.valueOf(latitude) + "\nLong - " + String.valueOf(longitude));
            updateFirebase(gps.getLocation());
            LatLong = String.valueOf(latitude) + "," + String.valueOf(longitude);

        } else {
            // can't get location GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                gps.showSettingsAlert();
        }

        Criteria criteria = new Criteria();
        mprovider = locationManager.getBestProvider(criteria, false);
        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
            }
            Location location = locationManager.getLastKnownLocation(mprovider);
            locationManager.requestLocationUpdates(mprovider, 1, 1, this);
            if (location != null)
                onLocationChanged(location);
            {
                boolean gps_enabled = false;
                boolean network_enabled = false;
                try {
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                }
                try {
                    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception ex) {
                }
                // notify user
                if (!gps_enabled && !network_enabled)
                    showSettingsAlert();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // if (location.hasAccuracy())
        {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            sh.saveSharedPrefString("Lat", String.valueOf(latitude));
            sh.saveSharedPrefString("Long", String.valueOf(longitude));

            updateFirebase(location);

            getAddress(location);
            LatLong = String.valueOf(latitude) + "," + String.valueOf(longitude);
            //tv_location.setText("Lat - " + String.valueOf(latitude) + "\nLong - " + String.valueOf(longitude));
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }


    @SuppressLint("MissingPermission")
    private String getDeviceInfo() {
        String deviceInfo = "";
        String serviceName = Context.TELEPHONY_SERVICE;
        TelephonyManager m_telephonyManager = (TelephonyManager) getSystemService(serviceName);
        String IMEI = "", IMSI = "", mPhoneNumber = "";
        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            IMEI = m_telephonyManager.getDeviceId();
//            IMSI = m_telephonyManager.getSubscriberId();
//            mPhoneNumber = m_telephonyManager.getLine1Number();
//            String s = "";
//            s += "^OS Version:" + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")";
//            s += "^OS API Level:" + Build.VERSION.SDK;
//            s += "^Device:" + Build.DEVICE;
//            s += "^Model (and Product):" + Build.MODEL + " (" + Build.PRODUCT + ")";

            // deviceInfo = "PhNo:" + mPhoneNumber + "^IMEI:" + IMEI + "^IMSI:" + IMSI + s + "^" + System.getProperty("os.version") + "^" + System.getProperty("android.os.Build.VERSION.SDK");
            //System.out.println("deviceInfo==="+deviceInfo);
            deviceInfo = IMEI;
        }

        return deviceInfo;
    }

    public void getAddress(Location mLocation) {
        // Ensure that a Geocoder services is available
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD
                &&
                Geocoder.isPresent()) {

            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            (new GetAddressTask(this)).execute(mLocation);
        }
    }

    /**
     * A subclass of AsyncTask that calls getFromLocation() in the
     * background. The class definition has these generic types:
     * Location - A Location object containing
     * the current location.
     * Void     - indicates that progress units are not used
     * String   - An address passed to onPostExecute()
     */
    private class GetAddressTask extends AsyncTask<Location, Void, String> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         * @params params One or more Location objects
         */
        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder =
                    new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            Location loc = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;
            if (loc != null) {
                try {
                    /*
                     * Return 1 address.
                     */
                    addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                } catch (Exception e1) {
                    Log.e("LocationSample", "IO Exception in getFromLocation()");

                    String errorString = "";

                    Log.e("LocationSample", errorString);
                    e1.printStackTrace();
                    return errorString;
                }


                // If the reverse geocode returned an address
                if (addresses != null && addresses.size() > 0) {
                    // Get the first address
                    Address address = addresses.get(0);

                    String addressText1 = "";
                    for (int i = 0; i < address.getMaxAddressLineIndex() - 1; i++) {
                        if (address.getAddressLine(i).toLowerCase().startsWith(addressText1.toLowerCase()))
                            addressText1 = address.getAddressLine(i);
                        else
                            addressText1 = addressText1 + ", " + address.getAddressLine(i);

                    }

                    /*
                     * Format the first line of address (if available),
                     * city, and country name.
                     */
                    addressText1 = addressText1 + ", " + address.getLocality() + ", " + address.getAdminArea()
                            + ", " + address.getCountryName()
                            + " " + address.getPostalCode();


                    // Return the text
                    return addressText1;
                } else {
                    return null;
                }
            } else {
                return null;
            }

        }

        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(String address) {
            if (address != null && address.length() > 2 && (!address.contains("Illegal"))) {
                sh.saveSharedPrefString("Addr", address);
                //LatLong = String.valueOf(latitude) + "," + String.valueOf(longitude);
//                tv_location.setText("Lat - " + String.valueOf(latitude) + "\nLong - " + String.valueOf(longitude)
//                        + "\nAddress - " + address);
            } else {
                Log.d("Illegal address --> ", " " + address);
            }
        }
    }


    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            Log.d("Battery level --> ", " " + level);
            if (isBatterySMSsend && level < AppConstant.BatteryLimit) {
                isBatterySMSsend = false;
                ArrayList<HashMap<String, Object>> aList = sh.getSharedPrefArrayList("ContactList");
                if (aList != null && aList.size() > 0) {

                    String ph = "";
                    for (int i = 0; i < aList.size(); i++) {
                        ph = aList.get(i).get(getResources().getString(R.string.et_phone)).toString();
                        String msg = getSavedLocation();
                        if (msg != null && msg.length() > 0) {
                            sendSMS(ph, "My Phone Battery is" + level + "%\n& " + msg);
                        } else {
                            // Toast.makeText(MainActivity.this, "Acquiring Current Location", Toast.LENGTH_LONG).show();
                            getGPSLocation();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mReceiver);
        super.onStop();
    }

    private boolean isNetworkAvaiable() {
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressLint("MissingPermission")
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    GoogleApiClient googleApiClient;

    public void GPSsettingsrequest() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true); //this is the key ingredient

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        GPSsettingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Enable GPS");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private void updateFirebase(Location location) {
        if(location != null) {
            String imei = getDeviceInfo();
            if (imei == null) {
                imei = "1234567890123456";
            }

            if (imei != null && imei.length() > 10 && location != null) {

                Log.d("location update-->>", location.toString());

                String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS").format(new Date());
                HashMap<String, Object> hm = new HashMap<>();
                hm.put("Longitude", location.getLongitude());
                hm.put("Latitude", location.getLatitude());
                hm.put("Accuracy", location.getAccuracy());
                hm.put("Altitude", location.getAltitude());
                hm.put("Provider", location.getProvider());
                hm.put("Speed", location.getSpeed());
//        hm.put("SpeedAccuracyMetersPerSecond", location.getSpeedAccuracyMetersPerSecond());
                hm.put("TimeStamp", timeStamp);
                hm.put("Username", sh.getSharedPrefString("Username"));
                hm.put("IMEI", imei);
                hm.put("isPanic",isPanic);

                Gson gson = new Gson();
                http_post_request(gson.toJson(hm), imei);
            } else {
                Log.d("IMEI Not Found -->>", imei);
//            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void http_post_request(String postBody, String imei) {
        try {
            String url = "https://securance-94e24.firebaseio.com/LocationTracker/" + imei + ".json";

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, postBody);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

/*            curl -X PUT \
            https://securance-94e24.firebaseio.com/LocationTracker/1234567890123456.json \
            -H 'Cache-Control: no-cache' \
            -H 'Content-Type: application/json' \
            -H 'Postman-Token: 3f490444-a22a-429d-ae4c-dcf676464b6e' \
            -d '{
            "Accuracy": 20,
                    "Altitude": 0,
                    "IMEI": "1234567890123456",
                    "Latitude": 37.421998333333335,
                    "Longitude": -121.08400000000002,
                    "Provider": "gps",
                    "Speed": 0,
                    "TimeStamp": "2019.02.17-10.19.15.273",
                    "Username": "Pritam",
                    "isPanic": false

        }'*/

            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();
            Log.d("-->>", request.toString());


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("-->>", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("-->>", response.toString());
                }
            });
        } catch (Exception e) {
            Log.d("-->>", getStackTrace(e));
        }
    }

    public String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    private void notifyFirebase(Boolean isPanic) {
        FirebaseApp.initializeApp(this);

        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        String imei = getDeviceInfo();
        if (imei == null) {
            imei = "1234567890123456";
        }

        String Username = sh.getSharedPrefString("Username");
        if (Username == null) {
            Username = "";
        }
        // store app title to 'app_title' node
        if(isPanic){
            mFirebaseInstance.getReference("app_title").setValue(Username  + " - " + imei);
        } else {
            mFirebaseInstance.getReference("app_title").setValue("");
        }

        // app_title change listener
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String appTitle = dataSnapshot.getValue(String.class);
                Log.d("FirebaseDatabase -->", "App title updated - " + appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("FirebaseDatabase -->", "Failed to read app title value.", error.toException());
            }
        });
    }

}
