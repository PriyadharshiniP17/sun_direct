package com.myplex.subscribe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.ContentDetails;
import com.myplex.api.request.content.SubscriptionRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardResponseData;
import com.myplex.model.MsisdnData;
import com.myplex.sdk.R;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.MsisdnRetrivalEngine;
import com.myplex.util.SDKUtils;
import com.myplex.util.WidevineDrm;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class SubcriptionEngine {
	public static final String TAG = "SubcriptionEngine";
	private Context mContext;
	private ProgressDialog mProgressDialog = null;
	private int mSelectedOption;
	private MsisdnRetrivalEngine mMsisdnRetrivalEngine;
	private CardDataPackagePriceDetailsItem mSelectedPriceItem;
	private CardDataPackages mSelectedPackageItem;
	private String couponCode = "";
	private MsisdnData msisdnData;

    public interface OnOfferSubscription{
        void onOfferPurchaseSuccess(String message,boolean showMessage);
        void onOfferPurchaseFailed(String errorMessage, int code, boolean showMessage);
    }

    public void setOnOfferSubscriptionStatusListener(OnOfferSubscription mOnOfferSubscription) {
        this.mOnOfferSubscription = mOnOfferSubscription;
    }

    private OnOfferSubscription mOnOfferSubscription;

    public static final String OPERATORS_NAME= "sundirect";
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	public SubcriptionEngine(Context context){
		this.mContext = context;
	}
	public void doSubscription(CardDataPackages packageitem,int selectedOption){
		this.mSelectedPackageItem = packageitem;
		mMsisdnRetrivalEngine = new MsisdnRetrivalEngine(mContext);
		this.mSelectedOption = selectedOption;
		if(mSelectedPackageItem == null || mSelectedPackageItem.priceDetails == null || mSelectedPackageItem.priceDetails.size() < mSelectedOption){
//			Util.showToast(mContext, "Error while subscribing", Util.TOAST_TYPE_ERROR);
            AlertDialogUtil.showToastNotification("Error while subscribing");
			return;
		}
		try {
			mSelectedPriceItem = mSelectedPackageItem.priceDetails.get(mSelectedOption);
			Log.e(TAG, "processing payment channel "+mSelectedPriceItem.paymentChannel +" webbased = "+mSelectedPriceItem.webBased);
			if(mSelectedPriceItem.paymentChannel != null && mSelectedPriceItem.paymentChannel.equalsIgnoreCase("OP")){
				if(mSelectedPriceItem.webBased){
					launchOperatorWebBasedSubscription();
				}else{
					if(mSelectedPriceItem.doubleConfirmation){
						showConfirmationDialog();
					}else{
						doOperatorBilling();
					}
				}
//				
			}else if(mSelectedPriceItem.paymentChannel != null && (mSelectedPriceItem.paymentChannel.equalsIgnoreCase("CC")||mSelectedPriceItem.paymentChannel.equalsIgnoreCase("DC")||mSelectedPriceItem.paymentChannel.equalsIgnoreCase("NB")
					|| mSelectedPriceItem.paymentChannel.equalsIgnoreCase("Paytm")
					|| mSelectedPriceItem.paymentChannel.equalsIgnoreCase("PayUmoney"))){
				
				launchWebBasedSubscription();
			}
		} catch (Exception e) {
            AlertDialogUtil.showToastNotification("Error while subscribing");
			Log.e(TAG, e.toString());
		}
	}
	private void showConfirmationDialog(){
		//subscription_confirmationtextview
		String msg = "";
		String contentName = SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.title;
		msg = mContext.getString(R.string.subscriptionconfirmationdialogtext) +" "+ mSelectedPackageItem.contentType + " "
                +contentName + " pack " +
                "for "+ "Rs."
                + mSelectedPriceItem.price;

        AlertDialogUtil.showAlertDialog(mContext, msg, "", mContext.getString(R.string.cancel),
                mContext.getString(R.string.feedbackokbutton),
                new AlertDialogUtil.DialogListener() {
                    @Override
                    public void onDialog1Click() {

                    }

                    @Override
                    public void onDialog2Click() {

                        doOperatorBilling();
                    }
                });
	}
    private boolean isDebug = false;
	private void doOperatorBilling(){
		showProgressBar();
       /* if(isDebug){
            sendSubscriptionRequest(null);
            return;
        }*/
		Log.e(TAG, "doOperatorBilling");
        mMsisdnRetrivalEngine.getMsisdnData(new MsisdnRetrivalEngine.MsisdnRetrivalEngineListener() {

            @Override
            public void onMsisdnData(MsisdnData data) {
                mMsisdnRetrivalEngine.deRegisterCallBacks();
                if (data == null) {
                    AlertDialogUtil.showToastNotification("Subscription failed");
//                    Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
                    dismissProgressBar();
                    return;
                }
                Log.e(TAG, "onMsisdnData msisdn " + data.msisdn + " operator " + data.operator);
                sendSubscriptionRequest(data);
            }
        });
	}
	
	private String lastRequestUrl = null;
	
	private void sendSubscriptionRequest(MsisdnData data){
		Log.e(TAG, "sendSubscriptionRequest");
        /*if(isDebug && data == null){
            data = new MsisdnData();
            data.msisdn = mContext.getResources().getString(R.string.msisdn_hardcoded);
            data.operator = OPERATORS_NAME[1];
        }*/
        SubscriptionRequest.Params subscriptionParams = new SubscriptionRequest.Params
                (SDKUtils.getCardExplorerData().cardDataToSubscribe._id,mSelectedPriceItem.paymentChannel,mSelectedPackageItem
                        .packageId,data.msisdn,SubcriptionEngine.OPERATORS_NAME);
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriptionParams, new
                APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> responseSet) {
                dismissProgressBar();
                if (responseSet == null
                        || responseSet.body() == null) {
                    if (mOnOfferSubscription != null) {
                        mOnOfferSubscription.onOfferPurchaseFailed(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIRequest.ERR_NO_NETWORK, false);
                        return;
                    }
                    AlertDialogUtil.showToastNotification("Subscription failed");
                    return;
                }
                if(responseSet != null && responseSet.body().code >= 200 && responseSet.body().code < 300){
                    Log.e(TAG, "onlineRequestSuccessListener success");
                    if(mOnOfferSubscription != null){
                        mOnOfferSubscription.onOfferPurchaseSuccess(responseSet.body().message,
                                responseSet.body().display);
                        return;
                    }
                    postSubscriptionSuccess();
                }else{
//                    Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
                    if(mOnOfferSubscription != null){
                        mOnOfferSubscription.onOfferPurchaseFailed(responseSet.body().message, responseSet.body().code,
                                responseSet.body().display);
                        return;
                    }
                    AlertDialogUtil.showToastNotification("Subscription failed");
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar();
                if(mOnOfferSubscription != null){
                    if(errorCode == APIRequest.ERR_NO_NETWORK || t instanceof SocketTimeoutException){
                        mOnOfferSubscription.onOfferPurchaseFailed(mContext.getResources()
                                        .getString(R.string.network_error),
                                APIRequest.ERR_NO_NETWORK, true);
                        return;
                    }
                    String reasonForFailure = "Subscription failed";
                    if(t != null){
                        reasonForFailure = t.getMessage();
                    }
                    mOnOfferSubscription.onOfferPurchaseFailed(reasonForFailure, APIRequest.ERR_NO_NETWORK, false);
                    return;
                }
                AlertDialogUtil.showToastNotification("Subscription failed");
            }
        });

        APIService.getInstance().execute(subscriptionRequest);
	}
	//invoked only for operator billing.not for webbased-subscription
	private void postSubscriptionSuccess(){

//		Util.showToast(mContext, "Subscription: Success",Util.TOAST_TYPE_INFO);
        AlertDialogUtil.showToastNotification("Subscription: Success");
//		Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();


        ContentDetails.Params contentDetailsParams = new ContentDetails.Params
                (SDKUtils.getCardExplorerData().cardDataToSubscribe._id,"mdpi",
                        "coverposter",10,APIConstants.HTTP_NO_CACHE);


        final ContentDetails contentDetails = new ContentDetails(contentDetailsParams,
                new APICallback<CardResponseData>() {

                    @Override
                    public void onResponse(APIResponse<CardResponseData> apiresponse) {
                        //Log.d(TAG, "success: " + apiresponse.body());
                        dismissProgressBar();
                        if(null == apiresponse) {
//                            closeSession(response);
                            return;
                        }
                        if(null == apiresponse.body()){
//                            closeSession(response);
                            return;
                        }
                        if(null != apiresponse.body().results
                                && apiresponse.body().results.size() > 0){


                            SDKUtils.getCardExplorerData().cardDataToSubscribe.currentUserData = apiresponse.body().results.get(0)
                                    .currentUserData;
                            List<CardData> dataToSave = new ArrayList<>();
                            dataToSave.add(SDKUtils.getCardExplorerData().cardDataToSubscribe);
//                            Util.showToast(SubscriptionView.this, "Subscription Info updated", Util.TOAST_TYPE_INFO);
                            AlertDialogUtil.showToastNotification("Subscription Info updated");
                        }
                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        dismissProgressBar();
                    }
                });

        APIService.getInstance().execute(contentDetails);
	}
	private void launchWebBasedSubscription(){
		Log.e(TAG, "launchWebBasedSubscription");
		String requestUrl = "";
//		if(couponCode.length()<1)
        String contentId = null;
        if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId != null) {
            contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId;
        } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null && SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
            contentId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
        }
        requestUrl = APIConstants.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel,
                mSelectedPackageItem.packageId, contentId);
        /*else
			requestUrl = APIConstants.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel, mSelectedPackageItem.packageId,cardDataToSubscribe._id);*/

		if(mSelectedPriceItem.paymentChannel != null && mSelectedPriceItem.paymentChannel.equalsIgnoreCase("OP")
				&& msisdnData != null && msisdnData.msisdn != null){
			requestUrl = requestUrl +"&operator="+mSelectedPriceItem.name +"&mobile="+MsisdnRetrivalEngine.format(msisdnData.msisdn);
		}

		requestUrl = requestUrl + APIConstants.AMPERSAND + APIConstants.getDRMDeviceParams();

		Intent i = new Intent(mContext,SubscriptionView.class);
		CardData subscribedData = SDKUtils.getCardExplorerData().cardDataToSubscribe;
		String commercialModel = mSelectedPackageItem.commercialModel; //Rental or Buy
		String ctype = movieOrLivetv(mSelectedPackageItem.contentType); //Movie or LiveTv
		String paymentMode = mSelectedPriceItem.name; //creditcard or debitcard etc
		Bundle b = new Bundle();
		String contentType =  mSelectedPackageItem.contentType;
		b.putString("url", requestUrl);
        if (subscribedData != null
                && subscribedData.generalInfo != null) {
            b.putString("contentname", subscribedData.generalInfo.title);
        }
        b.putString("contentid", contentId);
		b.putDouble("contentprice", mSelectedPriceItem.price);
		b.putString("commercialModel", commercialModel); //Rental or Buy
		b.putString("paymentMode", paymentMode);
		b.putString("contentType", contentType);//SD or HD
        b.putString("duration", mSelectedPackageItem.duration);//SD or HD
		b.putString("ctype", ctype);//LiveTv or Movie
		b.putString("packageId", mSelectedPackageItem.packageId);
        b.putString("packageName", mSelectedPackageItem.displayName);
