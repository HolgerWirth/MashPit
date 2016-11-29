package com.holger.mashpit.prefs;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.holger.mashpit.R;

import java.util.List;

public class SettingsActivity extends PreferenceActivity
{
    private static final String DEBUG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String tempMode = intent.getStringExtra("EXTRA_MODE");
        Log.i(DEBUG_TAG, "Modus: "+ tempMode);

        LinearLayout linearLayout = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, linearLayout, false);

        toolbar.setTitle(R.string.settings_title);
        linearLayout.addView(toolbar, 0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0,0);
                finish();
            }
        });

    }

    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target)
    {
        loadHeadersFromResource(R.xml.prefs_header, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        Log.i(DEBUG_TAG, "isValidFragment: "+fragmentName);

        if(fragmentName.equals("com.holger.mashpit.prefs.TempChartSettings"))
        {
            return TempChartSettings.class.getName().equals(fragmentName);
        }
        if(fragmentName.equals("com.holger.mashpit.prefs.MQTTSettings"))
        {
            return MQTTSettings.class.getName().equals(fragmentName);
        }
        if(fragmentName.equals("com.holger.mashpit.prefs.ProcessSettings"))
        {
            return ProcessSettings.class.getName().equals(fragmentName);
        }
        if(fragmentName.equals("com.holger.mashpit.prefs.ServiceSettings"))
        {
            return ServiceSettings.class.getName().equals(fragmentName);
        }
        return false;
    }
}