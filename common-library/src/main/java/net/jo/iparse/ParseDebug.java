package net.jo.iparse;

import android.util.Log;

public class ParseDebug {

    private static final String TAG = ParseDebug.class.getSimpleName();

    public static void log(Throwable e) {
        Log.d(TAG, e.getMessage());
    }

    public static void log(String msg) {
        Log.d(TAG, msg);
    }
}
