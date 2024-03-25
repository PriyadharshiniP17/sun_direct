package com.myplex.myplex.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.myplex.api.APIConstants;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;

public class ReminderReceiver extends BroadcastReceiver {


    private static final String TAG = ReminderReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "ReminderReceiver: onReceive");

        Bundle extras = intent.getExtras();
        if (extras == null ||
                extras.getString("_id") == null ||
                extras.getString("_id").length() < 1){
            return;
        }
        if( !PrefUtils.getInstance().isNotificationEnabled()) {
            return;
        }
        String nMessage = extras.getString("note");
        String nTitle = extras.getString(APIConstants.NOTIFICATION_PARAM_TITLE);
        String _id = extras.getString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
        String time = extras.getString(APIConstants.NOTIFICATION_PARAM_TIME);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //Log.d(TAG, "ReminderReceiver: onReceive - title " + nTitle + " _id " + _id);
        String contentType = extras.getString(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE);
        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE, contentType);
        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_TITLE, nTitle);
        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_TIME, time);
        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_ACTION, APIConstants.NOTIFICATION_PARAM_AUTOPLAY);
        notificationIntent.putExtra("auto_rem_message", nMessage);
        notificationIntent.putExtra("from_alaram", true);
        Analytics.gaNotificationEvent(Analytics.EVENT_ACTION_RECIEVED_AUTO_REMINDER,nTitle);
        Util.showNotification(context,nTitle,nMessage,notificationIntent);
    }

}
