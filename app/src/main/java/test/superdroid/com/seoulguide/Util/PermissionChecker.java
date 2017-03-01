package test.superdroid.com.seoulguide.Util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionChecker {
    public static final int REQUEST_FINE_LOCATION = 1;

    public static boolean checkPermission(Context context, String permission) {
        int permissionResult = ActivityCompat.checkSelfPermission(context, permission);

        if (permissionResult == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public static boolean verifyPermission(int[] grantResults) {
        if(grantResults.length == 0)
            return false;

        for(int result : grantResults) {
            if(result != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

}
