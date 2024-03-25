package com.myplex.api.request.user;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.NotificationList;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationsListRequest extends APIRequest {


    public NotificationsListRequest(APICallback<NotificationList> mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<NotificationList> genresListResponseCall = myplexAPI.getInstance().myplexAPIService
                .getServiceNotifications(clientKey, 1, 21);

        genresListResponseCall.enqueue(new Callback<NotificationList>() {
            @Override
            public void onResponse(Call<NotificationList> call, Response<NotificationList> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    // apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                NotificationsListRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<NotificationList> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    NotificationsListRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                NotificationsListRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
