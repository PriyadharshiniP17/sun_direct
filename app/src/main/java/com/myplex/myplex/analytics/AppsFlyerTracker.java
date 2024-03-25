package com.myplex.myplex.analytics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.BuildConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by THIVIKRAMREDDY on 6/30/2016.
 */
public class AppsFlyerTracker {


    //Events

    private static final String EVENT_DEVICE_REGISTERED = "device registered";
    private static final String EVENT_USER_REGISTRATION_COMPLETED = "user registration completed";
    private static final String EVENT_COMPLETE_REGISTRATION = "af_complete_registeration";

    private static final String EVENT_LOGIN_SUCCESS = "login success";
    private static final String EVENT_LOGIN_SUCCESS_FOR_IDEA = "af_login";
    private static final String EVENT_LOGIN_SUCCESS_FIRST_TIME = "login success first time";
    private static final String EVENT_LOGIN_SUCCESS_FIRST_TIME_FOR_IDEA = "af_login_first_time";
    private static final String EVENT_OFFER_ACTIVATED = "offer activated";
    private static final String EVENT_NOTIFICATION_RECIEVED = "notification received";
    private static final String EVENT_NOTIFICATION_OPENED = "notificaton opened";
    private static final String EVENT_PLAYED_LIVE_TV = "played live tv";
    private static final String EVENT_PLAYED_MOVIE = "played movie";
    private static final String EVENT_PLAYED_TVSHOWS = "played tv show";
    private static final String EVENT_PLAYED_VIDEO = "played video";
    private static final String EVENT_PLAYED_YOUTUBE = "played youtube";
    private static final String EVENT_TIMESHIFT_LIVE_TV = "timeshift live tv";


    private static final String EVENT_BROWSE_HELP = "browse help";
    private static final String EVENT_BROWSE_ABOUT = "browse about";
    private static final String EVENT_BROWSE_TERMS_AND_CONDITIONS = "browse terms and conditions";
    private static final String PROPERTY_SEARCH_KEYWORD = "keyword";
    private static final String TAG = AppsFlyerTracker.class.getSimpleName();
    private static final String EVENT_PLAYED_MUSIC_VIDEO = "played music video song";
    private static final String EVENT_PLAYED_VIDEO_FOR_30_SEC = "played video for 30 sec";
    private static final String EVENT_PLAYED_VIDEO_FOR_10_SEC = "af_firstvideo_10sec";
    public static  String VFPLAY_USER_ID = "vfplay_user_id";
    private static final String EVENT_PLAYED_VIDEO_FIRST_TIME = "af_first_video ";
    public static final String CONTENT_VIEWED = "af_content_view ";

