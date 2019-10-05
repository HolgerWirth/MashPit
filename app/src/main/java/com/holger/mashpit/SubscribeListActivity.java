package com.holger.mashpit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.holger.mashpit.model.Subscriber;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.SnackBar;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SubscribeListActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "SubscribeListActivity";
    FloatingActionButton actionButton;
    FloatingActionButton deleteButton;
    FloatingActionButton cancelButton;
    Intent l;
    SubscriberAdapter ca;
    SharedPreferences prefs;
    private int position;
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    Subscriber del_sub;
    int del_pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribelist);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.sublist_content);
        snb = new SnackBar(coordinatorLayout);
        snb.setmOnClickListener(
                mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(DEBUG_TAG, "Undo Clicked!");
                        ca.addItem(del_sub, del_pos);
                    }
                });

        final RecyclerView recList = findViewById(R.id.cardList);

        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        Toolbar toolbar = findViewById(R.id.subscriber_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                finish();
            }
        });

        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteMButton);
        actionButton = findViewById(R.id.actionButton);
        actionButton.setVisibility(View.VISIBLE);

        //start listeners
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: insert");

                l = new Intent(getApplicationContext(), SubscribeEdit.class);
                l.putExtra("ACTION", "insert");
                startActivityForResult(l, 0);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: cancel");
                actionButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: delete");
                del_sub = ca.getItem(position);
                del_pos = position;
                ca.addDelItem(del_sub);
                updateDelPrefString();
                ca.deleteItem(position);
                updatePrefString();
                snb.displayUndo("Deleted topic: " + del_sub.topic);
                actionButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
        });

        recList.setLayoutManager(llm);

        List<Subscriber> result = new ArrayList<>();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String subs = prefs.getString("sublist", "");
        assert subs != null;
        if (subs.length() > 0) {
            try {
                JSONObject subscribers = new JSONObject(subs);
                JSONArray subarray = subscribers.getJSONArray("subscriber");
                for (int i = 0; i < subarray.length(); i++) {
                    JSONObject subobj = subarray.getJSONObject(i);
                    Subscriber sub = new Subscriber();
                    sub.topic = subobj.getString("topic");
                    sub.interval = subobj.getString("interval");
                    sub.persistent = subobj.getBoolean("durable");
                    try
                    {
                        sub.remark = subobj.getString("remark");
                    } catch(JSONException e)
                    {
                        sub.remark="";
                    }
                    result.add(sub);
                }

            } catch (JSONException e) {
                Log.i(DEBUG_TAG, "sublist preference does not exist");
            }
        }
        ca = new SubscriberAdapter(result);

        String delsubs = prefs.getString("delsublist", "");
        assert delsubs != null;
        if (delsubs.length() > 0) {
            try {
                JSONObject subscribers = new JSONObject(delsubs);
                JSONArray subarray = subscribers.getJSONArray("delsubscriber");
                for (int i = 0; i < subarray.length(); i++) {
                    JSONObject subobj = subarray.getJSONObject(i);
                    Subscriber sub = new Subscriber();
                    sub.topic = subobj.getString("topic");
                    ca.addDelItem(sub);
                }

            } catch (JSONException e) {
                Log.i(DEBUG_TAG, "delsublist preference does not exist");
            }
        }

        ItemClickSupport.addTo(recList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");
                Subscriber sub = ca.getItem(position);

                l = new Intent(getApplicationContext(), SubscribeEdit.class);
                l.putExtra("ACTION", "edit");
                l.putExtra("pos", position);
                l.putExtra("subTopic", sub.topic);
                l.putExtra("subInterval", sub.interval);
                l.putExtra("persistent", sub.persistent);
                l.putExtra("remark",sub.remark);

                startActivityForResult(l, 0);
            }
        });

        ItemClickSupport.addTo(recList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView view, int pos, View v) {
                position = pos;
                actionButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                Log.i(DEBUG_TAG, "LongClicked!");
                return true;
            }
        });

        recList.setAdapter(ca);
    }

    @Override
    protected void onDestroy() {
        snb.stopEvents();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Retrieve data in the intent
        if (resultCode == 1) {
            String topic = data.getStringExtra("subTopic");
            String interval = data.getStringExtra("subInterval");
            boolean persistent = data.getBooleanExtra("persistent",false);
            String remark = data.getStringExtra("subRemark");

            Log.i(DEBUG_TAG, "onActivityResult: " + topic + " request: " + requestCode + " result: " + resultCode);
            Log.i(DEBUG_TAG, "Interval: " + interval);
            Log.i(DEBUG_TAG, "Durable: " + persistent);

            Subscriber sub = new Subscriber();
            sub.topic = topic;
            sub.interval = interval;
            sub.persistent = persistent;
            sub.remark = remark;

            if (data.getStringExtra("ACTION").equals("edit")) {
                ca.changeItem(data.getIntExtra("pos", 0), sub);
                updateDelPrefString();
            } else {
                ca.addItem(sub);
            }

            updatePrefString();
        }

        if (resultCode == 2) {
            int position = data.getIntExtra("pos", 0);
            del_sub = ca.getItem(position);
            del_pos = position;
            ca.addDelItem(del_sub);
            updateDelPrefString();
            ca.deleteItem(position);
            updatePrefString();
            snb.displayUndo("Deleted topic: " + del_sub.topic);
        }
    }

    private void updatePrefString() {
        Subscriber sub;
        List<Subscriber> sublist = ca.getSubscriberList();
        JSONObject subscribers = new JSONObject();
        JSONArray subarray = new JSONArray();

        for (int i = 0; i < sublist.size(); i++) {
            sub = sublist.get(i);
            JSONObject subobj = new JSONObject();
            try {
                subobj.put("topic", sub.topic);
                subobj.put("interval", sub.interval);
                subobj.put("durable", sub.persistent);
                subobj.put("remark", sub.remark);

                subarray.put(subobj);
                subscribers.put("subscriber", subarray);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i(DEBUG_TAG, "updatePrefString: " + subscribers.toString());
        prefs.edit().putString("sublist", subscribers.toString()).apply();
    }

    private void updateDelPrefString() {
        Subscriber sub;
        List<Subscriber> sublist = ca.getDelSubscriberList();
        JSONObject subscribers = new JSONObject();
        JSONArray subarray = new JSONArray();

        for (int i = 0; i < sublist.size(); i++) {
            sub = sublist.get(i);
            JSONObject subobj = new JSONObject();
            try {
                subobj.put("topic", sub.topic);
                subobj.put("interval", sub.interval);
                subarray.put(subobj);
                subscribers.put("delsubscriber", subarray);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i(DEBUG_TAG, "updateDelPrefString: " + subscribers.toString());
        prefs.edit().putString("delsublist", subscribers.toString()).apply();
    }


}