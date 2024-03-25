/*
 * Copyright Quickplay Media Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Quickplay Media Inc. and is protected by copyright
 * law. No license, implied or otherwise is granted by its use, unless licensed by Quickplay Media Inc.
 */
package com.myplex.myplex.partner.hooq;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;


/**
 * @author Yurii Chernyshov
 * @version 1.0
 * @since 2016-01-29
 */

/**
 * Helper class to provide ability to display {@link Toast} from any Thread.
 */
public final class SafeToast {

    /**
     * Handler associated with the UI Thread.
     */
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * default constructor.
     */
    private SafeToast() {
        super();
    }

    /**
     * Show {@link Toast}. This method can be invoked from any Thread.
     *
     * @param text Message to display in the {@link Toast}.
     */
    public static void showAnyThread(final CharSequence text) {

    }

    /**
     * Show {@link Toast}. This method can be invoked from any Thread.
     *
     * @param context Context of the Application.
     * @param text    Message to display in the {@link Toast}.
     */


    /**
     * Show {@link Toast} in the UI Thread.
     *
     * @param context Context of the Application.
     * @param text    Message to display in the {@link Toast}.
     */

}
