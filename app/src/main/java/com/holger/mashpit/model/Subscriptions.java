package com.holger.mashpit.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
    public class Subscriptions {
    @Id
    public long id;

    public String topic;
    public String action;
    public String name;
    public String server;
    public String sensor;
    public int interval;
    public int durable;
    public boolean deleted;
    public int deldays;
    @Transient
    public String aliasSensor;
    @Transient
    public String aliasServer;
}
