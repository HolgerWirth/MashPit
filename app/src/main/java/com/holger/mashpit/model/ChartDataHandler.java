package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class ChartDataHandler {
    Box<ChartData> dataBox;

    public ChartDataHandler() {
        dataBox = ObjectBox.get().boxFor(ChartData.class);
    }

    public void addChartData(List<ChartData> data)
    {
        dataBox.put(data);
    }

    public long getCount()
    {
        return dataBox.count();
    }

    public List<ChartData> queryChartData(String[] topic,long ts) {
        Query<ChartData> query = dataBox.query(ChartData_.topic.oneOf(topic)
                .and(ChartData_.TS.greater(ts)))
                .order(ChartData_.TS)
                .build();
        if(query.count()>0) {
            return (query.find());
        }
        return (new ArrayList<>());
    }

    public long deleteChartData(String[] topic, long ts)
    {
        Query<ChartData> query = dataBox.query(ChartData_.topic.oneOf(topic)
                .and(ChartData_.TS.less(ts)))
                .build();
        query.remove();
        return (query.count());
    }

    public double[] getMaxChartData(String[] topic, long ts)
    {
        double[] minmax = new double[2];
        Query<ChartData> query = dataBox.query(ChartData_.topic.oneOf(topic)
                .and(ChartData_.TS.greater(ts)))
                .build();
        minmax[0]=query.property(ChartData_.value).minDouble();
        minmax[1]=query.property(ChartData_.value).maxDouble();
        return(minmax);
    }

}
