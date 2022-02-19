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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    MaterialAlertDialogBuilder builder;
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
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Sensor Data");

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.i(DEBUG_TAG, "ResultCode=" + result.getResultCode());
                    if (result.getResultCode() == 1) {
                        Subscriptions sub = new Subscriptions();
                        sub.action = action;
                        sub.name = "";
                        assert result.getData() != null;
                        sub.server = result.getData().getStringExtra("server");
                        sub.sensor = result.getData().getStringExtra("sensor");
                        sub.interval = result.getData().getIntExtra("interval", 0);
                        sub.topic = "/SE/" + sub.server + "/temp/" + sub.sensor + "/" + sub.interval;
                        sub.durable = durable ? 1 : 0;
                        sub.name = "";
                        Log.i(DEBUG_TAG, "New subscription added: " + sub.topic);
                        subsHandler.addSubscription(sub);
                        subListChanged = true;
                        sa.refreshSubscribers(refreshSubscriber());
                        sa.notifyItemInserted(sa.getItemCount()-1);
                    }
                });

        subListChanged=false;
        builder = new MaterialAlertDialogBuilder(this);
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
            myActivityResultLauncher.launch(l);
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

    private List<Subscriptions> refreshSubscriber() {
        List<Subscriptions> dbresult;
        List<Subscriptions> tempSub = new ArrayList<>();
        dbresult = subsHandler.getActiveSubscriptions(action,"");
        for (Subscriptions sub : dbresult) {
            if(!sub.deleted) {
                tempSub.add(sub);
            }
        }
        return tempSub;
    }

    @Override
    public void onSubscriptionDeleted(final long id,int pos) {
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            subListChanged=true;
            subsHandler.setDeletedSubscriptions(id);
            sa.refreshSubscribers(refreshSubscriber());
            sa.notifyItemRemoved(pos);
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (subListChanged) {
            builder.setTitle(getString(R.string.Subchanged_alert_title));
            builder.setMessage(getString(R.string.Subchanged_text));
            builder.setPositiveButton(getString(R.string.Subchanged_button), (dialog, which) -> {
                Log.i(DEBUG_TAG, "Reconnect pressed!");
                Log.i(DEBUG_TAG, "Stop service!");
                Intent serviceIntent = new Intent(getApplicationContext(), TemperatureService.class);
                serviceIntent.setAction(Constants.ACTION.RESTART_ACTION);
                getApplicationContext().startService(serviceIntent);
                super.onBackPressed();
            });
            builder.setNegativeButton(getString(R.string.Subchanged_cancel), (dialog, which) -> finish());
            builder.show();
        }
        else {
            super.onBackPressed();
        }
    }
}