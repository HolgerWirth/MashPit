package com.holger.mashpit;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.model.MPStatus;
import com.holger.mashpit.tools.SnackBar;

import java.util.ArrayList;
import java.util.List;

public class MPStatusListActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MPStatusListActivity";
    SnackBar snb;
    MPStatusAdapter sa;

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
                overridePendingTransition(0, 0);
                finish();
            }
        });

        List<MPStatus> result = new ArrayList<>();
        for (int i = 0; i < MashPit.MPServerList.size(); i++) {
            String server = MashPit.MPServerList.get(i).getMPServer();
            MPStatus mpStatus = new MPStatus();
            int procs=0;
            if(result.size()==0)
            {
                mpStatus.MPServer=server;
                mpStatus.active=Integer.toString(1);
                mpStatus.processes=Integer.toString(1);
                procs=1;
                result.add(mpStatus);
            }
            for(int t=0; t < result.size();t++)
            {
                if(result.get(t).MPServer.equals(server))
                {
                    procs++;
                    result.get(t).processes=Integer.toString(procs);
                }
                else
                {
                    mpStatus.MPServer=server;
                    mpStatus.active="1";
                    mpStatus.processes="1";
                    result.add(mpStatus);
                    break;
                }
            }
        }
        sa = new MPStatusAdapter(result);
        mpstatusList.setAdapter(sa);
    }
}
