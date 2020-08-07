package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Charts", id = "clientId")
public class Charts extends Model implements Serializable {
    @Column(name = "id")
    public long id;
    @Column(name = "name",index=true)
    public String name;
    @Column(name = "description",index=true)
    public String description;
    @Column(name = "type")
    public String type;
    @Column(name = "valueY")
    public String valueY;
    @Column(name = "max")
    public int max;
    @Column(name = "min")
    public int min;
    @Column(name = "keep")
    public int keep;
    public int todelete;

    public Charts() {
        // Notice how super() has been called to perform default initialization
        // of our Model subclass
        super();
    }

    public Charts(String name,String desc,String type, String valueY, int max, int min, int keep) {
        super();
        this.name=name;
        this.description=desc;
        this.type=type;
        this.valueY=valueY;
        this.max=max;
        this.min=min;
        this.keep=keep;
    }
}