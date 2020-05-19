package com.holger.mashpit;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.holger.mashpit.events.SensorDataEvent;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.prefs.SettingsActivity;
import com.holger.mashpit.tools.SnackBar;
import com.holger.mashpit.tools.SubscriptionHandler;
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

    String action = "Pager";
    SubscriptionHandler subscriptionHandler;

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

        subscriptionHandler = new SubscriptionHandler(action);

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
                                Intent m = new Intent(getApplicationContext(), MPStatusListActivity.class);
                                startActivity(m);
                                break;

                            case R.id.nav_sensorconfig:
                                Intent n = new Intent(getApplicationContext(), SensorStatusListActivity.class);
                                startActivity(n);
                                break;

                            case R.id.nav_process:
                                Log.i(DEBUG_TAG, "Process selected!");
                                Intent o = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(o);
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
    public void getTempEvent(SensorDataEvent myEvent) {
        View pieChart;
        boolean found = false;
        String alias = subscriptionHandler.getSensorAlias(myEvent.getServer(),myEvent.getSensor());
        if (subscriptionHandler.checkSubscription(myEvent.getTopicString())) {
            Log.i(DEBUG_TAG, "SensorDataEvent arrived: " + myEvent.getTopicString());
            for (int i = 0; i < pieCharts.size(); i++) {
                pieChart = pieCharts.get(i);
                if (alias.contentEquals(pieChart.getContentDescription())) {
                    ((PieChart) pieChart).setCenterText(myEvent.getData("Temp") + "°");
                    pagerAdapter.notifyDataSetChanged();
                    pagerAdapter.updatePie(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                Log.i(DEBUG_TAG, "New page created: " + alias);
                pieChart = createPiePage(alias);
                ((PieChart) pieChart).setCenterText(myEvent.getData("Temp") + "°");
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
        snb = new SnackBar(coordinatorLayout);
        subscriptionHandler.refreshSubscription();

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
}
