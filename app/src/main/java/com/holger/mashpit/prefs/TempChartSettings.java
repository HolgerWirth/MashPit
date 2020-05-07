package com.holger.mashpit.prefs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.widget.EditText;

import com.activeandroid.query.Delete;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.MashPit;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.R;

import java.util.ArrayList;

public class TempChartSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEBUG_TAG = "TempChartSettings";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_tempchart, rootKey);
        final String Mode = getActivity().getIntent().getStringExtra("EXTRA_MODE");
        Log.i(DEBUG_TAG, "Mode: " + Mode);

        Context activityContext = getActivity();
// We need to set a TypedValue instance that will be used to retrieve the theme id
        TypedValue themeTypedValue = new TypedValue();
// We load our 'preferenceTheme' Theme attr into themeTypedValue
        activityContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
// We create a ContextWrapper which holds a reference to out Preference Theme
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);

        MashPit.modedeleted=false;
        PreferenceScreen root = getPreferenceScreen();

        ArrayList<String> entries = new ArrayList<>();
        if(Mode != null)
        {
            entries.add(Mode);
        }
        else
        {
            for (Temperature temperature : MashPit.TempModes) {
                entries.add(temperature.Mode);
            }
        }

        for (final String head : entries) {
            Log.i(DEBUG_TAG,"Creating preferences for: "+head);
            PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
            preferenceCategory.setTitle(getString(R.string.prefs_mode_category)+head+"'");
            getPreferenceScreen().addPreference(preferenceCategory); //Adding a category

            EditTextPreference editTextPreference = new EditTextPreference(contextThemeWrapper);
            editTextPreference.setKey("delete_"+head+"_key");
            editTextPreference.setTitle(R.string.prefs_delete_title);
            editTextPreference.setDefaultValue(head);
            editTextPreference.setSummary(getString(R.string.prefs_delete_sum)+head+"'");

            editTextPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(preference.getContext());
                    builder.setTitle(getString(R.string.delete_alert_title)+" '"+head+"'");
                    builder.setMessage(getString(R.string.delete_alert));
                    builder.setPositiveButton(getString(R.string.delete_key), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteAllData(head);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                    return true;
                }
            });
            getPreferenceScreen().addPreference(editTextPreference);

            EditTextPreference editTextData = new EditTextPreference(contextThemeWrapper);
            editTextData.setTitle("Keep data (days)");
            editTextData.setKey(head+"_key_data");
            editTextData.setDefaultValue("30");
            editTextData.setSummary("30");
            editTextData.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });
            root.addPreference(editTextData);

            EditTextPreference editTextMax = new EditTextPreference(contextThemeWrapper);
            editTextMax.setTitle("Max. Temperature");
            editTextMax.setKey(head+"_key_max");
            editTextMax.setDefaultValue("20");
            editTextMax.setSummary("20");
            editTextMax.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
            });
            root.addPreference(editTextMax);

            EditTextPreference editTextMin = new EditTextPreference(contextThemeWrapper);
            editTextMin.setTitle("Min. Temperature");
            editTextMin.setKey(head+"_key_min");
            editTextMin.setDefaultValue("0");
            editTextMin.setSummary("0");
            editTextMin.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
            });
            root.addPreference(editTextMin);

            EditTextPreference editTextMenu = new EditTextPreference(contextThemeWrapper);
            editTextMenu.setTitle("Menu Name");
            editTextMenu.setDefaultValue(head);
            editTextMenu.setKey(head+"_key_name");
            editTextMenu.setSummary(head);
            editTextMenu.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener()
            {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            });
            root.addPreference(editTextMenu);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
            String disp_sensor=prefs.getString(head + "_sens_name_0","");
/*
            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if(entry.getKey().startsWith(head)) {
                    Log.i(DEBUG_TAG,"Prefs: "+ entry.getKey() + ": " + entry.getValue().toString());
                }
            }
*/
            if(!disp_sensor.isEmpty()) {
                PreferenceCategory sensorBasedPrefCat = new PreferenceCategory(root.getContext());
                sensorBasedPrefCat.setTitle("Display Settings");
                root.addPreference(sensorBasedPrefCat); //Adding a category

                int j=0;
                while(true) {
                    disp_sensor=prefs.getString(head + "_sens_name_"+j,"");
                    Log.i(DEBUG_TAG,"Check: "+head+"_sens_name_"+j+": "+disp_sensor);
                    if(disp_sensor.isEmpty())
                    {
                        Log.i(DEBUG_TAG,"isEmpty()");
                        break;
                    }

                    EditTextPreference editSensorName = new EditTextPreference(contextThemeWrapper);
                    editSensorName.setTitle("Alias Name for " + (j+1)+". Sensor");
                    editSensorName.setDefaultValue(disp_sensor);
                    editSensorName.setKey(head + "_sens_name_" + j);
                    editSensorName.setSummary(disp_sensor);
//                    editText = editTextMenu.getEditText();
//                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    sensorBasedPrefCat.addPreference(editSensorName);
                    j++;
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(DEBUG_TAG, "onStop");
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(DEBUG_TAG, "onStart");
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i(DEBUG_TAG, "Key: " + key);

        Preference etp = findPreference(key);
        if(etp != null) {
            etp.setSummary(prefs.getString(key, ""));
        }
    }

    private void deleteAllData(String mode) {
        new Delete().from(Temperature.class).where("mode = ?", mode).execute();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getPreferenceScreen().getContext());
        int j=0;
        while(true) {
            String disp_sensor = prefs.getString(mode + "_sens_name_" + j, "");
            if (disp_sensor.isEmpty()) {
                break;
            }
            prefs.edit().remove(mode + "_sens_name_" + j).apply();
            Log.i(DEBUG_TAG, "Pref deleted: " + mode + "_sens_name_" + j + ": " + disp_sensor);
            j++;
        }
        MashPit.modedeleted=true;
    }
}
