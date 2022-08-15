package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.ChartParams;
import com.holger.mashpit.model.ChartParamsHandler;
import com.holger.mashpit.tools.ItemClickSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartParamsListActivity extends AppCompatActivity implements ChartParamsListAdapter.DeleteChartCallback {

    private static final String DEBUG_TAG = "ChartParamsListActivity";
    ChartParamsListAdapter sa;
    List<ChartParams> params = new ArrayList<>();
    List<ChartParams> mparams = new ArrayList<>();
    ChartParamsHandler paramsHandler;
    boolean dragged = false;
    boolean moved = false;
    String name;
    int chartListPos;
    boolean listUpdated=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartparamslist);

        Intent intent = getIntent();
        name = intent.getStringExtra("NAME");
        paramsHandler = new ChartParamsHandler(name);
        Toolbar toolbar = findViewById(R.id.paramsList_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            setResult(0,null);
            if(listUpdated) setResult(2,null);
            finish();
        });

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Charts Parameter");

        Log.i(DEBUG_TAG, "Activity started for " + name);

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    listUpdated=false;
                    if (result.getResultCode() == 1) {
                        listUpdated=true;
                        params = paramsHandler.updateParams(name);
                        assert result.getData() != null;
                        if (result.getData().getStringExtra("ACTION").equals("insert")) {
                            Log.i(DEBUG_TAG, "Chartlist added");
                            paramsHandler.saveParamPos(params);
                            sa.refreshCharts(params);
                            sa.notifyItemInserted(params.size()-1);
                        } else {
                            Log.i(DEBUG_TAG, "Chartlist changed");
                            sa.refreshCharts(params);
                            sa.notifyItemChanged(chartListPos);
                        }
                    }
                });

        FloatingActionButton fab = findViewById(R.id.paramsfabadd);
        fab.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked on ADD");
            Intent sintent = new Intent(getApplicationContext(), ChartParamsEditActivity.class);
            sintent.putExtra("ACTION", "insert");
            sintent.putExtra("name", name);
            sintent.putExtra("sort", sa.getMaxSort());
            sintent.putExtra("pos",0);
            myActivityResultLauncher.launch(sintent);
        });

        final RecyclerView chartList = findViewById(R.id.paramsList);
        chartList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        chartList.setLayoutManager(llm);

        params = refreshCharts();
        mparams = new ArrayList<>(params);
        sa = new ChartParamsListAdapter(params);
        sa.setOnItemClickListener(this);
        chartList.setAdapter(sa);

        ItemClickSupport.addTo(chartList).setOnItemClickListener((recyclerView, position, v) -> {
            Log.i(DEBUG_TAG, "Clicked on pos: " + position);
            chartListPos = position;
            Intent sintent = new Intent(getApplicationContext(), ChartParamsEditActivity.class);
            sintent.putExtra("ACTION", "edit");
            sintent.putExtra("name", name);
            sintent.putExtra("sort", params.get(position).sort);
            sintent.putExtra("pos",chartListPos);
            myActivityResultLauncher.launch(sintent);
        });

        ItemTouchHelper.Callback touchCallback = new ItemTouchHelper.Callback() {
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                final int toPosition = target.getAbsoluteAdapterPosition();
                Log.i(DEBUG_TAG, "OnMove() from: " + fromPosition + " to " + toPosition);
                Collections.swap(mparams, viewHolder.getAbsoluteAdapterPosition(), target.getAbsoluteAdapterPosition());
                sa.notifyItemMoved(viewHolder.getAbsoluteAdapterPosition(), target.getAbsoluteAdapterPosition());
                return true;
            }

            @Override
            public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                moved = true;
                Log.i(DEBUG_TAG, "OnMoved()");
            }

            @Override
            public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                Log.i(DEBUG_TAG, "OnSelectedChanged(); " + dragged);
                if (dragged) {
                    if (moved) {
                        Log.i(DEBUG_TAG, "Position changed: " + moved);
                        listUpdated=true;
                        paramsHandler.saveParamPos(mparams);
                        params = mparams;
                        moved = false;
                    }
                    sa.refreshCharts(mparams);
                }
                dragged = !dragged;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.i(DEBUG_TAG, "OnSwipe()");
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }
        };

        ItemTouchHelper touch = new ItemTouchHelper(touchCallback);
        touch.attachToRecyclerView(chartList);
    }

    private List<ChartParams> refreshCharts() {
        return paramsHandler.getParams();
    }

    @Override
    public void onChartDeleted(final int pos) {
        Log.i(DEBUG_TAG, "Clicked on delete on chart: " + pos);
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.sub_delete);
        builder.setMessage(R.string.chartparams_delete);
        builder.setPositiveButton(getString(R.string.delete_key), (dialog, which) -> {
            paramsHandler.deleteParam(pos);
            sa.notifyItemRemoved(pos);
            params = refreshCharts();
            sa.refreshCharts(params);
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Log.i(DEBUG_TAG, "OnBackPressed()");
        setResult(0,null);
        if(listUpdated) setResult(2,null);
        super.onBackPressed();
    }
}