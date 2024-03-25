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
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by Apalya on 12/3/2015.
 */
public class FragmentWebView extends BaseFragment implements View.OnKeyListener{
    private static final String TAG = FragmentWebView.class.getSimpleName();
    private static final CharSequence sundirect_VIDEO_DOWNLOAD_URL_PATH = "/index.php/download/";
    private static final CharSequence FORMAT_MP3 = ".mp3";
    private static final CharSequence SHOWTOAST= "showToast";
    private static final CharSequence SHOWMESAAGE= "showMessage";
    private static final CharSequence MESSAGE= "message";
    private static final CharSequence FORMAT_MP4 = ".mp4";
    private static final String DEFAULT_MUSIC_FILE_NAME = "sundirect_music.mp3";
    private static final String DEFAULT_VIDEO_FILE_NAME = "sundirect_music.mp4";
    private static final String MUSIC_FILE_LOCATION = "VFMUSIC";
    private static final CharSequence sundirect_DOWNLOAD_URL_PATH = "http://aks3dlre.sundirectmusic.in";
    private static final CharSequence FORMAT_3GP = ".3gp";
    public static final String PARAM_SHOW_TOOLBAR = "show_toolbar";
    public static final String PARAM_TOOLBAR_TITLE = "toolbar_title";
    public static final String PARAM_TOOLBAR_TITLE_LANG = "toolbar_title_lang";

    private static String sundirect_MUSIC_URL;
    public static final String PARAM_URL = "url";

