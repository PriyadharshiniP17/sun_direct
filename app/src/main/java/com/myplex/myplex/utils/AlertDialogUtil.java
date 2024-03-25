package com.myplex.myplex.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.myplex.api.myplexAPISDK;

import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;

import java.lang.reflect.Field;

/**
 * Created by Samir on 12/12/2015.
 */

public class AlertDialogUtil {


    private static Dialog sAlertDialog;
    private static MaterialAlertDialogBuilder sAlertDialogBuilder;
    private static ProgressDialog mProgressDialog;

    public interface DialogListener{
        void onDialog1Click();
        void onDialog2Click();
    }


    public interface DialogListenerWithCancelConsent{
        boolean onDialog1Click();
        boolean onDialog2Click();
    }

    public interface NeutralDialogListener{
        void onDialogClick(String buttonText);
    }

    /**
     * Shows a toast notification.
     *
     * @param text Message to be displayed.
     */
    public static void showToastNotification(final CharSequence text) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            Toast.makeText(myplexAPISDK.getApplicationContext(), text, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(myplexAPISDK.getApplicationContext(), text, Toast.LENGTH_LONG).show();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Shows alert dialog with provided message.
     *
     * @param message Error message to be displayed.
     * @param mContext Application context.
     */
    public static void showNeutralAlertDialog(final Context mContext, CharSequence message, CharSequence title, final String
            buttonText, final NeutralDialogListener mNeutralDialogListener) {
        try {
            if (mContext == null || ((Activity)mContext).isFinishing()) {
                return;
            }
            sAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
            sAlertDialogBuilder.setCancelable(false);
            if(title != null){
                sAlertDialogBuilder.setTitle(title);
            }
            if (message != null) {
                sAlertDialogBuilder.setMessage(message);
            }

            sAlertDialogBuilder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    if (sAlertDialog != null) {
                        sAlertDialog.dismiss();
                    }
                    if (mNeutralDialogListener != null) {
                        mNeutralDialogListener.onDialogClick(buttonText);
                    }

                }
            });
            sAlertDialog = sAlertDialogBuilder.create();
            if (sAlertDialog != null) {
                sAlertDialog.show();
            }
        } catch (final Throwable e) {
            Log.d(AlertDialogUtil.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }
    }

    /**
     * Shows alert dialog with provided message.
     *
     * @param message Error message to be displayed.
     * @param mContext Application context.
     */
    public static void showNeutralAlertDialog(Context mContext,CharSequence message, CharSequence title, boolean isCancellable,  final String
            buttonText,final NeutralDialogListener mNeutralDialogListener) {
        try {
            if (mContext == null || ((Activity)mContext).isFinishing()) {
                return;
            }
            sAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext,R.style.AlertDialogTheme);
            if(title != null){
                sAlertDialogBuilder.setTitle(title);
            }
            if(message != null){
                sAlertDialogBuilder.setMessage(message);
            }

            sAlertDialogBuilder.setCancelable(isCancellable);


            sAlertDialogBuilder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (sAlertDialog != null) {
                        sAlertDialog.dismiss();
                    }
                    if (mNeutralDialogListener != null) {
                        mNeutralDialogListener.onDialogClick(buttonText);
                    }

                }
            });
            sAlertDialog = sAlertDialogBuilder.create();
            if (sAlertDialog != null) {
                sAlertDialog.show();
            }
        } catch (final Throwable e) {
            Log.d(AlertDialogUtil.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }
    }

    /**
     * Shows alert dialog with provided message.
     *
     * @param message  Error message to be displayed.
     * @param mContext Application context.
     */
    public static void showAlertDialog(final Context mContext, CharSequence message, CharSequence title,
                                       final String negativeButtonTextbutton1,
                                       final String positiveButtonTextbutton2,
                                       final DialogListener mDialogListener) {
        try {
            if (mContext == null || ((Activity)mContext).isFinishing()) {
                return;
            }
            sAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext,R.style.AlertDialogTheme);

            if(title != null){
                sAlertDialogBuilder.setTitle(title);
            }
            if(message != null){
                sAlertDialogBuilder.setMessage(message);
            }

            sAlertDialogBuilder.setNegativeButton(negativeButtonTextbutton1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (sAlertDialog != null) {
                        sAlertDialog.dismiss();
                    }
                    if (mDialogListener != null) {
                        mDialogListener.onDialog1Click();
                    }

                }
            });


            sAlertDialogBuilder.setPositiveButton(positiveButtonTextbutton2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (sAlertDialog != null) {
                        sAlertDialog.dismiss();
                    }
                    if (mDialogListener != null) {
                        mDialogListener.onDialog2Click();
                    }

                }
            });
            sAlertDialog = sAlertDialogBuilder.create();
            if (sAlertDialog != null) {
                sAlertDialog.show();
            }
        } catch (final Throwable e) {
            Log.d(AlertDialogUtil.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }

    }


    public static void showAlertDialogWithWebView(final Context mContext,String url, CharSequence title,
                                       final String negativeButtonTextbutton1,
                                       final String positiveButtonTextbutton2,
                                       final DialogListener mDialogListener) {
        try {
            if (mContext == null || ((Activity)mContext).isFinishing()) {
                return;
            }
            sAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext,R.style.AlertDialogTheme);

            if(title != null){
                sAlertDialogBuilder.setTitle(title);
            }
            WebView wv = new WebView(mContext);
            wv.loadUrl(url);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

            });

            sAlertDialogBuilder.setView(wv);
            sAlertDialogBuilder.setNegativeButton(negativeButtonTextbutton1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (sAlertDialog != null) {
                        sAlertDialog.dismiss();
                    }
                    if (mDialogListener != null) {
                        mDialogListener.onDialog1Click();
                    }

                }
            });


            sAlertDialogBuilder.setPositiveButton(positiveButtonTextbutton2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (sAlertDialog != null) {
                        sAlertDialog.dismiss();
                    }
                    if (mDialogListener != null) {
                        mDialogListener.onDialog2Click();
                    }

                }
            });
            sAlertDialog = sAlertDialogBuilder.create();
            if (sAlertDialog != null) {
                sAlertDialog.show();
            }
        } catch (final Throwable e) {
            Log.d(AlertDialogUtil.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }

    }


    /**
     * Shows alert dialog with provided message.
     *
     * @param message  Error message to be displayed.
     * @param mContext Application context.
     */
    public static void showAlertDialog(final Context mContext, CharSequence message, CharSequence title,
                                       final boolean isCancellable,
                                       final String negativeButtonText,
                                       final String positiveButtonText,
                                       final DialogListener mDialogListener) {
            try {
                if (mContext == null || ((Activity)mContext).isFinishing()) {
                    return;
                }
                sAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext,R.style.AlertDialogTheme);
                sAlertDialogBuilder.setCancelable(isCancellable);
                if(title != null){
                    sAlertDialogBuilder.setTitle(title);
                }
                if(message != null){
                    sAlertDialogBuilder.setMessage(message);
                }
                sAlertDialogBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (sAlertDialog != null) {
                            sAlertDialog.dismiss();
                        }
                        if (mDialogListener != null) {
                            mDialogListener.onDialog1Click();
                        }

                    }
                });


                sAlertDialogBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (sAlertDialog != null) {
                            sAlertDialog.dismiss();
                        }
                        if (mDialogListener != null) {
                            mDialogListener.onDialog2Click();
                        }

                    }
                });
                sAlertDialog = sAlertDialogBuilder.create();
                if (sAlertDialog != null) {
                    sAlertDialog.show();
                }
            } catch (final Throwable e) {
                Log.d(AlertDialogUtil.class.getSimpleName(), "showAlertDialog(): Failed.", e);
            }

        }

    public static void showProgressAlertDialog(Context mContext) {
        try {
            if (mContext == null || ((Activity)mContext).isFinishing()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
            ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
            final int version = Build.VERSION.SDK_INT;
            if (version < 21) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void showProgressAlertDialog(Context mContext, CharSequence title, CharSequence message, boolean intermediate, boolean cancellable, DialogInterface.OnCancelListener cancelListener) {
        try {
            if (mContext == null || ((Activity)mContext).isFinishing()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            mProgressDialog = ProgressDialog.show(mContext, title, message, intermediate, cancellable, cancelListener);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
            ((TextView)mProgressDialog.findViewById(R.id.textView1)).setText(message);
            ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
            final int version = Build.VERSION.SDK_INT;
            if (version < 21) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void dismissProgressAlertDialog() {
        try {
            if (mProgressDialog != null
                    && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


/*
    private void showPrivacyPolicyUpdate(final Activity mContext) {
        if (mContext == null || mContext.isFinishing()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        builder.setCancelable(false);

        //alert.setTitle("Privacy Policy ");
        WebView webView = new WebView(mContext);
//        webView.setMinimumHeight((int) mContext.getResources().getDimension(R.dimen.margin_gap_192));
        webView.loadUrl(APIConstants.USER_PRIVACY_POLICY_URL);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //view.loadUrl(url);
                try {
                    if (mContext == null || mContext.isFinishing()) return false;
                    Intent i = new Intent(mContext, LiveScoreWebView.class);
                    //i.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));
                    i.putExtra("url", url);
                    mContext.startActivity(i);
                } catch (Throwable t) {
                    t.printStackTrace();
                    Crashlytics.logException(t);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                SDKLogger.debug("request- " + request);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    SDKLogger.debug("url- " + request.getUrl());
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                SDKLogger.debug("request- " + url);
            }
        });
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;

                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }

                return false;
            }
        });

        builder.setView(webView);
        builder.setNegativeButton(getResources().getString(R.string.deny), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                try {
                    if (mContext == null || mContext.isFinishing()) return;
                    CleverTap.eventPrivacyPolicyClicked(CleverTap.VALUE_DECLINE);
                    mContext.finish();
                } catch (Throwable t) {
                    t.printStackTrace();
                    Crashlytics.logException(t);
                }
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //dialog.dismiss();
                try {
                    if (dialog != null) {
                        dialog.dismiss();
                        PrefUtils.getInstance().setPrefShowPrivacyConsent(false);
                    }
                    proceedDeviceRegistration();
                    CleverTap.eventPrivacyPolicyClicked(CleverTap.VALUE_ACCEPT);
                } catch (Throwable t) {
                    t.printStackTrace();
                    Crashlytics.logException(t);
                }
            }
        });
//        TextView textView = new TextView(mContext);
//        textView.setText();
//        textView.setGravity(Gravity.CENTER_HORIZONTAL);
//        textView.setTextColor(mContext.getResources().getColor(R.color.white));
        builder.setTitle("Privacy Policy");
        AlertDialog dialog = builder.create();
        dialog.show();
        if (webView.getLayoutParams() == null) {
            SDKLogger.debug("null layout params");
        } else {
            webView.getLayoutParams().height = 400;
        }
    }
*/


    public static void dismissDialog() {
        if (sAlertDialog != null) {
            sAlertDialog.dismiss();
            sAlertDialog.cancel();
        }
    }

    /**
     * Shows alert dialog with provided message.
     *
     * @param message  Error message to be displayed.
     * @param mContext Application context.
     */
    public static void showCustomViewAlertDialog(final Context mContext,
                                                 boolean isCancellable,
                                                 CharSequence message,
                                                 CharSequence title,
                                                 View view,
                                                 final String negativeButtonTextbutton1,
                                                 final String positiveButtonTextbutton2,
                                                 final DialogListenerWithCancelConsent mDialogListener) {
        try {
            if (mContext == null || ((Activity) mContext).isFinishing()) {
                return;
            }
            sAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext,R.style.AlertDialogTheme);
            sAlertDialogBuilder.setCancelable(isCancellable);
            if (title != null) {
                sAlertDialogBuilder.setTitle(title);
            }
            if (message != null) {
                sAlertDialogBuilder.setMessage(message);
            }
            if (!TextUtils.isEmpty(positiveButtonTextbutton2)) {
                sAlertDialogBuilder.setPositiveButton(positiveButtonTextbutton2, null);
            }
            if (!TextUtils.isEmpty(negativeButtonTextbutton1)) {
                sAlertDialogBuilder.setNegativeButton(negativeButtonTextbutton1, null);
            }
            sAlertDialogBuilder.setView(view, 8, 16, 8, 16);
            sAlertDialog = sAlertDialogBuilder.create();
            sAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button negativeButton = ((AlertDialog)sAlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    Button positiveButton= ((AlertDialog)sAlertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    if (negativeButton != null)
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (sAlertDialog != null
                                        && mDialogListener != null
                                        && mDialogListener.onDialog1Click()) {
                                    sAlertDialog.dismiss();
                                }
                            }
                        });
                    if (positiveButton != null)
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (sAlertDialog != null
                                        && mDialogListener != null
                                        && mDialogListener.onDialog2Click()) {
                                    sAlertDialog.dismiss();
                                }
                            }
                        });
                }
            });
            if (sAlertDialog != null) {
                sAlertDialog.show();
            }
            Field f = null;
            try {
                f = sAlertDialog.getClass().getDeclaredField("mAlert");
                f.setAccessible(true);//Very important, this allows the setting to work.
                Object alert =  f.get(sAlertDialog);
                f = alert.getClass().getDeclaredField("mMessageView");
                f.setAccessible(true);//Very important, this allows the setting to work.
                TextView messagetextView = (TextView) f.get(alert);
                LinearLayout.LayoutParams layoutmargin = ((LinearLayout.LayoutParams) messagetextView.getLayoutParams());
                layoutmargin.topMargin = 24;
                messagetextView.invalidate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            Log.d(AlertDialogUtil.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }

    }


}
