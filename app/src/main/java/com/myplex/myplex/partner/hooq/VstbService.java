package com.myplex.myplex.partner.hooq;

import android.content.Context;
import android.content.Intent;

/**
 * subclass with unique class name to allow more than one application that uses the VSTB Library to
 * run on the same device.
 * <br/>
 * Otherwise you will can get this error:
 * java.lang.SecurityException: Not allowed to start service Intent { act=com.quickplay.vstb.ref.VstbSampleAppService }
 *                              without permission not exported from uid XXXX
 */
public class VstbService {

    /**
     * Default constructor.
     */
    public VstbService() {
        super();
    }

    /**
     * Factory method to make and return stop Intent.
     *
     * @param context Context of the callee.
     *
     * @return The Intent to start this Activity.
     */
    public static Intent makeStopIntent(final Context context) {
        return new Intent(context, VstbService.class);
    }
}
