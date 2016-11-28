package com.holger.mashpit;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.holger.mashpit.model.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class SubscriberAdapter extends RecyclerView.Adapter<SubscriberAdapter.SubscriberViewHolder> {

    private static final String DEBUG_TAG = "SubscriberAdapter";

    private List<Subscriber> subscriberList;
    private List<Subscriber> delsubscriberList = new ArrayList<>();

    public SubscriberAdapter(List<Subscriber> subscriberList) {

        this.subscriberList = subscriberList;
        if(subscriberList != null) Log.i(DEBUG_TAG, "Subscribers: "+this.subscriberList.size());
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
    public void onBindViewHolder(SubscriberViewHolder subscriberViewHolder, int i) {
        Subscriber sub = this.subscriberList.get(i);
        subscriberViewHolder.topic.setText(sub.topic);
        subscriberViewHolder.interval.setText(sub.interval);
        subscriberViewHolder.remark.setText(sub.remark);
        subscriberViewHolder.persistent.setText(R.string.no);
        if(sub.persistent)
        {
            subscriberViewHolder.persistent.setText(R.string.yes);
        }
    }

    @Override
    public SubscriberViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.subscribercard, viewGroup, false);

        return new SubscriberViewHolder(itemView);
    }

    public class SubscriberViewHolder extends RecyclerView.ViewHolder {
        protected TextView topic;
        protected TextView interval;
        protected TextView persistent;
        protected TextView remark;
        public CardView mCardView;

        public SubscriberViewHolder(final View v) {
            super(v);
            topic =  (TextView) v.findViewById(R.id.sTitel);
            interval = (TextView)  v.findViewById(R.id.sInterval);
            persistent = (TextView)  v.findViewById(R.id.sPersist);
            remark = (TextView)  v.findViewById(R.id.sRemark);

            mCardView = (CardView) v.findViewById(R.id.card_view);

           v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(DEBUG_TAG, "ViewHolder: onClick()");
                    mCardView.setPressed(true);
                }
            });

        }
    }
    public void addItem(Subscriber dataObj, int index) {
        subscriberList.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void addItem(Subscriber dataObj) {
        subscriberList.add(dataObj);
        notifyItemInserted(subscriberList.size());
    }

    public void addDelItem(Subscriber dataObj) {
        delsubscriberList.add(dataObj);
    }

    public Subscriber getItem(int index) {
        return subscriberList.get(index);
    }

    public void deleteItem(int index) {
        addDelItem(getItem(index));
        subscriberList.remove(index);
        notifyItemRemoved(index);
    }

    public void changeItem(int index,Subscriber dataObj) {
        Subscriber sub=subscriberList.get(index);
        sub.topic=dataObj.topic;
        sub.interval=dataObj.interval;
        sub.persistent=dataObj.persistent;
        sub.remark = dataObj.remark;
        addDelItem(dataObj);
        notifyItemChanged(index);
    }

    public List<Subscriber> getSubscriberList()
    {
        return subscriberList;
    }
    public List<Subscriber> getDelSubscriberList()
    {
        return delsubscriberList;
    }

}