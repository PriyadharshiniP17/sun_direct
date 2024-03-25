package com.myplex.myplex;

import static com.myplex.myplex.BuildConfig.BUILD_TYPE;
import static com.myplex.myplex.BuildConfig.FLAVOR;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDex;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.scheduler.Requirements;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
/*import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;*/
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.myplex.analytics.MyplexAnalytics;
import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.DownloadBandWidth;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.branchIO.BranchIOAnalytics;
import com.myplex.myplex.recievers.DownloadManagerReceiver;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.MOUUpdateRequestStorageList;
import com.myplex.myplex.utils.Util;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.player_sdk.download.DownloadBandwidthConfig;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.comscore.Analytics;
//import com.comscore.PublisherConfiguration;
//import com.facebook.FacebookSdk;
//import com.facebook.appevents.AppEventsLogger;
//import com.hifx.lens.Lens;
//import com.hifx.lens.tracker.LogLevel;
//import com.hifx.ssolib.SSO;




/**
 * Created by Apalya on 10-Dec-15.
 */
public class ApplicationController extends Application {


    private static final String TAG = ApplicationController.class.getSimpleName();
    public static final boolean FLAG_ENABLE_APPSFLYER_TRACKER = true;
    public static final boolean ENABLE_DOWNLOADS = true;
    public static final boolean ENABLE_PARENTAL_CONTROL = true;
    public static final boolean ENABLE_DELETE_SMS_OF_SUBSCRIPTIONS = true;
    public static boolean ENABLE_DIRECT_APP_LAUNCH = true;
    public static final boolean ENABLE_SAVE_LOGS_TO_FILE = false;
    public static final boolean ENABLE_USER_PRIVACY_POLICY_ALERT = false;
    public static boolean IS_PROMO_AD_SHOWN = false;
    public static boolean ENABLE_MIXPANEL_API = true;
    public static final boolean ENABLE_OTP_LOGIN = true;
    public static final boolean ENABLE_FILTER_THROUGH_VIEW = true;
    public static final boolean ENABLE_RATING = false;
    public static boolean ENABLE_RUPEE_SYMBOL = true;
    public static boolean ENABLE_HELP_SCREEN = false;
    public static final boolean ENABLE_SUBSCRIPTION_FROM_OFFER = false;
    public static final boolean ENABE_DEVICE_AUTO_ROTATE_SETTING = true;
    public static final String PARTNER_LOGO_BOTTOM_VISIBILTY = "bottomParnterLogoVisibility";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    public static Context mContext;
    private static ApplicationConfig mDisplayInfo;
    public static boolean sShowLiveTVPlaybackOptHint = true;
    public static boolean shouldUseFirebaseDeveloperMode = false;
    public  static  boolean IS_VERNACULAR_TO_BE_SHOWN = false;
    private static LocalBroadcastManager sLocalBroadcastManager;
    public static boolean shouldMutePreviewContent = true;
    private File downloadDirectory;
    public static boolean didPlayerEncounterError = false;
    public static boolean isMiniPlayerEnabled = false;

    public static Map<String,String> shouldAutoPlayContents = new HashMap<>();
    public static Map<String,String> shouldMuteAutoPlayContents = new HashMap<>();
    public static Map<String,Long> elapsedTimeAutoplayContent = new HashMap<>();

    public static String FIRST_TAB_NAME = "home";
    public static String currentTab = ApplicationController.FIRST_TAB_NAME;

    public static String advertiserID;
    public static boolean shouldUseFetchDownloadManager;
    //Enables ExoPlayer Download Manger
    public static boolean shouldUseExoDownloadManager=true;



    public static int INTERSTRIAL_CLICKS=10;

    public static ArrayMap BITRATE_CAP = new ArrayMap<String, Integer>();

    public static void setOfflineMOUData(MOUUpdateRequestStorageList downloadlist) {
        sMOUNotFiredList = downloadlist;
    }

    public static MOUUpdateRequestStorageList getMOUNotFiredData() {
        if (sMOUNotFiredList == null) {
            initMOUNotFiredListData();
        }
        return sMOUNotFiredList;
    }

