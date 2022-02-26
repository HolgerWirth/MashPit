package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.PropertyQuery;
import io.objectbox.query.Query;

public class ChartParamsHandler {
    Box<ChartParams> dataBox;
    private final String name;
    private List<ChartParams> chartParams;
    List<Integer> xBfactor;

    public ChartParamsHandler(String name) {
        dataBox = ObjectBox.get().boxFor(ChartParams.class);
        chartParams = new ArrayList<>();
        getChartParams(name);
        this.name = name;
    }

    public List<ChartParams> getChartParamsLines(String name,int sort)
    {
        Query<ChartParams> query = dataBox.query(ChartParams_.name.equal(name).and(ChartParams_.sort.equal(sort)))
                .order(ChartParams_.pos).order(ChartParams_.sort).build();
        return(query.find());
    }

    private void getChartParams(String name)
    {
        xBfactor = new ArrayList<>();
        xBfactor.add(24);
        xBfactor.add(168);
        xBfactor.add(720);
        xBfactor.add(1);

        List<ChartParams> tParams;
        chartParams = new ArrayList<>();
        Query<ChartParams> query = dataBox.query(ChartParams_.name.equal(name)).order(ChartParams_.pos).order(ChartParams_.sort).build();
        tParams=query.find();
        if(tParams.size()==0)
        {
            return;
        }

        int sort=tParams.get(0).sort;
        ChartParams mparams;
        mparams = tParams.get(0);
        mparams.topics = new ArrayList<>();
        mparams.XvarDescs = new ArrayList<>();
        mparams.Xvars = new ArrayList<>();
        mparams.Colors = new ArrayList<>();
        for(int i=0;i<tParams.size();i++)
        {
            if(tParams.get(i).sort != sort)
            {
                chartParams.add(mparams);
                mparams = tParams.get(i);
                mparams.topics = new ArrayList<>();
                mparams.XvarDescs = new ArrayList<>();
                mparams.Xvars = new ArrayList<>();
                mparams.Colors = new ArrayList<>();
            }

            mparams.topics.add(tParams.get(i).topic);
            mparams.Xvars.add(tParams.get(i).Xvar);
            mparams.XvarDescs.add(tParams.get(i).XvarDesc);
            mparams.Colors.add(tParams.get(i).color);
            sort=tParams.get(i).sort;
        }
        chartParams.add(mparams);
    }

    public int getXBfactor(int pos)
    {
        return xBfactor.get(pos);
    }

    public int getXBfactorIndex(int factor)
    {
        return Math.max(xBfactor.indexOf(factor), 0);
    }

    public long getXBounds(int pos)
    {
        return(chartParams.get(pos).XBounds * chartParams.get(pos).XBfactor);
    }

    public long getOldestData()
    {
        long max= getXBounds(0);
        for(int i=0; i<chartParams.size();i++)
        {
            if(getXBounds(i)>max) max=getXBounds(i);
        }
        return(max);
    }

    public List<ChartParams> updateParams(String name) {
        getChartParams(name);
        return chartParams;
    }

    public boolean topicExists(String topic,String var)
    {
        for(ChartParams params : chartParams ) {
            if (params.topics.contains(topic)) {
                if (params.Xvars.contains(var)) {
                    return (true);
                }
            }
        }
        return (false);
    }

    public String[] getTopics()
    {
        PropertyQuery query = dataBox.query(ChartParams_.name.equal(name)).build().property(ChartParams_.topic);
        return(query.distinct().findStrings());
    }

    public void saveParamPos(List<ChartParams> mparams)
    {
        for(int i=0;i<mparams.size();i++)
        {
            Query<ChartParams> query = dataBox.query(ChartParams_.name.equal(name).and(ChartParams_.sort.equal(mparams.get(i).sort))).build();
            for(ChartParams yparams: query.find())
            {
                yparams.pos=i;
                dataBox.put(yparams);
            }
        }
    }

    public void saveParams(List<ChartParams> mparams)
    {
        dataBox.put(mparams);
    }

    public void deleteParams(List<Long> del)
    {
        dataBox.removeByIds(del);
    }

    public void deleteParam(int pos) {
        Query<ChartParams> query = dataBox.query(ChartParams_.name.equal(name).and(ChartParams_.sort.equal(chartParams.get(pos).sort))).build();
        dataBox.remove(query.findIds());
        chartParams = new ArrayList<>();
        getChartParams(name);
    }

    public ArrayList<String> getAllXDescs(int pos)
    {
        return(new ArrayList<>(chartParams.get(pos).XvarDescs));
    }

    public int numVarInPos(int pos)
    {
        return(chartParams.get(pos).Xvars.size());
    }

    public int getVarInPos(int pos, String var)
    {
        return(chartParams.get(pos).Xvars.indexOf(var));
    }

    public boolean varExistsInPos(int pos,String var)
    {
        return chartParams.get(pos).Xvars.contains(var);
    }

    public int getColor(int pos,int var)
    {
        return(chartParams.get(pos).Colors.get(var));
    }

    public String getDescription(int pos)
    {
        return(chartParams.get(pos).XDesc);
    }

    public boolean getAutoscale(int pos)
    {
        return(chartParams.get(pos).autoscale);
    }

    public float getMinOffset(int pos)
    {
        return(chartParams.get(pos).minOffset);
    }

    public float getMaxOffset(int pos)
    {
        return(chartParams.get(pos).maxOffset);
    }

    public float getMinValue(int pos)
    {
        return(chartParams.get(pos).minValue);
    }

    public float getMaxValue(int pos)
    {
        return(chartParams.get(pos).maxValue);
    }

    public int getRoundDec(int pos)
    {
        return(chartParams.get(pos).roundDec);
    }

    public int getPositions()
    {
        return chartParams.size();
    }

    public String getFormat(int pos) { return chartParams.get(pos).YFormat;}

    public String getUnit(int pos) { return chartParams.get(pos).YUnit;}

    public List<ChartParams> getParams() { return chartParams; }
}

