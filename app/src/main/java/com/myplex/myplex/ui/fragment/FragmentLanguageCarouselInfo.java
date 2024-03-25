package com.myplex.myplex.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.R;
import com.myplex.myplex.model.recyclerViewScrollListener;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.views.BannerHorizontalItem3D;
import com.myplex.myplex.ui.views.BigWeeklyTrendingItemNew;
import com.myplex.myplex.ui.views.PortraitBannerPlayerItem;
import com.myplex.myplex.ui.views.PortraitViewPagerItem;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WrapperLinearLayoutManager;
import com.myplex.util.SDKUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.RenderScriptBlur;

/**
 * Created by Apalya on 12/3/2015.
 */
public class FragmentLanguageCarouselInfo extends BaseFragment implements AdapterCarouselInfo.CallbackListener {

    private static final String TAG = FragmentLanguageCarouselInfo.class.getSimpleName();
    private RecyclerView mRecyclerViewCarouselInfo;
    private AdapterCarouselInfo mAdapterCarouselInfo;
    private recyclerViewScrollListener mViewScrollListener;
    private WrapperLinearLayoutManager layoutManager;
    private String mMenuGroup = "searchLanguages5x";
    private String mPageTitle = "SearchLanguages";
    private ProgressBar mProgressBar;
//    private LinearLayout closeBtn;
    private List<CarouselInfoData> mListCarouselInfo;
    private SwipeRefreshLayout mSwipeToRefreshHome;
    private int mStartIndex = 1;
    private TextView no_data_text;
    public boolean isPullRefresh = false;
    private RelativeLayout mLayoutRetry;
    private ImageView mImageViewRetry;
    CircleImageView fab_top_scroll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_language_list, container, false);

        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mRecyclerViewCarouselInfo = rootView.findViewById(R.id.recyclerview);
        mSwipeToRefreshHome =  rootView.findViewById(R.id.swipe_to_refresh_home);
        mProgressBar = rootView.findViewById(R.id.loading_progress);
//        closeBtn = rootView.findViewById(R.id.close_btn);
        no_data_text = rootView.findViewById(R.id.no_data_text);
        mLayoutRetry = rootView.findViewById(R.id.retry_layout);
        mImageViewRetry = rootView.findViewById(R.id.imageview_error_retry);
        fab_top_scroll=rootView.findViewById(R.id.fab_top_scroll);
        mSwipeToRefreshHome =  rootView.findViewById(R.id.swipe_to_refresh_home);
        fab_top_scroll.setVisibility(GONE);

        mRecyclerViewCarouselInfo.setNestedScrollingEnabled(false);
        //  mRecyclerViewCarouselInfo.addItemDecoration(new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_2)));
        mRecyclerViewCarouselInfo.setItemAnimator(null);
        final SingleScrollDirectionEnforcer enforcer = new SingleScrollDirectionEnforcer();
        mRecyclerViewCarouselInfo.addOnItemTouchListener(enforcer);
        mRecyclerViewCarouselInfo.addOnScrollListener(enforcer);
