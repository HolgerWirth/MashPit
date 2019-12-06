package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SensorStatusListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "SensorList";
    SnackBar snb;
    SensorStatusAdapter sa;
    Intent sintent;
    List<SensorEvent> result = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensorstatus);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.sensorstatus_content);
        snb = new SnackBar(coordinatorLayout);
        Log.i(DEBUG_TAG, "OnCreate");

        final RecyclerView sensorstatusList = findViewById(R.id.sensorstatusList);

        sensorstatusList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        sensorstatusList.setLayoutManager(llm);

        Toolbar toolbar = findViewById(R.id.sensorstatus_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        result.addAll(updateServerList());
        sa = new SensorStatusAdapter(result);

        ItemClickSupport.addTo(sensorstatusList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");

                sintent = new Intent(getApplicationContext(), MPProcListActivity.class);
                sintent.putExtra("ACTION", "list");
                sintent.putExtra("server", result.get(position).getServer());
                sintent.putExtra("alias", result.get(position).getName());

                startActivityForResult(sintent, 0);
            }
        });

        ItemClickSupport.addTo(sensorstatusList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView view, int position, View v) {
                Log.i(DEBUG_TAG, "Long Clicked!");

                return true;
            }
        });

        sensorstatusList.setAdapter(sa);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky=true, threadMode = ThreadMode.MAIN)
    public void getSensorEvent(SensorEvent sensorEvent) {
        Log.i(DEBUG_TAG, "SensorEvent arrived: " + sensorEvent.getServer() + "/" + sensorEvent.getSensor());
        List<SensorEvent> updateresult = updateServerList();
        result.clear();
        result.addAll(updateresult);
        EventBus.getDefault().removeStickyEvent(SensorEvent.class);
        sa.notifyDataSetChanged();
    }

    private List<SensorEvent> updateServerList()
    {
        final List<SensorEvent> upresult = new ArrayList<>();
        List<SensorStatus> sensorStatuses = new Select().all().from(SensorStatus.class).orderBy("server ASC").execute();

        for (int i = 0; i < sensorStatuses.size(); i++) {
           String server = sensorStatuses.get(i).server;
           String alias=sensorStatuses.get(i).alias;

            SensorEvent sensorevent = new SensorEvent();
            if(upresult.size()==0)
            {
                sensorevent.setServer(server);
                sensorevent.setName(alias);
                sensorevent.setActive(sensorStatuses.get(i).active);
                upresult.add(sensorevent);
            }
            boolean found = false;
            for(int t=0; t < upresult.size();t++) {
                if (upresult.get(t).getServer().equals(server)) {
                    found = true;
                    sensorevent = upresult.get(t);
                    sensorevent.setName(alias);

                    upresult.set(t, sensorevent);
                    break;
                }
            }
            if(!found)
            {
                sensorevent.setServer(server);
                sensorevent.setName(alias);
                upresult.add(sensorevent);
            }
        }
        return upresult;
    }
}
