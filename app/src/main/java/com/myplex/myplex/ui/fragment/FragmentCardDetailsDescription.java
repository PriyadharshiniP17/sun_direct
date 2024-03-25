package com.myplex.myplex.ui.fragment;

import static com.myplex.api.APIConstants.IS_Subcriped;
import static com.myplex.myplex.ApplicationController.getAppContext;
import static com.myplex.myplex.ui.activities.MainActivity.INTENT_RESPONSE_TYPE_EMAIL_UPDATE_SUCCESS;
import static com.myplex.myplex.ui.fragment.PackagesFragment.PARAM_SUBSCRIPTION_TYPE_NONE;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.RequestMySubscribedPacks;
import com.myplex.api.request.user.CityListRequest;
import com.myplex.api.request.user.CountriesListRequest;
import com.myplex.api.request.user.DeviceUnRegRequest;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.SSOLoginRequest;
import com.myplex.api.request.user.StatesListRequest;
import com.myplex.api.request.user.UpdateProfileRequest;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.CountriesData;
import com.myplex.model.CountriesResponse;
import com.myplex.model.MenuDataModel;
import com.myplex.model.MySubscribedPacksResponseData;
import com.myplex.model.OfferResponseData;
import com.myplex.model.PlayerStatusUpdate;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.download.DownloadManagerUtility;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.events.MediaPageVisibilityEvent;
import com.myplex.myplex.events.SubscriptionsDataEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.MandatoryProfileActivity;
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.component.BriefDescriptionComponent;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.CustomLinearLayoutManager;
import com.myplex.myplex.ui.views.MiniCardVideoPlayer;
import com.myplex.myplex.ui.views.StickHeaderItemDecoration;
import com.myplex.myplex.ui.views.SubscriptionPacksDialog;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WeakRunnable;
import com.myplex.myplex.utils.WeakRunnableTwo;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.util.StringManager;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

//import com.hifx.ssolib.Model.SSOcallback;
//import com.hifx.ssolib.SSO;
//import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;

/**
 * Created by Srikanth on 15-Dec-15.
 */
public class FragmentCardDetailsDescription extends BaseFragment implements PlayerStatusUpdate, CardDetailViewFactory.CardDetailViewFactoryListener, RecyclerView.OnItemTouchListener {
    private static final String TAG = FragmentCardDetailsDescription.class.getSimpleName();

    private CardData mCardData;

    private String mId;
    private final CacheManager mCacheManager = new CacheManager();
    private MiniCardVideoPlayer mPlayer;
    private boolean isFirstTimeFromAutoPlay = true;
    private boolean isRequestedPackagesAlready;
    private String mSeasonName;
    private CardData mTVShowData;
    private boolean isLoginRequestThroughDownload;
    private boolean subscriptionFromPlayerScreen;
    private boolean mSoftUpdate;
    private boolean isLoginRequestThroughPlayback = false;
    private String tabName;
    private String mBgColor;
    private RecyclerView mRecyclerView;
    private RecyclerView mRecommendationsList;

    private CardDetailViewAdapter cardDetailViewRecyclerViewAdapter;

    private int packagesLayoutStartPosition = 2;
    private int episodesItemsStartPosition = 2;
    private int seasonsItemsStartPosition= 2;
    private int epgLayoutStartPosition = 4;
    private int briefDescriptionComponentPosition = 0;
    private List<CardData> mListSeasons;
    private List<String> mListSeasonNames;
    private int mSelectedSeasonPosition;
    private int mTVEpisodesListStartIndex = 1;
    private boolean isLoadMoreRequestInProgress;
    private boolean mIsLoadingMoreAvailable;
    private boolean mIsFromViewAll;
    private int mEpgDatePosition;
    private String mAffiliateValue;
    private ListView mSeasonsListView;
    private StickHeaderItemDecoration stickHeaderItemDecoration;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private GestureDetectorCompat gestureListener;
    private ArrayList<CardData> mListEpisodes;
    private VerticalSpaceItemDecoration verticalSpaceItemDecoration;
    private boolean playerLogsItemAdded = false;
    private boolean isToShowPacksScreenOrPopup = false;
    private StringBuilder playerLogs = new StringBuilder();
    public static final int PROFILE_UPDATE_REQUEST = 2001;
    public static final int SUBSCRIPTION_REQUEST = 100;
    public static final String IS_PROFILE_UPDATE_SUCCESS = "profile_update_success";

    private String state="";
    private String country="";
    private String city="";
    private List<CountriesData> countriesList=new ArrayList<>();
    private List<CountriesData> statesList=new ArrayList<>();
    private List<CountriesData> citiesList = new ArrayList<>();
    Spinner countrySpinner,stateSpinner,citySpinner;
    EditText cityEdit,pincodeEt,addressEt;
    Dialog editProfileDialog;
    private String  editUserName,editLastName,editGender,editMobile,editAge,editDob,editEmail;
    private TextView no_data_text;
    private String redirectLatest="";
    public RelativeLayout topContent_layout;
    private String seasonId = "";

    public interface DownloadStatusListener {
        void onSuccess();

        void onDownloadStarted();

        void onDownloadInitialized();

        void onFailure(String message);

        void onDownloadCancelled();

        boolean isToShowDownloadButton();
    }


    public void onDataLoaded(CardData cardData, String contentType, boolean softUpdate) {
        mCardData = cardData;
        playerLogs = new StringBuilder();
        playerLogsItemAdded = false;
        isFirstTimeFromAutoPlay = true;
        mTVEpisodesListStartIndex = 1;
        mSelectedSeasonPosition = 0;
        mEpgDatePosition = 0;
        packagesLayoutStartPosition = 2;
        episodesItemsStartPosition = 2;
        epgLayoutStartPosition = 4;
        briefDescriptionComponentPosition = 1;
        isLoadMoreRequestInProgress = false;
        mIsLoadingMoreAvailable = true;
        mSoftUpdate = softUpdate;
        if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
            mRecyclerView.setBackgroundColor(Color.parseColor(mBgColor));
        } else {
            mRecyclerView.setBackgroundColor(mContext.getResources().getColor(R.color.app_bkg));
        }
        if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
//            Analytics.createScreenGA(Analytics.SCREEN_PROGRAM_DETAILS);
            FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_PROGRAM_DETAILS);
        } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mCardData.generalInfo.type)) {
//            Analytics.createScreenGA(Analytics.SCREEN_MOVIE_DETAILS);
            FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_MOVIE_DETAILS);
        } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(mCardData.generalInfo.type)) {
            if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(contentType)) {
//                Analytics.createScreenGA(Analytics.SCREEN_TV_SHOW_DETAILS);
                FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_TV_SHOW_DETAILS);
            } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(contentType)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(contentType)) {
//                Analytics.createScreenGA(Analytics.SCREEN_VOD_DETAILS);
                FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_VOD_DETAILS);
            }
        } else if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mCardData.generalInfo.type)) {
//            Analytics.createScreenGA(Analytics.SCREEN_MUSIC_VIDEOS_DETAILS);
            FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_MUSIC_VIDEOS_DETAILS);
        }
        loadInterstitalAd();
        loadCarouselInfo();
        initAdapter();
    }

    AdManagerInterstitialAd mAdManagerInterstitialAd;

    public void loadInterstitalAd() {
        if (!Util.isPremiumUser() && PrefUtils.getInstance().isAdEnabled() && PrefUtils.getInstance().getInterstrialAdUnitId() != null) {
            if (PrefUtils.getInstance().getInterstrialAdClicks() == PrefUtils.getInstance().getMaximumInterstrialAdClicksToShow()) {
                AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

                AdManagerInterstitialAd.load(mContext, PrefUtils.getInstance().getInterstrialAdUnitId(), adRequest,
                        new AdManagerInterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                                mAdManagerInterstitialAd = interstitialAd;
                                if (mAdManagerInterstitialAd != null) {
                                    mAdManagerInterstitialAd.show((Activity) mContext);
                                    PrefUtils.getInstance().setInterstrialAdClicks(0);
                                } else {
                                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                                }
                                //Log.i(TAG, "onAdLoaded");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                //Log.i(TAG, loadAdError.getMessage());
                                mAdManagerInterstitialAd = null;
                            }
                        });
            }
        }
    }

    public static FragmentCardDetailsDescription newInstance(Bundle args) {
        FragmentCardDetailsDescription fragmentDetails = new FragmentCardDetailsDescription();
        fragmentDetails.setArguments(args);
        return fragmentDetails;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mScrollView.setVisibility(View.GONE);
            mSeasonsListView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            Log.e("mWidth FRAGMENT DESC", "VISIBILITY GONE");
        } else {
            mSeasonsListView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.post(new WeakRunnable<RecyclerView>(mRecyclerView) {
                @Override
                protected void safeRun(RecyclerView var1) {
                    var1.scrollTo(0, 0);
                }
            });
//            mScrollView.setVisibility(View.VISIBLE);
//            Log.e("mWidth FRAGMENT DESC", "VISIBILITY VISIBLE");
//            if (mScrollView != null) {
//                mScrollView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mScrollView.scrollVerticallyTo(0);
//                    }
//                });
//            }
        }
