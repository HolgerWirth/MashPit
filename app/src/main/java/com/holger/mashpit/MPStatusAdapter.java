package com.holger.mashpit;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.holger.mashpit.events.MPStatusEvent;

import java.util.List;

class MPStatusAdapter extends RecyclerView.Adapter<MPStatusAdapter.MPStatusViewHolder> {

    private static final String DEBUG_TAG = "MPStatusAdapter";

    private List<MPStatusEvent> statusList;

    MPStatusAdapter(List<MPStatusEvent> statusList) {
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
    public void onBindViewHolder(@NonNull MPStatusViewHolder mpStatusViewHolder, int i) {
        MPStatusEvent mpStatusEvent = this.getItem(i);
        mpStatusViewHolder.mpstatServer.setText(mpStatusEvent.getMPServer());
        mpStatusViewHolder.mpstatProcesses.setText(String.valueOf(mpStatusEvent.getProcesses()));
        mpStatusViewHolder.mpstatProcActive.setText(String.valueOf(mpStatusEvent.getActprocesses()));
    }

    @NonNull
    @Override
    public MPStatusViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.mpstatuscard, viewGroup, false);

        return new MPStatusViewHolder(itemView);
    }

    private MPStatusEvent getItem(int position) {
        return statusList.get(position);
    }

    class MPStatusViewHolder extends RecyclerView.ViewHolder {
        TextView mpstatServer;
        TextView mpstatProcesses;
        TextView mpstatProcActive;
        CardView mCardView;

        MPStatusViewHolder(final View v) {
            super(v);
            mpstatServer = v.findViewById(R.id.mpstatServer);
            mpstatProcesses = v.findViewById(R.id.mpstatProcesses);
            mpstatProcActive = v.findViewById(R.id.mpstatProcActive);

            mCardView = v.findViewById(R.id.mpstatuscard_view);

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