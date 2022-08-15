package com.holger.mashpit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.events.SensorDataEvent;
import com.holger.mashpit.model.ChartParams;
import com.holger.mashpit.model.ChartParamsHandler;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.model.SubscriptionsHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ChartParamsEditActivity extends AppCompatActivity implements ChartParamsEditAdapter.UpdateChartCallback {

    private static final String DEBUG_TAG = "ChartParamsEditActivity";
    private FloatingActionButton fabOK;
    private String editAction;

    ChartParamsEditAdapter sa;
    List<Subscriptions> topicList;
    boolean unsaved=false;
    MaterialAlertDialogBuilder builder;
    ChartParamsHandler paramsHandler;
    SubscriptionsHandler subHandler;

    TextView paramsXDesc;
    TextView paramsComment;
    CheckBox paramsAutoscale;
    TextView paramsMin;
    TextView paramsMinOffset;
    TextView paramsMax;
    TextView paramsMaxOffset;
    TextView paramsRound;
    TextView paramsFormat;
    TextView paramsUnit;
    View scalelayout;
    TextView paramsXBounds;
    Spinner paramsXSelector;
    ArrayList<ChartParams> params;
    String name;
    List<String> xBounds;
    Context context;
    int XBfactor;
    int sort=0;
    int pos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getBaseContext();

        setContentView(R.layout.activity_chartparamsedit);

        Toolbar toolbar = findViewById(R.id.paramsedit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        FloatingActionButton fabadd = findViewById(R.id.paramssubfabadd);
        fabOK = findViewById(R.id.editButton);
        FloatingActionButton fabcancel = findViewById(R.id.cancelButton);
        topicList = new ArrayList<>();
        builder = new MaterialAlertDialogBuilder(this);

        subHandler = new SubscriptionsHandler();

        final RecyclerView varList = findViewById(R.id.paramsVarList);

        varList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        varList.setLayoutManager(llm);

        paramsAutoscale = findViewById(R.id.paramsAutoScale);
        paramsMin = findViewById(R.id.paramsMin);
        paramsMinOffset = findViewById(R.id.paramsMinOffset);
        paramsMax = findViewById(R.id.paramsMax);
        paramsMaxOffset = findViewById(R.id.paramsMaxOffset);
        paramsRound = findViewById(R.id.paramsRound);
        paramsFormat = findViewById(R.id.paramsFormat);
        paramsUnit = findViewById(R.id.paramsUnit);
        paramsXDesc = findViewById(R.id.paramsDesc);
        paramsComment = findViewById(R.id.paramsComment);
        scalelayout = findViewById(R.id.autoscalelayout);
        paramsXBounds = findViewById(R.id.paramsXBounds);
        paramsXSelector = findViewById(R.id.paramsXSelect);

        xBounds = new ArrayList<>();
        xBounds.add(getString(R.string.chart_days));
        xBounds.add(getString(R.string.chart_weeks));
        xBounds.add(getString(R.string.chart_months));
        xBounds.add(getString(R.string.chart_hours));

        ArrayAdapter<String> xBoundsAdapter = new ArrayAdapter<>(context ,android.R.layout.simple_spinner_item,xBounds);
        xBoundsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paramsXSelector.setAdapter(xBoundsAdapter);
        paramsXSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(DEBUG_TAG, "Click on XBfactor spinner: "+position);
                if(XBfactor!=position) onChartLineUpdated("XBfactor", true);
                XBfactor = paramsHandler.getXBfactor(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fabadd.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'add' button");
            sa.addChartLine();
        });

        fabOK.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'OK' button");
            if(paramsXDesc.getError() == null && paramsXBounds.getError() == null) {
                if(UpdateChartParams()) {
                    Intent result = new Intent();
                    result.putExtra("ACTION", editAction);
                    setResult(1, result);
                    finish();
                }
            }
        });

        fabcancel.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked the FAB 'Cancel' button");
            setResult(0, null);
            onBackPressed();
        });

        final ActionBar ab = getSupportActionBar();
        editAction = getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Activity started with action=" + editAction);
        if (editAction.equals("insert")) {
            List<ChartParams> params= new ArrayList<>();
            ChartParams newParams = new ChartParams();
            name = getIntent().getStringExtra("name");
            paramsHandler = new ChartParamsHandler(name);
            newParams.name=name;
            newParams.pos=99;
            sort=getIntent().getIntExtra("sort",0)+10;
            newParams.sort=sort;
            paramsMin.setText("0.0");
            paramsMax.setText("0.0");
            paramsMinOffset.setText("0.0");
            paramsMaxOffset.setText("0.0");
            newParams.minValue=0.0F;
            newParams.maxValue=0.0F;
            newParams.minOffset=0.0F;
            newParams.maxOffset=0.0F;
            newParams.id = 0;
            newParams.error=false;
            newParams.XvarDesc="";
            params.add(newParams);
            fabOK.hide();
            fabcancel.show();
            assert ab != null;
            ab.setTitle("New ChartParams");
            sa = new ChartParamsEditAdapter(params);
            sa.setOnItemClickListener(this);
            varList.setAdapter(sa);
        }
        if (editAction.equals("edit")) {
            fabOK.hide();
            fabcancel.show();
            fabadd.show();
            sort = getIntent().getIntExtra("sort",0);
            name = getIntent().getStringExtra("name");
            pos = getIntent().getIntExtra("pos",0);
            paramsHandler = new ChartParamsHandler(name);
            params= new ArrayList<>(paramsHandler.getChartParamsLines(name,sort));
            fillParamsValues(params.get(0));
            assert ab != null;
            ab.setTitle(paramsHandler.getDescription(pos));
            sa = new ChartParamsEditAdapter(params);
            sa.setOnItemClickListener(this);
            varList.setAdapter(sa);
        }

        paramsXDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    fabOK.show();
                    unsaved=true;
                } else {
                    paramsXDesc.setError(context.getString(R.string.chartparams_XDescError));
                    fabOK.hide();
                }
            }
        });

        paramsComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                fabOK.show();
                unsaved=true;
            }
        });

        paramsAutoscale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            unsaved=true;
            fabOK.show();
            if(isChecked)
            {
                scalelayout.setVisibility(View.GONE);
            }
            else
            {
                scalelayout.setVisibility(View.VISIBLE);
            }
        });

        paramsXBounds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    unsaved=true;
                    fabOK.show();
                } else {
                    paramsXBounds.setError(context.getString(R.string.chartparams_XBoundsError));
                    fabOK.hide();
                }
            }
        });

        paramsMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsaved=true;
                fabOK.show();
            }
        });

        paramsMinOffset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsaved=true;
                fabOK.show();
            }
        });

        paramsMax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsaved=true;
                fabOK.show();
            }
        });

        paramsMaxOffset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsaved=true;
                fabOK.show();
            }
        });

        paramsRound.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsaved=true;
                fabOK.show();
            }
        });

        paramsFormat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                unsaved=true;
                fabOK.show();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        paramsUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsaved=true;
                fabOK.show();
            }
        });
    }

    private void fillParamsValues(ChartParams mparams)
    {
        paramsXDesc.setText(mparams.XDesc);
        paramsComment.setText(mparams.comment);
        paramsXBounds.setText(String.valueOf(mparams.XBounds));
        XBfactor = paramsHandler.getXBfactorIndex(mparams.XBfactor);
        paramsXSelector.setSelection(XBfactor);
        paramsMin.setText(String.valueOf(mparams.minValue));
        paramsMinOffset.setText(String.valueOf(mparams.minOffset));
        paramsMax.setText(String.valueOf(mparams.maxValue));
        paramsMaxOffset.setText(String.valueOf(mparams.maxOffset));
        if(mparams.autoscale)
        {
            paramsAutoscale.setChecked(true);
            scalelayout.setVisibility(View.GONE);
        }
        else
        {
            paramsAutoscale.setChecked(false);
            scalelayout.setVisibility(View.VISIBLE);
        }
        paramsRound.setText(String.valueOf(mparams.roundDec));
        paramsFormat.setText(mparams.YFormat);
        paramsUnit.setText(mparams.YUnit);
    }

    private boolean UpdateChartParams()
    {
        List<ChartParams> params = new ArrayList<>(sa.getChartLines());
        for(ChartParams param : params)
        {
            param.name= name;
            param.sort=sort;
            if(paramsXDesc.getText().toString().isEmpty())
            {
                paramsXDesc.setError(getString(R.string.chartparams_XDescError));
                return(false);
            }
            if(paramsXBounds.getText().toString().isEmpty())
            {
                paramsXBounds.setError(getString(R.string.chartparams_XBoundsError));
                return(false);
            }
            param.XDesc = paramsXDesc.getText().toString();
            if(param.Xvar==null || param.XvarDesc.isEmpty())
            {
                param.error=true;
                sa.notifyItemChanged(pos);
                return(false);
            }
            param.comment = paramsComment.getText().toString();
            param.XBounds = Long.parseLong(paramsXBounds.getText().toString());
            param.XBfactor = XBfactor;
            param.autoscale = paramsAutoscale.isChecked();
            param.minValue = Float.parseFloat(paramsMin.getText().toString());
            param.maxValue = Float.parseFloat(paramsMax.getText().toString());
            param.minOffset = Float.parseFloat(paramsMinOffset.getText().toString());
            param.maxOffset = Float.parseFloat(paramsMaxOffset.getText().toString());
            param.YFormat = paramsFormat.getText().toString();
            param.YUnit = paramsUnit.getText().toString();
            if(paramsRound.getText().toString().isEmpty())
            {
                param.roundDec=0;
            }
            else {
                param.roundDec = Integer.parseInt(paramsRound.getText().toString());
            }
        }
        paramsHandler.saveParams(params);
        paramsHandler.deleteParams(sa.getDeletedLines());
        unsaved=false;
        return(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getTempEvent(SensorDataEvent event) {
        String myTopic=event.getTopicString();
        if (sa.getTopicList().contains(myTopic)) {
            Log.i(DEBUG_TAG, "Topic arrived: " + myTopic);
            if(!sa.checkVarList(myTopic)) {
                sa.updateChartLine(myTopic);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(DEBUG_TAG, "OnStart()...");
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(DEBUG_TAG, "OnStop()...");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onChartLineUpdated(String var, boolean ok) {
        Log.i(DEBUG_TAG, "Chart line changed at position: " + var);
        unsaved=true;
        fabOK.show();
        if(!ok) fabOK.hide();
    }

    @Override
    public void onBackPressed() {
        Log.i(DEBUG_TAG, "OnBackPressed()");
        setResult(0, null);
        if (unsaved) {
            builder.setTitle(getString(R.string.NotSaved_title));
            builder.setMessage(getString(R.string.NotSaved_text));
            builder.setNegativeButton(getString(R.string.NotSaved_button), (dialog, which) -> {
                Log.i(DEBUG_TAG, "OK pressed!");
                ChartParamsEditActivity.super.onBackPressed();
            });
            builder.setPositiveButton(getString(R.string.NotSaved_cancel),null);
            builder.show();
        }
        else
        {
            ChartParamsEditActivity.super.onBackPressed();
        }
    }
}