package com.myplex.api.request.content;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.request.user.GcmIdRequest;
import com.myplex.model.PropertiesData;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to fetch the properties of app
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * we are maintaining the data related bit rate from properties API
 * <p></p>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  PropertiesRequest propertiesRequest = new PropertiesRequest(getActivity(), new PropertiesRequest.Params("client-key","appVersion","mccandMncValues","networkTYpe",), new APICallback<PropertiesData>() {
 *
 *  @Override public void onResponse(APIResponse<PropertiesData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(propertiesRequest);
 * }</pre>
 * Created by phani on 2/2/2015.
 */
public class PropertiesRequest extends APIRequest {

    private static final String TAG = GcmIdRequest.class.getSimpleName();

    private Params params;


    public static class Params {
        String clientKey;
        String appVersion;
        String connectivityType;
        String mcc;
        String mnc;
        String clientSecret;


        public Params(String clientKey,String appVersion,String connectivityType,String mcc,String mnc,String clientSecret){
            this.clientKey = clientKey;
            this.appVersion   = appVersion;
            this.connectivityType   = connectivityType;
            this.mcc = mcc;
            this.mnc = mnc;
            this.clientSecret = clientSecret;
        }
    }

    public PropertiesRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }
    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService fetch the details of properties api.
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request

        Call<PropertiesData> propertiesReqAPICall = myplexAPI.getInstance().myplexAPIService.propertiesRequest(params.clientKey,
                params.clientSecret,params.appVersion, params.connectivityType,params.mcc,params.mnc);


        propertiesReqAPICall.enqueue(new Callback<PropertiesData>() {
            @Override
            public void onResponse(Call<PropertiesData> call, Response<PropertiesData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                PropertiesRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<PropertiesData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException || t instanceof SocketTimeoutException){
                    PropertiesRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                PropertiesRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }
}
