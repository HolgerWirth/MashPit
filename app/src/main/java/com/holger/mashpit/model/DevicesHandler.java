package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.PropertyQuery;
import io.objectbox.query.Query;

public class DevicesHandler {
    Box<Devices> dataBox;

    public DevicesHandler() {
        dataBox = ObjectBox.get().boxFor(Devices.class);
    }

    public List<Devices> getDeviceStatus()
    {
        Query<Devices> query = dataBox.query().order(Devices_.device).build();
        if(query.count()>0)
        {
            return(query.find());
        }
        return(new ArrayList<>());
    }

    public void deleteDevice(String device)
    {
        Query<Devices> query = dataBox.query(Devices_.device.equal(device)).build();
        dataBox.remove(query.find());
    }

    public void updateAlias(String server,String alias)
    {
        try (Query<Devices> query = dataBox.query(Devices_.device.equal(server)
                .and(Devices_.active.equal(true))).build()) {
            if (query.count() > 0) {
                Devices myDev = query.findFirst();
                assert myDev != null;
                myDev.alias = alias;
                dataBox.put(myDev);
            }
        }
    }

    public void upsertDevice(Devices dev)
    {
        Query<Devices> query = dataBox.query(Devices_.device.equal(dev.device)).build();
        if(query.count()>0)
        {
            Devices myDev = query.findFirst();
            assert myDev != null;
            dev.id = myDev.id;
        }
        dataBox.put(dev);
    }

    public String getDeviceAlias(String device) {
        PropertyQuery query = dataBox.query(Devices_.device.equal(device)).build().property(Devices_.alias);
        if(!query.findString().isEmpty())
        {
            return query.findString();
        }
        return device;
    }
}
