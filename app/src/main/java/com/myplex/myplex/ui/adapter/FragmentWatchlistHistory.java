package com.myplex.myplex.ui.adapter;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.FetchWatchlistHistory;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.views.GridSpacingItemDecoration;
import com.myplex.myplex.utils.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ramraju on 2/20/2018.
 */

public class FragmentWatchlistHistory extends BaseFragment {

    private static String TAG = FragmentWatchlistHistory.class.getSimpleName();
    String navMenuName;
    private ImageView mImageNoWatchlist;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private ContinueWatchingHorizontalCarousel mAdapterMoviesGrid;
    private List<CardData> mListCardData = new ArrayList<>();
    private List<CarouselInfoData> mListCarouselInfo=new ArrayList<>();
    private boolean mIsLoadingMorePages = false;
    private int mStartIndex = 1;
    private boolean mIsLoadingMoreAvailable = true;
    private TextView mGridViewLoadingText;

    private String altitle,title;
    private String carouselName;




    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MainActivity) mBaseActivity).enableNavigation();
            ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
            mBaseActivity.removeFragment(FragmentWatchlistHistory.this);
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LoggerD.debugDownload("onCreateView");
        mContext = getActivity();
        //getBundleValues();
        View rootView = inflater.inflate(R.layout.fragment_watchhistory, container, false);
        mImageNoWatchlist = (ImageView) rootView.findViewById(R.id.coming_soon);
        mRecyclerView = rootView.findViewById(R.id.gridview_movies);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.card_loading_progres_bar);
        mGridViewLoadingText = (TextView)rootView.findViewById(R.id.grid_footer_text_loading);

        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);

        View mInflateView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_toolbar, null, false);
        TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        TextView mToolbarTitleOtherLang = (TextView) mInflateView.findViewById(R.id.toolbar_header_title_lang);
        ImageView mCloseIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
        RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        if(TextUtils.isEmpty(title)) {
            mToolbarTitle.setText("Watch History");
        }else{
            mToolbarTitle.setText(title);
        }
        mCloseIcon.setOnClickListener(mCloseAction);
        ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        if(PrefUtils.getInstance().getVernacularLanguage()){

            if ( !TextUtils.isEmpty(altitle)) {
                mToolbarTitleOtherLang.setText(altitle);
                mToolbarTitleOtherLang.setVisibility(View.VISIBLE);
            }else{
                mToolbarTitleOtherLang.setVisibility(View.GONE);
            }


        }else{
            mToolbarTitleOtherLang.setVisibility(View.GONE);
        }
        GridLayoutManager linearLayoutManager = new GridLayoutManager(mContext,2);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        GridSpacingItemDecoration mHorizontalMoviesDivieder = new GridSpacingItemDecoration(2,8,true);
        mRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerView.setItemAnimator(null);
        if (Util.checkUserLoginStatus()){
            fetchCarouselData();
        }else {
            mImageNoWatchlist.setVisibility(View.VISIBLE);
        }
        //loadData();
        return rootView;
    }

    private void fetchCarouselData() {
        showProgressBar();
        new MenuDataModel().fetchMenuList(navMenuName, mStartIndex, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                if (dataList == null) {
                    return;
                }
                if (dataList.get(0).name!=null){
                    mListCarouselInfo=dataList;
                    carouselName=dataList.get(0).name;
                    loadData();
                }else {
                    mImageNoWatchlist.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                if (dataList == null) {
                    return;
                }

                if (!dataList.isEmpty()&&dataList.get(0)!=null&&dataList.get(0).name!=null){
                    mListCarouselInfo=dataList;
                    carouselName=dataList.get(0).name;
                    loadData();
                }else {
                    mImageNoWatchlist.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                dismissProgressBar();
            }

        });


    }
    private void loadData() {
        if (!mIsLoadingMorePages) {
            showProgressBar();
        }
            FetchWatchlistHistory.Params params = new FetchWatchlistHistory.Params("","generalInfo,images",
                    mStartIndex,APIConstants.PAGE_INDEX_COUNT, carouselName);
            FetchWatchlistHistory fetchWatchlist = new FetchWatchlistHistory(params, new APICallback<CardResponseData>() {
                @Override
                public void onResponse(APIResponse<CardResponseData> response) {
                    mImageNoWatchlist.setVisibility(View.GONE);
                    dismissProgressBar();
                    mGridViewLoadingText.setVisibility(View.GONE);
                    if (response == null
                            || response.body() == null) {
                        mImageNoWatchlist.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (!mIsLoadingMorePages && (response == null
                            || response.body() == null)) {
                        mImageNoWatchlist.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<CardData> dataList = response.body().results;
                    if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                        mImageNoWatchlist.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (dataList == null || dataList.isEmpty()) {
                        mImageNoWatchlist.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                        mIsLoadingMoreAvailable = false;
                    }
                    if (mIsLoadingMorePages) {
                        mImageNoWatchlist.setVisibility(View.GONE);
                        mIsLoadingMorePages = false;
                        if (mAdapterMoviesGrid != null) {
                            mAdapterMoviesGrid.setData(dataList);
                            mAdapterMoviesGrid.notifyDataSetChanged();
                        }
                    } else {
                        updateData(response.body().results);
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    mImageNoWatchlist.setVisibility(View.VISIBLE);
                }
            });
            APIService.getInstance().execute(fetchWatchlist);

    }

    private void updateData(List<CardData> results) {
        if (results!=null&&!results.isEmpty()){
            mListCardData.addAll(results);
            mAdapterMoviesGrid = new ContinueWatchingHorizontalCarousel(mContext, results, mRecyclerView);
            mAdapterMoviesGrid.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
            mAdapterMoviesGrid.isContinueWatchingSection(checkIfContinueWatchingSection());
            mAdapterMoviesGrid.isToShowGridLayout(true);
            mRecyclerView.setAdapter(mAdapterMoviesGrid);
        }
    }

    private boolean checkIfContinueWatchingSection() {
        if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty() && mListCarouselInfo.get(0) != null
                && mListCarouselInfo.get(0).layoutType != null
                && mListCarouselInfo.get(0).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING)) {
            return true;
        }
        return true;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onBackClicked() {
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: onPause");
        return super.onBackClicked();
    }

    private void showDetailsFragment(CardData cardData, int position) {

        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);

        if (cardData != null
                && cardData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, cardData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, cardData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != cardData.startDate
                        && null != cardData.endDate) {
                    Date startDate = Util.getDate(cardData.startDate);
                    Date endDate = Util.getDate(cardData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }
        }
        if (cardData.generalInfo != null
                && cardData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
        }
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, "banners");
        if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
        }
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
        ((BaseActivity) mContext).showDetailsFragment(args, cardData);
    }

    public void showProgressBar() {

        if (mContext == null) {
            return ;
        }
        if(mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.GONE);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

    }

    public void dismissProgressBar() {
        try {
            if (!isAdded()) {
                return;
            }
            if(mProgressBar != null
                    && mProgressBar.getVisibility() == View.VISIBLE){
                mProgressBar.setVisibility(View.GONE);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            CarouselInfoData carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;

            Analytics.gaEventBrowsedCategoryContentType(carouselData);

            if (carouselInfoData != null) {
                Analytics.setVideosCarouselName(carouselInfoData.title);
            }

            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;


            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, carouselInfoData, null, parentPosition);
                return;
            }

            LoggerD.debugLog("pacakge name- " + mContext.getPackageName());

            if (carouselData != null
                    && carouselData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !TextUtils.isEmpty(carouselData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, carouselData.generalInfo.title, 1l);
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, carouselData.generalInfo.title));
                return;
            }

            if (carouselInfoData == null) {
                showDetailsFragment(carouselData, position, "", parentPosition);
            } else {
                showDetailsFragment(carouselData, carouselInfoData, parentPosition);
            }

        }

    };

    private void showDetailsFragment(CardData carouselData, CarouselInfoData carouselInfoData, int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }
        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }

        }

        if (!APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType)) {
            if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !carouselData.isTVSeries()
                    && !carouselData.isVODChannel()
                    && !carouselData.isVODYoutubeChannel()
                    && !carouselData.isVODCategory()
                    && !carouselData.isTVSeason()) {
                args.putSerializable(CardDetails.PARAM_QUEUE_LIST_CARD_DATA, (Serializable) carouselInfoData.listCarouselData);
            }
        }

        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void showDetailsFragment(CardData carouselData, int position, String carousalTitle, int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }
        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }
        }

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carousalTitle);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }



    public void setCarouselName(String carouselName){
        this.navMenuName=carouselName;
    }

    public void setAlttitle(String str){
        altitle = str;
    }

    public void settitle(String str){
        title = str;
    }

}