    private static void initMOUNotFiredListData() {
        try {
            ApplicationController.getApplicationConfig().offlineMOUPath = mContext.getFilesDir() + "/" + "offlinemoulist.bin";
            LoggerD.debugDownload("getApplicationConfig().offlineMOUPath- " + ApplicationController.getApplicationConfig().offlineMOUPath);
            if (!DownloadUtil.isFileExists(ApplicationController.getApplicationConfig().offlineMOUPath)) {
                LoggerD.debugDownload("getApplicationConfig().offlineMOUPath- " + ApplicationController.getApplicationConfig().offlineMOUPath + " file does not exists");
            } else {
                sMOUNotFiredList = (MOUUpdateRequestStorageList) SDKUtils.loadObject(ApplicationController.getApplicationConfig().offlineMOUPath);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            LoggerD.debugDownload("Exception- " + e.getMessage());
        }
        if (sMOUNotFiredList == null) {
            sMOUNotFiredList = new MOUUpdateRequestStorageList();
        }
    }

    private static MOUUpdateRequestStorageList sMOUNotFiredList;

    public static CardDownloadedDataList getDownloadData() {
        if (sDownloadList == null) {
            initDownloadData();
        }
        return sDownloadList;
    }

    public static LocalBroadcastManager getLocalBroadcastManager(){
        if(sLocalBroadcastManager == null){
            sLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        }

        return sLocalBroadcastManager;
    }

    private static void initDownloadData() {
        try {
            ApplicationController.getApplicationConfig().downloadCardsPath = mContext.getFilesDir() + "/" + "downloadlist.bin";
            LoggerD.debugDownload("getApplicationConfig().downloadCardsPath- " + ApplicationController.getApplicationConfig().downloadCardsPath);
            if (!DownloadUtil.isFileExists(ApplicationController.getApplicationConfig().downloadCardsPath)) {
                LoggerD.debugDownload("getApplicationConfig().downloadCardsPath- " + ApplicationController.getApplicationConfig().downloadCardsPath + " file does not exists");
            } else {
                sDownloadList = (CardDownloadedDataList) SDKUtils.loadObject(ApplicationController.getApplicationConfig().downloadCardsPath);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            LoggerD.debugDownload("Exception- " + e.getMessage());
        }
        if (sDownloadList == null) {
            sDownloadList = new CardDownloadedDataList();
        }
    }

    public static String getAdvertiserID() {
        return advertiserID;
    }

    public static void setAdvertiserID(String advertiserID) {
        ApplicationController.advertiserID = advertiserID;
    }

    public static CardDownloadedDataList sDownloadList;
    /*private static Tracker mTracker;*/
    public static boolean sIsGridView = true;
    public static final int PRELOAD_TIME_S = 20;
    private static List<String> sListSubscribedPackageIds;

    private static FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static List<String> getCardDataPackages() {
        return sListSubscribedPackageIds;
    }

    private static List<CardDataPackages> sCardDataPackages;

 /*   public static MixpanelAPI getMixPanel() {
        // Initialize the Mixpanel library for tracking and push notifications.
        if (mMixpanel == null)
            mMixpanel = MixpanelAPI.getInstance(mContext, mContext.getResources().getString(R.string.com_mixpanel_api_key));
        return mMixpanel;
    }*/

    public static FirebaseRemoteConfig getmFirebaseRemoteConfig(){
        long cacheExpiration;
        if (BuildConfig.DEBUG) {
            cacheExpiration = 0;
        } else {
            cacheExpiration = 43200L; // 12 hours same as the default value
        }
        if (mFirebaseRemoteConfig == null) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_for_meta);
            if (shouldUseFirebaseDeveloperMode) {
                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(cacheExpiration)
                        .build();
                mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            }
        }else{
            return mFirebaseRemoteConfig;
        }
        return mFirebaseRemoteConfig;
    }

    public static void setSubscribedPackages(List<CardDataPackages> results) {
        preparePackageIds(results);
        myplexAPISDK.PARAM_TO_SEND_ALL_PACKAGES_FIELD = Util.isToSendAllPackagesField();
    }

    public static void clearPackagesList(){
        if (sListSubscribedPackageIds != null) {
            sListSubscribedPackageIds.clear();
        }
        if (sListSubscribedPackageIds == null) {
            sListSubscribedPackageIds = new ArrayList<>();
        }
    }

