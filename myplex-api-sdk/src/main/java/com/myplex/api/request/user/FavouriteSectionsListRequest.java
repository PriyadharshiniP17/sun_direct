package com.myplex.api.request.user;


import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.FavouriteSectionsListResponse;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FavouriteSectionsListRequest extends APIRequest {

    private static final String TAG = FavouriteSectionsListRequest.class.getSimpleName();
    private boolean isFavourite;

    public FavouriteSectionsListRequest(APICallback<FavouriteSectionsListResponse> mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<FavouriteSectionsListResponse> favouriteGenreListResponseCall;

           favouriteGenreListResponseCall = myplexAPI.getInstance().myplexAPIService
                   .getFavouritesSectionsList(clientKey);
           favouriteGenreListResponseCall.enqueue(new Callback<FavouriteSectionsListResponse>() {


               @Override
               public void onResponse(Call<FavouriteSectionsListResponse> call, Response<FavouriteSectionsListResponse> response) {
                   APIResponse apiResponse = new APIResponse(response.body(), null);
                   if (null != response.body()) {
                       // apiResponse.setMessage(response.body().message);
                   }
                   apiResponse.setSuccess(response.isSuccessful());
                   FavouriteSectionsListRequest.this.onResponse(apiResponse);


               }

               @Override
               public void onFailure(Call<FavouriteSectionsListResponse> call, Throwable t) {
                   //Log.d(TAG, "Error :" + t.getMessage());
                   t.printStackTrace();
                   if (t instanceof UnknownHostException
                           || t instanceof ConnectException) {
                       FavouriteSectionsListRequest.this.onFailure(t, ERR_NO_NETWORK);
                       return;
                   }
                   FavouriteSectionsListRequest.this.onFailure(t, ERR_UN_KNOWN);
               }
           });
       }



}
