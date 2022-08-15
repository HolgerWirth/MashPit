package com.holger.mashpit;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.holger.mashpit.model.ChartDataHandler;
import com.holger.mashpit.model.ChartParamsHandler;
import com.holger.mashpit.model.SubscriptionsHandler;
import com.holger.mashpit.tools.SnackBar;
import com.holger.mashpit.tools.TempFormatter;
import com.holger.mashpit.tools.TimestampFormatter;

import com.holger.share.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TempChartActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "TempChartActivity" ;
    private List<ChartData> temps = null;
    private ProgressBar progress;
    Handler handler = new Handler();
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    float tempMin;
    float tempMax;
    ChartDataAdapter cda;
    TempChartData tempdata;
    ChartDataHandler chartDataHandler;
    ChartParamsHandler chartParamsHandler;
    SubscriptionsHandler subHandler;
    String name;
    int chartLinesExist=0;
    View chartView;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(DEBUG_TAG, "OnCreate");

        setContentView(R.layout.activity_temp_chart);
        chartView=findViewById(R.id.chartLayout);

        tempdata = TempChartData.getInstance();
        tempdata.clearData();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        Toolbar toolbar = findViewById(R.id.chart_toolbar);
        toolbar.setTitle(intent.getStringExtra("title"));
        setSupportActionBar(toolbar);

        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_content);
        snb = new SnackBar(coordinatorLayout);
        snb.setmOnClickListener(
                mOnClickListener = v -> {
                    Log.i(DEBUG_TAG, "Reconnect service");
                    Intent startIntent = new Intent(TempChartActivity.this, TemperatureService.class);
                    startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                    startService(startIntent);
                });

        myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 2) {
                        Log.i(DEBUG_TAG, "Change received");
                        recreate();
                    }
                });

        chartDataHandler = new ChartDataHandler();
        chartParamsHandler = new ChartParamsHandler(name);
        chartLinesExist = chartParamsHandler.getTopics().length;
        if(chartLinesExist==0)
        {
            chartView.setEnabled(false);
            chartView.setVisibility(View.GONE);
            Log.i(DEBUG_TAG, "No chart lines exist!");
        }
        else {
            subHandler = new SubscriptionsHandler();

            tempMin = 0;
            tempMax = 0;

            progress = findViewById(R.id.progressBar1);
            startLoadingData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        MenuItem chartParamsSetting = menu.findItem(R.id.action_chartParamsSetting);
        if(chartLinesExist==0) {
            chartParamsSetting.setEnabled(true);
            chartParamsSetting.getIcon().setAlpha(130);
        }
        else
        {
            chartParamsSetting.setEnabled(true);
            chartParamsSetting.getIcon().setAlpha(255);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i(DEBUG_TAG, "Settings selected");
        if(item.getItemId()==R.id.action_chartParamsSetting) {
            Intent intent = new Intent(getApplicationContext(), ChartParamsListActivity.class);
            intent.putExtra("NAME", name);
            myActivityResultLauncher.launch(intent);
        }
        else
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
            String[] topic = chartParamsHandler.getTopics();
            for(String mtopic : topic) {
                long delData = chartDataHandler.deleteChartData(mtopic, getDeleteTimestamp(mtopic));
                Log.i(DEBUG_TAG, mtopic + " deleted chart data: " + delData);
            }
            temps=chartDataHandler.queryChartData(topic,getFromTimestamp(chartParamsHandler.getOldestData()));
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
    private void generateData() {
        int LINES = chartParamsHandler.getPositions();
        @SuppressWarnings({"unchecked"})
        List<List<Entry>>[] yVals = new ArrayList[LINES];
        @SuppressWarnings({"unchecked"})
        ArrayList<ILineDataSet>[]  set = new ArrayList[LINES];
        @SuppressWarnings({"unchecked"})
        ArrayList<String>[] Xdescs = new ArrayList[LINES];

//        int[] count = new int[LINES];
        long[] ts = new long[LINES];

        for(int i=0;i<LINES;i++) {
            yVals[i] = new ArrayList<>();
            for(int t=0;t<chartParamsHandler.numVarInPos(i);t++) {
                yVals[i].add(new ArrayList<>());
            }
            Xdescs[i] = new ArrayList<>();
            Xdescs[i].addAll(chartParamsHandler.getAllXDescs(i));
            set[i] = new ArrayList<>();
//            count[i]=0;
            ts[i]=getFromTimestamp(chartParamsHandler.getXBounds(i));
        }

        for (ChartData chartData : temps) {
            boolean varexists=chartParamsHandler.topicExists(chartData.topic,chartData.var);

            if(varexists) {
                for (int i = 0; i < LINES; i++) {
                    if (chartData.TS > ts[i]) {
                        if(chartParamsHandler.varExistsInPos(i,chartData.var)) {
                            float entry=round(chartData.value,chartParamsHandler.getRoundDec(i));
                            yVals[i].get(chartParamsHandler.getVarInPos(i,chartData.var)).add(new Entry((float) chartData.TS, entry));
//                            count[i]++;
                        }
                    }
                }
            }
        }

        LineDataSet xset;
        for (int k = 0; k < LINES; k++) {
            for (int j = 0; j < Xdescs[k].size(); j++) {
                xset = new LineDataSet(yVals[k].get(j), Xdescs[k].get(j));
                xset.setValueFormatter(new TempFormatter(chartParamsHandler.getFormat(k),chartParamsHandler.getUnit(k)));
                xset.setValueTextSize(9f);
                xset.setCubicIntensity(0.4f);
                xset.setLineWidth(2f);
                xset.setDrawCircleHole(false);
                xset.setFillAlpha(65);
                int color = chartParamsHandler.getColor(k,j);
                xset.setColor(color);
                xset.setCircleColor(color);
                xset.setFillColor(color);

                set[k].add(xset);
            }
            tempdata.setData(new LineData(set[k]));
            tempdata.setDesc(chartParamsHandler.getDescription(k));
        }
    }

    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, RoundingMode.HALF_UP).floatValue();
    }

    public static long getFromTimestamp(long hours)
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime - (hours * 60 * 60);
    }

    public long getDeleteTimestamp(String mtopic)
    {
        long days = subHandler.getMaxDelDays(mtopic,"Chart");
        if(days==0)
        {
            return(0);
        }
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime - (days * 24 * 60 * 60);
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
            leftAxis.setValueFormatter(new TempFormatter(chartParamsHandler.getFormat(position),chartParamsHandler.getUnit(position)));
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

// set the formatter
            holder.chart.getAxisRight().setEnabled(false);

            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setValueFormatter(new TimestampFormatter());
            holder.chart.setData(data);

            holder.chart.invalidate();
            holder.chart.animateXY(1000, 2000);

            Log.i(DEBUG_TAG, "Pos: "+position+" Desc: "+tempdata.getDesc(position));
            Description desc = new Description();
            desc.setText(tempdata.getDesc(position));
            holder.chart.setDescription(desc);

            if(chartParamsHandler.getAutoscale(position)) {
                holder.chart.setAutoScaleMinMaxEnabled(chartParamsHandler.getAutoscale(position));
            }
            else
            {
                leftAxis.setAxisMinimum(chartParamsHandler.getMinValue(position) - chartParamsHandler.getMinOffset(position));
                leftAxis.setAxisMaximum(chartParamsHandler.getMaxValue(position) + chartParamsHandler.getMaxOffset(position));
            }

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
}
