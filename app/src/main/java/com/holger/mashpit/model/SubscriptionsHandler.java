package com.holger.mashpit.model;

import com.holger.mashpit.tools.ObjectBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.PropertyQuery;
import io.objectbox.query.Query;

public class SubscriptionsHandler {

    Box<Subscriptions> dataBox;
    DevicesHandler devicesHandler = new DevicesHandler();
    SensorsHandler sensorsHandler = new SensorsHandler();

    public SubscriptionsHandler() {
        dataBox = ObjectBox.get().boxFor(Subscriptions.class);
    }

    public void deleteSubsription(String topic)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.topic.equal(topic)
                .and(Subscriptions_.deleted.equal(true))).build();
        query.remove();
    }

    public List<String> getDeletedSubscriptions()
    {
        PropertyQuery query = dataBox.query(Subscriptions_.deleted.equal(true)).build().property(Subscriptions_.topic).distinct().distinct();
        if(query.count()>0)
        {
            return(new ArrayList<>(Arrays.asList(query.findStrings())));
        }
        return(new ArrayList<>());
    }

    public void setDeletedSubscriptions(long id)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.id.equal(id)).build();
        Subscriptions sub=query.findFirst();
        assert sub != null;
        sub.deleted=true;
        dataBox.put(sub);
    }

    public void setDeletedSubscriptions(String action, String name)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.action.equal(action)
        .and(Subscriptions_.name.equal(name))).build();
        if(query.count()>0) {
            Subscriptions sub = query.findFirst();
            assert sub != null;
            sub.deleted = true;
            dataBox.put(sub);
        }
    }

    public List<Subscriptions> getActiveSubscriptions(String action, String name)
    {
        List<Subscriptions> subs;
        Query<Subscriptions> query = dataBox.query(Subscriptions_.deleted.equal(false)
                .and(Subscriptions_.action.equal(action))
                .and(Subscriptions_.name.equal(name)))
                .order(Subscriptions_.server).build();

        subs = query.find();
        for(int i=0; i<subs.size(); i++)
        {
            subs.get(i).aliasServer = devicesHandler.getDeviceAlias(subs.get(i).server);
            subs.get(i).aliasSensor = sensorsHandler.getSensorAlias(subs.get(i).server, subs.get(i).sensor);
        }
        return (subs);
    }

    public List<String> getAllSubscription(boolean durable) {
        PropertyQuery query = dataBox.query(Subscriptions_.deleted.equal(false)
                .and(Subscriptions_.durable.equal(durable ? 1 : 0)))
                        .build().property(Subscriptions_.topic).distinct();
        if(query.count()>0) {
            return (new ArrayList<>(Arrays.asList(query.findStrings())));
        }
        return (new ArrayList<>());
    }

    public boolean checkSubscription(String topic,String action)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.topic.equal(topic)
                .and(Subscriptions_.action.equal(action))
                .and(Subscriptions_.deleted.equal(false))).build();
        return query.count() > 0;
    }

    public int getMaxDelDays(String topic, String action)
    {
        return(dataBox.query(Subscriptions_.deleted.equal(false)
                .and(Subscriptions_.topic.equal(topic))
                .and(Subscriptions_.action.equal(action))).build().property(Subscriptions_.deldays).distinct()).findInt();
    }

    public int getMaxDelDays(String name)
    {
        return(dataBox.query(Subscriptions_.deleted.equal(false)
                .and(Subscriptions_.name.equal(name))).build().property(Subscriptions_.deldays).distinct().findInt());
    }

    public void setMaxDelDays(String name,int deldays)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.name.equal(name)).build();
        if(query.count()>0) {
            for(Subscriptions sub : query.find())
            {
                sub.deldays=deldays;
                dataBox.put(sub);
            }
        }
    }

    public void addSubscription(Subscriptions sub)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.topic.equal(sub.topic)
                .and(Subscriptions_.action.equal(sub.action))
                .and(Subscriptions_.name.equal(sub.name))).build();
        if(query.count()>0)
        {
            Subscriptions found = query.findFirst();
            assert found != null;
            found.deleted=false;
            found.deldays=sub.deldays;
            dataBox.put(found);
        }
        else
        {
            dataBox.put(sub);
        }
        dataBox.closeThreadResources();
    }

    public void updateSubscription(Subscriptions sub)
    {
        dataBox.put(sub);
    }

    public boolean checkSubscription(String topic)
    {
        Query<Subscriptions> query = dataBox.query(Subscriptions_.topic.equal(topic)
                .and(Subscriptions_.deleted.equal(false))).build();
        return query.count() > 0;
    }

    public List<String> getRetainedSubscription()
    {
        List<String> retained = new ArrayList<>();
        retained.add("/SE/+/conf/#");
        retained.add("/SE/+/status");
        return(retained);
    }

    public void create()
    {
        Query<Subscriptions> query = dataBox.query().build();
        query.remove();
    }
}