    private static void preparePackageIds(List<CardDataPackages> results) {
        if (sListSubscribedPackageIds == null) {
            sListSubscribedPackageIds = new ArrayList<>();
        }
        for (int i = 0; i < results.size(); i++) {
            sListSubscribedPackageIds.add(i, results.get(i).packageId);
        }
    }

    public static void setDownloadData(CardDownloadedDataList downloadlist) {
        sDownloadList = downloadlist;
    }

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    /*Map<TrackerName, Tracker> mTrackers = new HashMap<>();*/

    public static int pageVisiblePos = 0;
    public static int pageItemPos =0;
    public static final int TIME_SHIFT = 15;

    public static final boolean ENABLE_AUTO_LOGIN_SCREEN = true;
    public static boolean SHOW_DEUBUG_SETTINGS = false;
    public static boolean ENABLE_BROWSE_PAST_EPG = false;
    public static boolean isDateChanged = false;
    public static boolean SHOW_PLAYER_LOGS = false;
    public static boolean ENABLE_MOU_TRACKING = true;
    public static boolean iS_CIRCLE_BASED_REQ = true;
    public static final boolean FLAG_ENABLE_OFFER_SUBSCRIPTION = true;
    public static final boolean FLAG_ENABLE_ADS = true;
    public static final boolean FLAG_ENABLE_TRYNBUY_SUBSCRIPTION = false;

    public static int CURRENT_SELECTED_PAGE_POS =0;
    public static int DATE_POSITION = 0 ;
    public static final boolean ENABLE_APP_UPDATE_CHECK = true;
    public static final boolean FLAG_ENABLE_SUBSCRIPTION_ON_WIFI = false;
    public static boolean ENABLE_HOOQ_VSTB_SDK = true;
    public static boolean IS_DEBUG_BUILD = false;
    public static boolean ENABLE_COMMENTS_SECTION = false;
    public static boolean ENABLE_FAVOURITE = true;
    public static final boolean ENABLE_CHROME_CAST = true;
    public static final boolean ENABLE_LIVETV_AUTO_PLAY = false;
    public static boolean shouldUseCustomLoadControl = true;
    public static final boolean ENABLE_ON_BOARDING = false;
    public static  boolean IS_FROME_HOME = true;
    public static  boolean IS_FROM_CONTINUE_WATCHING = false;


