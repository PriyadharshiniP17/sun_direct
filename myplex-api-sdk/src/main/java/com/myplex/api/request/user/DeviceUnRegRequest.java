package com.myplex.api.request.user;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Apparao on 22/02/219.
 */

public class DeviceUnRegRequest extends APIRequest {

    private static final String TAG = "DeviceUnRegRequest";
    public DeviceUnRegRequest(APICallback mListener) {
        super(mListener);
    }

    String clientKey = PrefUtils.getInstance().getPrefClientkey();

    @Override
    protected void execute(myplexAPI myplexAPI) {
        Call<BaseResponseData> deregisterRequest = myplexAPI.getInstance().myplexAPIService.unregisterDevice(clientKey);

        deregisterRequest.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                apiResponse.setSuccess(response.isSuccessful());
                DeviceUnRegRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    DeviceUnRegRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                DeviceUnRegRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });

    }
}
