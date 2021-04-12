package com.holger.mashpit;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import com.holger.mashpit.model.ChartData;
import com.holger.mashpit.model.ChartData_;
import com.holger.mashpit.tools.ObjectBox;
import com.holger.mashpit.tools.SnackBar;
import com.holger.mashpit.tools.TempFormatter;
import com.holger.mashpit.tools.TimestampFormatter;

import com.holger.share.Constants;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class TempChartActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "TempChartActivity" ;
    private List<ChartData> temps = null;
    private String[] topic;
    private ProgressBar progress;
    Handler handler = new Handler();
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    float tempMin;
    float tempMax;
    ChartDataAdapter cda;
    TempChartData tempdata;
    Box<ChartData> dataBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(DEBUG_TAG, "OnCreate");

        setContentView(R.layout.activity_temp_chart);

        dataBox = ObjectBox.get().boxFor(ChartData.class);

        tempdata = TempChartData.getInstance();
        tempdata.clearData();

        Toolbar toolbar = findViewById(R.id.chart_toolbar);

        Intent intent = getIntent();
        topic = intent.getStringArrayExtra("topics");
        toolbar.setTitle(intent.getStringExtra("title"));
        Log.i(DEBUG_TAG, "Topic: "+topic);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tempMin = 5;
        tempMax = 50;

        progress = findViewById(R.id.progressBar1);
        startLoadingData();
        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_content);

        snb= new SnackBar(coordinatorLayout);
        snb.setmOnClickListener(
                mOnClickListener = v -> {
                    Log.i(DEBUG_TAG, "Reconnect service");
                    Intent startIntent = new Intent(TempChartActivity.this, TemperatureService.class);
                    startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                    startService(startIntent);
                });
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(DEBUG_TAG, "onConfiguratonChanged");
    }

    @Override
    protected void onDestroy() {
        Log.i(DEBUG_TAG, "onDestroy");
        snb.stopEvents();
        super.onDestroy();
    }

/*
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
*/

    public void startLoadingData() {
        // do something long
        Runnable runnable = () -> {
            deleteData();
            temps = queryData();
            generateData();
            progress.post(() -> progress.setProgress(1));

            handler.post(() -> {
                progress.setVisibility(View.GONE);
                ListView lv = findViewById(R.id.chartListView);
                Log.i(DEBUG_TAG, "Creating list");
                cda = new ChartDataAdapter(this,tempdata.getData());
                if (lv != null) {
                    lv.setAdapter(cda);
                    lv.setClickable(true);
                }
            });
        };
        new Thread(runnable).start();
    }

    /*
    private void selectTempChart(int resid)
    {
        Temperature temp = MashPit.TempModes.get(resid);
        Log.i(DEBUG_TAG,"selectTempChart: "+temp.Mode);
        Intent k = new Intent(getApplicationContext(), TempChartActivity.class);
        k.putExtra("MODE", temp.Mode);
        startActivity(k);
        finish();
    }
*/

    private void generateData() {

        int LINES = 3;
        int[] HOURS = new int[LINES];
        HOURS[0]=24;
        HOURS[1]=7*24;
        HOURS[2]=30*24;

        String[] DESC = new String[LINES];
        DESC[0]=getString(R.string.chart_24h);
        DESC[1]=getString(R.string.chart_7d);
        DESC[2]=getString(R.string.chart_30d);

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

        for (ChartData chartData : temps) {
            float entry=round(chartData.value,1);

            if (!(sensors.contains(chartData.var))) {
                Log.i(DEBUG_TAG,"Found var: "+chartData.var);
                sensors.add(chartData.var);
                for(int i=0;i<LINES;i++) {
                    yVals[i].add(new ArrayList<>());
                }
            }
            int sensindex = sensors.indexOf(chartData.var);

            for(int i=0;i<LINES;i++) {
                if (chartData.TS > ts[i]) {
                    yVals[i].get(sensindex).add(new Entry((float)chartData.TS,entry));
                    count[i]++;
                }
            }
        }
        LineDataSet xset;
        for(int k=0;k<LINES;k++) {
            for (int j=0;j<sensors.size();j++) {
                    xset=new LineDataSet(yVals[k].get(j),sensors.get(j));
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

    public List<ChartData> queryData() {
        dataBox = ObjectBox.get().boxFor(ChartData.class);
        QueryBuilder<ChartData> builder = dataBox.query();
        builder.equal(ChartData_.topic, topic[0])
                .greater(ChartData_.TS, getFromTimestamp(30 * 24))
                .order(ChartData_.TS);
        return (builder.build().find());
    }

    private void deleteData()
    {
        dataBox = ObjectBox.get().boxFor(ChartData.class);
        QueryBuilder<ChartData> builder = dataBox.query();
        builder.equal(ChartData_.topic, topic[0])
                .less(ChartData_.TS, getDeleteTimestamp());
        builder.build().remove();
    }

    public static long getFromTimestamp(int hours)
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime - (hours * 60 * 60);
    }

    public long getDeleteTimestamp()
    {
        int deldays = 30;
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
                holder.chart= convertView.findViewById(R.id.chartitem);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setValueFormatter(new TempFormatter());
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//            leftAxis.addLimitLine(ll1);
//            leftAxis.addLimitLine(ll2);

            leftAxis.setAxisMaximum(tempMax);
            leftAxis.setAxisMinimum(tempMin);

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
//                    selectLineChart(TempMode,position);
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

    private void selectLineChart(String linemode, int pos)
    {
        Log.i(DEBUG_TAG,"selectLineChart: "+pos);
        Intent k = new Intent(getApplicationContext(), LineChartActivity.class);
        k.putExtra("POS",pos);
        k.putExtra("MODE", linemode);
        startActivity(k);
    }
}
