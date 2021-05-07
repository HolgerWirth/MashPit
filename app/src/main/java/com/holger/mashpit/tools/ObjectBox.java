package com.holger.mashpit.tools;

import android.content.Context;
import com.holger.mashpit.model.MyObjectBox;
import io.objectbox.BoxStore;
import io.objectbox.DebugFlags;

public class ObjectBox {
    private static BoxStore boxStore;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
//                .debugFlags(DebugFlags.LOG_QUERY_PARAMETERS)
                .build();
    }

    public static BoxStore get() { return boxStore; }
}