//        if (mCardDetailViewFactory != null
//                && mCardDetailViewFactory.getTodayEPGPOPUPWindow() != null) {
//            mCardDetailViewFactory.getTodayEPGPOPUPWindow().dismissPopupWindow();
//        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CardDetails.PARAM_CARD_ID, mId);
        outState.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, mCardData);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setRetainInstance(true);
    }

    public void updateButtonText(String buttonText) {
        if (!TextUtils.isEmpty(buttonText)) {
            if (mRecyclerView != null && mRecyclerView.findViewHolderForAdapterPosition(0) != null && mRecyclerView.findViewHolderForAdapterPosition(0) instanceof BriefDescriptionComponent) {
                ((BriefDescriptionComponent) mRecyclerView.findViewHolderForAdapterPosition(0)).changeBuyButtonText(buttonText);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView: isDetached:- " + isDetached());
        LoggerD.debugLog("FragmentCardDetailsDescription onCreateView");
        if (mContext == null) {
            mContext = getActivity();
            mBaseActivity = (BaseActivity) getActivity();
        }
        mBaseActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mBaseActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        // Set the adapter
        gestureListener = new GestureDetectorCompat(mContext, new RecyclerViewOnGestureListener(this));
        View view = inflater.inflate(R.layout.fragment_carddetailview_list, container, false);
        topContent_layout = view.findViewById(R.id.topContent_layout);
        mRecyclerView = view.findViewById(R.id.list);
        mRecommendationsList = view.findViewById(R.id.recommendations_list);
        mSeasonsListView = view.findViewById(R.id.list_seasons_container);
        no_data_text = view.findViewById(R.id.no_data_text);
        // mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        CustomLinearLayoutManager customLayoutManager = new CustomLinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(customLayoutManager);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mContext,RecyclerView.VERTICAL,false);
        mRecommendationsList.setLayoutManager(linearLayoutManager);
        mRecommendationsList.setNestedScrollingEnabled(false);
        VerticalSpaceItemDecoration verticalItemDecoration = new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_20));
        mRecommendationsList.removeItemDecoration(verticalItemDecoration);
        mRecommendationsList.addItemDecoration(verticalItemDecoration);
        mRecommendationsList.setItemAnimator(null);

        topContent_layout.setVisibility(View.VISIBLE);
//        initAdapter();
        return view;
    }

    private List<CarouselInfoData> mListCarouselInfo;
    private void loadCarouselInfo() {
        /*if(mCardData != null && mCardData.isTVEpisode() && !ApplicationController.IS_FROM_CONTINUE_WATCHING) {
            ApplicationController.IS_FROM_CONTINUE_WATCHING = false;
            return;
        }*/
        String type = "";
        if(mCardData != null && (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE) || mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)))
            type = "contentLiveRecommendtions";
        else
        if(mCardData != null && (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TV_SERIES) || mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)))
            type = "showsRecommendations";
        else
            type = PrefUtils.getInstance().getPortraitPlayerSuggestios();
        new MenuDataModel().fetchMenuList(type, 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {

                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    if (mCardData != null && mCardData.content != null && mCardData.content.isSupportCatchup != null && mCardData.content.isSupportCatchup.equalsIgnoreCase("false")) {
                        for(int i=0;i < mListCarouselInfo.size(); i++){
                            if(mListCarouselInfo.get(i).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_CATCHUP)) {
                                mListCarouselInfo.remove(i);
                                 fetchRecommendations(mListCarouselInfo);
                                 return;
                            }
                        }
                    } else {
                        for(int i=0;i < mListCarouselInfo.size(); i++){
                            if(mListCarouselInfo.get(i).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_RECOMMEDED_PROGRAM)) {
                                mListCarouselInfo.remove(i);
                                // fetchRecommendations(mListCarouselInfo);
                                //  return;
                            }
                        }
                    }
                   /* if(mCardData.isTVEpisode()) {
                        for(int i=0;i < mListCarouselInfo.size(); i++){
                            if(i == 0) {
                                mListCarouselInfo.remove(i);
                                // fetchRecommendations(mListCarouselInfo);
                                //  return;
                            }
                        }
                    }*/
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    if (mCardData != null && mCardData.content != null && mCardData.content.isSupportCatchup != null && (mCardData.content.isSupportCatchup.equalsIgnoreCase("false")||mCardData.content.isSupportCatchup.equalsIgnoreCase(""))) {
                        for(int i=0;i < mListCarouselInfo.size(); i++){
                            if(mListCarouselInfo.get(i).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_CATCHUP)) {
                                mListCarouselInfo.remove(i);
                                 fetchRecommendations(mListCarouselInfo);
                                  return;
                            }
                        }
                    } else {
                        for(int i=0;i < mListCarouselInfo.size(); i++){
                            if(mListCarouselInfo.get(i).name.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_RECOMMEDED_PROGRAM)) {
                                mListCarouselInfo.remove(i);
                                // fetchRecommendations(mListCarouselInfo);
                                //  return;
                            }
                        }
                    }
                   /* if(mCardData.isTVEpisode()) {
                        for(int i=0;i < mListCarouselInfo.size(); i++){
                            if(i == 0) {
                                mListCarouselInfo.remove(i);
                                // fetchRecommendations(mListCarouselInfo);
                                //  return;
                            }
                        }
                    }*/
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
                if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                    //showRetryOption(true);
                }
            }

        });

    }
    AdapterCarouselInfo carouselInfoData;
    private void updateCarouselInfo() {
        if (!isAdded()) {
            return;
        }
        mRecommendationsList.setVisibility(View.VISIBLE);
        if(mCardData.isTVSeries() || mCardData.isTVEpisode() ||mCardData.isTVSeason()){
            carouselInfoData = new AdapterCarouselInfo(mContext, mListCarouselInfo, false, mCardData.globalServiceId, seasonId);
            mRecommendationsList.setAdapter(carouselInfoData);
        }else {
            if(mCardData.globalServiceId != null) {
                carouselInfoData = new AdapterCarouselInfo(mContext, mListCarouselInfo, false, mCardData.globalServiceId);
                mRecommendationsList.setAdapter(carouselInfoData);
            } else {
                carouselInfoData = new AdapterCarouselInfo(mContext, mListCarouselInfo, false, mCardData._id);
                mRecommendationsList.setAdapter(carouselInfoData);
            }
        }

    }

    public void notifySeriesListHighlighter(){
        if(carouselInfoData!=null){
            carouselInfoData.notifyDataSetChanged();
        }
    }


    private void fetchRecommendations(List<CarouselInfoData> carouselInfoData) {
        String type = "";
        type = PrefUtils.getInstance().getPortraitPlayerSuggestios();
        new MenuDataModel().fetchMenuList(type, 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {

                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = carouselInfoData;
                if (dataList.size() > 0) {
                    for(int i=0;i < dataList.size(); i++){
                        if(dataList.get(i).name.equalsIgnoreCase("moviesReco")) {
                            mListCarouselInfo.add(dataList.get(i));
                        }
                    }
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = carouselInfoData;
                if (dataList.size() > 0) {
                    for(int i=0;i < dataList.size(); i++){
                        if(dataList.get(i).name.equalsIgnoreCase("moviesReco")) {
                            mListCarouselInfo.add(dataList.get(i));
                        }
                    }
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
                if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                    //showRetryOption(true);
                }
            }

        });


    }
    private void initAdapter() {
        if (mSoftUpdate) {
            mSeasonsListView.setVisibility(View.GONE);
            if (cardDetailViewRecyclerViewAdapter != null
                    && cardDetailViewRecyclerViewAdapter.getData() != null
                    && cardDetailViewRecyclerViewAdapter.getData().get(0) != null) {
                cardDetailViewRecyclerViewAdapter.getData().get(0).cardData = mCardData;
                cardDetailViewRecyclerViewAdapter.notifyItemChanged(0);
            }
            return;
        }
        mIsLoadingMoreAvailable = true;
        mTVEpisodesListStartIndex = 1;
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = new ArrayList<>();
        if (ApplicationController.SHOW_PLAYER_LOGS) {
            playerLogsItemAdded = true;
            episodesItemsStartPosition = episodesItemsStartPosition + 2;
            epgLayoutStartPosition = epgLayoutStartPosition + 2;
            packagesLayoutStartPosition = packagesLayoutStartPosition + 2;
            DetailsViewContent.DetailsViewDataItem titleItem = new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_PLAYER_LOGS_TITLE_VIEW, mCardData, playerLogs.toString(), null, mBgColor);
            DetailsViewContent.DetailsViewDataItem logsItem = new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_PLAYER_LOGS_VIEW, mCardData, playerLogs.toString(), null, mBgColor);
            detailsViewDataItemList.add(titleItem);
            detailsViewDataItemList.add(logsItem);
        }
        detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_BRIEF_DESCRIPTION, mCardData,
                mTVShowData, tabName, getSubscriptionStatus(mCardData), mBgColor, null,
                isToShowWatchLatestEpisodeButton()));
        String title = "People Also Watched";
        String titleVernacular = StringManager.getInstance().getString(APIConstants.PEOPLE_ALSO_WATCHED);
        if (isLiveOrProgram()) {
            title = mContext.getString(R.string.carddetaila_similar_programs_section_title);
            titleVernacular = StringManager.getInstance().getString(APIConstants.CURRENTLY_PLAY_OTHER_CHANNELS);
        }
        if (mCardData.isNewsContent()) {
            title = "Recommendations For You";
        }
        if (TextUtils.isEmpty(titleVernacular)) {
            titleVernacular = "";
        }

       /* if (isRelatedMediaAvailable()) {
            String relatedMediaTitle = "Similar Content";
            if (mCardData != null && mCardData.generalInfo != null && mCardData.generalInfo.type != null &&
                    mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_MOVIE)) {
                relatedMediaTitle = "Trailers and Teasers";
            }
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_TITLE_SECTION_VIEW, mCardData,
                    relatedMediaTitle, titleVernacular, mBgColor));
            int layoutTypeNew = DetailsViewContent.CARDDETAIL_RELATED_MEDIA_VIEW;
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(layoutTypeNew, mCardData, mBgColor,
                    APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM));
        }*/
//        Similar content section
      /*  if (mCardData != null
                && (mCardData.isMovie())) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_TITLE_SECTION_VIEW, mCardData,
                    title, titleVernacular, mBgColor));
            int layoutType = DetailsViewContent.CARDDETAIL_SIMILAR_CONTENT_VIEW_CAROUSEL;
            String type;
            *//*if (mCardData.isMusicVideo()) {
                layoutType = DetailsViewContent.CARDDETAIL_SIMILAR_CONTENT_VIEW_VOD;
            }*//*
            if(mCardData!=null&&(mCardData.isTVSeries()||mCardData.isMovie())){
                type=LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM;
            }else  {
                type=LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM;
            }
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(layoutType, mCardData, mBgColor,
                    type));
        }*/



