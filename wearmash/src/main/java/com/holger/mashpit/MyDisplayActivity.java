package com.holger.mashpit;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.holger.share.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MyDisplayActivity extends WearableActivity implements DataClient.OnDataChangedListener {

    private PieChart mChart;
    float cAngle = 270f;

    private static final String DEBUG_TAG = "MyDisplayActivity";
    private static final int MSG_UPDATE_SCREEN = 0;

    private final Handler mActiveModeUpdateHandler = new ActiveModeUpdateHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Log.i(DEBUG_TAG, "OnCreate()");

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

            setAmbientEnabled();
        }
    }

    protected void initPieDataAmbient() {
        Log.d(DEBUG_TAG, "initPieDataAmbient()");

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            entries.add(new PieEntry(360, ""));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);

        dataSet.setColor(Color.BLACK);
        dataSet.setHighlightEnabled(false);
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        mChart.setBackgroundColor(Color.BLACK);
        mChart.setHoleColor(Color.BLACK);
        mChart.setTransparentCircleColor(Color.DKGRAY);
        mChart.setCenterTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.setDrawEntryLabels(false);
        mChart.highlightValue(0, 0);
        mChart.invalidate();
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

        mChart.setBackgroundColor(Color.BLUE);
        mChart.setHoleColor(Color.WHITE);
//        mChart.setTransparentCircleColor(Color.BLUE);
        mChart.setCenterTextColor(Color.BLACK);

        mChart.setData(data);
        mChart.setDrawEntryLabels(false);
        mChart.highlightValue(0, 0);
        mChart.invalidate();
    }

    protected void updateTemperature(String temp)
    {
        mChart.setCenterText(temp);
        mChart.invalidate();
    }

        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
            Log.d(DEBUG_TAG, "onEnterAmbient()");

            /* Clears Handler queue (only needed for updates in active mode). */
            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            initPieDataAmbient();
        }

        @Override
        public void onUpdateAmbient() {
            Log.d(DEBUG_TAG, "onUpdateAmbient()");
            super.onUpdateAmbient();
        }
            /** Restores the UI to active (non-ambient) mode. */
            @Override
            public void onExitAmbient () {
                super.onExitAmbient();
                Log.d(DEBUG_TAG, "onExitAmbient()");
                initPieData();
            }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume()");
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause()");
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(DEBUG_TAG, "onDataChanged: " + dataEventBuffer);
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (Constants.WEAR.RUN_UPDATE_NOTIFICATION.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String message = dataMapItem.getDataMap().getString(Constants.WEAR.KEY_CONTENT);
                    Log.d(DEBUG_TAG, "Wear activity received message: " + message);
                    updateTemperature(dataMapItem.getDataMap().getString(Constants.WEAR.KEY_CONTENT));

                } else {
                    Log.d(DEBUG_TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(DEBUG_TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.d(DEBUG_TAG, "Unknown data event Type = " + event.getType());
            }
        }
    }

    /** Handler separated into static class to avoid memory leaks. */
    private static class ActiveModeUpdateHandler extends Handler {
        private final WeakReference<MyDisplayActivity> mMainActivityWeakReference;

        ActiveModeUpdateHandler(MyDisplayActivity reference) {
            mMainActivityWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message message) {
            MyDisplayActivity mainActivity = mMainActivityWeakReference.get();
            Log.d(DEBUG_TAG, "handleMessage()");

            if (mainActivity != null) {
                switch (message.what) {
                    case MSG_UPDATE_SCREEN:
                        break;
                }
            }
        }
    }
}
