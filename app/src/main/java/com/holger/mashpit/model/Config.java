package com.holger.mashpit.model;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import java.io.Serializable;

@Table(name = "Config")
public class Config extends Model implements Serializable {
    @Column(name = "name", index = true)
    public String name;
    @Column(name = "MPServer", index = true)
    public String MPServer;
    @Column(name = "type")
    public String type;
    @Column(name = "topic")
    public String topic;
    @Column(name = "active")
    public boolean active;
    @Column(name = "temp")
    public String temp;
    @Column(name = "minmax")
    public boolean minmax;
    @Column(name = "time")
    public String time;
    @Column(name = "hysterese")
    public String hysterese;
    @Column(name = "GPIO")
    public String GPIO;
    @Column(name = "IRid")
    public String IRid;
    @Column(name = "IRcode")
    public String IRcode;


    public Config() {
        super();
    }

    public Config(String name, String MPServer, String type, String topic, boolean active, String temp, boolean minmax, String time,
                  String hysterese, String GPIO, String IRid, String IRcode) {
        super();
        this.name = name;
        this.MPServer = MPServer;
        this.type = type;
        this.topic = topic;
        this.active = active;
        this.temp = temp;
        this.minmax = minmax;
        this.time = time;
        this.hysterese = hysterese;
        this.GPIO = GPIO;
        this.IRid = IRid;
        this.IRcode = IRcode;
    }
}
