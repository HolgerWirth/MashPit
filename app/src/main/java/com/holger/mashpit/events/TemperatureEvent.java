package com.holger.mashpit.events;

public class TemperatureEvent {
    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setTemperature(String event)
    {
        this.temperature=Float.parseFloat((event));
    }

    public float getTemperature() { return temperature; }

    public String getMode() { return mode; }

    public void setMode(String mode) { this.mode= mode; }

    private String sensor;
    private int interval;
    private String event=null;
    private long timestamp;
    private float temperature;
    private int QoS;
    private String status;
    private String mode;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    private String topic;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getQoS() {return QoS;}

    public void setQoS(int QoS) {this.QoS=QoS;}
}
