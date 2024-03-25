package com.myplex.util;

import com.myplex.api.myplexAPISDK;

/**
 * Created by Samir on 12/10/2015.
 */
public class Validate {
    public static void sdkInitialized() {
        if (!myplexAPISDK.isInitialized()){
            throw new RuntimeException(
                    "The SDK has not been initialized, make sure to call " +
                            "myplexAPISDK.sdkInitialize() first.");
        }
    }

    public static void notNull(Object arg, String name) {
        if (arg == null) {
            throw new NullPointerException("Argument '" + name + "' cannot be null");
        }
    }

}
