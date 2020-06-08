package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SensorPublishMQTT;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SensorDevListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "SensorDevListActivity";
    SnackBar snb;
    SensorDevAdapter sa;
    String server;
    String alias;
    String sensors;
    RecyclerView sensordevList;
    List<Sensors> result;
    boolean iscollapsed=false;
    boolean online=false;
    private static String translation;
    private long TS;
    private TextView serverSystem;
    private TextView serverVersion;
    private TextView sensorName;
    private CountDownTimer countdown;

    private int resultCode=0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensordevlist);

        translation=getApplicationContext().getResources().getString(R.string.uptimeformat);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.sensordev_content);
        snb = new SnackBar(coordinatorLayout);
        Log.i(DEBUG_TAG, "OnCreate");

        final MaterialAlertDialogBuilder alertDialog;
        alertDialog = new MaterialAlertDialogBuilder(this);
        final Context context = this;

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

        server = getIntent().getStringExtra("server");
        alias = getIntent().getStringExtra("alias");
        sensors = getIntent().getStringExtra("sensors");
        online = getIntent().getBooleanExtra("online", false);
        TS = getIntent().getLongExtra("TS", 0);

        TextView serverId = findViewById(R.id.serverId);
        serverSystem = findViewById(R.id.serverSystem);
        TextView serverUptime = findViewById(R.id.serverUptime);
        serverVersion = findViewById(R.id.serverVersion);
        sensorName = findViewById(R.id.sensorName);

        serverId.setEnabled(false);
        serverSystem.setEnabled(false);
        serverUptime.setEnabled(false);
        serverVersion.setEnabled(false);

        serverId.setText(server);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        if(!alias.isEmpty())
        {
            ab.setTitle(alias);
            sensorName.setText(alias);
        }
        else {
            ab.setTitle(server);
        }

        serverSystem.setText(getIntent().getStringExtra("system"));
        serverVersion.setText(getIntent().getStringExtra("version"));
        serverUptime.setText(Long.toString(TS));
        sensorName.setText(alias);
        sensorName.setEnabled(online);

        final FloatingActionButton fabadd = findViewById(R.id.devfabadd);
        final FloatingActionButton fabBME = findViewById(R.id.devfabaddBME);
        final FloatingActionButton fabDHT = findViewById(R.id.devfabaddDHT);
        final LinearLayout speeddial= this.findViewById(R.id.devspeeddial);
        final FloatingActionButton fabOK = findViewById(R.id.devfabOK);

        fabOK.hide();

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

        fabBME.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the BME FAB");
                speeddial.setVisibility(LinearLayout.GONE);
                iscollapsed=false;
                Intent l = new Intent(getApplicationContext(), SensorConfEdit.class);
                l.putExtra("ACTION", "insert");
                l.putExtra("sensor", "BME280");
                l.putExtra("type", "bme280");
                l.putExtra("server",server);
                l.putExtra("name","BME280");
                startActivityForResult(l, 0);
            }
        });

        fabDHT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked the DHT FAB");
                speeddial.setVisibility(LinearLayout.GONE);
                iscollapsed=false;
                Intent l = new Intent(getApplicationContext(), SensorConfEdit.class);
                l.putExtra("ACTION", "insert");
                l.putExtra("sensor", "DHT11");
                l.putExtra("type", "dht11");
                l.putExtra("server",server);
                l.putExtra("name","DHT11");
                l.putExtra("GPIO",0);
                l.putExtra("address","0");
                startActivityForResult(l, 0);
            }
        });

        fabOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.setTitle(getString(R.string.pubConfig));
                alertDialog.setMessage(getString(R.string.confPublishAlert, alias));
                alertDialog.setIcon(R.drawable.ic_launcher);

                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(DEBUG_TAG, "Clicked on Cancel!");
                    }
                });

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("alias", alias);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                        if (pubMQTT.PublishServerStatus(server, obj.toString())) {
                            snb.displayInfo(R.string.pubConfOK);
                            fabOK.hide();
                            fabadd.show();
                            resultCode = 1;
                        } else {
                            snb.displayInfo(R.string.pubConfNOK);
                        }
                    }
                });
                alertDialog.show();
            }
        });

        sensorName.addTextChangedListener(new TextWatcher() {
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
                    if(!editable.toString().equals(alias))
                    {
                        alias=editable.toString();
                        fabadd.hide();
                        fabOK.show();
                    }
                }
            }
        });

        result=refreshSensorList();
        sa = new SensorDevAdapter(result);
        startTimer(TS);

        ItemClickSupport.addTo(sensordevList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");

                Intent l;
                Sensors sensors = sa.getItem(position);

                l = new Intent(getApplicationContext(), SensorConfEdit.class);
                l.putExtra("ACTION", "edit");
                l.putExtra("sensor", sensors.sensor);
                l.putExtra("type",sensors.type);
                l.putExtra("name",sensors.name);
                l.putExtra("server",sensors.server);
                l.putExtra("active",sensors.active);
                l.putExtra("GPIO",sensors.port);
                l.putExtra("SDA",sensors.sda);
                l.putExtra("SCL",sensors.scl);
                l.putExtra("ALT",sensors.alt);
                startActivityForResult(l, 0);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                setResult(resultCode, null);
                finish();
            }
        });

        sa.setSensors(sensors);
        sa.setOnline(online);
        sensordevList.setAdapter(sa);
    }

    private List<Sensors> refreshSensorList() {
        List<Sensors> dbresult;
        dbresult = new Select().from(Sensors.class).where("server = ?", server).orderBy("sensor ASC").execute();
        final List<Sensors> upresult = new ArrayList<>();
        String mySensor = "";
        int t = (-1);
        for (int i = 0; i < dbresult.size(); i++) {
            if (!(dbresult.get(i).sensor.equals(mySensor))) {
                t++;
                upresult.add(dbresult.get(i));
                upresult.get(t).interval = 1;
                mySensor = dbresult.get(i).sensor;
            } else {
                upresult.get(t).interval++;
                if (dbresult.get(i).active) {
                    upresult.get(t).active = true;
                }
            }
        }
        List<String> defaultsensor = setSensors(sensors);
        boolean found;
        for (int i = 0; i < defaultsensor.size(); i++) {
            found=false;
            for (int j = 0; j < upresult.size(); j++) {
                if (defaultsensor.get(i).equals(upresult.get(j).sensor)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                t++;
                Sensors xSensor = new Sensors();
                xSensor.sensor = defaultsensor.get(i);
                xSensor.active = false;
                xSensor.interval = 0;
                xSensor.name = defaultsensor.get(i);
                xSensor.server = server;
                xSensor.type = "ds18b20";
                upresult.add(t, xSensor);
            }
        }
        return(upresult);
    }

    private List<String> setSensors(String sensors)
    {
        if(!sensors.isEmpty()) {
            return (Arrays.asList(sensors.split("/")));
        }
        return Collections.emptyList();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSensorStatusEvent(SensorEvent sensorEvent) {
        Log.i(DEBUG_TAG, "SensorEvent arrived for server: "+sensorEvent.getServer()+" sensor:" + sensorEvent.getSensor());
        resultCode=1;
        if(server.equals(sensorEvent.getServer())) {
            if(!sensorEvent.getSensor().isEmpty()) {
                sensors = sensorEvent.getSensor();
                result.clear();
                result.addAll(refreshSensorList());
                sa.notifyDataSetChanged();
            }
            sensorName.setText(sensorEvent.getName());
            sensorName.setEnabled(sensorEvent.isActive());
            serverSystem.setText(sensorEvent.getSystem());
            serverVersion.setText(sensorEvent.getVersion());
            TS=sensorEvent.getTS();
            if(countdown!=null) {
                countdown.cancel();
            }
            startTimer(TS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "ResultCode="+resultCode);
        if(resultCode == 1 )
        {
            result.clear();
            result.addAll(refreshSensorList());
            sa.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode,null);
        finish();
    }

    public void startTimer(final long TS) {
        final EditText TS_field = findViewById(R.id.serverUptime);
        if(TS==0)
        {
            TS_field.setText(R.string.SensorOffline);
            return;
        }
        countdown = new CountDownTimer(1800000, 1000) {
            long uptime = 0;

            public void onTick(long millisUntilFinished) {
                long tsnow = System.currentTimeMillis() / 1000;
                uptime = tsnow - TS;

                TS_field.setText(getFormattedTimeSpan(uptime));
            }

            public void onFinish() {
                TS_field.setText(getFormattedTimeSpan(uptime));

            }
        }.start();
    }

    public static String getFormattedTimeSpan(final long span) {
        long x = span;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60;
        x /= 60;
        long hours = x % 24;
        x /= 24;
        long days = x;

        return String.format(Locale.getDefault(),"%d %s %02d:%02d:%02d", days, translation,hours, minutes, seconds);
    }
}