    public static void eventPlayedVideoFor30Sec(HashMap<String, Object> params) {
        String eventName = AppsFlyerTracker.EVENT_PLAYED_VIDEO_FOR_30_SEC;
        int secs = 30;

        if(BuildConfig.FLAVOR.contains("idea")){
            try {
                eventName = AppsFlyerTracker.EVENT_PLAYED_VIDEO_FOR_10_SEC;
                String prefSecs = PrefUtils.getInstance().getPrefAppsflyerPlaybackEventSeconds();
                if (!TextUtils.isEmpty(prefSecs)) {
                    secs = Integer.parseInt(prefSecs);
                    eventName = "af_firstvideo_" + secs + "sec";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("idea appsflyer",eventName);
            AppsFlyerTracker.trackEvent(eventName, params, AppsFlyerTracker.EventPriority.HIGH);
        }else{
            try {

                String prefSecs = PrefUtils.getInstance().getPrefAppsflyerPlaybackEventSeconds();
                if (!TextUtils.isEmpty(prefSecs)) {
                    secs = Integer.parseInt(prefSecs);
                    eventName = "played video for " + secs + " sec";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppsFlyerTracker.trackEvent(eventName, params, AppsFlyerTracker.EventPriority.HIGH);
        }
    }


    public static final class EventPriority{
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
    };

    private static final int THRESHOLD_EVENT_PRIORITY = EventPriority.LOW;

    private static final Context mContext = ApplicationController.getAppContext();
    private static void trackEvent(String eventName, Map<String,Object> eventValues, int appsFlyerEventPriority){
        if(ApplicationController.IS_DEBUG_BUILD){
            LoggerD.debugAnalytics("AppsFlyerEvent: eventName- " + eventName);
            if(eventValues != null){
                for (String key : eventValues.keySet()){
                    LoggerD.debugAnalytics("\nkey- " + key +
                            " value- " + eventValues.get(key)
                    );
                }
            }
        }
        if (mContext == null ||  appsFlyerEventPriority < THRESHOLD_EVENT_PRIORITY || !ApplicationController.FLAG_ENABLE_APPSFLYER_TRACKER) {
            //Log.d(TAG,"Failed to send AppsFlyer server ignoring event");
            return;
        }
        if (PrefUtils.getInstance().getPrefUserId() != 0) {
            if (eventValues != null)
                eventValues.put(VFPLAY_USER_ID, PrefUtils.getInstance().getPrefUserId());
        }
    }


    public static void eventDeviceRegistrationSuccess(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_DEVICE_REGISTERED, params, AppsFlyerTracker.EventPriority.HIGH);
    }




    public static void eventLoginSuccess(Map<String, Object> params) {
        if(BuildConfig.FLAVOR.contains("idea")){
            Log.d("idea appsflyer",AppsFlyerTracker.EVENT_LOGIN_SUCCESS_FOR_IDEA);
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_LOGIN_SUCCESS_FOR_IDEA, params, AppsFlyerTracker.EventPriority.HIGH);
        }else {
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_LOGIN_SUCCESS, params, AppsFlyerTracker.EventPriority.HIGH);
        }
    }
    public static void eventLoginSuccessFirstTime(Map<String, Object> params) {
        if(BuildConfig.FLAVOR.contains("idea")){
            Log.d("idea appsflyer",AppsFlyerTracker.EVENT_LOGIN_SUCCESS_FIRST_TIME_FOR_IDEA);
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_LOGIN_SUCCESS_FIRST_TIME_FOR_IDEA, params, AppsFlyerTracker.EventPriority.HIGH);
        }else {
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_LOGIN_SUCCESS_FIRST_TIME, params, AppsFlyerTracker.EventPriority.HIGH);
        }
    }

    public static void eventUserRegistrationCompleted(Map<String, Object> params) {
        if(BuildConfig.FLAVOR.contains("idea")){
            Log.d("idea appsflyer",AppsFlyerTracker.EVENT_COMPLETE_REGISTRATION);
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_COMPLETE_REGISTRATION, params, AppsFlyerTracker.EventPriority.HIGH);
        }else{
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_USER_REGISTRATION_COMPLETED, params, AppsFlyerTracker.EventPriority.HIGH);
        }

    }

    public static void eventOfferActivated(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_OFFER_ACTIVATED, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventNotificationRecieved(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_NOTIFICATION_RECIEVED, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventNotificationOpened(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_NOTIFICATION_OPENED, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventPlayedLiveTv(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_LIVE_TV, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventPlayedMovie(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_MOVIE, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventPlayedTvshows(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_TVSHOWS, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventPlayedVideo(Map<String, Object> params) {
        if(BuildConfig.FLAVOR.contains("idea")&& !PrefUtils.getInstance().isVideoPlayedForFirstTime()) {
            PrefUtils.getInstance().setVideoPlayedForFirstTime(true);
            Log.d("idea appsflyer",AppsFlyerTracker.EVENT_PLAYED_VIDEO_FIRST_TIME);
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_VIDEO_FIRST_TIME, params, AppsFlyerTracker.EventPriority.HIGH);
        }else{
            AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_VIDEO, params, AppsFlyerTracker.EventPriority.HIGH);
        }
    }

    public static void eventPlayedYoutube(Map<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_YOUTUBE, params, AppsFlyerTracker.EventPriority.HIGH);
    }

    public static void eventBrowseTab(String tabName){
        if(TextUtils.isEmpty(tabName)){
            return;
        }
        Analytics.mixpanelBrowseTabName(tabName);
        AppsFlyerTracker.trackEvent(Analytics.ACTION_TYPES.browse + " " + tabName,new HashMap<String, Object>(), EventPriority.LOW);
    }


    public static void eventBrowseTabWithSectionViewAll(String tabName, String subSection, boolean isViewAll){
        if(TextUtils.isEmpty(tabName)){
            return;
        }

        subSection = TextUtils.isEmpty(subSection) ? "" : subSection;
        String eventName = Analytics.ACTION_TYPES.browse + " " + tabName + " " + subSection;
        if(isViewAll){
            eventName += " view all";
        }
        AppsFlyerTracker.trackEvent(eventName,new HashMap<String, Object>(), EventPriority.LOW);
    }

    public static void eventBrowseHelp() {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_BROWSE_HELP,new HashMap<String, Object>(), EventPriority.LOW);
    }
    public static void eventBrowseSupport() {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_BROWSE_HELP,new HashMap<String, Object>(), EventPriority.LOW);
    }


    public static void eventBrowseAbout() {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_BROWSE_ABOUT,new HashMap<String, Object>(), EventPriority.LOW);
    }

    public static void eventBrowseTermsAndConditions() {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_BROWSE_TERMS_AND_CONDITIONS,new HashMap<String, Object>(), EventPriority.LOW);
    }
    public static void eventSearchQuery(String query) {
        Map<String, Object> params = new HashMap<>();
        if(!TextUtils.isEmpty(query)){
            params.put(PROPERTY_SEARCH_KEYWORD, query.toLowerCase());
        }
        AppsFlyerTracker.trackEvent(Analytics.ACTION_TYPES.search.name(),params, EventPriority.LOW);
    }

    public static void eventTimeShiftLiveTV() {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_TIMESHIFT_LIVE_TV,new HashMap<String, Object>(), EventPriority.HIGH);
    }

    public static void aftrackEvent(String eventName, Map<String, Object> eventValues, int priority) {
        AppsFlyerTracker.trackEvent(eventName, eventValues, priority);
    }

    public static void eventPlayedMusicVideo(HashMap<String, Object> params) {
        AppsFlyerTracker.trackEvent(AppsFlyerTracker.EVENT_PLAYED_MUSIC_VIDEO, params, AppsFlyerTracker.EventPriority.HIGH);

    }
}