//		Analytics.mixPanelPaymentOptionsSelected(subscribedData.generalInfo._id, subscribedData.generalInfo.title, paymentMode, mSelectedPriceItem.price+"");
		String cCode = getCouponCode(); //couponCode
		if(cCode != null && cCode.length() > 0  ) {
			b.putString("couponCode", cCode);
			b.putDouble("priceAfterCoupon", APIConstants.priceTobecharged);
		}
		//Analytics.priceTobecharged = 0.0;
		i.putExtras(b);
		//sendMixPanelMessage();
		((Activity) mContext).startActivityForResult(i, APIConstants.SUBSCRIPTIONREQUEST);
	}
	
	public void showProgressBar() {
        if(mContext == null){
            return;
        }
        try{
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            mProgressDialog = ProgressDialog.show(mContext, "", "Loading...",true, false);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
        }catch (Exception e){
            e.printStackTrace();
        }
	}

    public void dismissProgressBar(){
        try {
            if(mContext == null || ((Activity)mContext).isFinishing()){
                return;
            }
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private void launchOperatorWebBasedSubscription() {
        final MsisdnRetrivalEngine msisdnRetrivalEngine = new MsisdnRetrivalEngine(mContext);
        boolean isToShowNetworkAlert = false;
        // check mobile data connection status
        // Skip if mobile data is disabled
        String errorMessage = mContext.getString(com.myplex.sdk.R.string.subscription_operator_data_disable);
        if (!ConnectivityUtil.isConnected(mContext)) {
            isToShowNetworkAlert = true;
            errorMessage = mContext.getString(com.myplex.sdk.R.string.network_error);
            Log.d("DataConnectionCheck",": is not connected to network");
        }else if (!ConnectivityUtil.isConnectedMobile(mContext)) {
            Log.d("DataConnectionCheck","is not connected to mobile data network may be on connected to wifi");
            Log.d("DataConnectionCheck","is not allowed to subscribe on wifi");
            isToShowNetworkAlert = true;
        }

        if(isToShowNetworkAlert){
            AlertDialogUtil.showAlertDialog(mContext, errorMessage,
                    "",
                    mContext.getString(R.string.alert_dataconnection_cancel), mContext.getString(R.string.alert_dataconnection_viewsetttings),
                    new AlertDialogUtil.DialogListener() {

                        @Override
                        public void onDialog1Click() {
//                            dofinish(PAYMENT_FAILED);
                        }

                        @Override
                        public void onDialog2Click() {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            mContext.startActivity(intent);
//                            dofinish(PAYMENT_FAILED);
                        }

                    });
            return;
        }


        msisdnRetrivalEngine.setResumeOldConnection(true);
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyMgr == null) {
            return;
        }
        if (mTelephonyMgr.getSimState() != TelephonyManager.SIM_STATE_READY) {
            return;
        }

        msisdnRetrivalEngine.setUseOnlyMobileData(true);


        showProgressBar();

		/*String operatorName = mTelephonyMgr.getNetworkOperatorName();

        if(TextUtils.isEmpty(operatorName))  return ;
		for (int i = 0; i < OPERATORS_NAME.length; i++) {
			if(operatorName.toLowerCase().contains(OPERATORS_NAME[i])){
				if(i == 0){
					// Operator is Airtel
					msisdnRetrivalEngine.setUrl(APIConstants.AIRTEL_MSISDN_RETRIEVER_URL);
				}if(i==2){
					msisdnRetrivalEngine.setUrl(APIConstants.IDEA_MSISDN_RETRIEVER_URL);
				}
				break;
			}
		}*/
        msisdnRetrivalEngine.getMsisdnData(new MsisdnRetrivalEngine.MsisdnRetrivalEngineListener() {

            @Override
            public void onMsisdnData(MsisdnData data) {
                msisdnRetrivalEngine.deRegisterCallBacks();
                dismissProgressBar();
                if (data == null || TextUtils.isEmpty(data.msisdn)) {

//					Util.showToast(mContext, mContext.getString(R.string.subscription_msisdn_failed), Util.TOAST_TYPE_ERROR);
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.subscription_msisdn_failed));
                    return;
                }

                msisdnData = data;
                launchWebBasedSubscription();
            }
        });
    }
	
	public void doSubscription(final CardDataPackages packageitem,final int selectedOption, boolean drmCheck){
		if(!drmCheck) {
			doSubscription(packageitem,selectedOption);
			return;
		}
		
		CardData subscribedData = SDKUtils.getCardExplorerData().cardDataToSubscribe;
		if (subscribedData != null && subscribedData.generalInfo!= null &&  subscribedData.generalInfo.type != null &&
				!subscribedData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_MOVIE)){
			doSubscription(packageitem,selectedOption);
			return;
		}
		
		WidevineDrm widevineDRM= new WidevineDrm(mContext);
		boolean drmStatus= widevineDRM.isProvisionedDevice();
		
		if(drmStatus){
			doSubscription(packageitem, selectedOption);
			return;
		}
		
		AlertDialogUtil
		.showAlertDialog(
                mContext,
                mContext.getString(R.string.subscription_drm_not_supported_alert),
                ""
                , mContext.getString(R.string.cancel), "ok",
                new AlertDialogUtil.DialogListener() {

                    @Override
                    public void onDialog1Click() {

                    }

                    @Override
                    public void onDialog2Click() {
                        doSubscription(packageitem, selectedOption);
                    }

                });
		
	}

    public String movieOrLivetv(String contentType) {
        String ctype = null;
        if("SD".equalsIgnoreCase(contentType) || "HD".equalsIgnoreCase(contentType) || "movie".equalsIgnoreCase(contentType) ) {
            ctype = "movies";
        }
        else if("Monthly".equalsIgnoreCase(contentType) || "Weekly".equalsIgnoreCase(contentType) ||
                "Yearly".equalsIgnoreCase(contentType) || "live".equalsIgnoreCase(contentType)) {
            ctype = "live tv";
        }
        else if("tvepisode".equalsIgnoreCase(contentType) || "tvseries".equalsIgnoreCase(contentType) || "tvseason".equalsIgnoreCase(contentType)) {
            ctype = "tv shows";
        }else {
            ctype = null;
        }
        return ctype;
    }
}
