package com.holger.share;

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "com.holger.mashpit.action.main";
        String CANCEL_ACTION = "com.holger.mashpit.action.cancel";
        String STARTFOREGROUND_ACTION = "com.holger.mashpit.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.holger.mashpit.action.stopforeground";
        String CONNECT_ACTION = "com.holger.mashpit.action.connect";
        String RECONNECT_ACTION = "com.holger.mashpit.action.reconnect";
        String CHECK_ACTION = "com.holger.mashpit.action.check";

    }

   public interface WEAR {
        String RUN_UPDATE_NOTIFICATION = "/MashPit";
        String KEY_TITLE = "/title";
        String KEY_CONTENT = "/content";
    }

    public interface PREFS {
        String PREFS_KEY_FIRST_START = "first_start";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
