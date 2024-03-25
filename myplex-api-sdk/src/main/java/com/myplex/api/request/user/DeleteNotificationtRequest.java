package com.myplex.api.request.user;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.DeleteNotificationResponse;
import com.myplex.model.NotificationList;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeleteNotificationtRequest extends APIRequest {



    private DeleteNotificationtRequest.Params params;

    public DeleteNotificationtRequest(DeleteNotificationtRequest.Params params, APICallback<DeleteNotificationResponse> mListener) {
        super(mListener);
        this.params = params;
    }

    public static class Params {
        private int notificationId;
        private String status;

        public Params(int notificationId, String status ) {
            this.notificationId = notificationId;
            this.status =status;
        }
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<DeleteNotificationResponse> genresListResponseCall;

        if(params.status.equalsIgnoreCase("viewed"))
            genresListResponseCall = myplexAPI.getInstance().myplexAPIService
                .geViewedNotifications(clientKey, params.notificationId);
        else
            genresListResponseCall = myplexAPI.getInstance().myplexAPIService
                    .getDeleteNotifications(clientKey, params.notificationId);

        genresListResponseCall.enqueue(new Callback<DeleteNotificationResponse>() {


            @Override
            public void onResponse(Call<DeleteNotificationResponse> call, Response<DeleteNotificationResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    // apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                DeleteNotificationtRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<DeleteNotificationResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    DeleteNotificationtRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                DeleteNotificationtRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