//        Channel programs section else Show epsiodes
        int headerDisplayPosition = -1;
        /*if (isLiveOrProgram()) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_EPG_DROPDOWN_VIEW, mCardData,
                    getEPGDateTitle(), null, mBgColor));
            headerDisplayPosition = detailsViewDataItemList.size() - 1;
//            for (int i = 0; i < 1; i++) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_EPG_VIEW, CardData.DUMMY_LIST.get(0)));
//            }
            fetchProgramData();
        } else */
        if (mTVShowData != null
                && (mTVShowData.isVODChannel()
                || mTVShowData.isTVSeries()
                || mTVShowData.isTVSeason()
                || mTVShowData.isVODYoutubeChannel()
                || mTVShowData.isVODCategory()
                || mTVShowData.isTVSeason())) {
            detailsViewDataItemList.get(0).cardData = mTVShowData;
            String contentId;
            if (mTVShowData.globalServiceId != null) {
                contentId = mTVShowData.globalServiceId;
            } else {
                contentId = mTVShowData._id;
            }

         /*   if (mTVShowData.isTVSeries()) {
                detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.
                        CARDDETAIL_SEASONS_TABS_VIEW,
                        mCardData, "Loading...", titleVernacular, mBgColor,null));
                //fetchTVSeasons(contentId);
            } else {
                detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_SEASON_DROPDOWN_VIEW,
                        mCardData, "Seasons", titleVernacular, mBgColor,null));
                fetchTVSeasons(contentId);
            }
            for (int i = 0; i < 1; i++) {
                detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_DUMMY_VIEW, CardData.DUMMY_LIST.get(0)));
            }*/
        }

        /*if (mCardData!=null&&!mCardData.isTVSeries()&&!mCardData.isTVEpisode()&&!mCardData.isTVSeason()&& !mCardData.isLive()){
            String recomendedForYouLayoutType;
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_TITLE_SECTION_VIEW, mCardData,
                    "Recommendations For You", titleVernacular, mBgColor));
            if(mCardData!=null&&(mCardData.isTVSeries()||mCardData.isMovie())){
                recomendedForYouLayoutType=LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM;
            }else {
                recomendedForYouLayoutType=LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM;
            }
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_RECOMMENDED_FOR_YOU_VIEW,
                    mCardData, mBgColor,
                    recomendedForYouLayoutType));
        }*/


        String sourceDetails = null;
        String source = null;
        if (mPlayer != null) {
            sourceDetails = mPlayer.getSourceDetails();
            source = mPlayer.getSource();
        }

        if (!isAdded()) return;

        cardDetailViewRecyclerViewAdapter = new CardDetailViewAdapter(mContext, detailsViewDataItemList, this,
                headerDisplayPosition, mTVShowData, mSeasonName, sourceDetails, source, getChildFragmentManager(),mRecyclerView);

        mRecyclerView.setAdapter(cardDetailViewRecyclerViewAdapter);
        if (stickHeaderItemDecoration == null) {
            stickHeaderItemDecoration = new StickHeaderItemDecoration(cardDetailViewRecyclerViewAdapter);
        }
        stickHeaderItemDecoration.setListener(cardDetailViewRecyclerViewAdapter);
        if (verticalSpaceItemDecoration == null) {
            verticalSpaceItemDecoration = new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_2));
        }
        mRecyclerView.removeItemDecoration(stickHeaderItemDecoration);
        mRecyclerView.removeItemDecoration(verticalSpaceItemDecoration);
        mRecyclerView.addItemDecoration(stickHeaderItemDecoration);
        mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                /*if (!isLoadMoreRequestInProgress
                        && mIsLoadingMoreAvailable
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == cardDetailViewRecyclerViewAdapter.getItemCount() - 1) {
                    if (mTVShowData != null
                            && (mTVShowData.isVODChannel()
                            || mTVShowData.isTVSeries()
                            || mTVShowData.isTVSeason()
                            || mTVShowData.isVODYoutubeChannel()
                            || mTVShowData.isVODCategory()
                            || mTVShowData.isTVSeason())) {
                        isLoadMoreRequestInProgress = true;
                        String contentId;
                        if (mTVShowData.globalServiceId != null) {
                            contentId = mTVShowData.globalServiceId;
                        } else {
                            contentId = mTVShowData._id;
                        }
                        if (mTVShowData.isTVSeries()) {
                            fetchTVEpisodes();
                            return;
                        }
                        fetchRelatedVODData(contentId);
                    }
                }*/
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private boolean isToShowWatchLatestEpisodeButton() {
        return !mCardData.isTVEpisode();
    }

    private boolean isRelatedMediaAvailable() {
        /*for (CardDataRelatedMultimediaItem relatedMultimediaItem : mCardData.relatedMultimedia.values) {
                if (relatedMultimediaItem.generalInfo != null
                        && APIConstants.TYPE_TRAILER.equalsIgnoreCase(relatedMultimediaItem.generalInfo.type)) {

                }
            }*/
        return mCardData != null
                && mCardData.relatedMultimedia != null
                && mCardData.relatedMultimedia.values != null
                && !mCardData.relatedMultimedia.values.isEmpty();
    }

    private String getSubscriptionStatus(CardData mData) {
        if (mData != null && mData.generalInfo != null && !mData.generalInfo.isSellable) {
            PrefUtils.getInstance().setSubscriptionStatusString(APIConstants.IS_SELLABLE_FALSE);
            return APIConstants.IS_SELLABLE_FALSE;
        }
        if (mData != null && mData.generalInfo != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)) {
            PrefUtils.getInstance().setSubscriptionStatusString(APIConstants.IS_YOU_TUBE_CONTENT);
            return APIConstants.IS_YOU_TUBE_CONTENT;
        }
        if (!Util.checkUserLoginStatus()) {
            PrefUtils.getInstance().setSubscriptionStatusString(APIConstants.USER_NOT_LOGGED_IN);
            return APIConstants.USER_NOT_LOGGED_IN;
        }
        if (mData != null
                && mData.currentUserData != null
                && mData.currentUserData.purchase != null
                && mData.currentUserData.purchase.size() > 0) {
            PrefUtils.getInstance().setSubscriptionStatusString(APIConstants.USER_ALREADY_SUBSCRIBED);
            return APIConstants.USER_ALREADY_SUBSCRIBED;
        } else if (mData != null
                && mData.currentUserData != null
                && mData.currentUserData.purchase != null) {
            if (mData != null && mData.generalInfo != null && mData.generalInfo.contentRights != null && mData.generalInfo.contentRights.size() > 0 && mData.generalInfo.contentRights.get(0) != null && mData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                PrefUtils.getInstance().setSubscriptionStatusString(APIConstants.USER_NOT_SUBSCRIBED);
            }else{
                PrefUtils.getInstance().setSubscriptionStatusString(APIConstants.USER_ALREADY_SUBSCRIBED);
            }

                return APIConstants.USER_NOT_SUBSCRIBED;
            } else {
                return APIConstants.NOT_AVAILABLE;
            }
        }

    @Override
    public boolean onBackClicked() {
        boolean datesPopupDisplaying = mSeasonsListView.getVisibility() == View.VISIBLE;
        if (datesPopupDisplaying) {
            mSeasonsListView.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    public void onViewChanged(boolean isFullScreen) {
        //Log.d(TAG, "onViewChanged()");
    }

    @Override
    public void onCloseFragment() {
        mListEpisodes = null;
        mListSeasons = null;
        mListSeasonNames = null;
    }

    //time analytics
    @Override
    public void onPause() {
        super.onPause();
        if (mSeasonsListView != null)
            mSeasonsListView.setVisibility(View.GONE);
        //Log.d(TAG, "CardDetails: onPause");

    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "CardDetails: onResume");

        String mId = null;
        String subscribeDataId = null;

        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null) {
            if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && mCardData.globalServiceId != null) {
                mId = mCardData.globalServiceId;
            } else if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    && mCardData._id != null) {
                mId = mCardData._id;
            }
        }
        if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type != null) {
            if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId != null) {
                subscribeDataId = SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId;
            } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                subscribeDataId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
            } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                subscribeDataId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
            }
        }
        if (subscribeDataId != null && mId != null) {
            if (subscribeDataId.equalsIgnoreCase(mId)) {
                mCardData = SDKUtils.getCardExplorerData().cardDataToSubscribe;
                if (!myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW) {
                    fetchPackageData(false);
                }
            }
        }
    }

    private void showHomePopUpPromotion() {
        PrefUtils.getInstance().setPopup(true);
        Dialog dialog = new Dialog(mContext);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.fragment_subscription_dialog);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.black)));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        // lp.y = -30; // Here is the param to set your dialog position. Same with params.x
        dialog.getWindow().setAttributes(lp);
        int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
        int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() ;
        lp.width = displayWidth ;
        lp.height = height;
        window.setAttributes(lp);
        lp.gravity= Gravity.FILL;
        TextView title = (TextView) dialog.findViewById(R.id.title);
        AppCompatButton button_done = (AppCompatButton) dialog.findViewById(R.id.button_done);
        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.onBackClicked();

            }
        });
        Typeface amazonEmberBold = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        title.setTypeface(amazonEmberBold);

       /* SuperEllipseCardView dummy = dialog.findViewById(R.id.sec);

        dummy.setShapeBorderWidth(2.f);
        dummy.setShapeRadius(10.f);
        dummy.setShapeCurveFactor(10.f);
        dummy.setShapeScale(10.f);
        dummy.setShapeForegroundColor(10);
        dummy.setShapeBackgroundColor(10);

        assert dummy.shapeBorderWidth == 2.f;
        assert dummy.shapeRadius == 10.f;
        assert dummy.shapeCurveFactor == 10.f;
        assert dummy.shapeScale == 10.f;*/

        dialog.show();

    }
    @Override
    public void playerStatusUpdate(String value) {

        Log.d(TAG, "playerStatusUpdate(): " + "carddetaildescription"+value);
        if (value == null)
            return;

        if (APIConstants.PLAY_ERR_NON_LOGGED_USER.equalsIgnoreCase(value)) {
            ((MainActivity) mContext).reloadData();
            if(((MainActivity) mContext).isMediaPlaying()){
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.onPause();
            }
            isLoginRequestThroughPlayback = true;
            PrefUtils.getInstance().setPrefLoginStatus("");
            String message = "Already Logged-in for Another Device. Please login here again to get access.";
           /* if(((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mAPIResponseVideosMessage != null)
                message = ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mAPIResponseVideosMessage;*/
            AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", mContext.getResources()
                            .getString(R.string.feedbackokbutton),
                    new AlertDialogUtil.NeutralDialogListener() {
                        @Override
                        public void onDialogClick(String buttonText) {
                            launchLoginActivity();
                            AlertDialogUtil.dismissDialog();
                        }
                    });
            //
        } else if (APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED.equalsIgnoreCase(value) || APIConstants.ERR_PACKAGES_NOT_DEFINED .equalsIgnoreCase(value)) {
            if(mCardData != null && mCardData.generalInfo !=null && mCardData.generalInfo.contentRights != null && mCardData.generalInfo.contentRights.size()>0 && mCardData.generalInfo.contentRights.get(0)!= null) {
                if (mCardData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
//                    showHomePopUpPromotion();
//                fetchOfferAvailability();
                    return;
                }
            }
            if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW) {
              /*  if(mContext != null && ((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null &&
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null &&
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.subscriptionErrorLayout != null &&
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.subscriptionErrorLayout.getVisibility() != View.VISIBLE){
                    return;
                }*/
                if(mContext != null && ((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null){
                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.isSubscriptionError = true;
                }
                if(mContext != null && ((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null &&
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isFullScreen()) {
                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.showSubscriptionError();
                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.resumePreviousOrientaion();
                    APIConstants.IS_SHOWING_SUBSCRIPTION_POPUP=true;
                    return;
                } else
                    fetchOfferAvailability();
            } else {
                fetchPackageData(true);
            }
        } else if (APIConstants.PLAY_ERR_MANDATORY_UPDATE.equalsIgnoreCase(value)) {
            Intent ip = new Intent(mContext, MandatoryProfileActivity.class);
            startActivityForResult(ip, PROFILE_UPDATE_REQUEST);
        }else if(value.equalsIgnoreCase("videos not available")){
            AlertDialogUtil.showNeutralAlertDialog(mContext, value, "", mContext.getResources()
                            .getString(R.string.feedbackokbutton),
                    new AlertDialogUtil.NeutralDialogListener() {
                        @Override
                        public void onDialogClick(String buttonText) {
                            AlertDialogUtil.dismissDialog();
                        }
                    });
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(System.currentTimeMillis());
        value = sdf.format(resultdate) + ":: " + value;
        playerLogs.append(value).append("\n");
        if (ApplicationController.SHOW_PLAYER_LOGS && cardDetailViewRecyclerViewAdapter != null) {
            int playerLogsItemPosition = 1;
            List<DetailsViewContent.DetailsViewDataItem> items = cardDetailViewRecyclerViewAdapter.getData();
            if (playerLogsItemAdded
                    && items != null
                    && items.size() > playerLogsItemPosition) {
                items.get(playerLogsItemPosition).title = playerLogs.toString();
                cardDetailViewRecyclerViewAdapter.notifyItemChanged(playerLogsItemPosition);
                return;
            }
        } else {
            if (playerLogsItemAdded) {
                playerLogsItemAdded = false;
                List<DetailsViewContent.DetailsViewDataItem> items = cardDetailViewRecyclerViewAdapter.getData();
                items.remove(0);
                items.remove(1);
                episodesItemsStartPosition = episodesItemsStartPosition - 2;
                epgLayoutStartPosition = epgLayoutStartPosition - 2;
                packagesLayoutStartPosition = packagesLayoutStartPosition - 2;
                cardDetailViewRecyclerViewAdapter.notifyItemRangeRemoved(0, 2);
            }
        }
    }

    private boolean isLiveOrProgram() {
        if (mCardData == null
                || mCardData.generalInfo == null) {
            return false;
        }
        return (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(mCardData.generalInfo.type));
    }

    private void showPackages(CardData packsData) {
        if (packsData == null || packsData.packages == null) {
            return;
        }

        float price = 10000.99f;
        if (packsData.currentUserData != null && packsData.currentUserData.purchase != null && packsData.currentUserData.purchase.size() != 0) {
            //paid content
        } else {
            for (CardDataPackages packageitem : packsData.packages) {
                if (packageitem.priceDetails != null) {
                    for (CardDataPackagePriceDetailsItem priceDetailItem : packageitem.priceDetails) {
                        if (!priceDetailItem.paymentChannel.equalsIgnoreCase(APIConstants
                                .PAYMENT_CHANNEL_INAPP) && priceDetailItem.price < price) {
                            price = priceDetailItem.price;
                            if (price > 0.0) {
                                List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
                                detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_TITLE_SECTION_VIEW,
                                        mCardData, mContext.getString(R.string.packages), null, mBgColor));
                                if (mCardData.packages != null) {
                                    int packageItemPosition = packagesLayoutStartPosition;
                                    for (CardDataPackages currentPackage : mCardData.packages) {
                                        episodesItemsStartPosition = episodesItemsStartPosition + 1;
                                        epgLayoutStartPosition = epgLayoutStartPosition + 1;
                                        detailsViewDataItemList.add(packageItemPosition, new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_PACKAGES_VIEW, mCardData, currentPackage));
                                        cardDetailViewRecyclerViewAdapter.notifyItemInserted(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
                                        packageItemPosition++;
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }

    }


    private void showPackagesPopup(CardData packsData) {
        if (packsData != null
                && packsData.packages != null
                && packsData.packages.size() > 0) {
            SubscriptionPacksDialog subscriptionPacksDialog = new SubscriptionPacksDialog(mContext);
            subscriptionPacksDialog.showDialog(packsData);
        }
    }

    private void fetchPackageData(final boolean isToRenderPackages) {
        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null
                && mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                && mCardData.globalServiceId != null) {
            mId = mCardData.globalServiceId;
        } else if (mCardData != null
                && mCardData._id != null) {
            mId = mCardData._id;
        }
        mCacheManager.getCardDetails(mId, false, new CacheManager.CacheManagerCallback() {
            @Override
            public void OnCacheResults(List<CardData> dataList) {
                if (null == dataList) {
                    return;
                }
                for (CardData cardData : dataList) {
                    if (null != cardData
                            && null != cardData._id) {
                        if (cardData._id.equalsIgnoreCase(mId)) {
                            SDKUtils.getCardExplorerData().cardDataToSubscribe = mCardData;
                            mCardData.packages = cardData.packages;
                            mCardData.userReviews = cardData.userReviews;
                            mCardData.currentUserData = cardData.currentUserData;
                            if (isLiveOrProgram()) {
                                if (isFirstTimeFromAutoPlay() || !isToRenderPackages) {
                                    isToShowPacksScreenOrPopup = false;
                                    isFirstTimeFromAutoPlay = false;
                                }
                            }
                            if (isToRedirectToPackagesScreen()) {
                                if (isToShowPacksScreenOrPopup) {
                                    isToShowPacksScreenOrPopup = false;
                                    launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                                }
                                return;
                            }
                            showPackages(cardData);
                            if (isToShowPacksScreenOrPopup) {
                                isToShowPacksScreenOrPopup = false;
                                showPackagesPopup(mCardData);
                            }
                        }
                    }
                }
            }

            @Override
            public void OnOnlineResults(List<CardData> dataList) {
                if (null == dataList) {
                    return;
                }
                for (CardData cardData : dataList) {
                    if (null != cardData
                            && null != cardData._id) {
                        if (cardData._id.equalsIgnoreCase(mId)) {
                            SDKUtils.getCardExplorerData().cardDataToSubscribe = mCardData;
                            mCardData.packages = cardData.packages;
                            mCardData.userReviews = cardData.userReviews;
                            mCardData.currentUserData = cardData.currentUserData;
                            if (isLiveOrProgram()) {
                                if (isFirstTimeFromAutoPlay() || !isToRenderPackages) {
                                    isToShowPacksScreenOrPopup = false;
                                    isFirstTimeFromAutoPlay = false;
                                }
                            }

                            if (isToRedirectToPackagesScreen()) {
                                if (isToShowPacksScreenOrPopup) {
                                    isToShowPacksScreenOrPopup = false;
                                    launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                                }
                                return;
                            }
                            showPackages(mCardData);
                            if (isToShowPacksScreenOrPopup) {
                                isToShowPacksScreenOrPopup = false;
                                showPackagesPopup(mCardData);
                            }

                        }
                    }
                }
            }

            @Override
            public void OnOnlineError(Throwable error, int errorCode) {
            }
        });
    }

    private boolean isToRedirectToPackagesScreen() {
        return PrefUtils.getInstance().getPrefShowAllPackagesOfferScreen()
                || ApplicationController.ENABLE_SUBSCRIPTION_FROM_OFFER;
    }

    private void launchPackagesScreen(int subscriptionType) {
        if (mContext == null) {
            return;
        }
        String source = null;
        String sourceDetails = null;
        if (mPlayer != null) {
            source = mPlayer.getSource();
            sourceDetails = mPlayer.getSourceDetails();
        }
        mContext.startActivity(LoginActivity.createIntent(mContext, true, true, subscriptionType, source, sourceDetails));

    }

    public void setPlayer(MiniCardVideoPlayer player) {
        mPlayer = player;
    }

    @Override
    public void onPlayTrailer() {
        if (mPlayer != null) {
            mPlayer.playTrailerContent();
        }
    }

    @Override
    public void onPlayTrailerFromCarousel(int position) {
        if (mPlayer != null) {
            mPlayer.playTrailerContent(position);
        }
    }

    @Override
    public void onBuy() {
        if (mPlayer != null) {
           /* playerStatusUpdate(APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED);
            FirebaseAnalytics.getInstance().onBuyClick(mCardData);*/
            fetchOfferAvailability();
            //  mPlayer.fetchUrl(null,null);
        }
    }

    @Override
    public void onSimilarMoviesDataLoaded(String status) {
    }

    @Override
    public void onSimilarMoviesDataLoaded(String status, int position) {
        if (cardDetailViewRecyclerViewAdapter != null) {
            cardDetailViewRecyclerViewAdapter.removeAt(position);
        }

    }

    @Override
    public void onShowPopup() {
        mSeasonsListView.setVisibility(View.VISIBLE);
        DatesAdapter mPopupListAdapter = new DatesAdapter(mContext, DatesAdapter.AdapterType.SEASONS, mListSeasonNames);
        int selectedPosition = mSelectedSeasonPosition;
        if (mCardData.isProgram() || mCardData.isLive()) {
            mPopupListAdapter = new DatesAdapter(mContext, Util.showNextDates());
            selectedPosition = mEpgDatePosition;
        }
        mPopupListAdapter.setSelectedPosition(selectedPosition);
        mSeasonsListView.setAdapter(mPopupListAdapter);
        mSeasonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSeasonsListView.setVisibility(View.GONE);
                if (mCardData.isProgram() || mCardData.isLive()) {
                    if (mEpgDatePosition == position) return;
                    mEpgDatePosition = position;
                    removeAllItems();
                    addDummyCard();
                    fetchProgramData();
                    updateEpgDateTitleItem();
                    return;
                }
                if (mSelectedSeasonPosition == position) return;
                mSelectedSeasonPosition = position;
                mTVEpisodesListStartIndex = 1;
                isLoadMoreRequestInProgress = false;
                mIsLoadingMoreAvailable = true;
                if (mTVShowData != null
                        && mTVShowData.isTVSeries()) {
                    updateSeasonTitleItem();
                    removeAllItems();
                    addDummyCard();
                    fetchTVSeasons(mTVShowData._id);
                }
            }
        });
    }

    private void addDummyCard() {
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int itemStartPosition = episodesItemsStartPosition;
        int layoutType = DetailsViewContent.CARDDETAIL_EPISODES_VIEW;
        if (mCardData.isLive() || mCardData.isProgram()) {
            itemStartPosition = epgLayoutStartPosition;
            layoutType = DetailsViewContent.CARDDETAIL_EPG_VIEW;
        }
        detailsViewDataItemList.add(itemStartPosition, new DetailsViewContent.DetailsViewDataItem(layoutType, CardData.DUMMY_LIST.get(0)));
    }


    private void addFooterProgressLoadingCard() {
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int layoutType = DetailsViewContent.CARDDETAIL_FOOTER_LOADING_VIEW;
        detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(layoutType, CardData.DUMMY_LIST.get(0)));
        int itemStartPosition = detailsViewDataItemList.size() - 1;
        cardDetailViewRecyclerViewAdapter.notifyItemInserted(itemStartPosition);
    }

    private void removeItem(int position) {
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        detailsViewDataItemList.remove(position);
        cardDetailViewRecyclerViewAdapter.notifyItemRemoved(position);
    }

    private void removeAllItems() {
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int itemStartPosition = episodesItemsStartPosition;
        if (mCardData.isLive() || mCardData.isProgram()) {
            itemStartPosition = epgLayoutStartPosition;
        }
        int size = detailsViewDataItemList.size();
//        for (int i = size; i >= itemStartPosition; i--) {
//            detailsViewDataItemList.remove(i);
//        }
        try {
            if (itemStartPosition < size) {
                detailsViewDataItemList.subList(itemStartPosition, size).clear();
                cardDetailViewRecyclerViewAdapter.notifyItemRangeRemoved(itemStartPosition, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPopupItemSelected(String date) {
        mSeasonName = date;
    }

    public void setAffiliateValue(String affiliateValue) {
        this.mAffiliateValue = affiliateValue;
    }

    public void setEpgDatePosition(int epgDatePosition) {
        this.mEpgDatePosition = epgDatePosition;
    }

    private boolean isFirstTimeFromAutoPlay() {
        return isFirstTimeFromAutoPlay;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SubscriptionWebActivity.SUBSCRIPTION_REQUEST) {
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.onBackClicked();
            String packageName = APIConstants.NOT_AVAILABLE;
            double price = -1;
            boolean isSMSFlow = false;
            String gaEventAction = packageName + Analytics.HEIFEN_WITH_SPACE_ENCLOSED + price;
            if (data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                if (extras.containsKey("packageName")) {
                    packageName = data.getStringExtra("packageName");
                }
                if (extras.containsKey("contentprice")) {
                    price = data.getDoubleExtra("contentprice", -1);
                }
                if (extras.containsKey("isSMS")) {
                    isSMSFlow = data.getBooleanExtra("isSMS", false);
                }
                gaEventAction = packageName + Analytics.HEIFEN_WITH_SPACE_ENCLOSED + (price < 0 ? APIConstants.NOT_AVAILABLE : price + "");
                if (extras.containsKey("cgPageLoaded")) {
                    if (data.getBooleanExtra("cgPageLoaded", false)) {
                        Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_CG_PAGE);
                        String duration = null;
                        if (extras.containsKey("duration")) {
                            duration = data.getStringExtra("duration");
                        }
                        String paymentModeSelected = null;
                        if (extras.containsKey("paymentMode")) {
                            paymentModeSelected = data.getStringExtra("paymentMode");
                        }
                        CleverTap.eventConsentPageViewed(gaEventAction, paymentModeSelected == null ? "NA" : paymentModeSelected, price + "", duration, isSMSFlow);
                    }
                }
            }
            LoggerD.debugLog("PackagesFragment: onActivityResult: resultCode- " + resultCode);
            if (resultCode == APIConstants.SUBSCRIPTIONSUCCESS) {
                Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_PAYMENT_SUCCESS);
                IS_Subcriped = true;
                updateButtonText("Subscribed");
                FirebaseAnalytics.getInstance().logPaymentsEvent(true, PrefUtils.getInstance().getProfileName(), PrefUtils.getInstance().getPrefEmailID(), PrefUtils.getInstance().getPrefMsisdnNo(),
                        (long) price, data.getStringExtra("paymentMode"), null, null, mCardData);
            } else if (resultCode == APIConstants.SUBSCRIPTIONCANCELLED) {
//                Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_PAYMENT_CANCEL);
                FirebaseAnalytics.getInstance().logPaymentCancelledEvent(false, PrefUtils.getInstance().getProfileName(), PrefUtils.getInstance().getPrefEmailID(), PrefUtils.getInstance().getPrefMsisdnNo(),
                        (long) price, data.getStringExtra("paymentMode"), "Payment Failed", null, mCardData);
            } else {
//                Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_PAYMENT_FAILED);
                if (data != null && data.hasExtra("paymentMode")) {
                    FirebaseAnalytics.getInstance().logPaymentsEvent(false, PrefUtils.getInstance().getProfileName(), PrefUtils.getInstance().getPrefEmailID(), PrefUtils.getInstance().getPrefMsisdnNo(),
                            (long) price, data.getStringExtra("paymentMode"), "Payment Failed", null, mCardData);
                } else {
                    FirebaseAnalytics.getInstance().logPaymentsEvent(false, PrefUtils.getInstance().getProfileName(), PrefUtils.getInstance().getPrefEmailID(), PrefUtils.getInstance().getPrefMsisdnNo(),
                            (long) price, "NA", "Payment Failed", null, mCardData);

                }
            }
            if (data != null) {

                String page = data.getStringExtra(APIConstants.NOTIFICATION_PARAM_PAGE);
                if (resultCode == APIConstants.SUBSCRIPTIONSUCCESS
                        || resultCode == APIConstants.SUBSCRIPTIONINPROGRESS) {
                    if (!TextUtils.isEmpty(page)
                            && APIConstants.APP_LAUNCH_NATIVE_OFFER.equalsIgnoreCase(page)) {
                        launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                        return;
                    } else if (!TextUtils.isEmpty(page)
                            && APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION.equalsIgnoreCase(page)) {
                        launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                        return;
                    }

                    if (!PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()) {
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                        EventBus.getDefault().post(new SubscriptionsDataEvent());
                    }
                    if (mPlayer != null && subscriptionFromPlayerScreen) {
                        updateBuyButton();
                        EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
                        //  mPlayer.playContent();
                    }
                }
            }
        }
        if (requestCode == MainActivity.INTENT_REQUEST_TYPE_LOGIN && resultCode == MainActivity.INTENT_RESPONSE_TYPE_SUCCESS) {
            if (isLoginRequestThroughDownload) {
//                if (mCardDetailViewFactory != null) {
//                    mCardDetailViewFactory.startDownload();
//                }
//                TODO Has to start download of the Item which has been clicked
                return;
            }
            if (mPlayer != null
                    && (mPlayer.isLoginRequestThroughPlayback()
                    || isLoginRequestThroughPlayback)) {
                EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
                //  mPlayer.playContent();
            }
        }

        if (resultCode == INTENT_RESPONSE_TYPE_EMAIL_UPDATE_SUCCESS) {
            EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
            if (mPlayer != null) {
                //   mPlayer.playContent();
            }
        }

        if (requestCode == PROFILE_UPDATE_REQUEST) {
            if (data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                if (extras.containsKey(IS_PROFILE_UPDATE_SUCCESS)) {
                    boolean isProfileUpdateSuccess = extras.getBoolean(IS_PROFILE_UPDATE_SUCCESS, true);
                    if (isProfileUpdateSuccess) {
                        PrefUtils.getInstance().setIsToShowForm(false);
                        if (mPlayer != null) {
                            //  mPlayer.playContent();
                        }
                    }
                }
            }
        }

    }
    private void fetchOfferAvailability() {
//        showProgressBar();
        if (isRequestedPackagesAlready) {
            return;
        }
        if (mCardData == null) {
            return;
        }
        subscriptionFromPlayerScreen = false;
        isRequestedPackagesAlready = true;
        if (mCardData != null && mCardData.globalServiceId != null) {
            mId = mCardData.globalServiceId;
        } else if (mCardData != null
                && mCardData._id != null) {
            mId = mCardData._id;
        }
        OfferedPacksRequest.Params params = null;
        if (mAffiliateValue != null) {
            params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.PARAM_CONTENT_DETAIL, mId, mAffiliateValue);
        } else {
            params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.PARAM_CONTENT_DETAIL, mId);
        }
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
                isRequestedPackagesAlready = false;
                if (response == null || response.body() == null) {
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW
                            && response.body().ui != null
                            && response.body().ui.action != null) {
                        switch (response.body().ui.action) {
                            case APIConstants.OFFER_ACTION_SHOW_MESSAGE:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                        @Override
                                        public void onDialogClick(String buttonText) {
                                        }
                                    });
                                }
                                break;
                            case APIConstants.OFFER_ACTION_SHOW_TOAST:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showToastNotification(response.body().message);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_WEB:
                                if (PrefUtils.getInstance().getIsShowUpdateProfile() && (TextUtils.isEmpty(PrefUtils.getInstance().getUserCountry()) || TextUtils.isEmpty(PrefUtils.getInstance().getUserState()) ||
                                        TextUtils.isEmpty(PrefUtils.getInstance().getUSerCity()))) {
                                    editProfileAlertDialog();
                                    return;
                                }
                                redirectLatest = response.body().ui.redirect;
                                if (!TextUtils.isEmpty(response.body().ui.redirect) && isAdded() && getActivity() != null) {
                                    subscriptionFromPlayerScreen = true;
                                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                    APIConstants.IS_SHOWING_SUBSCRIPTION_POPUP=true;
                                    APIConstants.IS_REFRESH_LIVETV = false;
                                    startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), response.body().ui.redirect, SubscriptionWebActivity.PARAM_LAUNCH_NONE,false), SUBSCRIPTION_REQUEST);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_OFFER:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                                break;
                            default:
                                break;
                        }
                        return;
                    }

                    boolean isOfferAvailable = false;
                    for (CardDataPackages offerPack : response.body().results) {
                        if (offerPack.subscribed) {
                            return;
                        }
                        if (APIConstants.TYPE_OFFER.equalsIgnoreCase(offerPack.packageType)) {
                            isOfferAvailable = true;
                            break;
                        }
                    }
                    if (isOfferAvailable) {
                        launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                        return;
                    }
                    launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugOTP("Failed: " + t);
                isRequestedPackagesAlready = false;
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
            }
        });

        APIService.getInstance().execute(contentDetails);
    }


    @Override
    public void onDownloadContent(final DownloadStatusListener downloadStatusListener) {
        if (!Util.checkUserLoginStatus()) {
            if (downloadStatusListener != null) {
                downloadStatusListener.onDownloadStarted();
            }
            isLoginRequestThroughDownload = true;
            launchLoginActivity();
            return;
        }
        DownloadManagerUtility mDownloadManagerUtility = new DownloadManagerUtility(getActivity());
        final ContentDownloadEvent contentDownloadEvent = new ContentDownloadEvent(mCardData, mSeasonName, mTVShowData);
        mDownloadManagerUtility.initializeDownload(contentDownloadEvent, new DownloadStatusListener() {
            @Override
            public void onSuccess() {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (downloadStatusListener != null) {
                            downloadStatusListener.onSuccess();
                        }
                        EventBus.getDefault().post(contentDownloadEvent);
                    }
                });
            }

            @Override
            public void onDownloadStarted() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (downloadStatusListener != null) {
                            downloadStatusListener.onDownloadStarted();
                        }
                    }
                });

            }

            @Override
            public void onDownloadInitialized() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (downloadStatusListener != null) {
                            downloadStatusListener.onDownloadInitialized();
                        }

                    }
                });

            }

            @Override
            public void onFailure(final String message) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (downloadStatusListener != null) {
                            downloadStatusListener.onFailure(message);
                        }
                    }
                });

                if (APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED.equalsIgnoreCase(message)) {
                    if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW) {
                        fetchOfferAvailability();
                        return;
                    }
                    fetchPackageData(true);
                }
            }

            @Override
            public void onDownloadCancelled() {
                if (downloadStatusListener != null) {
                    downloadStatusListener.onDownloadCancelled();
                }
            }

            @Override
            public boolean isToShowDownloadButton() {
                return downloadStatusListener != null && downloadStatusListener.isToShowDownloadButton();
            }
        });
    }

    @Override
    public void onSeasonDataLoaded(List<String> seasonsList) {
        //        TODO initialize season name
//        mSeasonName = seasonsList.get();
    }

    @Override
    public void onEpiosodesLoaded(List<CardData> episodes, boolean isLoadMore) {
        SDKLogger.debug("playNextItem episodes " + episodes);
        if (mPlayer != null) {
            mPlayer.queueListCardData(episodes);
            if (mSoftUpdate) {
                if (episodes != null
                        && !episodes.isEmpty()) {
                    mCardData = episodes.get(0);
                }
            }
        }
    }

    private void updateShowEpisodesLayout() {
        List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
        DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(episodesItemsStartPosition - 1);
        dataItem.isToShowEpisodesLayout = true;
        cardDetailViewRecyclerViewAdapter.notifyItemChanged(episodesItemsStartPosition - 1);
    }

    private void updateLatestEpisodeButtonText(CardData mCardData) {
        if (mCardData != null && mCardData.content != null && mCardData.content.serialNo != null) {
            List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
            DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(briefDescriptionComponentPosition - 1);
            dataItem.latestEpisodeText = mCardData.content.serialNo;
            cardDetailViewRecyclerViewAdapter.notifyItemChanged(briefDescriptionComponentPosition - 1);
        }
    }

    @Override
    public void onSeasonNotAvailable() {
    }

    @Override
    public FragmentManager getSuperChildFragmentManager() {
        return getChildFragmentManager();
    }

    @Override
    public int getEpisodesLayoutStartPosition() {
        return episodesItemsStartPosition;
    }

    @Override
    public void notifyItemChanged(final int adapterPosition) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView.isComputingLayout()) {
                    notifyItemChanged(adapterPosition);
                    return;
                }
                if (cardDetailViewRecyclerViewAdapter != null) {
                    cardDetailViewRecyclerViewAdapter.notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    @Override
    public void onLatestEpisodeButtonClick() {
        List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
        DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(briefDescriptionComponentPosition - 1);
        dataItem.isToShowWatchLatestEpisodeButton = false;
        cardDetailViewRecyclerViewAdapter.notifyItemChanged(briefDescriptionComponentPosition - 1);
        mPlayer.playContent();
        // updateShowEpisodesLayout();
    }

    public void setTVSeasonName(String seasonData) {
        this.mSeasonName = seasonData;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public void setTVShowCardData(CardData TVShowCardData) {
        this.mTVShowData = TVShowCardData;
        if (mPlayer != null)
            mPlayer.setRelatedCardData(mTVShowData);

    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public void setBgColor(String bgColor) {
        this.mBgColor = bgColor;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureListener.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private void launchLoginActivity() {
        if (mContext == null) {
            return;
        }
        String sourceDetails = null;
        String source = null;
        if (mPlayer != null) {
            source = mPlayer.getSource();
            sourceDetails = mPlayer.getSourceDetails();
        }
        ((Activity) mContext).startActivityForResult(LoginActivity.createIntent(mContext, true, false, PARAM_SUBSCRIPTION_TYPE_NONE, source, sourceDetails), MainActivity.INTENT_REQUEST_TYPE_LOGIN);
      /*  SSO.activity().callback(new SSOcallback() {
            @Override
            public void AccessToken(String accessToken, String expiry, String idToken) {
                if (accessToken != null && !TextUtils.isEmpty(accessToken)) {
                    //Toast.makeText(mContext,"access token:: "+accessToken,Toast.LENGTH_LONG).show();
                    makeSSOLoginRequest(accessToken, idToken, expiry);
                } else {
                    Toast.makeText(mContext, "Unable to fetch access token", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void ErrorMessage(String errorMessage) {
                Toast.makeText(mContext, "Error message:: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }).launchfromBackground(mContext);*/
    }

    public void makeSSOLoginRequest(String accessToken, String idToken, String expiry) {
        SSOLoginRequest.Params params = new SSOLoginRequest.Params(idToken, accessToken, expiry);
        SSOLoginRequest googleLogin = new SSOLoginRequest(new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response != null && response.body() != null) {
                    if (response.body().code != 200 && response.body().code != 201) {
                        Toast.makeText(mContext, "Unable to login", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (response.body().code == 200 || response.body().code == 201) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();

                        Toast.makeText(mContext, "Login Successful", Toast.LENGTH_LONG).show();

                        if (!TextUtils.isEmpty(response.body().email)) {
                            PrefUtils.getInstance().setPrefEmailID(response.body().email);
                        }
                        PrefUtils.getInstance().setPrefLoginStatus("success");

                        try {
                            LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                            PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                            Util.setUserIdInMyPlexEvents(mContext);
                           /* if(!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }*/
                            Analytics.mixpanelIdentify();
                            if (!TextUtils.isEmpty(response.body().email)) {
                                Analytics.setMixPanelEmail(response.body().email);
                            }

                            ComScoreAnalytics.getInstance().setEventLogin("NA", response.body().email, response.body().message, response.body().status);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (mPlayer != null
                                && (mPlayer.isLoginRequestThroughPlayback()
                                || isLoginRequestThroughPlayback)) {
                            EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
                            makeUserProfileRequest();
                        }
                    } else if (response.body().code == 401) {
                        if (response.body().message != null) {
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                        }
                    } else if (response.body().code == 423) {
                        if (response.body().message != null)
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 500) {
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 400) {
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_SHORT).show();
                    } else if (response.body().code == 403) {
                        DeviceUnRegRequest deviceUnregister = new DeviceUnRegRequest(new APICallback<BaseResponseData>() {
                            @Override
                            public void onResponse(APIResponse<BaseResponseData> response) {
                                if (response != null && response.body() != null) {
                                    if (response.body().code == 200) {
                                        makeSSOLoginRequest(accessToken, idToken, expiry);
                                    } else {

                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t, int errorCode) {

                            }
                        });
                        APIService.getInstance().execute(deviceUnregister);
                    } else {
                        Toast.makeText(mContext, "Unable to Login", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(mContext, "Unable to Login", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        }, params);
        APIService.getInstance().execute(googleLogin);
    }

    private void fetchMyPackages() {
        RequestMySubscribedPacks mRequestRequestContentList = new RequestMySubscribedPacks(new APICallback<MySubscribedPacksResponseData>() {
            @Override
            public void onResponse(APIResponse<MySubscribedPacksResponseData> response) {
                if (response == null || response.body() == null || response.body().results == null) {
                    ApplicationController.ENABLE_RUPEE_SYMBOL = false;
                    ApplicationController.clearPackagesList();
                    if (mPlayer != null
                            && (mPlayer.isLoginRequestThroughPlayback()
                            || isLoginRequestThroughPlayback)) {
                        EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
                        //  mPlayer.playContent();
                    }
                    return;
                }
                ApplicationController.ENABLE_RUPEE_SYMBOL = true;
                ApplicationController.setSubscribedPackages(response.body().results);
                if (mPlayer != null
                        && (mPlayer.isLoginRequestThroughPlayback()
                        || isLoginRequestThroughPlayback)) {
                    EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
                    //  mPlayer.playContent();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                ApplicationController.ENABLE_RUPEE_SYMBOL = false;
            }
        });
        // APIService.getInstance().execute(mRequestRequestContentList);
    }

    public void makeUserProfileRequest() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();

                    if (responseData.result.profile.first != null && !TextUtils.isEmpty(responseData.result.profile.first)) {
                        if (responseData.result.profile.last != null && !TextUtils.isEmpty(responseData.result.profile.last)) {
                            PrefUtils.getInstance().setPrefFullName(responseData.result.profile.first + " " + responseData.result.profile.last);
                        } else {
                            PrefUtils.getInstance().setPrefFullName(responseData.result.profile.first);
                        }
                    }

                    if (responseData.result.profile.dob !=null&&!TextUtils.isEmpty(responseData.result.profile.dob)){
                        PrefUtils.getInstance().setUserDOB(responseData.result.profile.dob);
                    }

                    if (responseData.result.profile.gender != null && !TextUtils.isEmpty(responseData.result.profile.gender)) {
                        PrefUtils.getInstance().setUserGender(responseData.result.profile.gender);
                    }

                    if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                        if (responseData.result.profile.locations.get(0) != null
                                && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                            PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                    }

                    if (responseData.result.profile.state !=null&&!TextUtils.isEmpty(responseData.result.profile.state)){
                        PrefUtils.getInstance().setUserState(responseData.result.profile.state);
                    }

                    if (responseData.result.profile.city !=null&&!TextUtils.isEmpty(responseData.result.profile.city)){
                        PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
                    }

                    if(responseData.result!=null&&responseData.result.profile!=null&&
                            responseData.result.profile.mobile_no!=null){
                        FirebaseAnalytics.getInstance().setMobileNumberProperty(responseData.result.profile.mobile_no);
                    }

                    if(responseData.result!=null&&responseData.result.profile!=null&&
                            responseData.result.profile.emails!=null && responseData.result.profile.emails.size() >0 &&!responseData.result.profile.emails.isEmpty()&&
                            responseData.result.profile.emails.get(0).email!=null){
                        FirebaseAnalytics.getInstance().setEmailProperty(responseData.result.profile.emails.get(0).email);
                    }

                    if (responseData.result!=null&&responseData.result.profile!=null&&responseData.result.profile.showForm) {
                        PrefUtils.getInstance().setIsToShowForm(true);
                        Intent ip = new Intent(mContext, MandatoryProfileActivity.class);
                        startActivityForResult(ip, PROFILE_UPDATE_REQUEST);
                    } else {
                        PrefUtils.getInstance().setIsToShowForm(false);
                        fetchMyPackages();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }


    public void onEventMainThread(ContentDownloadEvent event) {
        LoggerD.debugDownload("download event- " + event);
        if (event == null || event.cardData == null || cardDetailViewRecyclerViewAdapter == null)
            return;
        List<DetailsViewContent.DetailsViewDataItem> dataItems = cardDetailViewRecyclerViewAdapter.getData();
        for (int i = 0; i < dataItems.size(); i++) {
            DetailsViewContent.DetailsViewDataItem dataItem = dataItems.get(i);
            if (dataItem == null || dataItem.cardData == null || dataItem.cardData._id == null || dataItem.layoutType == DetailsViewContent.CARDDETAIL_BRIEF_DESCRIPTION)
                continue;
            if (dataItem.cardData._id.equalsIgnoreCase(event.cardData._id)) {
                if (cardDetailViewRecyclerViewAdapter != null) {
                    cardDetailViewRecyclerViewAdapter.notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        private final WeakReference<FragmentCardDetailsDescription> fragmentCardDetailsDescription;

        RecyclerViewOnGestureListener(FragmentCardDetailsDescription fragmentCardDetailsDescription) {
            this.fragmentCardDetailsDescription = new WeakReference<>(fragmentCardDetailsDescription);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (fragmentCardDetailsDescription.get() == null) {
                return super.onSingleTapConfirmed(e);
            }
            FragmentCardDetailsDescription fragmentCardDetailsDescription1 = fragmentCardDetailsDescription.get();
            if (fragmentCardDetailsDescription1.stickHeaderItemDecoration == null) {
                return super.onSingleTapConfirmed(e);
            }
            float touchY = e.getY();
            int headerHeight = fragmentCardDetailsDescription1.stickHeaderItemDecoration.getStickyHeaderHeight();
            if (fragmentCardDetailsDescription1.mRecyclerView == null
                    || fragmentCardDetailsDescription1.mRecyclerView.getLayoutManager() == null) {
                return super.onSingleTapConfirmed(e);
            }
            View view = mRecyclerView.findChildViewUnder(0, headerHeight);
            if (view == null
                    || view.getLayoutParams() == null) {
                return super.onSingleTapConfirmed(e);
            }
            // handle single tap
            //mGroupHeaderHeight is the height of the header which is used to determine whether the click is inside of the view, hopefully it's a fixed size it would make things easier here
            if (touchY < headerHeight) {
                int itemAdapterPosition = fragmentCardDetailsDescription1.mRecyclerView.getLayoutManager().getPosition(view);
                int headerPosition = episodesItemsStartPosition - 1;
                if (mCardData.isProgram() || mCardData.isLive()) {
                    headerPosition = epgLayoutStartPosition - 1;
                }
                if (itemAdapterPosition >= headerPosition) {
                    fragmentCardDetailsDescription.get().onShowPopup();
                    //Do stuff here no you have the position of the item that's been clicked
                    return true;
                }
            }
            return super.onSingleTapConfirmed(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            if (fragmentCardDetailsDescription.get() == null) {
                return super.onDown(e);
            }
            if (fragmentCardDetailsDescription.get().stickHeaderItemDecoration == null) {
                return super.onDown(e);
            }
            float touchY = e.getY();

            if (touchY < fragmentCardDetailsDescription.get().stickHeaderItemDecoration.getStickyHeaderHeight()) {
                return true;
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (fragmentCardDetailsDescription.get() == null) {
                return super.onSingleTapUp(e);
            }
            if (fragmentCardDetailsDescription.get().stickHeaderItemDecoration == null) {
                return super.onSingleTapUp(e);
            }
            float touchY = e.getY();

            if (touchY < fragmentCardDetailsDescription.get().stickHeaderItemDecoration.getStickyHeaderHeight()) {
                return true;
            }
            return super.onSingleTapUp(e);
        }

//        public void onLongPress(MotionEvent e) {
//            if (fragmentCardDetailsDescription == null || fragmentCardDetailsDescription.get() == null) {
//                super.onLongPress(e);
//                return;
//            }
//            if (fragmentCardDetailsDescription.get().stickHeaderItemDecoration == null) {
//                super.onLongPress(e);
//                return;
//            }
//            RecyclerView mRecyclerView = fragmentCardDetailsDescription.get().mRecyclerView;
//            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
//            int position = mRecyclerView.getChildPosition(view);
//
//            // handle long press
//
//            super.onLongPress(e);
//        }
    }


    private void fetchProgramData() {
        new EPGAPICallTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class EPGAPICallTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.Fr
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final WeakReference<FragmentCardDetailsDescription> fragmentEpisodesWeakReference;

        EPGAPICallTask(FragmentCardDetailsDescription fragmentCardDetailsDescription) {
            this.fragmentEpisodesWeakReference = new WeakReference<>(fragmentCardDetailsDescription);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null)
                return null;
            FragmentCardDetailsDescription fragmentCardDetails = fragmentEpisodesWeakReference.get();
            String cId;
            Date selectedDate = Util.getCurrentDate(fragmentCardDetails.mEpgDatePosition);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String selectedDateInString = format.format(selectedDate);
            String dateStamp = Util.getYYYYMMDD(selectedDateInString);
            if (dateStamp == null) {
                Date currentDate = new Date();
                dateStamp = Util.getYYYYMMDD(currentDate);
            }
            cId = fragmentCardDetails.mCardData._id;
            if (fragmentCardDetails.mCardData.isProgram()) {
                cId = fragmentCardDetails.mCardData.globalServiceId;
            }
            fragmentCardDetails.mCacheManager.getEPGChannelData(cId, dateStamp, false, new CacheManager.CacheManagerCallback() {
                @Override
                public void OnCacheResults(List<CardData> dataList) {
                    if (fragmentEpisodesWeakReference.get() == null)
                        return;
                    FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                    fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                        @Override
                        protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                            fragmentCardDetailsDescription1.skipCompletedProgramsAndApply(var1, false);
                        }
                    });
                }

                @Override
                public void OnOnlineResults(List<CardData> dataList) {
                    if (fragmentEpisodesWeakReference.get() == null)
                        return;
                    FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                    fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                        @Override
                        protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                            fragmentCardDetailsDescription1.skipCompletedProgramsAndApply(var1, false);
                        }
                    });
                }

                @Override
                public void OnOnlineError(Throwable error, int errorCode) {
                    //TODO Add EPG error message
                    if (fragmentEpisodesWeakReference.get() == null)
                        return;
                    FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                    fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, null) {
                        @Override
                        protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                            fragmentCardDetailsDescription1.skipCompletedProgramsAndApply(var1, false);
                        }
                    });
                }
            });
            return null;
        }
    }


    private void skipCompletedProgramsAndApply(final List<CardData> channelData, final boolean reset) {
        if (channelData == null ||
                channelData.isEmpty()) {
            updateEPGData(channelData, reset);
            return;
        }
        if (mEpgDatePosition - PrefUtils.getInstance().getPrefNoOfPastEpgDays() >= 0
                || !PrefUtils.getInstance().getPrefEnablePastEpg()) {
            for (Iterator<CardData> it = channelData.iterator(); it.hasNext(); ) {
                CardData cardData = it.next();
                if (null != cardData.endDate
                        && null != cardData.startDate) {
                    Date endDate = Util.getDate(cardData.endDate);
                    Date currentDate = new Date();
                    Date startDate = Util.getDate(cardData.startDate);
                    if (currentDate.after(endDate)) {
                        if (!(currentDate.after(startDate)
                                && currentDate.before(endDate))) {
                            it.remove();
                        }
                    }
                }
            }
        }
        if (channelData == null ||
                channelData.isEmpty()) {
            updateEPGData(channelData, reset);
            return;
        }
        CardData firstProgram = channelData.get(0);
        if (mEpgDatePosition - PrefUtils.getInstance().getPrefNoOfPastEpgDays() >= 0 || !PrefUtils.getInstance().getPrefEnablePastEpg()) {
            if (firstProgram != null && firstProgram.getEndDate() != null) {
                final long programEndDurationInMs = firstProgram.getEndDate().getTime() - new Date().getTime();
                //Log.d(TAG, "handler will update the ui after programEndDurationInMs: " + programEndDurationInMs + "from now");
                if (programEndDurationInMs > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (channelData.size() > 1) {
                                Message message = new Message();
                                message.obj = channelData;
                                mHandlerUIUpdate.sendMessageDelayed(message, programEndDurationInMs);
                            }
                        }
                    }).start();
                }
            }
        }
        if (mCardData == null) return;
        if (mCardData.isLive() || mCardData.isProgram()) {
            mHandler.post(new WeakRunnable<List<CardData>>(channelData) {
                @Override
                protected void safeRun(List<CardData> var1) {
                    updateEPGData(channelData, reset);
                }
            });
        }
    }

    private void updateEPGData(List<CardData> channelData, boolean reset) {
        if (channelData == null) {
            cardDetailViewRecyclerViewAdapter.removeEPGTitle();
            //showErrorMessageView();
            return;
        }
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int lastItem = cardDetailViewRecyclerViewAdapter.getItemCount() - 1;
        if (!reset) {
            removeItem(lastItem);
        } else {
            removeAllItems();
        }
        for (CardData cardData : channelData) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_EPG_VIEW, cardData));
            cardDetailViewRecyclerViewAdapter.notifyItemInserted(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
        }
    }

    private final Handler mHandlerUIUpdate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d(TAG, "mHandlerUIUpdate handleMessage: updating UI");
            if (msg != null && msg.obj != null) {
                Object msgObj = msg.obj;
                if (msgObj instanceof ArrayList) {
                    List<CardData> list = (List<CardData>) msgObj;
                    skipCompletedProgramsAndApply(list, true);
                }
            }
        }
    };


    private void fetchTVSeasons(String contentId) {
        new SeasonsAPICallTask(this, contentId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class SeasonsAPICallTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.Fr
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final WeakReference<FragmentCardDetailsDescription> fragmentEpisodesWeakReference;
        private final WeakReference<String> contentIdRef;

        SeasonsAPICallTask(FragmentCardDetailsDescription fragmentEpisodesWeakReference, String contentId) {
            this.fragmentEpisodesWeakReference = new WeakReference<>(fragmentEpisodesWeakReference);
            this.contentIdRef = new WeakReference<>(contentId);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null) return null;

            if (contentIdRef.get() == null) return null;
            fragmentEpisodesWeakReference.get().mCacheManager.getRelatedVODListTypeExclusion(contentIdRef.get(), 1, true, APIConstants.TYPE_TVSEASON,
                    15,
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.mListSeasons = var1;
                                    fragmentCardDetailsDescription1.updateSeasons(var1);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.mListSeasons = var1;
                                    fragmentCardDetailsDescription1.updateSeasons(var1);
                                }
                            });

                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            if (fragmentEpisodesWeakReference.get() == null) return;
                            //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                fragmentEpisodesWeakReference.get().mHandler.post(new WeakRunnable<FragmentCardDetailsDescription>(fragmentEpisodesWeakReference.get()) {
                                    @Override
                                    protected void safeRun(FragmentCardDetailsDescription var1) {
                                        AlertDialogUtil.showToastNotification(var1.mContext.getString(R.string.network_error));
                                    }
                                });
                            }
