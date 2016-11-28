package com.holger.mashpit.tools;

import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.holger.mashpit.events.StatusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SnackBar {

    private static final String DEBUG_TAG = "SnackBar";
    private static CoordinatorLayout coLayout;
    private View.OnClickListener mOnClickListener;

    public SnackBar(CoordinatorLayout coordinatorLayout) {
        EventBus.getDefault().register(this);

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Button clicked");
            }
        };

        Log.i(DEBUG_TAG, "SnackBar initialized");
        coLayout=coordinatorLayout;
    }

    public void setmOnClickListener(View.OnClickListener myListener)
    {
        this.mOnClickListener = myListener;
        Log.i(DEBUG_TAG, "Listener changed");
    }

    private void displayError(String snbText) {
        Snackbar snackbar = Snackbar
                .make(coLayout,snbText, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY",mOnClickListener);

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }

    private void displayInfo(String snbText) {
        Snackbar snackbar = Snackbar
                .make(coLayout,snbText, Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.GREEN);

        snackbar.show();
    }

    public void displayUndo(String snbText) {
        Snackbar snackbar = Snackbar
                .make(coLayout,snbText, Snackbar.LENGTH_LONG)
                .setAction("UNDO",mOnClickListener);

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }

    public void stopEvents()
    {
        Log.i(DEBUG_TAG, "stopEvents");
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void getStatusEvent(StatusEvent myEvent) {

        if (myEvent != null) {
            if (myEvent.getTopic().equals("mqttstatus")) {
                Log.i(DEBUG_TAG, "StatusEvent arrived: " + myEvent.getTopic());
                if(myEvent.getMode().equals("error")) {
                    displayError(myEvent.getStatus());
                }
                if(myEvent.getMode().equals("info")) {
                    displayInfo(myEvent.getStatus());
                }

            }
        }
    }
}