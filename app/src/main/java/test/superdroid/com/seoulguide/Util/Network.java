package test.superdroid.com.seoulguide.Util;

import android.content.Context;
import android.net.ConnectivityManager;

public class Network {
    public static boolean isNetworkConnected(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null;
    }
}
