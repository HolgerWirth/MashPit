package com.holger.mashpit.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Devices {
    @Id
    public long id;

    public String device;
    public String sensor;
    public boolean active;
    public String alias;
    public long TS;
    public String system;
    public String version;
    public String IP;
    public String wifi;
}
