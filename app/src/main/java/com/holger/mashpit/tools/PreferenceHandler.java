package com.holger.mashpit.tools;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PreferenceHandler {
    public static void saveArrayList(Context context, List<String> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray jsonArray = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            for (String topic : list) {
                jsonArray.put(topic);
            }
            obj.put(key, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putString(key, obj.toString());
        editor.apply();
    }

    public static List<String> getArrayList(Context context, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> topics = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        String json = prefs.getString(key, null);
        if(json == null)
        {
            return topics;
        }
        try {
            JSONObject obj = new JSONObject(json);
            jsonArray=obj.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try {
                topics.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return topics;
    }
}
