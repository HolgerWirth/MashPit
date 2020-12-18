package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Sensors")
public class Sensors extends Model implements Serializable {
    @Column(name = "topic",index=true)
    public String topic;
    @Column(name = "family",index=true)
    public String family;
    @Column(name = "dir",index=true)
    public String dir;
    @Column(name = "server",index=true)
    public String server;
    @Column(name = "sensor",index=true)
    public String sensor;
    @Column(name = "active")
    public boolean active;
    @Column(name = "alias")
    public String alias;
    @Column(name = "type")
    public String type;
    @Column(name = "name")
    public String name;
    @Column(name = "interval")
    public int interval;
    @Column(name = "reg")
    public String reg;
    @Column(name = "port")
    public int port;
    @Column(name = "hyst")
    public int hyst;
    @Column(name = "sda")
    public int sda;
    @Column(name = "scl")
    public int scl;
    @Column(name = "alt")
    public int alt;
    @Column(name = "address")
    public String address;

    public Sensors() {
        // Notice how super() has been called to perform default initialization
        // of our Model subclass
        super();
    }

    public Sensors(String topic, String family, String dir, String server, String sensor, boolean active, String alias, String type, String name,int interval, String reg, int port, int hyst, int sda,int scl,int alt,String address) {
        super();
        this.topic = topic;
        this.family=family;
        this.dir=dir;
        this.server=server;
        this.sensor=sensor;
        this.active=active;
        this.alias=alias;
        this.type=type;
        this.name=name;
        this.interval=interval;
        this.reg=reg;
        this.port=port;
        this.hyst=hyst;
        this.sda = sda;
        this.scl = scl;
        this.alt = alt;
        this.address=address;
    }
}