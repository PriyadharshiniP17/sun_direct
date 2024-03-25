package com.myplex.myplex.ui.adapter;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.FetchWatchlistFavorites;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ramraju on 2/20/2018.
 */

public class FragmentWatchlistFavourites extends BaseFragment {

    private static String TAG = FragmentWatchlistFavourites.class.getSimpleName();


    public static final int TYPE_MOVIES = 0;
    public static final int TYPE_TV_SHOWS = 1;
    public static final int TYPE_LIVE = 2;
    public static final int TYPE_VOD = 3;
    public static final int TYPE_TRAILER = 4;
    public static final int TYPE_NEWS=4;
    public static final int TYPE_ORIGINALS = 5;
    public static final int TYPE_MUSIC=6;
    private String contentType;
    int currentType = 0;

    int currentFragment = 0;
    private ImageView mImageNoWatchlist;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private AdapterMoviesForGrid mAdapterMoviesGrid;
    private List<CardData> mListCardData = new ArrayList<>();
    private boolean mIsLoadingMorePages = false;
    private int mStartIndex = 1;
    private boolean mIsLoadingMoreAvailable = true;
    private TextView mGridViewLoadingText;

    public void setTabName(String[] tabName) {
        this.tabName = tabName;
    }

    private String[] tabName;

    private int requestType;

    public FragmentWatchlistFavourites newInstance(int type,int requestType) {
        LoggerD.debugDownload("newInstance");
        FragmentWatchlistFavourites fragmentWatchlistFavourites = new FragmentWatchlistFavourites();
        Bundle args = new Bundle();
        currentType = 0;
        switch (type) {
            case TYPE_MOVIES:
                currentType = TYPE_MOVIES;
                break;
            case TYPE_TV_SHOWS:
                currentType = TYPE_TV_SHOWS;
                break;
            case TYPE_MUSIC:
                currentType = TYPE_MUSIC;
                break;
            case TYPE_NEWS:
                currentType = TYPE_NEWS;
                break;
        }
        args.putInt("TYPE", currentType);
        args.putInt("REQUEST_TYPE",requestType);
        fragmentWatchlistFavourites.setArguments(args);
        return fragmentWatchlistFavourites;
    }