//        initAdView();
        mAdapterCarouselInfo = new AdapterCarouselInfo(mContext, loadDummyInfo());
        mAdapterCarouselInfo.setCallBackListener(this);
        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
        layoutManager = new WrapperLinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mRecyclerViewCarouselInfo.setLayoutManager(layoutManager);
        layoutManager.setMeasurementCacheEnabled(false);
       /* int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
        SDKLogger.debug("margin: " + margin);*/
        VerticalSpaceItemDecoration mVerticalSpaceItemDecoration = new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.carousel_gap));
        mRecyclerViewCarouselInfo.removeItemDecoration(mVerticalSpaceItemDecoration);
        mRecyclerViewCarouselInfo.addItemDecoration(mVerticalSpaceItemDecoration);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(getActivity(), R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
          mRecyclerViewCarouselInfo.addOnScrollListener(mOnScrollListener);

        loadCarouselInfo();
        mSwipeToRefreshHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPullRefresh = true;
                mAdapterCarouselInfo = new AdapterCarouselInfo(mContext, loadDummyInfo());
                mAdapterCarouselInfo.setCallBackListener(FragmentLanguageCarouselInfo.this);
                mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
                //   mAdapterCarouselInfo.setCarouselInfoData(mListCarouselInfo);
                loadCarouselInfo();
//                if( ((MainActivity)requireActivity())!=null)
//                    ((MainActivity)requireActivity()).loadNotification();
                if(BannerHorizontalItem3D.bannerHorizontalItem3D != null) {
                    BannerHorizontalItem3D.bannerHorizontalItem3D.stopScroll();
                }
                if(BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew != null) {
                    BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew.stopScroll();
                }

            }
        });
        fab_top_scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerViewCarouselInfo.getLayoutManager().smoothScrollToPosition(mRecyclerViewCarouselInfo,new RecyclerView.State(), 0);
                showTopNavigateArrow(false);
                //((MainActivity)requireActivity()).updateBottomBar(true);
            //    ((MainActivity) requireActivity()).blurlayout_toolbar.setVisibility(GONE);

            }
        });
        if(BannerHorizontalItem3D.bannerHorizontalItem3D != null) {
            BannerHorizontalItem3D.bannerHorizontalItem3D.stopScroll();
        }
        if(BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew != null) {
            BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew.stopScroll();
        }

        eightbitlab.com.blurview.BlurView blurlayout = rootView.findViewById(R.id.blurLayout);

        View decorView = getActivity().getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView1 = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();
        blurlayout.setupWith(rootView1)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(mContext))
                .setBlurRadius(15f)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);



        blurlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   if (mBaseActivity instanceof MainActivity) {
                    ((MainActivity) mBaseActivity).removeFilterFragment();
                }
                mBaseActivity.removeFragment(FragmentLanguageCarouselInfo.this);*/
                if (mBaseActivity instanceof MainActivity) {
                    ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);
                    ((MainActivity) mBaseActivity).onBackPressed();
                    ((MainActivity)requireActivity()).updateBottomBar(true, 0);
                }

            }
        });
        return rootView;
    }

    public static FragmentLanguageCarouselInfo newInstance(Bundle args) {
        FragmentLanguageCarouselInfo fragmentCarouselViewAll = new FragmentLanguageCarouselInfo();
        fragmentCarouselViewAll.setArguments(args);
        return fragmentCarouselViewAll;
    }

    @Override
    public boolean isPageVisible() {
        return false;
    }


    private void loadCarouselInfo() {
        if (mMenuGroup == null) return;

        showProgressBar();
        new MenuDataModel().fetchMenuList(mMenuGroup, mStartIndex, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                mSwipeToRefreshHome.setRefreshing(false);
                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }

                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                mSwipeToRefreshHome.setRefreshing(false);
                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }

                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                dismissProgressBar();
                mSwipeToRefreshHome.setRefreshing(false);
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
                if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                    showRetryOption(true);
                }
            }

        });


    }

    private void updateCarouselInfo() {
        if (!isAdded()) {
            return;
        }
        mRecyclerViewCarouselInfo.setVisibility(View.VISIBLE);
        mAdapterCarouselInfo.setCarouselInfoData(mListCarouselInfo);
        mAdapterCarouselInfo.setMenuGroupName(mMenuGroup, mPageTitle);


    }

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

    private class SingleScrollDirectionEnforcer extends RecyclerView.OnScrollListener implements RecyclerView.OnItemTouchListener {
        private int scrollState = RecyclerView.SCROLL_STATE_IDLE;
        private int scrollPointerId = -1;
        private int initialTouchX;
        private int initialTouchY;
        private int dx;
        private int dy;

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            final int action = e.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    scrollPointerId = e.getPointerId(0);
                    initialTouchX = (int) (e.getX() + 0.5f);
                    initialTouchY = (int) (e.getY() + 0.5f);
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    final int actionIndex = e.getActionIndex();
                    scrollPointerId = e.getPointerId(actionIndex);
                    initialTouchX = (int) (e.getX(actionIndex) + 0.5f);
                    initialTouchY = (int) (e.getY(actionIndex) + 0.5f);
                    break;

                case MotionEvent.ACTION_MOVE: {
                    final int index = e.findPointerIndex(scrollPointerId);
                    if (index >= 0 && scrollState != RecyclerView.SCROLL_STATE_DRAGGING) {
                        final int x = (int) (e.getX(index) + 0.5f);
                        final int y = (int) (e.getY(index) + 0.5f);
                        dx = x - initialTouchX;
                        dy = y - initialTouchY;
                    }
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            int oldState = scrollState;
            scrollState = newState;
            if (oldState == RecyclerView.SCROLL_STATE_IDLE && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    final boolean canScrollHorizontally = layoutManager.canScrollHorizontally();
                    final boolean canScrollVertically = layoutManager.canScrollVertically();
                    if (canScrollHorizontally != canScrollVertically) {
                        if (canScrollHorizontally && Math.abs(dy) > Math.abs(dx)) {
                            recyclerView.stopScroll();
                        }
                        if (canScrollVertically && Math.abs(dx) > Math.abs(dy)) {
                            recyclerView.stopScroll();
                        }
                    }
                }
            }
        }

    }


    private List<CarouselInfoData> loadDummyInfo() {
        List<CarouselInfoData> carouselInfoList = new ArrayList<>();
        for (int i = 0; i < 0; i++) {
            CarouselInfoData carouselInfo = new CarouselInfoData();
            carouselInfo.title = mContext.getResources().getString(R.string.no_info_available);
            if (i % 2 == 0) {
                carouselInfo.layoutType = APIConstants.LAYOUT_TYPE_BANNER;
            }/* else if (i == 3) {
                carouselInfo.layoutType = APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM;
            }*/ else {
                carouselInfo.layoutType = APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM;
            }
            carouselInfoList.add(carouselInfo);
        }
        return carouselInfoList;
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
           /* int firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();
            if(firstVisibleItem <= -1 || firstVisibleItem<=1 &&ApplicationController.IS_FROME_HOME){
                     ((MainActivity) mContext).showToolbar();
                    ((MainActivity) mContext).showSystemUI();
            }*/
            if (!Util.doesCurrentTabHasPortraitBanner(mMenuGroup)) {
                View childView = recyclerView.getChildAt(0);
                if(childView!=null){
                    RecyclerView.ViewHolder portraitViewHolder = recyclerView.getChildViewHolder(childView);
                    if (portraitViewHolder instanceof PortraitBannerPlayerItem
                        || portraitViewHolder instanceof PortraitViewPagerItem) {
                     Util.addTabsThatHavePortraitBanner(mMenuGroup);
                     int recOffset = recyclerView.computeVerticalScrollOffset();
                     if (mViewScrollListener != null) {
                        mViewScrollListener.onViewScrolled(recOffset);
                          }
                    }
                 }
            } else {
                int recOffset = recyclerView.computeVerticalScrollOffset();
                if (mViewScrollListener != null) {
                    mViewScrollListener.onViewScrolled(recOffset);
                }

            }
            if (dy < 0) {
                if (mViewScrollListener != null) {
                    mViewScrollListener.onViewScrolledUp();
                }
            }
            if (dy > 0) {
               /* if(ApplicationController.IS_FROME_HOME) {
                    ((MainActivity) mContext).hideToolbar();
                ((MainActivity) mContext).hideStatusBar();
                }else{
//                    ((MainActivity) mContext).showToolbar();
                    ((MainActivity) mContext).showSystemUI();
                }*/

                if (mViewScrollListener != null) {
                    mViewScrollListener.onViewScrolledDown();
                }
            }
            if (!recyclerView.canScrollVertically(1)) {
                if (mViewScrollListener != null) {
                    mViewScrollListener.onScrolledToEnd();
                }
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            LinearLayoutManager myLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int startScrollPosition = myLayoutManager.findFirstVisibleItemPosition();
            int endScrollPosition = myLayoutManager.findLastVisibleItemPosition();

            showTopNavigateArrow((endScrollPosition >= 5));
            try {
                if (endScrollPosition > mLastFirstVisibleItem) {
                   // ((MainActivity)requireActivity()).updateBottomBar(false);
                    Log.i("a", "scrolling down...");
                } else if (endScrollPosition < mLastFirstVisibleItem) {
                    //((MainActivity)requireActivity()).updateBottomBar(true);
                    ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);
                    Log.i("a", "scrolling up...");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mLastFirstVisibleItem = endScrollPosition;
        }
        int mLastFirstVisibleItem;
    };

    public void setViewScrollListener(recyclerViewScrollListener mViewScrollListener) {
        this.mViewScrollListener = mViewScrollListener;
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

    private void showTopNavigateArrow(boolean isShow){
        if(isShow){
            fab_top_scroll.setVisibility(VISIBLE);
        }else {
            fab_top_scroll.setVisibility(GONE);
        }
    }

}