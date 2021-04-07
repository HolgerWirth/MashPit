package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.Subscriptions;
import com.holger.share.Constants;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SubscriberActivity extends AppCompatActivity implements SubscriberAdapter.DeleteSubscriptionCallback {

    private static final String DEBUG_TAG = "SubscriberActivity";
    SubscriberAdapter sa;
    String action = "TEST";
    boolean durable;
    boolean subListChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);

        Toolbar toolbar = findViewById(R.id.subscriber_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                finish();
            }
        });
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Sensor Data");

        subListChanged=false;
        action=getIntent().getStringExtra("ACTION");
        durable = getIntent().getBooleanExtra("DURABLE",false);
        if(action==null)
        {
            action="TEST";
        }

        Log.i(DEBUG_TAG, "SubscriberActivity started with: " + action + " and durable="+durable);

        final FloatingActionButton addButton = findViewById(R.id.subscriberfabadd);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked on FAB");
                Intent l = new Intent(getApplicationContext(), SelectSensorActivity.class);
                startActivityForResult(l, 0);
            }
        });

        final RecyclerView subscriberList = findViewById(R.id.subscriberList);
        subscriberList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        subscriberList.setLayoutManager(llm);

        sa = new SubscriberAdapter(refreshSubscriber());
        sa.setOnItemClickListener(this);
        subscriberList.setAdapter(sa);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private List<Subscriptions> refreshSubscriber() {
        String serverId="";
        List<Subscriptions> dbresult;
        List<Subscriptions> tempSub = new ArrayList<>();
        dbresult = new Select().from(Subscriptions.class).where("action=?",action).and("deleted=?",0).orderBy("server ASC").execute();
        for (Subscriptions sub : dbresult) {
            if(!sub.deleted) {
                List<SensorStatus> sensorStatuses = new Select().from(SensorStatus.class).where("server=?", sub.server).orderBy("server ASC").execute();
                for (SensorStatus sensor : sensorStatuses) {
                    if (sub.server.equals(sensor.server)) {
                        serverId = sensor.server;
                        if (!sensor.alias.isEmpty()) {
                            sub.aliasServer = sensor.alias;
                        }
                        break;
                    }
                }
                List<Sensors> sensorNames = new Select().from(Sensors.class).where("server=?", serverId).and("sensor=?", sub.sensor).execute();
                for (Sensors sensorName : sensorNames) {
                    if (!sensorName.name.isEmpty()) {
                        sub.aliasSensor = sensorName.name;
                    }
                    break;
                }
                tempSub.add(sub);
            }
        }
        return tempSub;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "ResultCode="+resultCode);
        if(resultCode==1) {
            String server = data.getStringExtra("server");
            String sensor = data.getStringExtra("sensor");
            int interval = data.getIntExtra("interval", 0);
            String topic = "/SE/"+server+"/temp/"+sensor+"/"+interval;
            Log.i(DEBUG_TAG, "New subscription added: " + topic);

            List<Subscriptions> dbresult = new Select().from(Subscriptions.class).where("action = ?", action)
                    .and("topic = ?", topic).execute();
            if (dbresult.isEmpty()) {
                Log.i(DEBUG_TAG, "Subscription inserted: " + action + ", " + server + ", " + sensor + ", " + interval);
                String name="";
                new Subscriptions(topic,action, name, server, sensor, interval, durable ? 1 : 0,false).save();
                subListChanged=true;
                sa.refreshSubscribers(refreshSubscriber());
                sa.notifyDataSetChanged();
            }
            else
            {
                Log.i(DEBUG_TAG, "Subscription exists (deleted): " + action +": "+topic);
                if(dbresult.get(0).deleted)
                {
                    new Update(Subscriptions.class)
                            .set("deleted = ?", 0)
                            .where("action = ? and topic = ?", action,topic)
                            .execute();
                    subListChanged=true;
                }
            }
        }
    }

    @Override
    public void onSubscriptionDeleted(final String position) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                subListChanged=true;
                Subscriptions sub = new Select().from(Subscriptions.class).where("clientId = ?", position).executeSingle();
                String topic = "/SE/" + sub.server + "/temp/" + sub.sensor + "/" + sub.interval;
                new Update(Subscriptions.class)
                        .set("deleted = ?", 1)
                        .where("action = ? and topic = ?", action,topic)
                        .execute();
                sa.refreshSubscribers(refreshSubscriber());
                sa.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }

    @Override
    protected void onStop() {
        if (subListChanged) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(getString(R.string.Subchanged_alert_title));
            builder.setMessage(getString(R.string.Subchanged_text));
            builder.setPositiveButton(getString(R.string.Subchanged_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(DEBUG_TAG, "Reconnect pressed!");
                    Log.i(DEBUG_TAG, "Stop service!");
                    Intent serviceIntent = new Intent(getApplicationContext(), TemperatureService.class);
                    serviceIntent.setAction(Constants.ACTION.RESTART_ACTION);
                    getApplicationContext().startService(serviceIntent);
                }
            });
            builder.setNegativeButton(getString(R.string.Subchanged_cancel), null);
            builder.show();
        }
        super.onStop();
    }
}