    @Override
    public void onCreate() {
        /*CleverTapAPI.changeCredentials(getString(R.string.clevertap_account_id), getString(R.string.clevertap_account_token));
        ActivityLifecycleCallback.register(this);*/
        FirebaseApp.initializeApp(this);
        shouldUseFirebaseDeveloperMode = getResources().getBoolean(R.bool.shouldUseFirebaseDevMode);
        //registerActivityLifecycleCallbacks(new FrameMetricsLogger.Builder().build());

        try {
            Class.forName("android.os.AsyncTask"); //it prevents from crashing Android 4.0.x
        } catch (Throwable ignored) {

        }
        IS_DEBUG_BUILD = BuildConfig.DEBUG;
        super.onCreate();

        SDKLogger.debug("IS_DEBUG_BUILD- " + IS_DEBUG_BUILD);
        if (IS_DEBUG_BUILD) {
            //VmaxSdk.setLogLevel(VmaxSdk.LogLevel.DEBUG);

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    // Only log right now because there are still violations that are being found.
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    // Only log because code that is out of our control such as Crashlytics can cause leaks.
                    .penaltyLog()
                    .build());
        }
   /*     if(getResources().getBoolean(R.bool.crashlytics_enable)){
            // Initializes Fabric for builds that don't use the debug build type.
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                    .build();
            Fabric.with(this, crashlyticsKit);
        }*/
        mContext = getApplicationContext();
//        mTracker = getTracker(TrackerName.APP_TRACKER, this);
        //        String setkeepAliveSystermProperty = System.setProperty("http.keepAlive", "false");
//        String getkeepAliveProperty = System.getProperty("http.keepAlive");
//        //Log.d(TAG, "init setkeepAliveSystermProperty- " + setkeepAliveSystermProperty + " \n setkeepAliveSystermProperty- " + getkeepAliveProperty);
        myplexAPISDK.sdkInitialize(this);
//        FacebookSdk.fullyInitialize();
//        AppEventsLogger.activateApp(this);
        FirebaseAnalytics.getInstance().init(this,true,true);
        FirebaseCrashlytics.getInstance().setUserId(""+PrefUtils.getInstance().getPrefUserId());
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefMsisdnNo())){
            FirebaseCrashlytics.getInstance().setCustomKey("Mobile",PrefUtils.getInstance().getPrefMsisdnNo());
        }
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefEmailID())) {
            FirebaseCrashlytics.getInstance().setCustomKey("Email", PrefUtils.getInstance().getPrefEmailID());
        }
        // CleverTap.init(this);
        //Default value kept
        PrefUtils.getInstance().setForceAcceptLanguage("hindi");
        String serviceName = mContext.getString(R.string.serviceName);
        PrefUtils.getInstance().setDefaultServiceName(serviceName);
        IS_VERNACULAR_TO_BE_SHOWN = true;
        ENABLE_HOOQ_VSTB_SDK = PrefUtils.getInstance().getPrefIsHooq_sdk_enabled();
        LeakCanary.install(this);
        //Log.d(TAG, "base url: " + getString(R.string.config_domain_name));
      /*  if (BuildConfig.DEBUG)
            APIConstants.BASE_URL = getString(R.string.staging_domain_name);
        else
            APIConstants.BASE_URL = getString(R.string.staging_domain_name);*/
        if(FLAVOR.equals("sundirectdev"))
            APIConstants.BASE_URL = getString(R.string.staging_domain_name);
        else if(FLAVOR.equals("sundirectpreprod"))
            APIConstants.BASE_URL = getString(R.string.pre_production_domain_name);
        else if(FLAVOR.equals("sundirectprod"))
            APIConstants.BASE_URL = getString(R.string.production_domain_name);
        else
            APIConstants.BASE_URL = getString(R.string.staging_domain_name);

        APIConstants.TENANT_ID=getString(R.string.tenant_id);
        APIConstants.DEVICE_REG_SALT1=getString(R.string.device_reg_salt1);
        APIConstants.DEVICE_REG_SALT2=getString(R.string.device_reg_salt2);
        APIConstants.DEVICE_REG_SALT3=getString(R.string.device_reg_salt3);

        APIConstants.MEDIA_DOMAIN = getString(R.string.media_domain);
        APIConstants.MEDIA_SCHEME  = getString(R.string.media_scheme);
        //Will be set based on App-Flavour HTTP is used for
        // Platform Flavor, and https for all others
        //Can be configured by adding the value below to respective Config files
        APIConstants.SCHEME =  getString(R.string.schemeForBaseUrl);

        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("EA13D4[]E1EACD25F3A26CEF5E81B16E72")).build();
        MobileAds.setRequestConfiguration(configuration);
       /* if (PrefUtils.getInstance().getWhiteMode()) {
            Log.v("application..","11111111111111");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            Log.v("application..","222222222222222222");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }*/

        ENABLE_MIXPANEL_API = false;
