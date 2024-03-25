package com.myplex.myplex.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.myplex.myplex.ui.adapter.AdapterCarouselInfo.ITEM_TYPE_SMALL_NATIVE_AD;
import static com.myplex.myplex.ui.adapter.AdapterCarouselInfo.ITEM_TYPE_VMAX_IMAGE_ADVERTISE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.EventNetworkConnectionChange;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.events.MediaPageVisibilityEvent;
import com.myplex.myplex.events.RefreshPotraitUI;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.recyclerViewScrollListener;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.HomePagerAdapterDynamicMenu;
import com.myplex.myplex.ui.adapter.TabsNestedAdapter;
import com.myplex.myplex.ui.views.BannerHorizontalItem3D;
import com.myplex.myplex.ui.views.BigWeeklyTrendingItemNew;
import com.myplex.myplex.ui.views.PortraitBannerPlayerItem;
import com.myplex.myplex.ui.views.PortraitViewPagerItem;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WrapperLinearLayoutManager;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Srikanth on 12/3/2015.
 */
public class FragmentCarouselInfo extends BaseFragment implements AdapterCarouselInfo.CallbackListener {
    public static final String PARAM_APP_FRAG_TYPE = "fragment_type";
    private static final String TAG = FragmentCarouselInfo.class.getSimpleName();
    public static final String PARAM_CAROUSAL = "carouselInfoData";
    public static final String PARAM_APP_PAGE_TITLE = "fragment_page_title";
    public static final String PARAM_ANALYTICS_CAROUSAL_SOURCE = "carousal_title";
    public static final String PARAM_ANALYTICS_CAROUSAL_SOURCE_NAVIGANTION = "navigation menu";
    public static final String PARAM_GENRE = "genre";
    public static final String PARAM_IS_GENRE_ONLY = "is_genre_only";
    public static final String PARAM_SHOW_TOOLBAR = "show_toolbar";
    public boolean isPullRefresh = false;
    private HomePagerAdapterDynamicMenu mHomePagerAdapterDynamicMenu;
    private boolean isMediaPageVisible;
    private recyclerViewScrollListener mViewScrollListener;


    public void setHomePagerAdapterDynamicMenu(HomePagerAdapterDynamicMenu homePagerAdapterDynamicMenu) {
        mHomePagerAdapterDynamicMenu = homePagerAdapterDynamicMenu;
    }
	
    private Context mContext;
    private RecyclerView mRecyclerViewCarouselInfo;
    private SwipeRefreshLayout mSwipeToRefreshHome;
    private AdapterCarouselInfo mAdapterCarouselInfo;
    private List<CarouselInfoData> mListCarouselInfo;
    private int mStartIndex = 1;
    private String mMenuGroup;
    private TextView mTextViewErrorRetryAgain;
    private TextView no_data_text;
    private WrapperLinearLayoutManager layoutManager;
    private LinearLayout tabsLinearLayout;
    private TabLayout nestedTabsLayout;
    private ViewPager2 viewPager2;
    private RelativeLayout mainLayout;

    CircleImageView fab_top_scroll;

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
    private String mPageTitle;
    private CarouselInfoData mCarouselInfoData;
    Toolbar mToolbar;

    @Override
    public void onDetach() {
        super.onDetach();
        ScopedBus.getInstance().unregister(this);
        destroyVmaxAdObject();
        destroyGoogleAdObject();

    }

