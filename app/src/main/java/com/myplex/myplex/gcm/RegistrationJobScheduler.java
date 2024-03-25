/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myplex.myplex.gcm;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myplex.api.APICallback;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.GcmIdRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.utils.Util;

import java.io.IOException;


public class RegistrationJobScheduler extends JobIntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    String token=null;


    public static final int JOB_ID = 1;

    public static void enqueueWork(final Context context, final Intent work) {
        enqueueWork(context, RegistrationJobScheduler.class, JOB_ID, work);
    }




    protected void onHandleIntent(Intent intent) {

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            LoggerD.debugLog(TAG + "CleverTapAPI: RegistrationIntentService: onHandleIntent:");

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            token = task.getResult();
                            if (token == null)
                                return;
                            PrefUtils.getInstance().setPrefIsCleverTapGCMTokenUpdated(true);
                   /* CleverTapAPI cleverTap;
                    try {
                        cleverTap = CleverTapAPI.getDefaultInstance(getApplicationContext());
                        if (cleverTap != null) {
                            cleverTap.pushFcmRegistrationId(token, true);
                        }
                    } catch (Exception e) {
                        // thrown if you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml
                        LoggerD.debugLog(TAG + "CleverTapAPI: RegistrationIntentService: CleverTapMetaDataNotFoundException:");
                        e.printStackTrace();
                    }*/
                            // [END get_token]
                            //Log.i(TAG, "GCM Registration Token: " + token);

                            // TODO: Implement this method to send any registration to your app's servers.
                            sendRegistrationToServer(token);


                        }
                    });


           /* FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    token = instanceIdResult.getToken();
                    if (token == null)
                        return;
                    PrefUtils.getInstance().setPrefIsCleverTapGCMTokenUpdated(true);
                   *//* CleverTapAPI cleverTap;
                    try {
                        cleverTap = CleverTapAPI.getDefaultInstance(getApplicationContext());
                        if (cleverTap != null) {
                            cleverTap.pushFcmRegistrationId(token, true);
                        }
                    } catch (Exception e) {
                        // thrown if you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml
                        LoggerD.debugLog(TAG + "CleverTapAPI: RegistrationIntentService: CleverTapMetaDataNotFoundException:");
                        e.printStackTrace();
                    }*//*
                    // [END get_token]
                    //Log.i(TAG, "GCM Registration Token: " + token);

                    // TODO: Implement this method to send any registration to your app's servers.
                    sendRegistrationToServer(token);
                }
            });*/


            // Subscribe to topic channels
           // subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
           // PrefUtils.getInstance().setBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, true);

            // [END register_for_gcm]
        } catch (Exception e) {
            //Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            PrefUtils.getInstance().setBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, true);
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        //Intent registrationComplete = new Intent(MyGcmListenerService.REGISTRATION_COMPLETE);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String version = Util.getAppVersionName(this);
        String mccAndMNCValues = getMCCAndMNCValues();
        String mccValue = "";
        String mncValue = "";
        if(!TextUtils.isEmpty(mccAndMNCValues)){
            String []mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }

        GcmIdRequest.Params params = new GcmIdRequest.Params(token,version,clientKey,mccValue,mncValue);
        GcmIdRequest gcmIdRequest = new GcmIdRequest(params,new APICallback<BaseResponseData>() {

            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if(response == null || response.body() == null){
                    PrefUtils.getInstance().setBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, false);

                    return;
                }
                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    PrefUtils.getInstance().setBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, false);
                    return;
                }

                if(response.body().code == 200 && response.body().status.equals("SUCCESS")){
                    PrefUtils.getInstance().setBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, true);

                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                PrefUtils.getInstance().setBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, false);

            }
        });
        APIService.getInstance().execute(gcmIdRequest);
        //Log.d(TAG, "clientKey=" + clientKey);



    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }
    // [END subscribe_topics]
    private  String getMCCAndMNCValues(){
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        String codes ="";
        if (!networkOperator.isEmpty()) {
            int mcc = Integer.parseInt(networkOperator.substring(0, 3));
            int mnc = Integer.parseInt(networkOperator.substring(3));
            codes = mcc+","+mnc;
        }
        return codes;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onHandleIntent(intent);
    }
}
