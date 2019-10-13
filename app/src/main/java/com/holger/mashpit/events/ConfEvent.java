package com.holger.mashpit.events;

public class ConfEvent {
        private String XMLString;
        private String conftopic;
        private String MPServer;

        public void setXMLString(String xmlString) { this.XMLString = xmlString;}
        public void setConfTopic(String topic) { this.conftopic = topic;}
        public String getXMLString() {return XMLString;}
        public String getConfTopic() {return conftopic;}
        public void setMPServer(String MPServer) {this.MPServer=MPServer;}
        public String getMPServer() {return MPServer;}
}
