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
        Query<Charts> query = dataBox.query(Charts_.name.equal(chart.name)).build();
        if(query.count()>0)
        {
            Charts found = query.findFirst();
            assert found != null;
            chart.id=found.id;
            dataBox.put(chart);
        }
    }

    public void deleteChart(Charts chart) {
        Query<Charts> query = dataBox.query(Charts_.name.equal(chart.name)).build();
        if (query.count() > 0) {
            Charts found = query.findFirst();
            assert found != null;
            chart.id = found.id;
            dataBox.remove(chart);
        }
    }
}
