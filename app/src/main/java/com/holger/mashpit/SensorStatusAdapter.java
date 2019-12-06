package com.holger.mashpit;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.events.SensorEvent;

import java.util.List;

class SensorStatusAdapter extends RecyclerView.Adapter<SensorStatusAdapter.SensorStatusViewHolder> {

    private static final String DEBUG_TAG = "SensorStatusAdapter";

    private List<SensorEvent> statusList;

    SensorStatusAdapter(List<SensorEvent> statusList) {
        this.statusList = statusList;
        if(statusList != null) Log.i(DEBUG_TAG, "MPServer: "+this.statusList.size());
    }

    @Override
    public int getItemCount() {
        if(statusList == null)
        {
            return 0;
        }
        return statusList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SensorStatusViewHolder mpStatusViewHolder, int i) {
        SensorEvent SensorStatusEvent = this.getItem(i);
        if(SensorStatusEvent.getName().isEmpty()) {
            mpStatusViewHolder.sensorServer.setText(SensorStatusEvent.getServer());
        }
        else
        {
            mpStatusViewHolder.sensorServer.setText(SensorStatusEvent.getName());
        }
//        mpStatusViewHolder.mpstatProcesses.setText(String.valueOf(mpStatusEvent.getProcesses()));
//        mpStatusViewHolder.mpstatProcActive.setText(String.valueOf(mpStatusEvent.getActprocesses()));
    }

    @NonNull
    @Override
    public SensorStatusViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.sensorstatuscard, viewGroup, false);

        return new SensorStatusViewHolder(itemView);
    }

    private SensorEvent getItem(int position) {
        return statusList.get(position);
    }

    class SensorStatusViewHolder extends RecyclerView.ViewHolder {
        TextView sensorServer;
        TextView sensorSensor;
        TextView sensorStatus;
        CardView mCardView;

        SensorStatusViewHolder(final View v) {
            super(v);
            sensorServer = v.findViewById(R.id.sensorServer);
            sensorSensor = v.findViewById(R.id.sensorSensor);
            sensorStatus = v.findViewById(R.id.sensorStatus);

            mCardView = v.findViewById(R.id.sensorstatuscard_view);

           v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "ViewHolder: onClick()");
                    mCardView.setPressed(true);
                }
            });
        }
    }
}