    private final AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                if (!mIsLoadingMorePages) {
                    if (!mIsLoadingMoreAvailable) {
                        return;
                    }
                    mIsLoadingMorePages = true;
                    mStartIndex++;
                    mGridViewLoadingText.setVisibility(View.VISIBLE);
                    loadData();
                }
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LoggerD.debugDownload("onCreateView");
        mContext = getActivity();
        getBundleValues();
        View rootView = inflater.inflate(R.layout.fragment_watchlist, container, false);
        mImageNoWatchlist = (ImageView) rootView.findViewById(R.id.coming_soon);
        mGridView = (GridView)rootView.findViewById(R.id.gridview_movies);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.card_loading_progres_bar);
        mGridViewLoadingText = (TextView)rootView.findViewById(R.id.grid_footer_text_loading);
        if (DeviceUtils.isTablet(mContext)) {
            if( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mGridView.setNumColumns(4);
                return rootView;
            }
            mGridView.setNumColumns(3);
        } else {
            mGridView.setNumColumns(2);
        }
        mGridView.setOnScrollListener(mScrollListener);
        mGridView.setOnItemClickListener(mProgramClickListener);
        loadData();
        return rootView;
    }


    private void loadData() {
        if (!mIsLoadingMorePages) {
            showProgressBar();
        }
        switch (currentFragment){
            case TYPE_MOVIES:
                contentType = APIConstants.TYPE_MOVIE+ ","+ APIConstants.TYPE_VOD +","+APIConstants.TYPE_YOUTUBE;
                break;
            case TYPE_TV_SHOWS:
                contentType = APIConstants.TYPE_TVSERIES + "," + APIConstants.TYPE_VODCHANNEL + "," + APIConstants.TYPE_VODCATEGORY + "," + APIConstants.TYPE_TVSEASON + "," + APIConstants.TYPE_VODYOUTUBECHANNEL;
                break;
            case TYPE_NEWS:
                contentType = APIConstants.TYPE_NEWS;
                break;
            case TYPE_MUSIC:
                contentType = APIConstants.TYPE_MUSIC_VIDEO;
                break;
        }
        FetchWatchlistFavorites.Params params = new FetchWatchlistFavorites.Params(contentType,"generalInfo,images",mStartIndex,"",APIConstants.PAGE_INDEX_COUNT,requestType);
        FetchWatchlistFavorites fetchWatchlistFavorites = new FetchWatchlistFavorites(params, new APICallback<CardResponseData>() {
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
                        mAdapterMoviesGrid.add(dataList);
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
        APIService.getInstance().execute(fetchWatchlistFavorites);

    }

    private void updateData(List<CardData> results) {
        mListCardData.addAll(results);
        switch (currentFragment){
            case TYPE_MOVIES:
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results,false,
                        APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER,requestType);
                mGridView.setNumColumns(3);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;
            case TYPE_TV_SHOWS:
                mGridView.setNumColumns(3);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results, false,
                        APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER,requestType);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;
            case TYPE_LIVE:
                mGridView.setNumColumns(2);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results, false,
                        APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM,requestType);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;
            case TYPE_VOD:
                mGridView.setNumColumns(2);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results, true,
                        APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM,requestType);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;
            case TYPE_NEWS:
                mGridView.setNumColumns(2);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results, true,
                        APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM,requestType);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;
            case TYPE_MUSIC:
                mGridView.setNumColumns(2);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results, true,
                        APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM,requestType);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;
            case 5:
                mGridView.setNumColumns(2);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, results);
                mGridView.setAdapter(mAdapterMoviesGrid);
                break;

        }
        if (mAdapterMoviesGrid != null) {
            mAdapterMoviesGrid.setDeleteEnabled(true);
            mAdapterMoviesGrid.setTabName(tabName);
        }
       /* if (mGridView != null) {
            mGridView.setNumColumns(Util.getNumColumns(mContext));
        }*/

    }

    private void getBundleValues() {
        Bundle args = getArguments();
        currentFragment = args.getInt("TYPE", 0);
        if (args.containsKey("REQUEST_TYPE")){
            requestType=args.getInt("REQUEST_TYPE");
        }
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
        mGridView.setNumColumns(Util.getNumColumns(mContext));
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onBackClicked() {
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: onPause");
        return super.onBackClicked();
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = (view, position, parentPosition, carouselData) -> {
        //movie item clicked we can have movie data here

        if (carouselData == null || carouselData._id == null) return;
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            Log.d("FragmentWatchList", "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            //showRelatedVODListFragment(carouselData, parentPosition);
            return;
        }

        String publishingHouse = carouselData == null
                || carouselData.publishingHouse == null
                || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;

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
        showDetailsFragment(carouselData, position);
    };

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


    private final AdapterView.OnItemClickListener mProgramClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            if ( mListCardData == null) {
                return;
            }
            if (position >= mAdapterMoviesGrid.getCount()) {
                //Log.d(TAG, "Index out of bounds");
                return;
            }
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setEnabled(true);
                }
            }, 500);
            view.setEnabled(false);
            final CardData cardData = mAdapterMoviesGrid.getItem(position);
            if (cardData == null || cardData._id == null) {
                return;
            }

            /*if (ApplicationController.DEBUG_DOWNLOAD) {
                ScopedBus.getInstance().post(new ContentDownloadEvent(cardData));
                return;
            }*/


            String publishingHouse = cardData == null
                    || cardData.publishingHouse == null
                    || TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName) ? null : cardData.publishingHouse.publishingHouseName;

            showDetailsFragment(cardData);
        }
    };

    private void showDetailsFragment(CardData cardData) {
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);

        if (cardData.generalInfo != null
                && cardData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
        }
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
        String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_WATCHLIST_VIDEOS);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_WATCHLIST_VIDEOS);
        ((BaseActivity) mContext).showDetailsFragment(args, cardData);
    }
    private void overlayRelatedVODListFragment(CardData cardData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));

    }

}
