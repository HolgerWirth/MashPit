package com.holger.mashpit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.model.Sensors;

import java.util.Arrays;
import java.util.List;

class SensorDevAdapter extends RecyclerView.Adapter<SensorDevAdapter.SensorDevViewHolder> {

    private static final String DEBUG_TAG = "SensorDevAdapter";
    private Context context;
    private List<Sensors> devList;
    private List<String> sensor_list;
    private boolean online=false;

    SensorDevAdapter(List<Sensors> devList) {
        this.devList = devList;
        if(devList != null) Log.i(DEBUG_TAG, "Sensor devices: "+this.devList.size());
    }

    @Override
    public void onBindViewHolder(@NonNull SensorDevViewHolder sensorDevViewHolder, int i) {
        Sensors devStatus = this.devList.get(i);
        if (devStatus.name.isEmpty()) {
            sensorDevViewHolder.devName.setText(devStatus.sensor);
        } else {
            sensorDevViewHolder.devName.setText(devStatus.name);
        }

        if (devStatus.active) {
            sensorDevViewHolder.devStatus.setText(context.getString(R.string.MPProcActiveYes));
            sensorDevViewHolder.devStatus.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            sensorDevViewHolder.devStatus.setText(context.getString(R.string.MPProcActiveNo));
            sensorDevViewHolder.devStatus.setBackgroundResource(android.R.color.holo_red_light);
        }

        switch (devStatus.type) {
            case ("ds18b20"):
                sensorDevViewHolder.devType.setText("DS18B20");
                if (online) {
                    if (!sensor_list.contains(devStatus.sensor)) {
                        sensorDevViewHolder.devStatus.setText(context.getString(R.string.devNotConnected));
                        sensorDevViewHolder.devStatus.setBackgroundResource(android.R.color.holo_red_light);
                    }
                }
                break;
            case ("bme280"):
                sensorDevViewHolder.devType.setText("BME280");
                break;
            case ("dht11"):
                sensorDevViewHolder.devType.setText("DHT11");
                break;
            default:
                sensorDevViewHolder.devType.setText("");
                break;
        }
    }

    public void setSensors(String sensors)
    {
        this.sensor_list = Arrays.asList(sensors.split("/"));
    }

    void setOnline(boolean online)
    {
        this.online=online;
    }

    @NonNull
    @Override
    public SensorDevViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.devlistcard, viewGroup, false);
        context=viewGroup.getContext();
        return new SensorDevViewHolder(itemView);
    }

    public Sensors getItem(int index) {
        return this.devList.get(index);
    }

    @Override
    public int getItemCount() {
        if(devList == null)
        {
            return 0;
        }
        return devList.size();
    }

    class SensorDevViewHolder extends RecyclerView.ViewHolder {
        TextView devName;
        TextView devStatus;
        TextView devType;
        CardView devCardView;

        SensorDevViewHolder(final View v) {
            super(v);
            devName = v.findViewById(R.id.devName);
            devStatus = v.findViewById(R.id.devStatus);
            devType = v.findViewById(R.id.devType);

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