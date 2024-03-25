package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.ui.adapter.AdapterPackages;
import com.myplex.myplex.utils.MainActivityLauncherUtil;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.myplex.myplex.utils.FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE;

public class PackagesFragment extends BaseFragment implements SubcriptionEngine.OnOfferSubscription {

    private static final String TAG = PackagesFragment.class.getSimpleName();
    public static final String PARAM_SHOW_PACKAGES_DURING_BROWSE = "showPackagesDuringBrowse";
    public static final String PARAM_SUBSCRIPTION_TYPE = "subscriptionType";
    public static final int PARAM_SUBSCRIPTION_TYPE_PACKAGES = 1;
    public static final int PARAM_SUBSCRIPTION_TYPE_OFFER = 2;
    public static final int PARAM_SUBSCRIPTION_TYPE_NONE = 0;

    private ProgressBar mProgressBar;
    private TextView mOfferSubscribeButton;
    private TextView mAppNoteHint;
    private LinearLayout descLayout;
    private TextView mTextViewSkip;
    private ListView mPackagesListView;
    private AdapterPackages mAdapterMyPacks;
    private ImageView mImageViewOfferSpecialNote;
    private int mSelectedPackagePosition;
    private List<CardDataPackages> mListPackages;
    private boolean isSubscriptionPackagesScreen;
    private TextView mTextViewErrorMsg;
    private boolean isDuringBrowse;

    public PackagesFragment() {
    }

    private Typeface mRegularFontTypeFace;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_packages, container, false);
//        Analytics.createScreenGA(Analytics.SCREEN_OFFER);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_OFFER);
        CleverTap.eventPageViewed(CleverTap.PAGE_OFFER);
        mPackagesListView = (ListView) rootView.findViewById(R.id.packs_list);
        mTextViewSkip = (TextView) rootView.findViewById(R.id.otp_skip_text);
        mPackagesListView.setVisibility(View.GONE);
        mOfferSubscribeButton = (TextView) rootView.findViewById(R.id.pack_offer_subscribe_btn);
        mAppNoteHint = (TextView) rootView.findViewById(R.id.app_note);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.card_loading_progres_bar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext,R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mOfferSubscribeButton.setVisibility(View.INVISIBLE);
        mAppNoteHint.setVisibility(View.INVISIBLE);
        mTextViewSkip.setVisibility(View.INVISIBLE);
        mImageViewOfferSpecialNote = (ImageView)rootView.findViewById(R.id.imgeview_special_offer_note);
        mTextViewErrorMsg = (TextView)rootView.findViewById(R.id.textview_pack_offer_error_msg);
        readBundle();
