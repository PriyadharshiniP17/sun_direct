package com.myplex.api;

/**
 * Created by Samir on 12/10/2015.
 */


import com.squareup.okhttp.ResponseBody;

/**
 * TODO
 */
public final class APIResponse<T>{

    private final T body;
    private final T errorBody;
    private boolean isSuccess;
    private String message;

    public APIResponse(T body, T errorBody) {
        this.body = body;
        this.errorBody = errorBody;
    }


    /**
     * HTTP status code.
     */
    public int code() {
        return -1;
    }

    /**
     * HTTP status message.
     */
    public String message() {
        return message;
    }


    /**
     * {@code true} if {@link #code()} is in the range [200..300).
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * The deserialize response body of a {@linkplain #isSuccess() successful} response.
     */
    public T body() {
        return body;
    }

    /** The raw response body of an {@linkplain #isSuccess() unsuccessful} response. */
    public T errorBody() {
        return errorBody;
    }

    @Override
    public String toString() {
        return "APIResponse: body- " + body  + " message- " + message + " code- " + code();
    }
}
