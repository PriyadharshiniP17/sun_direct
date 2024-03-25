package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FavouriteCheckRequest;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.model.PromoAdData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.UrlGatewayActivity;
import com.myplex.myplex.utils.DownloadUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.myplex.myplex.ui.activities.UrlGatewayActivity.PATH_ADD_TO_WATCH_LIST;
import static com.myplex.myplex.ui.activities.UrlGatewayActivity.PATH_WATCH;

/**
 * Created by Apalya on 12/3/2015.
 */
public class FullScreenWebViewFragment extends BaseFragment implements View.OnKeyListener {
    private static final String TAG = FullScreenWebViewFragment.class.getSimpleName();
    private static final CharSequence VIDEO_DOWNLOAD_URL_PATH = "/index.php/download/";
    private static final CharSequence FORMAT_MP3 = ".mp3";
    private static final CharSequence SHOWTOAST = "showToast";
    private static final CharSequence SHOWMESAAGE = "showMessage";
    private static final CharSequence MESSAGE = "message";
    private static final CharSequence FORMAT_MP4 = ".mp4";
    private static final String DEFAULT_MUSIC_FILE_NAME = "sundirect.mp3";
    private static final String DEFAULT_VIDEO_FILE_NAME = "sundirect.mp4";
    private static final String MUSIC_FILE_LOCATION = "SDMUSIC";

    private static final CharSequence FORMAT_3GP = ".3gp";
    public static final String PARAM_SHOW_TOOLBAR = "show_toolbar";
    public static final String PARAM_TOOLBAR_TITLE = "toolbar_title";
    public static final String PARAM_WEB_URL = "param_url";
    public static final String PARAM_MEDIA_PLAYBACK_REQUIRE_GESTURE = "param_media_playback_require_gesture";
    public static final int REQUEST_PROMO_AD = 100;
    public static final String PATH_SKIP = "sundirect://promo/ad/skip";
    public static final String PARAM_PROMO_AD_DATA = "promo_ad_data";
    private static final String PARAM_STATUS = "status";
    private static final String PROMO_VIDEO_AD_STATUS_COMPLETED = "completed";

    private static String webUrl;

