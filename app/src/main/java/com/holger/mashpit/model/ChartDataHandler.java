package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.PropertyQuery;
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

    public long deleteChartData(String topic, long ts)
    {
        Query<ChartData> query = dataBox.query(ChartData_.topic.equal(topic)
                .and(ChartData_.TS.less(ts)))
                .build();
        return (query.remove());
    }

    public long deleteChartData(String topic)
    {
        Query<ChartData> query = dataBox.query(ChartData_.topic.equal(topic))
                .build();
        return (query.remove());
    }


    public String[] getChartVars(String sensor)
    {
        PropertyQuery query = dataBox.query(ChartData_.sensor.equal(sensor)).build().property(ChartData_.var);
        return(query.distinct().findStrings());
    }
}
