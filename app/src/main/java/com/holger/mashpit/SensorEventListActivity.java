package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.SensorsHandler;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SensorPublishMQTT;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SensorEventListActivity extends AppCompatActivity implements SensorPublishMQTT.OnPublishConfiguration {
    private static final String DEBUG_TAG = "SensorEventListActivity";
    SensorEventListAdapter sa;
    RecyclerView eventList;
    MaterialAlertDialogBuilder alertDialog;
    CoordinatorLayout coordinatorLayout;
    String sensor;
    String type;
    String name;
    String server;
    String family;
    int GPIO;
    int mcpid;
    List<Sensors> sensors;
    String action;
    boolean iscollapsed=false;
    boolean active;

    int resultCode=0;
    SnackBar snb;
    EditText typeField = null;
    FloatingActionButton fabOK;
    Context context = this;
    EditText sensorName;
    String oldName;

    SensorsHandler sensorsHandler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorsHandler = new SensorsHandler();
        family = getIntent().getStringExtra("family");
        action = getIntent().getStringExtra("ACTION");
        sensor = getIntent().getStringExtra("sensor");
        type = getIntent().getStringExtra("type");
        name = getIntent().getStringExtra("name");
        server = getIntent().getStringExtra("server");
        GPIO = getIntent().getIntExtra("GPIO",0);
        mcpid=getIntent().getIntExtra("mcpid",0);
        active=getIntent().getBooleanExtra("active",false);

        alertDialog = new MaterialAlertDialogBuilder(this);

        setContentView(R.layout.activity_sensoreventlist);
        typeField = findViewById(R.id.sensorType);
        typeField.setText(type);
        typeField.setEnabled(false);
        coordinatorLayout = findViewById(R.id.layout_eventlist);

        oldName=name;
        final LinearLayout speeddial= this.findViewById(R.id.eventspeeddial);
        fabOK = findViewById(R.id.eventfabOK);
        final FloatingActionButton fabadd = findViewById(R.id.eventfabadd);
        FloatingActionButton faboutput = findViewById(R.id.eventfabOutput);
        FloatingActionButton fabinput = findViewById(R.id.eventfabInput);

        fabOK.hide();

        fabadd.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB button");
            if (iscollapsed) {
                speeddial.setVisibility(LinearLayout.GONE);
                iscollapsed = false;
            } else {
                speeddial.setVisibility(LinearLayout.VISIBLE);
                iscollapsed = true;
            }
        });

        faboutput.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB Output");
            Intent l = new Intent(getApplicationContext(), SensorEventEditActvity.class);
            l.putExtra("ACTION", "new");
            l.putExtra("family",family);
            l.putExtra("sensor", sensor);
            l.putExtra("name",name);
            l.putExtra("server",server);
            l.putExtra("dir","OUT");
            l.putExtra("hw","MCP");
            startActivityForResult(l, 0);
            speeddial.setVisibility(LinearLayout.GONE);
        });

        fabinput.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB Input");
            Intent l = new Intent(getApplicationContext(), SensorEventEditActvity.class);
            l.putExtra("ACTION", "new");
            l.putExtra("family",family);
            l.putExtra("sensor", sensor);
            l.putExtra("type",type);
            l.putExtra("name",name);
            l.putExtra("server",server);
            l.putExtra("dir","IN");
            l.putExtra("hw","MCP");
            startActivityForResult(l, 0);
            speeddial.setVisibility(LinearLayout.GONE);
        });

        final EditText sensorId = findViewById(R.id.sensorId);
        sensorName = findViewById(R.id.sensorName);

        snb = new SnackBar(coordinatorLayout);

        Toolbar toolbar = findViewById(R.id.sensoredit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(name);

        eventList = findViewById(R.id.sensorEventList);
        eventList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        eventList.setLayoutManager(llm);

        Log.i(DEBUG_TAG, "Started with action: " + action + " and type: " + type);

        sensors = refreshEventList(sensor);
        sa = new SensorEventListAdapter(sensors);
        eventList.setAdapter(sa);

        sensorName.setText(name);
        sensorId.setText(sensor);
        sensorId.setEnabled(false);

        sensorName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (iscollapsed) {
                    speeddial.setVisibility(LinearLayout.GONE);
                    iscollapsed = false;
                }
                fabadd.hide();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(DEBUG_TAG, "Name edited!");
                assert action != null;
                if (!editable.toString().isEmpty()) {
                    name = editable.toString();
                    fabOK.show();
                }
            }
        });

        fabadd.show();

        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            setResult(resultCode, null);
            finish();
        });

        fabOK.setOnClickListener(view -> {
            alertDialog.setTitle(getString(R.string.pubConfig));
            alertDialog.setMessage(getString(R.string.confPublishAlert, name));
            alertDialog.setIcon(R.drawable.ic_launcher);

            alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on Cancel!");
                sensorName.setText(oldName);
            });

            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                pubMQTT.PublishSensorConf(server, sensor, type, mcpid, createJSONConfig());
            });
            alertDialog.show();
        });

        ItemClickSupport.addTo(eventList).setOnItemClickListener((recyclerView, position, v) -> {
            Log.i(DEBUG_TAG, "Clicked!");

            Sensors sensors = sa.getItem(position);
            Intent l = new Intent(getApplicationContext(), SensorEventEditActvity.class);

            l.putExtra("ACTION", "edit");
            l.putExtra("family",sensors.family);
            l.putExtra("sensor", sensors.sensor);
            l.putExtra("type",sensors.type);
            l.putExtra("name",name);
            l.putExtra("event",sensors.event);
            l.putExtra("server",sensors.server);
            l.putExtra("active",sensors.active);
            l.putExtra("GPIO",sensors.port);
            l.putExtra("dir",sensors.dir);
            l.putExtra("reg",sensors.reg);
            l.putExtra("hyst",sensors.hyst);
            l.putExtra("hw","MCP");
            startActivityForResult(l, 0);
            speeddial.setVisibility(LinearLayout.GONE);
        });
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode,null);
        finish();
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
        snb.stopEvents();
        Log.i(DEBUG_TAG, "onStop()");
    }

    private List<Sensors> refreshEventList(String sensor)
    {
        return sensorsHandler.getEvents(server,sensor);
    }

    private String createJSONConfig()
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", type);
            obj.put("name", name);
            obj.put("active", active);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(DEBUG_TAG, "Config: " + obj.toString());
        return obj.toString();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSensorStatusEvent(SensorEvent sensorEvent) {
        String xServer=sensorEvent.getServer();
        Log.i(DEBUG_TAG, "SensorEvent arrived: " + xServer);
        if(server.equals(xServer)) {
            sa.setEventList(refreshEventList(sensor));
            sa.notifyDataSetChanged();
        }
    }

    @Override
    public void PublishConfigurationCallback(Boolean success,int position) {
        if (success) {
            snb.displayInfo(R.string.pubConfOK);
            setResult(1, null);
            finish();
        } else {
            snb.displayInfo(R.string.pubConfNOK);
        }
        sa.notifyDataSetChanged();
    }
}
