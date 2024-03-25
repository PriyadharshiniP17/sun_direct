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
import android.net.MailTo;
import android.net.ParseException;
import android.net.Uri;
import android.net.http.SslError;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.BuildConfig;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.utils.Util;


public class LiveScoreWebView extends BaseActivity implements
        AlertDialogUtil.DialogListener {
	private static final int LOAD_OFFSET = 8 * 1000;
	public static final String PARAM_TOOLBAR_TITLE = "toolbar_title";
	private WebView liveWebView;
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
	private java.lang.Runnable mDismissCallback = new Runnable() {
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
	private String title;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private View.OnClickListener mCloseAction = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.layout_webview);
		liveWebView = (WebView) findViewById(R.id.webview);
		mHandler = new Handler();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setContentInsetsAbsolute(0,0);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));

		progressBarLayout = (RelativeLayout) findViewById(R.id.progress_layout);
//		liveWebView.getSettings().setJavaScriptEnabled(true);
		url = new String();
		try {
			Intent extras = getIntent();
			if (extras != null) {
				url = getIntent().getStringExtra("url");
				Log.d("Live",url);
				title = getIntent().getStringExtra(PARAM_TOOLBAR_TITLE);
				if(title.equals(getResources().getString(R.string.contact_us)))
					mToolbar.setVisibility(View.VISIBLE);
				type = getIntent().getStringExtra("type");
				if (APIConstants.getFAQURL().equalsIgnoreCase(url)) {
					AppsFlyerTracker.eventBrowseTermsAndConditions();
					CleverTap.eventPageViewed(CleverTap.PAGE_TERMS_AND_CONDITIONS);
				} else if(APIConstants.getHelpURL().equalsIgnoreCase(url)){
					CleverTap.eventPageViewed(CleverTap.PAGE_HELP);
					AppsFlyerTracker.eventBrowseHelp();
				}

				contentName = getIntent().getStringExtra("contentName");
				if (APIConstants.getFAQURL().equalsIgnoreCase(url)) {
//					Analytics.createScreenGA(Analytics.SCREEN_TERMS_N_CONDITIONS);
					FirebaseAnalytics.getInstance().createScreenFA(this,Analytics.SCREEN_TERMS_N_CONDITIONS);
				} else if (APIConstants.getHelpURL().equalsIgnoreCase(url)) {
//					Analytics.createScreenGA(Analytics.SCREEN_HELP);
					FirebaseAnalytics.getInstance().createScreenFA(this,Analytics.SCREEN_HELP);
				}
				isProgressDialogCancelable = getIntent().getBooleanExtra("isProgressDialogCancelable", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		View mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
		TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
		ImageView mCloseIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
		RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
		Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		mRootLayout.setLayoutParams(layoutParams);
		mToolbar.addView(mInflateView);
		mCloseIcon.setOnClickListener(mCloseAction);
		ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
		mChannelImageView.setVisibility(View.GONE);
		mToolbarTitle.setText(R.string.app_name);
		if (!TextUtils.isEmpty(title)){
			mToolbarTitle.setText(title);
			mToolbarTitle.setTextColor(getResources().getColor(R.color.white));
		}
		mToolbar.setVisibility(View.VISIBLE);
//		setSupportActionBar(mToolbar);
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		getSupportActionBar().setCustomView(R.layout.activity_live_score);
		progressBar = (ProgressBar) findViewById(R.id.customactionbar_progressBar);
        if(url == null || url.isEmpty()){
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
		if (Build.VERSION.SDK_INT >= 21) {
			liveWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}

		//FOR WEBPAGE SLOW UI
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			liveWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			liveWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		liveWebView.setVerticalScrollBarEnabled(false);
		liveWebView.setHorizontalScrollBarEnabled(false);
		liveWebView.setWebViewClient(webviewclient = new FbWebViewClient());
		liveWebView.setWebChromeClient(new CustomChromeClient());
		WebSettings webSettings = liveWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadsImagesAutomatically(true);
		liveWebView.loadUrl(url);
		showLoadingScreen(true);
		showLoadingBarFirsTime();

	}

	private void showLoadingScreen(boolean b) {
		if (b) {
			liveWebView.setVisibility(View.GONE);
			progressBarLayout.setVisibility(View.VISIBLE);
			return;
		}
        progressBarLayout.setVisibility(View.INVISIBLE);
        liveWebView.setVisibility(View.VISIBLE);
	}

	@Override
    public void onDialog1Click() {
        liveWebView.loadUrl(url + "&force=true");
    }

    @Override
    public void onDialog2Click() {
        dofinish();
    }

    private class FbWebViewClient extends WebViewClient {
		boolean closed = false;


		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String redirectUrl) {
			//Log.i(TAG, "OVERRIDE " + closed + " " + redirectUrl);


			String appRedirectUrl="/home";
			if (PrefUtils.getInstance().getAppUrlRedirectionUrl()!=null&&!TextUtils.isEmpty(PrefUtils.getInstance().getAppUrlRedirectionUrl())){
				appRedirectUrl=PrefUtils.getInstance().getAppUrlRedirectionUrl();
			}
/*
			if (url != null
					&& !url.startsWith("https")
					&& !url.startsWith("http")) {
//				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//				startActivity(intent);
				return true;
			}
*/
			if (redirectUrl.startsWith("intent://")) {
				try {
					/*if (APIConstants.TYPE_SPORTS.equalsIgnoreCase(type)) {
						mHandler.removeCallbacks(mDismissCallback);
					}*/
					showLoadingScreen(false);
					/*if (mSingleTimeProgressDialog != null && mSingleTimeProgressDialog.isShowing()) {
						mSingleTimeProgressDialog.dismiss();
					}*/
					Intent intent = new Intent().parseUri(redirectUrl, Intent.URI_INTENT_SCHEME);

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
			}else if (redirectUrl.startsWith("mailto:")) {
				String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
				String phn = TextUtils.isEmpty(msisdn) ? "" : msisdn;
				String version = BuildConfig.VERSION_NAME;
				//String versionName = Util.getAppVersionName();
				String connectionType = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
				String operatingSystem = "Android "+ Build.VERSION.SDK_INT;
				String brand = Build.BRAND;
				String model = Build.MODEL;
				String device = Build.DEVICE;
				String emailBody = "\n\n\n\n\n-----------------\n\n\n Device Details:  \n\n APP VERSION: "+ version+
						"\n DEVICE:"+ device+"\n BRAND:"+ brand+"\n MODEL:"+ model+"\n OPERATING SYSTEM:"+ operatingSystem
						+"\n CONNECTION_TYPE:"+ connectionType
						;

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
					// Do something for lollipop and above versions
					BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
					int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
					emailBody = emailBody+"\n BATTERY_LEVEL:"+ batLevel;
				} else{
					// do something for phones running an SDK before lollipop
				}
				if(!TextUtils.isEmpty(phn)){
					if(Util.isValidEmail(phn)){
						emailBody = emailBody+"\n EMAIL:"+ phn;
					}else {
						emailBody = emailBody+"\n PHONE NUMBER:"+ phn;
					}
				}
				try {
					MailTo mailTo = MailTo.parse(redirectUrl);
					String emailAddress = mailTo.getTo();
					String subject = mailTo.getSubject();
					Intent mail = new Intent(Intent.ACTION_SENDTO);
					mail.setData(Uri.parse("mailto:"));
					mail.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
					mail.putExtra(Intent.EXTRA_SUBJECT, subject);
					mail.putExtra(Intent.EXTRA_TEXT, emailBody);
					startActivity(mail);
				} catch (ParseException e) {
					e.printStackTrace();
					AlertDialogUtil.showToastNotification("No Mail Clients were found");
				}
				return true;
			} else if (url != null && PrefUtils.getInstance().getContactUsPageURL() != null
                    && url.equalsIgnoreCase(PrefUtils.getInstance().getContactUsPageURL())) {
                if (redirectUrl.contains(appRedirectUrl)) {
                    dofinish();
                    return true;
                }
            }
			view.loadUrl(redirectUrl);
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
			if (liveWebView != null) {
				liveWebView.setVisibility(View.VISIBLE);
			}
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
		LoggerD.debugLog(TAG+" showLoadingBarFirsTime");
		String message = "Loading...";
		if(APIConstants.TYPE_SPORTS.equalsIgnoreCase(type)){
			message = getString(R.string.msg_loading_sports);
			Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, contentName,1l);
		} else {
			return;
		}
		if(mSingleTimeProgressDialog != null && mSingleTimeProgressDialog.isShowing()){
			return;
		}
		mSingleTimeProgressDialog = ProgressDialog.show(this, "", message, true, false, null);
		mSingleTimeProgressDialog.setContentView(R.layout.layout_progress_dialog);
		((TextView) mSingleTimeProgressDialog.findViewById(R.id.textView1)).setText(message);
		mSingleTimeProgressDialog.setCanceledOnTouchOutside(false);
		ProgressBar mProgressBar = (ProgressBar) mSingleTimeProgressDialog.findViewById(com.myplex.sdk.R.id.imageView1);
		final int version = Build.VERSION.SDK_INT;
		if (version < 21) {
			mProgressBar.setIndeterminate(false);
			mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(this,R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
		}
		mHandler.postDelayed(mDismissCallback,LOAD_OFFSET);
	}


	private class CustomChromeClient extends WebChromeClient {

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				final JsResult result) {
			//Log.d(TAG, "onJsAlert " + url);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					LiveScoreWebView.this);
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
					LiveScoreWebView.this);
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
					LiveScoreWebView.this);
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
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			newProgress=0;
			liveWebView.setBackgroundColor(getResources().getColor(R.color.black));
			super.onProgressChanged(view, newProgress);
			if(newProgress == 0){
				progressBar.setVisibility(View.GONE);
				liveWebView.setVisibility(View.VISIBLE);
				//Log.d(TAG,"onProgressChanged- newProgress- "+ newProgress);
			}
		}

		}



	@Override
	public void onBackPressed() {
		super.onBackPressed();

		try {
			if (liveWebView != null) {
				liveWebView.stopLoading();
				liveWebView.removeAllViews();
				liveWebView.clearCache(true);
				liveWebView.getSettings().setAppCacheEnabled(false);
				liveWebView.destroy();
				liveWebView = null;
			}
			finish();
		}catch(Throwable t) {
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
		LoggerD.debugLog(TAG+" showProgressDialog");
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(this, "", "Loading...", true,
				isProgressDialogCancelable, onCancelListener);
		mProgressDialog.setContentView(R.layout.layout_progress_dialog);
		mProgressDialog.setCanceledOnTouchOutside(false);
		ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(com.myplex.sdk.R.id.imageView1);
		final int version = Build.VERSION.SDK_INT;
		if (version < 21) {
			mProgressBar.setIndeterminate(false);
			mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(this,R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
		}
	}

	public void dismissProgressBar(){
		try {
			if(isFinishing()){
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

	public static Intent createIntent(Context context, String url, String type, String contentName){
		Intent i = new Intent(context, LiveScoreWebView.class);
		if(APIConstants.TYPE_SPORTS.equalsIgnoreCase(type)){
			i = new Intent(context, PlayerViewWebActivity.class);
		}
		i.putExtra("url", url);
		i.putExtra("type", type);
		i.putExtra("contentName", contentName);
		return i;
	}
}
