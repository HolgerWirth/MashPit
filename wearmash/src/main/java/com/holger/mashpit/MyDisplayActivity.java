package com.holger.mashpit;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.wear.ambient.AmbientModeSupport;

import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.holger.share.Constants;

import java.util.ArrayList;
import java.util.List;

public class MyDisplayActivity extends AppCompatActivity implements DataClient.OnDataChangedListener, AmbientModeSupport.AmbientCallbackProvider {
    float cAngle = 270f;

    private static final String DEBUG_TAG = "MyDisplayActivity";

    List<View> pieCharts = new ArrayList<>();
    WearTempPagerAdapter pagerAdapter;
    boolean xinit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Log.i(DEBUG_TAG, "OnCreate()");

        ViewPager pager = findViewById(R.id.pagerview);
        pagerAdapter = new WearTempPagerAdapter(pieCharts);
        pager.setAdapter(pagerAdapter);

        PieChart mChart=createPiePage("--");
        mChart.invalidate();

        if(pieCharts.isEmpty()) {
            PieChart pieChart = createPiePage("--");
            pieChart.setCenterText("--");
            pieCharts.add(pieChart);
            pagerAdapter.notifyDataSetChanged();
            xinit = true;
        }

        AmbientModeSupport.attach(this);
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

    protected void initPieDataAmbient() {
        Log.d(DEBUG_TAG, "initPieDataAmbient()");

        if(pieCharts.size()==0) return;

        PieChart mChart = (PieChart) pagerAdapter.getCurrentView();
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
        mChart.setCenterTextColor(Color.GRAY);

        mChart.setData(data);
        mChart.setDrawEntryLabels(false);
        mChart.highlightValue(0, 0);
        mChart.invalidate();
    }

    protected void initPieData() {
        if(pieCharts.size()==0) return;

        PieChart mChart = (PieChart) pagerAdapter.getCurrentView();
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

    private void sendData() {
        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.WEAR.RUN_UPDATE_NOTIFICATION);
        dataMap.getDataMap().putString(Constants.WEAR.KEY_TITLE, "Temperature");
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(DEBUG_TAG, "Sending message was successful: " + dataItem);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(DEBUG_TAG, "Sending message failed: " + e);
                    }
                });
    }

    protected void updateTemperature(String sensor,String temp) {
        View pieChart;
        boolean found = false;

        if(xinit)
        {
            pieCharts.remove(0);
            Log.i(DEBUG_TAG, "updateTemperature: xinit = true");
            xinit=false;
        }

        for (int i = 0; i < pieCharts.size(); i++) {
            pieChart = pieCharts.get(i);
            if (sensor.contentEquals(pieChart.getContentDescription())) {
                Log.i(DEBUG_TAG, "updateTemperature: " + pieChart.getContentDescription() + " found!");
                ((PieChart) pieChart).setCenterText(temp);
                pagerAdapter.notifyDataSetChanged();
                pagerAdapter.updatePie(i);
                found = true;
                break;
            }
        }
        if (!found) {
            pieChart = createPiePage(sensor);
            ((PieChart) pieChart).setCenterText(temp);
            pieCharts.add(pieChart);
            pagerAdapter.notifyDataSetChanged();
        }
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            // Handle entering ambient mode
            Log.d(DEBUG_TAG, "onEnterAmbient()");

            /* Clears Handler queue (only needed for updates in active mode). */
            initPieDataAmbient();
            sendData();
        }

        @Override
        public void onExitAmbient() {
            // Handle exiting ambient mode
            super.onExitAmbient();
            Log.d(DEBUG_TAG, "onExitAmbient()");
            initPieData();
        }

        @Override
        public void onUpdateAmbient() {
            // Update the content
            Log.d(DEBUG_TAG, "onUpdateAmbient()");
            super.onUpdateAmbient();
            sendData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume()");
        Wearable.getDataClient(this).addListener(this);
        sendData();
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
                    String sensor = dataMapItem.getDataMap().getString(Constants.WEAR.KEY_SENSOR);
                    Log.d(DEBUG_TAG, "Wear activity received message: " + sensor+"/"+message);
                    updateTemperature(sensor,message);

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

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }
}
