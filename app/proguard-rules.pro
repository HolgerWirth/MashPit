-keep class com.holger.mashpit.events.** { *; }
-keep class com.holger.mashpit.model.** { *; }
-keep class com.holger.mashpit.prefs.** { *; }
-keep class com.holger.mashpit.tools.** { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep class org.eclipse.paho.client.mqttv3.** { *; }
-keep class com.getkeepsafe.relinker.** { *; }
