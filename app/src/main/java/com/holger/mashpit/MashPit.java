package com.holger.mashpit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.SubMenu;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.holger.mashpit.events.ConfEvent;
import com.holger.mashpit.events.MPStatusEvent;
import com.holger.mashpit.model.Temperature;

import java.util.ArrayList;
import java.util.List;

public class MashPit extends com.activeandroid.app.Application {
    private static final String DEBUG_TAG = "MashPit";
    static Boolean menu_action=false;
    public static Boolean reconnect_action=false;
    public static List<Temperature> TempModes;
    public static boolean modedeleted;
    public static String mDeviceId;
    public static String MPDomain;
    public static String MPDomain_send;

    public static ArrayList<ConfEvent> confXMLList = new ArrayList<>();
    public static ArrayList<MPStatusEvent> MPServerList = new ArrayList<>();
    public static ArrayList<MPStatusEvent> MPStatusList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Notice this initialization code here
        ActiveAndroid.initialize(this);

        mDeviceId = String.format("MP_%s", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        if(TempModes==null)
        {
            refreshTempModes();
        }
        Log.i(DEBUG_TAG, "Initialized!");
    }

    public static void refreshTempModes()
    {
        Log.i(DEBUG_TAG, "refreshTempModes()");
        TempModes = queryData();
        if(TempModes!=null) {
            for (Temperature temperature : TempModes) {
                Log.i(DEBUG_TAG, "Mode: " + temperature.Mode);
            }
        }
        else
        {
            TempModes = new ArrayList<>();
        }
    }

    public static List<Temperature> queryData() {
            return new Select()
                    .distinct()
                    .from(Temperature.class)
                    .where("mode not null")
                    .groupBy("mode")
                    .execute();
        }

    public static void updateSubMenu(Menu menu,Context context)
    {
        menu.removeGroup(1);
        createSubMenu(menu,context);
    }

    public static void createSubMenu(Menu menu, Context context)
    {
        if (!MashPit.TempModes.isEmpty())
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SubMenu subMenu = menu.addSubMenu(1,0,0,context.getString(R.string.menu_title_charts));
            int i = 0;
            for (Temperature temperature : MashPit.TempModes) {
                String key=temperature.Mode+"_key_name";
                String menuname;
                if(sp.contains(key)) {
                    menuname = sp.getString(temperature.Mode + "_key_name", temperature.Mode);
                }
                else
                {
                    menuname = temperature.Mode;
                }
                subMenu.add(1, i, 0, menuname).setIcon(R.drawable.ic_chart_line_bw);
                i++;
            }
            subMenu.setGroupCheckable(1, true, true);
        }
    }

    public static float prefGetMax(SharedPreferences prefs,String mode)
    {
        String key=mode+"_key_max";
        if(prefs.contains(key))
        {
            return Float.parseFloat(prefs.getString(key, "20"));
        }
        return 20.0f;
    }

    public static float prefGetMin(SharedPreferences prefs,String mode)
    {
        String key=mode+"_key_min";
        if(prefs.contains(key))
        {
            return Float.parseFloat(prefs.getString(key, "-5"));
        }
        return -5f;
    }

    public static int prefGetDel(SharedPreferences prefs,String mode)
    {
        String key=mode+"_key_data";
        if(prefs.contains(key))
        {
            return Integer.parseInt(prefs.getString(key, "30"));
        }
        return 30;
    }

    public static String prefGetName(SharedPreferences prefs,String mode)
    {
        String key=mode+"_key_name";
        if(prefs.contains(key))
        {
            return prefs.getString(key,"Temperature");
        }
        return "Temperature";
    }

    public static String prefGetSensorName(SharedPreferences prefs,String mode,int pos, String def)
    {
        String key=mode+"_sens_name_"+pos;
        if(prefs.contains(key))
        {
            return prefs.getString(key,def);
        }
        else
        {
            prefs.edit().putString(key, def).apply();
        }
        return def;
    }
}
