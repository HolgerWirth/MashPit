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

import com.holger.mashpit.model.ChartParams;

import java.util.List;

class ChartParamsListAdapter extends RecyclerView.Adapter<ChartParamsListAdapter.ChartParamsListViewHolder> {
    private static final String DEBUG_TAG = "ChartListAdapter";
    private List<ChartParams> paramList;
    private DeleteChartCallback deleteChartCallback;

    ChartParamsListAdapter(List<ChartParams> paramList) {
        this.paramList = paramList;
        if(paramList != null) Log.i(DEBUG_TAG, "ChartParams: "+this.paramList.size());
    }

    void refreshCharts(List<ChartParams> paramList)
    {
        this.paramList = paramList;
        if(paramList != null) Log.i(DEBUG_TAG, "Refresh subscriptions: "+this.paramList.size());
    }

    public int getMaxSort()
    {
        int maxsort=0;
        for(ChartParams tp : paramList)
        {
            if(tp.sort>maxsort) maxsort=tp.sort;
        }
        return(maxsort);
    }

    @Override
    public int getItemCount() {
        if(paramList == null)
        {
            return 0;
        }
        return paramList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ChartParamsListViewHolder chartParamListViewHolder, int i) {
        ChartParams params = this.paramList.get(i);
        chartParamListViewHolder.pos = chartParamListViewHolder.getAbsoluteAdapterPosition();
        chartParamListViewHolder.name.setText(params.XDesc);
        chartParamListViewHolder.desc.setText(params.comment);
        chartParamListViewHolder.deleteButton.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public ChartParamsListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.chartparamslistcard, viewGroup, false);

        return new ChartParamsListViewHolder(itemView);
    }

    class ChartParamsListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView desc;
        ImageButton deleteButton;
        CardView mCardView;
        int pos;

        ChartParamsListViewHolder(final View v) {
            super(v);
            name = v.findViewById(R.id.chartParamsName);
            desc = v.findViewById(R.id.chartParamsDesc);
            mCardView = v.findViewById(R.id.chartparamslistcard_view);
            deleteButton=v.findViewById(R.id.chartParamsDeleteButton);

           deleteButton.setOnClickListener(view -> {
               Log.i(DEBUG_TAG, "ViewHolder: Click on delete: "+pos);
               deleteButton.setPressed(true);
               deleteChartCallback.onChartDeleted(pos);
           });
        }
    }

    public interface DeleteChartCallback {
        void onChartDeleted(int pos);
    }

    void setOnItemClickListener(ChartParamsListAdapter.DeleteChartCallback callback){
        deleteChartCallback = callback;
    }
}