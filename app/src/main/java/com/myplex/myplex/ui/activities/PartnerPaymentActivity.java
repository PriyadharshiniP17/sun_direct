package com.myplex.myplex.ui.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.model.OfferResponseData;

import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class PartnerPaymentActivity extends Activity implements AlertDialogUtil.DialogListener {
	private static final String TAG="SubscriptionView";
    private static final String SUBSCR_CALLBACK_SUBSCRIPTION_CANCEL = "status";

	public static final String PARTNER_CONTENT_ID = "PARTNER_CONTENT_ID";
	public static final String PARTNER_NAME = "PARTNER_NAME";
	public static final String PACK_TYPE = "PACK_TYPE";
	public static final String PACK_PRICE = "PACK_PRICE";
	public static final String CONTENT_NAME = "CONTENT_NAME";
	public static final String CONTENT_IMAGE_URL = "CONTENT_IMAGE_URL";

	public static final int PAYMENT_FAILED = 101;
	public static final int PAYMENT_SUCCESS = 102;
	public static final int PAYMENT_INPROGRESS = 103;
//	public static final int PAYMENT_CANCELLED = 104;

	String partnerContentId = null;
	String partnerName = null;
	String packPrice = null;
	String packType = null;
	String imageUrl = null;

	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;
	private String callbackUrl = new String();
	private ProgressDialog mProgressDialog = null;

	private String mUrl;
	public String status;
	private boolean isProgressDialogCancelable = false;
	
	private static final String IDEA_SUBSCR_CALLBACK_SUCCESS="myplexnow.tv/?status";
	private static final String IDEA_SUBSCR_CALLBACK_CANCEL="consent=No";
	private static final String RESPONSE_ACTION_SHOWMESSAGE = "showMessage";
	private static final String RESPONSE_ACTION_REDIRECT = "redirect";
	private String contentName;
	private String transactionid;
	private Context mContext;

	//	http://api.beta.myplex.in/user/v2/billing/callback/evergent/
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mContext = this;

		try {
			Bundle b = this.getIntent().getExtras();
			if (b != null) {
				isProgressDialogCancelable = b.getBoolean("isProgressDialogCancelable", false);
				partnerContentId = b.getString(PARTNER_CONTENT_ID);
				partnerName = b.getString(PARTNER_NAME);
				packType = b.getString(PACK_TYPE);
				packPrice = b.getString(PACK_PRICE);
				contentName = b.getString(CONTENT_NAME);
				imageUrl = b.getString(CONTENT_IMAGE_URL);
			}

			mUrl = APIConstants.getPartnerSusbcriptionUrl(partnerName,
					partnerContentId,
					packType,
					packPrice,
					contentName,
					imageUrl);
			//Log.d(TAG, "mUrl= " + mUrl);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		callbackUrl = APIConstants.SCHEME + APIConstants.BASE_URL
				+ APIConstants.SLASH + APIConstants.USER_CONTEXT
				+ APIConstants.SLASH + APIConstants.BILLING_EVENT
				+ "/callback/evergent/";
		setContentView(R.layout.layout_webview);

		mixpanelPaymentSelected();
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setContentInsetsAbsolute(0,0);
		mToolbar.setBackgroundColor(getResources().getColor(R.color.app_bkg));
		//setSupportActionBar(mToolbar);
		if (mToolbar != null) {
			mToolbar.setLogo(R.drawable.app_icon);
		}
		mWebView= (WebView)findViewById(R.id.webview);

		boolean isToShowNetworkAlert = false;
		// check mobile data connection status
		// Skip if mobile data is disabled
		String errorMessage = mContext.getString(com.myplex.sdk.R.string.subscription_operator_data_disable);
		if (!ConnectivityUtil.isConnected(mContext)) {
			isToShowNetworkAlert = true;
			errorMessage = mContext.getString(com.myplex.sdk.R.string.network_error);
			LoggerD.debugLog("DataConnectionCheck: is not connected to network");
		}else if (!ConnectivityUtil.isConnectedMobile(mContext)
				 && !PrefUtils.getInstance().getPrefAllowWiFiNetworkForPayment()) {
			LoggerD.debugLog("DataConnectionCheck: is not connected to mobile data network may be on connected to wifi");
				LoggerD.debugLog("DataConnectionCheck: is not allowed to subscribe on wifi");
				isToShowNetworkAlert = true;
		}

		if(isToShowNetworkAlert){
			AlertDialogUtil.showAlertDialog(mContext, errorMessage,
					"",
					mContext.getString(R.string.alert_dataconnection_cancel), mContext.getString(R.string.alert_dataconnection_viewsetttings),
					new AlertDialogUtil.DialogListener() {

						@Override
						public void onDialog1Click() {
//							dofinish(PAYMENT_FAILED);
						}

						@Override
						public void onDialog2Click() {

							Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
							mContext.startActivity(intent);
//							dofinish(PAYMENT_FAILED);
						}

					});
			return;
		}
		try{
//			setUpWebView(mUrl);
			fetchOfferAvailability();
		}catch(Exception e){
			e.printStackTrace();
//			dofinish(PAYMENT_FAILED, e != null ? e.getMessage() : null);
		}

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


	private void dofinish(int response){
			closeSession(response);
	}

	private void dofinish(int response, String message){

		/*if(response == PAYMENT_FAILED){
			mixpanelPaymentFailed(message);
		}*/
		closeSession(response);
	}

	private void closeSession(int response){

		if(response == PAYMENT_INPROGRESS || response == PAYMENT_SUCCESS){
			mixpanelPaymentSuccess();
		}
		setResult(response);
		dismissProgressBar();
		finish();
	}
	private void setUpWebView(String url){
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(fbwebviewclient= new FbWebViewClient());
		mWebView.setWebChromeClient(new CustomChromeClient());
		WebSettings webSettings = mWebView.getSettings();
	    webSettings.setJavaScriptEnabled(true);
	    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	    webSettings.setLoadsImagesAutomatically(true);
		mWebView.loadUrl(url);
	}
	private String getTokenValue(String token){
		String returnValue = new String();
		StringTokenizer subtoken = new StringTokenizer(token,"=");
		while (subtoken.hasMoreTokens()) {
			returnValue = subtoken.nextToken(); 
	    }
		return returnValue;
	}
//	status=FAILURE&message=expired+card
	private class FbWebViewClient extends WebViewClient {
		boolean closed= false;

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//Log.i(TAG, "OVERRIDE "+closed+" "+url);
//			http://www.myplexnow.tv/?status=SUCCESS&message=You%20are%20already%20activate%20for%20this%20service.&action=showMessage
//			http://www.myplexnow.tv/?status=FAILED&action=showMessage&message=Failed&redirectLink=http%3A%2F%2Fwww.myplexnow.tv%3Fstatus%3DFAILED
			if(url.contains(callbackUrl) || url.contains(IDEA_SUBSCR_CALLBACK_SUCCESS) ||url.contains(IDEA_SUBSCR_CALLBACK_CANCEL)
                    || url.contains(SUBSCR_CALLBACK_SUBSCRIPTION_CANCEL)){
				try {
					String status = new String();
					String message = new String();
					String action = new String();
					String redirectLink = new String();
					URL aURL = new URL(url);
					String query = aURL.getQuery();
					StringTokenizer token = new StringTokenizer(query,"&");
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


					if(paramMap.containsKey(statusString))
						status = paramMap.get(statusString);
					if(paramMap.containsKey(messageString))
						message = paramMap.get(messageString);
						message = URLDecoder.decode(message);

					if(paramMap.containsKey(redirectLinkString))
						redirectLink = paramMap.get(redirectLinkString);
					redirectLink = URLDecoder.decode(redirectLink);

					if(paramMap.containsKey(actionString))
						action = paramMap.get(actionString);

					if(paramMap.containsKey(transactionString)) { //the python server must return transaction-id
						transactionid = paramMap.get(transactionString);
					}else {
						transactionid = contentName +"transactionId";
					}
					if(status.equalsIgnoreCase("SUCCESS")){
						if (RESPONSE_ACTION_REDIRECT.equalsIgnoreCase(action) && !TextUtils.isEmpty(redirectLink)) {
							launchBrowser(redirectLink);
							closeSession(PAYMENT_SUCCESS);
						} else if (RESPONSE_ACTION_SHOWMESSAGE.equalsIgnoreCase(action) && !TextUtils.isEmpty(message)) {
							AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", "Ok", new AlertDialogUtil.NeutralDialogListener() {
								@Override
								public void onDialogClick(String buttonText) {
									closeSession(PAYMENT_SUCCESS);
								}
							});
						} else {
							dofinish(PAYMENT_SUCCESS);
						}
					}else if(status.equalsIgnoreCase("ERR_IN_PROGRESS")){
						//Log.d(TAG, "error is progress");
						AlertDialogUtil.showAlertDialog(PartnerPaymentActivity.this, getResources()
                                .getString(R.string.transaction_server_error)
                                , ""
                                , getResources().getString(R.string.retry)
                                , getResources().getString(R.string.cancel)
                                , PartnerPaymentActivity.this);
					}else if(status.equalsIgnoreCase("ACTIVATION_INPROGRESS")){
						//Log.d(TAG, "error is progress");
						if(RESPONSE_ACTION_REDIRECT.equalsIgnoreCase(action) && !TextUtils.isEmpty(redirectLink)){
							launchBrowser(redirectLink);
							closeSession(PAYMENT_INPROGRESS);
						}else if (RESPONSE_ACTION_SHOWMESSAGE.equalsIgnoreCase(action) && !TextUtils.isEmpty(message)) {
							AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", "Ok", new AlertDialogUtil.NeutralDialogListener() {
								@Override
								public void onDialogClick(String buttonText) {
									closeSession(PAYMENT_INPROGRESS);
								}
							});
						} else{
							dofinish(PAYMENT_INPROGRESS);
						}
					} else if (status.equalsIgnoreCase("FAILED")) {
						if (!TextUtils.isEmpty(message)) {
							AlertDialogUtil.showToastNotification(message);
						}
//						dofinish(PAYMENT_FAILED, message);
					} else {
						AlertDialogUtil.showToastNotification("Subscription: " + message);
//						dofinish(PAYMENT_FAILED, message);
					}
					
				} catch (MalformedURLException e) {
//					dofinish(PAYMENT_FAILED, e != null ? e.getMessage() : null);
					e.printStackTrace();
				}
				return true;
			}
			view.loadUrl(url);
			return false;
		}
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.e(TAG, "ONRECEIVEDERROR " + failingUrl + " " + description);
			closed = true;
