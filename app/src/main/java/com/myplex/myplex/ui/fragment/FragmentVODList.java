package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FilterRequest;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.api.request.epg.EpgCatchUpList;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.CategoryScreenFilters;
import com.myplex.model.ChannelsCatchupEPGResponseData;
import com.myplex.model.FilterItem;
import com.myplex.model.GenreFilterData;
import com.myplex.model.GenresData;
import com.myplex.model.Languages;
import com.myplex.model.MenuDataModel;
import com.myplex.model.Terms;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterVODList;
import com.myplex.myplex.ui.adapter.CustomPagerAdapter;
import com.myplex.myplex.ui.views.CatchupItem;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FragmentVODList extends BaseFragment {

    public static final String PARAM_APP_FRAG_TYPE = "home_type";
    private static final String TAG = FragmentVODList.class.getSimpleName();
    public static final String PARAM_ENABLE_FILTER = "param_enable_filter";

    private GridView mListViewVOD;
    private TextView mTextViewErroFetch;
    private Context mContext;
    private RequestContentList mRequestContentList;
    private View mFooterView;
    private ProgressBar mFooterPbBar;
    private boolean mIsLoadingMorePages = false;
    private boolean mIsLoadingMoreAvailable = true;
    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView,horizontalCarouselClose;
    private ImageView channelImageView;
    private RelativeLayout mRootLayout;
    private CarouselInfoData mCarouselInfoData;
    private String mContentType;
    private AdapterVODList mAdapterVODList;
    private int mStartIndex = 1;
    private CategoryScreenFilters categoryScreenFilters;
    String orderBy = null;
    String publishingHouseId = null;
    public static List<CardData> catchupList;



    private HashMap<Integer, ArrayList<String>> filterMap = new HashMap<>();
    private PopUpWindow mFilterMenuPopupWindow;
    private View mFilterMenuPopup;
    private boolean isFromCatchUp = false;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAdapterVODList == null) {
                return false;
            }
            if (position >= mAdapterVODList.getCount()) {
                //Log.d(TAG, "Index out of bounds");
                return false;
            }
            final CardData cardData = mAdapterVODList.getItem(position);
            if (cardData == null || cardData._id == null) {
                return false;
            }
            if (!TextUtils.isEmpty(cardData.getTitle())) {
                AlertDialogUtil.showToastNotification("" + cardData.getTitle());
            }
            return false;
        }
    };

    private AdapterView.OnItemClickListener mOnItemClickVODList = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            if (mAdapterVODList == null) {
                return;
            }
            if (position >= mAdapterVODList.getCount()) {
                //Log.d(TAG, "Index out of bounds");
                return;
            }
            CardData cardData = mAdapterVODList.getItem(position);
            if (cardData == null || cardData._id == null) {
                return;
            }
         /*   view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setEnabled(true);
                }
            }, 500);
            view.setEnabled(false);*/
            showCardDetailsFragment(cardData);
        }
    };
    private String mFragmentType;
    private boolean isFilterContent;
    private String mFilteredLanguages;
    private String mFilteredGenres;
    private ImageView mImageViewFilterIcon;
    private boolean isKeyRegenerateRequestMade;
    private boolean isFilterContentReset;
    private boolean isFilterEnabled = true;
    private int carouselPosition;

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
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }
    private void showCardDetailsFragment(CardData cardData) {
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        if (cardData != null && cardData.generalInfo != null
                && APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)) {
            args.putString(CardDetails
                    .PARAM_CARD_ID, cardData.globalServiceId);
        } else if (cardData != null) {
            args.putString(CardDetails
                    .PARAM_CARD_ID, cardData._id);
        }
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);

        if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_LIVE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                && !cardData.isTVSeries()
                && !cardData.isVODChannel()
                && !cardData.isVODYoutubeChannel()
                && !cardData.isVODCategory()
                && !cardData.isTVSeason()) {
            if (mAdapterVODList != null)
                CacheManager.setCardDataList(mAdapterVODList.getAllItems());

        }

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        if (mCarouselInfoData != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mCarouselInfoData.title);

        if (isFilterContent) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_FILTER);
            if (mCarouselInfoData != null
                    && mCarouselInfoData.title != null) {
                String filterString = mFilteredGenres != null ? mFilteredGenres : "";
                filterString = mFilteredLanguages != null ? filterString + "," + mFilteredLanguages : filterString + "";
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mFilteredGenres + ", " + filterString);
            }
        }

        if(!TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE))){
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS,getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
            args.putString(Analytics.PROPERTY_SOURCE,Analytics.VALUE_SOURCE_NOTIFICATION);
        }
        if (cardData.generalInfo != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
            String videosType = APIConstants.TYPE_VODCATEGORY + "," + APIConstants
                    .TYPE_VODYOUTUBECHANNEL;
            try {
                if (videosType.equalsIgnoreCase(mContentType)) {
                    Analytics.gaBrowseVideos(cardData.generalInfo.title);
                } else if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType)) {
                    Analytics.gaBrowseTVShows(cardData.generalInfo.title);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
            CacheManager.setCarouselInfoData(mCarouselInfoData);

        }
      /*  if (cardData != null
                && cardData.generalInfo != null
                &&(APIConstants.TYPE_LIVE.equalsIgnoreCase(cardData.generalInfo.type)||APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type))) {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }*/
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, cardData);
        if((((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null) && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup) {
        args.putBoolean(CatchupItem.IS_CATCH_UP, isFromCatchUp);
        //Fixed the catchup content not playing when click on catchup contnt from the see all page
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup=false;
        }else{
            args.putBoolean(CatchupItem.IS_CATCH_UP,false);
        }
        mBaseActivity.showDetailsFragment(args, cardData);
    }

    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                try {

                    if (!mIsLoadingMorePages) {
                        if (!mIsLoadingMoreAvailable) {
                            return;
                        }
                        mIsLoadingMorePages = true;
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.VISIBLE);
                        }
                        mStartIndex++;
                        fetchVODData();

                    }

                } catch (Exception e) {

                }
            }
        }
    };

    private void showRelatedVodListFragment(CardData cardData) {
        Bundle args = new Bundle();

        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        CacheManager.setCarouselInfoData(mCarouselInfoData);

        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        //Log.d(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_vodlist, container, false);
        readBundle();
        getCategoryData();
        initUI(rootView);
        fetchVODData();
        APIConstants.IS_BACK_FROM_FRAGMENT_VOD_LIST=true;
        return rootView;
    }

    private void getCategoryData() {

        categoryScreenFilters = PropertiesHandler.getCategoryScreenFilters(mContext);

        if (categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters.size() > 0) {
            for (int i = 0; i < categoryScreenFilters.categoryScreenFilters.size(); i++) {
                if (categoryScreenFilters.categoryScreenFilters.get(i) != null &&
                        !TextUtils.isEmpty(categoryScreenFilters.categoryScreenFilters.get(i).contentType)
                        && categoryScreenFilters.categoryScreenFilters.get(i).contentType.equalsIgnoreCase(APIConstants.TV_SERIES)) {
                    orderBy = categoryScreenFilters.categoryScreenFilters.get(i).orderBy;
                    publishingHouseId = categoryScreenFilters.categoryScreenFilters.get(i).publishingHouseId;
                }
            }
        }
    }
    private final View.OnClickListener mFilterIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//              showNewFilterMenuPopUp(mToolbar);
            ((MainActivity) mContext).addFilterFragment();
        }
    };
    private View inflateFilterView(Context mContext) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup_window_filters, null);
        if (view == null) return null;
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        btFilter = (Button) view.findViewById(R.id.btApply);
        tvReset = (TextView) view.findViewById(R.id.tvReset);
        return view;
    }
    private void showNewFilterMenuPopUp(View view) {
        mFilterMenuPopup = inflateFilterView(mContext);
//        Analytics.createScreenGA(Analytics.SCREEN_FILTER);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_FILTER);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int softButtonsHeight = UiUtil.getSoftButtonsBarSizePort((Activity) mContext)/* - UiUtil.SPACING_MOBILE_BETWEEN_CONTENT_LAYOUT_NAV_BAR*/;
        /*if (DeviceUtils.isTablet(this)) {
            softButtonsHeight = softButtonsHeight - UiUtil.SPACING_TABLET_BETWEEN_CONTENT_LAYOUT_NAV_BAR;
        }*/
        int popupHeight = dm.heightPixels - softButtonsHeight;
        mFilterMenuPopupWindow = new PopUpWindow(mFilterMenuPopup);
        if (UiUtil.hasSoftKeys(mContext)) {
            mFilterMenuPopupWindow = new PopUpWindow(mFilterMenuPopup, popupHeight,mToolbar);
        }
        mFilterMenuPopup.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mFilterMenuPopup.getLayoutParams();
                int softButtonsHeight = UiUtil.getSoftButtonsBarSizePort((Activity) mContext);
                params.setMargins(0, 0, 0, softButtonsHeight);
                params.bottomMargin = softButtonsHeight;
            }
        });
        mFilterMenuPopupWindow.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mToolbar.setVisibility(View.VISIBLE);
            }
        });
        mToolbar.hideOverflowMenu();
        fetchFilterData();
    }

    private List<FilterItem> mFilterLanguages;
    private List<FilterItem> mFilterGeners;
    private Button btFilter;
    private TextView tvReset;

    public void setData(List<FilterItem> mFilterLanguages, List<FilterItem> mFilterGeners) {
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //if (adapter == null) {
        this.mFilterLanguages = mFilterLanguages;
        this.mFilterGeners = mFilterGeners;
        //TODO: hide loading textview
        adapter = new CustomPagerAdapter(mContext, mFilterLanguages, mFilterGeners, new CustomPagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap) {
                updateFilterData(filterMap);
            }
        });
        //TODO Added extra
        adapter.setFilterSectionType(MainActivity.SECTION_MOVIES);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        adapter.notifyDataSetChanged();

        btFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && adapter instanceof CustomPagerAdapter) {
                    adapter.filterOnClickApply();
                    Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_FILTERED_BY_CATEGORY);
                    closeFilterMenuPopup();
                }
            }
        });
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && adapter instanceof CustomPagerAdapter) {
                    adapter.reset();
                }
            }
        });
    }

    TabLayout tabLayout;
    ViewPager viewPager;
    CustomPagerAdapter adapter;

    private void initUI(View rootView) {
        mListViewVOD = (GridView) rootView.findViewById(R.id.listView_vod);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar, null, false);
        mImageViewFilterIcon = (ImageView) mInflateView.findViewById(R.id.toolbar_filter_icon);
        mTextViewErroFetch = (TextView) rootView.findViewById(R.id.textview_error_fetch);
        RelativeLayout mainBackgroundLayout=rootView.findViewById(R.id.main_background);
        mAdapterVODList = new AdapterVODList(mContext, getDummyVODList(),"","");
        mAdapterVODList.setBottomPartnerLogoVisibility(getArguments().getBoolean(ApplicationController.PARTNER_LOGO_BOTTOM_VISIBILTY,true));
        if(getArguments()!=null){
            carouselPosition = getArguments().getInt(CleverTap.PROPERTY_CAROUSEL_POSITION, -1);
        }
        mListViewVOD.setAdapter(mAdapterVODList);
        if(!DeviceUtils.isTablet(mContext)) {
            int count = Util.getNumColumns(mContext);
            if (mCarouselInfoData != null && (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(mCarouselInfoData.showAllLayoutType)
                    || APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM.equalsIgnoreCase(mCarouselInfoData.layoutType)))
                count = (Util.getNumColumns(mContext) + 1);
            mListViewVOD.setNumColumns(count);
        }else
            mListViewVOD.setNumColumns(APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(mCarouselInfoData==null?getArguments().getString(APIConstants.NOTIFICATION_PARAM_LAYOUT):mCarouselInfoData.showAllLayoutType)?(Util.getNumColumns(mContext)+3):Util.getNumColumns(mContext));

        mFooterView = getActivity().getLayoutInflater().inflate(R.layout.view_footer_layout, mListViewVOD, false);
        mFooterPbBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mFooterPbBar.setIndeterminate(false);
            mFooterPbBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, com.myplex.sdk.R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
        }
        mFooterPbBar.setVisibility(View.GONE);
        RelativeLayout sectionTitle = (RelativeLayout) rootView.findViewById(R.id.titlelayout);
        sectionTitle.setVisibility(APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType) ? View.VISIBLE : View.GONE);

        if ((mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.name) || FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType)) || !TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME))) {
            setUpToolbar(rootView);
            sectionTitle.setVisibility(View.GONE);
        } else {
            mToolbar.setVisibility(View.GONE);
        }

        if (FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType) && isFilterEnabled) {
            sectionTitle.setVisibility(View.GONE);
            mImageViewFilterIcon =  mInflateView.findViewById(R.id.toolbar_filter_icon);
            mImageViewFilterIcon.setVisibility(View.GONE);
            if (FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType)) {
                mToolbarTitle.setVisibility(View.GONE);
                mImageViewFilterIcon.setOnClickListener(mFilterIconClickListener);
                mImageViewFilterIcon.setVisibility(View.VISIBLE);
            }
            if (ApplicationController.ENABLE_FILTER_THROUGH_VIEW) {
                mToolbarTitle.setVisibility(View.VISIBLE);
                mImageViewFilterIcon.setVisibility(View.VISIBLE);
                mImageViewFilterIcon.setOnClickListener(mFilterIconClickListener);
            }
        }
        mImageViewFilterIcon.setImageResource(R.drawable.actionbar_filter_icon_default);
        if (!TextUtils.isEmpty(mFilteredGenres) || !TextUtils.isEmpty(mFilteredLanguages)) {
            mImageViewFilterIcon.setImageResource(R.drawable.actionbar_filter_icon_default_non_empty);
        }

        if (mCarouselInfoData!=null&&mCarouselInfoData.bgColor!=null){
            mainBackgroundLayout.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            mToolbar.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            mToolbarTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            mInflateView.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            mTextViewErroFetch.setTextColor(mContext.getResources().getColor(R.color.black));
            mHeaderImageView.setImageResource(R.drawable.notification_nav_drawer_close);
//            mHeaderImageView.setLayoutParams();

        }

        prepareFilterData();
