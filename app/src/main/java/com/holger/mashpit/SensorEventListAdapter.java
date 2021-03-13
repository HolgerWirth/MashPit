package com.holger.mashpit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.tools.SensorPublishMQTT;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class SensorEventListAdapter extends RecyclerView.Adapter<SensorEventListAdapter.SensorMCPViewHolder> {

    private static final String DEBUG_TAG = "SensorEventListAdapter";
    private Context context;
    private List<Sensors> eventList;


    SensorEventListAdapter(List<Sensors> eventList) {
        this.eventList = eventList;
        if (eventList != null) Log.i(DEBUG_TAG, "MCP devices: " + this.eventList.size());
    }

    void setEventList(List<Sensors> eventList) {
        if (eventList.size() == 0) {
            this.eventList = null;
        } else {
            this.eventList.removeAll(eventList);
            this.eventList.addAll(eventList);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull SensorMCPViewHolder sensorMCPViewHolder, int i) {
        Sensors devStatus = this.eventList.get(i);
        Resources res = context.getResources();

        if (devStatus.name.isEmpty()) {
            sensorMCPViewHolder.devName.setText(devStatus.sensor);
        } else {
            sensorMCPViewHolder.devName.setText(devStatus.name);
        }

        if (devStatus.active) {
            sensorMCPViewHolder.devStatus.setText(context.getString(R.string.MPProcActiveYes));
            sensorMCPViewHolder.devStatus.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            sensorMCPViewHolder.devStatus.setText(context.getString(R.string.MPProcActiveNo));
            sensorMCPViewHolder.devStatus.setBackgroundResource(android.R.color.holo_red_light);
        }

        int resID;
        if (devStatus.family.equals("EV")) {
            sensorMCPViewHolder.devGPIO.setText("GPIO: " + Integer.toString(devStatus.port));
            sensorMCPViewHolder.devName.setText(devStatus.event);
            sensorMCPViewHolder.devTypeTitle.setText("Event:");

            switch (devStatus.dir) {
                case ("IN"):
                    sensorMCPViewHolder.devType.setText("Inbound");
                    sensorMCPViewHolder.devEventType.setText(devStatus.type);
                    resID = res.getIdentifier("ic_event_in_icon", "drawable", "com.holger.mashpit");
                    sensorMCPViewHolder.devIcon.setImageResource(resID);
                    break;

                case ("OUT"):
                    sensorMCPViewHolder.devType.setText("Outbound");
                    resID = res.getIdentifier("ic_event_out_icon", "drawable", "com.holger.mashpit");
                    sensorMCPViewHolder.devIcon.setImageResource(resID);
                    break;
            }
        }
    }

    @NonNull
    @Override
    public SensorMCPViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.devlistcard, viewGroup, false);
        context=viewGroup.getContext();
        return new SensorMCPViewHolder(itemView);
    }

    public Sensors getItem(int index) {
        return this.eventList.get(index);
    }

    @Override
    public int getItemCount() {
        if(eventList == null)
        {
            return 0;
        }
        return eventList.size();
    }

    static class SensorMCPViewHolder extends RecyclerView.ViewHolder {
        TextView devName;
        TextView devStatus;
        TextView devType;
        ImageView devIcon;
        CardView devCardView;
        TextView devTypeTitle;
        TextView devGPIO;
        TextView devEventType;
        SensorMCPViewHolder(final View v) {
            super(v);
            devName = v.findViewById(R.id.devName);
            devStatus = v.findViewById(R.id.devStatus);
            devType = v.findViewById(R.id.devType);
            devIcon = v.findViewById(R.id.device_icon);
            devTypeTitle = v.findViewById(R.id.devTypeTitle);
            devGPIO = v.findViewById(R.id.devEventGPIO);
            devEventType = v.findViewById(R.id.devEventType);
            devCardView = v.findViewById(R.id.devcard_view);

           v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "ViewHolder: onClick()");
                    devCardView.setPressed(true);
                }
            });

        }
    }
}