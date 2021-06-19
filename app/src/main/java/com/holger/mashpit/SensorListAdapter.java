package com.holger.mashpit;

import android.content.Context;
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

import java.util.Arrays;
import java.util.List;

class SensorListAdapter extends RecyclerView.Adapter<SensorListAdapter.SensorDevViewHolder> {

    private static final String DEBUG_TAG = "SensorListAdapter";
    private Context context;
    private final List<Sensors> devList;
    private List<String> sensor_list;
    private boolean online=false;

    SensorListAdapter(List<Sensors> devList) {
        this.devList = devList;
        if(devList != null) Log.i(DEBUG_TAG, "Sensor devices: "+this.devList.size());
    }

    @Override
    public void onBindViewHolder(@NonNull SensorDevViewHolder sensorDevViewHolder, int i) {
        Sensors devStatus = this.devList.get(i);
        Resources res = context.getResources();

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

        int resID;
        if(devStatus.family.equals("SE")) {
            switch (devStatus.type) {
                case ("ds18b20"):
                    sensorDevViewHolder.devType.setText("DS18B20");
                    if (online) {
                        if (!sensor_list.contains(devStatus.sensor)) {
                            sensorDevViewHolder.devStatus.setText(context.getString(R.string.devNotConnected));
                            sensorDevViewHolder.devStatus.setBackgroundResource(android.R.color.holo_red_light);
                        }
                    }
                    resID = res.getIdentifier("ic_temperature_sensor_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;

                case ("bme280"):
                    sensorDevViewHolder.devType.setText("BME280");
                    resID = res.getIdentifier("ic_bme_sensor_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;
                case ("dht11"):
                    sensorDevViewHolder.devType.setText("DHT11");
                    resID = res.getIdentifier("ic_bme_sensor_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;
                case ("bh1750"):
                    sensorDevViewHolder.devType.setText("BH1750");
                    resID = res.getIdentifier("ic_light_sensor_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;
                case ("mcp23017"):
                    sensorDevViewHolder.devType.setText("MCP23017");
                    resID = res.getIdentifier("ic_mcp_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;
                case ("ads1115"):
                    sensorDevViewHolder.devType.setText("ADS1115");
                    resID = res.getIdentifier("ic_adc_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;

                default:
                    sensorDevViewHolder.devType.setText("");
                    break;
            }
        }

        if(devStatus.family.equals("EV")) {
            sensorDevViewHolder.devGPIO.setText("GPIO: "+ devStatus.port);
            sensorDevViewHolder.devName.setText(devStatus.event);
            sensorDevViewHolder.devTypeTitle.setText("Event:");

            switch (devStatus.dir) {
                case ("IN"):
                    sensorDevViewHolder.devType.setText("Inbound");
                    sensorDevViewHolder.devEventType.setText(devStatus.type);
                    resID = res.getIdentifier("ic_event_in_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;

                case ("OUT"):
                    sensorDevViewHolder.devType.setText("Outbound");
                    resID = res.getIdentifier("ic_event_out_icon", "drawable", "com.holger.mashpit");
                    sensorDevViewHolder.devIcon.setImageResource(resID);
                    break;
            }
        }

        if(devStatus.family.equals("DISP")) {
            if (devStatus.type.equals("oled")) {
                sensorDevViewHolder.devType.setText("OLED");
                resID = res.getIdentifier("ic_oled_icon", "drawable", "com.holger.mashpit");
                sensorDevViewHolder.devIcon.setImageResource(resID);
            }
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

    static class SensorDevViewHolder extends RecyclerView.ViewHolder {
        TextView devName;
        TextView devStatus;
        TextView devType;
        ImageView devIcon;
        CardView devCardView;
        TextView devTypeTitle;
        TextView devGPIO;
        TextView devEventType;

        SensorDevViewHolder(final View v) {
            super(v);
            devName = v.findViewById(R.id.devName);
            devStatus = v.findViewById(R.id.devStatus);
            devType = v.findViewById(R.id.devType);
            devIcon = v.findViewById(R.id.device_icon);
            devTypeTitle = v.findViewById(R.id.devTypeTitle);
            devGPIO = v.findViewById(R.id.devEventGPIO);
            devEventType = v.findViewById(R.id.devEventType);
            devCardView = v.findViewById(R.id.devcard_view);

           v.setOnClickListener(view -> {
               Log.i(DEBUG_TAG, "ViewHolder: onClick()");
               devCardView.setPressed(true);
           });

        }
    }
}