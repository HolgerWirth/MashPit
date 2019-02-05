package com.holger.mashpit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.holger.mashpit.tools.UdpServer;

public class FindServerActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "FindServerActivity";
    SharedPreferences prefs;
    UdpServer myUdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setContentView(R.layout.activity_find_server);
        Toolbar toolbar = findViewById(R.id.findServer_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Cancelled!");
                if(myUdp!=null){
                    myUdp.closeSocket();
                }
                overridePendingTransition(0, 0);
                finish();
            }
        });

        Log.i(DEBUG_TAG, "Find server clicked!");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent data = new Intent();
                setResult(0,null);

                myUdp = new UdpServer();
                if(myUdp.runUdpServer())
                {
                    Log.i(DEBUG_TAG, "Found server: "+myUdp.getSenderIP());
                    Log.i(DEBUG_TAG, "Message: "+myUdp.getMessage());

                    String[] udpResult = myUdp.getMessage().split(":");
                    data.putExtra("IP",udpResult[0]);
                    data.putExtra("port",udpResult[1]);
                    setResult(1,data);
                }
                Log.i(DEBUG_TAG, "Finished!");
                if(myUdp!=null){
                    myUdp.closeSocket();
                }
                finish();
            }
        };

        new Thread(runnable).start();
    }

    @Override
    protected void onDestroy() {
        Log.i(DEBUG_TAG, "Destroyed!");
        if(myUdp!=null){
            myUdp.closeSocket();
        }
        super.onDestroy();
    }
}