//                        showNoDataMessage();
                        }
                    });

            return null;
        }
    }

    private void updateSeasons(List<CardData> dataList) {
        if (dataList == null
                || dataList.isEmpty()) {
            showErrorMessageView();
            return;
        }

        /*if (dataList.get(0).generalInfo.showDisplayTabs != null
                &&dataList.get(0).generalInfo.showDisplayTabs.showDisplayType!=null
                &&dataList.get(0).generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) {

            return;
        }*/

        updateEpisodeTabsItem(dataList);

//        dataList = sortDataList(dataList);
        /*mListSeasonNames = prepareSeasonNames(dataList);
        if (mListSeasonNames == null || mListSeasonNames.isEmpty()) {
            SDKLogger.debug("Unable to load season names possible cause might be serialNo from server is null in prepareSeasonName() -> CardData.get(index).content.serialNo");
            return;
        }
        updateSeasonTitleItem();
        fetchTVEpisodes();*/
    }

    private void updateEpisodeTabsItem(List<CardData> seasonList){
        List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
        DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(episodesItemsStartPosition - 1);
        isLoadMoreRequestInProgress=true;
        mIsLoadingMoreAvailable=false;
        dataItem.seasonsList=seasonList;
        cardDetailViewRecyclerViewAdapter.notifyItemChanged(episodesItemsStartPosition - 1);
    }

    private void updateSeasonTitleItem() {
        List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
        DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(episodesItemsStartPosition - 1);
        dataItem.layoutType=DetailsViewContent.CARDDETAIL_SEASON_DROPDOWN_VIEW;
        dataItem.title = mListSeasonNames.get(mSelectedSeasonPosition);
        /*cardDetailViewRecyclerViewAdapter.updateHeaderDisplayPosition(detailViewData.size()-1);*/
        cardDetailViewRecyclerViewAdapter.notifyItemChanged(episodesItemsStartPosition - 1);
    }

    private void updateEpgDateTitleItem() {
        List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
        DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(epgLayoutStartPosition - 1);
        dataItem.title = getEPGDateTitle();
        cardDetailViewRecyclerViewAdapter.notifyItemChanged(epgLayoutStartPosition - 1);
    }

    private void updateBuyButton() {
        List<DetailsViewContent.DetailsViewDataItem> detailViewData = cardDetailViewRecyclerViewAdapter.getData();
        DetailsViewContent.DetailsViewDataItem dataItem = detailViewData.get(briefDescriptionComponentPosition - 1);
//        dataItem.isSubscribed = APIConstants.USER_ALREADY_SUBSCRIBED;
        cardDetailViewRecyclerViewAdapter.notifyItemChanged(briefDescriptionComponentPosition - 1);
    }

    private String getEPGDateTitle() {
        ArrayList<String> nxtDateList = Util.showNextDates();
        return nxtDateList.get(mEpgDatePosition);
    }

   /* private List<String> prepareSeasonNames(List<CardData> dataList) {
        List<String> seasons = new ArrayList<>();
        String seasonText = "Season ";
        for (Iterator<CardData> iterator = dataList.iterator(); iterator.hasNext(); ) {
            CardData seasonData = iterator.next();
            if (seasonData != null && seasonData.content != null
                    && seasonData.content.serialNo != null) {
                seasons.add(seasonText + seasonData.content.serialNo);
            }
        }
        return seasons;
    }

    private void fetchTVEpisodes() {
        LoggerD.debugLog("FragmentEpisodes:: fetching episodes");
        if (mListSeasons == null || mListSeasons.isEmpty()) return;
        if (mSelectedSeasonPosition < mListSeasons.size()) {
            CardData seasonData = mListSeasons.get(mSelectedSeasonPosition);
            if (seasonData == null) return;
            if (isLoadMoreRequestInProgress)
                addFooterProgressLoadingCard();
            new EpisodesAPICallTask(this, seasonData._id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
*/
   /* private static class EpisodesAPICallTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<FragmentCardDetailsDescription> fragmentEpisodesWeakReference;
        private final WeakReference<String> contentIdReference;

        EpisodesAPICallTask(FragmentCardDetailsDescription fragmentEpisodesWeakReference, String contentId) {
            this.fragmentEpisodesWeakReference = new WeakReference<>(fragmentEpisodesWeakReference);
            this.contentIdReference = new WeakReference<>(contentId);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null)
                return null;
            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
            if (contentIdReference.get() == null) {
                return null;
            }
            //Update selected season text on drop down header
//            fragmentCardDetailsDescription.updateDropDownTitle();
//            fragmentCardDetailsDescription.showProgressBar();
            int startIndex = 1;
            if (fragmentCardDetailsDescription.isLoadMoreRequestInProgress)
                startIndex = fragmentCardDetailsDescription.mTVEpisodesListStartIndex + 1;
            fragmentCardDetailsDescription.mCacheManager.getRelatedVODList(contentIdReference.get(), startIndex, true,
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(final List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateEpisodesData(var1);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateEpisodesData(var1);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, null) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateEpisodesData(var1);
                                }
                            });
                        }
                    });
            return null;
        }

    }

    *//**
     * If data is not available for the seasons then replace the episodes view item with error message item.
     *//*
    private void updateEpisodesData(List<CardData> cardDataList) {
        if (cardDataList == null) {
            if (isLoadMoreRequestInProgress) {
                mIsLoadingMoreAvailable = false;
                if (cardDetailViewRecyclerViewAdapter != null
                        && cardDetailViewRecyclerViewAdapter.getData() != null) {
                    removeItem(cardDetailViewRecyclerViewAdapter.getData().size() - 1);
                }
                return;
            }
            showErrorMessageView();
            return;
        }
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int lastItem = cardDetailViewRecyclerViewAdapter.getItemCount() - 1;
        removeItem(lastItem);
//                                    if (isLoadMoreRequestInProgress) {
//                                        isLoadMoreRequestInProgress = false;
        for (int i = 0; i < cardDataList.size(); i++) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_EPISODES_VIEW, cardDataList.get(i)));
            cardDetailViewRecyclerViewAdapter.notifyItemInserted(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
        }
        mIsLoadingMoreAvailable = true;
        if (isLoadMoreRequestInProgress) {
            isLoadMoreRequestInProgress = false;
            mTVEpisodesListStartIndex++;
            if (mCacheManager.isLastPage()) {
                mIsLoadingMoreAvailable = false;
            }
            if (mListEpisodes != null) {
                mListEpisodes.addAll(cardDataList);
                return;
            }
        }
        onEpiosodesLoaded(cardDataList, false);
        mListEpisodes = new ArrayList<>(cardDataList);
    }

    private void fetchRelatedVODData(String contentId) {
        if (!mIsFromViewAll) {
            if (isLoadMoreRequestInProgress) {
                addFooterProgressLoadingCard();
            }
            new RelatedVODAPICallTask(this, contentId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        fetchCarouselData();
    }

    private void updateRelatedVODData(List<CardData> dataList) {
        if (dataList == null) {
            if (isLoadMoreRequestInProgress) {
                mIsLoadingMoreAvailable = false;
                if (cardDetailViewRecyclerViewAdapter != null
                        && cardDetailViewRecyclerViewAdapter.getData() != null) {
                    removeItem(cardDetailViewRecyclerViewAdapter.getData().size() - 1);
                }
                return;
            }
            showErrorMessageView();
            return;
        }
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        removeItem(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
        for (int i = 0; i < dataList.size(); i++) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_EPISODES_VIEW, dataList.get(i)));
            cardDetailViewRecyclerViewAdapter.notifyItemInserted(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
        }
        mIsLoadingMoreAvailable = true;
        if (isLoadMoreRequestInProgress) {
            mTVEpisodesListStartIndex++;
            if (mCacheManager.isLastPage()) {
                mIsLoadingMoreAvailable = false;
            }
            if (mListEpisodes != null) {
                mListEpisodes.addAll(dataList);
            }
            return;
        }

        onEpiosodesLoaded(dataList, false);
        mListEpisodes = new ArrayList<>(dataList);
    }*/
