package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.DevicesHandler;
import com.holger.mashpit.model.SensorsHandler;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.model.SubscriptionsHandler;
import com.holger.share.Constants;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SubscriberActivity extends AppCompatActivity implements SubscriberAdapter.DeleteSubscriptionCallback {

    private static final String DEBUG_TAG = "SubscriberActivity";
    SubscriberAdapter sa;
    String action = "TEST";
    boolean durable;
    boolean subListChanged;
    SubscriptionsHandler subsHandler;
    DevicesHandler devicesHandler;
    SensorsHandler sensorsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);

        subsHandler = new SubscriptionsHandler();
        devicesHandler = new DevicesHandler();
        sensorsHandler = new SensorsHandler();
        Toolbar toolbar = findViewById(R.id.subscriber_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            finish();
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

        addButton.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked on FAB");
            Intent l = new Intent(getApplicationContext(), SelectSensorActivity.class);
            startActivityForResult(l, 0);
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
        dbresult = subsHandler.getActiveSubscriptions(action,"");
        for (Subscriptions sub : dbresult) {
            if(!sub.deleted) {
                sub.aliasServer = devicesHandler.getDeviceAlias(sub.server);
                sub.aliasSensor = sensorsHandler.getSensorAlias(serverId,sub.sensor);
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
            Subscriptions sub = new Subscriptions();

            sub.action = action;
            sub.name = "";
            sub.server = data.getStringExtra("server");
            sub.sensor = data.getStringExtra("sensor");
            sub.interval = data.getIntExtra("interval", 0);
            sub.topic = "/SE/"+sub.server+"/temp/"+sub.sensor+"/"+sub.interval;
            sub.durable=durable ? 1 : 0;
            sub.name="";
            Log.i(DEBUG_TAG, "New subscription added: " + sub.topic);
            subsHandler.addSubscription(sub);
            subListChanged=true;
            sa.refreshSubscribers(refreshSubscriber());
            sa.notifyDataSetChanged();
        }
    }

    @Override
    public void onSubscriptionDeleted(final long id) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            subListChanged=true;
            subsHandler.setDeletedSubscriptions(id);
            sa.refreshSubscribers(refreshSubscriber());
            sa.notifyDataSetChanged();
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
            builder.setPositiveButton(getString(R.string.Subchanged_button), (dialog, which) -> {
                Log.i(DEBUG_TAG, "Reconnect pressed!");
                Log.i(DEBUG_TAG, "Stop service!");
                Intent serviceIntent = new Intent(getApplicationContext(), TemperatureService.class);
                serviceIntent.setAction(Constants.ACTION.RESTART_ACTION);
                getApplicationContext().startService(serviceIntent);
            });
            builder.setNegativeButton(getString(R.string.Subchanged_cancel), null);
            builder.show();
        }
        super.onStop();
    }
}