package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Subscriptions")
public class Subscriptions extends Model implements Serializable {
    @Column(name = "topic", index = true)
    public String topic;
    @Column(name="action", index = true)
    public String action;
    @Column(name="name", index = true)
    public String name;
    @Column(name = "server", index = true)
    public String server;
    @Column(name = "sensor", index = true)
    public String sensor;
    @Column(name = "interval")
    public int interval;
    @Column(name = "durable")
    public int durable;
    @Column(name = "deleted")
    public boolean deleted;

    public String aliasServer;
    public String aliasSensor;

    public Subscriptions() {
        super();
    }

    public Subscriptions(String topic, String action,String name,String server, String sensor, int interval, int durable, boolean deleted)
    {
        this.topic = topic;
        this.action = action;
        this.name = name;
        this.server = server;
        this.sensor = sensor;
        this.interval = interval;
        this.durable = durable;
        this.deleted = deleted;
    }
}