//			dofinish(PAYMENT_FAILED, description);
			dismissProgressBar();
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon){
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

	private void launchBrowser(String redirectLink) {
		try {
			if(TextUtils.isEmpty(redirectLink)){
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
		                PartnerPaymentActivity.this);
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
		                PartnerPaymentActivity.this);
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
					PartnerPaymentActivity.this);
			builder.setMessage(message)
					.setNeutralButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					}).show();
			result.cancel();
			return true;
		};
	}
	
	@Override
	public void onBackPressed() {
		if(mWebView != null
				&& mWebView.canGoBack()){
			mWebView.goBack();
			return;
		}
		AlertDialogUtil.showAlertDialog(PartnerPaymentActivity.this, getResources().getString(R.string
                .transaction_cancel_msg)
                ,""
                , getResources().getString(R.string.transaction_cancel_no)
                , getResources().getString(R.string.transaction_cancel_yes)

                , new AlertDialogUtil.DialogListener() {

            @Override
            public void onDialog1Click() {

            }

            @Override
            public void onDialog2Click() {

//                dofinish(PAYMENT_CANCELLED);
            }
        });
		
		
	}
	
	public void showProgressBar(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
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
		mProgressDialog = ProgressDialog.show(this,"", "Loading...", true,isProgressDialogCancelable,onCancelListener);
		mProgressDialog.setContentView(R.layout.layout_progress_dialog);

		ProgressBar mProgressbar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
		final int version = Build.VERSION.SDK_INT;
		if (version < 21) {
			mProgressbar.setIndeterminate(false);
			mProgressbar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
		}
//        mProgressDialog.setCanceledOnTouchOutside(false);

	}
	public void dismissProgressBar(){
			try {
				if(isFinishing()){
					return;
				}
				if(mProgressDialog != null && mProgressDialog.isShowing()){
					mProgressDialog.dismiss();
				}
				findViewById(R.id.customactionbar_progressBar).setVisibility(View.GONE);

			} catch (Throwable t) {
				t.printStackTrace();
			}
	}

    @Override
    public void onDialog1Click() {
        mWebView.loadUrl(mUrl+"&force=true");
    }

    @Override
    public void onDialog2Click() {
//        dofinish(PAYMENT_CANCELLED);

    }


	private void mixpanelPaymentSelected() {
		Map<String, String> params = new HashMap<>();
		params.put(Analytics.PROPERTY_CONTENT_NAME, contentName);
		params.put(Analytics.PARTNER_CONTENT_ID, partnerContentId);
		params.put(Analytics.PAYMENT_METHOD, "");
		params.put(Analytics.PAYMENT_PRICE, packPrice);
		params.put(Analytics.PURCHASE_TYPE, packType);
		params.put(Analytics.PARTNER_NAME, partnerName);
		Analytics.trackEvent(Analytics.EventPriority.LOW, Analytics.EVENT_PAYMENT_OPTION_SElECTED, params);
	}

	private void mixpanelPaymentSuccess() {
		Map<String, String> params = new HashMap<>();

		/*content name
		content id
		content type
		purchase type
		payment method
		content quality
		payment price
		user id
		language
		coupon used
		coupon discount
		partner name*/

		params.put(Analytics.PROPERTY_CONTENT_NAME, contentName);
		params.put(Analytics.PARTNER_CONTENT_ID, partnerContentId);
		params.put(Analytics.PAYMENT_METHOD, "");
		params.put(Analytics.PARTNER_NAME, partnerName);
		params.put(Analytics.PAYMENT_PRICE, packPrice);
		params.put(Analytics.PURCHASE_TYPE, packType);
		Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_PAYMENT_SUCCESS, params);
	}

	/*private void mixpanelPaymentFailed(String message) {
		Map<String, String> params = new HashMap<>();
		params.put(Analytics.PROPERTY_CONTENT_NAME, contentName);
		params.put(Analytics.PARTNER_CONTENT_ID, partnerContentId);
		params.put(Analytics.PAYMENT_METHOD, "");
		params.put(Analytics.PAYMENT_PRICE, packPrice);
		params.put(Analytics.REASON_FAILURE, TextUtils.isEmpty(message) ? APIConstants.NOT_AVAILABLE : message);
		params.put(Analytics.PURCHASE_TYPE, packType);
		params.put(Analytics.PARTNER_NAME, partnerName);
		Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_PAYMENT_FAILED, params);
	}*/


	private void fetchOfferAvailability() {
        showProgressBar();
		OfferedPacksRequest.Params params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.TYPE_HUNGAMA,null);
		final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
			@Override
			public void onResponse(APIResponse<OfferResponseData> response) {
				dismissProgressBar();
				if (response == null || response.body() == null) {
					Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
//					dofinish(PAYMENT_FAILED);
					return;
				}

				if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
					Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
//					dofinish(PAYMENT_FAILED);
					return;
				}
				if (response.body().status.equalsIgnoreCase("SUCCESS")
						&& response.body().code == 216) {
					Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
//					dofinish(PAYMENT_FAILED);
					return;
				}

				if (response.body().status.equalsIgnoreCase("SUCCESS")) {
					if (response.body().ui != null
							&& response.body().ui.action != null) {
						if (!TextUtils.isEmpty(response.body().ui.redirect)) {
							String loadUrl = response.body().ui.redirect;
							setUpWebView(loadUrl);
							return;
						}
						setUpWebView(mUrl);
						return;
					}
				}
//				dofinish(PAYMENT_FAILED);
			}

			@Override
			public void onFailure(Throwable t, int errorCode) {
				LoggerD.debugOTP("Failed: " + t);
				dismissProgressBar();
				if (errorCode == APIRequest.ERR_NO_NETWORK) {
					Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
//					dofinish(PAYMENT_FAILED);
					return;
				}
				Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
//				dofinish(PAYMENT_FAILED);
			}
		});

		APIService.getInstance().execute(contentDetails);
	}

}

