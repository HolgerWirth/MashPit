package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.events.MPStatusEvent;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SensorPublishMQTT;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SensorDevListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "SensorDevListActivity";
    SnackBar snb;
    SensorDevAdapter sa;
    String action = "";
    String server;
    String alias;
    String sensors;
    RecyclerView sensordevList;
    List<Sensors> result;
    boolean iscollapsed=false;
    boolean online=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensordevlist);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.sensordev_content);
        snb = new SnackBar(coordinatorLayout);
        Log.i(DEBUG_TAG, "OnCreate");

        sensordevList = findViewById(R.id.sensordevList);

        sensordevList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        sensordevList.setLayoutManager(llm);

        Toolbar toolbar = findViewById(R.id.sensordev_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        action = getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Started with action: " + action);
        if (action.equals("list")) {
            server = getIntent().getStringExtra("server");
            alias = getIntent().getStringExtra("alias");
            sensors = getIntent().getStringExtra("sensors");
            online=getIntent().getBooleanExtra("online",false);
        }

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        if(!alias.isEmpty())
        {
            ab.setTitle(alias);
        }
        else {
            ab.setTitle(server);
        }

        final FloatingActionButton fabadd = findViewById(R.id.devfabadd);
//        final FloatingActionButton fabpower = findViewById(R.id.procfabpower);
//        final FloatingActionButton fabssr = findViewById(R.id.procfabssr);
        final LinearLayout speeddial= this.findViewById(R.id.devspeeddial);

        fabadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the FAB button");
                if (iscollapsed) {
                    speeddial.setVisibility(LinearLayout.GONE);
                    iscollapsed = false;
                } else {
                    speeddial.setVisibility(LinearLayout.VISIBLE);
                    iscollapsed = true;
                }
            }
        });

/*        fabpower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the PWR button");
                speeddial.setVisibility(LinearLayout.GONE);
                iscollapsed=false;
                Intent l = new Intent(getApplicationContext(), ConfEdit.class);
                l.putExtra("ACTION", "insert");
                l.putExtra("adapter", "PWR");
                l.putExtra("server",server);
                l.putExtra("name",getString(R.string.procfabpowerdesc));
                startActivityForResult(l, 0);
            }
        });

        fabssr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the SSR button");
                speeddial.setVisibility(LinearLayout.GONE);
                iscollapsed=false;
                Intent l = new Intent(getApplicationContext(), ConfEdit.class);
                l.putExtra("ACTION", "insert");
                l.putExtra("adapter", "SSR");
                l.putExtra("server",server);
                l.putExtra("name",getString(R.string.procfabssrdesc));
                startActivityForResult(l, 0);
            }
        });
*/
        result = new Select().from(Sensors.class).where("server = ?", server).orderBy("server ASC").execute();
        sa = new SensorDevAdapter(result);

        ItemClickSupport.addTo(sensordevList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");

                Intent l;
                Sensors sensors = sa.getItem(position);

                l = new Intent(getApplicationContext(), SensorConfEdit.class);
                l.putExtra("ACTION", "edit");
                l.putExtra("sensor", sensors.sensor);
                l.putExtra("interval", sensors.interval);
                l.putExtra("type",sensors.type);
                l.putExtra("name",sensors.name);
                l.putExtra("server",sensors.server);
                l.putExtra("active",sensors.active);
                startActivityForResult(l, 0);
            }
        });

        sa.setSensors(sensors);
        sa.setOnline(online);
        sensordevList.setAdapter(sa);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.i(DEBUG_TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        Log.i(DEBUG_TAG, "onStop()");
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void getMPStatusEvent(MPStatusEvent mpstatusEvent) {
        Log.i(DEBUG_TAG, "MPStatusEvent arrived: " + mpstatusEvent.getMPServer() + "/" + mpstatusEvent.getStatusTopic());
        List<Sensors> updateresult = new Select().from(Sensors.class).where("server = ?", server).orderBy("alias ASC").execute();
        result.clear();
        result.addAll(updateresult);
        sa.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0 )
        {
            return;
        }

        String action = data.getStringExtra("ACTION");
        String type = data.getStringExtra("type");
        String name = data.getStringExtra("name");
        boolean active = data.getBooleanExtra("active",false);
        int interval = data.getIntExtra(("interval"),0);
        String sensor = data.getStringExtra("sensor");
        int gpio = data.getIntExtra(("GPIO"),0);

        JSONObject obj = new JSONObject();
        try {
            obj.put("type",type);
            obj.put("name",name);
            if(type.equals("bme280"))
            {
                obj.put("PIN",gpio);
            }
            obj.put("active",active);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(DEBUG_TAG, "Config: " + obj.toString());

        SensorPublishMQTT pubMQTT = new SensorPublishMQTT();
        if (resultCode == 1) {
            if (pubMQTT.PublishSensorConf(this,server,sensor,type,interval, obj.toString())) {
                snb.displayInfo(R.string.pubConfOK);
            } else {
                snb.displayInfo(R.string.pubConfNOK);
            }
        }
        if (resultCode == 2) {
            if (pubMQTT.PublishSensorConf(this,server,sensor, type, interval,"")) {
                snb.displayUndo(getString(R.string.conf_deleted) + name + "'");
            } else {
                snb.displayInfo(R.string.pubConfNOK);
            }
        }
    }
}