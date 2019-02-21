package com.securance;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.securance.pojo.LocationTrackerObj;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class TrackMapActivity extends FragmentActivity implements OnMapReadyCallback {


    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private ArrayList<HashMap<String, Object>> aList = new ArrayList<>();

    private GoogleMap mMap;
    private Boolean isFrist = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_map);

        //Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        //setActionBar(toolbar);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locTrackerSync();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        //http://www.programcreek.com/java-api-examples/index.php?class=com.google.android.gms.maps.GoogleMap&method=setMyLocationEnabled
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // mMap.setTrafficEnabled(true);

        //mMap.animateCamera( CameraUpdateFactory.zoomTo( 12.0f ) );
        //https://www.google.com/maps/place/Pune,+Maharashtra/@18.5248904,73.7228783,11z/data=!3m1!4b1!4m5!3m4!1s0x3bc2bf2e67461101:0x828d43bf9d9ee343!8m2!3d18.5204695!4d73.8567066
        LatLng latLng = new LatLng(18.53, 73.85);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f));

    }

    private void locTrackerSync() {
        // https://securance-94e24.firebaseio.com/LocationTracker.json

        FirebaseApp.initializeApp(this);

        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'LocationTracker' node
        mFirebaseDatabase = mFirebaseInstance.getReference("LocationTracker");
//        DatabaseReference db = mFirebaseDatabase.child("123456789012345");
//        String s = mFirebaseDatabase.getKey()+ "\n" + mFirebaseDatabase.child("1234567890123456").toString() ;
//        Log.d("FirebaseDatabase -->", s);

        // https://stackoverflow.com/questions/38652007/how-to-retrieve-specific-list-of-data-from-firebase

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
               /* aList = new ArrayList<>();
                HashMap<String, Object> hm = new HashMap<>();

               for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot postchildSnapshot : postSnapshot.getChildren()) {
//                    Log.d("FirebaseDatabase -->", postSnapshot.toString()
//                            +  postSnapshot.getKey().toString() + "\n"
//                            +  postSnapshot.getValue().toString() + "\n"
//                    );
//                    LocationTrackerObj aObj = postSnapshot.getValue(LocationTrackerObj.class);
                        hm.put(postchildSnapshot.getKey().toString(), postchildSnapshot.getValue());
                    }
                    aList.add(hm);
                }

                Log.d("FirebaseDatabase -->", aList.toString());
                drawMarker(aList);*/

                http_get_location();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    Boolean isWebNewRun = true;
    String strResponse;
    private void http_get_location() {
       
        try {

            if(isWebNewRun) {
                isWebNewRun = false;

                String url = "https://securance-94e24.firebaseio.com/LocationTracker.json";
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                Log.d("-->>", request.toString());


                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        isWebNewRun = true;
                        call.cancel();
                        Log.d("-->>", getStackTrace(e));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //Log.d("-->>", response.toString());
                        isWebNewRun = true;
                        strResponse = response.body().string();
                        if(strResponse != null && strResponse.length() > 3){
                            JsonObject json1 = (JsonObject) (new JsonParser()).parse(strResponse);
                            Set<Map.Entry<String, JsonElement>> entrySet = json1.entrySet();

                            for(Map.Entry<String,JsonElement> entry : entrySet) {
                                JsonObject jo = (JsonObject)json1.get(entry.getKey());
                                HashMap<String, Object> hm = new HashMap<>();
//                hm.put("Accuracy", jo.get("Accuracy"));
//                hm.put("Altitude", jo.get("Altitude"));
                                hm.put("Latitude", jo.get("Latitude"));
                                hm.put("Longitude", jo.get("Longitude"));
//                hm.put("Provider", jo.get("Provider"));
//                hm.put("Speed", jo.get("Speed"));
//                hm.put("TimeStamp", jo.get("TimeStamp"));
//                hm.put("IMEIKey", entry.getKey());
                                hm.put("Username", jo.get("Username"));
                                hm.put("IMEI", jo.get("IMEI"));
//                hm.put("isPanic", jo.get("isPanic"));
                                aList.add(hm);
                            }

                            Log.d("FirebaseDatabase -->", isFrist + "\t"+aList.toString());

                            drawMarker(aList);
                        }
                    }
                });

            }

        } catch (Exception e) {
            isWebNewRun = true;
           e.printStackTrace();
        }
    }


    Marker myMarker;
    private void drawMarker(ArrayList<HashMap<String, Object>> aList) {

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(18.5699098, 73.7733724);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker @ my location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        runOnUiThread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                if(isFrist){
                    isFrist = false;
                } else {
                    mMap.clear();
                    if(myMarker != null){
                        myMarker.remove();
                    }

                    //mMap = googleMap;

                    // Enabling MyLocation Layer of Google Map
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);

                    //http://www.programcreek.com/java-api-examples/index.php?class=com.google.android.gms.maps.GoogleMap&method=setMyLocationEnabled
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                }

                for (int i = 0; i < aList.size(); i++) {
                    try {
                        double Latitude = Double.parseDouble(aList.get(i).get("Latitude").toString());
                        double Longitude = Double.parseDouble(aList.get(i).get("Longitude").toString());
                        LatLng point = new LatLng(Latitude, Longitude);

                        myMarker = mMap.addMarker(new MarkerOptions()
                                        .position(point)
                                        .title(aList.get(i).get("Username").toString())
                                        .snippet(aList.get(i).get("IMEI").toString())
                                        .draggable(true)
                                        .alpha(0.6f)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.jkmarker))
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void appName() {
        // store app title to 'app_title' node
        mFirebaseInstance.getReference("app_title").setValue("Realtime Database");

        // app_title change listener
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("FirebaseDatabase -->", "App title updated");
                String appTitle = dataSnapshot.getValue(String.class);
                Toast.makeText(TrackMapActivity.this, appTitle, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("FirebaseDatabase -->", "Failed to read app title value.", error.toException());
                Toast.makeText(TrackMapActivity.this, "Failed to read app title value.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

}
