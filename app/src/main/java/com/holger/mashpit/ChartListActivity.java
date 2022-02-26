package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Charts;
import com.holger.mashpit.model.ChartsHandler;
import com.holger.mashpit.tools.ItemClickSupport;
import com.holger.mashpit.model.SubscriptionsHandler;
import com.holger.share.Constants;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    int chartPos=0;
    SubscriptionsHandler subscriptionsHandler;
    ChartsHandler chartsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartlist);

        subscriptionsHandler = new SubscriptionsHandler();
        chartsHandler = new ChartsHandler();
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

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1) {
                        charts=refreshCharts();
                        sa.refreshCharts(charts);
                        if(result.getData() != null && result.getData().getStringExtra("ACTION").equals("insert"))
                        {
                            Log.i(DEBUG_TAG, "Chartlist: chart added");
                            sa.notifyItemRangeChanged(0,charts.size());
                        }
                        else {
                            Log.i(DEBUG_TAG, "Chartlist changed");
                            sa.notifyItemChanged(chartPos);
                        }
                    }
                });

        FloatingActionButton fab = findViewById(R.id.chartfabadd);
        fab.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked on FAB");
            Intent l = new Intent(getApplicationContext(), ChartEditActivity.class);
            l.putExtra("ACTION", "insert");
            chartPos=charts.size()+1;
            myActivityResultLauncher.launch(l);
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
            Log.i(DEBUG_TAG, "Clicked on pos: "+position);
            chartPos=position;
            Intent sintent = new Intent(getApplicationContext(), ChartEditActivity.class);
            sintent.putExtra("ACTION", "edit");
            sintent.putExtra("name",charts.get(position).name);
            sintent.putExtra("desc",charts.get(position).description);
            sintent.putExtra("id",charts.get(position).id);
            myActivityResultLauncher.launch(sintent);
        });
    }

    private List<Charts> refreshCharts() {
        return chartsHandler.getallCharts();
    }

    @Override
    public void onChartDeleted(final int pos) {
        Log.i(DEBUG_TAG, "Clicked on delete on chart: "+charts.get(pos).name);
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.sub_delete_text);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            chartsHandler.deleteChart(charts.get(pos));
            subscriptionsHandler.setDeletedSubscriptions("Chart",charts.get(pos).name);
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
            charts=refreshCharts();
            sa.refreshCharts(charts);
            sa.notifyItemRemoved(pos);
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();

    }
}