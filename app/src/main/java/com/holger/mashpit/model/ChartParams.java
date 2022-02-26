package com.holger.mashpit.model;

import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class ChartParams {
    @Id
    public long id;
    public int pos;
    public String name;
    public String comment;
    public int sort;
    public boolean autoscale;
    public float minValue;
    public float maxValue;
    public float maxOffset;
    public float minOffset;
    public String topic;
    public String Xvar;
    public String XvarDesc;
    public int roundDec;
    public long XBounds;
    public int  XBfactor;
    public String XDesc;
    public int color;
    public String YFormat;
    public String YUnit;

    @Transient
    public List<String> topics;
    @Transient
    public List<String> Xvars;
    @Transient
    public List<String> XvarDescs;
    @Transient
    public List<Integer> Colors;
    @Transient
    public boolean error;
}
