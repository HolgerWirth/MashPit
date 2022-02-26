package com.holger.mashpit;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.model.ChartDataHandler;
import com.holger.mashpit.model.ChartParams;
import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.model.SubscriptionsHandler;
import com.holger.mashpit.tools.ColorSpinnerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ChartParamsEditAdapter extends RecyclerView.Adapter<ChartParamsEditAdapter.ChartParamsEditViewHolder> {
    private static final String DEBUG_TAG = "ChartParamsEdtAdapter";
    private UpdateChartCallback updateChartCallback;

    List<ChartParams> params;
    List<String> subList;
    List<String> topicList;
    List<String> varList;
    List<Integer> linecolor;
    List<Long> delList;
    private Context context;
    ChartDataHandler dataHandler = new ChartDataHandler();

    ChartParamsEditAdapter(List<ChartParams> mparam) {
        this.params=new ArrayList<>(mparam);
        SubscriptionsHandler subHandler = new SubscriptionsHandler();
        List<Subscriptions> subs = new ArrayList<>(subHandler.getActiveSubscriptions("Chart", params.get(0).name));
        createSubList(subs);
        linecolor = new ArrayList<>();
        linecolor.add(Color.BLACK);
        linecolor.add(Color.RED);
        linecolor.add(Color.BLUE);
        linecolor.add(Color.YELLOW);
        linecolor.add(Color.CYAN);
        linecolor.add(Color.GREEN);
        linecolor.add(Color.MAGENTA);
        linecolor.add(Color.GRAY);

        delList = new ArrayList<>();
    }

    public List<ChartParams> getParams()
    {
        return(params);
    }

    public void addChartLine()
    {
        ChartParams tparams = new ChartParams();
        tparams.id=0;
        tparams.error=false;
        tparams.XvarDesc="";
        params.add(tparams);
        notifyItemInserted(params.size()-1);
    }

    private void createSubList(List<Subscriptions> subs)
    {
        subList = new ArrayList<>();
        topicList = new ArrayList<>();
        for(Subscriptions msubs : subs) {
            subList.add(msubs.aliasServer + " | "+msubs.aliasSensor+" | "+msubs.interval);
            topicList.add(msubs.topic);
        }
    }

    public List<String> getTopicList()
    {
        return topicList;
    }

    public boolean checkVarList(String topic)
    {
        return dataHandler.getChartVars(topic).length != 0;
    }

    public void updateChartLine(String topic)
    {
        for(int i=0;i<params.size();i++)
        {
            if(params.get(i).topic.equals(topic))
            {
                Log.i(DEBUG_TAG, "Chart line "+i+" updated!");
                notifyItemChanged(i);
            }
        }
    }

    public List<Long> getDeletedLines()
    {
        return(delList);
    }

    public List<ChartParams> getChartLines()
    {
        return(params);
    }

    @Override
    public int getItemCount() {
        return params.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ChartParamsEditViewHolder chartParamEditViewHolder, int i) {
        if(params.get(i).error) {
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000);
            anim.setStartOffset(20);
            chartParamEditViewHolder.mCardView.startAnimation(anim);
            chartParamEditViewHolder.desc.setError(context.getString(R.string.confNameError));
        }
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(context ,android.R.layout.simple_spinner_item,subList);
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chartParamEditViewHolder.topic.setAdapter(subAdapter);
            final int topicPos = Math.max(topicList.indexOf(params.get(i).topic), 0);
            chartParamEditViewHolder.topic.setSelection(topicPos);
            chartParamEditViewHolder.topic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(DEBUG_TAG, "ViewHolder: Click on topic spinner: " + position);
                    if (topicPos != position) updateChartCallback.onChartLineUpdated("topic", true);
                    try {
                        varList = new ArrayList<>(Arrays.asList(dataHandler.getChartVars(topicList.get(position))));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        varList = new ArrayList<>(Arrays.asList(dataHandler.getChartVars(topicList.get(0))));
                    }
                    if (!varList.isEmpty()) {
                        ArrayAdapter<String> varAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, varList);
                        final int varPos = Math.max(varList.indexOf(params.get(chartParamEditViewHolder.getAbsoluteAdapterPosition()).Xvar), 0);
                        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        chartParamEditViewHolder.var.setAdapter(varAdapter);
                        chartParamEditViewHolder.var.setSelection(varPos);
                        chartParamEditViewHolder.var.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Log.i(DEBUG_TAG, "ViewHolder: Click on var spinner: " + position);
                                params.get(chartParamEditViewHolder.getAbsoluteAdapterPosition()).Xvar = varList.get(position);
                                if (varPos != position)
                                    updateChartCallback.onChartLineUpdated("Xvar", true);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                    else
                    {
                        updateChartCallback.onChartLineUpdated("Xvar", false);
                        params.get(chartParamEditViewHolder.getAbsoluteAdapterPosition()).Xvar = null;
                    }
                    params.get(chartParamEditViewHolder.getAbsoluteAdapterPosition()).topic = topicList.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.i(DEBUG_TAG, "ViewHolder: Nothing selected!");
                }
            });

        ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(context ,linecolor);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chartParamEditViewHolder.color.setAdapter(colorAdapter);
        final int colPos=Math.max(linecolor.indexOf(params.get(i).color),0);
        chartParamEditViewHolder.color.setSelection(colPos);
        chartParamEditViewHolder.color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(DEBUG_TAG, "ViewHolder: Click on color spinner: "+position);
                params.get(chartParamEditViewHolder.getAbsoluteAdapterPosition()).color = linecolor.get(position);
                if(colPos!=position) updateChartCallback.onChartLineUpdated("color",true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chartParamEditViewHolder.desc.setText(params.get(i).XvarDesc);
        chartParamEditViewHolder.desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    chartParamEditViewHolder.desc.setError(context.getString(R.string.confNameError));
                    updateChartCallback.onChartLineUpdated("desc",false);
                } else {
                    updateChartCallback.onChartLineUpdated("desc",true);
                    params.get(chartParamEditViewHolder.getAbsoluteAdapterPosition()).XvarDesc=chartParamEditViewHolder.desc.getText().toString();
                }
            }
        });
        chartParamEditViewHolder.deleteButton.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public ChartParamsEditViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.chartparamsvarlistcard, viewGroup, false);
        context = viewGroup.getContext();
        return new ChartParamsEditViewHolder(itemView);
    }

    class ChartParamsEditViewHolder extends RecyclerView.ViewHolder {
        Spinner topic;
        Spinner var;
        Spinner color;
        TextView desc;
        ImageButton deleteButton;
        CardView mCardView;
        int pos;

        ChartParamsEditViewHolder(final View v) {
            super(v);
            topic = v.findViewById(R.id.varTopic);
            color = v.findViewById(R.id.varColor);
            var = v.findViewById(R.id.varVar);
            desc = v.findViewById(R.id.varDesc);

            mCardView = v.findViewById(R.id.chartparamsvarlistcard_view);
            deleteButton=v.findViewById(R.id.varDeleteButton);
            deleteButton.setOnClickListener(view -> {
               Log.i(DEBUG_TAG, "ViewHolder: Click on delete: "+pos);
               deleteButton.setPressed(true);
               deleteChartLine(getAbsoluteAdapterPosition());
           });
        }
    }

    public void deleteChartLine(int pos)
    {
        if(params.get(pos).id!=0)
        {
            Log.i(DEBUG_TAG, "Chart line is in DB "+pos);
            delList.add(params.get(pos).id);
        }
        params.remove(pos);
        notifyItemRemoved(pos);
        updateChartCallback.onChartLineUpdated("ChartLine",true);
    }

    public interface UpdateChartCallback {
        void onChartLineUpdated(String var, boolean ok);
    }

    void setOnItemClickListener(ChartParamsEditAdapter.UpdateChartCallback callback){
        updateChartCallback = callback;
    }
}