//        mListViewVOD.addFooterView(mFooterView);
    }

    private void prepareFilterData() {
        if (mFilteredLanguages != null && !mFilteredLanguages.isEmpty()) {
            ArrayList<String> langList = new ArrayList<>();
            langList.addAll(Arrays.asList(mFilteredLanguages.split(",")));
            filterMap.put(0, langList);
        }
        if (mFilteredGenres != null && !mFilteredGenres.isEmpty()) {
            ArrayList<String> genreList = new ArrayList<>();
            genreList.addAll(Arrays.asList(mFilteredGenres.split(",")));
            filterMap.put(1, genreList);
        }
    }

    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            getActivity().finish();
            if (mBaseActivity instanceof MainActivity) {
               // ((MainActivity) mBaseActivity).removeFilterFragment();
                ((MainActivity) mBaseActivity).onBackPressed();
                APIConstants.IS_BACK_FROM_FRAGMENT_VOD_LIST=false;
                ((MainActivity)requireActivity()).updateBottomBar(false, 2);
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup=false;
            }
           // mBaseActivity.removeFragment(FragmentVODList.this);
        }
    };

    private void setUpToolbar(View rootView) {
        mToolbarTitle =  mInflateView.findViewById(R.id.toolbar_header_title);
        mToolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textsize_18));
        horizontalCarouselClose=mInflateView.findViewById(R.id.horizontal_carousel_close);
        mHeaderImageView =  mInflateView.findViewById(R.id.toolbar_settings_button);
        channelImageView =  mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mRootLayout =  mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        int statusBarHeight = Util.getStatusBarHeight(mContext);
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        layoutParams1.topMargin = statusBarHeight;
        mToolbar.setLayoutParams(layoutParams1);
        mToolbar.addView(mInflateView);
        if ((mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.title)) || !TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE))) {
            mToolbarTitle.setText((mCarouselInfoData != null) ? mCarouselInfoData.title : getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
        }
        channelImageView.setImageResource(R.drawable.toolbar_logo);
        showToolbarLogo();
        mHeaderImageView.setOnClickListener(mCloseAction);
        horizontalCarouselClose.setOnClickListener(mCloseAction);
        mHeaderImageView.setVisibility(View.VISIBLE);
        horizontalCarouselClose.setVisibility(View.GONE);
        if (isFilterEnabled)
            updateToolBarLogo();
    }

    private void showToolbarLogo() {
        if (mCarouselInfoData == null ) {
            return;
        }
        String logoUrl = mCarouselInfoData.getLogoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        if (logoUrl == null || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(logoUrl) == 0 || !isFilterEnabled) {
            channelImageView.setVisibility(View.GONE);
        } else {
            channelImageView.setVisibility(View.VISIBLE);
            if (logoUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
                channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                if (DeviceUtils.isTablet(mContext)) {
                    channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                }
            }

            PicassoUtil.with(mContext)
                    .load(logoUrl,channelImageView);
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        Bundle args = getArguments();
        if (args != null && args.containsKey(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE)) {
            if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE))) {
                mContentType = APIConstants.TYPE_VODCHANNEL;
            } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE))) {
                mContentType = APIConstants.TYPE_VODCATEGORY + "," + APIConstants.TYPE_VODYOUTUBECHANNEL;
            }
        }
        String videosType = APIConstants.TYPE_VODCATEGORY + "," + APIConstants
                .TYPE_VODYOUTUBECHANNEL;
        String tvshowsType = APIConstants.TYPE_VODCHANNEL;
        if (tvshowsType.equalsIgnoreCase(mContentType)) {
            ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity.SECTION_TVSHOWS));
            if (menuVisible) {
                if (mCarouselInfoData != null && TextUtils.isEmpty(mCarouselInfoData.name)) {
                    AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_PAGE_TV_SHOWS);
                } else if (mCarouselInfoData != null) {
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_TV_SHOWS, mCarouselInfoData.name, true);
                }
            }
        } else if (videosType.equalsIgnoreCase(mContentType)) {
            ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity.SECTION_VIDEOS));
            if (menuVisible) {
                AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_PAGE_VIDEOS);
            }
            if (mCarouselInfoData != null && TextUtils.isEmpty(mCarouselInfoData.name)) {
                AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_PAGE_VIDEOS);
            } else if (mCarouselInfoData != null) {
                AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_VIDEOS, mCarouselInfoData.name, true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
    }

    private List<CardData> getDummyVODList() {

        List dummyList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dummyList.add(new CardData());
        }
        return dummyList;
    }


    private void fetchCarouselData() {
        //Log.d(TAG, "fetchCarouselData:");
        if(((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup
                /*|| ((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null &&  ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer!=null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isFromCatchup*/) {
            getEPGCatchup();
            return;
        }
        final int pageSize = (mCarouselInfoData == null) ? getArguments().getInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT) : (mCarouselInfoData.pageSize > 0) ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
        new MenuDataModel().fetchCarouseldata(mContext, (mCarouselInfoData == null) ? getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) : mCarouselInfoData.name, mStartIndex, pageSize, true,mCarouselInfoData.modified_on, new MenuDataModel.CarouselContentListCallback() {
            @Override
            public void onCacheResults(List<CardData> dataList) {
                if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                    showNoDataMessage();
                    return;
                }

//                dataList = getFilteredResultsData(dataList);
                if (dataList != null && dataList.size() < pageSize) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mAdapterVODList != null) {
                        mAdapterVODList.addData(dataList);
                    }
                    return;
                }
                fillData(dataList);
            }

            @Override
            public void onOnlineResults(List<CardData> dataList) {
                if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                    showNoDataMessage();
                    return;
                }
