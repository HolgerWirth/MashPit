package com.holger.mashpit;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.holger.mashpit.model.Subscriptions;

import java.util.List;

class SubscriberAdapter extends RecyclerView.Adapter<SubscriberAdapter.SubscriberViewHolder> {
    private static final String DEBUG_TAG = "SubscriberAdapter";
    private List<Subscriptions> subscriberList;
    private DeleteSubscriptionCallback deleteSubscriptionCallback;

    SubscriberAdapter(List<Subscriptions> subscriberList) {
        this.subscriberList = subscriberList;
        if(subscriberList != null) Log.i(DEBUG_TAG, "Subscriptions: "+this.subscriberList.size());
    }

    void refreshSubscribers(List<Subscriptions> subscriberList)
    {
        this.subscriberList = subscriberList;
        if(subscriberList != null) Log.i(DEBUG_TAG, "Refresh subscriptions: "+this.subscriberList.size());
    }

    @Override
    public int getItemCount() {
        if(subscriberList == null)
        {
            return 0;
        }
        return subscriberList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriberViewHolder subscriberViewHolder, int i) {
        Subscriptions sub = this.subscriberList.get(i);
        String interval = Integer.toString(sub.interval);
        subscriberViewHolder.server.setText(sub.server);
        if(!sub.aliasServer.isEmpty())
        {
            subscriberViewHolder.server.setText(sub.aliasServer);
        }
        subscriberViewHolder.server.setTag(sub.topic);
        subscriberViewHolder.sensor.setText(sub.sensor);
        if(!sub.aliasSensor.isEmpty())
        {
            subscriberViewHolder.sensor.setText(sub.aliasSensor);
        }
        subscriberViewHolder.interval.setText(interval);
    }

    @NonNull
    @Override
    public SubscriberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.subscribercard, viewGroup, false);

        return new SubscriberViewHolder(itemView);
    }

    class SubscriberViewHolder extends RecyclerView.ViewHolder {
        TextView server;
        TextView sensor;
        TextView interval;
        ImageButton deleteButton;
        CardView mCardView;

        SubscriberViewHolder(final View v) {
            super(v);
            server = v.findViewById(R.id.subServer);
            sensor = v.findViewById(R.id.subSensor);
            interval = v.findViewById(R.id.subInterval);
            deleteButton = v.findViewById(R.id.subDeleteButton);
            mCardView = v.findViewById(R.id.subscribbercard_view);

           deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "ViewHolder: Click on delete");
                    deleteButton.setPressed(true);
                    deleteSubscriptionCallback.onSubscriptionDeleted((String)server.getTag());
                }
            });
        }
    }

    public interface DeleteSubscriptionCallback {
        void onSubscriptionDeleted(String position);
    }

    void setOnItemClickListener(SubscriberAdapter.DeleteSubscriptionCallback callback){
        deleteSubscriptionCallback = callback;
    }
}