package com.myplex.myplex.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.OTTAppRequest;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.OTTApp;
import com.myplex.model.OTTAppData;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.EventNetworkConnectionChange;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.adapter.AdapterMoviesCarousel2;
import com.myplex.myplex.ui.adapter.OTTAppsImageSliderAdapter;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.UiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apalya on 12/3/2015.
 */
public class FragmentMoviesCarousel2 extends BaseFragment implements AdapterCarouselInfo.CallbackListener {
    public static final String PARAM_APP_FRAG_TYPE = "fragment_type";
    public static final int PARAM_APP_FRAG_MOVIE = 0;
    public static final int PARAM_APP_FRAG_MUSIC = 1;
    private static final String TAG = FragmentMoviesCarousel2.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecyclerViewCarouselInfo;
    private AdapterMoviesCarousel2 mAdapterMoviesCarousel;
    private List<CarouselInfoData> mListMovieCarousel;
    private TextView mTextViewErrorRetryAgain;
    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loadCarouselInfo();
            showRetryOption(false);
        }
    };
    private RelativeLayout mLayoutRetry;
    private ImageView mImageViewRetry;
    private ProgressBar mProgressBar;
    private CarouselInfoData mCarouselInfoData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_single_recycler, container, false);
        rootView.findViewById(R.id.toolbar).setVisibility(View.GONE);
        mRecyclerViewCarouselInfo =  rootView.findViewById(R.id.recyclerview);
        mTextViewErrorRetryAgain =  rootView.findViewById(R.id.textview_error_retry);
        mLayoutRetry =  rootView.findViewById(R.id.retry_layout);
        mImageViewRetry =  rootView.findViewById(R.id.imageview_error_retry);
        mProgressBar =  rootView.findViewById(R.id.loading_progress);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(getActivity(), R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mLayoutRetry.setVisibility(View.GONE);
        mImageViewRetry.setOnClickListener(mRetryClickListener);
        mTextViewErrorRetryAgain.setOnClickListener(mRetryClickListener);
        mRecyclerViewCarouselInfo.setItemAnimator(null);
        Bundle args = getArguments();
        mCarouselInfoData = CacheManager.getCarouselInfoData();
        mAdapterMoviesCarousel = new AdapterMoviesCarousel2(mContext, loadDummyInfo());
        mAdapterMoviesCarousel.setCarouselInfoDataSection(mCarouselInfoData);

        mAdapterMoviesCarousel.setCallBackListener(this);
        mRecyclerViewCarouselInfo.setAdapter(mAdapterMoviesCarousel);
        mRecyclerViewCarouselInfo.addItemDecoration(new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_2)));
        mRecyclerViewCarouselInfo.setHasFixedSize(true);
        loadCarouselInfo();
        mRecyclerViewCarouselInfo.setLayoutManager(new LinearLayoutManager(mContext));
        args.clear();
        return rootView;
    }

    private List loadDummyInfo() {
        List<CarouselInfoData> movieCarouselInfoList = new ArrayList<>();
        for (int i = 0; i < 0; i++) {
            CarouselInfoData carouselInfo = new CarouselInfoData();
            carouselInfo.title = mContext.getResources().getString(R.string.no_info_available);
            movieCarouselInfoList.add(carouselInfo);
        }
        return movieCarouselInfoList;
    }

    private void loadCarouselInfo() {
        //Content list call
        showProgressBar();
        new MenuDataModel().fetchMenuList(APIConstants.TYPE_MOVIE, 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                if (dataList == null) {
                    return;
                }
                mListMovieCarousel = dataList;
                if (mListMovieCarousel.size() > 0) {
                    updateCarouselInfo();
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListMovieCarousel.size());
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                if (dataList == null) {
                    return;
                }
                mListMovieCarousel = dataList;
                if (mListMovieCarousel.size() > 0) {
                    updateCarouselInfo();
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListMovieCarousel.size());
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                dismissProgressBar();
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
                if (mListMovieCarousel == null || mListMovieCarousel.isEmpty()) {
                    showRetryOption(true);
                }
            }

        });

    }

    private void updateCarouselInfo() {
        if (!isAdded()) {
            return;
        }
//        mListMovieCarousel.addAll(mListMovieCarousel);
        mAdapterMoviesCarousel.setCarouselInfoData(mListMovieCarousel);
        mRecyclerViewCarouselInfo.setAdapter(mAdapterMoviesCarousel);
//        mRecyclerViewCarouselInfo.addOnScrollListener(Util.getSrollListenerForPicasso(mContext));

        fetchData(APIConstants.TYPE_MOVIE);
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity
                .SECTION_MOVIES));
        if (menuVisible) {
            //Log.d(TAG, "setMenuVisibility() from movies- " + menuVisible);
            Analytics.gaBrowse(Analytics.TYPE_MOVIES, 1l);
            AppsFlyerTracker.eventBrowseTab(Analytics.TYPE_MOVIES);
            if (mAdapterMoviesCarousel != null) {
                mAdapterMoviesCarousel.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");
    }


    private void fetchData(final String contentType) {

        final List<OTTAppsImageSliderAdapter.SliderModel> sliderItems = new ArrayList<>();

        OTTAppRequest.Params ottAppreqParams = new OTTAppRequest.Params(contentType);
        OTTAppRequest ottAppRequest = new OTTAppRequest(ottAppreqParams, new APICallback<OTTAppData>() {
            @Override
            public void onResponse(APIResponse<OTTAppData> response) {
                if (response == null || response.body() == null || response.body().results == null)
                    return;
                List<OTTApp> mAppsList = response.body().results;

                if (mAppsList == null) return;

                if (mAdapterMoviesCarousel != null)
                    mAdapterMoviesCarousel.setOttAppsList(mAppsList);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(ottAppRequest);
    }

    public void onEventMainThread(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        //Log.d(TAG, "onEventMainThread: mAdapterMoviesCarousel is null- " + (mAdapterMoviesCarousel == null) + " mMenuGroupName- " + " Movies " + " isMenuVisible- " + isMenuVisible());
        if (mAdapterMoviesCarousel != null && isMenuVisible()) {
            mAdapterMoviesCarousel.mOnItemClickListenerMovies.onClick(null, EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA, -1, searchMovieDataOnOTTApp.getMovieData());
            mAdapterMoviesCarousel.setSearchMoviedata(searchMovieDataOnOTTApp);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mAdapterMoviesCarousel != null) {
                mRecyclerViewCarouselInfo.getAdapter().notifyDataSetChanged();
//                mAdapterMoviesCarousel.notifyDataSetChanged();
//                mAdapterMoviesCarousel.onFullScreen(true);
            }
        } else {
            if (mAdapterMoviesCarousel != null) {
                mRecyclerViewCarouselInfo.getAdapter().notifyDataSetChanged();
//                mAdapterMoviesCarousel.notifyDataSetChanged();
//                mAdapterMoviesCarousel.onFullScreen(false);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void onEventMainThread(EventNetworkConnectionChange event) {
        /*if(mListMovieCarousel == null && event.isConnected()){
            loadCarouselInfo();
        }*/
    }

    private void showRetryOption(boolean b) {
        if (b) {
            mRecyclerViewCarouselInfo.setVisibility(View.GONE);
            mLayoutRetry.setVisibility(View.VISIBLE);
            return;
        }
        mRecyclerViewCarouselInfo.setVisibility(View.VISIBLE);
        mLayoutRetry.setVisibility(View.GONE);
    }


    public void showProgressBar() {

        if (mProgressBar == null) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void dismissProgressBar() {
        if (mProgressBar == null) {
            return;
        }
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SubscriptionWebActivity.SUBSCRIPTION_REQUEST) {
            if (mAdapterMoviesCarousel != null) {
                mAdapterMoviesCarousel.onActivityResult(requestCode, resultCode, data);
            }
//        }
    }

    @Override
    public boolean isPageVisible() {
        return getUserVisibleHint();
    }
}
