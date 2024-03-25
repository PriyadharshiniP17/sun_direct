package com.myplex.myplex.subscribe;


import static com.myplex.api.APIConstants.APP_LAUNCH_NATIVE_OFFER;
import static com.myplex.api.APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION;
import static com.myplex.api.APIConstants.OFFER_ACTION_MOBILE_DATA_ALERT;
import static com.myplex.api.APIConstants.OFFER_ACTION_PAGE_NAVIGATION;
import static com.myplex.api.APIConstants.SUBSCRIPTIONERROR;
import static com.myplex.api.APIConstants.SUBSCRIPTIONINPROGRESS;
import static com.myplex.api.APIConstants.SUBSCRIPTIONSMSCONFIRMATION;
import static com.myplex.api.APIConstants.SUBSCRIPTIONSMSFAILED;
import static com.myplex.api.APIConstants.SUBSCRIPTIONSUCCESS;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.BundleRequest;
import com.myplex.api.request.content.ContentDetails;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardResponseData;
import com.myplex.model.OfferResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.APIAnalytics;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.sms.SMSHandler;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.OtpReader;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.util.StringEscapeUtils;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

//import com.myplex.myplex.BuildConfig;

public class SubscriptionWebActivity extends AppCompatActivity implements AlertDialogUtil.DialogListener, OtpReader.OTPListener2 {
    public static final int SUBSCRIPTION_REQUEST = 100;
    public static final String TAG = "SubscriptionView2";
    private static final String SUBSCR_CALLBACK_SUBSCRIPTION_CANCEL = "status";
    private static final String RESPONSE_ACTION_SHOWMESSAGE = "showMessage";
    private static final String RESPONSE_ACTION_SHOWMESSAGE_AND_CLOSE = "showMessageAndClose";
    private static final String RESPONSE_ACTION_SHOWMESSAGE_AND_RETRY = "showMessageAndRetry";
    private static final String PARAM_IS_TO_REDIRECT_TO_HOME = "isToRedirectToHome";
    private static final String REDIRECT_URL = "redirectUrl";
    public static final String IS_FROM_PREMIUM="isFromPremium";
    public static final String ACTION_URL="actionUrl";
    private static final String CALLBACK_ACTION_OFFER_NATIVE = "redirectUrl";
    private static final int RESPONSE_ACTION_SUCCESS = 200;
    public static final int PARAM_LAUNCH_HOME = 11;
    public static final int PARAM_LAUNCH_NONE = 12;
    public static final int PARAM_LAUNCH_NATIVE_SUBSCRIPTION = 13;
    public static final int PARAM_LAUNCH_NATIVE_OFFER = 14;
    public static final int PARAM_LAUNCH_BANNER = 15;
    private static final String RESULT_ACTION = "resultAction";
    private static final String NETWORK_ERROR_DISCONNECTED = "net::ERR_INTERNET_DISCONNECTED";
    private static final String NETWORK_ERROR_CONN_TIME_OUT = "net::ERR_CONNECTION_TIMED_OUT";
    private static final String NETWORK_ERROR_PROXY_CONN_FAILED = "net::ERR_PROXY_CONNECTION_FAILED";
    private static final String ACTION_CG_SMS_WIFI = "cgSmsWifi";
    public static final int SMS_CONFIRMATION_TIMEOUT = 60;

    private static final String smsSent = "SMS_SENT";
    private static final String smsDelivered = "SMS_DELIVERED";

    private RelativeLayout mLayoutRetry;
    private TextView mTextViewErrorRetryAgain;
    private ImageView mImageViewRetry;
    WebView mWebView = null;
    FbWebViewClient fbwebviewclient = null;
    private String callbackUrl = new String();
    private ProgressDialog mProgressDialog = null;

    private String mUrl;
    public String status;
    private boolean isProgressDialogCancelable = false;

