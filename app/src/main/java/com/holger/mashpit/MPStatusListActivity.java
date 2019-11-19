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
import com.holger.mashpit.events.MPStatusEvent;
import com.holger.mashpit.model.MPServer;
import com.holger.mashpit.model.MPStatus;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MPStatusListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MPStatusListActivity";
    SnackBar snb;
    MPStatusAdapter sa;
    Intent sintent;
    List<MPStatusEvent> result = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpstatus);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.mpstatus_content);
        snb = new SnackBar(coordinatorLayout);
        Log.i(DEBUG_TAG, "OnCreate");

        final RecyclerView mpstatusList = findViewById(R.id.mpstatusList);

        mpstatusList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mpstatusList.setLayoutManager(llm);

        Toolbar toolbar = findViewById(R.id.mpstatus_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        result.addAll(updateServerList());
        sa = new MPStatusAdapter(result);

        ItemClickSupport.addTo(mpstatusList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");

                sintent = new Intent(getApplicationContext(), MPProcListActivity.class);
                sintent.putExtra("ACTION", "list");
                sintent.putExtra("server", result.get(position).getMPServer());
                sintent.putExtra("alias", result.get(position).getAlias());

                startActivityForResult(sintent, 0);
            }
        });

        ItemClickSupport.addTo(mpstatusList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView view, int position, View v) {
                Log.i(DEBUG_TAG, "Long Clicked!");

                return true;
            }
        });

        mpstatusList.setAdapter(sa);
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
    public void getMPStatusEvent(MPStatusEvent mpstatusEvent) {
        Log.i(DEBUG_TAG, "MPStatusEvent arrived: " + mpstatusEvent.getMPServer() + "/" + mpstatusEvent.getStatusTopic());
        List<MPStatusEvent> updateresult = updateServerList();
        result.clear();
        result.addAll(updateresult);
        EventBus.getDefault().removeStickyEvent(MPStatusEvent.class);
        sa.notifyDataSetChanged();
    }

    private List<MPStatusEvent> updateServerList()
    {
        final List<MPStatusEvent> upresult = new ArrayList<>();
        List<MPStatus> mpstatus = new Select().all().from(MPStatus.class).orderBy("MPServer ASC").execute();
        int procs=0;
        int active=0;

        for (int i = 0; i < mpstatus.size(); i++) {
           String server = mpstatus.get(i).MPServer;
           String alias="";
           List<MPServer> server_alias = new Select().from(MPServer.class).where("name = 'MashPit'").and("MPServer = ?",server).execute();
            if(server_alias.size() > 0) {
                alias=server_alias.get(0).alias;
            }

            MPStatusEvent mpevent = new MPStatusEvent();
            if(upresult.size()==0)
            {
                mpevent.setMPServer(server);
                mpevent.setAlias(alias);
                mpevent.setActive(mpstatus.get(i).active);
                mpevent.setProcesses(0);
                mpevent.setActprocesses(0);
                upresult.add(mpevent);
            }
            for(int t=0; t < upresult.size();t++)
            {
                if(upresult.get(t).getMPServer().equals(server))
                {
                    mpevent=upresult.get(t);
                    mpevent.setAlias(alias);
                    procs++;
                    mpevent.setProcesses(procs);
                    if(mpstatus.get(i).active)
                    {
                        active++;
                        mpevent.setActprocesses(active);
                    }

                    upresult.set(t,mpevent);
                }
                else
                {
                    mpevent.setMPServer(server);
                    mpevent.setAlias(alias);
                    mpevent.setProcesses(1);
                    mpevent.setActive(mpstatus.get(i).active);
                    if(upresult.get(t).isActive())
                    {
                        mpevent.setActprocesses(1);
                    }
                    else
                    {
                        mpevent.setActprocesses(0);
                    }
                    upresult.add(mpevent);
                    break;
                }
            }
        }
        return upresult;
    }
}
