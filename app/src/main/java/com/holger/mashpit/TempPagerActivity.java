package com.holger.mashpit;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.holger.mashpit.events.TemperatureEvent;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.prefs.SettingsActivity;
import com.holger.mashpit.tools.SnackBar;
import com.holger.share.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class TempPagerActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "TempPagerActivity";
    NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    float cAngle = 270f;
    boolean doubleBackToExitPressedOnce = false;

    SnackBar snb;
    View.OnClickListener mOnClickListener;
    TempPagerAdapter pagerAdapter;
    List<View> pieCharts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(DEBUG_TAG, "OnCreate");
        setContentView(R.layout.activity_temppager);

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

        final Menu menu;
        if (navigationView != null) {
            menu = navigationView.getMenu();
            MashPit.createSubMenu(menu, getApplicationContext());
        }

        ViewPager pager = findViewById(R.id.pagerview);
        pagerAdapter = new TempPagerAdapter(pieCharts);
        pager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.temptabs);
        tabLayout.addTab(tabLayout.newTab().setText("Text"));
        tabLayout.setupWithViewPager(pager);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        MashPit.menu_action = true;

                        Log.i(DEBUG_TAG, "setupDrawerContent()");

                        int id = menuItem.getItemId();
                        if (id < 100) {
                            selectTempChart(id);
                            return true;
                        }

                        switch (id) {
                            case android.R.id.home:
                                mDrawerLayout.openDrawer(GravityCompat.START);
                                return true;

                            case R.id.nav_settings:
                                Intent l = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(l);
                                break;

                            case R.id.nav_config:
                                Intent m = new Intent(getApplicationContext(), ConfListActivity.class);
                                startActivity(m);
                                break;

                            case R.id.nav_process:
                                Log.i(DEBUG_TAG, "Process selected!");
                                Intent n = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(n);
                                finish();
                                break;
                        }
                        return true;
                    }
                });
    }

    private void selectTempChart(int resid) {
        Temperature temp = MashPit.TempModes.get(resid);
        Log.i(DEBUG_TAG, "selectTempChart: " + temp.Mode);
        Intent k = new Intent(getApplicationContext(), TempChartActivity.class);
        k.putExtra("MODE", temp.Mode);
        startActivity(k);
        finish();
    }

    private PieChart createPiePage(String desc) {
        PieChart mChart = new PieChart(this);

        mChart.setContentDescription(desc);
        mChart.setMaxAngle(360);
        mChart.setHoleRadius(60f);
        mChart.setTransparentCircleRadius(97f);
        mChart.setDrawCenterText(true);
        mChart.setDrawHoleEnabled(true);
        mChart.setRotationAngle(cAngle);
        mChart.setRotationEnabled(false);

        Legend l = mChart.getLegend();
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setEnabled(false);

        mChart.setCenterText("--");
        mChart.setCenterTextSize(20);

        List<PieEntry> entries = new ArrayList<>();
        for (int t = 0; t < 1; t++) {
            entries.add(new PieEntry(360, ""));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);

        dataSet.setColor(Color.BLUE);
        dataSet.setHighlightEnabled(true);
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        mChart.setBackgroundColor(Color.BLUE);
        mChart.setHoleColor(Color.WHITE);
        mChart.setCenterTextColor(Color.BLACK);

        mChart.setData(data);
        mChart.setDrawEntryLabels(false);
        mChart.highlightValue(0, 0);

        mChart.setBackgroundColor(Color.BLUE);

        Description pieDesc = new Description();
        pieDesc.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        pieDesc.setTextSize((float) 16.0);
        pieDesc.setText(desc);
        mChart.setDescription(pieDesc);

        return mChart;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getTempEvent(TemperatureEvent myEvent) {
        View pieChart;
        boolean found = false;

        Log.i(DEBUG_TAG, "getTempEvent");
        if (myEvent != null) {
            Log.i(DEBUG_TAG, "TempEvent arrived: " + myEvent.getTopic());
            for(int i=0; i<pieCharts.size(); i++)
            {
                pieChart = pieCharts.get(i);
                if(myEvent.getSensor().contentEquals(pieChart.getContentDescription()))
                {
                    Log.i(DEBUG_TAG, "getTempEvent: "+pieChart.getContentDescription()+" found!");
                    ((PieChart) pieChart).setCenterText(myEvent.getEvent());
                    pagerAdapter.notifyDataSetChanged();
                    pagerAdapter.updatePie(i);
                    found = true;
                    break;
                }
            }
            if(!found)
            {
                pieChart=createPiePage(myEvent.getSensor());
                ((PieChart) pieChart).setCenterText(myEvent.getEvent());
                pieCharts.add(pieChart);
                pagerAdapter.notifyDataSetChanged();
            }
        }
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
        EventBus.getDefault().register(this);
        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_content);
        snb=new SnackBar(coordinatorLayout);

        snb.setmOnClickListener(
                mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(DEBUG_TAG, "Retry service");
                        Intent startIntent = new Intent(TempPagerActivity.this, TemperatureService.class);
                        startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                        startService(startIntent);
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(DEBUG_TAG, "onOptionsItemSelected()");
        int id = item.getItemId();
        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
