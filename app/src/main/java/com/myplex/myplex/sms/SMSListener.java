package com.myplex.myplex.sms;

/**
 * Created by Srikanth on 31-Jul-17.
 */

public interface SMSListener {
    void onSMSSent(String phoneNo,String message);
}
