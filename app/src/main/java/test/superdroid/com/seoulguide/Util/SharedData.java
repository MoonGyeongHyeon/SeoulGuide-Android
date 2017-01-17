package test.superdroid.com.seoulguide.Util;

import android.app.Application;

public class SharedData extends Application {
    private static final String serverIP = "http://52.42.208.72";

    public static String getServerIP() {
        return serverIP;
    }
}
