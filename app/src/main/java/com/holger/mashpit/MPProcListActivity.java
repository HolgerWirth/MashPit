package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;
import com.holger.mashpit.events.MPStatusEvent;
import com.holger.mashpit.model.MPStatus;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;

public class MPProcListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MPProcListActivity";
    SnackBar snb;
    MPProcAdapter sa;
    String action = "";
    String server;
    Intent sintent;
    RecyclerView mpprocList;
    List<MPStatus> result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpproc);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.mpproc_content);
        snb = new SnackBar(coordinatorLayout);
        Log.i(DEBUG_TAG, "OnCreate");

        mpprocList = findViewById(R.id.mpprocList);

        mpprocList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mpprocList.setLayoutManager(llm);

        Toolbar toolbar = findViewById(R.id.mpproc_toolbar);
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
        }

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(server);

        result = new Select().from(MPStatus.class).where("MPServer = ?", server).orderBy("topic ASC").execute();
        sa = new MPProcAdapter(result);

        ItemClickSupport.addTo(mpprocList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");

                sintent = new Intent(getApplicationContext(), ConfListActivity.class);
                sintent.putExtra("ACTION", "list");
                sintent.putExtra("topic", result.get(position).topic);
                sintent.putExtra("type", result.get(position).Type);

                startActivityForResult(sintent, 0);
            }
        });

        ItemClickSupport.addTo(mpprocList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView view, int position, View v) {
                Log.i(DEBUG_TAG, "Long Clicked!");

                return true;
            }
        });

        mpprocList.setAdapter(sa);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMPStatusEvent(MPStatusEvent mpstatusEvent) {
        Log.i(DEBUG_TAG, "MPStatusEvent arrived: " + mpstatusEvent.getMPServer() + "/" + mpstatusEvent.getStatusTopic());
        List<MPStatus> updateresult = new Select().from(MPStatus.class).where("MPServer = ?", server).orderBy("topic ASC").execute();
        result.clear();
        result.addAll(updateresult);
        sa.notifyDataSetChanged();
    }
}