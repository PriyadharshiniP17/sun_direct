package com.myplex.myplex.ui.views;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import androidx.appcompat.app.AlertDialog;
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
import com.myplex.api.APIConstants;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.LoginActivity;

public class PrivacyPolicyDialogWebView {
    private final Activity mContext;
    private View aboutDialogView;
    private TextView acceptBtn;
    private TextView declineBtn;
    private TextView mTextViewLoading;
    private WebView liveWebView;
    private AlertDialog aboutAlertDialog;
    private FbWebViewClient webviewclient;
    private ProgressBar progressBar;
    private int enabledisablePlayerLogsClickCount;
    private DialogListener dialogListener;

    public void showDialog() {
        if (mContext == null || mContext.isFinishing()) {
            LoggerD.debugLog("alertDialog instance is null or isFinishing");
            return;
        }
        if (aboutAlertDialog == null) {
            LoggerD.debugLog("alertDialog instance is null");
        }
        aboutAlertDialog.show();
    }

    public PrivacyPolicyDialogWebView(Activity context, final DialogListener dialogListener) {
        this.dialogListener = dialogListener;
        this.mContext = context;
        if (mContext == null || mContext.isFinishing()) {
            LoggerD.debugLog("alertDialog instance is null or isFinishing");
            return;
        }
        try {
//            CleverTap.eventPageViewed(CleverTap.PAGE_ABOUT);
//            AppsFlyerTracker.eventBrowseAbout();
//            Analytics.createScreenGA(Analytics.SCREEN_ABOUT);
//            String versionName = Util.getAppVersionName(mContext);
            AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);
            LayoutInflater aboutInflater = LayoutInflater.from(mContext);
            aboutBuilder.setCancelable(false);
            aboutDialogView = aboutInflater.inflate(R.layout.privacy_policy_layout, null);
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
          //  liveWebView.loadUrl(APIConstants.USER_PRIVACY_POLICY_URL);
//            TextView aboutTxt = (TextView) aboutDialogView.findViewById(R.id.about_txt);
            TextView aboutTitleText = (TextView) aboutDialogView.findViewById(R.id
                    .about_title);
            acceptBtn = (TextView) aboutDialogView.findViewById(R.id.accept_txt_btn);
            declineBtn = (TextView) aboutDialogView.findViewById(R.id.deny_txt_btn);
            acceptBtn.setVisibility(View.GONE);
//            aboutTitleText.setText(mContext.getString(R.string.app_name) + "\t" + versionName + "." + mContext.getString(R.string.app_sub_version));
//            aboutTxt.setText(mContext.getResources().getString(R.string.about_sundirect));
            aboutBuilder.setView(aboutDialogView);
            aboutAlertDialog = aboutBuilder.create();
            aboutAlertDialog.setCancelable(false);
            //aboutAlertDialog.setCancelable(false);
            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aboutAlertDialog.cancel();
                    if (dialogListener != null) {
                        dialogListener.onAccept();
                    }
                }
            });
            //aboutAlertDialog.setCancelable(false);
            declineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aboutAlertDialog.cancel();
                    if (dialogListener != null) {
                        dialogListener.onDecline();
                    }
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
            try {
                if (mContext == null || mContext.isFinishing()) return false;
                if (mContext instanceof LoginActivity) {
                    if (((LoginActivity)mContext).isActivityVisible()) {
                        Intent i = new Intent(mContext, LiveScoreWebView.class);
                        //i.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));
                        i.putExtra("url", url);
                        mContext.startActivity(i);
                    }
                } else {
                    view.loadUrl(url);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            LoggerD.debugLog("ONRECEIVEDERROR " + failingUrl + " " + description);
            closed = true;
//            dofinish();
            mTextViewLoading.setText(description);
            dismissProgressBar();
            acceptBtn.setVisibility(View.GONE);
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
            if (!closed) {
                mTextViewLoading.setVisibility(View.GONE);
                liveWebView.setVisibility(View.VISIBLE);
                acceptBtn.setVisibility(View.VISIBLE);
                declineBtn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.cancel(); // Ignore SSL certificate errors
            acceptBtn.setVisibility(View.GONE);
            dismissProgressBar();
        }
    }

    private void dofinish() {
        if (aboutAlertDialog != null) {
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
        }

    }


    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public interface DialogListener {
        void onAccept();

        void onDecline();
    }
}
