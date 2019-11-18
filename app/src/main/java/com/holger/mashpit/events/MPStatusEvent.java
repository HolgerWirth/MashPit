package com.holger.mashpit.events;

public class MPStatusEvent {
        private String topic;
        private String MPServer;
        private String alias;
        private boolean active;
        private String PID;
        private int processes;
        private int actprocesses;

        public int getActprocesses() {
                return actprocesses;
        }

        public void setActprocesses(int actprocesses) {
                this.actprocesses = actprocesses;
        }

        public boolean isActive() {
                return active;
        }

        public void setActive(boolean active) {
                this.active = active;
        }

        public int getProcesses() {
                return processes;
        }

        public void setProcesses(int processes) {
                this.processes = processes;
        }

        public String getPID() {
                return PID;
        }

        public void setPID(String PID) {
                this.PID = PID;
        }

        public String getType() {
                return Type;
        }

        public void setType(String type) {
                this.Type = type;
        }

        public void setAlias(String alias) {this.alias = alias;}
        private String Type;

        public String getStatusTopic() {return topic;}
        public void setStatusTopic(String topic) {this.topic = topic;}
        public void setMPServer(String MPServer) {this.MPServer=MPServer;}
        public String getMPServer() {return MPServer;}
        public String getAlias() {return alias;}
}
