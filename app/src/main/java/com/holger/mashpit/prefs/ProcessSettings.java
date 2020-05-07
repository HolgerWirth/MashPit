package com.holger.mashpit.prefs;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.holger.mashpit.R;

public class ProcessSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_process, rootKey);
    }
}
