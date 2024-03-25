package com.myplex.api.request.user;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.DeviceRegData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to generate the new clientKey while client key expire case.
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * API for generating/renewing the key to be used with APIs made from devices.
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  GenerateKeyRequest generateKeyRequest = new GenerateKeyRequest(getActivity(), new GenerateKeyRequest.Params("deviceId"), new APICallback<DeviceRegData>() {
 *
 *  @Override public void onResponse(APIResponse<DeviceRegData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(generateKeyRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class GenerateKeyRequest extends APIRequest {

    private static final String TAG = "APIService";

    public static class Params {
        String clientSecret;

        public Params(String clientSecret){
            this.clientSecret = clientSecret;
        }
    }

    public GenerateKeyRequest(APICallback mListener) {
        super(mListener);
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute generate clientKey
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String deviceId = PrefUtils.getInstance().getPrefDeviceid();
        Call<DeviceRegData> registerDeviceAPICall = myplexAPI.getInstance().myplexAPIService
                .generateKeyRequest(clientKey, deviceId);

        registerDeviceAPICall.enqueue(new Callback<DeviceRegData>() {
            @Override
            public void onResponse(Call<DeviceRegData> call, Response<DeviceRegData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                apiResponse.setSuccess(response.isSuccessful());
                if(null != response.body()
                        && null != response.body().clientKey
                        && null != response.body().expiresAt){
                    apiResponse.setMessage(response.body().message);

                    //Log.d(TAG, "clientKey=" + response.body().clientKey);
                    PrefUtils.getInstance().setPrefClientkey(response.body().clientKey);
                    PrefUtils.getInstance().setPrefClientkeyExpiry(response.body().expiresAt);
                }
                GenerateKeyRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<DeviceRegData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    GenerateKeyRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                GenerateKeyRequest.this.onFailure(t, ERR_UN_KNOWN);
            }


        });



    }

}
