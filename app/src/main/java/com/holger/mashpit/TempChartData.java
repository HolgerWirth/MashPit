package com.holger.mashpit;

import android.util.Log;

import com.github.mikephil.charting.data.LineData;

import java.util.ArrayList;

class TempChartData {
    private static TempChartData instance;
    private ArrayList<LineData> glist = new ArrayList<>();
    private ArrayList<String> desc = new ArrayList<>();

    private static final String DEBUG_TAG = "TempChartData";

    // Restrict the constructor from being instantiated
    private TempChartData(){}

    public void setData(LineData chartdata){
        this.glist.add(chartdata);
    }
    void setDesc(String description){ this.desc.add(description); }

    LineData getData(int pos){
        return this.glist.get(pos);
    }
    String getDesc(int pos){
        return this.desc.get(pos);
    }

    public ArrayList<LineData> getData(){
        return this.glist;
    }

    void clearData(){
        this.glist.clear();
        this.desc.clear();
    }

    static synchronized TempChartData getInstance(){
        if(instance==null){
            Log.i(DEBUG_TAG, "instance==null");
            instance=new TempChartData();
        }
        return instance;
    }
}
