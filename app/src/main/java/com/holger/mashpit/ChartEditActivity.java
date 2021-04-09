package com.holger.mashpit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Charts;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.Subscriptions;
import com.holger.share.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChartEditActivity extends AppCompatActivity implements SubscriberAdapter.DeleteSubscriptionCallback {

    private static final String DEBUG_TAG = "ChartEditActivity";
    String action = "Chart";

    private FloatingActionButton fabOK;
    private FloatingActionButton fabadd;

    private TextView chartname;
    private TextView chartdesc;

    private String editAction;

    private String name = "";
    private String desc = "";
    SubscriberAdapter sa;
    RecyclerView subscriberList;
    List<Subscriptions> topicList;
    boolean subListChanged = false;
    boolean unsaved=false;
    MaterialAlertDialogBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartedit);

        Toolbar toolbar = findViewById(R.id.chartedit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        fabadd = findViewById(R.id.chartsubfabadd);
        fabOK = findViewById(R.id.editButton);
        FloatingActionButton fabcancel = findViewById(R.id.cancelButton);
        topicList = new ArrayList<>();
        builder = new MaterialAlertDialogBuilder(this);

        chartname = findViewById(R.id.chartName);
        chartname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    fabadd.show();
                } else {
                    fabadd.hide();
                }
            }
        });

        chartdesc = findViewById(R.id.chartDesc);
        chartdesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    if(!chartdesc.getText().toString().equals(desc)) {
                        fabOK.show();
                    }
                } else {
                    fabOK.hide();
                }
            }
        });

        fabadd.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'add' button");
            Intent l = new Intent(getApplicationContext(), SelectSensorActivity.class);
            startActivityForResult(l, 0);
        });

        fabOK.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'OK' button");
            if (editAction.equals("insert")) {
                setResult(1, null);
                new Charts(chartname.getText().toString(), chartdesc.getText().toString(), "", "", 0, 0, 30).save();
                for (Subscriptions sub : topicList) {
                    if (!sub.deleted) {
                        String topic = "/SE/" + sub.server + "/temp/" + sub.sensor + "/" + sub.interval;
                        new Subscriptions(topic, sub.action, sub.name, sub.server, sub.sensor, sub.interval, 1, false).save();
                        subListChanged=true;
                        unsaved=false;
                    }
                }
            }
            if (editAction.equals("edit")) {
                setResult(1, null);
                if (!chartdesc.getText().toString().equals(desc)) {
                    new Update(Charts.class)
                            .set("description = ?", chartdesc.getText().toString())
                            .where("name = ?", name)
                            .execute();
                }
                if (subListChanged) {
                    for (Subscriptions sub : topicList) {
                        if (sub.deleted) {
                            if (sub.getId() != null) {
                                new Update(Subscriptions.class)
                                        .set("deleted = ?", 1)
                                        .where("action = ? and name = ? and topic = ?", sub.action, sub.name, sub.topic)
                                        .execute();
                            }
                        } else {
                            if (sub.getId() == null) {
                                boolean exist = new Select().from(Subscriptions.class).where("action=?", action)
                                        .and("name = ?", name)
                                        .and("deleted=?", 1)
                                        .orderBy("server ASC").exists();
                                if (exist) {
                                    new Update(Subscriptions.class)
                                            .set("deleted = ?", 0)
                                            .where("action = ? and name = ? and topic = ? and deleted = ?", sub.action, sub.name, sub.topic, 1)
                                            .execute();
                                } else {
                                    new Subscriptions(sub.topic, sub.action, sub.name, sub.server, sub.sensor, sub.interval, 1, false).save();
                                }
                            }
                        }
                    }
                }
            }
            if(subListChanged) {
                builder.setTitle(getString(R.string.Subchanged_alert_title));
                builder.setMessage(getString(R.string.Subchanged_text));
                builder.setPositiveButton(getString(R.string.Subchanged_button), (dialog, which) -> {
                    Log.i(DEBUG_TAG, "Reconnect pressed!");
                    Log.i(DEBUG_TAG, "Stop service!");
                    Intent serviceIntent = new Intent(getApplicationContext(), TemperatureService.class);
                    serviceIntent.setAction(Constants.ACTION.RESTART_ACTION);
                    getApplicationContext().startService(serviceIntent);
                    finish();
                });
                builder.setNegativeButton(getString(R.string.Subchanged_cancel), (dialog, which) -> finish());
                builder.show();
            }
            else
            {
                finish();
            }
            unsaved=false;
        });

        fabcancel.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'Cancel' button");
            setResult(0, null);
            onBackPressed();
        });

        final ActionBar ab = getSupportActionBar();
        editAction = getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Activity started with action=" + editAction);
        if (editAction.equals("insert")) {
            fabOK.hide();
            fabadd.hide();
            fabcancel.show();
            assert ab != null;
            ab.setTitle("New Chart");
        }
        if (editAction.equals("edit")) {
            fabOK.hide();
            fabcancel.show();
            fabadd.show();
            name = getIntent().getStringExtra("name");
            desc = getIntent().getStringExtra("desc");
            chartname.setText(name);
            chartdesc.setText(desc);
            chartname.setEnabled(false);
            assert ab != null;
            ab.setTitle(name);
        }

        subscriberList = findViewById(R.id.chartSubscriberList);
        subscriberList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        subscriberList.setLayoutManager(llm);

        topicList = initSubscriber();
        sa = new SubscriberAdapter(refreshSubscriber(topicList));
        sa.setOnItemClickListener(this);
        subscriberList.setAdapter(sa);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "ResultCode=" + resultCode);
        String type = chartname.getText().toString();
        Subscriptions subscriptions;
        boolean exists = false;

        if (resultCode == 1) {
            unsaved=true;
            String server = data.getStringExtra("server");
            String sensor = data.getStringExtra("sensor");
            int interval = data.getIntExtra("interval", 0);
            String topic = "/SE/" + server + "/temp/" + sensor + "/" + interval;
            Log.i(DEBUG_TAG, type + ": New subscription selected: " + topic);

            for (Subscriptions sub : topicList) {
                if (sub.topic.equals(topic)) {
                    if (sub.deleted) {
                        Log.i(DEBUG_TAG, "Subscription was deleted before!");
                        sub.deleted = false;
                        exists = true;
                    }
                }
            }

            if (exists) {
                Log.i(DEBUG_TAG, "Subscription already exists!");
            } else {
                Log.i(DEBUG_TAG, "New subscription for: " + type);
                subListChanged = true;
                subscriptions = new Subscriptions(topic, "Chart", type, server, sensor, interval, 1, false);
                topicList.add(subscriptions);
                name = type;
                sa = new SubscriberAdapter(refreshSubscriber(topicList));
                sa.setOnItemClickListener(this);
                subscriberList.setAdapter(sa);
                fabOK.show();
            }
        }
    }

    private List<Subscriptions> initSubscriber() {
        List<Subscriptions> dbresult;
        dbresult = new Select().from(Subscriptions.class).where("action=?", action)
                .and("name = ?", name)
                .and("deleted=?", false)
                .orderBy("server ASC").execute();

        return dbresult;
    }

    private List<Subscriptions> refreshSubscriber(List<Subscriptions> subscriptions) {
        String serverId = "";
        List<Subscriptions> tempSub = new ArrayList<>();
        for (Subscriptions sub : subscriptions) {
            if (!sub.deleted) {
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
    public void onSubscriptionDeleted(final String topic) {
        Log.i(DEBUG_TAG, "Subscription deleted on position: " + topic);
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            subListChanged = true;
            List<Subscriptions> tempSub = new ArrayList<>();
            for (Subscriptions sub : topicList) {
                if (!sub.topic.equals(topic)) {
                    tempSub.add(sub);
                } else {
                    sub.deleted = true;
                }
            }
            sa.refreshSubscribers(tempSub);
            sa.notifyDataSetChanged();
            fabOK.show();
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Log.i(DEBUG_TAG, "OnBackPressed()");
        if (unsaved || !(chartdesc.getText().toString().equals(desc))) {
            builder.setTitle(getString(R.string.NotSaved_title));
            builder.setMessage(getString(R.string.NotSaved_text));
            builder.setNegativeButton(getString(R.string.NotSaved_button), (dialog, which) -> {
                Log.i(DEBUG_TAG, "OK pressed!");
                ChartEditActivity.super.onBackPressed();
            });
            builder.setPositiveButton(getString(R.string.NotSaved_cancel),null);
            builder.show();
        }
        else
        {
            ChartEditActivity.super.onBackPressed();
        }
    }
}