package com.holger.mashpit;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.model.Charts;

import java.util.List;

class ChartListAdapter extends RecyclerView.Adapter<ChartListAdapter.ChartListViewHolder> {
    private static final String DEBUG_TAG = "ChartListAdapter";
    private List<Charts> chartList;
    private DeleteChartCallback deleteChartCallback;

    ChartListAdapter(List<Charts> chartList) {
        this.chartList = chartList;
        if(chartList != null) Log.i(DEBUG_TAG, "Charts: "+this.chartList.size());
    }

    void refreshCharts(List<Charts> chartList)
    {
        this.chartList = chartList;
        if(chartList != null) Log.i(DEBUG_TAG, "Refresh subscriptions: "+this.chartList.size());
    }

    @Override
    public int getItemCount() {
        if(chartList == null)
        {
            return 0;
        }
        return chartList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ChartListViewHolder chartListViewHolder, int i) {
        Charts chart = this.chartList.get(i);
        chartListViewHolder.name.setText(chart.name);
        chartListViewHolder.name.setTag(chart.name);
        chartListViewHolder.desc.setText(chart.description);
        chartListViewHolder.deleteButton.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public ChartListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.chartlistcard, viewGroup, false);

        return new ChartListViewHolder(itemView);
    }

    class ChartListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView desc;
        ImageButton deleteButton;
        CardView mCardView;

        ChartListViewHolder(final View v) {
            super(v);
            name = v.findViewById(R.id.chartName);
            desc = v.findViewById(R.id.chartDesc);
            mCardView = v.findViewById(R.id.chardlistcard_view);
            deleteButton=v.findViewById(R.id.chartDeleteButton);

           deleteButton.setOnClickListener(view -> {
               Log.i(DEBUG_TAG, "ViewHolder: Click on delete");
               deleteButton.setPressed(true);
               deleteChartCallback.onChartDeleted((String) name.getTag());
           });
        }
    }

    public interface DeleteChartCallback {
        void onChartDeleted(String name);
    }

    void setOnItemClickListener(ChartListAdapter.DeleteChartCallback callback){
        deleteChartCallback = callback;
    }
}