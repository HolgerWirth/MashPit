package com.holger.mashpit.events;

public class StatusEvent {
    private String topic="";
    private String mode="";
    private String status="";

    public void setMode(String mode) {
        this.mode = mode;
    }
    public String getMode() {
        return mode;
    }

    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
