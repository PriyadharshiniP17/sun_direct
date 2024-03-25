package com.myplex.api;

/**
 * Created by Samir on 12/10/2015.
 */

import javax.security.auth.callback.Callback;

import retrofit2.Retrofit;

/**
 * Communicates responses from a server or offline requests. One and only one method will be
 * invoked in response to a given request.
 * <p>
 * Callback methods are executed using the {@link Retrofit} callback executor. When none is
 * specified, the following defaults are used:
 * <ul>
 * <li>Android: Callbacks are executed on the application's main (UI) thread.</li>
 * <li>JVM: Callbacks are executed on the background thread which performed the request.</li>
 * </ul>
 *
 * @param <T> expected response type
 */
public interface APICallback<T> {
    /** Successful HTTP response. */
    void onResponse(APIResponse<T> response);

    /** Invoked when a network or unexpected exception occurred during the HTTP request. */
    void onFailure(Throwable t, int errorCode);
}
