package com.myplex.api.request.user;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.UserProfileResponseData;
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
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  MSISDNLogin msisdnRequest = new MSISDNLogin(getActivity(), new MSISDNLogin.Params("msisdn","imsi"), new APICallback<BaseReponseData>() {
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
public class UpdateProfileRequest extends APIRequest {

    private static final String TAG = UpdateProfileRequest.class.getSimpleName();

    private Params params;


    public static class Params {
        private String mobile;
        private String email;
        private String name,last;
        private String smc, otp;
        private String country,gender,age,state,city,dob,address,pincode,language;

        public Params(String name, String mobile, String emailID,String gender,String country,String age,String last,
                      String state,String city,String dob) {
            this.mobile = mobile;
            this.email = emailID;
            this.name = name;
            this.last=last;
            this.country = country;
            this.gender = gender;
            this.age = age;
            this.state=state;
            this.city=city;
            this.dob=dob;
        }
        public Params(String name, String mobile, String emailID,String smc) {
            this.mobile = mobile;
            this.email = emailID;
            this.name = name;
            this.smc=smc;
        }
        public Params(String name, String mobile, String emailID,String smc, String otp) {
            this.mobile = mobile;
            this.email = emailID;
            this.name = name;
            this.smc=smc;
            this.otp = otp;
        }

        public Params(String country,String state,String city,String dob,String address,String pincode,String language) {
            this.country = country;
            this.state=state;
            this.city=city;
            this.dob=dob;
            this.address=address;
            this.pincode=pincode;
            this.language=language;
        }
    }

    public UpdateProfileRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to login the user
     *
     * @param myplexAPIIn Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPIIn) {
        // Send request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();


        //Log.d(TAG, "clientKey=" + clientKey);
        Call<UserProfileResponseData> mProfileUpdateAPICall = myplexAPI.getInstance().myplexAPIService
                .userProfileUpdateRequest(clientKey,params.name,params.mobile,params.email,params.gender,params.country,params.age,
                        params.last,params.dob,params.city,params.state,params.pincode,params.address, params.otp,params.language);

        mProfileUpdateAPICall.enqueue(new Callback<UserProfileResponseData>() {


            @Override
            public void onResponse(Call<UserProfileResponseData> call, Response<UserProfileResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                UpdateProfileRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<UserProfileResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    UpdateProfileRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                UpdateProfileRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
