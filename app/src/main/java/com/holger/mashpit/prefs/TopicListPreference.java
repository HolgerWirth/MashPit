package com.holger.mashpit.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

import com.holger.mashpit.model.Subscriber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopicListPreference extends MultiSelectListPreference {

    private static final String DEBUG_TAG = "TopicListPreference";
    SharedPreferences prefs;

    public TopicListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String prefEntry;

        prefEntry = getKey();
        Log.i(DEBUG_TAG, "Called with: "+prefEntry);

        setOnPreferenceChangeListener(new MultiSelectListPreference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Set<String> defaults;
                defaults = (HashSet) newValue;
                prefs.edit().putStringSet(prefEntry, defaults).apply();
                Log.i(DEBUG_TAG, "Save "+prefEntry+": "+defaults.toString());
                return true;
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entriesValues = new ArrayList<>();
        String subs = prefs.getString("sublist", "");
        if (subs.length() > 0) {
            try {
                JSONObject subscribers = new JSONObject(subs);
                JSONArray subarray = subscribers.getJSONArray("subscriber");
                for (int i = 0; i < subarray.length(); i++) {
                    JSONObject subobj = subarray.getJSONObject(i);
                    Subscriber sub = new Subscriber();
                    sub.topic = subobj.getString("topic");
                    sub.interval = subobj.getString("interval");
                    entries.add(sub.topic+"/"+sub.interval);
                    entriesValues.add(sub.topic+"/"+sub.interval);
                }
            } catch (JSONException e) {
                Log.i(DEBUG_TAG, "sublist preference does not exist");
            }
        }

        if(prefs.contains(prefEntry)) {
            Log.i(DEBUG_TAG, prefEntry+" preference exists");
            Set<String> prefdefaults = prefs.getStringSet(prefEntry, new HashSet<String>());
            Set<String> defaults = new HashSet<>();
                for (String s : prefdefaults) {
                    if (entries.contains(s)) {
                        defaults.add(s);
                    }
                }
                prefs.edit().putStringSet(prefEntry, defaults).apply();
                setDefaultValue(defaults);
            }
        setEntries(entries.toArray(new CharSequence[entries.size()]));
        setEntryValues(entriesValues.toArray(new CharSequence[entriesValues.size()]));
    }
}
