package net.jo.common;

import android.util.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Shell {

    private static final String TAG = Shell.class.getSimpleName();

    public static void exec(String command) {
        try {
            int code = Runtime.getRuntime().exec(command).waitFor();
            if (code != 0) Logger.getLogger(TAG).log(Level.INFO, "Shell command '"+command+"' failed with exit code '"+code+"'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}