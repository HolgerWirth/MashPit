package com.holger.mashpit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Charts;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.tools.SubscriptionHandler;
import com.holger.share.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChartEditActivity extends AppCompatActivity implements SubscriberAdapter.DeleteSubscriptionCallback {

    private static final String DEBUG_TAG = "ChartEditActivity";
    String action="Chart";

    private FloatingActionButton fabOK;
    private FloatingActionButton fabadd;

    private TextView chartname;
    private TextView chartdesc;

    private String editAction;

    SubscriptionHandler subscriptionHandler;
    private String name="";
    private String desc="";
    private boolean durable=false;
    SubscriberAdapter sa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartedit);

        Toolbar toolbar = findViewById(R.id.chartedit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fabadd= findViewById(R.id.chartsubfabadd);
        fabOK = findViewById(R.id.editButton);
        FloatingActionButton fabcancel = findViewById(R.id.cancelButton);

        chartname = findViewById(R.id.chartName);
        chartdesc = findViewById(R.id.chartDesc);
        ActiveAndroid.beginTransaction();

        chartname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty())
                {
                    fabadd.show();
                }
                else
                {
                    fabadd.hide();
                }
            }
        });

        fabadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the FAB 'add' button");
                Intent l = new Intent(getApplicationContext(), SelectSensorActivity.class);
                startActivityForResult(l, 0);
            }
        });

        fabOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the FAB 'OK' button");
                if(editAction.equals("insert")) {
                    new Charts(chartname.getText().toString(), chartdesc.getText().toString(), "", "", 0, 0, 30).save();
                    setResult(1, null);
                }
                if(editAction.equals("edit"))
                {
                    boolean update=false;
                    if(!chartdesc.getText().toString().equals(desc))
                    {
                        update=true;
                    }

                    if(update)
                    {
                        new Update(Charts.class)
                                .set("description = ?", chartdesc.getText().toString())
                                .where("name = ?", name)
                                .execute();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
                ActiveAndroid.endTransaction();
                setResult(1, null);
                onBackPressed();
            }
        });

        fabcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the FAB 'Cancel' button");
                setResult(0, null);
                ActiveAndroid.endTransaction();
                onBackPressed();
            }
        });

        editAction=getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Activity started with action="+action);
        if(editAction.equals("insert"))
        {
            fabOK.hide();
            fabadd.hide();
            fabcancel.show();
        }
        if(editAction.equals("edit"))
        {
            fabOK.show();
            fabcancel.show();
            fabadd.show();
            name=getIntent().getStringExtra("name");
            desc=getIntent().getStringExtra("desc");
            chartname.setText(name);
            chartdesc.setText(desc);
            chartname.setEnabled(false);
        }

        subscriptionHandler = new SubscriptionHandler(action);
        final RecyclerView subscriberList = findViewById(R.id.chartSubscriberList);
        subscriberList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        subscriberList.setLayoutManager(llm);

        sa = new SubscriberAdapter(refreshSubscriber());
        sa.setOnItemClickListener(this);
        subscriberList.setAdapter(sa);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "ResultCode=" + resultCode);
        String type= chartname.getText().toString();
        Subscriptions subscriptions;

        if (resultCode == 1) {
            String server = data.getStringExtra("server");
            String sensor = data.getStringExtra("sensor");
            int interval = data.getIntExtra("interval", 0);
            String topic = "/SE/" + server + "/temp/" + sensor + "/" + interval;
            Log.i(DEBUG_TAG, type+": New subscription selected: " + topic);

            boolean exists = new Select().from(Subscriptions.class).where("action = ?", type)
                    .and("server = ?", server)
                    .and("sensor=?", sensor)
                    .and("interval=?", interval).exists();

            if(exists) {
                Log.i(DEBUG_TAG, "Subscription already exists!");
            }
            else
            {
                Log.i(DEBUG_TAG, "New subscription for: "+type);
                subscriptions = new Subscriptions("Chart",type, server, sensor, interval, 1);
                subscriptions.save();
                fabOK.show();
            }
        }
    }

    private List<Subscriptions> refreshSubscriber() {
        List<Subscriptions> dbresult;
        List<Subscriptions> subscriptions = new ArrayList<>();
        String serverId="";
        dbresult = new Select().from(Subscriptions.class).where("action=?",action)
                .and("name = ?", name)
                .orderBy("server ASC").execute();

        for (Subscriptions sub : dbresult) {
            sub.id=sub.getId();
            List<SensorStatus> sensorStatuses = new Select().from(SensorStatus.class).where("server=?",sub.server).orderBy("server ASC").execute();
            for (SensorStatus sensor : sensorStatuses) {
                if (sub.server.equals(sensor.server)) {
                    serverId=sensor.server;
                    if (!sensor.alias.isEmpty()) {
                        sub.server = sensor.alias;
                    }
                    break;
                }
            }
            List<Sensors> sensorNames = new Select().from(Sensors.class).where("server=?",serverId).and("sensor=?",sub.sensor).execute();
            for(Sensors sensorName : sensorNames)
            {
                if(!sensorName.name.isEmpty())
                {
                    sub.sensor=sensorName.name;
                }
                break;
            }
            subscriptions.add(sub);
        }
        return subscriptions;
    }

    @Override
    public void onSubscriptionDeleted(final long position) {
        Log.i(DEBUG_TAG, "Subscription deleted on position: "+position);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Subscriptions sub = new Select().from(Subscriptions.class).where("clientId = ?", position).executeSingle();
                String topic = "/SE/" + sub.server + "/temp/" + sub.sensor + "/" + sub.interval;

                new Delete().from(Subscriptions.class).where("clientId = ?", position).execute();
                sa.refreshSubscribers(refreshSubscriber());
                sa.notifyDataSetChanged();

                if (!(subscriptionHandler.getAllSubscription(durable).contains(topic))) {
                    Intent serviceIntent = new Intent(ChartEditActivity.this, TemperatureService.class);
                    serviceIntent.setAction(Constants.ACTION.UNSUBSCRIBE_ACTION);
                    serviceIntent.putExtra("TOPIC", topic);
                    startService(serviceIntent);
                }
                fabOK.show();
            }
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }
}