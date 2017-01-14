package com.holger.mashpit.prefs;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.activeandroid.query.Delete;
import com.holger.mashpit.MashPit;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.R;

import java.util.ArrayList;

public class TempChartSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEBUG_TAG = "TempChartSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String Mode = getActivity().getIntent().getStringExtra("EXTRA_MODE");
        Log.i(DEBUG_TAG, "Mode: " + Mode);

        MashPit.modedeleted=false;

        addPreferencesFromResource(R.xml.prefs_tempchart);
        PreferenceScreen root = this.getPreferenceScreen();

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

        for (String head : entries) {
            Log.i(DEBUG_TAG,"Creating preferences for: "+head);

            PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(root.getContext());
            dialogBasedPrefCat.setTitle(getString(R.string.prefs_mode_category)+head+"'");
            root.addPreference(dialogBasedPrefCat); //Adding a category


            Preference editTextDel = new Preference( root.getContext());
            editTextDel.setTitle(R.string.prefs_delete_title);
            editTextDel.setKey("delete_"+head+"_key");
            editTextDel.setDefaultValue(head);
            editTextDel.setSummary(getString(R.string.prefs_delete_sum)+head+"'");
            editTextDel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    final String mode=preference.getPreferenceManager().getSharedPreferences().getString(preference.getKey(),"");
                    AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                    builder.setTitle(getString(R.string.delete_alert_title)+" '"+mode+"'");
                    builder.setMessage(getString(R.string.delete_alert));
                    builder.setPositiveButton(getString(R.string.delete_key), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteAllData(mode);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();

                    return true;
                }
            });
            dialogBasedPrefCat.addPreference(editTextDel);

            EditText editText;
            EditTextPreference editTextData = new SettingsEdit(root.getContext());
            editTextData.setTitle("Keep data (days)");
            editTextData.setKey(head+"_key_data");
            editTextData.setDefaultValue("30");
            editTextData.setSummary("30");
            editText = editTextData.getEditText();
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            dialogBasedPrefCat.addPreference(editTextData);

            EditTextPreference editTextMax = new SettingsEdit(root.getContext());
            editTextMax.setTitle("Max. Temperature");
            editTextMax.setKey(head+"_key_max");
            editTextMax.setDefaultValue("20");
            editTextMax.setSummary("20");
            editText = editTextMax.getEditText();
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            dialogBasedPrefCat.addPreference(editTextMax);

            EditTextPreference editTextMin = new SettingsEdit(root.getContext());
            editTextMin.setTitle("Min. Temperature");
            editTextMin.setKey(head+"_key_min");
            editTextMin.setDefaultValue("0");
            editTextMin.setSummary("0");
            editText = editTextMin.getEditText();
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            dialogBasedPrefCat.addPreference(editTextMin);

            EditTextPreference editTextMenu = new SettingsEdit(root.getContext());
            editTextMenu.setTitle("Menu Name");
            editTextMenu.setDefaultValue(head);
            editTextMenu.setKey(head+"_key_name");
            editTextMenu.setSummary(head);
            editText = editTextMenu.getEditText();
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogBasedPrefCat.addPreference(editTextMenu);

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

                    EditTextPreference editSensorName = new SettingsEdit(root.getContext());
                    editSensorName.setTitle("Alias Name for " + (j+1)+". Sensor");
                    editSensorName.setDefaultValue(disp_sensor);
                    editSensorName.setKey(head + "_sens_name_" + j);
                    editSensorName.setSummary(disp_sensor);
                    editText = editTextMenu.getEditText();
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
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
        etp.setSummary(prefs.getString(key, ""));
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
