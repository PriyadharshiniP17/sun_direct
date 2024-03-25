package com.myplex.api;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Samir on 12/10/2015.
 */
public abstract class APIRequest {

    protected final APICallback mListener;

    public static final int ERR_NO_NETWORK = -300;
    public static final int ERR_UN_KNOWN = -200;

    public APIRequest(APICallback mListener) {
        this.mListener = mListener;
    }
    /**
     * Create a successful response from {@code apiResponse}
     */
    public void onResponse(APIResponse apiResponse){
        if(mListener != null){
            mListener.onResponse(apiResponse);
        }
    }

    /**
     * Create a synthetic error response with an HTTP status code of {@code code} and {@code t}
     * as the error body.
     */
    public void onFailure(Throwable t,int errorCode){
        if(mListener != null){
            mListener.onFailure(t, errorCode);
        }
    }

    /**
     * check the error code is related to network
     * if its related network error result will be true o else false
     * @param t error response
     * @return network error existence
     */
    public boolean isNetworkError (Throwable t){

        boolean isNetworkError = false;
        if (t == null) return isNetworkError;
        if (t instanceof UnknownHostException
                || t instanceof ConnectException || t instanceof SocketTimeoutException) {
            isNetworkError = true;
        }

        return isNetworkError;
    }
    protected abstract void execute(myplexAPI myplexAPI);

}
