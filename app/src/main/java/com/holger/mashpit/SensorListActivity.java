package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
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
import com.holger.share.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SensorListActivity extends AppCompatActivity implements SensorPublishMQTT.OnPublishConfiguration {
    private static final String DEBUG_TAG = "SensorListActivity";
    SnackBar snb;
    SensorListAdapter sa;
    String server;
    String alias;
    String sensors;
    String IP;
    String system;
    RecyclerView sensorList;
    List<Sensors> result;
    boolean iscollapsed=false;
    boolean online=false;
    private static String translation;
    private long TS;
    private TextView serverSystem;
    private TextView sensorName;
    private Button serverVersion;
    private CountDownTimer countdown;

    private FloatingActionButton fabOK;
    private FloatingActionButton fabadd;

    private int resultCode=0;
    CoordinatorLayout coordinatorLayout=null;
    SensorsHandler sensorsHandler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensordevlist);

        translation=getApplicationContext().getResources().getString(R.string.uptimeformat);

        coordinatorLayout = findViewById(R.id.snb_content);
        Log.i(DEBUG_TAG, "OnCreate");

        sensorsHandler = new SensorsHandler();
        sensorList = findViewById(R.id.sensordevList);
        final MaterialAlertDialogBuilder alertDialog;
        alertDialog = new MaterialAlertDialogBuilder(this);
        final Context context = this;

        sensorList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        sensorList.setLayoutManager(llm);

        Toolbar toolbar = findViewById(R.id.sensordev_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        server = getIntent().getStringExtra("server");
        system = getIntent().getStringExtra("system");
        alias = getIntent().getStringExtra("alias");
        sensors = getIntent().getStringExtra("sensors");
        online = getIntent().getBooleanExtra("online", false);
        IP = getIntent().getStringExtra("IP");
        TS = getIntent().getLongExtra("TS", 0);

        TextView serverId = findViewById(R.id.serverId);
        serverSystem = findViewById(R.id.serverSystem);
        TextView serverUptime = findViewById(R.id.serverUptime);
        serverVersion = findViewById(R.id.serverVersion);
        sensorName = findViewById(R.id.sensorName);

        serverId.setEnabled(false);
        serverSystem.setEnabled(false);
        serverUptime.setEnabled(false);

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

        serverSystem.setText(system);
        serverVersion.setText(getIntent().getStringExtra("version"));
        serverUptime.setText(Long.toString(TS));
        sensorName.setText(alias);
        sensorName.setEnabled(online);

        fabadd = findViewById(R.id.devfabadd);
        final FloatingActionButton fabGPIO = findViewById(R.id.devfabaddGPIO);
        final LinearLayout speeddial= this.findViewById(R.id.devspeeddial);
        fabOK = findViewById(R.id.devfabOK);

        fabOK.hide();

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                myresult -> {
                    if (myresult.getResultCode() == 1) {
                        result.clear();
                        result.addAll(refreshSensorList());
                        sa.notifyDataSetChanged();
                    }
                });

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

        fabGPIO.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the GPIO FAB");
            speeddial.setVisibility(LinearLayout.GONE);
            iscollapsed=false;
            Intent l = new Intent(getApplicationContext(), SensorConfEdit.class);
            l.putExtra("ACTION", "insert");
            l.putExtra("sensor", "");
            l.putExtra("type", "gpio");
            l.putExtra("server",server);
            l.putExtra("name","");
            l.putExtra("GPIO",0);
            l.putExtra("address","0");
            myActivityResultLauncher.launch(l);
        });

        fabOK.setOnClickListener(view -> {

            alertDialog.setTitle(getString(R.string.pubConfig));
            alertDialog.setMessage(getString(R.string.confPublishAlert, alias));
            alertDialog.setIcon(R.drawable.ic_launcher);

            alertDialog.setNegativeButton("Cancel", (dialog, which) -> Log.i(DEBUG_TAG, "Clicked on Cancel!"));

            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                JSONObject obj = new JSONObject();
                try {
                    obj.put("alias", alias);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                pubMQTT.PublishServerStatus(server, obj.toString());
            });
            alertDialog.show();
        });

        serverVersion.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on serverVersion");
            Intent l = new Intent(getApplicationContext(), SensorUpdateActivity.class);
            l.putExtra("server", server);
            l.putExtra("system", system);
            l.putExtra("IP", IP);
            l.putExtra("alias",alias);
            myActivityResultLauncher.launch(l);
        });

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
        sa = new SensorListAdapter(result);
        startTimer(TS);

        ItemClickSupport.addTo(sensorList).setOnItemClickListener((recyclerView, position, v) -> {
            Log.i(DEBUG_TAG, "Clicked!");

            Intent l;
            Sensors sensors = sa.getItem(position);
            l = new Intent(getApplicationContext(), SensorConfEdit.class);

            if(sensors.type.equals("mcp23017"))
            {
                l = new Intent(getApplicationContext(), SensorEventListActivity.class);
            }
            if(sensors.sensor.equals("GPIO"))
            {
                l = new Intent(getApplicationContext(), SensorEventEditActvity.class);
                l.putExtra("hw","GPIO");
            }

            l.putExtra("ACTION", "edit");
            l.putExtra("family",sensors.family);
            l.putExtra("sensor", sensors.sensor);
            l.putExtra("type",sensors.type);
            l.putExtra("name",sensors.name);
            l.putExtra("server",sensors.server);
            l.putExtra("active",sensors.active);
            l.putExtra("GPIO",sensors.port);
            l.putExtra("SDA",sensors.sda);
            l.putExtra("SCL",sensors.scl);
            l.putExtra("ALT",sensors.alt);
            l.putExtra("mcpid",sensors.interval);
            l.putExtra("event",sensors.event);
            l.putExtra("dir",sensors.dir);
            l.putExtra("hyst",sensors.hyst);
            myActivityResultLauncher.launch(l);
        });

        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            setResult(resultCode, null);
            finish();
        });

        sa.setSensors(sensors);
        sa.setOnline(online);
        sensorList.setAdapter(sa);
    }

    private List<Sensors> refreshSensorList() {
        List<Sensors> dbresult = sensorsHandler.getAllSensors(server);
        final List<Sensors> upresult = new ArrayList<>();
        String mySensor = "";
        int t = (-1);
        for (int i = 0; i < dbresult.size(); i++) {
            if(dbresult.get(i).family.equals("EV"))
            {
                if(dbresult.get(i).sensor.equals("GPIO")) {
                    t++;
                    upresult.add(dbresult.get(i));
                }
                continue;
            }
            String key=dbresult.get(i).dir+"-"+dbresult.get(i).sensor;
            if (!(key.equals(mySensor))) {
                t++;
                upresult.add(dbresult.get(i));
                upresult.get(t).interval = upresult.get(t).interval;
                mySensor = key;
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
            if(defaultsensor.get(i).equals("unknown"))
            {
                continue;
            }
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
                xSensor.type = getSensorType(defaultsensor.get(i));
                xSensor.family=getSensorFamily(defaultsensor.get(i));
                upresult.add(t, xSensor);
            }
        }

        return(upresult);
    }

    private List<String> setSensors(String sensors)
    {
        List<String> defsensors = new ArrayList<>();
        if(!sensors.isEmpty()) {
            String[] defsensorslist=sensors.split("/");
            for (String s : defsensorslist) {
                String[] defdev = s.split("-");
                if (defdev[0].equals("ads1115")) {
                    for (int k = 0; k < 4; k++) {
                        defsensors.add(s+"-" + k);
                    }
                } else {
                    defsensors.add(s);
                }
            }
            return (defsensors);
        }
        return Collections.emptyList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        snb = new SnackBar(coordinatorLayout);
        snb.setmOnClickListener(
                v -> {
                    Log.i(DEBUG_TAG, "Retry service");
                    Intent startIntent = new Intent(this, TemperatureService.class);
                    startIntent.setAction(Constants.ACTION.CONNECT_ACTION);
                    startService(startIntent);
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        snb.stopEvents();
        Log.i(DEBUG_TAG, "onStop()");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSensorStatusEvent(SensorEvent sensorEvent) {
        Log.i(DEBUG_TAG, "SensorEvent arrived for server: " + sensorEvent.getServer() + " sensor:" + sensorEvent.getSensor());
        resultCode = 1;
        if (server.equals(sensorEvent.getServer())) {
            sensors = sensorEvent.getSensor();
            result.clear();
            result.addAll(refreshSensorList());
            sa.notifyDataSetChanged();
            if (!sensorEvent.getSensor().equals("GPIO")) {
                sensorName.setText(sensorEvent.getName());
                sensorName.setEnabled(sensorEvent.isActive());
                serverSystem.setText(sensorEvent.getSystem());
                serverVersion.setText(sensorEvent.getVersion());
                TS = sensorEvent.getTS();
            }
            if (countdown != null) {
                countdown.cancel();
            }
            startTimer(TS);
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
            serverVersion.setEnabled(false);
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

    private static String getSensorType(String autodetect)
    {
        String[] type=autodetect.split("-");
        switch(type[0]) {
            case "28":
                return ("ds18b20");

            case "bme280":
                return ("bme280");

            case "oled":
                return ("oled");

            case "unknown":
                return ("unknown");

            case "bh1750":
                return ("bh1750");

            case "ads1115":
                return ("ads1115");

            case "mcp23017":
                return ("mcp23017");
        }
        return("");
    }

    private static String getSensorFamily(String autodetect)
    {
        String[] type=autodetect.split("-");
        switch(type[0]) {
            case "28":

            case "bme280":

            case "bh1750":

            case "mcp23017":

            case "ads1115":
                return ("SE");

            case "oled":
                return ("DISP");

            case "unknown":
                return ("unknown");
        }
        return("");
    }

    @Override
    public void PublishConfigurationCallback(Boolean success) {
        if (success) {
            snb.displayInfo(R.string.pubConfOK);
            fabOK.hide();
            fabadd.show();
            resultCode = 1;
        } else {
            snb.displayInfo(R.string.pubConfNOK);
        }
    }
}