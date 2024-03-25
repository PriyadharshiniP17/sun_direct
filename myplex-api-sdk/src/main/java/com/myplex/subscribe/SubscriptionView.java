package com.myplex.subscribe;


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

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.BundleRequest;
import com.myplex.api.request.content.ContentDetails;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.sdk.R;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.SDKUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class SubscriptionView extends Activity implements AlertDialogUtil.DialogListener {
	private static final String TAG="SubscriptionView";

	private static final String RESPONSE_ACTION_SHOWMESSAGE = "showMessage";
	private static final java.lang.String PARAM_IS_TO_REDIRECT_TO_HOME = "isToRedirectToHome";
	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;
	private String callbackUrl = new String();
	private ProgressDialog mProgressDialog = null;

	private String url;
	public String status;
	private boolean isProgressDialogCancelable = false;
	
	private static final String IDEA_SUBSCR_CALLBACK_SUCCESS="myplexnow.tv/?status";
	private static final String IDEA_SUBSCR_CALLBACK_CANCEL="consent=No";
	private static final String RESPONSE_ACTION_SHOWMESSAGE_REDIRECT = "showMessage/redirect";
	private static final String RESPONSE_ACTION_REDIRECT = "redirect";
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

	//	http://api.beta.myplex.in/user/v2/billing/callback/evergent/
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mContext = this;
		url = new String();
		try {
			Bundle b = this.getIntent().getExtras();
			url = b.getString("url");
			isProgressDialogCancelable= b.getBoolean("isProgressDialogCancelable", false);

			contentName = b.getString("contentname");
			contentId = b.getString("contentid");
			contentPrice = b.getDouble("contentprice");
			ctype = b.getString("ctype");
			commercialModel = b.getString("commercialModel");
			paymentModel =  b.getString("paymentMode");
			contentType = b.getString("contentType");
			couponCode = b.getString("couponCode");
			priceTobecharged2 = b.getDouble("priceAfterCoupon");
			packageId = b.getString("packageId");
			packageName = b.getString("packageName");
			if(priceTobecharged2 == 0.0){
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

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setContentInsetsAbsolute(0,0);
		mToolbar.setBackgroundColor(getResources().getColor(R.color.app_bkg));
		//setSupportActionBar(mToolbar);
		if (mToolbar != null) {
			mToolbar.setLogo(R.drawable.app_icon);
		}
		mWebView= (WebView)findViewById(R.id.webview);
		try{		
			setUpWebView(url);
		}catch(Exception e){
			e.printStackTrace();
			dofinish(APIConstants.SUBSCRIPTIONERROR);
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
	

	private boolean isCouponApplied() {
		if(couponCode != null && couponCode.length() > 0) return true;
		return false;
	}
	
	private void dofinish(final int response){
		if (response == APIConstants.SUBSCRIPTIONSUCCESS
				|| response == APIConstants.SUBSCRIPTIONINPROGRESS) {
//			Util.showToast(SubscriptionView.this, "Subscription: Success",Util.TOAST_TYPE_INFO);
//			Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();
            String contentId = null;
            if(SDKUtils.getCardExplorerData().cardDataToSubscribe != null
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo != null
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type != null){
                if(SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                        && SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId != null){
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId;
                }else if(SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        && SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                    contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
                }else if(SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
					contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
				}
            }
			fetchSubscriptionPackages(contentId, response);
		}else{
//			Util.showToast(SubscriptionView.this, "Subscription: Cancelled", Util.TOAST_TYPE_ERROR);
            AlertDialogUtil.showToastNotification("Subscription: Cancelled");
//			Toast.makeText(SubscriptionView.this, "Subscription: Cancelled", Toast.LENGTH_SHORT).show();
			closeSession(response);
		}
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
											if (response == APIConstants.SUBSCRIPTIONINPROGRESS && subscribedData
													.currentUserData != null && subscribedData.currentUserData.purchase != null) {
												if (subscribedData.currentUserData.purchase.isEmpty() || subscribedData.currentUserData.purchase.size() == 0) {
													AlertDialogUtil.showNeutralAlertDialog(SubscriptionView.this,
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
											AlertDialogUtil.showToastNotification("Subscription Info updated");
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

	private void closeSession(int response){
		setResult(response,getIntent());
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
		if (url != null
				&& url.contains("")) {
			Bundle b = SubscriptionView.this.getIntent().getExtras();
			b.putBoolean("cgPageLoaded", true);
			getIntent().putExtras(b);
		}
		if(url.contains(callbackUrl) || url.contains(IDEA_SUBSCR_CALLBACK_SUCCESS) ||url.contains(IDEA_SUBSCR_CALLBACK_CANCEL)
				){
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
						closeSession(APIConstants.SUBSCRIPTIONSUCCESS);
					} else if (RESPONSE_ACTION_SHOWMESSAGE.equalsIgnoreCase(action) && !TextUtils.isEmpty(message)) {
						AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", "Ok", new AlertDialogUtil.NeutralDialogListener() {
							@Override
							public void onDialogClick(String buttonText) {
								closeSession(APIConstants.SUBSCRIPTIONSUCCESS);
							}
						});
					} else {
						dofinish(APIConstants.SUBSCRIPTIONSUCCESS);
					}
				}else if(status.equalsIgnoreCase("ERR_IN_PROGRESS")){
					//Log.d(TAG, "error is progress");
					AlertDialogUtil.showAlertDialog(mContext, getResources()
									.getString(R.string.transaction_server_error)
							, ""
							, getResources().getString(R.string.retry)
							, getResources().getString(R.string.cancel)
							, SubscriptionView.this);
				}else if(status.equalsIgnoreCase("ACTIVATION_INPROGRESS")){
					//Log.d(TAG, "error is progress");
					if(RESPONSE_ACTION_REDIRECT.equalsIgnoreCase(action) && !TextUtils.isEmpty(redirectLink)){
						launchBrowser(redirectLink);
						closeSession(APIConstants.SUBSCRIPTIONINPROGRESS);
					}else if (RESPONSE_ACTION_SHOWMESSAGE.equalsIgnoreCase(action) && !TextUtils.isEmpty(message)) {
						AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", "Ok", new AlertDialogUtil.NeutralDialogListener() {
							@Override
							public void onDialogClick(String buttonText) {
								closeSession(APIConstants.SUBSCRIPTIONINPROGRESS);
							}
						});
					} else{
						dofinish(APIConstants.SUBSCRIPTIONERROR);
					}
				} else if (status.equalsIgnoreCase("FAILED")) {
					if (!TextUtils.isEmpty(message)) {
						AlertDialogUtil.showToastNotification(message);
					}
					dofinish(APIConstants.SUBSCRIPTIONERROR);
				} else {
					AlertDialogUtil.showToastNotification("Subscription: " + message);
					dofinish(APIConstants.SUBSCRIPTIONERROR);
				}

			} catch (MalformedURLException e) {
				dofinish(APIConstants.SUBSCRIPTIONERROR);
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
			Log.e(TAG, "ONRECEIVEDERROR "+failingUrl + " " +description);
			closed= true;
			dofinish(APIConstants.SUBSCRIPTIONERROR);
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
		                SubscriptionView.this);
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
		                SubscriptionView.this);
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
					SubscriptionView.this);
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
		
		AlertDialogUtil.showAlertDialog(SubscriptionView.this, getResources().getString(R.string
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
                dofinish(APIConstants.SUBSCRIPTIONCANCELLED);
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
        mWebView.loadUrl(url+"&force=true");
    }

    @Override
    public void onDialog2Click() {
        dofinish(APIConstants.SUBSCRIPTIONERROR);

    }
}

