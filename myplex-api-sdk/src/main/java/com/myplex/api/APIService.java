package com.myplex.api;

import android.content.Context;


import com.myplex.util.PropertiesHandler;

/**
 * Primary SDK class to interface all the api calls.
 * <p/>
 * APIService adapts a Java interface to HTTP calls
 * It is singleton class, all the myplex api calls must use APIService class.
 * <p/>
 * APIService also takes care of the local disk cache , it cashes the http response based on http cache control headers.
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 * DeviceRegistration deviceRegistration = new DeviceRegistration(getActivity(), new DeviceRegistration.Params("ClientSecret"), new APICallback<DeviceRegData>() {
 *
 *  @Override public void onResponse(APIResponse<DeviceRegData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(deviceRegistration);
 * }</pre>
 *
 * Created by Samir on 12/10/2015.
 **/
public class APIService {

    private static APIService _self;


    public static APIService getInstance() {
        if (_self == null) {
            _self = new APIService();
        }
        return _self;
    }


    /**
     * Use this method to execute all the http api request, check also {@link APIService}     *
     * <p/>
     *  <pre>
     * {@code
     *
     * DeviceRegistration deviceRegistration = new DeviceRegistration(getActivity(), new DeviceRegistration.Params("ClientSecret"), new APICallback<DeviceRegData>() {
     *
     * @Override public void onResponse(APIResponse<DeviceRegData> response) {
     *
     * }
     * @Override public void onFailure(Throwable t, int errorCode) {
     *
     * }
     * });
     *  }</pre>
     * <p/>
     * @param apiRequest Instance of the One of Request class
     */
    public void execute(APIRequest apiRequest) {
        if (apiRequest != null) {
            apiRequest.execute(myplexAPI.getInstance());
        }
    }
    /**
     * Use this method for calling properties api
     * @param clientKey    device clientKey
     * @param internetConnectivity  it shows type of internet connectivity ex:wifi/4G/3G/2G
     * @param mccAndMNCValues        it represents Country code and sim carrier ID
     * @param appVersionName         it represents App current version
     */

    public void updateProperties(Context mContext, String clientKey, String internetConnectivity,
                                 String mccAndMNCValues, String appVersionName,String clientSecret){
        PropertiesHandler propertiesHandler = new PropertiesHandler();
        propertiesHandler.init(mContext,clientKey,internetConnectivity,mccAndMNCValues,
                appVersionName,clientSecret);

    }
    public void destroy(){
        _self = null;
        myplexAPI.getInstance().destroy();
    }
}
