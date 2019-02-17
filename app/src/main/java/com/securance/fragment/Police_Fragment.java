package com.securance.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.securance.R;
import com.securance.adaptor.CustomImgListAdapter;
import com.securance.util.AppConstant;
import com.securance.util.SharedPrefUtils;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Police_Fragment extends Fragment {

    View view;
    ArrayList<HashMap<String, Object>> aList = new ArrayList<>();
    //View activityView;
    ListView listView;
    CustomImgListAdapter adapter;

    //ProgressDialog progress;
    Context mContext;
    SharedPrefUtils sh;
    String LatLong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);

        mContext = getActivity().getApplicationContext();
        sh = new SharedPrefUtils(getContext());

        Intent intent = getActivity().getIntent();
        LatLong  = intent.getStringExtra("LatLong");

//        progress = new ProgressDialog(mContext, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);//Theme.AppCompat.Light android.R.style.Theme_Holo_Light_Dialog
//        progress.setMessage("Loading, Please wait ...");
//        progress.setCancelable(false);

        getList();

        return view;
    }

    private void getList() {
        String s = sh.getSharedPrefString("policeJson");
        if (s != null && s.length() > 0) {
            try {
                aList = new ArrayList<>();

                JsonObject json1 = (JsonObject) (new JsonParser()).parse(s);
                JsonArray jsonArr = (JsonArray) json1.get("results");

                for (int i = 0; i < jsonArr.size(); i++) {
                    HashMap<String, Object> hm = new HashMap<>();
                    JsonObject jobj = (JsonObject) jsonArr.get(i);

                    hm.put("name", jobj.get("name").getAsString()); // .toString().replaceAll("\"",""

                    hm.put("icon", jobj.get("icon").getAsString());
                    hm.put("place_id", jobj.get("place_id").getAsString());

                    //hm.put("vicinity", jobj.get("vicinity"));
                    //hm.put("user_ratings_total", jobj.get("user_ratings_total"));

                    JsonObject json3 = (JsonObject) jobj.get("geometry");
                    JsonObject json4 = (JsonObject) json3.get("location");
                    hm.put("lat", json4.get("lat"));
                    hm.put("lng", json4.get("lng"));

                    hm.put("Name", jobj.get("name").getAsString());
                    String str = json4.get("lat") + " " + json4.get("lng");
                    hm.put("Phone", str);

                    aList.add(hm);
                }
                ListViewRefresh();
                //Log.d("-->>", aList.toString());
            } catch (Exception e) {
                Log.d("-->>", getStackTrace(e));
            }
        } else {
            if (isNetworkAvaiable()) {
                http_get_request();
            } else {
//                Snackbar.make(view, "No Internet Available", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Toast.makeText(mContext, "No Internet Available", Toast.LENGTH_LONG).show();
            }

        }

    }

    private void ListViewRefresh() {
        if (aList.size() == 0) {
//            Snackbar.make(view, "No near place Found", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            Toast.makeText(mContext, "No near Police Station found", Toast.LENGTH_LONG).show();
        }

        // Getting a reference to listview of main.xml layout file
        listView = (ListView) view.findViewById(R.id.ListView);
        adapter = new CustomImgListAdapter(mContext, aList);
        // adapter.notifyDataSetChanged();
        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                openGoogleMap(position);
            }
        });
    }

    private void openGoogleMap(int position) {
        //uri = Uri.parse("https://www.google.co.in/maps/place/Bungalowpark+'t+Jachthuis/@52.1319344,5.3307744,13z/data=!4m5!3m4!1s0x0:0x9c2b6e1795ff49a0!8m2!3d52.1319344!4d5.3307744");
        //uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=47.5951518,-122.3316393&query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY");
        String s = aList.get(position).get("lat") + "," + aList.get(position).get("lng") + "&query_place_id=" + aList.get(position).get("place_id");
        // Log.d("Google MAP-->", s);
        Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + s);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void http_get_request() {
        try {
//            progress.show();
            //LatLong = "18.5699098,73.7733724";
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ LatLong +"&radius=5000&type=police&key="+ AppConstant.GooglePlaceAPIKey;

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            //Log.d("-->>", request.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
//                    progress.dismiss();
                    Log.d("-->>", getStackTrace(e));
                    alertDialog("Request Failure", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, final Response response)  {
//                    progress.dismiss();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (response.isSuccessful()) {
                                try {
                                    String s = response.body().string();//response.body().toString(); response.toString() + "\n" +
                                    sh.saveSharedPrefString("policeJson", s);

                                    aList = new ArrayList<>();
                                    JsonObject json1 = (JsonObject) (new JsonParser()).parse(s);

                                    String status = json1.get("status").getAsString();
                                    if(status != null && status.equals("OK"))
                                    {
                                        JsonArray jsonArr = (JsonArray) json1.get("results");

                                        for (int i = 0; i < jsonArr.size(); i++) {
                                            HashMap<String, Object> hm = new HashMap<>();
                                            JsonObject jobj = (JsonObject) jsonArr.get(i);

                                            hm.put("name", jobj.get("name").getAsString()); // .toString().replaceAll("\"",""

                                            hm.put("icon", jobj.get("icon").getAsString());
                                            hm.put("place_id", jobj.get("place_id").getAsString());

                                            //hm.put("vicinity", jobj.get("vicinity"));
                                            //hm.put("user_ratings_total", jobj.get("user_ratings_total"));

                                            JsonObject json3 = (JsonObject) jobj.get("geometry");
                                            JsonObject json4 = (JsonObject) json3.get("location");
                                            hm.put("lat", json4.get("lat"));
                                            hm.put("lng", json4.get("lng"));

                                            hm.put("Name", jobj.get("name").getAsString());
                                            String str = json4.get("lat") + " " + json4.get("lng");
                                            hm.put("Phone", str);

                                            aList.add(hm);

                                            ListViewRefresh();
                                        }
                                    } else if(status != null && status.equals("OVER_QUERY_LIMIT")) {
                                        String error_message = json1.get("error_message").getAsString();
                                        alertDialog("Google Place API Limit Over!", error_message );
                                    } else {
                                        String error_message = json1.get("error_message").getAsString();
                                        alertDialog("API Error - "+ status, error_message );
                                    }
                                    //Log.d("-->>", aList.toString());
                                } catch (Exception e) {
                                    Log.d("-->>", getStackTrace(e));
//                                  // myResponse += response.toString();
                                }
                            } else {
                                Log.d("-->>", response.toString() + "\n" + response.body().toString());
                                Toast.makeText(mContext, "Something went Wrong", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            });
        } catch (Exception e) {
//            progress.dismiss();
            Log.d("-->>", getStackTrace(e));
            alertDialog("Exception", getStackTrace(e));
        }
    }

    private boolean isNetworkAvaiable() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    public String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public void alertDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.ic_launcher_foreground);
        alertDialog.setCancelable(false);

        // Setting Cancel Button
        alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                alertDialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}

