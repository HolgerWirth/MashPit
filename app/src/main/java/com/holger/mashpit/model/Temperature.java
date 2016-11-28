package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Temperature")
public class Temperature extends Model implements Serializable {
    @Column(name = "timeStamp", index=true)
    public long timeStamp;

    @Column(name = "Temp")
    public float Temp;

    @Column(name = "Name", index=true)
    public String Name;

    @Column(name = "Mode", index=true)
    public String Mode;

    public Temperature() {
        // Notice how super() has been called to perform default initialization
        // of our Model subclass
        super();
    }

    public Temperature(long timeStamp, float Temp, String Name,String Mode) {
        super();
        this.timeStamp = timeStamp;
        this.Temp = Temp;
        this.Name = Name;
        this.Mode = Mode;
    }

    @Override
    public String toString() {
        return "Name: "
                + Name
                + " Temperature: "
                + Temp
                + " Mode: "
                + Mode
                + " "
                + timeStamp;
    }

}
