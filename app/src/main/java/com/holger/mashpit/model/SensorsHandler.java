package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.PropertyQuery;
import io.objectbox.query.Query;

public class SensorsHandler {
    Box<Sensors> dataBox;

    public SensorsHandler() {
        dataBox = ObjectBox.get().boxFor(Sensors.class);
    }

    public String getSensorAlias(String device, String sensor)
    {
        PropertyQuery query = dataBox.query(Sensors_.server.equal(device)
        .and(Sensors_.sensor.equal(sensor))).build().property(Sensors_.name);
        if(query.count()>0)
        {
            return query.findString();
        }
        return sensor;
    }

    public void deleteAllSensors()
    {
        Query<Sensors> query = dataBox.query().build();
        if(query.count()>0)
        {
            dataBox.remove(query.find());
            dataBox.closeThreadResources();
        }
    }

    public void deleteSensor(String topic)
    {
        Query<Sensors> query = dataBox.query(Sensors_.topic.equal(topic)).build();
        if(query.count()>0)
        {
            dataBox.remove(query.find());
            dataBox.closeThreadResources();
        }
    }

    public void upsertSensor(Sensors sensors)
    {
        Query<Sensors> query = dataBox.query(Sensors_.topic.equal(sensors.topic)).build();
        if(query.count()>0)
        {
            Sensors mySens = query.findFirst();
            assert mySens != null;
            sensors.id = mySens.id;
        }
        dataBox.put(sensors);
    }

    public List<Sensors> getAllSensors(String device)
    {
        Query<Sensors> query = dataBox.query(Sensors_.server.equal(device)).order(Sensors_.sensor).build();
        if(query.count()>0)
        {
            return(query.find());
        }
        return (new ArrayList<>());
    }

    public List<Sensors> getIntervals(String device, String sensor)
    {
        Query<Sensors> query = dataBox.query(Sensors_.server.equal(device)
        .and(Sensors_.sensor.equal(sensor))).order(Sensors_.interval).build();
        if(query.count()>0)
        {
            return(query.find());
        }
        return (new ArrayList<>());
    }

    public List<Sensors> getEvents(String device, String sensor)
    {
        Query<Sensors> query = dataBox.query(Sensors_.server.equal(device)
                .and(Sensors_.sensor.equal(sensor))
                .and(Sensors_.family.equal("EV"))).order(Sensors_.dir).order(Sensors_.family).build();
        if(query.count()>0)
        {
            return(query.find());
        }
        return (new ArrayList<>());
    }

    public boolean checkGPIO(String device,int gpio)
    {
        Query<Sensors> query = dataBox.query(Sensors_.server.equal(device)
                .and(Sensors_.port.equal(gpio))).order(Sensors_.interval).build();
        return query.count() > 0;
    }
}