    public void destroyVmaxAdObject(){
        try {
            if (mAdapterCarouselInfo != null) {
                int numberOfItems = mAdapterCarouselInfo.getItemCount();
                if (numberOfItems > 0) {
                    for (int i = 0; i < numberOfItems; i++) {
                        int itemType = mAdapterCarouselInfo.getItemViewType(i);
                        if (itemType == AdapterCarouselInfo.ITEM_TYPE_VMAX_VIDEO_ADVERTISE || itemType == ITEM_TYPE_VMAX_IMAGE_ADVERTISE) {
                            Log.e("VMAX", "VMAX object found at " + i);
                            if (layoutManager != null) {



                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void destroyGoogleAdObject(){
        try {
            if (mAdapterCarouselInfo != null) {
                int numberOfItems = mAdapterCarouselInfo.getItemCount();
                if (numberOfItems > 0) {
                    for (int i = 0; i < numberOfItems; i++) {
                        int itemType = mAdapterCarouselInfo.getItemViewType(i);
                        if (itemType == AdapterCarouselInfo.ITEM_TYPE_MEDIUM_NATIVE_AD || itemType == ITEM_TYPE_SMALL_NATIVE_AD) {
                            Log.e(TAG, "Admob object found at " + i);
                            if (layoutManager != null) {

                                TemplateView templateView= (TemplateView) layoutManager.getChildAt(itemType);
                                if (templateView!=null){
                                    Log.e(TAG, "GoogleAdView " + templateView.getId());
                                    templateView.destroyNativeAd();
                                }

                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ScopedBus.getInstance().register(this);
        SDKLogger.debug("context:" + context);
    }

    public  void setCarouselInfoData(CarouselInfoData carouselInfoData) {
        mCarouselInfoData = carouselInfoData;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_carouselinfo, container, false);
        ScopedBus.getInstance().register(this);
        mRecyclerViewCarouselInfo =  rootView.findViewById(R.id.recyclerview);
        mSwipeToRefreshHome =  rootView.findViewById(R.id.swipe_to_refresh_home);
        mTextViewErrorRetryAgain =  rootView.findViewById(R.id.textview_error_retry);
        mLayoutRetry =  rootView.findViewById(R.id.retry_layout);
        mImageViewRetry =  rootView.findViewById(R.id.imageview_error_retry);
        mProgressBar =  rootView.findViewById(R.id.loading_progress);
        viewPager2=rootView.findViewById(R.id.nested_carousels_viewpager);
        tabsLinearLayout=rootView.findViewById(R.id.tabs_layout);
        nestedTabsLayout=rootView.findViewById(R.id.nested_carousels_tab_layout);
        mainLayout=rootView.findViewById(R.id.main_layout);
        no_data_text=rootView.findViewById(R.id.no_data_text);
        fab_top_scroll=rootView.findViewById(R.id.fab_top_scroll);
        fab_top_scroll.setVisibility(GONE);
        mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(getActivity(), R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mLayoutRetry.setVisibility(View.GONE);
        mImageViewRetry.setOnClickListener(mRetryClickListener);
        mTextViewErrorRetryAgain.setOnClickListener(mRetryClickListener);
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
        Bundle args = getArguments();
        if (args != null && args.containsKey(PARAM_APP_FRAG_TYPE)) {
            mMenuGroup = args.getString(PARAM_APP_FRAG_TYPE);
        }
        if (args != null && args.containsKey(PARAM_APP_PAGE_TITLE)) {
            mPageTitle = args.getString(PARAM_APP_PAGE_TITLE);
        }

        mCarouselInfoData = CacheManager.getCarouselInfoData();
        if (args != null && args.containsKey(PARAM_CAROUSAL) && args.getSerializable(PARAM_CAROUSAL)!= null) {
            mCarouselInfoData =(CarouselInfoData) args.getSerializable(PARAM_CAROUSAL);
        }
        if (mCarouselInfoData != null
                && args!= null
                && args.containsKey(PARAM_SHOW_TOOLBAR)
                && args.getBoolean(PARAM_SHOW_TOOLBAR, false)) {
            showToolbar(rootView);
        }
        mAdapterCarouselInfo.setCarouselInfoDataSection(mCarouselInfoData);
        if(mCarouselInfoData!=null&&mCarouselInfoData.bgColor!=null){
            mAdapterCarouselInfo.setBgColor(mCarouselInfoData.bgColor);
        }
        loadCarouselInfo();
        layoutManager = new WrapperLinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mRecyclerViewCarouselInfo.setLayoutManager(layoutManager);
        layoutManager.setMeasurementCacheEnabled(false);
       /* int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
        SDKLogger.debug("margin: " + margin);*/
        VerticalSpaceItemDecoration mVerticalSpaceItemDecoration = new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.carousel_gap));
        mRecyclerViewCarouselInfo.removeItemDecoration(mVerticalSpaceItemDecoration);
        mRecyclerViewCarouselInfo.addItemDecoration(mVerticalSpaceItemDecoration);
        mRecyclerViewCarouselInfo.addOnScrollListener(mOnScrollListener);
        if(mCarouselInfoData!=null&&mCarouselInfoData.bgColor!=null&&!TextUtils.isEmpty(mCarouselInfoData.bgColor)){
            mainLayout.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            mRecyclerViewCarouselInfo.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            nestedTabsLayout.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
        }

        mSwipeToRefreshHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPullRefresh = true;
                mAdapterCarouselInfo = new AdapterCarouselInfo(mContext, loadDummyInfo());
                mAdapterCarouselInfo.setCallBackListener(FragmentCarouselInfo.this);
                mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
             //   mAdapterCarouselInfo.setCarouselInfoData(mListCarouselInfo);
                myplexAPI.clearCache(APIConstants.BASE_URL);
                myplexAPISDK.ENABLE_FORCE_CACHE = true;
                MenuDataModel.clearCache();
                loadCarouselInfo();
               if( ((MainActivity)requireActivity())!=null)
                ((MainActivity)requireActivity()).loadNotification();
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
               // mRecyclerViewCarouselInfo.scrollToPosition(0);
                mRecyclerViewCarouselInfo.getLayoutManager().smoothScrollToPosition(mRecyclerViewCarouselInfo,new RecyclerView.State(), 0);
                showTopNavigateArrow(false);
                ((MainActivity)requireActivity()).updateBottomBar(true, 0);
                ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);

            }
        });


        return rootView;
    }

    public void startToScroll() {
        if (mRecyclerViewCarouselInfo != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerViewCarouselInfo.scrollToPosition(0);
                    fab_top_scroll.setVisibility(GONE);
                }
            }, 200);

        }
    }


    public void  setTabPosition(String name){
        if(mListCarouselInfo != null) {
            for (int i = 0; i < mListCarouselInfo.size(); i++) {
                if (mListCarouselInfo.get(i).name.equalsIgnoreCase(name)) {
                    viewPager2.setCurrentItem(i);
                    TabLayout.Tab tab = nestedTabsLayout.getTabAt(i);
                    tab.select();
                }
            }
        }
    }
    private void showToolbar(View rootView) {
        mToolbar = rootView.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
     /*   ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) mRecyclerViewCarouselInfo.getLayoutParams();
        marginLayoutParams.setMargins(0, mToolbar.getLayoutParams().height, 0, 0);
        mRecyclerViewCarouselInfo.setLayoutParams(marginLayoutParams);*/
        if (mCarouselInfoData != null)
            ((TextView) mToolbar.findViewById(R.id.toolbar_header_title)).setText(mCarouselInfoData.title);
        mToolbar.findViewById(R.id.toolbar_settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mToolbar.findViewById(R.id.action_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    //((MainActivity)requireActivity()).setCarouselInfoData(mCarouselInfoData);
                    ((MainActivity) getActivity()).showSearch(mCarouselInfoData.shortDesc);
                }
            }
        });

        if(mCarouselInfoData.bgColor!=null&&!TextUtils.isEmpty(mCarouselInfoData.bgColor)){
            mToolbar.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            ((ImageView) mToolbar.findViewById(R.id.toolbar_settings_button)).setImageResource(R.drawable.player_back_icon);
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

    private void loadCarouselInfo() {
        if (mMenuGroup == null) return;

        showProgressBar();
        new MenuDataModel().fetchMenuList(mMenuGroup, mStartIndex, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }
                mSwipeToRefreshHome.setRefreshing(false);
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                dismissProgressBar();
                if (dataList == null) {
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    updateCarouselInfo();
                } else {
                    no_data_text.setVisibility(View.VISIBLE);
                }
                mSwipeToRefreshHome.setRefreshing(false);
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                dismissProgressBar();
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
                if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                    showRetryOption(true);
                    mSwipeToRefreshHome.setRefreshing(false);
                }
            }

        });


    }