//        if (SHOW_PLAYER_LOGS && ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
//            SDKUtils.captureLogsToSDCard();
//        }
        MyplexAnalytics.getInstance().registerAnalyticsLibs(new BranchIOAnalytics());
        if (ENABLE_HOOQ_VSTB_SDK) {
            //TODO: disable hooq content by reading properties response

        }

        BITRATE_CAP.put("vod_sd_auto_max", 4300);
        BITRATE_CAP.put("vod_sd_auto_min", 500);
        BITRATE_CAP.put("vod_sd_hd_max", 3000);
        BITRATE_CAP.put("vod_sd_hd_min", 1500);
        BITRATE_CAP.put("vod_sd_medium_max", 1400);
        BITRATE_CAP.put("vod_sd_medium_min", 800);
        BITRATE_CAP.put("vod_sd_low_max", 700);
        BITRATE_CAP.put("vod_sd_low_min", 500);

        BITRATE_CAP.put("vod_hd_auto_max", 4300);
        BITRATE_CAP.put("vod_hd_auto_min", 500);
        BITRATE_CAP.put("vod_hd_hd_max", 3500);
        BITRATE_CAP.put("vod_hd_hd_min", 1500);
        BITRATE_CAP.put("vod_hd_medium_max", 2100);
        BITRATE_CAP.put("vod_hd_medium_min", 900);
        BITRATE_CAP.put("vod_hd_low_max", 800);
        BITRATE_CAP.put("vod_hd_low_min", 500);

        BITRATE_CAP.put("live_sd_auto_max", 1300);
        BITRATE_CAP.put("live_sd_auto_min", 80);
        BITRATE_CAP.put("live_sd_hd_max", 1300);
        BITRATE_CAP.put("live_sd_hd_min", 900);
        BITRATE_CAP.put("live_sd_medium_max", 950);
        BITRATE_CAP.put("live_sd_medium_min", 500);
        BITRATE_CAP.put("live_sd_low_max", 400);
        BITRATE_CAP.put("live_sd_low_min", 80);

        BITRATE_CAP.put("live_hd_auto_max", 1300);
        BITRATE_CAP.put("live_hd_auto_min", 200);
        BITRATE_CAP.put("live_hd_hd_max", 3000);
        BITRATE_CAP.put("live_hd_hd_min", 900);
        BITRATE_CAP.put("live_hd_medium_max", 1300);
        BITRATE_CAP.put("live_hd_medium_min", 500);
        BITRATE_CAP.put("live_hd_low_max", 400);
        BITRATE_CAP.put("live_hd_low_min", 120);



        // initBitrateCapArray();
        setUpMyplexDownloadManager();
        initVideoCastManager();
        MultiDex.install(this);
        //if (Util.checkUserLoginStatus()) {
        PrefUtils.getInstance().setAppLaunchCount((PrefUtils.getInstance().getAppLaunchCount())+1);
        int count = PrefUtils.getInstance().getAppLaunchCountUpUntill20();
        if (count <= 20)
            PrefUtils.getInstance().setAppLaunchCountUpUntill20(++count);
        SDKLogger.debug("app launch count- " + count);
        // }
        myplexAPISDK.setmRemoteConfig(getmFirebaseRemoteConfig());
        updateDownloadedData();
        AppsFlyerTracker.VFPLAY_USER_ID = mContext.getResources().getString(R.string.user_id);



//        PublisherConfiguration myPublisherConfig = new PublisherConfiguration.Builder()
//                .publisherId("7947673")// Provide your Publisher ID here.
//                .build();
//         Analytics.getConfiguration().addClient( myPublisherConfig );
//         Analytics.getConfiguration().enableImplementationValidationMode();
//         Analytics.start(mContext);

        //lens
