/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myplex.myplex.utils;

import android.content.Context;
import android.text.TextUtils;

import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.google.android.gms.common.images.WebImage;
import com.myplex.myplex.ui.activities.ExpandedControlsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link OptionsProvider} to provide {@link CastOptions}.
 */
public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
        List<String> buttonActions = new ArrayList<>();

        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK);
        buttonActions.add(MediaIntentReceiver.ACTION_STOP_CASTING);

        int[] compatButtonActionsIndicies = new int[]{ 0, 1 };
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                /*.setActions(Arrays.asList(
                        MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_STOP_CASTING), new int[]{0, 1})*/
                .setActions(buttonActions, compatButtonActionsIndicies)
                .setTargetActivityClassName(ExpandedControlsActivity.class.getName())
                .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .setImagePicker(new ImagePickerImpl())
                .setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName())
                .build();
        String chromeCastReiverId = PrefUtils.getInstance().getPrefChromeCastRecieverId();
        if (TextUtils.isEmpty(chromeCastReiverId)) {
            chromeCastReiverId = context.getString(R.string.chrome_cast_reciever_id);
        }
        return new CastOptions.Builder()
                .setReceiverApplicationId(chromeCastReiverId)
                .setStopReceiverApplicationWhenEndingSession(true)
                .setCastMediaOptions(mediaOptions)
                .setEnableReconnectionService(true)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context appContext) {
        return null;
    }

    private static class ImagePickerImpl extends ImagePicker {

        @Override
        public WebImage onPickImage(MediaMetadata mediaMetadata, int type) {
            if ((mediaMetadata == null) || !mediaMetadata.hasImages()) {
                return null;
            }
            List<WebImage> images = mediaMetadata.getImages();
            if (images.size() == 1) {
                return images.get(0);
            } else {
                if (type == ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
                    return images.get(0);
                } else if(type == ImagePicker.IMAGE_TYPE_NOTIFICATION_THUMBNAIL) {
                    return images.get(1);
                }else{
                    return images.get(0);
                }
            }
        }
    }
}
