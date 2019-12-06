package com.holger.mashpit.events;

public class SensorEvent {
        private String server;
        private String sensor;
        private int interval;
        private String type;
        private String name;
        private boolean active;

        public String getServer() {
                return server;
        }

        public void setServer(String server) {
                this.server = server;
        }

        public String getSensor() {
                return sensor;
        }

        public void setSensor(String sensor) {
                this.sensor = sensor;
        }

        public int getInterval() {
                return interval;
        }

        public void setInterval(int interval) {
                this.interval = interval;
        }

        public String getType() {
                return type;
        }

        public void setType(String type) {
                this.type = type;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public boolean isActive() {
                return active;
        }

        public void setActive(boolean active) {
                this.active = active;
        }
}