    private Context mContext;
    private WebView liveWebView;
    private TextView mTextViewError;
    private ProgressBar progressBar;
    private FbWebViewClient webviewclient;
    private ProgressDialog mProgressDialog = null;
    private boolean isProgressDialogCancelable = true;
    private boolean isLoaded = false;
    private boolean isLoadingFailedWithUnInitializedView = false;
    private RelativeLayout progressBarLayout;

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeFragment();
        }
    };
    private boolean mediaPlaybackRequireGesture = true;
    private PromoAdData promoAdData;

    private void closeFragment() {
        try {
            ((MainActivity) mBaseActivity).enableNavigation();
            ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
            mBaseActivity.removeFragment(FullScreenWebViewFragment.this);
        } catch (Exception e) {
            e.printStackTrace();
            mBaseActivity.finish();
        }
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        liveWebView = (WebView) rootView.findViewById(R.id.webview);
        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        mToolbar.setVisibility(View.GONE);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
//		liveWebView.getSettings().setJavaScriptEnabled(true);
//        setSupportActionBar(mToolbar);
//		getSupportActionBar().setCustomView(R.layout.activity_live_score);
        View mInflateView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_toolbar, null, false);
        TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        ImageView mCloseIcon = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mCloseIcon.setOnClickListener(mCloseAction);
        ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        progressBar = (ProgressBar) rootView.findViewById(R.id.customactionbar_progressBar);
        mTextViewError = (TextView) rootView.findViewById(R.id.textview_network_error);
        liveWebView.setOnKeyListener(this);
        //You need to add the following line for this solution to work; thanks skayred
        Bundle args = getArguments();
        if (args != null) {
            webUrl = args.getString(PARAM_WEB_URL);
        }
        if (args != null) {
            promoAdData = (PromoAdData) args.getSerializable(PARAM_PROMO_AD_DATA);
        }
        if (promoAdData != null) {
            webUrl = promoAdData.htmlURL;
        }
        if (args != null) {
            mediaPlaybackRequireGesture = args.getBoolean(PARAM_MEDIA_PLAYBACK_REQUIRE_GESTURE);
        }

        boolean isToShowToolbar = false;
        if (args.containsKey(PARAM_SHOW_TOOLBAR) && args.getBoolean(PARAM_SHOW_TOOLBAR)) {
            isToShowToolbar = true;
            mToolbar.setVisibility(View.VISIBLE);
            mToolbarTitle.setText(R.string.app_name);
            if (args.containsKey(PARAM_TOOLBAR_TITLE) && !TextUtils.isEmpty(args.getString(PARAM_TOOLBAR_TITLE))) {
                mToolbarTitle.setText(args.getString(PARAM_TOOLBAR_TITLE));
            }
        }
        if (promoAdData != null)
            CleverTap.eventPromoVideoShown(promoAdData.id == null ? APIConstants.NOT_AVAILABLE : promoAdData.id, String.valueOf(PrefUtils.getInstance().getAppLaunchCountUpUntill20()));
        setUpWebView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView()");
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fullscreen_webview_layout, container, false);
        progressBarLayout = (RelativeLayout) rootView.findViewById(R.id.progress_layout);
        return rootView;
    }


    private void setUpWebView() {
        if (liveWebView == null) {
            isLoadingFailedWithUnInitializedView = true;
            return;
        }
        liveWebView.setVerticalScrollBarEnabled(false);
        liveWebView.setHorizontalScrollBarEnabled(false);
        liveWebView.setWebViewClient(webviewclient = new FbWebViewClient());
        liveWebView.setWebChromeClient(new CustomChromeClient());
        WebSettings webSettings = liveWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (!mediaPlaybackRequireGesture)
            webSettings.setMediaPlaybackRequiresUserGesture(false);

        if (!ConnectivityUtil.isConnected(mContext)) {
            showNetworkError();
            return;
        }
        showLoadingScreen(true);
        liveWebView.loadUrl(webUrl);
    }

    private void showNetworkError() {
        if (liveWebView == null || mTextViewError == null) {
            return;
        }
        dismissProgressBar();
        liveWebView.setVisibility(View.GONE);
        mTextViewError.setVisibility(View.VISIBLE);
        mTextViewError.setText(mContext.getString(R.string.network_error) + ", Touch to try " +
                "again later");
        mTextViewError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpWebView();
            }
        });
    }

    private void hideNetworkError() {
        if (liveWebView == null || mTextViewError == null) {
            return;
        }
        dismissProgressBar();
        liveWebView.setVisibility(View.VISIBLE);
        mTextViewError.setVisibility(View.GONE);
    }

    /**
     * Called when a hardware key is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     * <p>Key presses in software keyboards will generally NOT trigger this method,
     * although some may elect to do so in some situations. Do not assume a
     * software input method has to be key-based; even if it is, it may use key presses
     * in a different way than you expect, so there is no way to reliably catch soft
     * input key presses.
     *
     * @param v       The view the key has been dispatched to.
     * @param keyCode The code for the physical key that was pressed
     * @param event   The KeyEvent object containing full information about
     *                the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (liveWebView.canGoBack()) {
                        //Log.d(TAG, "web view previous page");
                        liveWebView.goBack();
                    } else {
                        this.getView().setFocusableInTouchMode(true);
                        this.getView().requestFocus();
                        this.getView().setOnKeyListener(null);
                    }
                    return true;
            }

        }
        return false;
    }


    private class FbWebViewClient extends WebViewClient {
        boolean closed = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //Log.i(TAG, "OVERRIDE " + closed + " " + url);
            if (url != null && mContext != null && url.contains(SHOWTOAST) && url.contains(MESSAGE)) {
                String[] msg = url.split("message=");
                String message = msg[1];
                if (message.contains("%20")) {
                    message = message.replace("%20", " ");
                    AlertDialogUtil.showToastNotification(message);
                    if (url.contains("status=SUCCESS")) {
                        liveWebView.loadUrl(webUrl);
                    }
                }
                return true;
            }
            if (url != null && mContext != null && (url.contains("sundirect://"))) {
                handleDeeplink(url);
                return true;
            }
            if (getActivity() != null && getActivity().getIntent() != null && getPartnerExitUrl(getActivity().getIntent().getStringExtra(APIConstants.PARTNER_NAME)) != null) {
                if(url != null && mContext != null && url.contains(getPartnerExitUrl(getActivity().getIntent().getStringExtra(APIConstants.PARTNER_NAME)))){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    closeFragment();
                    return true;
                }
            }
            if (url != null && mContext != null && url.contains(SHOWMESAAGE) && url.contains(MESSAGE)) {
                String[] msg = url.split("message=");
                String message = msg[1];
                if (!TextUtils.isEmpty(message) && message.contains("%20")) {
                    message = message.replace("%20", " ");
                    AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", false, "Ok", new AlertDialogUtil.NeutralDialogListener() {
                        @Override
                        public void onDialogClick(String buttonText) {
                            return;
                        }
                    });
                }
                if (url.contains("status=SUCCESS")) {
                    liveWebView.loadUrl(webUrl);
                }
                return true;
            }

            if (url != null
                    && mContext != null
                    && url.contains("")) {
//                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
                if (url.contains(FORMAT_MP3)) {
                    String songName = DEFAULT_MUSIC_FILE_NAME;
                    try {
                        String[] urlTokensWithSlash = url.split("/");
                        if (urlTokensWithSlash.length > 0) {
                            Log.d("songName", "last token- " + urlTokensWithSlash[urlTokensWithSlash.length - 1]);
                            urlTokensWithSlash = urlTokensWithSlash[urlTokensWithSlash.length - 1].split("\\?");
                            if (urlTokensWithSlash[0].contains(FORMAT_MP3)) {
                                songName = urlTokensWithSlash[0];
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    CardData data = new CardData();
                    CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
                    generalInfo.title = songName;
                    generalInfo.type = APIConstants.TYPE_MUSIC;
                    data.generalInfo = generalInfo;
                    Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.download.name(), data, 1L, 1L);
                    Log.d("songName", "song name- " + songName);
                    DownloadUtil.startDownload2(mContext, url, songName, File.separator +
                            MUSIC_FILE_LOCATION);
                } else if (url.contains(".mp4") || url.contains(".3gp")) {
                    String type = "video/mp4"; // It works for all video application
                    Intent videoLaunchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    videoLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    videoLaunchIntent.setDataAndType(Uri.parse(url), type);
                    final PackageManager pm = mContext.getPackageManager();
                    int i = 0;
                    for (ResolveInfo ri : pm.queryIntentActivities(videoLaunchIntent,
                            PackageManager.MATCH_DEFAULT_ONLY)) {
                        if ((i == 0 || i == 1) && ri.activityInfo.enabled) {
                            videoLaunchIntent.setClassName(ri.activityInfo.packageName,
                                    ri.activityInfo.name);
                        }
                        i++;
                    }
                    String videoName = DEFAULT_VIDEO_FILE_NAME;
                    try {
                        String[] urlTokensWithSlash = url.split("/");
                        if (urlTokensWithSlash.length > 0) {
                            Log.d("songName", "last token- " + urlTokensWithSlash[urlTokensWithSlash.length - 1]);
                            urlTokensWithSlash = urlTokensWithSlash[urlTokensWithSlash.length - 1].split("\\?");
                            if (urlTokensWithSlash[0].contains(FORMAT_MP4)
                                    || urlTokensWithSlash[0].contains(FORMAT_3GP)) {
                                videoName = urlTokensWithSlash[0];
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    CardData data = new CardData();
                    CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
                    generalInfo.title = videoName;
                    generalInfo.type = APIConstants.TYPE_MUSIC;
                    data.generalInfo = generalInfo;
                    Analytics.gaPlayedVideoTimeCalculation("played video", data, 1L, 1L);
                    startActivity(videoLaunchIntent);
                    //Log.i(TAG, "started video player");
                    return true;
                }
            } else if (url.startsWith("intent://")) {
                try {
                    Log.e(TAG, "url-" + url);
                    Context context = view.getContext();
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                    if (intent != null) {
                        view.stopLoading();

                        PackageManager packageManager = context.getPackageManager();
                        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        String packageName = intent.getPackage();
                        if (info != null) {
                            context.startActivity(intent);
                            closeFragment();
                        }
                        else {
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            view.loadUrl(fallbackUrl);
                            // or call external broswer
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
//                    context.startActivity(browserIntent);
                        }

                        return true;
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Can't resolve intent://", e);
                }
            } else if (!url.contains("http://")
                    && !url.contains("https://")) {
                try {
                    Log.e(TAG, "url-" + url);
                    Intent videoLaunchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    videoLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    videoLaunchIntent.setData(Uri.parse(url));
                    final PackageManager pm = mContext.getPackageManager();
                    int i = 0;
                    for (ResolveInfo ri : pm.queryIntentActivities(videoLaunchIntent,
                            PackageManager.MATCH_DEFAULT_ONLY)) {
                        if ((i == 0 || i == 1) && ri.activityInfo.enabled) {
                            videoLaunchIntent.setClassName(ri.activityInfo.packageName,
                                    ri.activityInfo.name);
                        }
                        i++;
                    }
                    startActivity(videoLaunchIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Can't resolve intent://" + url, e);
                    e.printStackTrace();
                }
                return true;
            }
            //Log.i(TAG, "loading url- " + url);
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "ONRECEIVEDERROR " + failingUrl + " " + description);
            if ("Couldn't find the URL.".equalsIgnoreCase(description)
                    || "net::ERR_ADDRESS_UNREACHABLE".equalsIgnoreCase(description)) {
                showNetworkError();
            }
            closed = true;
            dofinish();
        }

        private void dofinish() {
//            onBackPressed();
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
            if (ConnectivityUtil.isConnected(mContext)) {
                hideNetworkError();
            }
            dismissProgressBar();
            showLoadingScreen(false);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.cancel(); // Ignore SSL certificate errors
            dismissProgressBar();
        }


    }

    private String getPartnerExitUrl(String publishingHouseName) {

        if(publishingHouseName ==null){
            return null;
        }
        if (mContext != null) {
            PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
            String partnerName = publishingHouseName;
            if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null ) {
                for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                    if (partnerDetailsResponse != null
                            && partnerDetailsResponse.partnerDetails != null
                            && partnerDetailsResponse.partnerDetails.get(i) != null
                            && !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
                            && partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                        if(partnerDetailsResponse.partnerDetails.get(i) != null
                                && partnerDetailsResponse.partnerDetails.get(i).packageName != null)
                        if(partnerDetailsResponse.partnerDetails.get(i).webViewCloseString!= null)
                            return partnerDetailsResponse.partnerDetails.get(i).webViewCloseString;
                    }
                }
            }
        }
        return null;
    }

    private void handleDeeplink(String url) {

        Uri uri = Uri.parse(url);
        UrlGatewayActivity.SchemeType scheme = UrlGatewayActivity.SchemeType.valueOf(uri.getScheme());

//		String url = uri.toString();
        List<String> list = new ArrayList<>();
        if (uri.getPathSegments() != null) {
            list = uri.getPathSegments();
        }


        switch (scheme) {
            case http:
            case https:
            default:
//			sundirectplay://notification/detailsPage/1952/
//			sundirectplay://notification/screen/{screen name}/
// sundirectplay://promo/ad/add-to-watchlist/111/contentType/live

                /*
                Stream now :
sundirectplay://promo/ad/watch/46725

sundirectplay://promo/ad/watch/46725?promoId=12345

Watchlist :
sundirectplay://promo/ad/add-to-watchlist/46725/content-type/movie

sundirectplay://promo/ad/add-to-watchlist/46725/content-type/movie?promoId=12345

Skip
sundirectplay://promo/ad/skip

sundirectplay://promo/ad/skip?status=completed&promoId=12345
sundirectplay://promo/ad/skip?status=notCompleted&promoId=12345 */
                if (list != null) {
                    int indexOfPathQuery = 0;
                    String firstPathSegment = null;
                    try {
                        if (list.contains(PATH_WATCH)) {
                            indexOfPathQuery = list.indexOf(PATH_WATCH);
                        }
                        if (list.contains(PATH_ADD_TO_WATCH_LIST)) {
                            indexOfPathQuery = list.indexOf(PATH_ADD_TO_WATCH_LIST);
                        }
                        if (url != null
                                && url.contains(PATH_SKIP)) {
                            String status = uri.getQueryParameter(PARAM_STATUS);
                            SDKLogger.debug("uri.getQueryParam status: " + status);
                            if (promoAdData != null
                                    && status != null
                                    && status.equalsIgnoreCase(PROMO_VIDEO_AD_STATUS_COMPLETED)) {
                                CleverTap.eventPromoVideoCompleted(promoAdData.id == null ? APIConstants.NOT_AVAILABLE : promoAdData.id, String.valueOf(PrefUtils.getInstance().getAppLaunchCountUpUntill20()));
                            } else {
                                if (promoAdData != null) {
                                    CleverTap.eventPromoVideoSkipped(promoAdData.id == null ? APIConstants.NOT_AVAILABLE : promoAdData.id, String.valueOf(PrefUtils.getInstance().getAppLaunchCountUpUntill20()));
                                }
                            }
                            getActivity().finish();
                            return;
                        }

//                        /promo/ad/skip
                        firstPathSegment = list.get(indexOfPathQuery);
                        String secondPathSegment = list.get(indexOfPathQuery + 1);
                        switch (firstPathSegment) {
                            case PATH_WATCH:
                                Intent intent = new Intent(mContext, UrlGatewayActivity.class);
                                intent.setData(uri);
                                startActivity(intent);
                                getActivity().finish();
                                break;
                            case PATH_ADD_TO_WATCH_LIST:
                                if (list.contains(PATH_ADD_TO_WATCH_LIST)) {
                                    String fourthPathSegment = list.get(indexOfPathQuery + 3);
                                    postCheckFavoriteContent(secondPathSegment, fourthPathSegment);
                                }
                                break;
                        }
                    } catch (Exception e) {
                        //Log.d(TAG, "Invalid content id:" + firstPathSegment);
                        //Log.d(TAG, e.getMessage());
                    }
                }
                break;
        }
    }

    private class CustomChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            //Log.d(TAG, "onJsAlert " + url);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(message)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
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
                    mContext);
            builder.setMessage(message)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(message)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    }).show();
            result.cancel();
            return true;
        }

        ;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                //Log.d(TAG, "onProgressChanged- newProgress- " + newProgress);
                isLoaded = true;
            }
        }
    }

   /* @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }*/

    public void showProgressBar() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        if (isProgressDialogCancelable) {
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (isProgressDialogCancelable) {
//                    finish();
                }

            }
        };
        mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true,
                isProgressDialogCancelable, onCancelListener);
        mProgressDialog.setContentView(R.layout.layout_progress_dialog);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        //Log.d(TAG, "setMenuVisibility() " + menuVisible);
        super.setMenuVisibility(menuVisible);
        ScopedBus.getInstance().post(new ChangeMenuVisibility(false, MainActivity.SECTION_MUSIC));
        if (menuVisible) {
//            Analytics.createScreenGA(Analytics.SCREEN_MUSIC);
            FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_MUSIC);
            if (getArguments() != null && APIConstants.PARAM_APP_FRAG_MUSIC == getArguments().getInt(APIConstants.PARAM_APP_FRAG_TYPE)) {
                AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_MUSIC);
            }
            //Log.d(TAG, "setMenuVisibility() loading the web isLoaded " + isLoaded);
            if (!isLoaded) {
                setUpWebView();
            }
        }
    }

    public void dismissProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (liveWebView != null
                && liveWebView.getUrl() != null) {
            //Log.d(TAG, "onResume() getUrl- " + liveWebView.getUrl());
            if (liveWebView.getUrl().contains(VIDEO_DOWNLOAD_URL_PATH)) {
                if (liveWebView.canGoBack()) {
                    //Log.d(TAG, "onResume() goBack()");
                    liveWebView.goBack();
                }
            }
        }


        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();
        this.getView().setOnKeyListener(this);
        //Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");
    }

    @Override
    public boolean onBackClicked() {
        return liveWebView != null && liveWebView.canGoBack();
    }

    public static FullScreenWebViewFragment newInstance(String url, PromoAdData adData, boolean showToolbar, boolean mediaPlaybackRequireGesture) {
        FullScreenWebViewFragment fullScreenWebViewFragment = new FullScreenWebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_PROMO_AD_DATA, adData);
        bundle.putSerializable(PARAM_WEB_URL, url);
        bundle.putBoolean(PARAM_SHOW_TOOLBAR, showToolbar);
        bundle.putBoolean(PARAM_MEDIA_PLAYBACK_REQUIRE_GESTURE, mediaPlaybackRequireGesture);
        fullScreenWebViewFragment.setArguments(bundle);
        return fullScreenWebViewFragment;
    }

    private void postCheckFavoriteContent(final String contentId, final String type) {
        final FavouriteCheckRequest.Params contentDetailsParams = new FavouriteCheckRequest.Params(contentId, type);
        final FavouriteCheckRequest contentDetails = new FavouriteCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            if (response.body().favorite) {
                                AlertDialogUtil.showToastNotification("Already Added to Watchlist");
                                return;
                            }
                            FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(contentId, type);
                            executeFavouriteRequest(favouritesParams);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }

    private void executeFavouriteRequest(final FavouriteRequest.Params favouritesParams) {

//                AlertDialogUtil.showToastNotification("Please wait while we update the data...");
        FavouriteRequest mRequestFavourites = new FavouriteRequest(favouritesParams,
                new APICallback<FavouriteResponse>() {
                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        if (response.body().code == 402) {
                            PrefUtils.getInstance().setPrefLoginStatus("");
                            return;
                        }

                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                            if (response.body().favorite) {
                                if (promoAdData != null)
                                    CleverTap.eventPromoVideoAddedToWatchList(promoAdData.id == null ? APIConstants.NOT_AVAILABLE : promoAdData.id, favouritesParams.contentId, CleverTap.SOURCE_PROMO_VIDEO_AD);
                                AlertDialogUtil.showToastNotification("Added to Watchlist");
                            } else {
                                AlertDialogUtil.showToastNotification("Removed from Watchlist");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);
    }


    private void showLoadingScreen(boolean b) {
        if (b) {
            liveWebView.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.VISIBLE);
            return;
        }
        liveWebView.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
    }

}