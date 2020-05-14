package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Subscriptions", id = "clientId")
public class Subscriptions extends Model implements Serializable {
    @Column(name = "id")
    public long id;
    @Column(name="action", index = true)
    public String action;
    @Column(name = "server", index = true)
    public String server;
    @Column(name = "sensor", index = true)
    public String sensor;
    @Column(name = "interval")
    public int interval;
    @Column(name = "durable")
    public int durable;

    public Subscriptions() {
        super();
    }

    public Subscriptions(String action,String server, String sensor, int interval, int durable)
    {
        this.action = action;
        this.server = server;
        this.sensor = sensor;
        this.interval = interval;
        this.durable = durable;
    }
}
