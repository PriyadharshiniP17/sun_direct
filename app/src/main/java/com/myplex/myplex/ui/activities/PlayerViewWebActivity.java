package com.myplex.myplex.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.web.VideoEnabledWebChromeClient;
import com.myplex.myplex.web.VideoEnabledWebView;


public class PlayerViewWebActivity extends BaseActivity implements
        AlertDialogUtil.DialogListener {
    private static final int LOAD_OFFSET = 8 * 1000;
    private String url;
    private boolean isProgressDialogCancelable;
    private String TAG = getClass().getSimpleName();
    private FbWebViewClient webviewclient;
    private ProgressDialog mProgressDialog = null;
    private ProgressBar progressBar;
    private RelativeLayout progressBarLayout;
    private String type;
    private ProgressDialog mSingleTimeProgressDialog;
    private Handler mHandler;
    private Runnable mDismissCallback = new Runnable() {
        @Override
        public void run() {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (mSingleTimeProgressDialog != null && mSingleTimeProgressDialog.isShowing()) {
                mSingleTimeProgressDialog.dismiss();
            }
        }
    };
    private String contentName;

    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private Toolbar mToolbar;
    private PlayerViewWebActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_web_playerview);

        // Save the web view
        webView = (VideoEnabledWebView) findViewById(R.id.webView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        getWindow().setBackgroundDrawable(null);
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Your code...
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new FbWebViewClient());

        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
//        webView.loadUrl("http://m.youtube.com");
//		webView.loadUrl("http://www.sonyliv.com/details/show/5487006851001/Zimbabwe-Tour-Of-Sri-Lanka--2017-");

        mHandler = new Handler();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        progressBarLayout = (RelativeLayout) findViewById(R.id.progress_layout);
