package com.holger.mashpit.events;

public class ProcessEvent {

    private String temperature=null;
    private String topic="";

    public String getTemp() {
        return temperature;
    }
    public void setTemp(String event) {
        this.temperature = event;
    }

    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

}