    private static final String IDEA_SUBSCR_CALLBACK_SUCCESS = "myplexnow.tv/?status";
    private static final String IDEA_SUBSCR_CALLBACK_CANCEL = "consent=No";
    private static final String RESPONSE_ACTION_SHOWMESSAGE_REDIRECT = "showMessage/redirect";
    private static final String RESPONSE_ACTION_REDIRECT = "redirect";
    private static final String SHREYAS_ET_ACTION_REDIRECT="myplexapi.shreyaset.com";
    private static  String BCN_ACTION_REDIRECT="qamyplexapi.apexott.com";
    String contentName = null;
    String contentType = null; //SD or HD
    String contentId = null;
    Double contentPrice = null;
    String ctype = null;
    String commercialModel = null;
    String paymentModel = null;
    String transactionid = null;
    String couponCode = null;
    //double priceTobecharged = 0.0;
    double priceTobecharged2 = 0.0;
    String packageId = null;
    private Context mContext;
    private String packageName;
    private int resultAction;
    private boolean isFromPremium;
    private String actionUrl;
    Toolbar mToolbar;
    public static boolean isFromSignIn = false;


    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setUpWebView(mUrl);
        }
    };
    private OtpReader mOtpReader;
    private SMSHandler mSmsHandler;
    private String mReceiverId;
    private String mActivationMessage;
    private SMSSendListener mSmsSentListner;
    private String keyWord;
    private boolean enableSkip = false;

    //	http://api.beta.myplex.in/user/v2/billing/callback/evergent/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DeviceUtils.isTabletOrientationEnabled(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        Analytics.gaCategorySubscription(Analytics.EVENT_ACTION_OFFER,Analytics.EVENT_LABEL_OFFER_SCREEN);
        mContext = this;
        mUrl = new String();
        try {
            Bundle b = this.getIntent().getExtras();
            mUrl = b.getString(REDIRECT_URL);
            isFromPremium=b.getBoolean(IS_FROM_PREMIUM);
            actionUrl=b.getString(ACTION_URL);

            resultAction = b.getInt(SubscriptionWebActivity.RESULT_ACTION);

            isProgressDialogCancelable = b.getBoolean("isProgressDialogCancelable", false);

            contentName = b.getString("contentname");
            contentId = b.getString("contentid");
            contentPrice = b.getDouble("contentprice");
            ctype = b.getString("ctype");
            commercialModel = b.getString("commercialModel");
            paymentModel = b.getString("paymentMode");
            contentType = b.getString("contentType");
            couponCode = b.getString("couponCode");
            priceTobecharged2 = b.getDouble("priceAfterCoupon");
            packageId = b.getString("packageId");
            packageName = b.getString("packageName");
            if (priceTobecharged2 == 0.0) {
                priceTobecharged2 = contentPrice;
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        callbackUrl = APIConstants.SCHEME + APIConstants.BASE_URL
                + APIConstants.SLASH + APIConstants.USER_CONTEXT
                + APIConstants.SLASH + APIConstants.BILLING_EVENT
                + "/callback/evergent/";
        setContentView(R.layout.layout_webview);

        mTextViewErrorRetryAgain = (TextView) findViewById(R.id.textview_error_retry);
        mLayoutRetry = (RelativeLayout) findViewById(R.id.retry_layout);
        mImageViewRetry = (ImageView) findViewById(R.id.imageview_error_retry);
        mLayoutRetry.setVisibility(View.GONE);
        mImageViewRetry.setOnClickListener(mRetryClickListener);
        mTextViewErrorRetryAgain.setOnClickListener(mRetryClickListener);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mToolbar.setContentInsetsAbsolute(0, 0);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.app_bkg));
            ImageView mCloseIcon = findViewById(R.id.toolbar_settings_button);
            mCloseIcon.setOnClickListener(mCloseAction);
            mCloseIcon.setVisibility(View.VISIBLE);
        View mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        TextView mToolbarTitleOtherLang = (TextView) mInflateView.findViewById(R.id.toolbar_header_title_lang);
        ImageView closeIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
        RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        closeIcon.setOnClickListener(mCloseAction);
        ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mToolbar.addView(mInflateView);
            //setSupportActionBar(mToolbar);
            /*if (mToolbar != null) {
                mToolbar.setLogo(R.drawable.app_icon);
            }*/
            setSupportActionBar(mToolbar);


        mWebView = (WebView) findViewById(R.id.webview);
        try {
            if(!isFromPremium){
                mWebView.setBackgroundColor(0x00000000);
                setUpWebView(mUrl);
            }else {
                callPacksApi();
            }

        } catch (Exception e) {
            e.printStackTrace();
            dofinish(APIConstants.SUBSCRIPTIONERROR);
        }
        if(PARAM_LAUNCH_BANNER == resultAction){
            mToolbar.setVisibility(View.VISIBLE);
            mToolbarTitle.setText("");

        }else{
            mToolbar.setVisibility(View.GONE);
        }
    }

    private void callPacksApi() {
        showProgressBar();
        OfferedPacksRequest.Params params = null;
        if (!TextUtils.isEmpty(actionUrl)){
            params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_1002,actionUrl);
        }else {
            params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_1001);
        }
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
                dismissProgressBar();
                if (response == null || response.body() == null) {
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW
                            && response.body().ui != null
                            && response.body().ui.action != null) {
                        switch (response.body().ui.action) {
                            case APIConstants.OFFER_ACTION_SHOW_MESSAGE:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                        @Override
                                        public void onDialogClick(String buttonText) {
                                        }
                                    });
                                }
                                break;
                            case APIConstants.OFFER_ACTION_SHOW_TOAST:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showToastNotification(response.body().message);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_WEB:
                                if (!TextUtils.isEmpty(response.body().ui.redirect)) {
                                    setUpWebView(response.body().ui.redirect);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_OFFER:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                                break;
                            default:
                                break;
                        }
                        return;
                    }

                    boolean isOfferAvailable = false;
                    for (CardDataPackages offerPack : response.body().results) {
                        if (offerPack.subscribed) {
                            return;
                        }
                        if (APIConstants.TYPE_OFFER.equalsIgnoreCase(offerPack.packageType)) {
                            isOfferAvailable = true;
                            break;
                        }
                    }
                    if (isOfferAvailable) {
                        launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                        return;
                    }
                    launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar();
                LoggerD.debugOTP("Failed: " + t);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
            }
        });

        APIService.getInstance().execute(contentDetails);
    }

    private void launchPackagesScreen(int subscriptionType) {
        if (mContext == null) {
            return;
        }
        String source = null;
        String sourceDetails = null;
        mContext.startActivity(LoginActivity.createIntent(mContext, true, true, subscriptionType, source, sourceDetails));
    }



    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub

        super.onStop();
    }


    private boolean isCouponApplied() {
        if (couponCode != null && couponCode.length() > 0) return true;
        return false;
    }

    private void dofinish(final int response) {
        if (response == SUBSCRIPTIONSUCCESS
                || response == SUBSCRIPTIONINPROGRESS) {
//			Util.showToast(SubscriptionView.this, "Subscription: Success",Util.TOAST_TYPE_INFO);
//			Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();
            /*String contentId = null;
            if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo != null
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type != null) {
                if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                        && SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId;
                } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        && SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
                } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
                }
            }*/
//            fetchSubscriptionPackages(contentId, response);
            if (!PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()) {
                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
            }

            switch (resultAction){
                case PARAM_LAUNCH_HOME:
                    launchActivity(response);
                    return;
                case PARAM_LAUNCH_NATIVE_OFFER:
                    launchActivity(response);
                    return;
                case PARAM_LAUNCH_NATIVE_SUBSCRIPTION:
                    launchActivity(response);
                    return;
            }
            closeSession(response);
        } else {
//			Util.showToast(SubscriptionView.this, "Subscription: Cancelled", Util.TOAST_TYPE_ERROR);
//            AlertDialogUtil.showToastNotification("Subscription: Cancelled");
//			Toast.makeText(SubscriptionView.this, "Subscription: Cancelled", Toast.LENGTH_SHORT).show();
             if (response == APIConstants.SUBSCRIPTIONERROR){
//                    getActivity().finish();
                showDeviceAuthenticationFailed(null,response);
             } else {
                 closeSession(response);
             }
        }

    }

    private void dofinish(final int response, String message) {
        if (response == SUBSCRIPTIONSUCCESS
                || response == SUBSCRIPTIONINPROGRESS) {
//			Util.showToast(SubscriptionView.this, "Subscription: Success",Util.TOAST_TYPE_INFO);
//			Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();
            /*String contentId = null;
            if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo != null
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type != null) {
                if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                        && SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId;
                } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        && SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
                } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
                }
            }*/
//            fetchSubscriptionPackages(contentId, response);
            if (!PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()) {
                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
            }

            switch (resultAction){
                case PARAM_LAUNCH_HOME:
                    launchActivity(response);
                    return;
                case PARAM_LAUNCH_NATIVE_OFFER:
                    launchActivity(response);
                    return;
                case PARAM_LAUNCH_NATIVE_SUBSCRIPTION:
                    launchActivity(response);
                    return;
            }
            closeSession(response);
        } else {
//			Util.showToast(SubscriptionView.this, "Subscription: Cancelled", Util.TOAST_TYPE_ERROR);
//            AlertDialogUtil.showToastNotification("Subscription: Cancelled");
//			Toast.makeText(SubscriptionView.this, "Subscription: Cancelled", Toast.LENGTH_SHORT).show();
            if (response == APIConstants.SUBSCRIPTIONERROR){
//                    getActivity().finish();
                showDeviceAuthenticationFailed(message,response);
            } else {
                closeSession(response);
            }
        }
    }
    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            onBackPressed();
        }
    };

    private void showDeviceAuthenticationFailed(String message, final int response) {
        if (message == null) {
            message = mContext.getResources().getString(R.string.canot_connect_server);
        }
        String buttonText = mContext.getString(R.string.feedbackokbutton);

        if (!ConnectivityUtil.isConnected(mContext)
                || NETWORK_ERROR_CONN_TIME_OUT.equalsIgnoreCase(message)
                || NETWORK_ERROR_PROXY_CONN_FAILED.equalsIgnoreCase(message)
                || NETWORK_ERROR_DISCONNECTED.equalsIgnoreCase(message)) {
            message = mContext.getString(R.string.network_error) + " and try again";
        }
        if(response == SUBSCRIPTIONERROR){
            showNetWorkError(message);
            return;
        }
        dismissProgressBar();
        final String finalMessage = message;
        AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", false, buttonText, new AlertDialogUtil.NeutralDialogListener() {
            @Override
            public void onDialogClick(String buttonText) {
                if (mContext.getString(R.string.network_error).equalsIgnoreCase(finalMessage)) {
                    setUpWebView(mUrl);
                } else {
                    closeSession(response);
                }
            }
        });
    }

    private void launchActivity(int response) {
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName("com.myplex.myplex.ui.activities.MainActivity"));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            showDeviceAuthenticationFailed(null,response);
        }
        finish();
    }

    private void fetchSubscriptionPackages(String contentId, final int response) {

        ContentDetails.Params contentDetailsParams = new ContentDetails.Params
                (contentId, "mdpi",
                        "coverposter", 10, APIConstants.HTTP_NO_CACHE);

        final ContentDetails contentDetails = new ContentDetails(contentDetailsParams,
                new APICallback<CardResponseData>() {

                    @Override
                    public void onResponse(APIResponse<CardResponseData> apiresponse) {
                        //Log.d(TAG, "success: " + apiresponse.body());
                        if (null == apiresponse) {
                            closeSession(response);
                            return;
                        }
                        if (null == apiresponse.body()) {
                            closeSession(response);
                            return;
                        }
                        if (null != apiresponse.body().results
                                && apiresponse.body().results.size() > 0) {

                            final CardData subscribedData = SDKUtils.getCardExplorerData()
                                    .cardDataToSubscribe;
                            SDKUtils.getCardExplorerData().cardDataToSubscribe.currentUserData = apiresponse.body().results.get(0).currentUserData;
                            subscribedData.currentUserData = apiresponse.body().results.get(0).currentUserData;
                            List<CardData> dataToSave = new ArrayList<>();
                            dataToSave.add(subscribedData);
                            BundleRequest.Params bundleParams = new BundleRequest.Params
                                    (packageId);
                            BundleRequest bundleUpdateRequest = new BundleRequest(bundleParams,
                                    new APICallback<com.myplex.model.Bundle>() {
                                        @Override
                                        public void onResponse(APIResponse<com.myplex.model.Bundle>
                                                                       bundleResponse) {
                                            if (response == SUBSCRIPTIONINPROGRESS && subscribedData
                                                    .currentUserData != null && subscribedData.currentUserData.purchase != null) {
                                                if (subscribedData.currentUserData.purchase.isEmpty() || subscribedData.currentUserData.purchase.size() == 0) {
                                                    AlertDialogUtil.showNeutralAlertDialog(SubscriptionWebActivity.this,
                                                            "",
                                                            getResources().getString(R.string.transaction_inprogress_message),
                                                            "Ok", new AlertDialogUtil.NeutralDialogListener() {
                                                                @Override
                                                                public void onDialogClick(String buttonText) {
                                                                    closeSession(response);
                                                                }
                                                            });
                                                    return;
                                                }
                                            }
                                            closeSession(response);
//											AlertDialogUtil.showToastNotification("Subscription Info updated");
                                        }

                                        @Override
                                        public void onFailure(Throwable t, int errorCode) {
                                            //Log.d(TAG, "Failed: " + t);
                                            closeSession(response);
                                        }
                                    });
                            APIService.getInstance().execute(bundleUpdateRequest);

                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        closeSession(response);
                    }
                });

        APIService.getInstance().execute(contentDetails);
    }

    private void closeSession(int response) {
        setResult(response, getIntent());
        dismissProgressBar();
        stopOtpReader();
        finish();
    }

    private void setUpWebView(String url) {
//        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //FOR WEBPAGE SLOW UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if(TextUtils.isEmpty(url)){
            dofinish(APIConstants.SUBSCRIPTIONERROR);
            return;
        }
        mWebView.setVisibility(View.VISIBLE);
        mLayoutRetry.setVisibility(View.GONE);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(fbwebviewclient = new FbWebViewClient());
        mWebView.setWebChromeClient(new CustomChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        if(url.contains("sundirect.in")){
            mToolbar.setVisibility(View.VISIBLE);
        }

        mWebView.loadUrl(url);
    }

    private String getTokenValue(String token) {
        String returnValue = new String();
        StringTokenizer subtoken = new StringTokenizer(token, "=");
        while (subtoken.hasMoreTokens()) {
            returnValue = subtoken.nextToken();
        }
        return returnValue;
    }

    public static Intent createIntent(Context context, String redirect, int paramLaunchHome) {
        Intent intent = new Intent(context, SubscriptionWebActivity.class);
        intent.putExtra(SubscriptionWebActivity.REDIRECT_URL, redirect);
        intent.putExtra(SubscriptionWebActivity.RESULT_ACTION, paramLaunchHome);
        return intent;
    }

    public static Intent createIntent(Context context, String redirect, int paramLaunchHome, boolean isFromSignIn1) {
        Intent intent = new Intent(context, SubscriptionWebActivity.class);
        intent.putExtra(SubscriptionWebActivity.REDIRECT_URL, redirect);
        intent.putExtra(SubscriptionWebActivity.RESULT_ACTION, paramLaunchHome);
        isFromSignIn = isFromSignIn1;
        return intent;
    }

    public static Intent createIntent(Context context, String redirect, int paramLaunchHome, String notifcationId, boolean isFromSignin1) {
        Intent intent = new Intent(context, SubscriptionWebActivity.class);
        intent.putExtra(SubscriptionWebActivity.REDIRECT_URL, redirect);
        intent.putExtra(SubscriptionWebActivity.RESULT_ACTION, paramLaunchHome);
        intent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, notifcationId);
        isFromSignIn = isFromSignin1;
        return intent;
    }

    //	status=FAILURE&message=expired+card
    private class FbWebViewClient extends WebViewClient {
        boolean closed = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            //Log.i(TAG, "OVERRIDE " + closed + " " + url);
//			http://www.myplexnow.tv/?status=SUCCESS&message=You%20are%20already%20activate%20for%20this%20service.&action=showMessage
//			http://www.myplexnow.tv/?status=FAILED&action=showMessage&message=Failed&redirectLink=http%3A%2F%2Fwww.myplexnow.tv%3Fstatus%3DFAILED
//		&message=Invalid%20mobile%20number,%20Please%20use%20mobile%20number%20that%20used%20for%20installing%20application
//		mUrl = "http://myplexnow.tv/?status=success&action=nativeOfferPage";

//            myplexnow.tv?status=SMS_CONFIRMATION&action=cgSmsWifi&keyword=xxx&shortCode=xxx&receiverId=xxxx&message=xxxx
            if(url!=null && mContext!=null){
                if(url.contains("close=yes")){
                    if(isFromSignIn) {
                        Intent mIntent = new Intent(mContext, MainActivity.class);
                      //  mIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, "143728");
                        if (getIntent().hasExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID)) {
                            final String _id = getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
                            mIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
                        }
                        mIntent.putExtra("isFromSplash", true);
                        startActivity(mIntent);
                        finish();
                        return true;
                    }else{
                   /* liveWebView.canGoBack();
                    closeFragment();*/
//                        setResult(RESULT_OK);
                      setResult(APIConstants.ONSUBSCRIPTIONDONE);
                        APIConstants.IS_REFRESH_LIVETV1=false;
                        finish();
                        return  true;
                    }

                }
            }
            if (url != null
                    && url.contains("")) {
                Bundle b = SubscriptionWebActivity.this.getIntent().getExtras();
                b.putBoolean("cgPageLoaded", true);
                getIntent().putExtras(b);
                String gaEventAction = "offer cg page loaded";
                Analytics.gaCategorySubscription(gaEventAction,Analytics.EVENT_LABEL_CG_PAGE);
            }
            boolean statusContains = url.contains(SUBSCR_CALLBACK_SUBSCRIPTION_CANCEL);
            boolean statusContains1 = url.contains(callbackUrl);
            boolean statusContains2 = url.contains(IDEA_SUBSCR_CALLBACK_SUCCESS);
            boolean statusContains3 = url.contains(IDEA_SUBSCR_CALLBACK_CANCEL);
            boolean statusContains4 = url.contains(APIConstants.BASE_URL);
            Log.e("billing1",""+statusContains1);
            Log.e("billing2",""+statusContains2);
            Log.e("billing3",""+statusContains3);
            Log.e("billing4",""+statusContains4);
            /*if(BuildConfig.FLAVOR.contains("bcn")){
                if(PrefUtils.getInstance().getAppAcionRedirect()!=null){
                    BCN_ACTION_REDIRECT=PrefUtils.getInstance().getAppAcionRedirect();
                }
                boolean statusContains5 = url.contains(BCN_ACTION_REDIRECT);
                Log.e("billing5",""+statusContains5+"  "+PrefUtils.getInstance().getAppAcionRedirect());
            }else{
                boolean statusContains5 = url.contains(SHREYAS_ET_ACTION_REDIRECT);
                Log.e("billing5",""+statusContains5);
            }*/
            Log.e("billing",""+statusContains);

            if ((url.contains(callbackUrl) || url.contains(IDEA_SUBSCR_CALLBACK_SUCCESS) || url.contains(IDEA_SUBSCR_CALLBACK_CANCEL)
                    || url.contains(SUBSCR_CALLBACK_SUBSCRIPTION_CANCEL)) &&( url.contains(APIConstants.BASE_URL) || (url.contains(SHREYAS_ET_ACTION_REDIRECT) || url.contains(BCN_ACTION_REDIRECT)))) {
                try {
//myplexnow.tv?status=SMS_CONFIRMATION&action=cgSmsWifi&keyword=xxx&shortCode=xxx&receiverId=xxxx&message=xxx&cancelMessage=xxx&activationMessage=xxxx

                    String status = new String();
                    String message = new String();
                    String action = new String();
                    String page = new String();
                    String redirectLink = new String();
                    String keyword = new String();
                    String shortCode = new String();
                    String receiverId = new String();
                    String cancelMessage = new String();
                    String activationMessage = new String();
                    URL aURL = new URL(url);
                    String query = aURL.getQuery();
                    query = URLDecoder.decode(query);
                    StringTokenizer token = new StringTokenizer(query, "&");
                    HashMap<String, String> paramMap = new HashMap<String, String>();
                    while (token.hasMoreTokens()) {
                        String tokenName = token.nextToken();
                        try {
                            String key = tokenName.split("=")[0];
                            String value = tokenName.split("=")[1];
                            paramMap.put(key, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    String statusString = "status";
                    String messageString = "message";
                    String transactionString = "transactionId";
                    String redirectLinkString = "redirectLink";
                    String actionString = "action";
                    String pageString = "page";
                    String shortCodeString = "shortCode";
                    String receiverIdString = "receiverId";
                    String keywordString = "keyword";
                    String cancelMessageString = "cancelMessage";
                    String activationMessageString = "activationMessage";


                    if (paramMap.containsKey(statusString))
                        status = paramMap.get(statusString);
                    status = URLDecoder.decode(status);
                    if (paramMap.containsKey(messageString))
                        message = paramMap.get(messageString);
                    message = URLDecoder.decode(message);

                    if (paramMap.containsKey(redirectLinkString))
                        redirectLink = paramMap.get(redirectLinkString);
                    redirectLink = URLDecoder.decode(redirectLink);

                    if (paramMap.containsKey(actionString))
                        action = paramMap.get(actionString);
                    action = URLDecoder.decode(action);

                    if (paramMap.containsKey(pageString))
                        page = paramMap.get(pageString);
                    page = URLDecoder.decode(page);

                    if (paramMap.containsKey(transactionString)) { //the python server must return transaction-id
                        transactionid = paramMap.get(transactionString);
                    } else {
                        transactionid = contentName + "transactionId";
                    }

                    if (paramMap.containsKey(shortCodeString))
                        shortCode = paramMap.get(shortCodeString);
                    shortCode = URLDecoder.decode(shortCode);

                    if (paramMap.containsKey(receiverIdString))
                        receiverId = paramMap.get(receiverIdString);
                    receiverId = URLDecoder.decode(receiverId);


                    if (paramMap.containsKey(keywordString))
                        keyword = paramMap.get(keywordString);
                    keyword = URLDecoder.decode(keyword);

                    if (paramMap.containsKey(activationMessageString))
                        activationMessage = paramMap.get(activationMessageString);
                    activationMessage = URLDecoder.decode(activationMessage);

                    if (paramMap.containsKey(cancelMessageString))
                        cancelMessage = paramMap.get(cancelMessageString);
                    cancelMessage = URLDecoder.decode(cancelMessage);

                    activationMessage = StringEscapeUtils.unescapeJava(activationMessage);
                    message = StringEscapeUtils.unescapeJava(message);
                    cancelMessage = StringEscapeUtils.unescapeJava(cancelMessage);


                    int responseCode = SUBSCRIPTIONERROR;
                    if (status.equalsIgnoreCase("SUCCESS")) {
                        responseCode = SUBSCRIPTIONSUCCESS;
                    } else if (status.equalsIgnoreCase("ACTIVATION_INPROGRESS")) {
                        responseCode = SUBSCRIPTIONINPROGRESS;
                    } else if (status.equalsIgnoreCase("SMS_CONFIRMATION")) {
                        responseCode = SUBSCRIPTIONSMSCONFIRMATION;
                    }else if (status.equalsIgnoreCase("Failed")) {
                        responseCode = SUBSCRIPTIONSMSFAILED;
                    }
                    else if (status.equalsIgnoreCase("ERR_IN_PROGRESS")) {
                        //Log.d(TAG, "error is progress");
                        AlertDialogUtil.showAlertDialog(mContext, getResources()
                                        .getString(R.string.transaction_server_error)
                                , ""
                                , getResources().getString(R.string.retry)
                                , getResources().getString(R.string.cancel)
                                , SubscriptionWebActivity.this);
                        return true;
                    }
                    Intent data = SubscriptionWebActivity.this.getIntent();
                    switch (action) {
                        case RESPONSE_ACTION_REDIRECT:
                            launchBrowser(redirectLink);
                            dofinish(responseCode);
                            break;
                        case RESPONSE_ACTION_SHOWMESSAGE:
                            if (!TextUtils.isEmpty(message)) {
                                final int finalResponseCode1 = responseCode;
                                AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", false, "Ok", new AlertDialogUtil.NeutralDialogListener() {
                                    @Override
                                    public void onDialogClick(String buttonText) {
                                        if(finalResponseCode1 == SUBSCRIPTIONSUCCESS || finalResponseCode1 == SUBSCRIPTIONINPROGRESS){
                                            dofinish(finalResponseCode1);
                                        }
                                    }
                                });
                            }
                            break;
                        case RESPONSE_ACTION_SHOWMESSAGE_AND_CLOSE:
                            if (!TextUtils.isEmpty(message)) {
                                final int finalResponseCode = responseCode;
                                AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", false, "Ok", new AlertDialogUtil.NeutralDialogListener() {
                                    @Override
                                    public void onDialogClick(String buttonText) {
                                        dofinish(finalResponseCode);
                                    }
                                });
                            }
                            break;
                        case RESPONSE_ACTION_SHOWMESSAGE_AND_RETRY:
                            if (!TextUtils.isEmpty(message)) {
                                AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", false, "Ok", new AlertDialogUtil.NeutralDialogListener() {
                                    @Override
                                    public void onDialogClick(String buttonText) {
                                        setUpWebView(mUrl);
                                    }
                                });
                            }
                            break;
                        case OFFER_ACTION_MOBILE_DATA_ALERT:
                            if (!TextUtils.isEmpty(message)) {
                                AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", false, "Ok", new AlertDialogUtil.NeutralDialogListener() {
                                    @Override
                                    public void onDialogClick(String buttonText) {
                                    }
                                });
                            }
                            break;
                        case APP_LAUNCH_NATIVE_OFFER:
                            data.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, APP_LAUNCH_NATIVE_OFFER);
                            resultAction = PARAM_LAUNCH_NATIVE_OFFER;
                            dofinish(responseCode);
                            break;

                        case ACTION_CG_SMS_WIFI:
