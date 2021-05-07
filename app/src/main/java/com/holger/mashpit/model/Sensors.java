package com.holger.mashpit.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Sensors {
    @Id
    public long id;

    public String topic;
    public String family;
    public String dir;
    public String server;
    public String sensor;
    public String event;
    public boolean active;
    public String alias;
    public String type;
    public String name;
    public int interval;
    public String reg;
    public int port;
    public int hyst;
    public int sda;
    public int scl;
    public int alt;
    public String address;
    public String params;
}