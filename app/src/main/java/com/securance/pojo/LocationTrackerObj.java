package com.securance.pojo;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LocationTrackerObj {

    public Double Accuracy;
    public Double Altitude;
    public String IMEI;
    public String Latitude;
    public String Longitude;
    public String Provider;
    public Double Speed;
    public String TimeStamp;
    public String Username;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public LocationTrackerObj() {
    }

    public LocationTrackerObj(Double accuracy, Double altitude, String IMEI, String latitude, String longitude, String provider, Double speed, String timeStamp, String username) {
        Accuracy = accuracy;
        Altitude = altitude;
        this.IMEI = IMEI;
        Latitude = latitude;
        Longitude = longitude;
        Provider = provider;
        Speed = speed;
        TimeStamp = timeStamp;
        Username = username;
    }
}
