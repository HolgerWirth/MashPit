package com.holger.mashpit.events;

public class MPStatusEvent {
        private String status;
        private String topic;
        private String MPServer;

        public void setStatus(String status) { this.status = status;}
        public String getStatus() {return status;}
        public String getStatusTopic() {return topic;}
        public void setStatusTopic(String topic) {this.topic = topic;}
        public void setMPServer(String MPServer) {this.MPServer=MPServer;}
        public String getMPServer() {return MPServer;}
}
