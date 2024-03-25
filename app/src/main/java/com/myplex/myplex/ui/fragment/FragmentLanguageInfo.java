package com.myplex.myplex.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;

import com.myplex.model.Categories;
import com.myplex.model.CategoryScreenFilters;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;

import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.adapter.AdapterLiveTvItem;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.ui.views.posterview.StartSnapHelper;
import com.myplex.myplex.utils.Util;


import java.util.List;

import static com.myplex.myplex.ui.fragment.FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE_NAVIGANTION;

public class FragmentLanguageInfo extends BaseFragment  {



    private static final String TAG = FragmentLanguageInfo.class.getSimpleName();

    private Context mContext;

    private int mStartIndex = 1;



    private ProgressBar mProgressBar;
    private RecyclerView mMoviesRecyclerView,mLiveTVRecyclerView,mTVShowsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManagerLive,mLayoutManagerMovie,mLayoutManagerTVShows;
    private TextView liveCategoryText,movieCategoryText,tvshowsCategoryText,errorMessage;
    private String language;
    private String genre,mSourceDetailForAnalytics,mSourceForAnalytics;
    private boolean isGenreOnly;
    private LinearLayout liveTVlayout,movieLayout,tvShowLayout;
    private boolean didTVShowsLoad = true;
    private boolean didMoviesLoad = true;
    private boolean didLiveTVLoad = true;
    private ImageView liveIcon,movieIcon,TVShowsIcon;
    private CarouselInfoData carouselInfoData;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mToolbarCloseButton;
    private ImageView mToolbarListGridLayoutSwitch;
    private ImageView channelImageView;
    private RelativeLayout mRootLayout;
    private ImageView mImageViewFilterIcon;
    private CategoryScreenFilters categoryScreenFilters;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        ((MainActivity) mBaseActivity).disableNavigation();
        View rootView = inflater.inflate(R.layout.fragment_languages_info, container, false);

