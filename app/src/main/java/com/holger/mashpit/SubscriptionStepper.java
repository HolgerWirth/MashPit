package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.stepper.SubscriptionHolder;
import com.holger.mashpit.stepper.SubSelectDevice;
import com.holger.mashpit.stepper.SubSelectInterval;
import com.holger.mashpit.stepper.SubSelectSensor;

import ernestoyaquello.com.verticalstepperform.Step;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

public class SubscriptionStepper extends AppCompatActivity implements StepperFormListener {

    private static final String DEBUG_TAG = "SelectSensorStepper";
    private SubSelectDevice subSelectDevice;
    private SubSelectSensor subSelectSensor;
    private SubSelectInterval subSelectInterval;
    private VerticalStepperFormView verticalStepperForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_stepper_layout);

        SubscriptionHolder subscriptionHolder = new SubscriptionHolder();
        subscriptionHolder.init();

        Toolbar toolbar = findViewById(R.id.sensoredit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(0);
            onBackPressed();
        });
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Select Sensor");

        // Create the steps.
        subSelectDevice = new SubSelectDevice(this.getResources().getString(R.string.header_delect_device));
        subSelectSensor = new SubSelectSensor(this.getResources().getString(R.string.header_select_sensor));
        subSelectInterval = new SubSelectInterval(this.getResources().getString(R.string.header_select_interval));

        // Find the form view, set it up and initialize it.
        verticalStepperForm = findViewById(R.id.stepper_form);
        verticalStepperForm
                .setup(this, subSelectDevice, subSelectSensor, subSelectInterval)
                .displayBottomNavigation(false)
                .lastStepNextButtonText(this.getResources().getString(R.string.stepper_sensor_create))
                .lastStepCancelButtonText(this.getResources().getString(R.string.vertical_stepper_form_cancel_button))
                .displayStepButtons(false)
                .init();
    }

    @Override
    public void onCompletedForm() {
        Log.i(DEBUG_TAG, "Stepper completed!");
        Log.i(DEBUG_TAG, "Selected device: "+ subSelectDevice.getStepData());
        Log.i(DEBUG_TAG, "Selected sensor: "+ subSelectSensor.getStepData());
        Log.i(DEBUG_TAG, "Selected interval: "+ subSelectInterval.getStepData());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("server", subSelectDevice.getStepData()) ;
        returnIntent.putExtra("sensor", subSelectSensor.getStepData());
        returnIntent.putExtra("interval",Integer.valueOf(subSelectInterval.getStepData()));
        setResult(1, returnIntent);
        finish();
    }

    @Override
    public void onCancelledForm() {
        Log.i(DEBUG_TAG, "Stepper cancelled!");
        setResult(0, null);
        SubscriptionStepper.super.onBackPressed();
        finish();
    }

    @Override
    public void onStepAdded(int index, Step<?> addedStep) {
        Log.i(DEBUG_TAG, "Step added");
    }

    @Override
    public void onStepRemoved(int index) {
        Log.i(DEBUG_TAG, "Stepper removed");
    }

    @Override
    public void onBackPressed() {
        if(verticalStepperForm.isAnyStepCompleted())
        {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(getString(R.string.NotSaved_title));
            builder.setMessage(getString(R.string.NotSaved_text));
            builder.setNegativeButton(getString(R.string.NotSaved_button), (dialog, which) -> {
                Log.i(DEBUG_TAG, "OK pressed!");
                setResult(0, null);
                SubscriptionStepper.super.onBackPressed();
                finish();
            });
            builder.setPositiveButton(getString(R.string.NotSaved_cancel),null);
            builder.show();
        }
        else {
            setResult(0, null);
            finish();
        }
    }
}