//		liveWebView.getSettings().setJavaScriptEnabled(true);
        url = new String();
        try {
            Intent extras = getIntent();
            if (extras != null) {
                url = getIntent().getStringExtra("url");
                type = getIntent().getStringExtra("type");
                if (APIConstants.getFAQURL().equalsIgnoreCase(url)) {
                    AppsFlyerTracker.eventBrowseTermsAndConditions();
                    CleverTap.eventPageViewed(CleverTap.PAGE_TERMS_AND_CONDITIONS);
                } else if (APIConstants.getHelpURL().equalsIgnoreCase(url)) {
                    CleverTap.eventPageViewed(CleverTap.PAGE_HELP);
                    AppsFlyerTracker.eventBrowseHelp();
                }

                contentName = getIntent().getStringExtra("contentName");
                if (APIConstants.getFAQURL().equalsIgnoreCase(url)) {
//                    Analytics.createScreenGA(Analytics.SCREEN_TERMS_N_CONDITIONS);
                    FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_TERMS_N_CONDITIONS);
                } else if (APIConstants.getHelpURL().equalsIgnoreCase(url)) {
//                    Analytics.createScreenGA(Analytics.SCREEN_HELP);
                    FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_HELP);
                }
                isProgressDialogCancelable = getIntent().getBooleanExtra("isProgressDialogCancelable", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setSupportActionBar(mToolbar);
//		getSupportActionBar().setCustomView(R.layout.activity_live_score);
        progressBar = (ProgressBar) findViewById(R.id.customactionbar_progressBar);
        if (url == null || url.isEmpty()) {
            dofinish();
            return;
        }
        setUpWebView(url.trim());

    }


    @Override
    public void setOrientation(int value) {

    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    private void dofinish() {
        setResult(Activity.RESULT_OK);
        finish();
        dismissProgressBar();
    }

    private void setUpWebView(String url) {
        webView.loadUrl(url);
		showLoadingScreen(true);
        showLoadingBarFirsTime();

    }

    private void showLoadingScreen(boolean b) {
        if (b) {
            webView.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.VISIBLE);
            return;
        }
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDialog1Click() {
        webView.loadUrl(url + "&force=true");
    }

    @Override
    public void onDialog2Click() {
        dofinish();
    }

    private class FbWebViewClient extends WebViewClient {
        boolean closed = false;


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //Log.i(TAG, "OVERRIDE " + closed + " " + url);
/*
            if (url != null
					&& !url.startsWith("https")
					&& !url.startsWith("http")) {
//				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//				startActivity(intent);
				return true;
			}
*/
            if (url.startsWith("intent://")) {
                try {
					/*if (APIConstants.TYPE_SPORTS.equalsIgnoreCase(type)) {
						mHandler.removeCallbacks(mDismissCallback);
					}*/
                    showLoadingScreen(false);
					/*if (mSingleTimeProgressDialog != null && mSingleTimeProgressDialog.isShowing()) {
						mSingleTimeProgressDialog.dismiss();
					}*/
                    Intent intent = new Intent().parseUri(url, Intent.URI_INTENT_SCHEME);

                    if (intent != null) {
                        view.stopLoading();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
/*
						PackageManager packageManager = context.getPackageManager();
						ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
						if (info != null) {
							finish();
							context.startActivity(intent);
						} else {*/
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        view.loadUrl(fallbackUrl);
//							 or call external broswer
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
//                    context.startActivity(browserIntent);
//						}

                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Can't resolve intent://", e);
                }
            }

            view.loadUrl(url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "ONRECEIVEDERROR " + failingUrl + " " + description);
            closed = true;
            dofinish();
            dismissProgressBar();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //Log.d(TAG, "PageStarted " + url);
            showProgressBar();
            webView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //Log.d(TAG, "PageFinished " + url);
            dismissProgressBar();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.cancel(); // Ignore SSL certificate errors
            dismissProgressBar();
        }
    }

    private void showLoadingBarFirsTime() {
        LoggerD.debugLog(TAG + " showLoadingBarFirsTime");
        String message = "Loading...";
        if (APIConstants.TYPE_SPORTS.equalsIgnoreCase(type)) {
            message = getString(R.string.msg_loading_sports);
            Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, contentName, 1l);
        } else {
            return;
        }
        if (mSingleTimeProgressDialog != null && mSingleTimeProgressDialog.isShowing()) {
            return;
        }
        mSingleTimeProgressDialog = ProgressDialog.show(this, "", message, true, false, null);
        mSingleTimeProgressDialog.setContentView(com.myplex.sdk.R.layout.layout_progress_dialog);
        ((TextView) mSingleTimeProgressDialog.findViewById(R.id.textView1)).setText(message);
        mSingleTimeProgressDialog.setCanceledOnTouchOutside(false);
        ProgressBar mProgressBar = (ProgressBar) mSingleTimeProgressDialog.findViewById(com.myplex.sdk.R.id.imageView1);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(this, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mHandler.postDelayed(mDismissCallback, LOAD_OFFSET);
    }


    private class CustomChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            //Log.d(TAG, "onJsAlert " + url);
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    PlayerViewWebActivity.this);
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
                    PlayerViewWebActivity.this);
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
                    PlayerViewWebActivity.this);
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
            if (!webChromeClient.onBackPressed()) {
                // Standard back button implementation (for example this could close the app)
                if (webView != null) {
                    webView.stopLoading();
                    webView.removeAllViews();
                    webView.clearCache(true);
                    webView.getSettings().setAppCacheEnabled(false);
                    webView.destroy();
                    webView = null;
                }
                finish();
                super.onBackPressed();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void showProgressBar() {
        if (isProgressDialogCancelable) {
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        OnCancelListener onCancelListener = new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (isProgressDialogCancelable) {
                    finish();
                }
            }
        };
        showProgressDialog(onCancelListener);
    }

    private void showProgressDialog(OnCancelListener onCancelListener) {
        LoggerD.debugLog(TAG + " showProgressDialog");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressDialog.show(this, "", "Loading...", true,
                isProgressDialogCancelable, onCancelListener);
        mProgressDialog.setContentView(com.myplex.sdk.R.layout.layout_progress_dialog);
        mProgressDialog.setCanceledOnTouchOutside(false);
        ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(com.myplex.sdk.R.id.imageView1);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(this, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void dismissProgressBar() {
        try {
            if (isFinishing()) {
                return;
            }
            progressBar.setVisibility(View.INVISIBLE);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Intent createIntent(Context context, String url, String type, String contentName) {
        Intent i = new Intent(context, PlayerViewWebActivity.class);
        i.putExtra("url", url);
        i.putExtra("type", type);
        i.putExtra("contentName", contentName);
        return i;
    }

}
