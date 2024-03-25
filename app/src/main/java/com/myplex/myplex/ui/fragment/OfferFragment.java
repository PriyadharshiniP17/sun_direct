package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.OfferResponseData;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.util.StringEscapeUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.utils.MainActivityLauncherUtil;

import java.util.HashMap;
import java.util.Map;

import static com.myplex.myplex.utils.FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE;

public class OfferFragment extends BaseFragment implements SubcriptionEngine.OnOfferSubscription {

    private static final String TAG = OfferFragment.class.getSimpleName();

    public static final String PARAM_PACKAGES_DURING_BROWSE = "packagesDuringBrowse";
    private static final long MILLIS_DELAY_AUTO_SUBSCRIBE = 5 * 1000;
    private Context mContext;

    private ProgressBar mProgressBar;
    private TextView mOfferTitle;
    private TextView mOfferDescription;
    private TextView mOfferSubscribeButton;
    private TextView mAppNoteHint;
    private LinearLayout descLayout;
    private Runnable mRunnableAutoSubscribe = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) {
                return;
            }
            if (mOfferSubscribeButton != null) {
                mOfferSubscribeButton.performClick();
            }

        }
    };

    private boolean isOfferScreenShownDuring;
    private boolean isDuringBrowse;

    public OfferFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offer_description,container,false);
