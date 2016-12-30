package com.holger.mashpit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.prefs.SettingsActivity;
import com.holger.mashpit.prefs.TempChartSettings;
import com.holger.mashpit.tools.SnackBar;
import com.holger.mashpit.tools.TempFormatter;
import com.holger.mashpit.tools.TimestampFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TempChartActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "TempChartActivity" ;
    private List<Temperature> temps = null;
    private static String TempMode = "";
    private ProgressBar progress;
    private DrawerLayout mDrawerLayout;
    Handler handler = new Handler();
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    boolean doubleBackToExitPressedOnce = false;
    SharedPreferences prefs;
    float tempMin;
    float tempMax;
    ChartDataAdapter cda;
    NavigationView navigationView;
    TempChartData tempdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(DEBUG_TAG, "OnCreate");

        tempdata = TempChartData.getInstance();
        tempdata.clearData();

        setContentView(R.layout.activity_temp_chart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        final Menu menu;
        if (navigationView != null) {
            menu = navigationView.getMenu();
            MashPit.createSubMenu(menu,getApplicationContext());
        }

        Intent intent = getIntent();
        TempMode = intent.getStringExtra("MODE");
        Log.i(DEBUG_TAG, "Modus: "+TempMode);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        tempMin= MashPit.prefGetMin(prefs,TempMode);
        tempMax= MashPit.prefGetMax(prefs,TempMode);
        ab.setTitle(MashPit.prefGetName(prefs,TempMode));

        progress = (ProgressBar) findViewById(R.id.progressBar1);

        startLoadingData();

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        snb= new SnackBar(coordinatorLayout);
        snb.setmOnClickListener(
                mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(DEBUG_TAG, "Reconnect service");
                        Intent startIntent = new Intent(TempChartActivity.this, TemperatureService.class);
                        startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                        startService(startIntent);
                    }
                });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(DEBUG_TAG, "onConfiguratonChanged");
    }

    @Override
    protected void onDestroy() {
        Log.i(DEBUG_TAG, "onDestroy");
        snb.stopEvents();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        tempMin= MashPit.prefGetMin(prefs,TempMode);
        tempMax= MashPit.prefGetMax(prefs,TempMode);
        Log.i(DEBUG_TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(DEBUG_TAG, "onResume");
        super.onResume();
        boolean update=false;
        if(tempMin!= MashPit.prefGetMin(prefs,TempMode))
        {
            update=true;
        }
        if(tempMax!= MashPit.prefGetMax(prefs,TempMode))
        {
            update=true;
        }

        if(update) {
            Log.i(DEBUG_TAG, "Settings changed");
            cda.notifyDataSetChanged();
        }
        if(MashPit.modedeleted)
        {
            MashPit.refreshTempModes();
            final Menu menu;
            if (navigationView != null) {
                menu = navigationView.getMenu();
                MashPit.updateSubMenu(menu,getApplicationContext());
                MashPit.modedeleted=false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tempchart, menu);
        return true;
    }

        public void startLoadingData() {
        // do something long
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new Delete().from(Temperature.class).where("timeStamp < ?",getDeleteTimestamp()).and("mode=?",TempMode).execute();
                temps = queryData();
                generateData();
                progress.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.setProgress(1);
                        }
                    });

                handler.post(new Runnable(){
                    public void run() {
                        progress.setVisibility(View.GONE);
                        ListView lv = (ListView) findViewById(R.id.listView1);
                        Log.i(DEBUG_TAG, "Creating list");
                        cda = new ChartDataAdapter(getApplicationContext(), tempdata.getData());
                        if (lv != null) {
                            lv.setAdapter(cda);
                            lv.setClickable(true);
                        }
                     }
                });
            }
        };
        new Thread(runnable).start();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        MashPit.menu_action=true;

                        int id = menuItem.getItemId();

                        if(id<100)
                        {
                            tempdata.clearData();
                            selectTempChart(id);
                            return true;
                        }

                        switch (id) {

                            case android.R.id.home:
                                mDrawerLayout.openDrawer(GravityCompat.START);
                                return true;

                            case R.id.nav_process:
                                Log.i(DEBUG_TAG, "Process selected!");
                                Intent m = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(m);
                                finish();
                                break;

                            case R.id.nav_settings:
                                // Launch settings activity
                                Intent l = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(l);
                                break;

                        }
                        return true;
                    }
                });
    }

    private boolean selectTempChart(int resid)
    {
        Temperature temp = MashPit.TempModes.get(resid);
        Log.i(DEBUG_TAG,"selectTempChart: "+temp.Mode);
        Intent k = new Intent(getApplicationContext(), TempChartActivity.class);
        k.putExtra("MODE", temp.Mode);
        startActivity(k);
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_tempsettings:
                Log.i(DEBUG_TAG, "Settings selected");
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, TempChartSettings.class.getName() );
                intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
                intent.putExtra("EXTRA_MODE",TempMode);
                startActivity(intent);
                break;

         }
        return super.onOptionsItemSelected(item);
    }

    private void generateData() {

        int LINES = 3;
        int[] HOURS = new int[LINES];
        HOURS[0]=24;
        HOURS[1]=7*24;
        HOURS[2]=30*24;

        String[] DESC = new String[LINES];
        DESC[0]="24 Stunden";
        DESC[1]="7 Tage";
        DESC[2]="30 Tage";

        @SuppressWarnings({"unchecked"})
        List<List<Entry>>[] yVals = new ArrayList[LINES];
        @SuppressWarnings({"unchecked"})
        ArrayList<ILineDataSet>[]  set = new ArrayList[LINES];
        int[] count = new int[LINES];
        long[] ts = new long[LINES];

        for(int i=0;i<LINES;i++) {
            yVals[i] = new ArrayList<>();
            set[i] = new ArrayList<>();
            count[i]=0;
            ts[i]=getFromTimestamp(HOURS[i]);
        }

        ArrayList<Integer> linecolor = new ArrayList<>();
        ArrayList<String> sensors = new ArrayList<>();

        linecolor.add(Color.BLACK);
        linecolor.add(Color.RED);
        linecolor.add(Color.BLUE);
        linecolor.add(Color.YELLOW);
        linecolor.add(Color.CYAN);
        linecolor.add(Color.GREEN);
        linecolor.add(Color.MAGENTA);
        linecolor.add(Color.GRAY);

        Log.i(DEBUG_TAG, "generateData "+temps.size());

        for (Temperature temperature : temps) {
            float entry=round(temperature.Temp,1);

            if (!(sensors.contains(temperature.Name))) {
                Log.i(DEBUG_TAG,"Found sensor: "+temperature.Name);
                sensors.add(temperature.Name);
                for(int i=0;i<LINES;i++) {
                    yVals[i].add(new ArrayList<Entry>());
                }
            }
            int sensindex = sensors.indexOf(temperature.Name);

            for(int i=0;i<LINES;i++) {
                if (temperature.timeStamp > ts[i]) {
                    yVals[i].get(sensindex).add(new Entry((float)temperature.timeStamp,entry));
                    count[i]++;
                }
            }
        }
        LineDataSet xset;
        for(int k=0;k<LINES;k++) {
            for (int j=0;j<sensors.size();j++) {
                    xset=new LineDataSet(yVals[k].get(j), MashPit.prefGetSensorName(prefs, TempMode, j, sensors.get(j)));
                    xset.setValueFormatter(new TempFormatter());
                    xset.setValueTextSize(9f);
//                    xset.setDrawCubic(true);
                    xset.setCubicIntensity(0.4f);
                    xset.setLineWidth(2f);
                    xset.setDrawCircleHole(false);
                    xset.setFillAlpha(65);
                    xset.setColor(linecolor.get(j));
                    xset.setCircleColor(linecolor.get(j));
                    xset.setFillColor(linecolor.get(j));

                    set[k].add(xset);

            }
            Log.i(DEBUG_TAG, "generated Data" + k + ": " + count[k]);
            if(count[k]>0) {
                tempdata.setData(new LineData(set[k]));
                tempdata.setDesc(DESC[k]);
            }
        }

        Log.i(DEBUG_TAG, "generateData finished!");
    }

    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static List<Temperature> queryData() {
        if(TempMode.isEmpty()) {
            Log.i(DEBUG_TAG, "queryData: TempMode is empty");

            return new Select()
                    .from(Temperature.class)
                    .where("timeStamp > ?", getFromTimestamp(30 * 24))
                    .orderBy("timeStamp ASC")
                    .execute();
        }
        else
        {
            Log.i(DEBUG_TAG, "queryData: TempMode = "+TempMode);
            long ts=getFromTimestamp(30 * 24);
            return new Select()
                    .from(Temperature.class)
                    .where("timeStamp > ?", ts)
                    .and("mode = ?",TempMode)
                    .orderBy("timeStamp ASC")
                    .execute();
        }
    }

    public static long getFromTimestamp(int hours)
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime - (hours * 60 * 60);
    }

    public long getDeleteTimestamp()
    {
        int deldays = MashPit.prefGetDel(prefs,TempMode);
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime - (deldays * 24 * 60 * 60);
    }

    private class ChartDataAdapter extends ArrayAdapter<LineData> {

        ChartDataAdapter(Context context, List<LineData> objects) {
            super(context, 0, objects);

        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            LineData data = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tempchart,parent, false );
                holder.chart= (LineChart) convertView.findViewById(R.id.chartitem);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setValueFormatter(new TempFormatter());
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//            leftAxis.addLimitLine(ll1);
//            leftAxis.addLimitLine(ll2);

            leftAxis.setAxisMaximum(MashPit.prefGetMax(prefs,TempMode));
            leftAxis.setAxisMinimum(MashPit.prefGetMin(prefs,TempMode));

// set the formatter
            holder.chart.getAxisRight().setEnabled(false);

            XAxis xAxis = holder.chart.getXAxis();
//            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(new TimestampFormatter());

            // apply styling
//            holder.chart.setDescription("");
//            holder.chart.setDrawGridBackground(false);

            // set data
            holder.chart.setData(data);
//            holder.chart.setAutoScaleMinMaxEnabled(true);


            // do not forget to refresh the chart
            holder.chart.invalidate();
            holder.chart.animateXY(1000, 2000);

            Log.i(DEBUG_TAG, "Pos: "+position+" Desc: "+tempdata.getDesc(position));
            Description desc = new Description();
            desc.setText(tempdata.getDesc(position));
            holder.chart.setDescription(desc);

            holder.chart.setOnChartGestureListener(new OnChartGestureListener() {

                @Override
                public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture gesture) {

                }

                @Override
                public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture gesture) {

                }

                @Override
                public void onChartSingleTapped(MotionEvent me) {
                    selectLineChart(TempMode,position);
                }

                @Override
                public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {

                }

                @Override
                public void onChartLongPressed(MotionEvent me) {
                }

                @Override
                public void onChartFling(MotionEvent me1, MotionEvent me2,
                                         float velocityX, float velocityY) {
                }

                @Override
                public void onChartDoubleTapped(MotionEvent me) {

                }
            });


            return convertView;
        }

        private class ViewHolder {

            LineChart chart;
        }
    }

    private boolean selectLineChart(String linemode,int pos)
    {
        Log.i(DEBUG_TAG,"selectLineChart: "+pos);
        Intent k = new Intent(getApplicationContext(), LineChartActivity.class);
        k.putExtra("POS",pos);
        k.putExtra("MODE", linemode);
        startActivity(k);
        return true;
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
