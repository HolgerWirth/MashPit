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

import com.holger.mashpit.model.MPStatus;

import java.util.List;

class MPProcAdapter extends RecyclerView.Adapter<MPProcAdapter.MPProcViewHolder> {

    private static final String DEBUG_TAG = "MPProcAdapter";
    private Context context;
    private List<MPStatus> procList;

    MPProcAdapter(List<MPStatus> procList) {
        this.procList = procList;
        if(procList != null) Log.i(DEBUG_TAG, "MPProcesses: "+this.procList.size());
    }

    @Override
    public int getItemCount() {
        if(procList == null)
        {
            return 0;
        }
        return procList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MPProcViewHolder mpProcViewHolder, int i) {
        MPStatus mpStatus = this.procList.get(i);
        mpProcViewHolder.mpprocName.setText(mpStatus.topic);
        if(mpStatus.active.equals("1"))
        {
            mpProcViewHolder.mpprocStatus.setText(context.getString(R.string.MPProcActiveYes));
            mpProcViewHolder.mpprocStatus.setBackgroundResource(android.R.color.holo_green_light);
            mpProcViewHolder.mpprocPIDTitle.setVisibility(View.VISIBLE);
            mpProcViewHolder.mpprocPID.setVisibility(View.VISIBLE);
        }
        else
        {
            mpProcViewHolder.mpprocStatus.setText(context.getString(R.string.MPProcActiveNo));
            mpProcViewHolder.mpprocStatus.setBackgroundResource(android.R.color.holo_red_light);
            mpProcViewHolder.mpprocPIDTitle.setVisibility(View.GONE);
            mpProcViewHolder.mpprocPID.setVisibility(View.GONE);
        }

        switch(mpStatus.Type)
        {
            case("PWR"):
                mpProcViewHolder.mpprocType.setText(context.getString(R.string.MPProcTypePWR));
                break;
            case("SSR"):
                mpProcViewHolder.mpprocType.setText(context.getString(R.string.MPProcTypeSSR));
                break;
            case("SVR"):
                mpProcViewHolder.mpprocType.setText(context.getString(R.string.MPProcTypeSVR));
                break;
            default:
                mpProcViewHolder.mpprocType.setText("");
                break;
        }
        mpProcViewHolder.mpprocPID.setText(mpStatus.PID);
    }

    @NonNull
    @Override
    public MPProcViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.mpproccard, viewGroup, false);
        context=viewGroup.getContext();
        return new MPProcViewHolder(itemView);
    }

    class MPProcViewHolder extends RecyclerView.ViewHolder {
        TextView mpprocName;
        TextView mpprocStatus;
        TextView mpprocPID;
        TextView mpprocPIDTitle;
        TextView mpprocType;
        CardView mCardView;

        MPProcViewHolder(final View v) {
            super(v);
            mpprocName = v.findViewById(R.id.mpstatProcName);
            mpprocStatus = v.findViewById(R.id.mpstatProcStatus);
            mpprocPID = v.findViewById(R.id.mpstatProcPID);
            mpprocPIDTitle = v.findViewById(R.id.mpstatProcPIDTitle);
            mpprocType = v.findViewById(R.id.mpstatProcType);

            mCardView = v.findViewById(R.id.mpproccard_view);

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