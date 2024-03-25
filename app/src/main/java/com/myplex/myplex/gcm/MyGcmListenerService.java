/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myplex.myplex.gcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


import com.github.pedrovgs.LoggerD;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.StringEscapeUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.LocalLanguageUtil;
import com.myplex.myplex.utils.LocalLanguageUtil.Languages;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.WeakRunnable;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyGcmListenerService extends FirebaseMessagingService {

    public static final String NOTIFICATION_PROMO_TXT = "promo";
    private String _ll = Languages.english.toString();
    private boolean isSupportLang;
    private Intent cIntent;
    private static final String TAG = MyGcmListenerService.class.getSimpleName();

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String NOTIFICATION_ACTION = "action";
    private Context mContext;


    private HelperTarget mTarget;
    private HelperTarget.TargetListener targetListener;


    // [{google.sent_time=1494842013396,

    // google.message_id=0:1494842013403164%d3c6efbff9fd7ecd,
// wzrk_pivot=wzrk_default,
// wzrk_sound=false,
// wzrk_id=1494841940_20170515,
// wzrk_pn=true}]
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mContext = this;
        String from = remoteMessage.getFrom();
        //Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Remotemessage: " + remoteMessage.getNotification());
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationContent = remoteMessage.getNotification().getBody();
        Uri uri = remoteMessage.getNotification().getImageUrl();
        String _id = remoteMessage.getMessageId();
//        String st = data.getString("st");
//        String ver = data.getString("ver");
        Bundle bundle = new Bundle();
        bundle.putString("title",notificationTitle);
        bundle.putString("mp_message",notificationContent);
        bundle.putString("image_url", String.valueOf(uri));
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        LoggerD.debugLog("onMessageReceived from- " + from + "" +
                "Message:  " + bundle);
        final Bundle data = bundle;
        LoggerD.debugLog("onMessageReceived from- " + from + "" +
                " data- " + data);
        if (!data.containsKey("mp_message")
                && !(data.containsKey(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_WZRK_PIVOT)
                && APIConstants.PARAM_CLEVERTAP_NOTIFICATION_WZRK_DEFAULT.equalsIgnoreCase(data.getString(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_WZRK_PIVOT)))
                || !PrefUtils.getInstance().isNotificationEnabled()) {
            return;
        }

        processMessageData(data);

    }

    /**
     * In order to generate debug notification or to get access to showNotification of MyGcmListenerService, the method requires the context so set context through this method.
     */
    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * custom bundle data from notification tool or debug message to showNotification.
     */
    public void processMessageData(final Bundle data) {
        final String message = data.getString("mp_message");

        String _id = data.getString("_id");
        String st = data.getString("st");
        String ver = data.getString("ver");


        Analytics.gaNotificationEvent(Analytics.EVENT_ACTION_RECIEVED, message);
        AppsFlyerTracker.eventNotificationRecieved(new HashMap<String, Object>());
        //Log.d(TAG, "Message: " + message);
        //Log.d(TAG, "st: " + st);
        //Log.d(TAG, "_id: " + _id);

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        if ("0".equals(st)) { //  need to
            //Display notification with out the image.
            handleIntentService(data, null);
            return;
        }
        if (data.containsKey(APIConstants.FA_NOTIFICATION_PARAM_IMAGE_URL)) {
            //Log.d(TAG, "imageUrl is available return to show available image url");
            handleChannelImageData(null, data);
            return;
        }
        if (data.containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
            handleIntentService(data, null);
            return;
        }
        handleIntentService(data, null);
    }

    private void handleIntentService(Bundle bundle, Bitmap bitmap) {

        String message = bundle.getString("mp_message");
        if (bundle.containsKey(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_NM)) {
            message = bundle.getString(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_NM);
        }
        try {
            final String lastNotificationMessage = PrefUtils.getInstance().getPrefLastNotificationData();
          /*  if (message != null && message.equalsIgnoreCase(lastNotificationMessage)) {
                return;
            }*/
            PrefUtils.getInstance().setPrefLastNotificationData(message);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d(TAG, "MP GCM Unable to compare the previous notification message:");
            throw new RuntimeException("MP GCM Unable to compare the previous notification message");
        }
        if (TextUtils.isEmpty(message)) {
            return;
        }
        final String _id = bundle.getString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);

        final String nNotificationType = bundle.getString("type");

        //Log.d(TAG, "MP GCM notification received: " + message);
        //Log.d(TAG, "MP GCM notification _id received: " + _id);

        final String latestVersion = bundle.getString("ver");
        /*Analytics.gaNotificationRecieved(context, intent);*/

        final String page = bundle.getString(APIConstants.NOTIFICATION_PARAM_PAGE);
        String mp_title = bundle.getString("mp_title");

        if (bundle.containsKey(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_NT)) {
            mp_title = bundle.getString(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_NT);
        }
        final String vurl = bundle.getString(APIConstants.NOTIFICATION_PARAM_VURL);

        final String yuid = bundle.getString(APIConstants.NOTIFICATION_PARAM_YUID);

        final String url = bundle.getString(APIConstants.NOTIFICATION_PARAM_URL);
        //Log.d(TAG, "MP GCM notification url received: " + url);

        final String _aid = bundle.getString(APIConstants.NOTIFICATION_PARAM_AID);
        final String partnerId = bundle.getString(APIConstants.NOTIFICATION_PARAM_PARTNER_ID);
        final String partnerName = bundle.getString(APIConstants.NOTIFICATION_PARAM_PARTNER_NAME);
        final String carouselName = bundle.getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME);
        final String carouselPageCount = bundle.getString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT);
        final String carouselLayout = bundle.getString(APIConstants.NOTIFICATION_PARAM_LAYOUT);
        final String carouselTitle = bundle.getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE);
        final String launchURL = bundle.getString(APIConstants.NOTIFICATION_LAUNCH_URL);
        if (bundle.containsKey("_ll")) {
            _ll = bundle.getString("_ll");
        }

        final String nid = bundle.getString(APIConstants.NOTIFICATION_PARAM_NID);
        final String notification_tags = bundle.getString(mContext.getResources()
                .getString(R.string.notification_tags));
        if (null != notification_tags) {
            Analytics.mixPanelPeopleSetNotifcaionTags(notification_tags);
        }
        final PackageManager manager = mContext.getPackageManager();
        Intent appIntent = manager.getLaunchIntentForPackage(mContext
                .getPackageName());
     /*   if (appIntent == null) {
            appIntent = new Intent(mContext, LoginActivity.class);
        }
        if (MainActivity.isOpen) {
            appIntent = new Intent(mContext, MainActivity.class);
        }*/

        if(!Util.checkUserLoginStatus()) {
            appIntent = new Intent(mContext, LoginActivity.class);
        } else {
            appIntent = new Intent(mContext, MainActivity.class);
        }

        if (appIntent != null && !TextUtils.isEmpty(carouselName))
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, carouselName);
        if (appIntent != null && !TextUtils.isEmpty(carouselPageCount))
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, carouselPageCount);
        if (appIntent != null && !TextUtils.isEmpty(carouselLayout))
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_LAYOUT, carouselLayout);
        if (appIntent != null && !TextUtils.isEmpty(launchURL))
            appIntent.putExtra(APIConstants.NOTIFICATION_LAUNCH_URL, launchURL);

        String notificationTitle = mContext.getResources().getString(R.string.app_name);
        if (appIntent != null && !TextUtils.isEmpty(carouselTitle))
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, carouselTitle);
        if (!TextUtils.isEmpty(partnerId)) {
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_ID, partnerId);
            //Log.d(TAG, "partnerId: " + partnerId);
        }

        if (!TextUtils.isEmpty(partnerName)) {
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_NAME, partnerName);
            //Log.d(TAG, "partnerName: " + partnerName);
        }

        if (!TextUtils.isEmpty(mp_title)) {
            notificationTitle = mp_title;
        }
        appIntent.putExtras(bundle);
        appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE, message);
        long when = Calendar.getInstance().getTimeInMillis();
        appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_NOTIFICATION_ID, String.valueOf(when));
        appIntent.putExtra("message_type", "push");
        if (!TextUtils.isEmpty(_id)) {
            appIntent.putExtra("_id", _id);
            String action = bundle.getString(NOTIFICATION_ACTION);
            if (!TextUtils.isEmpty(action)) {
                appIntent.putExtra(NOTIFICATION_ACTION, action);
            }

        }
        if (!TextUtils.isEmpty(nNotificationType)) {
            appIntent.putExtra("type", nNotificationType);
            //Log.d(TAG, "type: " + nNotificationType);
        }

        if (!TextUtils.isEmpty(_aid)) {
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_AID, _aid);
            //Log.d(TAG, "_aid: " + _aid);
        }
        if (!TextUtils.isEmpty(notification_tags)) {
            appIntent.putExtra(mContext.getResources().getString(R.string.notification_tags),
                    notification_tags);
            //Log.d(TAG, "notification_tags: " + notification_tags);
        }
        if (!TextUtils.isEmpty(_id)) {
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
            String action = bundle.getString(NOTIFICATION_ACTION);
            String promo = bundle.getString(NOTIFICATION_PROMO_TXT);
            if (!TextUtils.isEmpty(action)) {
                appIntent.putExtra(NOTIFICATION_ACTION, action);
            }

            if (!TextUtils.isEmpty(promo)) {
                appIntent.putExtra(NOTIFICATION_PROMO_TXT, promo);
            }

        } else if (!TextUtils.isEmpty(page)) {
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, page);
        } else if (!TextUtils.isEmpty(latestVersion)) {
           /* //Log.d(TAG, "MP GCM notification latestVer received: "
                    + latestVersion);*/

            PackageInfo info;
            try {
                int latVer = Integer.valueOf(latestVersion);
                info = manager.getPackageInfo(mContext.getPackageName(), 0);

                if (latVer <= info.versionCode) {
                    // Ignore , app is already up to date.
                    //Log.d(TAG, "Ignore , app is already up to date");
                    return;
                }

            } catch (Throwable e) {
                //Log.d(TAG, "Ignore , error");
                e.printStackTrace();
                return;
            }

            appIntent = new Intent(Intent.ACTION_VIEW);
            // appIntent.setData();
            appIntent.putExtra("ver", latestVersion);

        } else if (!TextUtils.isEmpty(vurl)) {
            //Log.d(TAG, "vurl: " + vurl);
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_VURL, vurl);
        }

        if (!TextUtils.isEmpty(url)) {
            //Log.d(TAG, "url: " + url);
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_URL, url);
        }

        if (!TextUtils.isEmpty(yuid)) {
            //Log.d(TAG, "yuid: " + yuid);
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_YUID, yuid);
        }

        if (!TextUtils.isEmpty(nid)) {
            //Log.d(TAG, APIConstants.NOTIFICATION_PARAM_NID + nid);
            appIntent.putExtra(APIConstants.NOTIFICATION_PARAM_NID, nid);
        }

        cIntent = appIntent;
        isSupportLang = false;
        if (_ll != null && !_ll.equalsIgnoreCase(Languages.english.toString())) {

            if (_ll.equals(Languages.te.name())) {
                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.telugu.toString());
            } else if (_ll.equals(Languages.ta.name())) {
                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.tamil
                                .toString());

            } else if (_ll.equals(Languages.hi.name())) {

                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.hindi
                                .toString());

            } else if (_ll.equals(Languages.gu.name())) {
                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.gujarati
                                .toString());


            } else if (_ll.equals(Languages.bn.name())) {
                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.bengali
                                .toString());


            } else if (_ll.equals(Languages.mr.name())) {
                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.marathi
                                .toString());

            } else if (_ll.equals(Languages.kn.name())) {
                isSupportLang = LocalLanguageUtil
                        .checkLanguageSupport(mContext, Languages.kannada
                                .toString());

            }
        }

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                R.layout.push_notify);
        remoteViews.setTextViewText(R.id.push_subject, notificationTitle);
        remoteViews.setImageViewResource(R.id.push_icon, R.drawable.shreyaset_logo);
        message = StringEscapeUtils.unescapeJava(message);
        if (isSupportLang || _ll.equalsIgnoreCase("english")) {
            remoteViews.setViewVisibility(R.id.push_message_img, View.GONE);
            remoteViews.setViewVisibility(R.id.push_message, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.push_subject, View.VISIBLE);
            remoteViews.setTextViewText(R.id.push_message, message);
        } else {
            remoteViews.setViewVisibility(R.id.push_message_img, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.push_message, View.GONE);
            remoteViews.setViewVisibility(R.id.push_subject, View.VISIBLE);
            remoteViews.setTextViewText(R.id.push_subject, notificationTitle);
            if (Languages.te.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/telugu.ttf"));
            } else if (Languages.hi.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/hindi.ttf"));

            } else if (Languages.ta.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/tamil.ttf"));

            } else if (Languages.gu.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/gujarati.ttf"));

            } else if (Languages.bn.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/bengali.ttf"));

            } else if (Languages.mr.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/marathi.ttf"));

            } else if (Languages.kn.name().equalsIgnoreCase(_ll)) {
                remoteViews.setImageViewBitmap(R.id.push_message_img,
                        LocalLanguageUtil.getFontBitmap(mContext, message,
                                "fonts/kannada.ttf"));
            }
        }

        showNotification(mContext, appIntent,
                notificationTitle,
                message,
                remoteViews,
                bitmap, (int) when);
    }
    // [END receive_message]

    private void handleChannelImageData(List<CardData> dataList, final Bundle notificationData) {
        String imgUrl = null;
        if (dataList != null && !dataList.isEmpty()) {
            CardData cardData = dataList.get(0);
            imgUrl = getChannelImageUrl(cardData);
        }

        if (notificationData.containsKey(APIConstants.FA_NOTIFICATION_PARAM_IMAGE_URL)) {
            imgUrl = notificationData.getString(APIConstants.FA_NOTIFICATION_PARAM_IMAGE_URL);
        }

        //Log.d(TAG, "handleChannelImageData - imgUrl- " + imgUrl);
        if (TextUtils.isEmpty(imgUrl)) {
            handleIntentService(notificationData, null);
            return;
        }

        Handler uiHandler = new Handler(Looper.getMainLooper());
        final String finalImgUrl = imgUrl;
        uiHandler.post(new WeakRunnable<Context>(new WeakReference(mContext)) {
            @Override
            protected void safeRun(Context context) {
                Picasso.get().load(finalImgUrl).into(HelperTarget.getInstance());
                //PicassoUtil.with(mContext).load(finalImgUrl,HelperTarget.getInstance());
                HelperTarget.TargetListener target = new HelperTarget.TargetListener() {
                    @Override
                    public void onTarget(Bitmap bitmap) {
                        handleIntentService(notificationData, bitmap);
                    }
                };
                HelperTarget.getInstance().setOnTargetListener(new WeakReference<>(target));

                /*mTarget = HelperTarget.getInstance();
                if (!TextUtils.isEmpty(finalImgUrl))
                    PicassoUtil.with(context).load(finalImgUrl, mTarget);
                targetListener = new HelperTarget.TargetListener() {
                    @Override
                    public void onTarget(Bitmap bitmap) {
                        handleIntentService(notificationData, bitmap);
                    }
                };
                mTarget.setOnTargetListener(new WeakReference<>(targetListener));*/
            }
        });

        /*AsyncImageRequest imageDataRequest = new AsyncImageRequest(imgUrl);
        imageDataRequest.execute();
        imageDataRequest.setOnResponseListener(new AsyncImageRequest.ResponseListener() {
            @Override
            public void onBitmapResponse(Bitmap stringResponse) {
                handleIntentService(notificationData, stringResponse);
            }
        });*/
    }

    private String getChannelImageUrl(CardData cardData) {
        String imgUrl = null;
        if (cardData.images != null) {
            for (CardDataImagesItem imageItem : cardData.images.values) {
                if (imageItem.type != null
                        && imageItem.type.equalsIgnoreCase("coverposter")
                        && imageItem.profile != null
                        && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)) {
                    if (!TextUtils.isEmpty(imageItem.link)) {
                        imgUrl = imageItem.link;
                    }
                    break;
                }
            }
        }
        return imgUrl;
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void showNotification(Context mContext, Intent notificationIntent, String title, CharSequence message,
                                  RemoteViews remoteViews, Bitmap largeBitmap, int when) {

        try {

            /*create unique this intent from  other intent using setData */

            if (notificationIntent != null && !notificationIntent.hasExtra("ver")) {
                notificationIntent.setData(Uri.parse("content://" + when));
            }
            /*create new task for each notification with pending intent so we set Intent.FLAG_ACTIVITY_NEW_TASK */
            PendingIntent contentIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

// define sound URI, the sound to be played when there's a notification
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && notificationManager != null ) {
                NotificationChannel channel = new NotificationChannel("default",
                        "Play",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Play Notifications");
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "default");

            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable
                    .sundirect_final_app_icon);
            builder.setContentIntent(contentIntent)
                    .setLargeIcon(largeIcon)
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .setShowWhen(false)
                    .setContentTitle(title)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                    .setContentText(message)
                    .setColor(mContext.getResources().getColor(R.color.white));

            if(Build.VERSION.SDK_INT >=33){
                builder.setSmallIcon(R.drawable.sundirect_logo_notification);
            }else {
                builder.setSmallIcon(R.drawable.sundirect_final_app_icon);
            }
            //builder.addAction(android.R.drawable.ic_media_play, "watch now", contentIntent);
            Notification notification;

            //use remoteview only when largebitmap is null or it is not a supported language.
            if (largeBitmap == null && !isSupportLang && !_ll.equalsIgnoreCase("english")) {
                builder.setContent(remoteViews);
            }
            if (Build.VERSION.SDK_INT < 16) {
                notification = builder.getNotification();
                notification.defaults |= Notification.DEFAULT_ALL;
            } else {
                if (largeBitmap != null) {
                    if (DeviceUtils.isTablet(mContext)) {
                        remoteViews.setViewVisibility(R.id.push_image, View.VISIBLE);
                        remoteViews.setImageViewBitmap(R.id.push_image, largeBitmap);
                        builder.setCustomBigContentView(remoteViews);
                    } else {
                        remoteViews.setViewVisibility(R.id.push_image, View.GONE);
                        NotificationCompat.BigPictureStyle bigPictureNotification = new NotificationCompat.BigPictureStyle();
                        bigPictureNotification.bigPicture(largeBitmap).setBigContentTitle(title).setSummaryText(message);
                        builder.setStyle(bigPictureNotification);
                    }
                }
                if (isSupportLang || Languages.english.name().equalsIgnoreCase(_ll)) {
                    builder.setContentTitle(title);
                    NotificationCompat.BigTextStyle bigTextNotification = new NotificationCompat.BigTextStyle();
                    bigTextNotification.bigText(message);
                    builder.setStyle(bigTextNotification);
                    if (largeBitmap != null) {
                        if (DeviceUtils.isTablet(mContext)) {
                            remoteViews.setViewVisibility(R.id.push_image, View.VISIBLE);
                            remoteViews.setImageViewBitmap(R.id.push_image, largeBitmap);
                            builder.setCustomBigContentView(remoteViews);
                        } else {
                            remoteViews.setViewVisibility(R.id.push_image, View.GONE);
                            NotificationCompat.BigPictureStyle bigPictureNotification = new NotificationCompat.BigPictureStyle();
                            bigPictureNotification.bigPicture(largeBitmap).setBigContentTitle(title).setSummaryText(message);
                            builder.setStyle(bigPictureNotification);
                        }
                    }
                    String nNotificationType = cIntent.getStringExtra("type");
                    if (null != cIntent && null != nNotificationType) {
                        if (cIntent.hasExtra(APIConstants.NOTIFICATION_PARAM_AID) && nNotificationType.equalsIgnoreCase("text")) {
                            builder.addAction(android.R.drawable.ic_dialog_info, "catch story!", contentIntent);
                        } else if (cIntent.hasExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID)
                                && nNotificationType.equalsIgnoreCase("video")) {
                            Intent autoPlayIntent = cIntent;
                            autoPlayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            autoPlayIntent.putExtra("action", "autoplay");
                            //Log.d(TAG, "action: autoplay");
                            autoPlayIntent.setAction("play");
                            final PendingIntent autoPlayContentIntent = PendingIntent.getActivity(
                                    mContext.getApplicationContext(), 0, autoPlayIntent, // add this pass
                                    // null to
                                    // intent
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                            builder.addAction(android.R.drawable.ic_media_play, "watch now", autoPlayContentIntent);
                            builder.setAutoCancel(true);
                        } else if (nNotificationType.equalsIgnoreCase("videoandtext")) {
                            builder.addAction(android.R.drawable.ic_dialog_info, "catch story!",
                                    contentIntent);
                            Intent autoPlayIntent = cIntent;
                            autoPlayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            autoPlayIntent.putExtra("action", "autoplay");
                            //Log.d(TAG, "action: autoplay");
                            autoPlayIntent.setAction("play");
                            final PendingIntent autoPlayContentIntet = PendingIntent.getActivity(
                                    mContext.getApplicationContext(), 0, autoPlayIntent, // add this pass
                                    // null to
                                    // intent
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                            builder.addAction(android.R.drawable.ic_media_play, "watch now", autoPlayContentIntet);
                            builder.setAutoCancel(true);
                        }
                    }

                }
                notification = builder.build();
                notification.priority = Notification.PRIORITY_MAX;
            }
            try {
                if (Build.VERSION.SDK_INT >= 21) {
                    int smallIconViewId = mContext.getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

                    if (notification != null && smallIconViewId != 0) {
                        if (notification.contentView != null)
                            notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                        if (notification.headsUpContentView != null)
                            notification.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                        if (notification.bigContentView != null)
                            notification.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            if (notificationManager != null)
                notificationManager.notify(when, notification);
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("firebase token",s);
        SDKLogger.debug(s);
        /*Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);*/
        Context appContext = getApplicationContext();

        try {
            if (mContext != null) {
                RegistrationJobScheduler.enqueueWork(mContext, new Intent());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