//                dataList = getFilteredResultsData(dataList);

                if (dataList != null && dataList.size() < pageSize) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mAdapterVODList != null) {
                        mAdapterVODList.addData(dataList);
                    }
                    return;
                }
                fillData(dataList);
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                if (mFooterPbBar != null) {
                    mFooterPbBar.setVisibility(View.GONE);
                }
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                            "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
                    return;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    return;
                }
                showNoDataMessage();
            }
        });
    }

    private void fetchFilterDataWithLanguageAndGenre() {
//        updateToolbarTitle();
        isFilterContent = true;
        mIsLoadingMoreAvailable = true;
        if (isEpgLiveCarouselItem()) {
            EPG.genreFilterValues = mFilteredGenres;
            EPG.langFilterValues = mFilteredLanguages;
            fetchEpgData();
            return;
        }
        RequestContentList.Params contentListparams = new RequestContentList.Params(mContentType, mStartIndex, APIConstants.PAGE_INDEX_COUNT, mFilteredLanguages, mFilteredGenres,orderBy, PrefUtils.getInstance().getPrefpublisherGroupIds_Android());
        RequestContentList mRequestContentList = new RequestContentList(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }

                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message - " + response.body().message);

                        List<CardData> dataList = response.body().results;
                        if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                            showNoDataMessage();
                            return;
                        }
                        if (dataList == null || dataList.isEmpty()) {
                            return;
                        }
                        if (dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                            mIsLoadingMoreAvailable = false;
                        }
