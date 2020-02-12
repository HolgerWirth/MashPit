package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.model.Sensors;

import java.util.List;

class SensorConfEditAdapter extends RecyclerView.Adapter<SensorConfEditAdapter.IntervalViewHolder> {

    private static final String DEBUG_TAG = "SensorConfEditAdapter";
    private List<Sensors> intervalList;
    private IntervalChangeCallback listener;
    private Context context;
    private Sensors sensors;

    SensorConfEditAdapter(List<Sensors> intervalList) {
        this.intervalList = intervalList;
        if(intervalList != null) Log.i(DEBUG_TAG, "Intervals: "+this.intervalList.size());
        }

    void setIntervalList(List<Sensors> intervalList)
    {
        if(intervalList.size()==0)
        {
            this.intervalList=null;
        }
        else {
            this.intervalList.removeAll(intervalList);
            this.intervalList.addAll(intervalList);
        }
    }

    @Override
    public int getItemCount() {
        if(intervalList == null)
        {
            return 0;
        }
        return this.intervalList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final IntervalViewHolder intervalViewHolder, int i) {
        sensors = this.getItem(i);
        intervalViewHolder.sensorIntervalView.setTag(i);
        intervalViewHolder.sensorIntervalView.setText(Integer.toString(sensors.interval));

        if(sensors.active)
        {
            intervalViewHolder.sensorIntervalActive.setChecked(true);
            intervalViewHolder.deleteButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            intervalViewHolder.sensorIntervalActive.setChecked(false);
            intervalViewHolder.deleteButton.setVisibility(View.VISIBLE);
        }

        if(intervalViewHolder.sensorIntervalView.getText().toString().equals("0"))
        {
            sensors.interval=0;
            sensors.active=false;
            intervalViewHolder.sensorIntervalView.setVisibility(View.GONE);
            intervalViewHolder.sensorInterval.setVisibility(View.VISIBLE);
            intervalViewHolder.sensorInterval.setEnabled(true);
            intervalViewHolder.sensorInterval.requestFocus();
            intervalViewHolder.deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public IntervalViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.intervallistcard, viewGroup, false);
        context = viewGroup.getContext();
        return new IntervalViewHolder(itemView);
    }

    public interface IntervalChangeCallback{
        void onIntervalDeleted(int position);
        void onIntervalCreated(int interval, boolean active);
        void onIntervalActivated(int position, boolean active);
    }

    void setOnItemClickListener(IntervalChangeCallback callback){
        listener = callback;
    }

    public Sensors getItem(int position) {
        return intervalList.get(position);
    }

    class IntervalViewHolder extends RecyclerView.ViewHolder {
        TextView sensorIntervalView;
        EditText sensorInterval;
        Switch sensorIntervalActive;
        CardView mCardView;
        ImageButton deleteButton;

        IntervalViewHolder(final View v) {
            super(v);
            sensorIntervalView = v.findViewById(R.id.devInterval);
            sensorInterval = v.findViewById(R.id.devIntervalEdit);
            sensorIntervalActive = v.findViewById(R.id.devIntervalActive);
            deleteButton = v.findViewById(R.id.devIntervalDeleteButton);
            mCardView = v.findViewById(R.id.intervallistcard_view);

            sensorInterval.setVisibility(View.GONE);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "IntervalDeleteButton: onClick()");
                    deleteButton.setPressed(true);
                    listener.onIntervalDeleted((int) sensorIntervalView.getTag());
                }
            });

            sensorInterval.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Log.i(DEBUG_TAG, "Interval changed: " + editable.toString());
                    if (!editable.toString().isEmpty()) {
                        if (Integer.parseInt(editable.toString()) > 0) {
                            listener.onIntervalCreated(Integer.parseInt(editable.toString()), true);
                            sensors.interval=Integer.parseInt(editable.toString());
                        }
                        else
                        {
                            sensorInterval.setError(context.getString(R.string.devIntervalWarning));
                        }
                    }
                    else
                    {
                        sensorInterval.setError(context.getString(R.string.devIntervalWarning));
                    }
                }
            });

            sensorIntervalActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "New interval: onClick() Switch");
                    sensors.active=sensorIntervalActive.isChecked();
                    listener.onIntervalActivated((int) sensorIntervalView.getTag(), sensorIntervalActive.isChecked());
                }
            });
        }
    }
}