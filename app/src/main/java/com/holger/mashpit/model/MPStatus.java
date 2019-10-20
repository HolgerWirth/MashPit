package com.holger.mashpit.model;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import java.io.Serializable;

@Table(name = "MPStatus")
public class MPStatus extends Model implements Serializable {
    @Column(name = "topic", index=true)
    public String topic;
    @Column(name = "MPServer",index=true)
    public String MPServer;
    @Column(name = "processes")
    public String processes;
    @Column(name = "active")
    public String active;
    @Column(name = "PID")
    public String PID;
    @Column(name = "Type")
    public String Type;

    public MPStatus() {
        // Notice how super() has been called to perform default initialization
        // of our Model subclass
        super();
    }

    public MPStatus(String topic, String MPServer, String processes,String active, String PID, String Type) {
        super();
        this.topic=topic;
        this.MPServer=MPServer;
        this.processes=processes;
        this.active=active;
        this.PID=PID;
        this.Type=Type;
    }

    @Override
    public String toString() {
        return "topic: "
                + topic
                + " MPServer: "
                + MPServer
                + " processes: "
                + processes
                + "active: "
                + active
                + " PID: "
                + PID
                + "Type: "
                + Type;
    }
}