package com.holger.mashpit;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.holger.mashpit.model.Config;

import java.util.List;

class ConfAdapter extends RecyclerView.Adapter<ConfAdapter.ConfViewHolder> {

    private static final String DEBUG_TAG = "ConfAdapter";

    private List<Config> confList;

    ConfAdapter(List<Config> confList) {
        this.confList = confList;
        if(confList != null) Log.i(DEBUG_TAG, "Config: "+this.confList.size());
    }

    @Override
    public int getItemCount() {
        if(confList == null)
        {
            return 0;
        }
        return confList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ConfViewHolder confViewHolder, int i) {
        Config conf = this.confList.get(i);
        confViewHolder.confConf.setText(conf.name);
        confViewHolder.confActive.setChecked(false);
        if(conf.active)
        {
            confViewHolder.confActive.setChecked(true);
        }
        confViewHolder.confActive.setClickable(false);
        confViewHolder.confTemp.setText(conf.temp);
        confViewHolder.confMinMax.setChecked(false);
        if(conf.minmax)
        {
            confViewHolder.confMinMax.setChecked(true);
        }
        confViewHolder.confMinMax.setClickable(false);
        confViewHolder.confTime.setText(conf.time);
        confViewHolder.confTopic.setText(conf.topic);
        confViewHolder.confHyst.setText(conf.hysterese);
    }

    @NonNull
    @Override
    public ConfViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.confcard, viewGroup, false);

        return new ConfViewHolder(itemView);
    }

    class ConfViewHolder extends RecyclerView.ViewHolder {
        TextView confConf;
        Switch confActive;
        TextView confTopic;
        TextView confTemp;
        TextView confTime;
        TextView confHyst;
        Switch confMinMax;

        CardView mCardView;

        ConfViewHolder(final View v) {
            super(v);
            confConf = v.findViewById(R.id.confConf);
            confActive = v.findViewById(R.id.confActive);
            confTopic = v.findViewById(R.id.confTopic);
            confTemp = v.findViewById(R.id.confTemp);
            confTime = v.findViewById(R.id.confTime);
            confMinMax = v.findViewById(R.id.confMaxTemp);
            confHyst = v.findViewById(R.id.confHyst);

            mCardView = v.findViewById(R.id.confcard_view);

           v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "ViewHolder: onClick()");
                    mCardView.setPressed(true);
                }
            });

        }
    }

    void addItem(Config dataObj) {
        confList.add(dataObj);
        notifyItemInserted(confList.size());
    }

    Config getItem(int index) {
        return confList.get(index);
    }

    void deleteItem(int index) {
        confList.remove(index);
        notifyItemRemoved(index);
    }

    void changeItem(int index, Config dataObj) {
        Config conf = confList.get(index);
        conf.name=dataObj.name;
        conf.topic=dataObj.topic;
        conf.active=dataObj.active;
        conf.hysterese=dataObj.hysterese;
        conf.temp=dataObj.temp;
        conf.minmax=dataObj.minmax;
        conf.time=dataObj.time;

        notifyItemChanged(index);
    }
}