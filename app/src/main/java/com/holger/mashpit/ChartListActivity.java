package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Charts;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.share.Constants;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ChartListActivity extends AppCompatActivity implements ChartListAdapter.DeleteChartCallback {

    private static final String DEBUG_TAG = "ChartListActivity";
    ChartListAdapter sa;
    List<Charts> charts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartlist);

        Toolbar toolbar = findViewById(R.id.chartList_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            finish();
        });
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Charts");

        Log.i(DEBUG_TAG, "Activity started!");

        FloatingActionButton fab = findViewById(R.id.chartfabadd);
        fab.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked on FAB");
            Intent l = new Intent(getApplicationContext(), ChartEditActivity.class);
            l.putExtra("ACTION", "insert");
            startActivityForResult(l, 0);
        });

        final RecyclerView chartList = findViewById(R.id.chartList);
        chartList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        chartList.setLayoutManager(llm);

        charts=refreshCharts();
        sa = new ChartListAdapter(charts);
        sa.setOnItemClickListener(this);
        chartList.setAdapter(sa);

        ItemClickSupport.addTo(chartList).setOnItemClickListener((recyclerView, position, v) -> {
            Log.i(DEBUG_TAG, "Clicked!");
            Intent sintent = new Intent(getApplicationContext(), ChartEditActivity.class);
            sintent.putExtra("ACTION", "edit");
            sintent.putExtra("name",charts.get(position).name);
            sintent.putExtra("desc",charts.get(position).description);
            startActivityForResult(sintent, 0);
        });
    }

    private List<Charts> refreshCharts() {
        List<Charts> charts = new ArrayList<>();
        List<Charts> dbresult = new Select().from(Charts.class).orderBy("name ASC").execute();
        for (Charts chart : dbresult) {
            chart.id = chart.getId();
            charts.add(chart);
        }
        return charts;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "Chartlist changed");
        if(resultCode==1)
        {
            charts=refreshCharts();
            sa.refreshCharts(charts);
            sa.notifyDataSetChanged();
        }
    }

        @Override
    public void onChartDeleted(final String name) {
        Log.i(DEBUG_TAG, "Clicked on delete on chart: "+name);
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            new Delete().from(Charts.class).where("name = ?", name).execute();
            if(new Select().from(Subscriptions.class).where("action = ?","Chart").and("name = ?",name).and("deleted = ?",0).exists())
            {
                new Update(Subscriptions.class)
                        .set("deleted = ?", 1)
                        .where("action = ? and name = ?", "Chart", name)
                        .execute();

                builder.setTitle(getString(R.string.Subchanged_alert_title));
                builder.setMessage(getString(R.string.Subchanged_text));
                builder.setPositiveButton(getString(R.string.Subchanged_button), (dialog1, which1) -> {
                    Log.i(DEBUG_TAG, "Reconnect pressed!");
                    Log.i(DEBUG_TAG, "Stop service!");
                    Intent serviceIntent = new Intent(getApplicationContext(), TemperatureService.class);
                    serviceIntent.setAction(Constants.ACTION.RESTART_ACTION);
                    getApplicationContext().startService(serviceIntent);
                });
                builder.setNegativeButton(getString(R.string.Subchanged_cancel), null);
                builder.show();
            }
            charts=refreshCharts();
            sa.refreshCharts(charts);
            sa.notifyDataSetChanged();
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();

    }
}