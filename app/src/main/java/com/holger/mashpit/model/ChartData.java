package com.holger.mashpit.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class ChartData {
    @Id
    public long id;

    public String topic;
    public String sensor;
    public long TS;
    public String var;
    public float value;
}
