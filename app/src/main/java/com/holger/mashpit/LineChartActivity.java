package com.holger.mashpit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.holger.mashpit.prefs.SettingsActivity;
import com.holger.mashpit.prefs.ChartSettings;
import com.holger.mashpit.tools.TempFormatter;
import com.holger.mashpit.tools.TimestampFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LineChartActivity extends AppCompatActivity implements OnChartGestureListener {

    private static final String DEBUG_TAG = "LineChartActivity" ;

    private static String TempMode = "";
    SharedPreferences prefs;
    int mpos;
    LineData data;
    TempChartData tempdata;
    String descTitle;

    List<List<Entry>> yVals = new ArrayList<>();
    ArrayList<String> sensors = new ArrayList<>();

    private LineChart mChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(DEBUG_TAG,"onCreate()");

        tempdata = TempChartData.getInstance();

        setContentView(R.layout.activity_sub_linechart);

        Intent intent = getIntent();
        TempMode = intent.getStringExtra("MODE");
        Log.i(DEBUG_TAG, "Modus: "+TempMode);
        mpos = intent.getIntExtra("POS",0);
        Log.i(DEBUG_TAG, "Position: "+mpos);
        if(mpos<0)
        {
            descTitle=intent.getStringExtra("TITLE");
        }
        else {
            descTitle=tempdata.getDesc(mpos);
        }
        Log.i(DEBUG_TAG, "Title: "+descTitle);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar toolbar= findViewById(R.id.my_chart_toolbar);
/*
        if (toolbar != null) {
            toolbar.setTitle(MashPit.prefGetName(prefs,TempMode));
        }
*/
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                overridePendingTransition(0,0);
                finish();
            });
        }

        mChart = findViewById(R.id.chart2);
        if (mChart != null) {
            mChart.setOnChartGestureListener(this);
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);

            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setPinchZoom(true);
        }

//        tempMin= MashPit.prefGetMin(prefs,TempMode);
//        tempMax= MashPit.prefGetMax(prefs,TempMode);

        Description desc = new Description();
        desc.setText(descTitle);
        mChart.setDescription(desc);

        if(!setTempData()) return;

        mChart.animateXY(1000, 2000);

        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tempchart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_tempsettings) {
            Log.i(DEBUG_TAG, "Settings selected");
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ChartSettings.class.getName());
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            intent.putExtra("EXTRA_MODE", TempMode);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public static long getFromTimestamp(int hours)
    {
        long unixTime = System.currentTimeMillis() / 1000L;
//        return unixTime - (30 * 24 * 60 * 60);
        return unixTime - ((long) hours * 60 * 60 );

    }

    public boolean setTempData()
    {
        Log.i(DEBUG_TAG,"setTempData");

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter (new TempFormatter("0.0","°"));
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

//        leftAxis.setAxisMaximum(MashPit.prefGetMax(prefs,TempMode));
//        leftAxis.setAxisMinimum(MashPit.prefGetMin(prefs,TempMode));

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new TimestampFormatter());

        mChart.getAxisRight().setEnabled(false);

        if(mpos<0) {
            ArrayList<ILineDataSet>  set = new ArrayList<>();
/*
            List<Temperature> temps = getAll(24);
            if(temps.size()==0)
            {
                temps = getAll(30 * 24);
            }

            if(temps.size()==0) return false;
*/
            ArrayList<Integer> linecolor = new ArrayList<>();
            linecolor.add(Color.BLACK);
            linecolor.add(Color.RED);
            linecolor.add(Color.BLUE);
            linecolor.add(Color.YELLOW);
            linecolor.add(Color.CYAN);
            linecolor.add(Color.GREEN);
            linecolor.add(Color.MAGENTA);
            linecolor.add(Color.GRAY);

            /*
            for (Temperature temperature : temps) {

                float entry=round(temperature.Temp,1);

                if (!(sensors.contains(temperature.Name))) {
                    Log.i(DEBUG_TAG,"Found sensor: "+temperature.Name);
                    sensors.add(temperature.Name);
                    yVals.add(new ArrayList<>());
                }
                int sensindex = sensors.indexOf(temperature.Name);
                yVals.get(sensindex).add(new Entry((float)temperature.timeStamp,entry));
            }
            */

            LineDataSet xset;
            for (int j=0;j<sensors.size();j++) {
                xset = new LineDataSet(yVals.get(j), "");

                xset.setValueFormatter(new TempFormatter("0.0","°"));
                xset.setCubicIntensity(0.4f);
                xset.setLineWidth(2f);
                xset.setDrawCircleHole(false);
                xset.setValueTextSize(9f);
                xset.setFillAlpha(65);

                xset.setColor(linecolor.get(j));
                xset.setCircleColor(linecolor.get(j));
                xset.setFillColor(linecolor.get(j));
                set.add(xset);
            }
            data = new LineData(set);
        }
        else {
            data = tempdata.getData(mpos);
            data.getEntryCount();
        }
        mChart.setData(data);
        return true;
    }

    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).floatValue();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    protected void onDestroy() {
        Log.i(DEBUG_TAG, "OnDestroy()...");
        super.onDestroy();
        }
}
