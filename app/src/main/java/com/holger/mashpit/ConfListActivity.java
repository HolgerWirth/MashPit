package com.holger.mashpit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.holger.mashpit.events.ConfEvent;
import com.holger.mashpit.model.Config;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.tools.PublishMQTT;
import com.holger.mashpit.tools.SnackBar;
import com.melnykov.fab.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ConfListActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "ConfListActivity";
    FloatingActionButton actionButton;
    FloatingActionButton deleteButton;
    FloatingActionButton cancelButton;
    Intent l;
    ConfAdapter sa;
    private int position;
    SnackBar snb;
    View.OnClickListener mOnClickListener;
    Config del_conf;
    int del_pos;
    String del_xml;
    String del_name;
    String action;
    String topic;
    String type;

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conflist);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.conflist_content);
        snb = new SnackBar(coordinatorLayout);
        Log.i(DEBUG_TAG, "OnCreate");

        action = getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Started with action: " + action);
        if (action.equals("list")) {
            topic = getIntent().getStringExtra("topic");
            type = getIntent().getStringExtra("type");
        }

        final Context context = this;

        snb.setmOnClickListener(
                mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(DEBUG_TAG, "Undo Clicked!");
                        PublishMQTT pubMQTT = new PublishMQTT();
                        if(pubMQTT.PublishConf(context,del_name,del_xml))
                        {
                            snb.displayInfo(R.string.pubConfOK);
                        }
                        else
                        {
                            snb.displayInfo(R.string.pubConfNOK);
                        }
                    }
                });

        final RecyclerView confList = findViewById(R.id.confList);

        confList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        Toolbar toolbar = findViewById(R.id.conf_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(topic);

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

                l = new Intent(getApplicationContext(), ConfEdit.class);
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
                del_conf = sa.getItem(position);
                del_pos = position;
//                snb.displayUndo("Deleted topic: " + del_conf.topic);
                actionButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
        });

        confList.setLayoutManager(llm);

        List<Config> result = new ArrayList<>();
        for (int i = 0; i < MashPit.confXMLList.size(); i++) {
            if(MashPit.confXMLList.get(i).getConfTopic().equals(topic)) {
                Config conf = confReadXML(i);
                result.add(conf);
            }
        }
        sa = new ConfAdapter(result);

        ItemClickSupport.addTo(confList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");

                Config conf = sa.getItem(position);

                l = new Intent(getApplicationContext(), ConfEdit.class);
                l.putExtra("ACTION", "edit");
                l.putExtra("pos", position);
                l.putExtra("confActive","0");
                l.putExtra("confTopic", conf.topic);
                if(conf.active)
                {
                    l.putExtra("confActive","1");
                }
                l.putExtra("confMinMax", "0");
                if(conf.minmax)
                {
                    l.putExtra("confMinMax","1");
                }

                l.putExtra("confName", conf.name);
                l.putExtra("confTime", conf.time);
                l.putExtra("confTemp", conf.temp);
                l.putExtra("confHyst", conf.hysterese);

                startActivityForResult(l, 0);
            }
        });

        ItemClickSupport.addTo(confList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
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

        confList.setAdapter(sa);
    }

    @Override
    protected void onDestroy() {
        snb.stopEvents();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0 )
        {
            return;
        }
        //Retrieve data in the intent
        Config conf = new Config();
        conf.name = data.getStringExtra("confName");
        conf.topic = data.getStringExtra("confTopic");
        conf.temp = data.getStringExtra("confTemp");
        conf.time = data.getStringExtra("confTime");
        conf.hysterese = data.getStringExtra("confHyst");

        StringWriter xmlstring = new StringWriter();

        try {
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setOutput(xmlstring);
            //start Document
            xmlSerializer.startDocument("UTF-8", true);
            //open tag <items>
            xmlSerializer.startTag("", conf.name);
            xmlSerializer.startTag("", "config");

            xmlSerializer.startTag("", "status");
            xmlSerializer.text(data.getStringExtra("confActive"));
            xmlSerializer.endTag("", "status");

            xmlSerializer.startTag("", "topic");
            xmlSerializer.text(conf.topic);
            xmlSerializer.endTag("", "topic");

            xmlSerializer.startTag("", "maxtemp");
            xmlSerializer.text(conf.temp);
            xmlSerializer.endTag("", "maxtemp");

            xmlSerializer.startTag("", "minmax");
            xmlSerializer.text(data.getStringExtra("confMinMax"));
            xmlSerializer.endTag("", "minmax");

            xmlSerializer.startTag("", "freeze");
            xmlSerializer.text(conf.time);
            xmlSerializer.endTag("", "freeze");

            xmlSerializer.startTag("", "hyst");
            xmlSerializer.text(conf.hysterese);
            xmlSerializer.endTag("", "hyst");

            xmlSerializer.endTag("", "config");
            xmlSerializer.endTag("", conf.name);

            xmlSerializer.endDocument();
            Log.i(DEBUG_TAG, "Config XML: " + xmlstring.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        PublishMQTT pubMQTT = new PublishMQTT();
        if (resultCode == 1) {
            if (pubMQTT.PublishConf(this,conf.name, xmlstring.toString())) {
                snb.displayInfo(R.string.pubConfOK);
            } else {
                snb.displayInfo(R.string.pubConfNOK);
            }
        }
        if (resultCode == 2) {
            del_name = conf.name;
            del_xml = xmlstring.toString();
            if (pubMQTT.PublishConf(this,conf.name, "")) {
                snb.displayUndo(getString(R.string.conf_deleted) + conf.name + "'");
            } else {
                snb.displayInfo(R.string.pubConfNOK);
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getConfEvent(ConfEvent confEvent) {
        Log.i(DEBUG_TAG, "getConfEvent: " + confEvent.getConfTopic());
        int i;
        boolean foundconf = false;
        for (i = 0; i < MashPit.confXMLList.size(); i++)
            if (MashPit.confXMLList.get(i).getConfTopic().equals(confEvent.getConfTopic())) {
                if(confEvent.getXMLString().isEmpty())
                {
                    MashPit.confXMLList.remove(i);
                    sa.deleteItem(i);
                }
                else {
                    MashPit.confXMLList.get(i).setXMLString(confEvent.getXMLString());
                    sa.changeItem(i, confReadXML(i));
                }
                Log.i(DEBUG_TAG, "getConfEvent: " + confEvent.getConfTopic() + " conf found");
                foundconf = true;
            }
        if (foundconf) {
            Log.i(DEBUG_TAG, "getConfEvent: " + confEvent.getConfTopic() + " conf found");
        }
        else {
            if (!confEvent.getXMLString().isEmpty()) {
                MashPit.confXMLList.add(i, confEvent);
                sa.addItem(confReadXML(i));
            }
        }
        EventBus.getDefault().removeStickyEvent(confEvent);
    }

    public Config confReadXML(int pos)
    {
        String text="";
        Config config = new Config();

        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(new StringReader(MashPit.confXMLList.get(pos).getXMLString()));
                int eventType = parser.getEventType();
                int counter=0;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagname = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (counter==0) {
                                config.name=parser.getName();
                            }
                            counter++;
                            break;

                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            if (tagname.equalsIgnoreCase("status")) {
                                if (text != null && text.equals("1")) {
                                    config.active = true;
                                }
                                if (text != null && text.equals("0")) {
                                    config.active = false;
                                }
                                break;
                            }
                            if (tagname.equalsIgnoreCase("topic"))
                            {
                                config.topic = text;
                                break;
                            }
                            if (tagname.equalsIgnoreCase("maxtemp"))
                            {
                                config.temp = text;
                                break;
                            }
                            if (tagname.equalsIgnoreCase("minmax"))
                            {
                                if (text != null && text.equals("1")) {
                                    config.minmax = true;
                                }
                                if (text != null && text.equals("0")) {
                                    config.minmax = false;
                                }
                                break;
                            }
                            if (tagname.equalsIgnoreCase("freeze"))
                            {
                                config.time = text;
                                break;
                            }
                            if (tagname.equalsIgnoreCase("hyst"))
                            {
                                config.hysterese = text;
                                break;
                            }
                            break;

                        default:
                            break;
                    }
                    eventType = parser.next();
                }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}