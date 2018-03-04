package com.holger.mashpit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import com.holger.share.Constants;
import java.util.ArrayList;
import java.util.List;

public class MyDisplayActivity extends Activity implements DataClient.OnDataChangedListener {

    private PieChart mChart;
    float cAngle = 270f;
    GoogleApiClient mGoogleApiClient;

    private static final String DEBUG_TAG = "MyDisplayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Log.i(DEBUG_TAG, "OnCreate()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(DEBUG_TAG, "Connected");
                        Wearable.DataApi.addListener(mGoogleApiClient, MyDisplayActivity.this);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                })
                .build();
        mGoogleApiClient.connect();

        mChart = findViewById(R.id.wearchart1);

        if (mChart != null) {
            mChart.setMaxAngle(360);
            mChart.setHoleRadius(60f);

            mChart.setTransparentCircleRadius(97f);

            mChart.setDrawCenterText(true);
            mChart.setDrawHoleEnabled(true);

            mChart.setRotationAngle(cAngle);
            // enable rotation of the chart by touch
            mChart.setRotationEnabled(false);
//            mChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Semibold.ttf"));

            mChart.setTouchEnabled(false);

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

            Legend l = mChart.getLegend();
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(5f);

            l.setEnabled(false);

            mChart.setCenterText("--");
            mChart.setCenterTextSize(20);

            initPieData();

            mChart.animateXY(1500, 1500);
            mChart.spin(2000, 0, cAngle, Easing.EasingOption.EaseInOutCirc);
        }
    }

    protected void initPieData() {
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            entries.add(new PieEntry(360, ""));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);

        dataSet.setColor(Color.BLUE);
        dataSet.setHighlightEnabled(true);
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        mChart.setData(data);
        mChart.setDrawEntryLabels(false);
        mChart.highlightValue(0, 0);
        mChart.invalidate();

//        updateTemperature("20.233°");
    }

    protected void updateTemperature(String temp)
    {
        mChart.setCenterText(temp);
        mChart.setCenterTextSize(30);
        mChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume()");
        Wearable.DataApi.addListener(mGoogleApiClient, MyDisplayActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause()");
        Wearable.DataApi.removeListener(mGoogleApiClient, MyDisplayActivity.this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(DEBUG_TAG, "onDataChanged()");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(Constants.WEAR.RUN_UPDATE_NOTIFICATION) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.d(DEBUG_TAG, "DataItem changed: " + event.getDataItem().getUri());
                    updateTemperature(dataMap.getString(Constants.WEAR.KEY_CONTENT));
//                    Toast toast = Toast.makeText(getApplicationContext(), dataMap.getString(Constants.WEAR.KEY_CONTENT), Toast.LENGTH_SHORT);
//                    toast.show();
                }
            }
        }
    }
}
