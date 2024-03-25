package com.myplex.api.request.user;


import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.LocationInfo;
import com.myplex.model.VstbLoginSessionResponse;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Srikanth on 12/10/2015.
 */
public class VstbLoginSessionRequest extends APIRequest {

    private static final String TAG = VstbLoginSessionRequest.class.getSimpleName();

    private Params params;

    public static class Params {
        String contentId;
        String fields;
        LocationInfo locationInfo;
        public String deviceId;

        public Params(String contentId, String deviceId, String fields,LocationInfo locationInfo){
            this.contentId = contentId;
            this.deviceId = deviceId;
            this.fields = fields;
            this.locationInfo = locationInfo;
        }
    }

    public VstbLoginSessionRequest(Params params, APICallback<VstbLoginSessionResponse> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String country = null,postalCode = null,area = null;
        if(params.locationInfo != null){
            if(!TextUtils.isEmpty(params.locationInfo.postalCode)){
                postalCode = params.locationInfo.postalCode;
            }
            if(!TextUtils.isEmpty(params.locationInfo.country)){
                country = params.locationInfo.country;
            }
            if(!TextUtils.isEmpty(params.locationInfo.area)){
                area = params.locationInfo.area;
            }
        }
        String mccAndMNCValues = SDKUtils.getMCCAndMNCValues(myplexAPISDK.getApplicationContext());
        String mccValue = "";
        String mncValue = "";

        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        //Log.d(TAG, "clientKey=" + clientKey);
        Call<VstbLoginSessionResponse> signupAPICall = myplexAPI.getInstance().myplexAPIService
                .hooqSessionLoginRequest(clientKey,
                        params.contentId,
                        params.deviceId,
                        params.fields,
                        APIConstants.HTTP_NO_CACHE,
                        postalCode,
                        country,
                        area,
                        mccValue,
                        mncValue);

        signupAPICall.enqueue(new Callback<VstbLoginSessionResponse>() {

            @Override
            public void onResponse(Call<VstbLoginSessionResponse> call, Response<VstbLoginSessionResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                VstbLoginSessionRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<VstbLoginSessionResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    VstbLoginSessionRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                VstbLoginSessionRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
