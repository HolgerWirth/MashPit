package com.holger.mashpit.events;

import java.util.ArrayList;

public class SensorStickyEvent {
    public ArrayList<SensorDataEvent> sticky = new ArrayList<>();

    public void addSticky(SensorDataEvent event)
    {
        int i=0;
        boolean found=false;
        for(SensorDataEvent sensor : sticky)
        {
            String uniqueSensor = sensor.getDevice()+sensor.getSensor();
            if(uniqueSensor.equals(event.getDevice()+event.getSensor()))
            {
                sticky.set(i,event);
                found=true;
                break;
            }
            i++;
        }
        if(!found)
        {
            sticky.add(event);
        }
    }
}
