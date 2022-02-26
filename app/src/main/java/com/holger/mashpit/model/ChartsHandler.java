package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class ChartsHandler {
    Box<Charts> dataBox;

    public ChartsHandler() {
        dataBox = ObjectBox.get().boxFor(Charts.class);
    }

    public List<Charts> getallCharts()
    {
        Query<Charts> query = dataBox.query().order(Charts_.description).build();
        if(query.count()>0)
        {
            return(query.find());
        }
        return(new ArrayList<>());
    }

    public void addChart(Charts chart)
    {
        dataBox.put(chart);
    }

    public void updateChart(Charts chart)
    {
        dataBox.put(chart);
    }

    public void deleteChart(Charts chart) {
        dataBox.remove(chart);
    }
}
