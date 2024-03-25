package com.myplex.api.request.content;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.VernacularResponse;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VernacularRequest extends APIRequest {
    private static final String TAG = "VernacularRequest";

    public VernacularRequest(APICallback mListener) {
        super(mListener);
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
//        //Log.d(TAG,"clientKey=" + clientKey);

//            TODO  Add login check with fields


        String url = PrefUtils.getInstance().getVernacularLanguageURL();

        SDKLogger.debug("url:: " + url);
        Call<VernacularResponse> languageRequest = myplexAPI.getInstance().myplexAPIService
                .vernacularRequest(url, clientKey);

        languageRequest.enqueue(new Callback<VernacularResponse>() {
            @Override
            public void onResponse(Call<VernacularResponse> call, Response<VernacularResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }

                apiResponse.setSuccess(response.isSuccessful());
                VernacularRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<VernacularResponse> call, Throwable t) {
                SDKLogger.debug(TAG + "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    VernacularRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                VernacularRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });

    }
}