    private void updateCarouselInfo() {
        if (!isAdded()) {
            return;
        }
      /*  if(mMenuGroup.equals(MENU_HOME)){
            ViewGroup.MarginLayoutParams marginLayoutParams =
                    (ViewGroup.MarginLayoutParams) mRecyclerViewCarouselInfo.getLayoutParams();
            marginLayoutParams.setMargins(0, 0, 0, (int)getResources().getDimension(R.dimen.margin_gap_64));
            mRecyclerViewCarouselInfo.setLayoutParams(marginLayoutParams);
        } else {
            ViewGroup.MarginLayoutParams marginLayoutParams =
                    (ViewGroup.MarginLayoutParams) mRecyclerViewCarouselInfo.getLayoutParams();
            marginLayoutParams.setMargins(0, 0, 0, 0);
            mRecyclerViewCarouselInfo.setLayoutParams(marginLayoutParams);
        }*/

        if(checkIsNestedTabLayout(mListCarouselInfo)){
            mRecyclerViewCarouselInfo.setVisibility(View.GONE);
            tabsLinearLayout.setVisibility(View.VISIBLE);
            setTabsData(mListCarouselInfo);
        }else {
            mRecyclerViewCarouselInfo.setVisibility(View.VISIBLE);
            tabsLinearLayout.setVisibility(View.GONE);
            mAdapterCarouselInfo.setCarouselInfoData(mListCarouselInfo);
           //mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
            mAdapterCarouselInfo.setMenuGroupName(mMenuGroup, mPageTitle);
        }

    }

