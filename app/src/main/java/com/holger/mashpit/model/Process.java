package com.holger.mashpit.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Process")
public class Process extends Model implements Serializable {
    @Column(name = "myJSONString")
    public String myJSONString;

    public Process() {
        // Notice how super() has been called to perform default initialization
        // of our Model subclass
        super();
    }

    public Process(String myJSONString) {
        super();
        this.myJSONString = myJSONString;
    }

    @Override
    public String toString() {
        return "myJSONString: "
                + myJSONString;
    }

}
