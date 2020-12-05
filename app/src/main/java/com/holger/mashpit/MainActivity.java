package com.holger.mashpit;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.holger.mashpit.events.ProcessEvent;
import com.holger.mashpit.events.TemperatureEvent;
import com.holger.mashpit.model.Process;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.prefs.ProcessSettings;
import com.holger.mashpit.prefs.SettingsActivity;
import com.holger.mashpit.tools.RastFormatter;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.holger.share.Constants;

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "MainActivity";
    private static final String SERVICE_CLASSNAME = "com.holger.mashpit.TemperatureService";

    private PieChart mChart;
    String centerText = "---°";
    String currTemp = "---°";
    String descTitle;
    float cAngle=270f;

    private DrawerLayout mDrawerLayout;
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    private SharedPreferences sp;

    boolean doubleBackToExitPressedOnce = false;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawerLayout);

        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        TempChartData tempdata = TempChartData.getInstance();
        tempdata.clearData();

        mChart = findViewById(R.id.chart1);

        if (mChart != null) {
            mChart.setUsePercentValues(false);

            mChart.setMaxAngle(360);
            mChart.setDrawEntryLabels(true);
//            mChart.setDrawSliceText(true);
            mChart.setHoleRadius(60f);
            mChart.setTransparentCircleRadius(63f);

            mChart.setDrawCenterText(true);

            mChart.setDrawHoleEnabled(true);

            mChart.setRotationAngle(cAngle);
            // enable rotation of the chart by touch
            mChart.setRotationEnabled(false);
            mChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Semibold.ttf"));

            mChart.setOnChartGestureListener(new OnChartGestureListener() {

                @Override
                public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture gesture) {
                    Log.i(DEBUG_TAG, "Gesture: started");

                }

                @Override
                public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture gesture) {
                    Log.i(DEBUG_TAG, "Gesture: ended");

                }

                @Override
                public void onChartSingleTapped(MotionEvent me) {
                    Log.i(DEBUG_TAG, "Gesture: single tapped");
                    RectF rect = mChart.getCircleBox();
                    if (rect.contains(me.getX(), me.getY())) {
                        Log.i(DEBUG_TAG, "Gesture: single tapped in circle");
                        selectLineChart("mash");
                    }
                }

                @Override
                public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {

                }

                @Override
                public void onChartLongPressed(MotionEvent me) {
                    Log.i(DEBUG_TAG, "Gesture: long pressed");
                }

                @Override
                public void onChartFling(MotionEvent me1, MotionEvent me2,
                                         float velocityX, float velocityY) {
                }

                @Override
                public void onChartDoubleTapped(MotionEvent me) {
                    Log.i(DEBUG_TAG, "Gesture: double pressed");

                }
            });
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstStart = sp.getBoolean(Constants.PREFS.PREFS_KEY_FIRST_START, true);
        if(isFirstStart)
        {
            Log.i(DEBUG_TAG, "App first start");
            sp.edit().putBoolean(Constants.PREFS.PREFS_KEY_FIRST_START, false).apply();
        }
        else {
            Log.i(DEBUG_TAG, "App already started");
        }

        if(sp.getBoolean("ScreenOn",false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.i(DEBUG_TAG, "Keep screen on");
        }

        final Menu menu;
        if (navigationView != null) {
            menu = navigationView.getMenu();
            MashPit.createSubMenu(menu,getApplicationContext());
        }

        Legend l = mChart.getLegend();
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        TemperatureEvent mqttEvent = EventBus.getDefault().getStickyEvent(TemperatureEvent.class);

        if (mqttEvent != null) {
            centerText = mqttEvent.getEvent();
        }

        mChart.setCenterText(centerText);
        mChart.setCenterTextSize(20);

        try {
            setData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mChart.animateXY(1500, 1500);
        mChart.spin(2000, 0, cAngle, Easing.EaseInOutCirc);

        if (!serviceIsRunning()) {
            Log.i(DEBUG_TAG, "Starting service");

            Intent startIntent = new Intent(MainActivity.this, TemperatureService.class);
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(startIntent);
        }
        else {
            if (!MashPit.menu_action) {
                Log.i(DEBUG_TAG, "Checking connection");
                Intent startIntent = new Intent(MainActivity.this, TemperatureService.class);
                startIntent.setAction(Constants.ACTION.CHECK_ACTION);
                startService(startIntent);
            }
        }

        if(MashPit.TempModes != null)
        {
            if(MashPit.TempModes.isEmpty()) {
                Log.i(DEBUG_TAG, "Refresh TempModes");
                MashPit.refreshTempModes();
            }
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        MashPit.menu_action=true;

                        Log.i(DEBUG_TAG, "setupDrawerContent()");

                        int id = menuItem.getItemId();
                        if(id<100)
                        {
                            selectTempChart(id);
                            return true;
                        }
                        if(id==android.R.id.home)
                        {
                            mDrawerLayout.openDrawer(GravityCompat.START);
                            return true;
                        }
                        if(id==R.id.nav_settings)
                        {
                            Intent l = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(l);
                        }
                        if(id==R.id.nav_config)
                        {
                            Intent m = new Intent(getApplicationContext(), MPStatusListActivity.class);
                            startActivity(m);
                        }
                        if(id==R.id.nav_sensorconfig) {
                            Intent n = new Intent(getApplicationContext(), SensorStatusListActivity.class);
                            startActivity(n);
                        }
                        if(id==R.id.nav_temppager) {
                            Intent o = new Intent(getApplicationContext(), TempPagerActivity.class);
                            startActivity(o);
                        }
                        return true;
                    }
                });
    }

    private boolean serviceIsRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (SERVICE_CLASSNAME.equals(service.service.getClassName())) {
                    Log.i(DEBUG_TAG, "Service: service is running");
                    return true;
                }
            }
        }
        return false;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getTempEvent(TemperatureEvent myEvent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.i(DEBUG_TAG, "getTempEvent");
        if (myEvent != null) {
            Log.i(DEBUG_TAG, "TempEvent arrived: " + myEvent.getTopic());

            if(myEvent.getQoS()==2) {
                boolean found = false;
                for (Temperature temperature : MashPit.TempModes) {
                    if (temperature.Mode.equals(myEvent.getMode())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    MashPit.refreshTempModes();
                    final Menu menu;
                    if (navigationView != null) {
                        menu = navigationView.getMenu();
                        MashPit.updateSubMenu(menu, getApplicationContext());
                    }

                }
            }
            Set<String> prefdefaults = prefs.getStringSet("process_topics", new HashSet<String>());
            if (prefdefaults.contains(myEvent.getSensor() + "/" + myEvent.getInterval())) {
                currTemp = myEvent.getEvent();
                updatePieData(currTemp);
            }

            mChart.invalidate();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getProcessEvent(ProcessEvent myEvent)
    {
        Log.i(DEBUG_TAG, "getProcessEvent: ");
        Log.i(DEBUG_TAG, "ProcessEvent arrived: " + myEvent.getTopic());
        currTemp = myEvent.getTemp();
        if(currTemp==null)
        {
            currTemp = "---°";
        }
        updatePieData(currTemp);
        mChart.invalidate();
    }

    protected void setCenterHeater(String temp, String toTemp) {
        mChart.setHoleColor(Color.RED);

        SpannableString styledString = new SpannableString(temp + "\n" +
                getString(R.string.proc_heat) +
                toTemp + "°");

        int textlength= getString(R.string.proc_heat).length();

        styledString.setSpan(new RelativeSizeSpan(2f), 0, temp.length(), 0);
        styledString.setSpan(new RelativeSizeSpan(1f), temp.length() + 1, textlength, 0);
        styledString.setSpan(new RelativeSizeSpan(1f), temp.length() + textlength+1, temp.length() + textlength+1 + toTemp.length() + 1, 0);
        mChart.setCenterText(styledString);
    }

    protected void setCenterRast(String name, String minute, String temp) {
        mChart.setHoleColor(Color.BLUE);

        SpannableString styledString = new SpannableString(minute + "\n" +
                name + "\n" +
                temp);

        styledString.setSpan(new RelativeSizeSpan(2f), 0, minute.length(), 0);
        styledString.setSpan(new RelativeSizeSpan(1f), minute.length() + 1, minute.length() + 1 + name.length(), 0);
        styledString.setSpan(new RelativeSizeSpan(1f), minute.length() + 1 + name.length() + 1, minute.length() + 1 + name.length() + 1 + temp.length(), 0);
        mChart.setCenterText(styledString);
    }

    protected void setCenterEnd(String temp) {
        mChart.setHoleColor(Color.CYAN);

        SpannableString styledString = new SpannableString(getString(R.string.proc_stop) +
                temp);

        styledString.setSpan(new RelativeSizeSpan(2f), 0, 4, 0);
        styledString.setSpan(new RelativeSizeSpan(1f), 5, 5 + temp.length(), 0);
        mChart.setCenterText(styledString);
    }

    @Override
    protected void onDestroy() {
        Log.i(DEBUG_TAG, "OnDestroy()...");
        sp.edit().putBoolean(Constants.PREFS.PREFS_KEY_FIRST_START, true).apply();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(DEBUG_TAG, "onOptionsItemSelected()");

        int id = item.getItemId();
        if(id==android.R.id.home)
        {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        if(id==R.id.action_procsettings)
        {
            Log.i(DEBUG_TAG, "Settings selected");
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, ProcessSettings.class.getName() );
            intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(DEBUG_TAG, "OnStop()...");
        EventBus.getDefault().unregister(this);
        snb.stopEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(DEBUG_TAG, "OnStart()...");
        EventBus.builder().addIndex(new MashPitEventBusIndex()).build();
        EventBus.getDefault().register(this);
        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_content);
        snb=new SnackBar(coordinatorLayout);

        snb.setmOnClickListener(
                mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(DEBUG_TAG, "Retry service");
                        Intent startIntent = new Intent(MainActivity.this, TemperatureService.class);
                        startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                        startService(startIntent);
                    }
                });

    }

    private void selectTempChart(int resid)
    {
        Temperature temp = MashPit.TempModes.get(resid);
        Log.i(DEBUG_TAG,"selectTempChart: "+temp.Mode);
        Intent k = new Intent(getApplicationContext(), TempChartActivity.class);
        k.putExtra("MODE", temp.Mode);
        startActivity(k);
        finish();
    }

    private void selectLineChart(String linemode)
    {
        Log.i(DEBUG_TAG,"selectLineChart: "+linemode);
        Intent k = new Intent(getApplicationContext(), LineChartActivity.class);
        k.putExtra("MODE", linemode);
        k.putExtra("POS",-1);
        k.putExtra("TITLE",descTitle);
        startActivity(k);
    }

    public static List<Process> getAll() {
        return new Select()
                .from(Process.class)
                .execute();
    }

    private void setData() throws JSONException {

        String message = "";

        List<Process> currproc = getAll();
        for (Process process : currproc) {
            message = process.myJSONString;
        }

        if (message.isEmpty()) {
            Log.i(DEBUG_TAG, "Process message is empty");
            return;
        }

        initPieData(message);
        updatePieData(currTemp);

        mChart.highlightValues(null);

        mChart.invalidate();
    }

    protected void initPieData(String JSONMessage) throws JSONException {
        JSONObject obj = new JSONObject(JSONMessage);
        JSONArray jprocess = obj.getJSONArray("Proc");
        int count = jprocess.length();
        String[] mRast = new String[count];
        Float[] mDauer = new Float[count];
        String[] mTemp = new String[count];
        for (int i = 0; i < count; i++) {
            JSONObject jproc = jprocess.getJSONObject(i);
            mRast[i] = jproc.getString("name");
            mTemp[i] = jproc.getString("temp");
            mDauer[i] = (float) jproc.getInt("length");
        }

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
              entries.add(new PieEntry(mDauer[i], mRast[i] + " " + mTemp[i] + "°"));
        }
        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setSliceSpace(3f);

        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new RastFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);
        mChart.invalidate();
    }

    protected void updatePieData(String currtemp) {
        Log.i(DEBUG_TAG, "updatePie");

        String message = "";

        List<Process> currproc = getAll();
        for (Process process : currproc) {
            message = process.myJSONString;
        }

        if (message.isEmpty()) {
            Log.i(DEBUG_TAG, "Process message is empty");
            return;
        }

        JSONObject obj;
        try {
            obj = new JSONObject(message);
            descTitle=obj.getString("title");
            Description desc = new Description();
            desc.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            desc.setTextSize((float) 16.0);
            desc.setText(descTitle);
            mChart.setDescription(desc);

            if (obj.getString("Heizen").equals("an")) {
                String toTemp = obj.getString("Ziel");
                mChart.setRotationAngle(cAngle);
                setCenterHeater(currtemp, toTemp);
            }
            if (obj.getString("Heizen").equals("aus")) {
                setCenterRast(obj.getString("Rast"), String.valueOf((obj.getInt("Dauer") - obj.getInt("Minute"))), currtemp);

                int mMin = obj.getInt("Minute");
                String name = obj.getString("Rast");
                JSONArray jprocess = obj.getJSONArray("Proc");

                float[] mAngles = mChart.getAbsoluteAngles();
                float[] dAngles = mChart.getDrawAngles();
                float step=1.0f;
                int count = jprocess.length();
                int cRast = 0;
                for (int i = 0; i < count; i++) {
                    JSONObject jproc;
                    jproc = jprocess.getJSONObject(i);
                    if (jproc.getString("name").equals(name)) {
                        int pDauer = jproc.getInt("length");
                        step=(dAngles[i]/pDauer);
                        cRast=i;
                        break;
                    }
                }

                if(cRast==0)
                {
                    cAngle=(270f - ((mMin+1) * step));
                }
                else {
                    cAngle = (270f - mAngles[cRast-1] - ((mMin+1) * step));
                }
                mChart.setRotationAngle(cAngle);
            }
            if (obj.getString("Heizen").equals("ende")) {
                setCenterEnd(currtemp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            MashPit.menu_action=false;
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.click_back, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
