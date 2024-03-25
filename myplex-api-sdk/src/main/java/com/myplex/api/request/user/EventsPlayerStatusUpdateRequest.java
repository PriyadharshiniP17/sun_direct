package com.myplex.api.request.user;


import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
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


/**
 * This class is used to login the user through the MSISDN
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 * <p></p>
 * <p>
 * For example,
 * <p>
 * <pre>{@code
 *  MSISDNLogin msisdnRequest = new MSISDNLogin(getActivity(), new MSISDNLogin.Params("msisdn","emailID"), new APICallback<BaseReponseData>() {
 *
 *  @Override public void onResponse(APIResponse<BaseReponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(msisdnRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class EventsPlayerStatusUpdateRequest extends APIRequest {

    private static final String TAG = EventsPlayerStatusUpdateRequest.class.getSimpleName();

    private Params params;


    public static class Params {
        private String contentId;
        private int elapsedTime;
        public String action;
        public String streamName;

        public Params(int elapsedTime, String action, String contentId,String streamName) {
            this.elapsedTime = elapsedTime;
            this.action = action;
            this.contentId = contentId;
            this.streamName=streamName;
        }
    }

    public EventsPlayerStatusUpdateRequest(Params params, APICallback<BaseResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to login the user
     *
     * @param myplexAPI Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String mediaSessionToken=PrefUtils.getInstance().getMediaSessionToken();


        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> loginAPICall = myplexAPI.getInstance().myplexAPIService
                .eventsPlayerStatusUpdateRequest(clientKey,
                        params.contentId,
                        params.action,
                        params.elapsedTime,
                        params.streamName,
                        mediaSessionToken,
                        APIConstants.HTTP_NO_CACHE);

        loginAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                EventsPlayerStatusUpdateRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    EventsPlayerStatusUpdateRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                EventsPlayerStatusUpdateRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