        mMoviesRecyclerView     =  rootView.findViewById(R.id.movies);
        mLiveTVRecyclerView     =  rootView.findViewById(R.id.liveTv);
        mTVShowsRecyclerView    =  rootView.findViewById(R.id.tvShows);
        mLayoutManagerLive      = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false);
        mLayoutManagerMovie     = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false);
        mLayoutManagerTVShows   = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false);
        mProgressBar            =  rootView.findViewById(R.id.card_loading_progres_bar);
        errorMessage            = rootView.findViewById(R.id.error_message);
        liveTVlayout            = rootView.findViewById(R.id.liveTVlayout);
        movieLayout             = rootView.findViewById(R.id.movieLayout);
        tvShowLayout            = rootView.findViewById(R.id.tvShowLayout);
        liveIcon                = rootView.findViewById(R.id.liveIcon);
        movieIcon               = rootView.findViewById(R.id.moviesIcon);
        TVShowsIcon             = rootView.findViewById(R.id.tvShowsIcon);

        TextView liveMore       = rootView.findViewById(R.id.liveMore);
        TextView moviesMore     = rootView.findViewById(R.id.moviesMore);
        TextView TVShowsMore    = rootView.findViewById(R.id.tvShowsMore);

        liveCategoryText        = rootView.findViewById(R.id.liveCategoryText);
        movieCategoryText       = rootView.findViewById(R.id.moviesCategoryText);
        tvshowsCategoryText     = rootView.findViewById(R.id.tvShowsCategoryText);
        Toolbar toolbar         =  rootView.findViewById(R.id.toolbar);
        liveMore.setOnClickListener(onClickListener);
        moviesMore.setOnClickListener(onClickListener);
        TVShowsMore.setOnClickListener(onClickListener);

        mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar_carousel_view_all, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mToolbarCloseButton = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);

        channelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mImageViewFilterIcon = (ImageView) mInflateView.findViewById(R.id.toolbar_filter_icon);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        mToolbarListGridLayoutSwitch = (ImageView) mInflateView.findViewById(R.id.toolbar_list_grid_converter);
        channelImageView.setVisibility(View.GONE);
        mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
        genre = getArguments().getString(FragmentCarouselInfo.PARAM_GENRE);
        isGenreOnly=getArguments().getBoolean(FragmentCarouselInfo.PARAM_IS_GENRE_ONLY);
        mSourceDetailForAnalytics=getArguments().getString(FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE);
        mSourceForAnalytics = getArguments().getString(PARAM_ANALYTICS_CAROUSAL_SOURCE_NAVIGANTION);
        if (!isGenreOnly)
            language = getArguments().getString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE);

        carouselInfoData = CacheManager.getCarouselInfoData();


        toolbar.addView(mInflateView);
        toolbar.setContentInsetsAbsolute(0, 0);
        if (carouselInfoData != null && carouselInfoData.title != null) {
            mToolbarTitle.setText(carouselInfoData.title);
        }
        mToolbarCloseButton.setOnClickListener(mCloseAction);

        liveCategoryText.setText(mContext.getResources().getString(R.string.live_tv));
        movieCategoryText.setText(mContext.getResources().getString(R.string.movies));
        tvshowsCategoryText.setText(mContext.getResources().getString(R.string.tv_shows));

        Bitmap bitmap = Util.getBitmap(mContext,"epgListNewLiveTVLayout",null,false);
        if (bitmap == null) {
            bitmap = Util.getBitmap(mContext,"Live_tv_newlayout",null,false);
        }
        liveIcon.setImageBitmap(bitmap);
        movieIcon.setImageBitmap(Util.getBitmap(mContext,APIConstants.TYPE_MOVIE,null, false));
        TVShowsIcon.setImageBitmap(Util.getBitmap(mContext,APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW,null, false));


        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(getActivity(), R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        HorizontalItemDecorator mHorizontalMoviesDivieder = new HorizontalItemDecorator(8);

        mMoviesRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);
        mLiveTVRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);
        mTVShowsRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);

        mMoviesRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);
        mLiveTVRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);
        mTVShowsRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);

        mMoviesRecyclerView.setLayoutManager(mLayoutManagerLive);
        mLiveTVRecyclerView.setLayoutManager(mLayoutManagerMovie);
        mTVShowsRecyclerView.setLayoutManager(mLayoutManagerTVShows);

        categoryScreenFilters = PropertiesHandler.getCategoryScreenFilters(mContext);

        SnapHelper snapHelper = new StartSnapHelper();
        snapHelper.attachToRecyclerView(mMoviesRecyclerView);
        SnapHelper livesnapHelper = new StartSnapHelper();
        livesnapHelper.attachToRecyclerView(mLiveTVRecyclerView);
        SnapHelper tvshowssnapHelper = new StartSnapHelper();
        tvshowssnapHelper.attachToRecyclerView(mTVShowsRecyclerView);

        //Check for Network before making calls
        checkNetworkAndMakeCalls();
        return rootView;
    }

    public static FragmentLanguageInfo newInstance(Bundle args) {
        FragmentLanguageInfo fragmentLanguageInfo = new FragmentLanguageInfo();
        fragmentLanguageInfo.setArguments(args);
        return fragmentLanguageInfo;
    }


    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseActivity == null || mBaseActivity.isFinishing()) {
                return;
            }
            if (mBaseActivity instanceof MainActivity) {
              //  ((MainActivity) mBaseActivity).removeFilterFragment();
                ((MainActivity) mBaseActivity).onBackPressed();
            }
        //    mBaseActivity.removeFragment(FragmentLanguageInfo.this);
        }
    };

    private void checkNetworkAndMakeCalls() {

        if(Util.isNetworkAvailable(mContext)) {
            loadMovieCarouselInfo();
            fetchEpgData();
            loadTVShowCarouselInfo();
        }else{
            errorMessage.setText(mContext.getResources().getString(R.string.network_error));
            dismissProgressBar();
            liveTVlayout.setVisibility(View.GONE);
            movieLayout.setVisibility(View.GONE);
            tvShowLayout.setVisibility(View.GONE);

        }
    }




    private void loadTVShowCarouselInfo() {

        String orderBy = "siblingOrder";
        String publishingHouseId = null;
        if (categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters.size() > 0) {
            for (int i = 0; i < categoryScreenFilters.categoryScreenFilters.size(); i++) {
                if (categoryScreenFilters.categoryScreenFilters.get(i) != null
                        && !TextUtils.isEmpty(categoryScreenFilters.categoryScreenFilters.get(i).contentType)
                        && categoryScreenFilters.categoryScreenFilters.get(i).contentType.equalsIgnoreCase(APIConstants.TV_SERIES)) {
                    orderBy = categoryScreenFilters.categoryScreenFilters.get(i).orderBy;
                    publishingHouseId = categoryScreenFilters.categoryScreenFilters.get(i).publishingHouseId;
                }
            }
        }
        showProgressBar();
        RequestContentList.Params contentListparams = new RequestContentList.Params(APIConstants.TYPE_TVSERIES, mStartIndex, APIConstants.PAGE_INDEX_COUNT, language, genre,orderBy,publishingHouseId);
        RequestContentList mRequestContentList = new RequestContentList(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        dismissProgressBar();
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message - " + response.body().status);
                        List<CardData> dataList = response.body().results;
                        if (dataList == null || dataList.isEmpty()) {
                            didTVShowsLoad = false;
                            checkOtherLayouts();
                            tvShowLayout.setVisibility(View.GONE);
                            return;
                        }

                        AdapterBigHorizontalCarousel adapterBigHorizontalCarousel = new AdapterBigHorizontalCarousel(mContext, dataList);
                        adapterBigHorizontalCarousel.setSourceDetailsForAnalytics(mSourceDetailForAnalytics,mSourceForAnalytics);
                        mTVShowsRecyclerView.setAdapter(adapterBigHorizontalCarousel);
                        tvShowLayout.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        didTVShowsLoad = false;
                        checkOtherLayouts();
                        tvShowLayout.setVisibility(View.GONE);
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + t);
                    }
                });
        APIService.getInstance().execute(mRequestContentList);
    }


    private void fetchEpgData() {

        showProgressBar();
        final String previousLanguages = EPG.langFilterValues;
        final String previousGenres=EPG.genreFilterValues;
        if(carouselInfoData != null && !TextUtils.isEmpty(carouselInfoData.title)){
            if(!isGenreOnly)
                EPG.langFilterValues = carouselInfoData.title.toLowerCase();
            else
                EPG.genreFilterValues=carouselInfoData.title.toLowerCase();
        }
        final int pageSize;
        if (carouselInfoData != null) {
            pageSize = carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
        }else{
            pageSize = APIConstants.PAGE_INDEX_COUNT;
        }
        EPG.getInstance(Util.getCurrentDate(0)).findPrograms(mContext, pageSize, mStartIndex, new EPG.CacheManagerCallback() {

            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                EPG.langFilterValues = previousLanguages;
                EPG.genreFilterValues=previousGenres;
                dismissProgressBar();
                if ((dataList == null || dataList.isEmpty())) {
                    checkOtherLayouts();
                    liveTVlayout.setVisibility(View.GONE);
                    return;
                }
                AdapterLiveTvItem adapterBigHorizontalCarousel = new AdapterLiveTvItem(mContext, dataList,mLiveTVRecyclerView);
                adapterBigHorizontalCarousel.setSourceDetailsForAnalytics(mSourceDetailForAnalytics,mSourceForAnalytics);
                mLiveTVRecyclerView.setAdapter(adapterBigHorizontalCarousel);
                liveTVlayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                EPG.langFilterValues = previousLanguages;
                EPG.genreFilterValues=previousGenres;
                String reason = error.getMessage() == null ? "NA" : error.getMessage();
                liveTVlayout.setVisibility(View.GONE);
                didLiveTVLoad = false;
                checkOtherLayouts();
                //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + error);
            }
        });


    }


    private void loadMovieCarouselInfo() {

        String orderBy = "releaseDate";
        String publishingHouseId = null;
        Categories categories;
        if (categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters.size() > 0) {
            for (int i=0; i<categoryScreenFilters.categoryScreenFilters.size(); i++){
                if(categoryScreenFilters.categoryScreenFilters.get(i) != null
                        &&    !TextUtils.isEmpty(categoryScreenFilters.categoryScreenFilters.get(i).contentType)
                        && categoryScreenFilters.categoryScreenFilters.get(i).contentType.equalsIgnoreCase(APIConstants.TYPE_MOVIE)){
                    categories = categoryScreenFilters.categoryScreenFilters.get(i);
                    orderBy = categoryScreenFilters.categoryScreenFilters.get(i).orderBy;
                    publishingHouseId = categoryScreenFilters.categoryScreenFilters.get(i).publishingHouseId;
                }
            }
        }

        showProgressBar();
            // updateToolbarTitle();
            RequestContentList.Params contentListparams = new RequestContentList.Params(APIConstants.TYPE_MOVIE, mStartIndex, APIConstants.PAGE_INDEX_COUNT, language, genre,orderBy,publishingHouseId);
            RequestContentList mRequestContentList = new RequestContentList(contentListparams,
                    new APICallback<CardResponseData>() {
                        @Override
                        public void onResponse(APIResponse<CardResponseData> response) {
                            //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message - " + response.body().status);
                            dismissProgressBar();
                            List<CardData> dataList = response.body().results;
                            if (dataList == null || dataList.isEmpty()) {
                                didMoviesLoad = false;
                                checkOtherLayouts();
                                movieLayout.setVisibility(View.GONE);
                                return;
                            }

                            AdapterBigHorizontalCarousel adapterBigHorizontalCarousel = new AdapterBigHorizontalCarousel(mContext, dataList);
                            adapterBigHorizontalCarousel.setSourceDetailsForAnalytics(mSourceDetailForAnalytics,mSourceForAnalytics);
                            mMoviesRecyclerView.setAdapter(adapterBigHorizontalCarousel);
                            movieLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            didMoviesLoad = false;
                            checkOtherLayouts();
                            movieLayout.setVisibility(View.GONE);
                            //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + t);
                        }
                    });
            APIService.getInstance().execute(mRequestContentList);


    }




    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
    }


    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CarouselInfoData carouselInfoData = new CarouselInfoData();
            Bundle args = new Bundle();
            if (!isGenreOnly)
                args.putString(FragmentCarouselViewAll.PARAM_LANGUAGE_FILTER_VALUE, language);
            else
                args.putString(FragmentCarouselViewAll.PARAM_GENRE_FILTER_VALUE, genre);
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE, FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER);
            switch (view.getId()) {
                case R.id.liveMore:
                    if (carouselInfoData != null) {
                        carouselInfoData.title = "Live TV";
                        carouselInfoData.appAction = APIConstants.APP_ACTION_LIVE_PROGRAM_LIST;
                    }
                    args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
                    args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
                    CacheManager.setCarouselInfoData(carouselInfoData);

                    args.putBoolean(FragmentVODList.PARAM_ENABLE_FILTER, false);
                    ((MainActivity) mContext).pushFragment(FragmentVODList.newInstance(args));
                    break;
                case R.id.moviesMore:

                    args.putBoolean(FragmentVODList.PARAM_ENABLE_FILTER, false);
                    carouselInfoData.title = "Movies";
                    args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
                    args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
                    args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE,APIConstants.TYPE_MOVIE);
                    CacheManager.setCarouselInfoData(carouselInfoData);

                    ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
                    break;
                case R.id.tvShowsMore:
                    carouselInfoData.title = "TV Shows";
                    args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
                    args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
                    args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE,APIConstants.TYPE_TVSERIES);
                    CacheManager.setCarouselInfoData(carouselInfoData);
                    args.putBoolean(FragmentVODList.PARAM_ENABLE_FILTER, false);
                    ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
                    break;
            }
        }
    };

    public void showProgressBar() {

        if (mProgressBar == null) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);

    }

    public void dismissProgressBar() {
        if (mProgressBar == null) {
            return;
        }
        mProgressBar.setVisibility(View.GONE);
    }
    // Displays error Message when content doesn't load
    private void checkOtherLayouts() {
        if(!didLiveTVLoad && !didMoviesLoad && !didTVShowsLoad){
            errorMessage.setText(mContext.getResources().getString(R.string.canot_connect_server));
        }

    }




}
