-keep public class com.holger.mashpit.ProcessEvent
-keep public class com.holger.mashpit.StatusEvent
-keep public class com.holger.mashpit.TemperatureEvent

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# activeandroid
-keep class com.activeandroid.** { *; }
-keep class com.activeandroid.**.** { *; }
-keep class * extends com.activeandroid.Model
-keep class * extends com.activeandroid.serializer.TypeSerializer

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-libraryjars libs
-keep class org.eclipse.paho.client.mqttv3.** { *; }