//        mAppNoteHint.setVisibility(View.INVISIBLE);
        mRegularFontTypeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/amazon_ember_cd_regular.ttf");
        fetchOfferAvailability();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void readBundle() {
        Bundle args = getArguments();
        if(args == null || args.isEmpty()){
            return;
        }
        isSubscriptionPackagesScreen = args.getBoolean(PARAM_SHOW_PACKAGES_DURING_BROWSE,false);
        isDuringBrowse = args.getBoolean(PARAM_LOGIN_DURING_BROWSE,false);
    }

    private void showProgressBar() {
        if(mProgressBar == null){
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
    }


    private void hideProgressBar() {
        if(mProgressBar == null){
            return;
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void fetchOfferAvailability() {
        showProgressBar();
        OfferedPacksRequest.Params params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_3);
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {

            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
//                hideProgressBar();

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
                    showErrorLoading(response.body().message);
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {

                    return;
                }

                if (isSubscriptionPackagesScreen) {
                    showPackages(response.body());
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (response.body().code == 219
                            || response.body().code == 220
                            || (response.body().results != null
                            && response.body().results.size() <= 0)) {
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                        Analytics.mixpanelOfferActivationSuccess(params);
                        launchMainActivity();
                        return;
                    }

                    /*for(CardDataPackages offerPack : response.body().results){
                        for(CardDataPackagePriceDetailsItem packPriceItem : offerPack.priceDetails){
                            if(packPriceItem.name.equalsIgnoreCase("")){
                                showOffer(offerPack);
                                break;
                            }
                        }
                    }*/
                    showPackages(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "Failed: " + t);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
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

    private void showPackages(OfferResponseData offerResponseData) {
        if (offerResponseData == null
                || offerResponseData.results == null
                || offerResponseData.results.isEmpty()) {
            showErrorLoading(null);
            return;
        }
        Analytics.gaCategorySubscription(Analytics.EVENT_ACTION_OFFER,Analytics.EVENT_LABEL_OFFER_SCREEN);
        this.mListPackages = offerResponseData.results;
        if(isOfferPackAvailable(mListPackages)){
            showOffersScreen();
            return;
        }
        filterSubscribedPackages(mListPackages);
        if (offerResponseData.ui == null) {
            mImageViewOfferSpecialNote.setVisibility(View.GONE);
            showUI(mListPackages);
            return;
        }
        if (offerResponseData.ui != null) {
            if (!TextUtils.isEmpty(offerResponseData.ui.terms)) {
                //Log.d(TAG, "offerResponseData.ui.terms: " + offerResponseData.ui.terms);
                mAppNoteHint.setText(offerResponseData.ui.terms);
            }
            if (!TextUtils.isEmpty(offerResponseData.ui.actionButtonText)) {
                //Log.d(TAG, "offerResponseData.ui.promoImage: " + offerResponseData.ui.actionButtonText);
                mOfferSubscribeButton.setText(offerResponseData.ui.actionButtonText);
            }
            if (TextUtils.isEmpty(offerResponseData.ui.promoImage)) {
                mImageViewOfferSpecialNote.setVisibility(View.GONE);
                showUI(mListPackages);
                return;
            }
            if (!TextUtils.isEmpty(offerResponseData.ui.promoImage)) {
                if (!TextUtils.isEmpty(offerResponseData.ui.promoImage)) {
                    //Log.d(TAG, "offerResponseData.ui.promoImage: " + offerResponseData.ui.promoImage);
                    PicassoUtil.with(mContext)
                            .loadWithMemoryPolicy(offerResponseData.ui.promoImage,new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    mImageViewOfferSpecialNote.setVisibility(View.VISIBLE);
                                    mImageViewOfferSpecialNote.setImageBitmap(bitmap);
                                    showUI(mListPackages);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    mImageViewOfferSpecialNote.setVisibility(View.GONE);
                                    showUI(mListPackages);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });
                }
            }

        }

    }

    private void showUI(final List<CardDataPackages> mListPackages) {
        hideProgressBar();
        mTextViewSkip.setVisibility(View.VISIBLE);
        mOfferSubscribeButton.setVisibility(View.VISIBLE);
        mAppNoteHint.setVisibility(View.VISIBLE);
        mTextViewSkip.setPaintFlags(mAppNoteHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mPackagesListView.setVisibility(View.VISIBLE);
        mOfferSubscribeButton.setVisibility(View.VISIBLE);

        mAdapterMyPacks = new AdapterPackages(mContext, mListPackages);
        mPackagesListView.setAdapter(mAdapterMyPacks);
//        if(PrefUtils.getInstance().getPrefEnableSkipOnOTP()){
        mTextViewSkip.setVisibility(View.VISIBLE);
        mTextViewSkip.setTypeface(mRegularFontTypeFace, Typeface.BOLD);
        mTextViewSkip.setOnClickListener(mSkipClickListener);
//        }
        mPackagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedPackagePosition = i;
                mAdapterMyPacks.setSelectedPackagePosition(mSelectedPackagePosition);
                mAdapterMyPacks.notifyDataSetChanged();
            }
        });

        mOfferSubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListPackages == null || mListPackages.isEmpty()) {
                    return;
                }
                CardDataPackages offeredPackage = mListPackages.get(mSelectedPackagePosition);
                SDKUtils.getCardExplorerData().cardDataToSubscribe = new CardData();
                SubcriptionEngine subscriptionEngine = new SubcriptionEngine(mContext);
                subscriptionEngine.setOnOfferSubscriptionStatusListener(PackagesFragment.this);
//                for (CardDataPackages offerPack : mListPackages) {

//                }
            }
        });
    }

    private void showErrorLoading(String message) {
        if (mTextViewErrorMsg != null) {
            if (!TextUtils.isEmpty(message)) {
                mTextViewErrorMsg.setText(message);
            }
            mTextViewErrorMsg.setVisibility(View.VISIBLE);
        }
        hideProgressBar();
    }

    private boolean isOfferPackAvailable(List<CardDataPackages> listPackages) {
        if(listPackages == null || listPackages.isEmpty()){
            return false;
        }
        for (int i =0; i < listPackages.size(); i++) {
            CardDataPackages cardDataPackage = listPackages.get(i);
            if (APIConstants.TYPE_OFFER.equalsIgnoreCase(cardDataPackage.packageType) && !cardDataPackage.subscribed) {
//                            mBaseActivity.pushFragment(OfferFragment.newInstance(null));
                return true;
            }
        }
        return false;
    }

    private void filterSubscribedPackages(List<CardDataPackages> listPackages) {
        if(listPackages == null || listPackages.isEmpty()){
            return;
        }
        for (int i =0; i < listPackages.size(); i++) {
            CardDataPackages cardDataPackage = listPackages.get(i);
            if (cardDataPackage.subscribed) {
                listPackages.remove(cardDataPackage);
            }
        }
    }

    private void showOffersScreen() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(OfferFragment.PARAM_PACKAGES_DURING_BROWSE,isSubscriptionPackagesScreen);
            args.putBoolean(PARAM_LOGIN_DURING_BROWSE,isDuringBrowse);
            mBaseActivity.pushFragment(OfferFragment.newInstance(args));
        }
    }


    private void showDeviceAuthenticationFailed(String message) {
        if (message == null) {
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
    public void onOfferPurchaseSuccess(String message, boolean show) {
        if (show
                && message != null
                && !message.equals("")) {
            AlertDialogUtil.showToastNotification(message);
        }
        Map<String, String> params = new HashMap<>();
        Analytics.mixpanelOfferActivationSuccess(params);
        AppsFlyerTracker.eventOfferActivated(new HashMap<String, Object>());
        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
        launchMainActivity();

    }

    @Override
    public void onOfferPurchaseFailed(String errorMessage, int code, boolean show) {
        if (show && errorMessage != null
                && !errorMessage.equals("")) {
            AlertDialogUtil.showToastNotification(errorMessage);
        }
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId() == 0 ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefUserId() + "");
        params.put(Analytics.REASON_FAILURE, errorMessage == null ? APIConstants.NOT_AVAILABLE : errorMessage);
        params.put(Analytics.ERROR_CODE, String.valueOf(code));
        Analytics.mixpanelOfferActivationFailed(params);
    }

    public static PackagesFragment newInstance(Bundle args) {
        PackagesFragment packagesFragment = new PackagesFragment();
        packagesFragment.setArguments(args);
        return packagesFragment;
    }

    private View.OnClickListener mSkipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Analytics.gaCategorySubscription(Analytics.EVENT_ACTION_OFFER,Analytics.EVENT_LABEL_OFFER_SKIPPED);
            if (isSubscriptionPackagesScreen) {
                getActivity().finish();
                return;
            }
            PrefUtils.getInstance().setPrefIsSkipPackages(true);
            launchMainActivity();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String packageName = APIConstants.NOT_AVAILABLE;
        double price = -1;
        String gaEventAction = packageName + Analytics.HEIFEN_WITH_SPACE_ENCLOSED + price;
        if (data != null){
            Bundle extras = data.getExtras();
            if (extras.containsKey("packageName")) {
                packageName = data.getStringExtra("packageName");
            }
            if (extras.containsKey("contentprice")) {
                price = data.getDoubleExtra("contentprice",-1);
            }
            boolean isSMSFlow = false;
            if (extras.containsKey("isSMS")) {
                isSMSFlow = data.getBooleanExtra("isSMS",false);
            }
            gaEventAction = packageName + Analytics.HEIFEN_WITH_SPACE_ENCLOSED + (price < 0 ? APIConstants.NOT_AVAILABLE : price + "");
            if (extras.containsKey("cgPageLoaded")) {
                if(data.getBooleanExtra("cgPageLoaded", false)){
                    Analytics.gaCategorySubscription(gaEventAction,Analytics.EVENT_LABEL_CG_PAGE);
                    String duration = null;
                    if (extras.containsKey("duration")) {
                        duration = data.getStringExtra("duration");
                    }
                    String paymentModeSelected = null;
                    if (extras.containsKey("paymentMode")) {
                        paymentModeSelected = data.getStringExtra("paymentMode");
                    }
                    CleverTap.eventConsentPageViewed(
                            gaEventAction, paymentModeSelected == null ? "NA" : paymentModeSelected, price + "", duration, isSMSFlow);
                }
            }
        }
        LoggerD.debugLog("PackagesFragment: onActivityResult: resultCode- " + resultCode);
        if(resultCode == APIConstants.SUBSCRIPTIONINPROGRESS
                || resultCode == APIConstants.SUBSCRIPTIONSUCCESS){
            Analytics.gaCategorySubscription(gaEventAction,Analytics.EVENT_LABEL_PAYMENT_SUCCESS);

            if (isSubscriptionPackagesScreen) {
                Activity activity = mActivity;
                if (activity == null || activity.isFinishing()) return;
                activity.finish();
                return;
            }
            PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
            launchMainActivity();
        } else if (resultCode == APIConstants.SUBSCRIPTIONCANCELLED){
//            Analytics.gaCategorySubscription(gaEventAction,Analytics.EVENT_LABEL_PAYMENT_CANCEL);
        } else {
//            Analytics.gaCategorySubscription(gaEventAction,Analytics.EVENT_LABEL_PAYMENT_FAILED);
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
