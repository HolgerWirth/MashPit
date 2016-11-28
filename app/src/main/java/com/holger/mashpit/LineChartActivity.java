package com.holger.mashpit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.activeandroid.query.Select;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.holger.mashpit.events.TemperatureEvent;
import com.holger.mashpit.model.Temperature;
import com.holger.mashpit.tools.TempFormatter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LineChartActivity extends AppCompatActivity implements OnChartGestureListener {

    private static final String DEBUG_TAG = "LineChartActivity" ;

    private ArrayList<String> xVals = new ArrayList<>();
    private ArrayList<Entry> yVals1 = new ArrayList<>();
    private ArrayList<Entry> yVals2 = new ArrayList<>();
    private int lastEntry;
    private static String TempMode = "";
    SharedPreferences prefs;
    float tempMin;
    float tempMax;
    int mpos;
    LineData data;
    TempChartData tempdata;
    String descTitle;

    private LineChart mChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        Toolbar toolbar= (Toolbar) findViewById(R.id.my_chart_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(MashPit.prefGetName(prefs,TempMode));
        }
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    overridePendingTransition(0,0);
                    finish();
                }
            });
        }

        mChart = (LineChart) findViewById(R.id.chart2);
        if (mChart != null) {
            mChart.setOnChartGestureListener(this);
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);

            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setPinchZoom(true);
        }

        tempMin= MashPit.prefGetMin(prefs,TempMode);
        tempMax= MashPit.prefGetMax(prefs,TempMode);

        mChart.setDescription(descTitle);
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        setTempData();

        mChart.animateXY(1000, 2000);

        if(mpos<0)
        {
            EventBus.getDefault().register(this);
        }
        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_line_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static List<Temperature> getAll(int hours) {
        return new Select()
                .from(Temperature.class)
                .where("timeStamp > ?",getFromTimestamp(hours))
                .and("mode = ?",TempMode)
                .orderBy("timeStamp ASC")
                .execute();
    }

    public static long getFromTimestamp(int hours)
    {
        long unixTime = System.currentTimeMillis() / 1000L;
//        return unixTime - (30 * 24 * 60 * 60);
        return unixTime - (hours * 60 * 60 );

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getTemperatureEvent(TemperatureEvent myEvent) {

        Log.i(DEBUG_TAG, "getTemperatureEvent");
        if(myEvent != null) {
            if (myEvent.getQoS() > 0) {
                SimpleDateFormat fmtout = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                Date df = new java.util.Date(myEvent.getTimestamp() * 1000);
                Log.i(DEBUG_TAG, myEvent.getSensor() + ": Timestamp: " + Long.toString(myEvent.getTimestamp()) + " Format: " + fmtout.format(df));
                xVals.add(fmtout.format(df));

                lastEntry++;
                if (myEvent.getSensor().equalsIgnoreCase("sensor1")) {
                    yVals1.add(new Entry(round(myEvent.getTemperature(), 1), lastEntry));
                }
                if (myEvent.getSensor().equalsIgnoreCase("sensor2")) {
                    yVals2.add(new Entry(round(myEvent.getTemperature(), 1), lastEntry));
                }
                mChart.notifyDataSetChanged();
                Log.i(DEBUG_TAG, "DataSet changed");
            }
        }
    }

    public void setTempData()
    {
        Log.i(DEBUG_TAG,"setTempData");

        LineDataSet set1 = new LineDataSet(yVals1, "Sensor 1");
        LineDataSet set2 = new LineDataSet(yVals2, "Sensor 2");
        set1.setValueFormatter(new TempFormatter());
        set1.setCubicIntensity(0.4f);

        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(2f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);

        set2.setValueFormatter(new TempFormatter());
        set2.setCubicIntensity(0.4f);
        set2.enableDashedLine(10f, 5f, 0f);
        set2.setColor(Color.RED);
        set2.setCircleColor(Color.RED);
        set2.setLineWidth(2f);
        set2.setDrawCircleHole(false);
        set2.setValueTextSize(9f);
        set2.setFillAlpha(65);
        set2.setFillColor(Color.RED);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets
        dataSets.add(set2); // add the datasets

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter (new TempFormatter());
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

        leftAxis.setAxisMaxValue(MashPit.prefGetMax(prefs,TempMode));
        leftAxis.setAxisMinValue(MashPit.prefGetMin(prefs,TempMode));

        mChart.getAxisRight().setEnabled(false);

        if(mpos<0) {
            List<Temperature> temps = getAll(24);
            SimpleDateFormat fmtout = new SimpleDateFormat("HH:mm", Locale.GERMANY);
            if(temps.size()==0)
            {
                temps = getAll(30 * 24);
                fmtout = new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY);
            }

            int i=0;
            for (Temperature temperature : temps) {
                Date df = new Date(temperature.timeStamp * 1000);

                xVals.add(fmtout.format(df));

                if (temperature.Name.equalsIgnoreCase("sensor1")) {
                    yVals1.add(new Entry(round(temperature.Temp, 1), i));
                }
                if (temperature.Name.equalsIgnoreCase("sensor2")) {
                    yVals2.add(new Entry(round(temperature.Temp, 1), i));
                }
                i++;
            }
            lastEntry = i;
            data = new LineData(xVals, dataSets);
        }
        else {
            data = tempdata.getData(mpos);
            lastEntry = data.getXValCount();
        }
        // set data
        mChart.setData(data);
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
        if(mpos<0) {
            EventBus.getDefault().unregister(this);
        }
    }
}