    public static FragmentCarouselInfo newInstance(Bundle args) {
        FragmentCarouselInfo fragmentCarouselInfo = new FragmentCarouselInfo();
        fragmentCarouselInfo.setArguments(args);
        return fragmentCarouselInfo;
    }

    private void setTabsData(List<CarouselInfoData> mListCarouselInfo) {
        TabsNestedAdapter nestedTabsAdapter=new TabsNestedAdapter(this);
        nestedTabsAdapter.setTabsData(mListCarouselInfo,mContext);
        viewPager2.setAdapter(nestedTabsAdapter);
        if (mListCarouselInfo != null && mListCarouselInfo.size() > 1){
            viewPager2.setOffscreenPageLimit(mListCarouselInfo.size()-1);
        }else {
            viewPager2.setOffscreenPageLimit(1);
        }
        viewPager2.setUserInputEnabled(false);
        new TabLayoutMediator(nestedTabsLayout, viewPager2,
                (tab, position) -> tab.setText(robotoStringFont(mListCarouselInfo.get(position).title))
        ).attach();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(isVisible()&&mListCarouselInfo!=null&&!mListCarouselInfo.isEmpty()){
                    sendPageChangeListener(mListCarouselInfo.get(position).name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    private boolean checkIsNestedTabLayout(List<CarouselInfoData> mListCarouselInfo){
        return mListCarouselInfo.get(0).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (getArguments() != null && getArguments().containsKey(PARAM_APP_FRAG_TYPE)) {
            mMenuGroup = getArguments().getString(PARAM_APP_FRAG_TYPE);
        }

        //Log.d(TAG, "setMenuVisibility() from " + menuVisible + " mMenuGroup: " + mMenuGroup);
        if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(mMenuGroup)) {
            ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity.SECTION_TVSHOWS));
            if (menuVisible) {
                Analytics.gaBrowse(APIConstants.TYPE_VODCHANNEL, 1L);
                AppsFlyerTracker.eventBrowseTab(Analytics.TYPE_TVSHOWS);
            }
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
            ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity.SECTION_VIDEOS));
            if (menuVisible) {
                Analytics.gaBrowse(APIConstants.TYPE_VODCATEGORY, 1L);
                AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_PAGE_VIDEOS);
            }
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
            ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity.SECTION_MUSIC_VIDEOS));
            if (menuVisible) {
//                Analytics.createScreenGA(Analytics.SCREEN_MUSIC_VIDEOS);
                Analytics.gaBrowse(APIConstants.TYPE_MUSIC_VIDEO, 1L);
                AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_PAGE_MUSIC_VIDEOS);
            }
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup)) {
            ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity.SECTION_KIDS));
            if (menuVisible) {
//                Analytics.createScreenGA(Analytics.SCREEN_KIDS);
                FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext,Analytics.SCREEN_KIDS);
                Analytics.gaBrowse(APIConstants.TYPE_MOVIE, 1L);
                AppsFlyerTracker.eventBrowseTab(HomePagerAdapter.getPageKids());
            }
        } else if (menuVisible && !TextUtils.isEmpty(mPageTitle)) {
//            Analytics.createScreenGA(mPageTitle.toLowerCase());
            FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,mPageTitle.toLowerCase());
            Analytics.gaBrowse(mPageTitle.toLowerCase(), 1L);
            AppsFlyerTracker.eventBrowseTab(mPageTitle.toLowerCase());
        }
        if (menuVisible) {
            if (mAdapterCarouselInfo != null) {
              //  mAdapterCarouselInfo.notifyDataSetChanged();
            }
        }
        /*if(mListCarouselInfo == null && menuVisible){
            loadCarouselInfo();
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if(BannerHorizontalItem3D.bannerHorizontalItem3D != null) {
            BannerHorizontalItem3D.bannerHorizontalItem3D.startScroll();
        }
        if(BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew != null) {
            BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew.startScroll();
        }

        /*int firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();
        if(firstVisibleItem <= -1 || firstVisibleItem==0 && ApplicationController.IS_FROME_HOME){
            ((MainActivity) mContext).showToolbar();
            ((MainActivity) mContext).showSystemUI();
        }else{
            ((MainActivity) mContext).hideToolbar();

        }*/
        //Log.d(TAG, "onResume()");

    }


    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");

    }
    int i =0;
    public void onEventMainThread(RefreshPotraitUI event) {
        Log.e("RefreshUI", "Refrshed");
        int totalView = mAdapterCarouselInfo.getItemCount();

        for (; i < totalView; i++) {
            Log.e("RefreshUI", "Refrshed");
            if (APIConstants.LAYOUT_TYPE_PORTRAIT_BANNER.equalsIgnoreCase(mListCarouselInfo.get(i).layoutType)
                    || APIConstants.LAYOUT_TYPE_SQUARE_BANNER.equalsIgnoreCase(mListCarouselInfo.get(i).layoutType)) {
                if (mHomePagerAdapterDynamicMenu != null) {
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded() && mHomePagerAdapterDynamicMenu != null) {
                                mAdapterCarouselInfo.notifyItemChanged(i);
                                mHomePagerAdapterDynamicMenu.notifyDataSetChanged();
                            }
                        }
                    };
                    handler.postDelayed(r, 1500);
                }
            }
        }

    }

    @SuppressLint("RestrictedApi")
    public void onEventMainThread(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        //Log.d(TAG, "onEventMainThread: mAdapterCarouselInfo is null- " + (mAdapterCarouselInfo == null) + " mMenuGroupName- " + mMenuGroup + " isMenuVisible- " + isMenuVisible());
        if (mAdapterCarouselInfo != null && isMenuVisible()) {
            mAdapterCarouselInfo.mOnItemClickListenerMovies.onClick(null, EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA, -1, searchMovieDataOnOTTApp.getMovieData());
            mAdapterCarouselInfo.setSearchMoviedata(searchMovieDataOnOTTApp);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (!DeviceUtils.isTablet(mContext)) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (mAdapterCarouselInfo != null) {
                    mainLayout.setVisibility(View.GONE);

                }
            } else {
                if (mAdapterCarouselInfo != null) {
                    mainLayout.setVisibility(View.VISIBLE);
                    //  mRecyclerViewCarouselInfo.getAdapter().notifyDataSetChanged();
                }
            }
        }
        if (mRecyclerViewCarouselInfo != null) {
            mRecyclerViewCarouselInfo.getAdapter().notifyDataSetChanged();
        }


        super.onConfigurationChanged(newConfig);
    }

    public void onEventMainThread(EventNetworkConnectionChange event) {

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

    }

    public void dismissProgressBar() {
        if (mProgressBar == null) {
            return;
        }
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * isPageVisible returns true when this carousel fragment is visible to user and the media page is hidden.
     * */
    @Override
    public boolean isPageVisible() {
        return getUserVisibleHint() && !isMediaPageVisible;
    }

    /**
     * An event for playback state, based on this event we can decide whether to allow viewpager auto scroll or disable it.
     */
    public void onEventMainThread(MediaPageVisibilityEvent event) {
        SDKLogger.debug("EventNetworkConnectionChange event data:: " + event);
        if (event == null) {
            return;
        }
        isMediaPageVisible = false;
        if (event.isMediaPageVisible()) {
            isMediaPageVisible = true;
            onPause();
            return;
        }
        onResume();
    }

    public void setViewScrollListener(recyclerViewScrollListener mViewScrollListener) {
        this.mViewScrollListener = mViewScrollListener;
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

            try {
                Log.d(TAG, "onScrolled: dx "+ dx + " dy "+ dy);
                LinearLayoutManager myLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int startScrollPosition = myLayoutManager.findFirstVisibleItemPosition();
                int endScrollPosition = myLayoutManager.findLastVisibleItemPosition();

                Log.d(TAG, "onScrolled: startScrollPosition "+ startScrollPosition+ " endScrollPosition "+ endScrollPosition);
//                ((MainActivity)requireActivity()).updateToolbar(startScrollPosition < 1);
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (!Util.doesCurrentTabHasPortraitBanner(mMenuGroup)) {
                View childView = recyclerView.getChildAt(0);
                RecyclerView.ViewHolder portraitViewHolder=null;
                if(childView!=null){
               portraitViewHolder = recyclerView.getChildViewHolder(childView);
                }
                if(portraitViewHolder instanceof PortraitBannerPlayerItem
                        || portraitViewHolder instanceof PortraitViewPagerItem){
                    Util.addTabsThatHavePortraitBanner(mMenuGroup);
                    int recOffset = recyclerView.computeVerticalScrollOffset();
                    if (mViewScrollListener != null) {
                        mViewScrollListener.onViewScrolled(recOffset);
                    }
                }
            }else{
                int recOffset = recyclerView.computeVerticalScrollOffset();
                if (mViewScrollListener != null) {
                    mViewScrollListener.onViewScrolled(recOffset);
                }

            }
            if(dy < 0){
                if (mViewScrollListener != null) {
                    mViewScrollListener.onViewScrolledUp();
                }
            }
            if(dy > 0){
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
                //Fix for the issue, the auto scrolling of the banners which are present under continue watching carousel is not working
                if(mListCarouselInfo!=null && mListCarouselInfo.size()>0){
                    for(int i=0;i<mListCarouselInfo.size();i++){
                        if(mListCarouselInfo.get(i).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_PORTRAIT_BANNER)){
                            if(BannerHorizontalItem3D.bannerHorizontalItem3D != null) {
                                BannerHorizontalItem3D.isStarted=false;
                                BannerHorizontalItem3D.bannerHorizontalItem3D.startScroll();
                            }
                            if(BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew != null) {
                                BigWeeklyTrendingItemNew.isStarted=false;
                                BigWeeklyTrendingItemNew.bigWeeklyTrendingItemNew.startScroll();
                            }
                        }
                    }
                }
            }
            if(!recyclerView.canScrollVertically(1)){
                if(mViewScrollListener != null){
                    mViewScrollListener.onScrolledToEnd();
                }
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            LinearLayoutManager myLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int startScrollPosition = myLayoutManager.findFirstVisibleItemPosition();
            int findFirstCompletelyVisibleItemPosition = myLayoutManager.findFirstCompletelyVisibleItemPosition();
            int endScrollPosition = myLayoutManager.findLastVisibleItemPosition();

//            if(newState == 0)
//                    Log.i("a", "scrolling stopped...");

            showTopNavigateArrow((endScrollPosition >= 6));

            try {
                if (endScrollPosition > mLastFirstVisibleItem) {
                    ((MainActivity)requireActivity()).updateBottomBar(false, 0);
                    Log.i("a", "scrolling down...");
                } else if (endScrollPosition < mLastFirstVisibleItem) {
                    if (endScrollPosition < 3) {
                        ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);
                    }else{
                        ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(VISIBLE);
                    }
                    ((MainActivity)requireActivity()).updateBottomBar(true, 0);
                    Log.i("a", "scrolling up...");
                }
                if(findFirstCompletelyVisibleItemPosition == 0)
                    ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);

            } catch (Exception e) {
                e.printStackTrace();
            }
            mLastFirstVisibleItem = endScrollPosition;
            }

        int mLastFirstVisibleItem;
   };



    private void sendPageChangeListener(String name) {
        Log.e("Inline", "PAGE_CHANGE_BROADCAST");
        ApplicationController.FIRST_TAB_NAME = name;
        Intent intent = new Intent(APIConstants.PAGE_CHANGE_BROADCAST);
        intent.putExtra(APIConstants.TAB_NAME, name);
        ApplicationController.getLocalBroadcastManager().sendBroadcast(intent);
    }

    private List<CarouselInfoData> getStaticList(){
        List<CarouselInfoData> staticListData=new ArrayList<>();
        staticListData.add(getListItem(APIConstants.LAYOUT_TYPE_CUSTOM_AD_BANNER,300,100,
                "/21830968352/Test_Ad_Card_Bw_Rails"));
        staticListData.add(getListItem(APIConstants.LAYOUT_TYPE_CUSTOM_AD_BANNER,300,250,
                "/21830968352/Test_Ad_Card_Bw_Rails"));
        staticListData.add(getListItem(APIConstants.LAYOUT_TYPE_CUSTOM_AD_BANNER,336,280,
                "/21830968352/Test_Ad_Card_Bw_Rails"));
        staticListData.add(getListItem(APIConstants.LAYOUT_TYPE_CUSTOM_AD_BANNER,300,250,
                "/21830968352/Test_Native_Video"));
        staticListData.add(getListItem(APIConstants.LAYOUT_TYPE_CUSTOM_AD_BANNER,300,250,
                "/21830968352/Test_Native_Image"));
        return staticListData;
    }

    public CarouselInfoData getListItem(String layoutType,int width,int height,String adId){
        CarouselInfoData carouselInfoData=new CarouselInfoData() ;
        carouselInfoData.layoutType=layoutType;
        carouselInfoData.adWidth=width;
        carouselInfoData.adHeight=height;
        carouselInfoData.adId=adId;
        carouselInfoData.title=mListCarouselInfo.get(0).title;
        carouselInfoData.showTitle=mListCarouselInfo.get(0).showTitle;
        carouselInfoData.bgColor=mListCarouselInfo.get(0).bgColor;
        carouselInfoData.name=mListCarouselInfo.get(0).name;
        carouselInfoData.actionUrl=mListCarouselInfo.get(0).actionUrl;
        carouselInfoData.appAction=mListCarouselInfo.get(0).appAction;
        carouselInfoData.showAll=mListCarouselInfo.get(0).showAll;
        carouselInfoData.showAllLayoutType=mListCarouselInfo.get(0).showAllLayoutType;
        carouselInfoData.altTitle=mListCarouselInfo.get(0).actionUrl;
        carouselInfoData.listCarouselData=mListCarouselInfo.get(0).listCarouselData;
        carouselInfoData.listNestedCarouselInfoData=mListCarouselInfo.get(0).listNestedCarouselInfoData;
        carouselInfoData.images=mListCarouselInfo.get(0).images;
        carouselInfoData.menuIcon=mListCarouselInfo.get(0).menuIcon;
        carouselInfoData.texture=mListCarouselInfo.get(0).texture;
        return carouselInfoData;
    }


    private class SingleScrollDirectionEnforcer  extends RecyclerView.OnScrollListener implements RecyclerView.OnItemTouchListener{
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

    private void showTopNavigateArrow(boolean isShow){
        if(isShow){
            fab_top_scroll.setVisibility(VISIBLE);
        }else {
            fab_top_scroll.setVisibility(GONE);
        }
    }

}
