package com.holger.mashpit;

class Constants {
    interface ACTION {
        String MAIN_ACTION = "com.holger.mashpit.action.main";
        String CANCEL_ACTION = "com.holger.mashpit.action.cancel";
        String STARTFOREGROUND_ACTION = "com.holger.mashpit.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.holger.action.stopforeground";
        String CONNECT_ACTION = "com.holger.mashpit.action.connect";
        String RECONNECT_ACTION = "com.holger.mashpit.action.reconnect";
        String CHECK_ACTION = "com.holger.mashpit.action.check";
    }

    interface PREFS {
        String PREFS_KEY_FIRST_START = "first_start";
    }

    interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
