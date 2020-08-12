package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Charts;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.tools.ItemClickSupport;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                finish();
            }
        });
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Charts");

        Log.i(DEBUG_TAG, "Activity started!");

        FloatingActionButton fab = findViewById(R.id.chartfabadd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(DEBUG_TAG, "Clicked on FAB");
                Intent l = new Intent(getApplicationContext(), ChartEditActivity.class);
                l.putExtra("ACTION", "insert");
                startActivityForResult(l, 0);
            }
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

        ItemClickSupport.addTo(chartList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.i(DEBUG_TAG, "Clicked!");
                Intent sintent = new Intent(getApplicationContext(), ChartEditActivity.class);
                sintent.putExtra("ACTION", "edit");
                sintent.putExtra("name",charts.get(position).name);
                sintent.putExtra("desc",charts.get(position).description);
                startActivityForResult(sintent, 0);
            }
        });
    }

    private List<Charts> refreshCharts() {
        List<Charts> dbresult;
        List<Subscriptions> subresult;
        List<Charts> charts = new ArrayList<>();
        dbresult = new Select().from(Charts.class).orderBy("name ASC").execute();
        for (Charts chart : dbresult) {
            chart.id = chart.getId();
            subresult=new Select().from(Subscriptions.class).where("action=?","Chart")
                    .and("name=?",chart.name).execute();
            chart.todelete=subresult.size();
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
    public void onChartDeleted(final long position) {
        Log.i(DEBUG_TAG, "Clicked on delete on position: "+position);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Delete().from(Charts.class).where("clientId = ?", position).execute();
                charts=refreshCharts();
                sa.refreshCharts(charts);
                sa.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }
}