package com.myplex.myplex.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.myplex.api.APIConstants;
import com.myplex.util.SDKLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tamim on 11/5/16.
 */
public class OtpReader {

    private static OtpReader _self;
    private boolean isFullMessageReciever;
    private Handler mHandler;

    private OtpReader(Context context) {
        this.context = context;
        mHandler = new Handler(context.getMainLooper());
    }

    public OtpReader(Context context, boolean isFullMessageReciever) {
        this.isFullMessageReciever = isFullMessageReciever;
        mHandler = new Handler(context.getMainLooper());
    }

    public static synchronized OtpReader getInstance(Context mContext) {
        if (_self == null) {
            _self = new OtpReader(mContext);
        }
        return _self;
    }

    public interface OTPListener {

        void otpReceived(String messageText);

        void otpTimeOut();
    }

    public interface OTPListener2 extends OTPListener {
        void otpReceived(String address, String message);
    }

    OTPListener otpListener;
    IncomingSms incomingSms;
    Context context;
    String otp;
    public boolean otpSent = false;

    private Runnable runnableOtpTimeOut = new Runnable() {
        @Override
        public void run() {
            if (otpListener != null
                    && !otpSent) {
                otpListener.otpTimeOut();
            }
        }
    };

    public static synchronized OtpReader getInstance(Context mContext, boolean isFullMessageReciever) {
        if (_self == null) {
            _self = new OtpReader(mContext, isFullMessageReciever);
        }
        return _self;
    }

    public void start(Context context, int timeout) {
        MySMSBroadcastReceiver reciever = new MySMSBroadcastReceiver();
        otp = null;
        otpSent = false;
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(reciever, intentFilter);
        if (context != null) {
            mHandler.removeCallbacks(runnableOtpTimeOut);
            mHandler.postDelayed(runnableOtpTimeOut,  1000);
        }
    }

    public void stop() {
        if (incomingSms != null && context != null) {
            try {
                context.unregisterReceiver(incomingSms);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOtpListener(OTPListener otpListener) {
        this.otpListener = otpListener;
        if (otpListener == null) {
            return;
        }
        if (!TextUtils.isEmpty(otp)) {
            if (!otpSent) {
                otpListener.otpReceived(otp);
                otpSent = true;
            }
        }
    }

    public class IncomingSms extends BroadcastReceiver {

        // Get the object of SmsManager
        final SmsManager sms = SmsManager.getDefault();

        @Override
        public void onReceive(Context context, Intent intent) {
            LoggerD.debugOTP("onReceive");

            // Retrieves a map of extended data from the intent.
            final Bundle bundle = intent.getExtras();

            try {
                LoggerD.debugOTP("bundle- " + bundle);

                if (bundle != null) {

                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    LoggerD.debugOTP("pdusObj- " + pdusObj);
                    for (int i = 0; i < pdusObj.length; i++) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        String address = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();
                        Pattern patt;
                        Matcher m;
                        LoggerD.debugOTP("otpListener- " + otpListener + " otpSent- " + otpSent);
                        LoggerD.debugOTP("message- " + message + " message- " + address);
                        if (message != null) {
                            if(isFullMessageReciever){
                                if (otpListener != null && !otpSent && otpListener instanceof OTPListener2) {
                                    ((OTPListener2) otpListener).otpReceived(address, message);
                                }
                            } else if (address.toUpperCase().contains(APIConstants.SHREYAS_SENDER_ID_1)
                                    || address.toUpperCase().contains(APIConstants.SHREYAS_SENDER_ID_2)
                                    || address.toUpperCase().contains(APIConstants.SHREYAS_SENDER_ID_3)
                                    || address.toUpperCase().contains(APIConstants.SHREYAS_SENDER_ID_4)
                                    || address.toUpperCase().contains(APIConstants.SHREYAS_SENDER_ID_5)) {
                                patt = Pattern.compile("(|^)\\d{6}");
                                m = patt.matcher(message);
                                if (m.find()) {
                                    otp = m.group(0);
                                    if (otpListener != null && !otpSent) {
                                        otpListener.otpReceived(otp);
                                        otpSent = true;
                                    }
                                } else {
                                    patt = Pattern.compile("(|^)\\d{4}");
                                    m = patt.matcher(message);
                                    if (m.find()) {
                                        otp = m.group(0);
                                        if (otpListener != null && !otpSent) {
                                            otpListener.otpReceived(otp);
                                            otpSent = true;
                                        }
                                    }
                                }
                            }
                        }
                    } // end for loop
                } // bundle is null

            } catch (Exception e) {
                Log.e("SmsReceiver", "Exception smsReceiver" + e);

            }
        }
    }


    public class MySMSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(action)) {
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    return;
                }
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                if (status == null) {
                    return;
                }
                switch (status.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        String smsMessage = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                        SDKLogger.debug("SMSRetriever-  " + "Success     "+"message-       "+smsMessage);
                        if(smsMessage!= null) {
                            smsMessage = smsMessage.replace("[#]", "");
                            String newSmsMessage = smsMessage.substring(smsMessage.length() - 11);
                            smsMessage = smsMessage.replace(newSmsMessage, "");
                            Pattern patt = Pattern.compile("(|^)\\d{6}");
                            Matcher m = patt.matcher(smsMessage);
                            if (m.find()) {
                                otp = m.group(0);
                                LoggerD.debugOTP("SMSRetriever-   " + "OTP-  " + otp);
                                if (otpListener != null && !otpSent) {
                                    otpListener.otpReceived(otp);
                                    otpSent = true;
                                }
                            } else {
                                patt = Pattern.compile("(|^)\\d{4}");
                                m = patt.matcher(smsMessage);
                                if (m.find()) {
                                    otp = m.group(0);
                                    LoggerD.debugOTP("SMSRetriever-   " + "OTP-  " + otp);
                                    if (otpListener != null && !otpSent) {
                                        otpListener.otpReceived(otp);
                                        otpSent = true;
                                    }
                                }
                            }
                        }
                        break;
                    case CommonStatusCodes.TIMEOUT:
                        SDKLogger.debug("SMSRetriever-  " + "TimeOUT");
                        break;
                    default:
                        break;
                }
            }
        }
    }


}
