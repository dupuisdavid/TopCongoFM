package com.dnd.radioTopCongo;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

public class NotificationExtender extends NotificationExtenderService {
    private static final String TAG = NotificationExtender.class.getSimpleName();

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.
        Log.w(TAG, receivedResult.toString());
        // Return true to stop the notification from displaying.
        return false;
    }
}