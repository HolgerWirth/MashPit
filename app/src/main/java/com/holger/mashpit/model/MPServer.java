package com.holger.mashpit.model;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import java.io.Serializable;

@Table(name = "MPServer")
public class MPServer extends Model implements Serializable {
    @Column(name = "name", index = true)
    public String name;
    @Column(name = "MPServer", index = true)
    public String MPServer;
    @Column(name = "alias", index = true)
    public String alias;
    @Column(name = "TS")
    public long TS;

    public MPServer() {
        super();
    }

    public MPServer(String name, String MPServer, String alias,long TS)
    {
        this.name=name;
        this.MPServer=MPServer;
        this.alias=alias;
        this.TS=TS;
    }
}
