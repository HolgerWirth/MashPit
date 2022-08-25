package com.holger.mashpit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Charts;
import com.holger.mashpit.model.DevicesHandler;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.model.ChartsHandler;
import com.holger.mashpit.model.SubscriptionsHandler;
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
    private TextView chartdeldays;

    private String editAction;

    private String name = "";
    private String desc = "";
    private long id;
    SubscriberAdapter sa;
    RecyclerView subscriberList;
    List<Subscriptions> topicList;
    boolean subListChanged = false;
    boolean unsaved=false;
    MaterialAlertDialogBuilder builder;
    SubscriptionsHandler subscriptionsHandler;
    DevicesHandler devicesHandler;
    ChartsHandler chartsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartedit);

        subscriptionsHandler = new SubscriptionsHandler();
        chartsHandler = new ChartsHandler();
        devicesHandler = new DevicesHandler();

        Toolbar toolbar = findViewById(R.id.chartedit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        fabadd = findViewById(R.id.chartsubfabadd);
        fabOK = findViewById(R.id.editButton);
        FloatingActionButton fabcancel = findViewById(R.id.cancelButton);
        topicList = new ArrayList<>();
        builder = new MaterialAlertDialogBuilder(this);
        Button posButton = findViewById(R.id.chartParamsButton);

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.i(DEBUG_TAG, "ResultCode=" + result.getResultCode());
                    String type = chartname.getText().toString();
                    Subscriptions subscriptions = new Subscriptions();
                    boolean exists = false;
                    if (result.getResultCode() == 1) {
                        unsaved = true;
                        subscriptions.action = "Chart";
                        subscriptions.durable = 1;
                        subscriptions.deleted = false;
                        assert result.getData() != null;
                        subscriptions.server = result.getData().getStringExtra("server");
                        subscriptions.sensor = result.getData().getStringExtra("sensor");
                        subscriptions.interval = result.getData().getIntExtra("interval", 0);
                        subscriptions.topic = "/SE/" + subscriptions.server + "/temp/" + subscriptions.sensor + "/" + subscriptions.interval;
                        subscriptions.name = type;
                        Log.i(DEBUG_TAG, type + ": New subscription selected: " + subscriptions.topic);

                        for (Subscriptions sub : topicList) {
                            if (sub.topic.equals(subscriptions.topic)) {
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
                            name = type;
                            subscriptionsHandler.addSubscription(subscriptions);
                            topicList = subscriptionsHandler.getActiveSubscriptions("Chart",name);
                            sa = new SubscriberAdapter(refreshSubscriber(topicList));
                            sa.setOnItemClickListener(this);
                            subscriberList.setAdapter(sa);
                            fabOK.show();
                        }
                    }
                });

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
                    fabOK.hide();
                    fabadd.hide();
                }
            }
        });

        chartdesc = findViewById(R.id.chartDesc);
        chartdeldays = findViewById(R.id.chartDeleteDays);
        chartdeldays.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    fabOK.show();
                } else {
                    chartdeldays.setError("Mandatory!");
                    fabOK.hide();
                }
            }
        });

        posButton.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the 'Position' button");
            Intent l = new Intent(getApplicationContext(), ChartParamsListActivity.class);
            l.putExtra("NAME",name);
            myActivityResultLauncher.launch(l);
        });

         fabadd.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'add' button");
//            Intent l = new Intent(getApplicationContext(), SelectSensorActivity.class);
            Intent l = new Intent(getApplicationContext(), SubscriptionStepper.class);
            l.putExtra("ACTION", "insert");
            myActivityResultLauncher.launch(l);
        });

        fabOK.setOnClickListener(view -> {
            Intent result = new Intent();
            Log.i(DEBUG_TAG, "Clicked the FAB 'OK' button");
            if (editAction.equals("insert")) {
                if(chartname.getText().toString().isEmpty())
                {
                    chartname.setError("Mandatory!");
                    return;
                }
                if(topicList.isEmpty())
                {
                    return;
                }
                Charts newChart = new Charts();
                newChart.name=chartname.getText().toString();
                newChart.description=chartdesc.getText().toString();
                newChart.type="";
                chartsHandler.addChart(newChart);
                for (Subscriptions sub : topicList) {
                    if (!sub.deleted) {
                        sub.deldays= Integer.parseInt(chartdeldays.getText().toString());
                        sub.durable=1;
                        subscriptionsHandler.addSubscription(sub);
                        subListChanged=true;
                        unsaved=false;
                    }
                }
                result.putExtra("ACTION","insert");
                setResult(1, result);
            }
            if (editAction.equals("edit")) {
                subscriptionsHandler.setMaxDelDays(name,Integer.parseInt(chartdeldays.getText().toString()));
                if (!chartdesc.getText().toString().equals(desc)) {
                    Charts upChart = new Charts();
                    upChart.id=id;
                    upChart.name=name;
                    upChart.description=chartdesc.getText().toString();
                    upChart.type="";
                    chartsHandler.updateChart(upChart);
                }
                if (subListChanged) {
                    for (Subscriptions sub : topicList) {
                        if (sub.deleted) {
                            if (sub.id != 0) {
                                subscriptionsHandler.updateSubscription(sub);
                            }
                        } else {
                            if (sub.id == 0) {
                                subscriptionsHandler.addSubscription(sub);
                            }
                        }
                    }
                }
                result.putExtra("ACTION","edit");
                setResult(1, result);
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
            chartname.setText("");
            chartdesc.setText("");
            chartdeldays.setText("0");
            assert ab != null;
            ab.setTitle("New Chart");
        }
        if (editAction.equals("edit")) {
            fabOK.hide();
            fabcancel.show();
            fabadd.show();
            name = getIntent().getStringExtra("name");
            desc = getIntent().getStringExtra("desc");
            id = getIntent().getLongExtra("id",0);
            chartname.setText(name);
            chartdesc.setText(desc);
            chartname.setEnabled(false);
            chartdeldays.setText(String.valueOf(subscriptionsHandler.getMaxDelDays(name)));
            assert ab != null;
            ab.setTitle(name);
        }

        topicList = initSubscriber();
        subscriberList = findViewById(R.id.chartSubscriberList);
        subscriberList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        subscriberList.setLayoutManager(llm);

        sa = new SubscriberAdapter(refreshSubscriber(topicList));
        sa.setOnItemClickListener(this);
        subscriberList.setAdapter(sa);
    }

    private List<Subscriptions> initSubscriber() {
        return subscriptionsHandler.getActiveSubscriptions(action,name);
    }

    private List<Subscriptions> refreshSubscriber(List<Subscriptions> subscriptions) {
        List<Subscriptions> tempSub = new ArrayList<>();
        for (Subscriptions sub : subscriptions) {
            if (!sub.deleted) {
                tempSub.add(sub);
            }
        }
        return tempSub;
    }

    @Override
    public void onSubscriptionDeleted(final long id, int pos) {
        Log.i(DEBUG_TAG, "Subscription deleted on position: " + id);
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            subListChanged = true;
            List<Subscriptions> tempSub = new ArrayList<>();
            for (Subscriptions sub : topicList) {
                if (sub.id!=id) {
                    tempSub.add(sub);
                } else {
                    sub.deleted = true;
                }
            }
            sa.refreshSubscribers(tempSub);
            sa.notifyItemRemoved(pos);
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