//                dataList = getFilteredResultsData(dataList);

                        if (mIsLoadingMorePages) {
                            mIsLoadingMorePages = false;
                            if (mAdapterVODList != null) {
                                mAdapterVODList.addData(dataList);
                            }
                            return;
                        }
                        fillData(dataList);

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + t);
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.GONE);
                        }
                        mIsLoadingMorePages = false;
                    }
                });
        APIService.getInstance().execute(mRequestContentList);


    }

    private void fetchVODData() {
        if (FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType)
                || isFilterContent) {
            fetchFilterDataWithLanguageAndGenre();
            return;
        }

        if ((mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.name)) || getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
            fetchCarouselData();
            return;
        }

        if (mContentType == null || mContentType.equalsIgnoreCase("")) {
            return;
        }

        RequestContentList.Params contentListparams = new RequestContentList.Params(mContentType, mStartIndex, APIConstants.PAGE_INDEX_COUNT, null, null,orderBy,PrefUtils.getInstance().getPrefpublisherGroupIds_Android());

        mRequestContentList = new RequestContentList(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if (!mIsLoadingMorePages && (response == null
                                || response.body() == null)) {
                            showNoDataMessage();
                            return;
                        }
                        if (!mIsLoadingMorePages
                                && response.body().status.equalsIgnoreCase("ERR_INVALID_SESSION_ID")
                                && response.body().code == 401) {
                            //Log.d(TAG, "fetchVODData: onResponse: status - ERR_INVALID_SESSION_ID");
                           /* Util.genKeyRequest(mContext, mContext.getString(R.string.genKeyReqPath), params);
                            Util.setKeyListener(CardExplorer.this);*/
                            showNoDataMessage();
                            return;
                        }
                        //Log.d(TAG, "fetchVODData: onResponse: message - " + response.body().message);

                        if (!mIsLoadingMorePages && (response.body().code != 200 || response.body().results == null)) {
                            showNoDataMessage();
                            return;
                        }
                        List<CardData> listFilteredResults = response.body().results;
                        if (listFilteredResults != null && listFilteredResults.size() < APIConstants.PAGE_INDEX_COUNT) {
                            mIsLoadingMoreAvailable = false;
                        }
                        if (mIsLoadingMorePages) {
                            mIsLoadingMorePages = false;
                            if (mAdapterVODList != null) {
                                mAdapterVODList.addData(listFilteredResults);
                            }
                            return;
                        }
                        //Log.d(TAG, "fetchVODData: onResponse: size - " + listFilteredResults.size());
                        if (listFilteredResults == null) {
                            if (mListViewVOD != null) {
                                mListViewVOD.setVisibility(View.INVISIBLE);
                            }
                            showNoDataMessage();
                            return;
                        }
                        fillData(listFilteredResults);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "fetchVODData: onResponse: t- " + t);
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.GONE);
                        }
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                                    "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
                            return;
                        }
                        if (!mIsLoadingMorePages) {
                            showNoDataMessage();
                        }
                        mIsLoadingMorePages = false;
                    }
                });
        APIService.getInstance().execute(mRequestContentList);


    }
    public void getEPGCatchup() {
        String mId = null;

        if (catchupList != null) {
            if(catchupList.get(0).generalInfo!=null && catchupList.get(0).generalInfo.type!=null){
                if(catchupList.get(0).generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM) &&catchupList.get(0).globalServiceId!=null ){
                    mId=catchupList.get(0).globalServiceId;
                }else{
                    mId=catchupList.get(0)._id;
                }
            }

            final int pageSize = mCarouselInfoData.pageSize > 0 ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
            final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String selectedDateInString = format.format(date);
            String dateStamp = Util.getYYYYMMDD(selectedDateInString);
            EpgCatchUpList.Params params = new EpgCatchUpList.Params(mId, dateStamp, true);
            EpgCatchUpList channelListEPG = new EpgCatchUpList(params, new APICallback<ChannelsCatchupEPGResponseData>() {
                @Override
                public void onResponse(APIResponse<ChannelsCatchupEPGResponseData> response) {
                    if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                        if (!mIsLoadingMorePages && (catchupList == null || catchupList.isEmpty())) {
                            showNoDataMessage();
                            return;
                        }
//                dataList = getFilteredResultsData(dataList);

                        if (catchupList != null && catchupList.size() < pageSize) {
                            mIsLoadingMoreAvailable = false;
                        }
                        if (mIsLoadingMorePages) {
                            mIsLoadingMorePages = false;
                            if (mAdapterVODList != null) {
                                mAdapterVODList.addData(catchupList);
                            }

                            return;
                        }
                        fillData(catchupList);


                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    // catchUpLinear.setVisibility(GONE);
                }
            });
            APIService.getInstance().execute(channelListEPG);
        }
    }



    private boolean isEpgLiveCarouselItem() {
        if (mCarouselInfoData == null || mCarouselInfoData.appAction == null)
            return false;

        return APIConstants.APP_ACTION_LIVE_PROGRAM_LIST.equalsIgnoreCase(mCarouselInfoData.appAction);
    }

    private void readBundle() {
        //Content list call
        Bundle args = getArguments();
        mCarouselInfoData = CacheManager.getCarouselInfoData();
        if (args == null) return;
        if (args.containsKey(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE)) {
            mContentType = (String) args.getSerializable(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE);
        }

        isFilterEnabled = args.getBoolean(PARAM_ENABLE_FILTER, true);
        isFromCatchUp = args.getBoolean(CatchupItem.IS_CATCH_UP, false);
        if (args.containsKey(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE)) {
            if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE))) {
                mContentType = APIConstants.TYPE_VODCHANNEL;
//                Analytics.createScreenGA(Analytics.SCREEN_TVSHOWS_LIST);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_TVSHOWS_LIST);
            } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE))) {
                mContentType = APIConstants.TYPE_VODCATEGORY + "," + APIConstants.TYPE_VODYOUTUBECHANNEL;
//                Analytics.createScreenGA(Analytics.SCREEN_VIDEOS_LIST);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_VIDEOS_LIST);
            } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE))) {
                mContentType = APIConstants.TYPE_MUSIC_VIDEO;
//                Analytics.createScreenGA(Analytics.SCREEN_MUSIC_VIDEOS_LIST);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_MUSIC_VIDEOS_LIST);
            } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE)) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE))) {
//                Analytics.createScreenGA(Analytics.SCREEN_KIDS_LIST);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_KIDS_LIST);
            }
        }

        if (args.containsKey(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE)) {
            mFragmentType = args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE);
        }

        if (args.containsKey(FragmentCarouselViewAll.PARAM_LANGUAGE_FILTER_VALUE)) {
            mFilteredLanguages = args.getString(FragmentCarouselViewAll.PARAM_LANGUAGE_FILTER_VALUE);
        }

        if (args.containsKey(FragmentCarouselViewAll.PARAM_GENRE_FILTER_VALUE)) {
            mFilteredGenres = args.getString(FragmentCarouselViewAll.PARAM_GENRE_FILTER_VALUE);
        }
        args.clear();
    }

    private void fillData(List<CardData> mListVODData) {
        if (mListVODData == null
                || mListVODData.isEmpty()) {
            showNoDataMessage();
            return;
        }
        mTextViewErroFetch.setVisibility(View.GONE);
        mListViewVOD.setVisibility(View.VISIBLE);
        List<CardData> dataList = new ArrayList<>(mListVODData);
        mAdapterVODList = new AdapterVODList(mContext, dataList,mCarouselInfoData.name,mCarouselInfoData.layoutType);
        mAdapterVODList.setBottomPartnerLogoVisibility(getArguments().getBoolean(ApplicationController.PARTNER_LOGO_BOTTOM_VISIBILTY,true));
        if (mCarouselInfoData != null) {
          //  mAdapterVODList.setIsSmallSquareItem(true);
            mAdapterVODList.showTitle(mCarouselInfoData.showTitle);
            mAdapterVODList.setBgColor(mCarouselInfoData.bgColor);
        }
        if (mCarouselInfoData != null && (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(mCarouselInfoData.showAllLayoutType)
            || APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM.equalsIgnoreCase(mCarouselInfoData.layoutType)))
            mAdapterVODList.setIsSmallSquareItem(true);
        mListViewVOD.setAdapter(mAdapterVODList);
        mListViewVOD.setOnItemClickListener(mOnItemClickVODList);
        mListViewVOD.setOnItemLongClickListener(mOnItemLongClickListener);
        mListViewVOD.setOnScrollListener(mScrollListener);
        mAdapterVODList.notifyDataSetChanged();
    }

    private void showNoDataMessage() {
        if (mTextViewErroFetch != null) {
            mListViewVOD.setVisibility(View.GONE);
            if (mTextViewErroFetch != null) {
                if (isFilterContent) {
                    mTextViewErroFetch.setText(mContext.getString(R.string.error_fetch_filter_data));
                }
                mTextViewErroFetch.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");
        if(((MainActivity) mContext).mFragmentCardDetailsPlayer!=null) {
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup = false;
        }
    }

    private AlertDialogUtil.NeutralDialogListener mNeutralDialogListener = new AlertDialogUtil.NeutralDialogListener() {
        @Override
        public void onDialogClick(String buttonText) {
            if (null != buttonText &&
                    buttonText.equalsIgnoreCase(mContext.getString(R.string.play_button_retry))) {
                fetchVODData();
            }
        }
    };


    public static FragmentVODList newInstance(Bundle args) {
        FragmentVODList mFragmentMovies = new FragmentVODList();
        mFragmentMovies.setArguments(args);
        return mFragmentMovies;
    }

    public static FragmentVODList newInstance(Bundle args,List<CardData> cardDataList) {
        FragmentVODList mFragmentMovies = new FragmentVODList();
        mFragmentMovies.setArguments(args);
        catchupList=cardDataList;
        return mFragmentMovies;
    }




    private void updateToolBarLogo() {

        try {

            String iconUrl = getImageLink(mCarouselInfoData.images);
            if (!TextUtils.isEmpty(iconUrl)) {
                //Log.d(TAG, "mToolbarIconUrl: " + iconUrl);

                if (iconUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
                    channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    if (DeviceUtils.isTablet(mContext)) {
                        channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    } else {
                        channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    }
                }

                PicassoUtil.with(mContext)
                        .load(iconUrl,channelImageView);
            }

            /*if (mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.bgColor)) {
                //Log.d(TAG, "mToolbarBgColor: " + mCarouselInfoData.bgColor);
                if (!TextUtils.isEmpty(mCarouselInfoData.bgColor)) {
                    try {
                        mInflateView.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String getImageLink(List<CardDataImagesItem> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && UiUtil.getScreenDensity(mContext).equalsIgnoreCase(imageItem.profile)) {
                        Log.e("SCREEN DENSITY ", UiUtil.getScreenDensity(mContext)
                                + " IMAGE ITEM PROFILE " + imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        return imageItem.link;
                    }
//                    if (imageType.equalsIgnoreCase(imageItem.type)
//                        && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
//                    return imageItem.link;
//                }
                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
                        Log.e("IMAGE ITEM PROFILE ", imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        return imageItem.link;
                    }
                }
            }
        }

        return null;
    }

    private void closeFilterMenuPopup() {
        if (mFilterMenuPopupWindow != null) {
            mFilterMenuPopupWindow.dismissPopupWindow();
        }
    }

    public void updateFilterData(HashMap<Integer, ArrayList<String>> filterValuesMap) {
        ArrayList<String> languageList = new ArrayList<>();
        ArrayList<String> genreFilterList = new ArrayList<>();
        int genreKey = 1;
        int languageKey = 0;
        if (filterValuesMap != null && filterValuesMap.containsKey(genreKey)) {
            genreFilterList = filterValuesMap.get(genreKey);
        }
        if (filterValuesMap != null && filterValuesMap.containsKey(languageKey)) {
            languageList = filterValuesMap.get(languageKey);
        }
        if (filterValuesMap == null) filterValuesMap = new HashMap<>();

        filterMap = filterValuesMap;
            /*if(genreFilterList.size()== 0 && languageList.size() == 0){
                closeFilterMenuPopup();
                return;
            }*/
        if (genreFilterList.size() > 0 && genreFilterList.get(0).equals("All")) {
            genreFilterList = new ArrayList<>();
        }
        if (languageList.size() > 0 && languageList.get(0).equals("All")) {
            languageList = new ArrayList<>();
        }

        String genreValues = joinList(genreFilterList, ",");
        String langValues = joinList(languageList, ",");

        String gaFilterNames = null;
        if (genreValues != null
                && !genreValues.equals("")) {
            gaFilterNames = genreValues;
        }

        if (langValues != null
                && !langValues.equals("")) {
            if (gaFilterNames != null) {
                gaFilterNames = gaFilterNames + "," + langValues;
            } else {
                gaFilterNames = langValues;
            }

            Analytics.mixpanelEventAppliedFilter(langValues, genreValues);
            Analytics.mixpanelSetPeopleProperty(Analytics.MIXPANEL_PEOPLE_SETTINGS_LANGUAGE_USED, true);
        }

        if (gaFilterNames != null) {
            Analytics.gaBrowseFilter(gaFilterNames, 1l);
        }

        closeFilterMenuPopup();
        mToolbar.setVisibility(View.VISIBLE);
        //mToolbar.showOverflowMenu();

        //Check previous filter is empty and current filter values are also empty then return
        if (TextUtils.isEmpty(langValues)
                && TextUtils.isEmpty(genreValues)
                && TextUtils.isEmpty(mFilteredGenres)
                && TextUtils.isEmpty(mFilteredLanguages)) {
            return;
        }

        mStartIndex = 1;
        mFilteredLanguages = langValues;
        mFilteredGenres = genreValues;
        isFilterContentReset = true;
        fetchFilterDataWithLanguageAndGenre();

        mImageViewFilterIcon.setImageResource(R.drawable.actionbar_filter_icon_default);
        if ((genreFilterList != null && !genreFilterList.isEmpty()
                || (languageList != null && !languageList.isEmpty()))) {
            mImageViewFilterIcon.setImageResource(R.drawable.actionbar_filter_icon_default_non_empty);
        }
    }

    private String joinList(ArrayList list, String literal) {
        return list.toString().replaceAll(",", literal).replaceAll("[\\[.\\].\\s+]", "");
    }


    /*
     * Fetching the list data
     */
    private void fetchFilterData() {

        FilterRequest.Params requestParams = new FilterRequest.Params(mContentType);
        final FilterRequest request = new FilterRequest(requestParams, new APICallback<GenreFilterData>() {
            @Override
            public void onResponse(APIResponse<GenreFilterData> response) {
                if (null == response.body() || response.body().results == null) {
                    closeFilterMenuPopup();
                    mToolbar.setVisibility(View.VISIBLE);
                    return;
                }

                try {
                    parseFilterResponseData(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                closeFilterMenuPopup();
                mToolbar.setVisibility(View.VISIBLE);
            }
        });

        APIService.getInstance().execute(request);
    }

    private void parseFilterResponseData(GenreFilterData body) {
        List<FilterItem> groupGenres = new ArrayList<>();
        List<FilterItem> groupLanguages = new ArrayList<>();

        if (body.results == null) {
            LoggerD.debugLog("GenreFilterData == null");
            closeFilterMenuPopup();
            return;
        }

        Languages languages = body.results.languages;
        List<Terms> languagesList = null;
        if (languages != null) {
            languagesList = languages.terms;
        }
        GenresData genresData = body.results.genres;
        List<Terms> genresDataList = null;
        if (genresData != null) {
            genresDataList = genresData.terms;
        }

        if (languagesList != null
                && genresDataList != null
                && languagesList.size() == 0
                && genresDataList.size() == 0) {
            System.out.println("phani filter null");
            closeFilterMenuPopup();
            return;
        }

        if (languagesList != null) {
            for (int i = 0; i < languagesList.size(); i++) {
                FilterItem filterItem = new FilterItem();
                filterItem.setTitle(languagesList.get(i).term);
                if (filterMap != null && filterMap.size() > 0) {
                    if (filterMap.containsKey(0)) {
                        ArrayList<String> genreFilterItems = filterMap.get(0);
                        if (genreFilterItems != null && genreFilterItems.size() > 0) {
                            for (int k = 0; k < genreFilterItems.size(); k++) {
                                if (genreFilterItems.get(k).equals(languagesList.get(i).term)) {
                                    filterItem.setIsChecked(true);
                                    break;
                                } else {
                                    filterItem.setIsChecked(false);
                                }
                            }
                        } else {
                            filterItem.setIsChecked(false);
                        }
                    }
                } else {
                    filterItem.setIsChecked(false);
                }
                groupLanguages.add(filterItem);
            }

        }
        /*int langCheckCnt =0;
        for(int i =0;i<groupLanguages.size();i++){
            if(groupLanguages.get(i).isChecked()){
                langCheckCnt++;
            }
        }
        FilterItem langFilterItem = new FilterItem();
        if(langCheckCnt == groupLanguages.size()){
            langFilterItem.setIsChecked(true);
            langFilterItem.setTitle("All");
            groupLanguages.add(0, langFilterItem);
        }else {
            langFilterItem.setIsChecked(false);
            langFilterItem.setTitle("All");
            groupLanguages.add(0, langFilterItem);
        }*/

        if (genresDataList != null) {
            for (int i = 0; i < genresDataList.size(); i++) {
                FilterItem filterItem = new FilterItem();
                filterItem.setTitle(genresDataList.get(i).humanReadable);

                if (filterMap.size() > 0) {
                    if (filterMap.containsKey(1)) {
                        ArrayList<String> genreFilterItems = filterMap.get(1);
                        if (genreFilterItems != null && genreFilterItems.size() > 0) {
                            for (int k = 0; k < genreFilterItems.size(); k++) {
                                if (genreFilterItems.get(k).equals(genresDataList.get(i).humanReadable)) {
                                    filterItem.setIsChecked(true);
                                    break;
                                } else {
                                    filterItem.setIsChecked(false);
                                }
                            }
                        } else {
                            filterItem.setIsChecked(false);
                        }
                    }
                } else {
                    filterItem.setIsChecked(false);
                }
                groupGenres.add(filterItem);
            }
        }
       /* int checkCnt =0;
        for(int i =0;i<groupGenres.size();i++){
            if(groupGenres.get(i).isChecked()){
                checkCnt++;
            }
        }*/
        /*if(checkCnt == groupGenres.size()){
            filterItem.setIsChecked(true);
            filterItem.setTitle("All");
            groupGenres.add(0, filterItem);
        }else {
            filterItem.setIsChecked(false);
            filterItem.setTitle("All");
            groupGenres.add(0, filterItem);
        }*/
        //TODO: change here
        if (groupLanguages == null || groupGenres == null) {
            Log.e("lang or gener", "null");
            return;
        }
        if (mFilterMenuPopup != null)
            //((FilterView) mFilterMenuPopup).setData(groupLanguages, groupGenres);
            setData(groupLanguages,groupGenres);
        else
            Log.e("popupmenu", "isEmpty");
        //TODO: COMMENT HERE
        /*mFilterListGroup.add(new FilterData(getResources().getString(R.string.languages_txt), groupLanguages));
        mFilterListGroup.add(new FilterData(getResources().getString(R.string.genre_txt), groupGenres));
        if (mFilterListGroup.get(0).mFilterItemList.size() > 0 || mFilterListGroup.get(1).mFilterItemList.size() > 0) {

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                public void run() {
                    //addBlur();
                }
            });

            mFilterLoadingTxt.setVisibility(View.GONE);
//            mApplyLayout.setVisibility(View.VISIBLE);
            mButtonFilterApply.setVisibility(View.VISIBLE);
            //System.out.
            mPopupListAdapter = new FilterAdapter(mContext, mFilterListGroup, new FilterAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap) {
                    // handleOTTAppClicked(ottApp);
                    updateFilterData(filterMap);
                }
            });
            mPopupListAdapter.setFilterSectionType(MainActivity.SECTION_MOVIES);

            // mPopupListAdapter = new FilterAdapter(mContext, mFilterListGroup);
            mPopUpListView.setAdapter(mPopupListAdapter);
        }*/

        //showFilterMenuPopUp(v);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(TAG, "dismiss pop up window");
            if (mFilterMenuPopupWindow != null) {
                mFilterMenuPopupWindow.dismissPopupWindow();
            }
        }
        if (mCarouselInfoData != null && mCarouselInfoData.showAllLayoutType != null) {
            if(!DeviceUtils.isTablet(mContext)) {
//                mListViewVOD.setNumColumns(APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(mCarouselInfoData.showAllLayoutType) ? (Util.getNumColumns(mContext) + 1) : Util.getNumColumns(mContext));
                int count = Util.getNumColumns(mContext);
                if (mCarouselInfoData != null && (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(mCarouselInfoData.showAllLayoutType)
                        || APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM.equalsIgnoreCase(mCarouselInfoData.layoutType)))
                    count = (Util.getNumColumns(mContext) + 1);
                mListViewVOD.setNumColumns(count);
            }else
                mListViewVOD.setNumColumns(APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(mCarouselInfoData.showAllLayoutType)?(Util.getNumColumns(mContext)+3):Util.getNumColumns(mContext));
        }
        super.onConfigurationChanged(newConfig);
    }

    private void fetchEpgData() {
        final int pageSize = mCarouselInfoData.pageSize > 0 ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
        EPG.getInstance(Util.getCurrentDate(0)).findPrograms(mContext, pageSize, mStartIndex, new EPG.CacheManagerCallback() {

            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                if (mFooterPbBar != null) mFooterPbBar.setVisibility(View.GONE);
                if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                    showNoDataMessage();
                    return;
                }

//                dataList = getFilteredResultsData(dataList);
                if (dataList != null && dataList.size() < pageSize) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages && !isFilterContentReset) {
                    mIsLoadingMorePages = false;
                    if (mAdapterVODList != null) {
                        mAdapterVODList.addData(dataList);
                    }
                    return;
                }
                if (isFilterContentReset) isFilterContentReset = false;
                fillData(dataList);
            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                String reason = error.getMessage() == null ? "NA" : error.getMessage();

                if (reason.equalsIgnoreCase("ERR_INVALID_SESSION_ID") && errorCode == 401 && !isKeyRegenerateRequestMade) {
                    isKeyRegenerateRequestMade = true;
                    SDKUtils.makeReGenerateKeyRequest(mContext, new SDKUtils.RegenerateKeyRequestCallback() {
                        @Override
                        public void onSuccess() {
                            fetchEpgData();
                        }

                        @Override
                        public void onFailed(String msg) {
                            if (mFooterPbBar != null) mFooterPbBar.setVisibility(View.GONE);
                        }
                    });
//                    cacheManagerCallback.OnlineError(new Throwable(""), errorCode);
                    return;
                }
                if (mFooterPbBar != null) mFooterPbBar.setVisibility(View.GONE);
            }
        });


    }


}
