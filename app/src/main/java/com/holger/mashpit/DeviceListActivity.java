package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.Devices;
import com.holger.mashpit.model.DevicesHandler;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "DeviceList";
    SnackBar snb;
    DeviceListAdapter sa;
    Intent sintent;
    List<SensorEvent> result = new ArrayList<>();
    CoordinatorLayout coordinatorLayout=null;
    DevicesHandler devicesHandler;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1)
        {
            List<SensorEvent> updateresult = updateServerList();
            result.clear();
            result.addAll(updateresult);
            sa.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensorstatus);

        devicesHandler = new DevicesHandler();
        coordinatorLayout = findViewById(R.id.sensorstatus_content);
        Log.i(DEBUG_TAG, "OnCreate");

        final RecyclerView sensorstatusList = findViewById(R.id.sensorstatusList);

        sensorstatusList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        sensorstatusList.setLayoutManager(llm);

        Toolbar toolbar = findViewById(R.id.sensorstatus_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        sa = new DeviceListAdapter(result);

        ItemClickSupport.addTo(sensorstatusList).setOnItemClickListener((recyclerView, position, v) -> {
            Log.i(DEBUG_TAG, "Clicked!");

            sintent = new Intent(getApplicationContext(), SensorListActivity.class);
            sintent.putExtra("ACTION", "list");
            sintent.putExtra("server", result.get(position).getServer());
            sintent.putExtra("alias", result.get(position).getName());
            sintent.putExtra("sensors",result.get(position).getSensor());
            sintent.putExtra("online",result.get(position).isActive());
            sintent.putExtra("system",result.get(position).getSystem());
            sintent.putExtra("version",result.get(position).getVersion());
            sintent.putExtra("IP",result.get(position).getIP());
            sintent.putExtra("TS",result.get(position).getTS());

            startActivityForResult(sintent, 0);
        });

        sensorstatusList.setAdapter(sa);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(DEBUG_TAG, "OnStart");
        EventBus.getDefault().register(this);
        snb = new SnackBar(coordinatorLayout);
        result.clear();
        result.addAll(updateServerList());
        sa.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(DEBUG_TAG, "OnStop");
        EventBus.getDefault().unregister(this);
        snb.stopEvents();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSensorEvent(SensorEvent sensorEvent) {
        Log.i(DEBUG_TAG, "SensorEvent arrived: " + sensorEvent.getServer() + "/" + sensorEvent.getSensor());
        List<SensorEvent> updateresult = updateServerList();
        result.clear();
        result.addAll(updateresult);
        sa.notifyDataSetChanged();
    }

    private List<SensorEvent> updateServerList() {
        final List<SensorEvent> upresult = new ArrayList<>();

        List<Devices> devices = devicesHandler.getDeviceStatus();

        for (Devices dev : devices) {
            SensorEvent sensorevent = new SensorEvent();
            sensorevent.setServer(dev.device);
            sensorevent.setName(dev.alias);
            sensorevent.setActive(dev.active);
            sensorevent.setSensor(dev.sensor);
            sensorevent.setSystem(dev.system);
            sensorevent.setTS(dev.TS);
            sensorevent.setVersion(dev.version);
            sensorevent.setIP(dev.IP);
            upresult.add(sensorevent);
        }
        return upresult;
    }
}
