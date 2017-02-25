package com.dnd.radioTopCongo.helpers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class PermissionHelper {

    private static final String TAG = PermissionHelper.class.getSimpleName();

    public static boolean isPermissionGranted(Activity activity, String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission (" + permission + ") is granted");
                return true;
            } else {
                Log.d(TAG, "Permission (" + permission + ") is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return false;
            }
        } else {
            // Permission is automatically granted on sdk<23 upon installation
            Log.d(TAG, "Permission is granted");
            return true;
        }
    }
}
