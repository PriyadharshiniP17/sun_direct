package com.myplex.model;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.ImageUploadResponse;
//import com.myplex.util.LogUtils;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by apalya on 4/10/2017.
 */

public class UploadImage extends APIRequest{

    //private static final String TAG = UpdateProfile.class.getSimpleName();
    private String userID;
    RequestBody image;

    public UploadImage(RequestBody image, String userID, APICallback mListener) {
        super(mListener);
        this.image = image;
        this.userID = userID;

    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        Call<ImageUploadResponse> requestUserSubProfile = myplexAPI.getInstance().myplexAPIService.postImage(clientKey,image,userID);
        requestUserSubProfile.enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                    apiResponse.setSuccess(true);
                }else
                    apiResponse.setSuccess(false);
                UploadImage.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
               // LogUtils.debug(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    UploadImage.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                UploadImage.this.onFailure(t, ERR_UN_KNOWN);

            }
        });

    }
}