//        Analytics.createScreenGA(Analytics.SCREEN_OFFER);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_OFFER);
        CleverTap.eventPageViewed(CleverTap.PAGE_OFFER);
        mContext = getActivity();

        ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        readBundle();
        mOfferTitle = (TextView) rootView.findViewById(R.id.pack_offer_title);
        mOfferDescription = (TextView) rootView.findViewById(R.id.pack_offer_description);
        mOfferSubscribeButton = (TextView) rootView.findViewById(R.id.pack_offer_subscribe_btn);
        mAppNoteHint = (TextView) rootView.findViewById(R.id.app_note);
        descLayout = (LinearLayout)rootView.findViewById(R.id.descLayout);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.card_loading_progres_bar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mOfferTitle.setVisibility(View.INVISIBLE);
        mOfferDescription.setVisibility(View.INVISIBLE);
        mOfferSubscribeButton.setVisibility(View.INVISIBLE);
        mAppNoteHint.setVisibility(View.INVISIBLE);


        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(false);
        return rootView;
    }

    private void readBundle() {
        Bundle args = getArguments();

        isOfferScreenShownDuring = false;

        if (args != null
                && args.containsKey(PARAM_PACKAGES_DURING_BROWSE)) {
            isOfferScreenShownDuring = args.getBoolean(PARAM_PACKAGES_DURING_BROWSE);
        }
        isDuringBrowse = false;
        if (args != null
                && args.containsKey(PARAM_LOGIN_DURING_BROWSE)) {
            isDuringBrowse = args.getBoolean(PARAM_PACKAGES_DURING_BROWSE);
        }
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }


    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();

        Bundle args = getArguments();
        fetchOfferAvailability();

    }

    private void fetchOfferAvailability() {
        showProgressBar();
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(new APICallback<OfferResponseData>() {

            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
                hideProgressBar();

                if (response == null || response.body() == null) {
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
                    return;
                }
                Map<String, String> params = new HashMap<>();

                params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId() == 0 ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefUserId() + "");
                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    params.put(Analytics.REASON_FAILURE, response.body().message);
                    params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {

                    return;

                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if(response.body().code == 219
                            || response.body().code == 220
                            || (response.body().results != null
                            && response.body().results.size() <= 0)){
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                        params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId()+"");
                        Analytics.mixpanelOfferActivationSuccess(params);
                        launchMainActivity();
                        return;
                    }


                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "Failed: " + t);
                if(errorCode == APIRequest.ERR_NO_NETWORK){
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
                showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
            }
        });

        APIService.getInstance().execute(contentDetails);
    }



    private void showOffer(final CardDataPackages offeredPackage) {

        mOfferTitle.setVisibility(View.VISIBLE);
        mOfferDescription.setVisibility(View.VISIBLE);
        mOfferSubscribeButton.setVisibility(View.VISIBLE);
        if (offeredPackage != null && offeredPackage.packageName != null) {
            mOfferTitle.setText(offeredPackage.bbDescription);
        }
        if(offeredPackage != null && !TextUtils.isEmpty(offeredPackage.actionButtonText)){
            mOfferSubscribeButton.setText(StringEscapeUtils.unescapeJava(offeredPackage.actionButtonText));
        }
        if (offeredPackage != null && offeredPackage.bbDescription != null) {
            mOfferDescription.setText(offeredPackage
                    .cpDescripton);

            String description = offeredPackage.cpDescripton;

            if(description.contains("Special 3G") || description.contains("Special 4G")) {
                Spannable WordtoSpan = new SpannableString(description);
                int startIndex = description.indexOf("Special");
                int endIndex = description.indexOf("G", startIndex);
                if (startIndex > 0 && endIndex > 0) {
                    WordtoSpan.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mOfferDescription.setText(WordtoSpan);
                }
            }
        }

        mOfferSubscribeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SDKUtils.getCardExplorerData().cardDataToSubscribe = new CardData();
                        SubcriptionEngine subscriptionEngine = new SubcriptionEngine(mContext);
                        subscriptionEngine.setOnOfferSubscriptionStatusListener(OfferFragment.this);
                            for (int i = 0; i < offeredPackage.priceDetails.size(); i++) {
                                CardDataPackagePriceDetailsItem packPriceItem = offeredPackage.priceDetails.get(i);
                                if (packPriceItem.name.equalsIgnoreCase("")) {
                                    subscriptionEngine.doSubscription(offeredPackage, i);
                                    return;
                                }
                            }
                        mOfferSubscribeButton.removeCallbacks(mRunnableAutoSubscribe);
                    }
                });
        if (offeredPackage != null && offeredPackage.autoSubscribe) {
            mOfferSubscribeButton.postDelayed(mRunnableAutoSubscribe, MILLIS_DELAY_AUTO_SUBSCRIBE);
        }
        return;

    }

    private void showDeviceAuthenticationFailed(String message) {
        if(message == null){
            message = mContext.getResources().getString(R.string.dev_auth_failed_message);
        }
        AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", mContext.getResources()
                        .getString(R.string.feedbackokbutton),
                new AlertDialogUtil.NeutralDialogListener() {
                    @Override
                    public void onDialogClick(String buttonText) {
                        onBackClicked();
                    }
                });
    }

    private void launchMainActivity() {
        Activity activity = mActivity;
        if (activity == null || activity.isFinishing()) return;
        MainActivityLauncherUtil.initStartUpCalls(activity);
    }


    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }

    @Override
    public void onOfferPurchaseSuccess(String message,boolean show) {
        if(show
                && message != null
                && !message.equals("")){
            AlertDialogUtil.showToastNotification(message);
        }
        Map<String, String> params = new HashMap<>();
        Analytics.mixpanelOfferActivationSuccess(params);
        AppsFlyerTracker.eventOfferActivated(new HashMap<String, Object>());
        if (isOfferScreenShownDuring && isDuringBrowse) {
            getActivity().finish();
            return;
        }
        launchMainActivity();

    }

    @Override
    public void onOfferPurchaseFailed(String errorMessage, int code, boolean show) {
        if(show && errorMessage != null
                && !errorMessage.equals("")){
            AlertDialogUtil.showToastNotification(errorMessage);
        }
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId() == 0 ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefUserId() + "");
        params.put(Analytics.REASON_FAILURE, errorMessage == null ? APIConstants.NOT_AVAILABLE : errorMessage);
        params.put(Analytics.ERROR_CODE, String.valueOf(code));
        Analytics.mixpanelOfferActivationFailed(params);
    }

    public static OfferFragment newInstance(Bundle args) {
        OfferFragment offerFragment = new OfferFragment();
        offerFragment.setArguments(args);
        return offerFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LoggerD.debugLog("OfferFragment: onActivityResult: resultCode- " + resultCode);
        if(resultCode == APIConstants.SUBSCRIPTIONINPROGRESS
                || resultCode == APIConstants.SUBSCRIPTIONSUCCESS){
            launchMainActivity();
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }
}
