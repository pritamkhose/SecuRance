package com.securance.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class SharedPrefUtils {

    /* Constants */
    private static String SHARED_PREF_NAME = "MySharedPreference";

	/* Static Members */
	private static SharedPreferences settings;
	private static SharedPreferences.Editor editor;
	private static Context context;

    public SharedPrefUtils(Context context) {
        this.context = context;
    }

	public String getSharedPrefString(String key) {
		settings = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
		return settings.getString(key, null);
	}

	public void saveSharedPrefString(String key, String data) {
		settings = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
		editor = settings.edit();

		//Save to SharedPreferences
		editor.putString(key, data).commit();
		editor.commit();
	}


	public ArrayList<HashMap<String, Object>> getSharedPrefArrayList(String key) {
		settings = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
		Gson gson = new Gson();
		String empty_list = gson.toJson(new ArrayList<HashMap<String, Object>>());

		ArrayList<HashMap<String, Object>> mSelectedList = gson.fromJson(settings.getString(key, empty_list),
				new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType());

		return mSelectedList;
	}

	public void saveSharedPrefArrayList(String key, ArrayList<HashMap<String, Object>> data) {
		settings = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
		editor = settings.edit();

		Gson gson = new Gson();
		String jsonString = gson.toJson(data);
		//Save to SharedPreferences
		editor.putString(key, jsonString).commit();
		editor.commit();
	}

}