/*

    private static class RelatedVODAPICallTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<FragmentCardDetailsDescription> fragmentEpisodesWeakReference;
        private final WeakReference<String> contentIdReference;

        RelatedVODAPICallTask(FragmentCardDetailsDescription fragmentEpisodesWeakReference, String contentId) {
            this.fragmentEpisodesWeakReference = new WeakReference<>(fragmentEpisodesWeakReference);
            this.contentIdReference = new WeakReference<>(contentId);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null)
                return null;
            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
            int startIndex = 1;
            if (fragmentCardDetailsDescription.isLoadMoreRequestInProgress)
                startIndex = fragmentCardDetailsDescription.mTVEpisodesListStartIndex + 1;
            fragmentEpisodesWeakReference.get().mCacheManager.getRelatedVODList(contentIdReference.get(), startIndex, true,
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateRelatedVODData(var1);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateRelatedVODData(var1);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, null) {
                                @Override
                                protected void safeRun(FragmentCardDetailsDescription fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateRelatedVODData(var1);
                                }
                            });
                        }
                    });
            return null;
        }
    }


    private void fetchCarouselData() {
        //Log.d(TAG, "fetchCarouselData:");
        CarouselInfoData mCarouselInfoData = CacheManager.getCarouselInfoData();
        if (mCarouselInfoData == null) {
            return;
        }
        if (isLoadMoreRequestInProgress) {
            addFooterProgressLoadingCard();
        }
        new CarouselInfTask(this, mCarouselInfoData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void updateCarouselInfoData(List<CardData> dataList) {
        if (dataList == null) {
            if (isLoadMoreRequestInProgress) {
                mIsLoadingMoreAvailable = false;
                if (cardDetailViewRecyclerViewAdapter != null
                        && cardDetailViewRecyclerViewAdapter.getData() != null) {
                    removeItem(cardDetailViewRecyclerViewAdapter.getData().size() - 1);
                }
                return;
            }
            showErrorMessageView();
            return;
        }
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int lastItem = cardDetailViewRecyclerViewAdapter.getItemCount() - 1;
        removeItem(lastItem);
        for (int i = 0; i < dataList.size(); i++) {
            detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_EPISODES_VIEW, dataList.get(i)));
            cardDetailViewRecyclerViewAdapter.notifyItemInserted(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
        }
        mIsLoadingMoreAvailable = true;
        if (isLoadMoreRequestInProgress) {
            mTVEpisodesListStartIndex++;
            if (mCacheManager.isLastPage()) {
                mIsLoadingMoreAvailable = false;
            }
            if (mListEpisodes != null) {
                mListEpisodes.addAll(dataList);
                return;
            }
        }
        onEpiosodesLoaded(dataList, false);
        mListEpisodes = new ArrayList<>(dataList);
    }
*/
    private void showErrorMessageView() {
        List<DetailsViewContent.DetailsViewDataItem> detailsViewDataItemList = cardDetailViewRecyclerViewAdapter.getData();
        int lastItem = cardDetailViewRecyclerViewAdapter.getItemCount() - 1;
        detailsViewDataItemList.remove(lastItem);
        cardDetailViewRecyclerViewAdapter.notifyItemRemoved(lastItem);
        String errorMessage = mContext.getString(R.string.error_fetch_videos);
        if (mCardData != null && (mCardData.isLive() || mCardData.isProgram())) {
            errorMessage = mContext.getString(R.string.programguide_data_fetch_error);
        }
        detailsViewDataItemList.add(new DetailsViewContent.DetailsViewDataItem(DetailsViewContent.CARDDETAIL_ERROR_MESSAGE_VIEW,
                CardData.DUMMY_LIST.get(0), errorMessage, null, mBgColor));
        cardDetailViewRecyclerViewAdapter.notifyItemInserted(cardDetailViewRecyclerViewAdapter.getItemCount() - 1);
    }

