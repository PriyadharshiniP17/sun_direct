package com.myplex.api.request.user;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.DeleteNotificationResponse;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeleteAllNotificationtRequest extends APIRequest {



    private DeleteAllNotificationtRequest.Params params;

    public DeleteAllNotificationtRequest(APICallback<DeleteNotificationResponse> mListener) {
        super(mListener);
        this.params = params;
    }

    public static class Params {
        private int notificationId;

        public Params(int notificationId ) {
            this.notificationId = notificationId;
        }
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<DeleteNotificationResponse> genresListResponseCall = myplexAPI.getInstance().myplexAPIService
                .getDeleteAllNotifications(clientKey);

        genresListResponseCall.enqueue(new Callback<DeleteNotificationResponse>() {


            @Override
            public void onResponse(Call<DeleteNotificationResponse> call, Response<DeleteNotificationResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    // apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                DeleteAllNotificationtRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<DeleteNotificationResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    DeleteAllNotificationtRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                DeleteAllNotificationtRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
