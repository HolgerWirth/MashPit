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
        Query<Subscriptions> query = dataBox.query(Subscriptions_.deleted.equal(false)
                .and(Subscriptions_.action.equal(action))
                .and(Subscriptions_.name.equal(name)))
                .order(Subscriptions_.server).build();
        return (query.find());
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
/*
        List<Subscriptions> mysubs = new ArrayList<>();
        Subscriptions subs;
        subs= new Subscriptions();
        subs.action="Pager";
        subs.deleted=false;
        subs.durable=0;
        subs.interval=500;
        subs.name="";
        subs.sensor="bme280-76";
        subs.server="ESP-58B6ED";
        subs.topic="/SE/ESP-58B6ED/temp/bme280-76/500";
        mysubs.add(subs);

        subs = new Subscriptions();
        subs.action="Pager";
        subs.deleted=false;
        subs.durable=0;
        subs.interval=400;
        subs.name="";
        subs.sensor="bme280-76";
        subs.server="ESP-862F92";
        subs.topic="/SE/ESP-862F92/temp/bme280-76/400";
        mysubs.add(subs);

        subs = new Subscriptions();
        subs.action="Service";
        subs.deleted=false;
        subs.durable=0;
        subs.interval=600;
        subs.name="";
        subs.sensor="28-aa085a401401";
        subs.server="ESP-E7BEF1";
        subs.topic="/SE/ESP-E7BEF1/temp/28-aa085a401401/600";
        mysubs.add(subs);

        subs = new Subscriptions();
        subs.action="Chart";
        subs.deleted=false;
        subs.durable=1;
        subs.interval=500;
        subs.name="Test1";
        subs.sensor="bme280-76";
        subs.server="ESP-58B6ED";
        subs.topic="/SE/ESP-58B6ED/temp/bme280-76/500";
        mysubs.add(subs);

        subs = new Subscriptions();
        subs.action="Chart";
        subs.deleted=false;
        subs.durable=1;
        subs.interval=600;
        subs.name="Temp";
        subs.sensor="28-ff9a13631402";
        subs.server="ESP-862F92";
        subs.topic="/SE/ESP-862F92/temp/28-ff9a13631402/600";
        mysubs.add(subs);

        subs = new Subscriptions();
        subs.action="Chart";
        subs.deleted=false;
        subs.durable=1;
        subs.interval=600;
        subs.name="Temp";
        subs.sensor="28-aa085a401401";
        subs.server="ESP-E7BEF1";
        subs.topic="/SE/ESP-E7BEF1/temp/28-aa085a401401/600";
        mysubs.add(subs);

        subs = new Subscriptions();
        subs.action="Chart";
        subs.deleted=false;
        subs.durable=1;
        subs.interval=300;
        subs.name="Druck";
        subs.sensor="ads1115-48-0";
        subs.server="ESP-E7BEF1";
        subs.topic="/SE/ESP-E7BEF1/temp/ads1115-48-0/300";
        mysubs.add(subs);

        dataBox.put(mysubs);
        Log.i("SubscriptionHandler", "Subscriptions count: "+dataBox.count());
*/
    }
}