//        Lens.init(mContext);
//        Lens.lensConfig().setEndpoint(APIConstants.LENS_SDK_END_POINT_URL);
//        Lens.lensConfig().schemaName(APIConstants.LENS_SDK_SHEME_NAME);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
//        Lens.lensConfig().EmitterConfig().tick(1).appId(APIConstants.LENS_SDK_APP_ID).version(versionName).buildVersion(versionCode+"").build();
//        Lens.lensConfig().trackerConfig().level(LogLevel.OFF).flushInterval(5).build();

    }

    private void initVideoCastManager() {
        //        0BC2130D -> Default Reciever VF Play
//        9790E03C -> Hungama App ID


        String chromeCastReiverId = PrefUtils.getInstance().getPrefChromeCastRecieverId();
        if (TextUtils.isEmpty(chromeCastReiverId)) {
            chromeCastReiverId = getResources().getString(R.string.chrome_cast_reciever_id);
        }
       /* VideoCastManager.initialize(mContext, new CastConfiguration.Builder(chromeCastReiverId)
                .enableAutoReconnect()
                .enableCaptionManagement()
                .enableLockScreen()
                .enableWifiReconnection()
                .enableNotification()
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_SKIP_NEXT, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)

                .setNextPrevVisibilityPolicy(CastConfiguration.NEXT_PREV_VISIBILITY_POLICY_HIDDEN).build());*/
    }

    private void initBitrateCapArray() {
        PropertiesHandler.BITRATE_CAP.put("vod_sd_hd_tv_show_min",1400);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_hd_tv_show_max",2500);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_auto_tv_show_max", 1400);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_auto_tv_show_min", 600);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_medium_tv_show_max", 1400);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_medium_tv_show_min", 600);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_low_tv_show_max", 800);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_low_tv_show_min", 400);

        PropertiesHandler.BITRATE_CAP.put("vod_hd_auto_tv_show_max", 2000);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_auto_tv_show_min", 500);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_hd_tv_show_max", 3200);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_hd_tv_show_min", 900);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_medium_tv_show_max", 2000);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_medium_tv_show_min", 650);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_low_tv_show_max", 800);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_low_tv_show_min", 600);

        PropertiesHandler.BITRATE_CAP.put("vod_sd_auto_max", 1400);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_auto_min", 600);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_hd_max", 2000);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_hd_min", 1300);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_medium_max", 1400);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_medium_min", 600);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_low_max", 600);
        PropertiesHandler.BITRATE_CAP.put("vod_sd_low_min", 500);

        PropertiesHandler.BITRATE_CAP.put("vod_hd_auto_max", 2000);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_auto_min", 600);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_hd_max", 3200);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_hd_min", 900);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_medium_max", 2000);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_medium_min", 850);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_low_max", 800);
        PropertiesHandler.BITRATE_CAP.put("vod_hd_low_min", 600);

        PropertiesHandler.BITRATE_CAP.put("live_sd_auto_max", 2300);
        PropertiesHandler.BITRATE_CAP.put("live_sd_auto_min", 200);
        PropertiesHandler.BITRATE_CAP.put("live_sd_hd_max", 2300);
        PropertiesHandler.BITRATE_CAP.put("live_sd_hd_min", 1600);
        PropertiesHandler.BITRATE_CAP.put("live_sd_medium_max", 1400);
        PropertiesHandler.BITRATE_CAP.put("live_sd_medium_min", 800);
        PropertiesHandler.BITRATE_CAP.put("live_sd_low_max", 800);
        PropertiesHandler.BITRATE_CAP.put("live_sd_low_min", 200);

        PropertiesHandler.BITRATE_CAP.put("live_hd_auto_max", 4000);
        PropertiesHandler.BITRATE_CAP.put("live_hd_auto_min", 800);
        PropertiesHandler.BITRATE_CAP.put("live_hd_hd_max", 4000);
        PropertiesHandler.BITRATE_CAP.put("live_hd_hd_min", 2000);
        PropertiesHandler.BITRATE_CAP.put("live_hd_medium_max", 1900);
        PropertiesHandler.BITRATE_CAP.put("live_hd_medium_min", 1000);
        PropertiesHandler.BITRATE_CAP.put("live_hd_low_max", 1000);
        PropertiesHandler.BITRATE_CAP.put("live_hd_low_min", 800);
    }

    /**
     * Returns the stored application context.
     *
     * @return The application context.
     */
    public static Context getAppContext() {
        return ApplicationController.mContext;
    }

    public static ApplicationConfig getApplicationConfig(){
        if(mDisplayInfo == null){
            mDisplayInfo = new ApplicationConfig();
        }
        return mDisplayInfo;
    }


    /**
     * Gets the default {@link } for this {@link Application}.
     *
     * @return tracker
     */
   /* synchronized Tracker getTracker(TrackerName trackerId, Context context) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.tracker)
                    : analytics.newTracker(R.xml.tracker);
            analytics.setLocalDispatchPeriod(30);
            mTrackers.put(trackerId, t);
        }
        // Set the log level to verbose.
        GoogleAnalytics.getInstance(this).getLogger()
                .setLogLevel(Logger.LogLevel.VERBOSE);
        return mTrackers.get(trackerId);
    }

    public static Tracker getTracker(){return mTracker;}*/

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }



    protected static String userAgent;


    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = getExternalCacheDir();
            if (downloadDirectory == null) {
                downloadDirectory = getCacheDir();
            }
        }
        return downloadDirectory;
    }



    /*
       TO update downloaded content status, if downloaded status not update when user kills the app while downloading any content
    */
    public static void updateDownloadedData() {
        CardDownloadedDataList downloadlist;
        try {
            downloadlist = ApplicationController.getDownloadData();
        } catch (Exception e) {
            return;
        }

        if (downloadlist == null) {
            return;
        }
        HashMap<String, CardDownloadData> mDownloadedList = downloadlist.mDownloadedList;
        if (mDownloadedList == null || mDownloadedList.isEmpty()) {
            return;
        }
        CardDownloadData cardDownloadData;
        boolean isCardDownloadDataUpdated = false;

        for (String contentId : mDownloadedList.keySet()) {
            cardDownloadData = mDownloadedList.get(contentId);
            if (cardDownloadData == null) {
                continue;
            }
            SDKLogger.debug( "download complete for content id :" + contentId);
            FetchDownloadProgress fetchDownloadProgress = FetchDownloadProgress.getInstance(ApplicationController.getAppContext());
            int mDownloadStatus = fetchDownloadProgress.getDownloadStatusFromSDK(cardDownloadData._id);

            SDKLogger.debug( "download complete status :" + fetchDownloadProgress.getDownloadFailedReason(mDownloadStatus));
            if (DownloadManagerMaintainer.STATE_COMPLETED == mDownloadStatus && !cardDownloadData.mCompleted) {
                Log.e("DOWNLOADED_STATUS", "SUCCESS from SplashActivity, DownloadId - " + cardDownloadData.mVideoDownloadId);
                cardDownloadData.mCompleted = true;
                isCardDownloadDataUpdated = true;
            }
        }
        if(isCardDownloadDataUpdated)
            SDKUtils.saveObject(downloadlist, ApplicationController.getApplicationConfig().downloadCardsPath);
    }


    private void setUpMyplexDownloadManager() {
        //BandWidths must be provided in kbps only
        DownloadBandwidthConfig config;
        if(!TextUtils.isEmpty(PrefUtils.getInstance().getString(APIConstants.DOWNLOAD_BITRATE_CONFIG))){
            config = getDownloadBandwidthConfig(PrefUtils.getInstance().getString(APIConstants.DOWNLOAD_BITRATE_CONFIG));
        }else{
            DownloadBandwidthConfig.DownloadBandWidthBuilder bandwidthConfig = new
                    DownloadBandwidthConfig.DownloadBandWidthBuilder();
            bandwidthConfig.setDataSaverMin((int) BITRATE_CAP.get("vod_hd_low_min"));
            bandwidthConfig.setDataSaverMax((int) BITRATE_CAP.get("vod_hd_low_max"));

            bandwidthConfig.setGoodMin((int) BITRATE_CAP.get("vod_sd_medium_min"));
            bandwidthConfig.setGoodMax((int) BITRATE_CAP.get("vod_sd_medium_max"));

            bandwidthConfig.setBestMin((int) BITRATE_CAP.get("vod_hd_medium_min"));
            bandwidthConfig.setBestMax((int) BITRATE_CAP.get("vod_hd_medium_max"));

            bandwidthConfig.setHdMin((int) BITRATE_CAP.get("vod_hd_hd_min"));
            bandwidthConfig.setHdMax((int) BITRATE_CAP.get("vod_hd_hd_max"));
            bandwidthConfig.setAdaptiveTrackSelectionEnabled(true);
            config = bandwidthConfig.build();
        }
        Requirements requirements;
        if (PrefUtils.getInstance().isDownloadOnlyOnWifi()) {
            requirements = new Requirements(Requirements.NETWORK_UNMETERED);
        }else{
            requirements = new Requirements(Requirements.NETWORK);
        }

        DownloadManagerMaintainer.getInstance()
                .initDownloadManager(this,
                        3,
                        config,
                        requirements,
                        getPackageName(), DownloadManagerReceiver.class.getCanonicalName());
    }

    public static DownloadBandwidthConfig getDownloadBandwidthConfig(String downloadBitrateConfigAndroid){
        Gson gson = new Gson();
        try {
            DownloadBandWidth bandWidth = gson.fromJson((downloadBitrateConfigAndroid),DownloadBandWidth.class);
            if(bandWidth != null){
                DownloadBandwidthConfig.DownloadBandWidthBuilder bandWidthBuilder = new DownloadBandwidthConfig.DownloadBandWidthBuilder();
                bandWidthBuilder.setDataSaverMin(bandWidth.datasaver_min);
                bandWidthBuilder.setDataSaverMax(bandWidth.datasaver_max);
                bandWidthBuilder.setGoodMin(bandWidth.good_min);
                bandWidthBuilder.setGoodMax(bandWidth.good_max);
                bandWidthBuilder.setBestMin(bandWidth.best_min);
                bandWidthBuilder.setBestMax(bandWidth.best_max);
                bandWidthBuilder.setHdMin(bandWidth.hd_min);
                bandWidthBuilder.setHdMax(bandWidth.hd_max);
                bandWidthBuilder.setAdaptiveTrackSelectionEnabled(false);
                return bandWidthBuilder.build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
