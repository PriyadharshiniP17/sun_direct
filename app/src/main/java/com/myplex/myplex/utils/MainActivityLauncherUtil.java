package com.myplex.myplex.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.RequestMySubscribedPacks;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.MySubscribedPacksResponseData;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;

import java.util.List;

/**
 * Created by Srikanth on 10/27/2015.
 */
public class MainActivityLauncherUtil {
    private static ProgressDialog mProgressDialog;
    private static boolean isKeyRegenerateRequestMade;
    private static String launchMessage;

    public static void fetchMenuData(final Activity context) {
//        showProgressBar(context);
        new MenuDataModel().fetchMenuList(context.getString(R.string.MENU_TYPE_GROUP_ANDROID_NAV_MENU), 1,APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
//                dismissProgressBar();mIsLogin
                launchAndFinishActivitywithMenuData(context,dataList);
                LoggerD.debugLog("fetchMenuData: onResponse: size - " + dataList);
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
//                dismissProgressBar();
                launchAndFinishActivitywithMenuData(context, dataList);
                LoggerD.debugLog("fetchMenuData: onResponse: size - " + dataList);
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
//                dismissProgressBar();
                String reason = (error == null || error.getMessage() == null) ? "NA" : error.getMessage();

                LoggerD.debugLog("fetchMenuData: onOnlineError: error- " + reason + " errorCode- " + errorCode);
//                TODO Check if previous response data is available.
                if (reason.equalsIgnoreCase("ERR_INVALID_SESSION_ID") && errorCode == 401 && !isKeyRegenerateRequestMade) {
                    isKeyRegenerateRequestMade = true;
                    SDKUtils.makeReGenerateKeyRequest(context, new SDKUtils.RegenerateKeyRequestCallback() {
                        @Override
                        public void onSuccess() {
                            fetchMenuData(context);
                        }

                        @Override
                        public void onFailed(String msg) {
                            if (APIConstants.menuListPath == null) {
                                APIConstants.menuListPath = context.getFilesDir() + "/" + "menuList.bin";
                            }
                            List<CarouselInfoData> carouselInfoDatas = (List<CarouselInfoData>) SDKUtils.loadObject(APIConstants.menuListPath);
                            launchAndFinishActivitywithMenuData(context,carouselInfoDatas);
                        }
                    });
//                    cacheManagerCallback.OnlineError(new Throwable(""), errorCode);
                    return;
                }
                if (APIConstants.menuListPath == null) {
                    APIConstants.menuListPath = context.getFilesDir() + "/" + "menuList.bin";
                }
                List<CarouselInfoData> carouselInfoDatas = (List<CarouselInfoData>) SDKUtils.loadObject(APIConstants.menuListPath);
                launchAndFinishActivitywithMenuData(context,carouselInfoDatas);
            }

        });

    }

    public static void initStartUpCalls(Activity context) {
        launchMessage = null;
      /*  if (Util.checkUserLoginStatus()) {
            fetchMyPackages(context);
        } else {*/
            fetchMenuData(context);
      //  }
        if (PrefUtils.getInstance().getPrefIsHooq_sdk_enabled()) {
            //      TODO Start HOOQ SDK initialization.
//      since it is registered user, launch hooq library initialization instead main activity.
            LoggerD.debugHooqVstbLog("LoginFragment: launchMainActivity> starting hooq vstb sdk");
//            HooqVstbUtilityHandler.getInstance().requestOptionalPermissions((Activity) context);

        }
    }

    private static void launchAndFinishActivitywithMenuData(Activity context, List<CarouselInfoData> carouselInfoDataList) {
        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CacheManager.setCarouselInfoDataList(carouselInfoDataList);
        mainActivity.putExtra(MainActivity.INTENT_PARAM_TOAST_MESSAGE, launchMessage);
        mainActivity.putExtra("isFromSplash", true);
        if(getNotificationViewAllData()!=null){
            mainActivity.putExtra(APIConstants.NOTIFICATION_PARAM_LAYOUT,notificationViewAllData.getString(APIConstants.NOTIFICATION_PARAM_LAYOUT));
            mainActivity.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT,notificationViewAllData.getString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
            mainActivity.putExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME,notificationViewAllData.getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
            mainActivity.putExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE,notificationViewAllData.getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
            mainActivity.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID,notificationViewAllData.getString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID));
            if(notificationViewAllData.getString(APIConstants.NOTIFICATION_LAUNCH_URL) != null )
                mainActivity.putExtra(APIConstants.NOTIFICATION_LAUNCH_URL,notificationViewAllData.getString(APIConstants.NOTIFICATION_LAUNCH_URL));
        }
        setNotificationViewAllData(null);
        context.startActivity(mainActivity);
        if((context != null && context instanceof  LoginActivity))
            ((LoginActivity) context).finish();
    }

    public static void setNotificationViewAllData(Bundle notificationViewAllData){
        MainActivityLauncherUtil.notificationViewAllData=notificationViewAllData;
    }

    public static Bundle getNotificationViewAllData() {
        return notificationViewAllData;
    }

    private static Bundle notificationViewAllData;

    private static void fetchMyPackages(final Activity context) {
        //Content list call

//        AlertDialogUtil.showProgressAlertDialog(mContext);
        RequestMySubscribedPacks mRequestRequestContentList = new RequestMySubscribedPacks(new APICallback<MySubscribedPacksResponseData>() {
            @Override
            public void onResponse(APIResponse<MySubscribedPacksResponseData> response) {
                if (response == null || response.body() == null || response.body().results == null) {
                    ApplicationController.ENABLE_RUPEE_SYMBOL = false;
                }
                fetchMenuData(context);
                if (response != null
                        && response.body() != null
                        && response.body().results != null)
                    ApplicationController.setSubscribedPackages(response.body().results);
//                if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
//                    initUI();
//                } else {
//                    loadCarouselInfo();
//                }
//                //Log.d(TAG, "fetchMyPackages: onResponse: size - " + response.body().results.size());
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                ApplicationController.ENABLE_RUPEE_SYMBOL = false;
                fetchMenuData(context);
//                //Log.d(TAG, "fetchMyPackages: onResponse: t- " + t);
//                if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
//                    initUI();
//                } else {
//                    loadCarouselInfo();
//                }
            }
        });
        APIService.getInstance().execute(mRequestRequestContentList);
    }

    public static void initStartUpCalls(Activity activity, String message) {
        launchMessage = message;
    /*    if (Util.checkUserLoginStatus()) {
            fetchMyPackages(activity);
        } else*/ {
            fetchMenuData(activity);
        }
        if (PrefUtils.getInstance().getPrefIsHooq_sdk_enabled()) {
            //      TODO need to go through hooq sdk initialization.
//      since it is registered user, launch hooq library initialization instead main activity.
            LoggerD.debugHooqVstbLog("LoginFragment: launchMainActivity> starting hooq vstb sdk");
//            HooqVstbUtilityHandler.getInstance().requestOptionalPermissions((Activity) context);

        }
    }
}
