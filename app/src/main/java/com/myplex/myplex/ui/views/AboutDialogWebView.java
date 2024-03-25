package com.myplex.myplex.ui.views;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.BuildConfig;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.debug.NotificationDebugActivity;
import com.myplex.myplex.utils.Util;

public class AboutDialogWebView {
    private static final java.lang.String ABOUT_DIALOG_URL = "/debug/about";
    private final Context mContext;
    private TextView mTextViewLoading;
    private WebView liveWebView;
    private AlertDialog aboutAlertDialog;
    private FbWebViewClient webviewclient;
    private ProgressBar progressBar;
    private int enabledisablePlayerLogsClickCount;

    public void showDialog() {
        if(aboutAlertDialog == null){
            LoggerD.debugLog("alertDialog instance is null");
        }
        aboutAlertDialog.show();
    }

    public AboutDialogWebView(Context context){
		this.mContext = context;
        if(mContext == null){
            LoggerD.debugLog("alertDialog instance is null");
            return;
        }
        try {
            CleverTap.eventPageViewed(CleverTap.PAGE_ABOUT);
            AppsFlyerTracker.eventBrowseAbout();
//            Analytics.createScreenGA(Analytics.SCREEN_ABOUT);
            FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_ABOUT);
            String versionName = Util.getAppVersionName(mContext);
            AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);
            LayoutInflater aboutInflater = LayoutInflater.from(mContext);
            final View aboutDialogView = aboutInflater.inflate(R.layout.about_layout, null);
            liveWebView = (WebView) aboutDialogView.findViewById(R.id.webview);
            progressBar = (ProgressBar) aboutDialogView.findViewById(R.id.customactionbar_progressBar);
//            textview_network_error
            mTextViewLoading = (TextView) aboutDialogView.findViewById(R.id.textview_network_error);
            mTextViewLoading.setVisibility(View.VISIBLE);
            mTextViewLoading.setText(mContext.getString(R.string.loading_txt));
            liveWebView.setVisibility(View.GONE);
            liveWebView.setVerticalScrollBarEnabled(false);
            liveWebView.setHorizontalScrollBarEnabled(false);

            liveWebView.setWebViewClient(webviewclient = new FbWebViewClient());
            liveWebView.setWebChromeClient(new CustomChromeClient());
            WebSettings webSettings = liveWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setLoadsImagesAutomatically(true);
            String aboutAppURL = ABOUT_DIALOG_URL;
            //aboutAppURL = "http://"+mContext.getString(R.string.config_domain_name)+aboutAppURL;
            if(!TextUtils.isEmpty(PrefUtils.getInstance().getAboutapp_url())) {
                aboutAppURL = PrefUtils.getInstance().getAboutapp_url();
            }else{
                aboutAppURL = "http://"+mContext.getString(R.string.config_domain_name)+aboutAppURL;
            }
            liveWebView.loadUrl(aboutAppURL);
//            TextView aboutTxt = (TextView) aboutDialogView.findViewById(R.id.about_txt);
            TextView aboutTitleText = (TextView) aboutDialogView.findViewById(R.id
                    .about_title);
            TextView aboutOkTxt = (TextView) aboutDialogView.findViewById(R.id.about_ok_txt);
            aboutTitleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SDKLogger.debug("is debug build- " + BuildConfig.DEBUG);
                    if (!BuildConfig.DEBUG) {
                        return;
                    }
                    enabledisablePlayerLogsClickCount++;
                    if (enabledisablePlayerLogsClickCount % 6 == 0) {
                        Intent i = new Intent(mContext, NotificationDebugActivity.class);
                        mContext.startActivity(i);
                    }
                }
            });
            aboutTitleText.setText(mContext.getString(R.string.app_name) + "\t" + versionName + "." + mContext.getString(R.string.app_sub_version));
//            aboutTxt.setText(mContext.getResources().getString(R.string.about_sundirect));
            aboutBuilder.setView(aboutDialogView);
            aboutAlertDialog = aboutBuilder.create();

            //aboutAlertDialog.setCancelable(false);
            aboutOkTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aboutAlertDialog.cancel();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
    private class FbWebViewClient extends WebViewClient {
        boolean closed = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LoggerD.debugLog("OVERRIDE " + closed + " " + url);
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            LoggerD.debugLog("ONRECEIVEDERROR " + failingUrl + " " + description);
            closed = true;
            dofinish();
            mTextViewLoading.setText(description);
            dismissProgressBar();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            LoggerD.debugLog("PageStarted " + url);
            showProgressBar();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            LoggerD.debugLog("PageFinished " + url);
            dismissProgressBar();
            mTextViewLoading.setVisibility(View.GONE);
            liveWebView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.cancel(); // Ignore SSL certificate errors
            dismissProgressBar();
        }
    }

    private void dofinish() {
        if(aboutAlertDialog != null){
            aboutAlertDialog.cancel();
        }
    }

    private class CustomChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            LoggerD.debugLog("onJsAlert " + url);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
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
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            LoggerD.debugLog("onJsConfirm " + url);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
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
            LoggerD.debugLog("onJsPrompt " + url);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
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
        };
    }


    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

}