/*
    private static class CarouselInfTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<FragmentCardDetailsDescription> fragmentEpisodesWeakReference;
        private final WeakReference<CarouselInfoData> carouselInfoDataReference;

        CarouselInfTask(FragmentCardDetailsDescription fragmentEpisodesWeakReference, CarouselInfoData carouselInfoData) {
            this.fragmentEpisodesWeakReference = new WeakReference<>(fragmentEpisodesWeakReference);
            this.carouselInfoDataReference = new WeakReference<>(carouselInfoData);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null || carouselInfoDataReference.get() == null)
                return null;
            FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
            CarouselInfoData mCarouselInfoData = carouselInfoDataReference.get();
            final int pageSize = mCarouselInfoData.pageSize > 0 ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
            new MenuDataModel().fetchCarouseldata(fragmentCardDetailsDescription.mContext, mCarouselInfoData.name, fragmentCardDetailsDescription.mTVEpisodesListStartIndex, pageSize, true, mCarouselInfoData.modified_on, new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    if (fragmentEpisodesWeakReference.get() == null)
                        return;
                    FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                    fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                        @Override
                        protected void safeRun(FragmentCardDetailsDescription referenceOne, List<CardData> referenceTwo) {
                            referenceOne.updateCarouselInfoData(referenceTwo);
                        }
                    });
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    if (fragmentEpisodesWeakReference.get() == null)
                        return;
                    FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                    fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, dataList) {
                        @Override
                        protected void safeRun(FragmentCardDetailsDescription referenceOne, List<CardData> referenceTwo) {
                            referenceOne.updateCarouselInfoData(referenceTwo);
                        }
                    });
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {
                    //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                    if (fragmentEpisodesWeakReference.get() == null)
                        return;
                    FragmentCardDetailsDescription fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                    fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<FragmentCardDetailsDescription, List<CardData>>(fragmentCardDetailsDescription, null) {
                        @Override
                        protected void safeRun(FragmentCardDetailsDescription referenceOne, List<CardData> referenceTwo) {
                            referenceOne.updateCarouselInfoData(referenceTwo);
                        }
                    });
                }
            });
            return null;
        }
    }
*/

    private void editProfileAlertDialog() {
        editProfileDialog = new Dialog(mContext);
        editProfileDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.alert_edit_profile, null);
        editProfileDialog.setContentView(view);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(editProfileDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        countrySpinner = view.findViewById(R.id.countrySpinner);
        stateSpinner = view.findViewById(R.id.stateSpinner);
        citySpinner = view.findViewById(R.id.citySpinner);
        cityEdit = view.findViewById(R.id.cityEdit);
        addressEt = view.findViewById(R.id.addressEt);
        pincodeEt = view.findViewById(R.id.pincodeEt);
        Button updateButton = view.findViewById(R.id.updateProfile);

        getProfileDetails();


        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (countriesList != null && countriesList.size() != 0) {
                    country = countriesList.get(position).name;
                    countrySpinner.setSelection(getCountryIndex(country));
                    String code = getCountryCodeIndex(countriesList.get(position).name);
                    getStatesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (statesList != null && statesList.size() != 0) {
                    state = statesList.get(position).name;
                    stateSpinner.setSelection(getStateIndex(state));
                    String code = getStateCodeIndex(statesList.get(position).name);
                    getCitiesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (citiesList != null && citiesList.size() != 0) {
                    city = citiesList.get(position).name;
                    citySpinner.setSelection(getCityIndex(city));
                    //String code = getStateCodeIndex(statesList.get(position).name);
                    //getCitiesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        editProfileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editProfileDialog.dismiss();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = addressEt.getText().toString();
                String pincode = pincodeEt.getText().toString();
                if (TextUtils.isEmpty(country) || country.equalsIgnoreCase("Select Country")) {
                    Toast.makeText(mContext, "Please select Country", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(state) || state.equalsIgnoreCase("Select State")) {
                    Toast.makeText(mContext, "Please select State", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(city) || city.equalsIgnoreCase("Select City")) {
                    Toast.makeText(mContext, "Please select City", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateUserData(citySpinner.getSelectedItem().toString(),stateSpinner.getSelectedItem().toString(),editDob,
                        country,addressEt.getText().toString(),pincodeEt.getText().toString());

            }
        });

        editProfileDialog.show();
        editProfileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editProfileDialog.getWindow().setAttributes(lp);

    }

    private void updateUserData(String city,String state,String dob,String country,String address,String pincode){
        UpdateProfileRequest.Params params = new UpdateProfileRequest.Params( country,state,city, dob, address,pincode,"");
        UpdateProfileRequest updateProfileRequest=new UpdateProfileRequest(params,new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    editProfileDialog.dismiss();
                    UserProfileResponseData responseData = response.body();
                    if(responseData.status!=null&&responseData.status.equals(APIConstants.SUCCESS)){

                        if (dob!=null){
                            PrefUtils.getInstance().setUserDOB(dob);
                        }

                        if (!TextUtils.isEmpty(country)) {
                            PrefUtils.getInstance().setUSerCountry(country);
                        }

                        if (!TextUtils.isEmpty(state)){
                            PrefUtils.getInstance().setUserState(state);
                        }

                        if (!TextUtils.isEmpty(city)){
                            PrefUtils.getInstance().setUserCity(city);
                        }

                        AlertDialogUtil.showToastNotification(response.message());

                        if (!TextUtils.isEmpty(redirectLatest) && isAdded() && getActivity() != null) {
                            subscriptionFromPlayerScreen = true;
                            // startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), redirectLatest, SubscriptionWebActivity.PARAM_LAUNCH_NONE), SUBSCRIPTION_REQUEST);
                        }
                    }else {
                        AlertDialogUtil.showToastNotification(response.message());
                    }
                }else {
                    if (response.message()!=null&&!TextUtils.isEmpty(response.message())){
                        AlertDialogUtil.showToastNotification(response.message());
                    }else {
                        AlertDialogUtil.showToastNotification(getResources().getString(R.string.default_profile_update_message));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(updateProfileRequest);
    }

    private void getCountriesList(){
        CountriesListRequest countriesListRequest=new CountriesListRequest(new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().countries != null && response.body().countries.size()!= 0){
                    countriesList.clear();
                    countriesList.add(new CountriesData("NA","NA","Select Country"));
                    countriesList.addAll(response.body().countries);
                    ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item,
                            getCountriesListInString());
                    countrySpinner.setAdapter(countriesAdapter);
                    if (country != null && !TextUtils.isEmpty(country)) {
                        countrySpinner.setSelection(getCountryIndex(country));
                    }
                    getStatesList(getCountryCodeIndex(country));
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(countriesListRequest);
    }

    private void getStatesList(String code){
        StatesListRequest.Params params=new StatesListRequest.Params(code);
        StatesListRequest statesListRequest=new StatesListRequest(params,new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().states != null && response.body().states.size()!= 0){
                    statesList.clear();
                    statesList.add(new CountriesData("NA","NA","Select State"));
                    statesList.addAll(response.body().states);
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(mContext,
                            R.layout.spinner_item,
                            getStatesListInString());
                    stateSpinner.setAdapter(statesAdapter);
                    if (state!=null&&!TextUtils.isEmpty(state)){
                        stateSpinner.setSelection(getStateIndex(state));
                    }else {
                        stateSpinner.setSelection(0);
                        state=stateSpinner.getSelectedItem().toString();
                    }
                    getCitiesList(getStateCodeIndex(state));
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(statesListRequest);
    }


    private void getCitiesList(String code) {
        CityListRequest.Params params = new CityListRequest.Params(code);
        CityListRequest statesListRequest = new CityListRequest(params, new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().cities != null && response.body().cities.size() != 0) {
                    citiesList.clear();
                    citiesList.add(new CountriesData("NA","NA","Select City"));
                    citiesList.addAll(response.body().cities);
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item,
                            getCityListInString());
                    citySpinner.setAdapter(statesAdapter);
                    if (city != null && !TextUtils.isEmpty(city)) {
                        citySpinner.setSelection(getCityIndex(city));
                    } else {
                        citySpinner.setSelection(0);
                        city = citySpinner.getSelectedItem().toString();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(statesListRequest);
    }

    private String getStateCodeIndex(String country) {
        String code = null;
        for (int p = 0; p < statesList.size(); p++) {
            if (statesList.get(p).name.equalsIgnoreCase(country)) {
                code = statesList.get(p).code;
            }
        }
        return code;
    }

    private int getCityIndex(String cityName) {
        int index = 0;
        for (int p = 0; p < citiesList.size(); p++) {
            if (cityName.equalsIgnoreCase(citiesList.get(p).name)) {
                index = p;
            }
        }
        return index;
    }

    private List<String> getCityListInString() {
        List<String> statesListNew = new ArrayList<>();
        for (int p = 0; p < citiesList.size(); p++) {
            statesListNew.add(citiesList.get(p).name);
        }
        return statesListNew;
    }

    private List<String> getCountriesListInString(){
        List<String> countriesListNew=new ArrayList<>();
        for (int p=0;p<countriesList.size();p++){
            countriesListNew.add(countriesList.get(p).name);
        }
        return countriesListNew;
    }

    private List<String> getStatesListInString(){
        List<String> statesListNew=new ArrayList<>();
        for (int p=0;p<statesList.size();p++){
            statesListNew.add(statesList.get(p).name);
        }
        //statesListNew.add(0,"Select State");
        return statesListNew;
    }

    private int getCountryIndex(String countryName){
        int index=0;
        for (int p=0;p<countriesList.size();p++){
            if (countryName.equalsIgnoreCase(countriesList.get(p).name)){
                index=p;
            }
        }
        return index;
    }

    private int getStateIndex(String stateName){
        int index=0;
        for (int p=0;p<statesList.size();p++){
            if (stateName.equalsIgnoreCase(statesList.get(p).name)){
                index=p;
            }
        }
        return index;
    }

    private String getCountryCodeIndex(String country){
        String code = null;
        for (int p=0;p<countriesList.size();p++){
            if(countriesList.get(p).name.equalsIgnoreCase(country)){
                code=countriesList.get(p).indexCode;
            }
        }
        return code;
    }

    private void getProfileDetails() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if ( responseData.result != null
                            && responseData.result.profile != null) {
                        setData(responseData);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    private void setData(UserProfileResponseData responseData){

        if (responseData.result.profile.first!=null&&!TextUtils.isEmpty(responseData.result.profile.first)){
            editUserName=responseData.result.profile.first;
        }
        if (responseData.result.profile.last!=null&&!TextUtils.isEmpty(responseData.result.profile.last)){
            editLastName=responseData.result.profile.last;
        }
        if (responseData.result.profile.gender!=null&&!TextUtils.isEmpty(responseData.result.profile.gender)){
            if (responseData.result.profile.gender.equalsIgnoreCase("M")){
                editGender="male";
            }else if(responseData.result.profile.gender.equalsIgnoreCase("F")){
                editGender="female";
            }else {
                editGender="Select Gender";
            }
        }
        if (responseData.result.profile.mobile_no!=null&&!TextUtils.isEmpty(responseData.result.profile.mobile_no)){
            editMobile=responseData.result.profile.mobile_no;
        }

        if (responseData.result.profile.age!=null&&!TextUtils.isEmpty(responseData.result.profile.age)){
            editAge = responseData.result.profile.age;
        }

        if (responseData.result.profile.dob !=null&&!TextUtils.isEmpty(responseData.result.profile.dob)){
            editDob=responseData.result.profile.dob;
        }

        if (responseData.result.profile.emails != null && responseData.result.profile.emails.size() >0 && !TextUtils.isEmpty(responseData.result.profile.emails.get(0).email)){
            editEmail=responseData.result.profile.emails.get(0).email;
        }

        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
            if (responseData.result.profile.locations.get(0) != null
                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
        }

        if (responseData.result.profile.state !=null&&!TextUtils.isEmpty(responseData.result.profile.state)){
            PrefUtils.getInstance().setUserState(responseData.result.profile.state);
        }

        if (responseData.result.profile.city !=null&&!TextUtils.isEmpty(responseData.result.profile.city)){
            PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
        }


        if (responseData.result.profile.city !=null&&!TextUtils.isEmpty(responseData.result.profile.city)){
            cityEdit.setText(responseData.result.profile.city);
            city=responseData.result.profile.city;
        }

        if(responseData.result.profile.locations.size()!=0){
            country=responseData.result.profile.locations.get(0);
        }
        state=responseData.result.profile.state;
        getCountriesList();
    }

}
