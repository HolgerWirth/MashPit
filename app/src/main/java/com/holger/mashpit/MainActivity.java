package com.holger.mashpit;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.holger.mashpit.model.Charts;
import com.holger.mashpit.prefs.ProcessSettings;
import com.holger.mashpit.prefs.SettingsActivity;
import com.holger.mashpit.model.ChartsHandler;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import com.holger.share.Constants;

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "MainActivity";
    private static final String SERVICE_CLASSNAME = "com.holger.mashpit.TemperatureService";

    private DrawerLayout mDrawerLayout;
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    private SharedPreferences sp;

    boolean doubleBackToExitPressedOnce = false;
    NavigationView navigationView;

    public List<Charts> chartMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

/*
        DeleteData delData = new DeleteData();
        delData.deleteData();
*/

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

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
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    MashPit.menu_action=true;

                    Log.i(DEBUG_TAG, "setupDrawerContent()");

                    int id = menuItem.getItemId();
                    if(id<100)
                    {
                        Intent k = new Intent(getApplicationContext(), TempChartActivity.class);
                        k.putExtra("name",chartMenu.get(id).name);
                        k.putExtra("title",chartMenu.get(id).description);
                        startActivity(k);
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
                    if(id==R.id.nav_sensorconfig) {
                        Intent n = new Intent(getApplicationContext(), DeviceListActivity.class);
                        startActivity(n);
                    }
                    if(id==R.id.nav_temppager) {
                        Intent o = new Intent(getApplicationContext(), TempPagerActivity.class);
                        startActivity(o);
                    }
                    return true;
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
        snb.stopEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(DEBUG_TAG, "OnStart()...");
        EventBus.builder().addIndex(new MashPitEventBusIndex()).build();
        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_content);
        snb=new SnackBar(coordinatorLayout);
        snb.setmOnClickListener(
                mOnClickListener = v -> {
                    Log.i(DEBUG_TAG, "Retry service");
                    Intent startIntent = new Intent(MainActivity.this, TemperatureService.class);
                    startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                    startService(startIntent);
                });

        if (navigationView != null) {
            setupDrawerContent(navigationView);
            updateSubMenu(navigationView.getMenu(), getApplicationContext());
        }
    }

    public void updateSubMenu(Menu menu, Context context)
    {
        menu.removeGroup(1);
        createSubMenu(menu,context);
    }

    public void createSubMenu(Menu menu, Context context)
    {
        ChartsHandler myCharts = new ChartsHandler();
        chartMenu = myCharts.getallCharts();
        if(chartMenu.isEmpty())
        {
            return;
        }
        int i=0;
        SubMenu subMenu = menu.addSubMenu(1,0,0,context.getString(R.string.menu_title_charts));
        for(Charts charts : chartMenu)
        {
            subMenu.add(1, i, 0, charts.description).setIcon(R.drawable.ic_chart_line_bw);
            i++;
        }
        subMenu.setGroupCheckable(1, true, true);
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

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}
