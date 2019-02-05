package com.holger.mashpit.events;

public class ConfEvent {
        private String XMLString;
        private String conftopic;

        public void setXMLString(String xmlString) { this.XMLString = xmlString;}
        public void setConfTopic(String topic) { this.conftopic = topic;}
        public String getXMLString() {return XMLString;}
        public String getConfTopic() {return conftopic;}
}