//                            data.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, APP_LAUNCH_NATIVE_OFFER);
//                            resultAction = PARAM_LAUNCH_NATIVE_OFFER;
//                            dofinish(responseCode);
                            initiateSMSTransaction(keyword, shortCode, receiverId, message,activationMessage,cancelMessage);
                            return true;
                        case APP_LAUNCH_NATIVE_SUBSCRIPTION:
                            resultAction = PARAM_LAUNCH_NATIVE_SUBSCRIPTION;
                            data.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, APP_LAUNCH_NATIVE_OFFER);
                            dofinish(responseCode);
                            break;
                        case OFFER_ACTION_PAGE_NAVIGATION:
                            if (!TextUtils.isEmpty(page)) {
                                data.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, page);
                            }
                            dofinish(responseCode);
                            break;
                        default:
                            dofinish(responseCode);
                            break;
                    }
                } catch (MalformedURLException e) {
                    dofinish(APIConstants.SUBSCRIPTIONERROR);
                    e.printStackTrace();
                }
                return true;

            }
          /*  if(url.contains("QuickRecharge")){
                mToolbar.setVisibility(View.VISIBLE);

            }else{
                mToolbar.setVisibility(View.GONE);
            }*/
            mToolbar.setVisibility(View.VISIBLE);
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "ONRECEIVEDERROR " + failingUrl + " " + description);
//            net::ERR_INTERNET_DISCONNECTED
//            net::ERR_CONNECTION_TIMED_OUT
            closed = true;
            dofinish(APIConstants.SUBSCRIPTIONERROR,description);
            dismissProgressBar();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //Log.d(TAG, "PageStarted " + url);
            showProgressBar();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //Log.d(TAG, "PageFinished " + url);
            dismissProgressBar();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.cancel(); // Ignore SSL certificate errors
            dismissProgressBar();
        }
    }

    @Override
    public void otpReceived(String messageText) {

    }

    @Override
    public void otpTimeOut() {
        if(!Util.checkActivityPresent(this)){
            //Log.d(TAG, "otpTimeOut: finished no need to handle this return back");
            return;
        }
        String phone = PrefUtils.getInstance().getPrefMsisdnNo();
        if (!TextUtils.isEmpty(phone)) {
            //9876543210
            phone = phone.replace(phone.substring(2,5),"xxx");
            phone = "+91 " + phone;
        } else {
            phone = "";
        }
        String text = String.format(getString(R.string.failed_to_process_request), phone);
        LoggerD.debugLog("formatted phone number- " + text);
        AlertDialogUtil.showToastNotification(text);
        stopOtpReader();
        AlertDialogUtil.dismissProgressAlertDialog();
        //Log.d(TAG, "otpTimeOut: text- " + text);
    }

    @Override
    public void otpReceived(String address, String message) {
        //Log.d(TAG, "otpReceived: address- " + address + " message- " + message);
        if (!TextUtils.isEmpty(mReceiverId)
                && !TextUtils.isEmpty(address)
                && address.toLowerCase().startsWith(mReceiverId)) {
           /* if (ApplicationController.ENABLE_DELETE_SMS_OF_SUBSCRIPTIONS) {
                mSmsSentListner = new SMSSendListener(address, "1", new WeakReference<SubscriptionWebActivity>(SubscriptionWebActivity.this));
                registerReceiver(mSmsSentListner, new IntentFilter(smsSent));
            }*/
            mSmsHandler = new SMSHandler();
            String confiramtionSMS = "1";
            mSmsHandler.sendSMS(address, confiramtionSMS, null, null);
            stopOtpReader();
            APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_SUBSCRIPTION, APIAnalytics.EVENT_SUBSCRIPTION_SENT_CONFIRMATION_SMS, keyWord, String.valueOf(1));
            if (!TextUtils.isEmpty(mActivationMessage)) {
                AlertDialogUtil.showToastNotification(mActivationMessage);
            }
            dofinish(SUBSCRIPTIONSUCCESS);
            AlertDialogUtil.dismissProgressAlertDialog();
        }
    }

    private void startOtpReader() {
        if (mContext == null) {
            LoggerD.debugOTP("mCotext == null");
        }
        mOtpReader = OtpReader.getInstance(mContext, true);
        mOtpReader.start(ApplicationController.getAppContext(), SMS_CONFIRMATION_TIMEOUT);
        mOtpReader.setOtpListener(this);
    }

    private void stopOtpReader() {
        if(mOtpReader == null){
            return;
        }
        mOtpReader.stop();
        mOtpReader.setOtpListener(null);
        mOtpReader = null;
    }


    private void initiateSMSTransaction(final String keyword, final String shortCode, String receiverId, String message, String activationMessage, final String cancelMessage) {
        Bundle b = SubscriptionWebActivity.this.getIntent().getExtras();
        b.putBoolean("isSMS", true);
        this.mReceiverId = receiverId;
        this.mActivationMessage = activationMessage;
        this.keyWord = keyword;
        //Log.v(TAG, "initiateSMSTransaction: mReceiverId- " + mReceiverId + " keyword- " + keyword + " keyWord- " + shortCode + " activationMessage- " + activationMessage + " cancelMessage- " + cancelMessage);
       /* //Log.v(TAG, "initiateSMSTransaction: unescaped(message)- " + StringEscapeUtils.unescapeJava(message) + " message- " + message + "" +
                "unicode" + "\u2795" + "unescaped unicode- " + StringEscapeUtils.unescapeJava("\u2795"));*/

        message = StringEscapeUtils.unescapeJava(message);
        AlertDialogUtil.showAlertDialog(this, message, "", getString(R.string.transaction_cancel_no), getString(R.string.transaction_cancel_yes), new AlertDialogUtil.DialogListener() {
            @Override
            public void onDialog1Click() {
                AlertDialogUtil.showToastNotification(cancelMessage);
                /*if (resultAction == PARAM_LAUNCH_HOME) {
                    launchActivity(APIConstants.SUBSCRIPTIONCANCELLED);
                    return;
                }
                dofinish(APIConstants.SUBSCRIPTIONCANCELLED);*/
                APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_SUBSCRIPTION, APIAnalytics.EVENT_SUBSCRIPTION_CANCELLED_ACTIVATION, keyWord, String.valueOf(1));
            }

            @Override
            public void onDialog2Click() {
                startOtpReader();
                AlertDialogUtil.showProgressAlertDialog(SubscriptionWebActivity.this, "", "Please wait while we process your request...", true, false, null);
                if (ApplicationController.ENABLE_DELETE_SMS_OF_SUBSCRIPTIONS) {
                    mSmsSentListner = new SMSSendListener(shortCode, keyWord, new WeakReference<SubscriptionWebActivity>(SubscriptionWebActivity.this));
                    registerReceiver(mSmsSentListner, new IntentFilter(smsSent));
                }
                PendingIntent piSent = PendingIntent.getBroadcast(mContext, 0, new Intent(smsSent), 0| PendingIntent.FLAG_MUTABLE);
                mSmsHandler = new SMSHandler();
                mSmsHandler.sendSMS(shortCode, keyword,piSent, null);
                APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_SUBSCRIPTION, APIAnalytics.EVENT_SUBSCRIPTION_SENT_ACTIVATION_SMS, keyWord, String.valueOf(1));
            }
        });
    }
    private void showNetWorkError(String message) {
        if(mWebView == null)
            return;

        mWebView.setVisibility(View.GONE);
        mTextViewErrorRetryAgain.setText(message);
        mLayoutRetry.setVisibility(View.VISIBLE);

    }

    private void launchBrowser(String redirectLink) {
        try {
            if (TextUtils.isEmpty(redirectLink)) {
                return;
            }
            //Log.d(TAG, "launchBrowser: redirectLink- " + redirectLink);
            Uri appStoreLink = Uri.parse(redirectLink);
            startActivity(new Intent(Intent.ACTION_VIEW, appStoreLink));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class CustomChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            //Log.d(TAG, "onJsAlert " + url);
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    SubscriptionWebActivity.this);
            builder.setMessage(message)
                    .setNeutralButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    }).show();
            result.cancel();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            //Log.d(TAG, "onJsConfirm " + url);
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    SubscriptionWebActivity.this);
            builder.setMessage(message)
                    .setNeutralButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    }).show();
            result.cancel();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, final JsPromptResult result) {
            //Log.d(TAG, "onJsPrompt " + url);
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    SubscriptionWebActivity.this);
            builder.setMessage(message)
                    .setNeutralButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    }).show();
            result.cancel();
            return true;
        }

        ;
    }

    @Override
    public void onBackPressed() {

        if(mWebView != null && mWebView.canGoBack()){
            if(mUrl.contains("sundthott") || mUrl.contains("api.sundirectgo.in") ){
                mToolbar.setVisibility(View.GONE);
            }else {
                mToolbar.setVisibility(View.VISIBLE);
            }
            Log.d("TAG","WEB URL"+mUrl);
            mWebView.goBack();
            return;
        }
        /*AlertDialogUtil.showAlertDialog(SubscriptionWebActivity.this, getResources().getString(R.string
                        .transaction_cancel_msg)
                , ""
                , getResources().getString(R.string.transaction_cancel_no)
                , getResources().getString(R.string.transaction_cancel_yes)

                , new AlertDialogUtil.DialogListener() {

                    @Override
                    public void onDialog1Click() {

                    }

                    @Override
                    public void onDialog2Click() {
                        dofinish(APIConstants.SUBSCRIPTIONCANCELLED);
                    }
                });*/
        setResult(APIConstants.ONSUBSCRIPTIONDONE);
        dofinish(APIConstants.SUBSCRIPTIONCANCELLED);
    }

    public void showProgressBar() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        findViewById(R.id.customactionbar_progressBar).setVisibility(View.VISIBLE);
        OnCancelListener onCancelListener = new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (isProgressDialogCancelable) {
                    finish();
                }

            }
        };
        if(Util.checkActivityPresent(this)) {
            mProgressDialog = ProgressDialog.show(this, "", "Loading...", true, isProgressDialogCancelable, onCancelListener);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
            ProgressBar mProgressbar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
            final int version = Build.VERSION.SDK_INT;
            if (version < 21) {
                mProgressbar.setIndeterminate(false);
                mProgressbar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    public void dismissProgressBar() {
        if (isFinishing()) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        findViewById(R.id.customactionbar_progressBar).setVisibility(View.GONE);
        return;
    }

    @Override
    public void onDialog1Click() {
        mWebView.loadUrl(mUrl + "&force=true");
    }

    @Override
    public void onDialog2Click() {
        dofinish(APIConstants.SUBSCRIPTIONERROR);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!enableSkip) {
            return super.onCreateOptionsMenu(menu);
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subscription_menu_settings, menu);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(SDKUtils.getColor(this,R.color.dim_gray)), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_skip) {
            Bundle b = SubscriptionWebActivity.this.getIntent().getExtras();
            b.putString(APIConstants.NOTIFICATION_PARAM_PAGE, String.valueOf(item.getTitle()));
            getIntent().putExtras(b);
            PrefUtils.getInstance().setPrefIsSkipPackages(true);
            Analytics.gaCategorySubscription(Analytics.EVENT_ACTION_OFFER,Analytics.EVENT_LABEL_OFFER_SKIPPED);
            if (resultAction == PARAM_LAUNCH_HOME) {
                launchActivity(APIConstants.SUBSCRIPTIONCANCELLED);
                return true;
            }
            dofinish(APIConstants.SUBSCRIPTIONCANCELLED);
        }

        return true;
    }
    class SMSSendListener extends BroadcastReceiver {
        private final String phoneNo;
        private final String message;
        private final WeakReference<SubscriptionWebActivity> refSubscriptionWebActivity;

        public SMSSendListener(String phoneNo, String message, WeakReference<SubscriptionWebActivity> subscriptionWebActivity){
            this.phoneNo = phoneNo;
            this.message = message;
            this.refSubscriptionWebActivity = subscriptionWebActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "onReceive: intent.getAction()-" + intent.getAction());
            if (intent.getAction() != null
                    && !intent.getAction().equalsIgnoreCase(smsSent)) {
                return;
            }
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SMSHandler.deleteMessage(SubscriptionWebActivity.this, phoneNo, message);
                        }
                    }, 5000);
                    break;
            }
        }
    };

}

