package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.Subscriptions;

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

        action=getIntent().getStringExtra("ACTION");
        if(action==null)
        {
            action="TEST";
        }
        Log.i(DEBUG_TAG, "SubscriberActivity started with: " + action);

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
        List<Subscriptions> dbresult;
        List<Subscriptions> subscriptions = new ArrayList<>();
        dbresult = new Select().from(Subscriptions.class).where("action=?",action).orderBy("server ASC").execute();
        for (Subscriptions sub : dbresult) {
            sub.id=sub.getId();
            List<SensorStatus> sensorStatuses = new Select().from(SensorStatus.class).where("server=?",sub.server).orderBy("server ASC").execute();
            for (SensorStatus sensor : sensorStatuses) {
                if (sub.server.equals(sensor.server)) {
                    if (!sensor.alias.isEmpty()) {
                        sub.server = sensor.alias;
                    }
                    break;
                }
            }
            List<Sensors> sensorNames = new Select().from(Sensors.class).where("sensor=?",sub.sensor).execute();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "ResultCode="+resultCode);
        if(resultCode==1) {
            String server = data.getStringExtra("server");
            String sensor = data.getStringExtra("sensor");
            int interval = data.getIntExtra("interval", 0);
            Log.i(DEBUG_TAG, "New subscription: " + action + ", " + server + ", " + sensor + ", " + interval);

            boolean exists = new Select().from(Subscriptions.class).where("action = ?", action)
                    .and("server = ?", server)
                    .and("sensor=?", sensor)
                    .and("interval=?", interval).exists();
            if (!exists) {
                Log.i(DEBUG_TAG, "Subscription inserted: " + action + ", " + server + ", " + sensor + ", " + interval);
                Subscriptions subscriptions = new Subscriptions(action, server, sensor, interval, 0);
                subscriptions.save();

                sa.refreshSubscribers(refreshSubscriber());
                sa.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSubscriptionDeleted(final long position) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Delete().from(Subscriptions.class).where("clientId = ?", position).execute();
                sa.refreshSubscribers(refreshSubscriber());
                sa.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }
}