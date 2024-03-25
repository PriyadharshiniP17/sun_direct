package com.myplex.myplex.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.myplex.myplex.subscribe.SubscriptionWebActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Srikanth on 08-Jun-17.
 */

public class SMSHandler {
    public static void sendSMS(String phoneNo, String message, PendingIntent sentIntet, PendingIntent delIntet) {
// Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();
// Send a text based SMS
        smsManager.sendTextMessage(phoneNo, null, message, sentIntet, delIntet);
//        sms.sendTextMessage("your recipient num here", null, "Your Message here", null, null);    }
    }

    public static List<SimInfo> getSIMInfo(Context context) {
        List<SimInfo> simInfoList = new ArrayList<>();
        Uri URI_TELEPHONY = Uri.parse("content://telephony/siminfo/");
        Cursor c = context.getContentResolver().query(URI_TELEPHONY, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex("_id"));
                int slot = c.getInt(c.getColumnIndex("slot"));
                String display_name = c.getString(c.getColumnIndex("display_name"));
                String icc_id = c.getString(c.getColumnIndex("icc_id"));
                SimInfo simInfo = new SimInfo(id, display_name, icc_id, slot);
                Log.d("apipas_sim_info", simInfo.toString());
                simInfoList.add(simInfo);
            } while (c.moveToNext());
        }
        c.close();

        return simInfoList;
    }


    // Receiver for Sent SMS.
    class SMSSentFilter extends BroadcastReceiver {
        private final String phoneNo;
        private final String message;
        private final SMSListener mListener;

        public SMSSentFilter(String phoneNo, String message, SMSListener listener){
            this.phoneNo = phoneNo;
            this.message = message;
            this.mListener = listener;
        }
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS sent",
                            Toast.LENGTH_SHORT).show();
                    if(mListener != null){
                        mListener.onSMSSent(phoneNo,message);
                    }
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "Generic failure",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
//        new IntentFilter(smsSent))
    }

    // Receiver for Delivered SMS.
    class SMSDeliveredFilter extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
//        , new IntentFilter(smsDelivered)
    }

    public static void deleteMessage(Context context, String phoneNo, String message) {

        try {
            Log.d(SubscriptionWebActivity.TAG, "deleteMessage: Deleting SMS from inbox");
            Uri uriSms = Uri.parse("content://sms/");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[]{"_id", "thread_id", "address",
                            "person", "date", "body"}, null, null, null);
            Uri uri = null;
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);
                    int rowsDeleted = 0;
                    Log.d(SubscriptionWebActivity.TAG, "Deleting threads: " + threadId);
                    Log.d(SubscriptionWebActivity.TAG, "deleteMessage: id- "+ id + "" +
                            " threadId- " + threadId + "" +
                            " body- " + body + "" +
                            " rowsDeleted- " + rowsDeleted + "" +
                            " address- " + address);
                    if (address.equalsIgnoreCase(phoneNo)
                            && body.equalsIgnoreCase(message)) {
                        ConversationQueryHandler handler = new ConversationQueryHandler(context.getContentResolver(), context);
                        synchronized (sDeletingThreadsLock) {
                            Log.v(SubscriptionWebActivity.TAG, "Conversation startDelete sDeletingThreads: " + sDeletingThreads);
                            if (sDeletingThreads) {
                                Log.e(SubscriptionWebActivity.TAG, "startDeleteAll already in the middle of a delete", new Exception());
                            }
                            sDeletingThreads = true;
                            uri = ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId);
                            String selection = true ? null : "locked=0";

                            handler.setDeleteToken(0);
                            handler.startDelete(0, new Long(threadId), uri, selection, null);
                        }
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.d(SubscriptionWebActivity.TAG, "deleteMessage: Could not delete SMS from inbox: " + e.getMessage());
        }
    }
    private static Object sDeletingThreadsLock = new Object();
    private static boolean sDeletingThreads;

    public static class ConversationQueryHandler extends AsyncQueryHandler {
        private int mDeleteToken;
        private Context mContext;

        public ConversationQueryHandler(ContentResolver cr, Context context) {
            super(cr);
            mContext = context;
        }

        public void setDeleteToken(int token) {
            mDeleteToken = token;
        }

        /**
         * Always call this super method from your overridden onDeleteComplete function.
         */
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            if (token == mDeleteToken) {
                // release lock
                synchronized (sDeletingThreadsLock) {
                    sDeletingThreads = false;
                    Log.v(SubscriptionWebActivity.TAG, "Conversation onDeleteComplete sDeletingThreads: " + sDeletingThreads);
                    sDeletingThreadsLock.notifyAll();
                }
            }
        }
    }
}