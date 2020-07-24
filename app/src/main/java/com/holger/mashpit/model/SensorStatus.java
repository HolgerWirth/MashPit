package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "SensorStatus")
public class SensorStatus extends Model implements Serializable {
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
    @Column(name = "TS")
    public long TS;
    @Column(name = "system")
    public String system;
    @Column(name = "version")
    public String version;
    @Column(name = "IP")
    public String IP;

    public SensorStatus() {
        // Notice how super() has been called to perform default initialization
        // of our Model subclass
        super();
    }

    public SensorStatus(String server, String sensor, boolean active, String alias, String type,long TS,String system,String version,String IP) {
        super();
        this.server=server;
        this.sensor=sensor;
        this.active=active;
        this.alias=alias;
        this.type=type;
        this.TS=TS;
        this.system=system;
        this.version=version;
        this.IP=IP;
    }
}