    private Context mContext;
    private WebView liveWebView;
    private TextView mTextViewError;
    private ProgressBar progressBar;
    private FbWebViewClient webviewclient;
    private ProgressDialog mProgressDialog = null;
    private boolean isProgressDialogCancelable = true;
    private boolean isLoaded = false;
    private boolean isLoadingFailedWithUnInitializedView = false;
    private boolean didRecieveErrorWhileLoading = false;
    Toolbar mToolbar;
    RelativeLayout mRootLayout;
    RelativeLayout mainLayout;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG,"onCreateView()");
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.layout_webview,container,false);
        liveWebView = (WebView) rootView.findViewById(R.id.webview);
        mainLayout =  (RelativeLayout) rootView.findViewById(R.id.main_layout);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        int statusBarHeight = Util.getStatusBarHeight(mContext);
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        layoutParams1.topMargin = statusBarHeight;
        if(mToolbar.getVisibility() == getView().GONE){
            mainLayout.setLayoutParams(layoutParams1);
        }
        View mInflateView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_toolbar, null, false);
        TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        TextView mToolbarTitleOtherLang = (TextView) mInflateView.findViewById(R.id.toolbar_header_title_lang);
        ImageView mCloseIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mCloseIcon.setOnClickListener(mCloseAction);
        ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        progressBar = (ProgressBar) rootView.findViewById(R.id.customactionbar_progressBar);
        mTextViewError = (TextView) rootView.findViewById(R.id.textview_network_error);
        liveWebView.setOnKeyListener(this);
        liveWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //You need to add the following line for this solution to work; thanks skayred
        Bundle args = getArguments();
        if (args != null) {
            sundirect_MUSIC_URL = args.getString(PARAM_URL);
        }
        boolean isToShowToolbar = false;
        if (args.containsKey(PARAM_SHOW_TOOLBAR) && args.getBoolean(PARAM_SHOW_TOOLBAR)) {
            isToShowToolbar = true;
            mToolbarTitle.setText(R.string.app_name);
            if (args.containsKey(PARAM_TOOLBAR_TITLE) && !TextUtils.isEmpty(args.getString(PARAM_TOOLBAR_TITLE))) {
                mToolbarTitle.setText(args.getString(PARAM_TOOLBAR_TITLE));
            }
            if(PrefUtils.getInstance().getVernacularLanguage()){

                if (args.containsKey(PARAM_TOOLBAR_TITLE_LANG) && !TextUtils.isEmpty(args.getString(PARAM_TOOLBAR_TITLE_LANG))) {
                    mToolbarTitleOtherLang.setText(args.getString(PARAM_TOOLBAR_TITLE_LANG));
                    mToolbarTitleOtherLang.setVisibility(View.VISIBLE);
                }else{
                    mToolbarTitleOtherLang.setVisibility(View.GONE);
                }


            }else{
                mToolbarTitleOtherLang.setVisibility(View.GONE);
            }
        }
        if (!isLoaded && (isLoadingFailedWithUnInitializedView || isToShowToolbar)) {
            setUpWebView();
        }
        return rootView;
    }
    private void closeFragment() {

            ((MainActivity) mBaseActivity).enableNavigation();
            ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
        if(!sundirect_MUSIC_URL.contains("https://www.sundirect.in/")) {
            ((MainActivity) mBaseActivity).mDrawerLayout.openDrawer(Gravity.START);
        }
            mBaseActivity.removeFragment(FragmentWebView.this);

    }

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (liveWebView.canGoBack()) {
                liveWebView.goBack();
                mToolbar.setVisibility(View.VISIBLE);
                int statusBarHeight = Util.getStatusBarHeight(mContext);
                FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                layoutParams1.topMargin = statusBarHeight;
//                    mainLayout.setLayoutParams(layoutParams1);


//            closeFragment();
            }
            else {
                closeFragment();
        }

        }
    };


    private void setUpWebView() {
        if(liveWebView == null){
            isLoadingFailedWithUnInitializedView = true;
            return;
        }
        didRecieveErrorWhileLoading = false;
        liveWebView.setVerticalScrollBarEnabled(false);
        liveWebView.setHorizontalScrollBarEnabled(false);
        liveWebView.setWebViewClient(webviewclient = new FbWebViewClient());
        liveWebView.setWebChromeClient(new CustomChromeClient());
        WebSettings webSettings = liveWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        if(!ConnectivityUtil.isConnected(mContext)){
            showNetworkError();
            return;
        }
        if(sundirect_MUSIC_URL.contains("https://www.sundirect.in/")){
            FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            mainLayout.setLayoutParams(layoutParams1);
            mToolbar.setVisibility(View.VISIBLE);
        }
        else{
            mToolbar.setVisibility(View.GONE);
        }
        liveWebView.loadUrl(sundirect_MUSIC_URL);
    }

    private void showNetworkError() {
        if(liveWebView == null || mTextViewError == null){
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
        if(liveWebView == null || mTextViewError == null){
            return;
        }
        if(didRecieveErrorWhileLoading){
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
                        //Log.d(TAG,"web view previous page");
                        liveWebView.goBack();
                        int statusBarHeight = Util.getStatusBarHeight(mContext);
                        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams1.topMargin = statusBarHeight;
//                        mainLayout.setLayoutParams(layoutParams1);
//                        mToolbar.setVisibility(View.GONE);
                    }else{
                        this.getView().setFocusableInTouchMode(true);
                        this.getView().requestFocus();
                        this.getView().setOnKeyListener(null);
                        closeFragment();
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
            if(url!=null && mContext!=null){
                if(url.contains("close=yes")){
                   liveWebView.canGoBack();
                   closeFragment();
                }
            }
            if (url != null && mContext != null && url.contains(SHOWTOAST) && url.contains(MESSAGE) ) {
                String[] msg = url.split("message=");
                String message = msg[1];
                if (message.contains("%20")) {
                    message = message.replace("%20", " ");
                    AlertDialogUtil.showToastNotification(message);
                    if (url.contains("status=SUCCESS")) {
                        liveWebView.loadUrl(sundirect_MUSIC_URL);
                    }
                }
                return true;
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
                    liveWebView.loadUrl(sundirect_MUSIC_URL);
                }
                return true;
            }

            if(url != null
                    && mContext != null
                    && url.contains(sundirect_DOWNLOAD_URL_PATH)){
//                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
                if(url.contains(FORMAT_MP3)){
                    String songName = DEFAULT_MUSIC_FILE_NAME;
                    try{
                        String[] urlTokensWithSlash = url.split("/");
                        if(urlTokensWithSlash.length > 0){
                            Log.d("songName" , "last token- "+urlTokensWithSlash[urlTokensWithSlash.length-1]);
                            urlTokensWithSlash = urlTokensWithSlash[urlTokensWithSlash.length-1].split("\\?");
                            if(urlTokensWithSlash[0].contains(FORMAT_MP3)){
                                songName = urlTokensWithSlash[0];
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    CardData data = new CardData();
                    CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
                    generalInfo.title = songName;
                    generalInfo.type = APIConstants.TYPE_MUSIC;
                    data.generalInfo = generalInfo;
                    Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.download.name(), data, 1L,1L);
                    Log.d("songName" , "song name- "+songName);
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
                        if (info != null) {
                            context.startActivity(intent);
                            closeFragment();
                        } else {
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

            if(url.contains("QuickRecharge")){
                FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                mainLayout.setLayoutParams(layoutParams1);
                mToolbar.setVisibility(View.VISIBLE);

            }else{
                mToolbar.setVisibility(View.GONE);
            }
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "ONRECEIVEDERROR " + failingUrl + " " + description);
            if("Couldn't find the URL.".equalsIgnoreCase(description)
                    || "net::ERR_ADDRESS_UNREACHABLE".equalsIgnoreCase(description)
                    || "net::ERR_PROXY_CONNECTION_FAILED".equalsIgnoreCase(description)){
                didRecieveErrorWhileLoading = true;
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
            if(ConnectivityUtil.isConnected(mContext)){
                hideNetworkError();
            }
            dismissProgressBar();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.cancel(); // Ignore SSL certificate errors
            dismissProgressBar();
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
        };

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            newProgress=0;
            liveWebView.setBackgroundColor(getResources().getColor(R.color.transparent));
            super.onProgressChanged(view, newProgress);
            if(newProgress == 0){
                progressBar.setVisibility(View.GONE);
                liveWebView.setVisibility(View.VISIBLE);
                //Log.d(TAG,"onProgressChanged- newProgress- "+ newProgress);
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
        if(menuVisible){
//            Analytics.createScreenGA(Analytics.SCREEN_MUSIC);
            if(getArguments() != null && APIConstants.PARAM_APP_FRAG_MUSIC == getArguments().getInt(APIConstants.PARAM_APP_FRAG_TYPE)){
                AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_MUSIC);
            }
            //Log.d(TAG, "setMenuVisibility() loading the web isLoaded " + isLoaded);
            if(!isLoaded){
                setUpWebView();
            }
        }
    }

    public void dismissProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDetach() {
        ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.black_40));
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = requireActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }
        ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
        if(liveWebView != null
                && liveWebView.getUrl() != null){
            //Log.d(TAG, "onResume() getUrl- " + liveWebView.getUrl());
            if(liveWebView.getUrl().contains(sundirect_VIDEO_DOWNLOAD_URL_PATH)){
                if(liveWebView.canGoBack()){
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
        ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.black_40));
        //Log.d(TAG, "onPause()");
    }

    @Override
    public boolean onBackClicked() {

        return liveWebView != null && liveWebView.canGoBack();

    }
}