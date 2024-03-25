package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FilterRequest;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.api.request.user.ArtistProfileContentList;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.CategoryScreenFilters;
import com.myplex.model.FilterItem;
import com.myplex.model.GenreFilterData;
import com.myplex.model.GenresData;
import com.myplex.model.Languages;
import com.myplex.model.MenuDataModel;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.model.Terms;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterMovieList;
import com.myplex.myplex.ui.adapter.AdapterMoviesForGrid;
import com.myplex.myplex.ui.adapter.CustomPagerAdapter;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Apalya on 12/3/2015.
 */
public class FragmentCarouselViewAll extends BaseFragment {
    private static final String TAG = FragmentCarouselViewAll.class.getSimpleName();
    public static final String PARAM_FRAGMENT_TYPE_CAROUSEL_GRID = "carousel_grid";
    public static final String PARAM_CAROUSEL_DATA = "carousel_data";
    public static final String PARAM_FRAGMENT_MENU_GROUP_TYPE = "menu_group_type";
    public static final String PARAM_FROM_VIEW_ALL = "from_view_carousel";
    public static final String PARAM_LANGUAGE_FILTER_VALUE = "filtered_languages";
    public static final String PARAM_GENRE_FILTER_VALUE = "filtered_genres";
    public static final String PARAM_FRAGMENT_TYPE = "fragment_type";
    public static final String PARAM_FRAGMENT_TYPE_FILTER = "fragment_filter";
    public static final String PARAM_ARTIST_NAME ="carousel_list";
    public static final String PARAM_FRAGMENT_TYPE_ARTIST_PROFILE="artist_profile";

    private CarouselInfoData mCarouselInfoData;

    private Toolbar mToolbar;
    private View mInflateView;
    private ListView mListView;
    private TextView mToolbarTitle;
    private TextView mErrorTextView;
    private ImageView mToolbarCloseButton;
    private ImageView mToolbarListGridLayoutSwitch;
    private ImageView channelImageView;
    private RelativeLayout mRootLayout;
    private RelativeLayout mainBackgroundLayout;
    private Context mContext;
    private RelativeLayout helpScreenLayout;
    private AdapterMovieList mAdapterMovieList;
    private boolean mIsLoadingMorePages = false;
    private boolean mIsLoadingMoreAvailable = true;
    private int mStartIndex = 1;
    private View mFooterView;
    private ProgressBar mFooterPbBar;
    private String mContentType;
    private GridView mGridView;
    private ImageView mGridButton;
    private AdapterMoviesForGrid mAdapterMoviesGrid;
    private CategoryScreenFilters categoryScreenFilters;
    String orderBy = null;
    String publishingHouseId = null;
    ProfileAPIListAndroid profileAPIListAndroid;
    String artistName;

    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            if (mAdapterMovieList == null) {
                return;
            }
            if (position >= mAdapterMovieList.getCount()) {
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
            final CardData cardData = mAdapterMovieList.getItem(position);
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

            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(cardData, mContext, mCarouselInfoData, null);
                return;
            }
/*
            if (cardData.generalInfo != null
                    && cardData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
                if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                        || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                    Analytics.gaBrowseTVShows(cardData.generalInfo.title);
                } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)) {
                    Analytics.gaBrowseVideos(cardData.generalInfo.title);
                }
                //Log.d(TAG, "type: " + cardData.generalInfo.type + " title: " + cardData.generalInfo.title);
                overlayRelatedVODListFragment(cardData);
                return;
            }*/

            showDetailsFragment(cardData,carouselPosition);
        }
    };
    private ImageView mImageViewFilterIcon;
    private String mFilteredLanguages;
    private String mFilteredGenres;
    private String mFragmentType;

    private View.OnClickListener mListGridSwitchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LoggerD.debugLog("SwitchLayouts: from onCreateView ApplicationController.sIsGridView- " + ApplicationController.sIsGridView);
            switchLayouts();
        }
    };
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAdapterMovieList == null) {
                return false;
            }
            if (position >= mAdapterMovieList.getCount()) {
                //Log.d(TAG, "Index out of bounds");
                return false;
            }
            final CardData cardData = mAdapterMovieList.getItem(position);
            if (cardData == null || cardData._id == null) {
                return false;
            }
            if (!TextUtils.isEmpty(cardData.getTitle())) {
                AlertDialogUtil.showToastNotification("" + cardData.getTitle());
            }
            return false;
        }
    };
    private boolean isFilterEnabled = true;
    private int carouselPosition = -1;

    private void switchLayouts() {
        if (ApplicationController.sIsGridView) {
            switchToLinearLayout();
        } else {
            switchToGridLayout();
        }
    }


    private void showLayouts() {
        if (ApplicationController.sIsGridView) {
            switchToGridLayout();
        } else {
            switchToLinearLayout();
        }
    }

    private void switchToGridLayout() {
        ApplicationController.sIsGridView = true;
        mGridViewLayout.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mToolbarListGridLayoutSwitch.setImageResource(R.drawable.movies_list_view_icon);
    }

    private void switchToLinearLayout() {
        ApplicationController.sIsGridView = false;
        mGridViewLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mToolbarListGridLayoutSwitch.setImageResource(R.drawable.movies_grid_view_icon);
    }

    private boolean isFilterContent;
    private RelativeLayout mGridViewLayout;
    private TextView mGridFooterTextViewLoading;

    private void overlayRelatedVODListFragment(CardData cardData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        if (mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.bgSectionColor)) {
            args.putString(FragmentRelatedVODList.PARAM_BG_SECTION_COLOR, mCarouselInfoData.bgSectionColor);
            args.putString(FragmentRelatedVODList.PARAM_BG_SECTION_LOGO_URL, getImageLink(mCarouselInfoData.images));
            ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));
            return;
        }
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));

    }

    private void showDetailsFragment(CardData cardData, int carouselPosition) {
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
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
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
        String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DOWNLOADED_VIDEOS);
        if (mCarouselInfoData != null
                && mCarouselInfoData.title != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mCarouselInfoData.title);

        if (isFilterContent){
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_FILTER);
            if (mCarouselInfoData != null
                    && mCarouselInfoData.title != null) {
                String filterString = mFilteredGenres != null ? mFilteredGenres : "";
                filterString = mFilteredLanguages != null ? filterString + "," +mFilteredLanguages : filterString + "";
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mFilteredGenres + ", " + filterString);
            }
        }
        if(!TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE))){
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS,getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
            args.putString(Analytics.PROPERTY_SOURCE,Analytics.VALUE_SOURCE_NOTIFICATION);
        }

        ((BaseActivity) mContext).showDetailsFragment(args, cardData);
    }

    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseActivity == null || mBaseActivity.isFinishing()) {
                return;
            }
            if (mBaseActivity instanceof MainActivity) {
             //   ((MainActivity) mBaseActivity).removeFilterFragment();
                ((MainActivity) mBaseActivity).onBackPressed();
            }
          //  mBaseActivity.removeFragment(FragmentCarouselViewAll.this);
        }
    };

    private final View.OnClickListener mFilterIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            showNewFilterMenuPopUp(mToolbar);
            ((MainActivity)mContext).addFilterFragment();
        }
    };

    private View inflateFilterView(Context mContext) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            return null;
        }
        View view = inflater.inflate(R.layout.popup_window_filters, null);
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
        this.mFilterLanguages = mFilterLanguages;
        this.mFilterGeners = mFilterGeners;
        //TODO: hide loading textview
        adapter = new CustomPagerAdapter(mContext, mFilterLanguages, mFilterGeners, new CustomPagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap) {
                updateFilterData(filterMap);
            }
        });
        if (this.mFilterGeners != null && this.mFilterGeners != null && this.mFilterGeners.size() > 0 && this.mFilterLanguages.size() > 0) {
            //TextView tvFilter = (TextView) findViewById(R.id.filter_loading_txt);
            //tvFilter.setVisibility(View.GONE);
        }
        //TODO Added extra
        adapter.setFilterSectionType(MainActivity.SECTION_MOVIES);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        adapter.notifyDataSetChanged();

            //adapter.setFilterSectionType(mSectionType);
            //viewPager.setAdapter(adapter);
            //tabLayout.setupWithViewPager(viewPager);
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
                    if (mFooterPbBar != null) {
                        mFooterPbBar.setVisibility(View.VISIBLE);
                    }
                    if (mGridFooterTextViewLoading != null && ApplicationController.sIsGridView) {
                        mGridFooterTextViewLoading.setVisibility(View.VISIBLE);
                    }
                    mStartIndex++;
                    if(mFragmentType!=null&&mFragmentType.equals(PARAM_FRAGMENT_TYPE_ARTIST_PROFILE)){
                        fetchArtistCarouselData(profileAPIListAndroid);
                    }else {
                        fetchData();
                    }
                }
            }
        }
    };
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        mListView = (ListView) rootView.findViewById(R.id.listview_movies);
        mGridViewLayout = (RelativeLayout) rootView.findViewById(R.id.grid_layout);
        mGridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        mGridFooterTextViewLoading = (TextView) rootView.findViewById(R.id.grid_footer_text_loading);
        mErrorTextView = (TextView) rootView.findViewById(R.id.error_message);
        mainBackgroundLayout=rootView.findViewById(R.id.main_background);
        helpScreenLayout = (RelativeLayout) rootView.findViewById(R.id.prog_help_screen_layout);
        mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar_carousel_view_all, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mToolbarCloseButton = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        channelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mImageViewFilterIcon = (ImageView) mInflateView.findViewById(R.id.toolbar_filter_icon);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        mToolbarListGridLayoutSwitch = (ImageView) mInflateView.findViewById(R.id.toolbar_list_grid_converter);
        mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
        mToolbarListGridLayoutSwitch.setOnClickListener(mListGridSwitchListener);
//        mLogoView
        //        mToolbar.setLogo(R.drawable.app_icon);
//        mInflateView.findViewById(R.id.toolbar_filter_button).setVisibility(View.GONE);

        Bundle args = getArguments();
        readBundle(args);
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mContentType)) {
//            Analytics.createScreenGA(Analytics.SCREEN_MOVIE_LIST);
            FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_MOVIE_LIST);
        }
        getCategoryData();
        initUI();
        fetchData();
        Util.getNumColumns(mContext,mCarouselInfoData);
        return rootView;
    }

    private void getCategoryData() {

        categoryScreenFilters = PropertiesHandler.getCategoryScreenFilters(mContext);

        if (categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters.size() > 0) {
            for (int i = 0; i < categoryScreenFilters.categoryScreenFilters.size(); i++) {
                if(categoryScreenFilters.categoryScreenFilters.get(i) != null
                        && !TextUtils.isEmpty(categoryScreenFilters.categoryScreenFilters.get(i).contentType)
                        && categoryScreenFilters.categoryScreenFilters.get(i).contentType.equalsIgnoreCase(APIConstants.TV_SERIES)) {
                    orderBy = categoryScreenFilters.categoryScreenFilters.get(i).orderBy;
                    publishingHouseId = categoryScreenFilters.categoryScreenFilters.get(i).publishingHouseId;
                }
            }
        }
    }

    private void fetchData() {
        mErrorTextView.setVisibility(View.GONE);
        showLayouts();
        if (PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType)
                || isFilterContent) {
            fetchFilterDataWithLanguageAndGenre();
            return;
        }
        if(mFragmentType!=null&&mFragmentType.equals(PARAM_FRAGMENT_TYPE_ARTIST_PROFILE)){
            fetchArtistCarouselData(profileAPIListAndroid);
        }else {
            fetchCarouselData();
        }
    }

    private void readBundle(Bundle args) {

        mCarouselInfoData = CacheManager.getCarouselInfoData();

        if (args != null) {
            isFilterEnabled = getArguments().getBoolean(FragmentVODList.PARAM_ENABLE_FILTER, true);
        }
        if (args != null && args.containsKey(PARAM_FRAGMENT_TYPE)) {
            mFragmentType = args.getString(PARAM_FRAGMENT_TYPE);
        }

        if (args!=null&& args.containsKey(CleverTap.PROPERTY_PROFILE_API_LIST)){
            profileAPIListAndroid= (ProfileAPIListAndroid) args.getSerializable(CleverTap.PROPERTY_PROFILE_API_LIST);
            artistName=args.getString(PARAM_ARTIST_NAME);
            mFragmentType=args.getString(PARAM_FRAGMENT_TYPE);
        }

        if (args != null && args.containsKey(PARAM_FRAGMENT_MENU_GROUP_TYPE)) {
            mContentType = args.getString(PARAM_FRAGMENT_MENU_GROUP_TYPE);
            if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mContentType)||APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mContentType)) {
//                Analytics.createScreenGA(Analytics.SCREEN_KIDS_LIST);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_KIDS_LIST);
            }
        }

        if (args != null && args.containsKey(PARAM_LANGUAGE_FILTER_VALUE)) {
            mFilteredLanguages = args.getString(PARAM_LANGUAGE_FILTER_VALUE);
        }

        if (args != null) {
            carouselPosition = args.getInt(CleverTap.PROPERTY_CAROUSEL_POSITION, -1);
        }

        if (args != null && args.containsKey(PARAM_GENRE_FILTER_VALUE)) {
            mFilteredGenres = args.getString(PARAM_GENRE_FILTER_VALUE);
        }
        mImageViewFilterIcon.setImageResource(R.drawable.actionbar_filter_icon_default);
        if (!TextUtils.isEmpty(mFilteredGenres) || !TextUtils.isEmpty(mFilteredLanguages)) {
            mImageViewFilterIcon.setImageResource(R.drawable.actionbar_filter_icon_default_non_empty);
        }

        prepareFilterData();
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

    private void showNoDataMessage() {
        if (mErrorTextView != null) {
            mListView.setVisibility(View.GONE);
            mGridView.setVisibility(View.GONE);
            if (mErrorTextView != null) {
                if (isFilterContent) {
                    mErrorTextView.setText(mContext.getString(R.string.error_fetch_filter_data));
                }
                mErrorTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchFilterDataWithLanguageAndGenre() {
       // updateToolbarTitle();
        isFilterContent = true;
        mIsLoadingMoreAvailable = true;
        RequestContentList.Params contentListparams = new RequestContentList.Params(mContentType, mStartIndex, APIConstants.PAGE_INDEX_COUNT, mFilteredLanguages, mFilteredGenres,orderBy,PrefUtils.getInstance().getPrefpublisherGroupIds_Android());
        RequestContentList mRequestContentList = new RequestContentList(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if (!mIsLoadingMorePages && (response == null
                                || response.body() == null)) {
                            showNoDataMessage();
                            return;
                        }

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
                        if (mIsLoadingMorePages) {
                            mIsLoadingMorePages = false;
                            if (mAdapterMovieList != null) {
                                mAdapterMovieList.add(dataList);
                                mAdapterMovieList.notifyDataSetChanged();
                            }
                            if (mFooterPbBar != null) {
                                mFooterPbBar.setVisibility(View.GONE);
                            }
                            if (mGridFooterTextViewLoading != null) {
                                mGridFooterTextViewLoading.setVisibility(View.GONE);
                            }
                            return;
                        }
                        mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, dataList);
                        mAdapterMoviesGrid.setCarouselInfoData(mCarouselInfoData);
                        mAdapterMovieList = new AdapterMovieList(mContext, dataList);
                        mListView.setAdapter(mAdapterMovieList);
                        mGridView.setAdapter(mAdapterMoviesGrid);
                        mAdapterMovieList.notifyDataSetChanged();
                        mAdapterMoviesGrid.notifyDataSetChanged();
                        mGridView.setOnScrollListener(mScrollListener);
                        mListView.setOnScrollListener(mScrollListener);
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

    private void updateToolbarTitle() {
        mToolbarTitle.setVisibility(View.GONE);
//        channelImageView.setVisibility(View.GONE);

        channelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
        channelImageView.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
        channelImageView.setVisibility(View.VISIBLE);
        channelImageView.setImageDrawable(getResources().getDrawable(R.drawable.toolbar_logo));
    }

    private void fetchArtistCarouselData(final ProfileAPIListAndroid profileAndroid){
        ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileAndroid.type, mStartIndex,
                profileAndroid.pageCount,artistName,
                profileAndroid.publishingHouseId, profileAndroid.orderBy, "-1", profileAndroid.language,profileAndroid.tags);

        ArtistProfileContentList artistProfileMovieList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                List<CardData> mDataList = response.body().results;
                if (mDataList == null || mDataList.isEmpty()) {
                    LoggerD.debugLog("empty or null dataList returning back.");
                    if (!mIsLoadingMorePages)
                        showNoDataMessage();
                    else {
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.GONE);
                        }
                        if (mGridFooterTextViewLoading != null) {
                            mGridFooterTextViewLoading.setVisibility(View.GONE);
                        }
                    }
                    return;
                }

                if (mDataList.size() < profileAndroid.pageCount) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mAdapterMovieList != null) {
                        mAdapterMovieList.add(mDataList);
                    }
                    if (mFooterPbBar != null) {
                        mFooterPbBar.setVisibility(View.GONE);
                    }
                    if (mGridFooterTextViewLoading != null) {
                        mGridFooterTextViewLoading.setVisibility(View.GONE);
                    }
                    return;
                }

                if (mDataList == null) {
                    showNoDataMessage();
                    return;
                }
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, mDataList);
                mAdapterMovieList = new AdapterMovieList(mContext, mDataList);
                mAdapterMovieList.showTitle(true);
                mAdapterMoviesGrid.showTitle(true);
                mListView.setAdapter(mAdapterMovieList);
                mGridView.setAdapter(mAdapterMoviesGrid);

                mGridView.setOnScrollListener(mScrollListener);
                mListView.setOnScrollListener(mScrollListener);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(artistProfileMovieList);
    }

    private void fetchCarouselData() {
        //Log.d(TAG, "fetchCarouselData:");
        if (mCarouselInfoData == null && !getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
            return;
        }
        final int totalPageCount=(mCarouselInfoData==null)?getArguments().getInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT):((mCarouselInfoData.pageSize > 0) ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT);
        final int pageSize = 10;
        mIsLoadingMoreAvailable = true;
        new MenuDataModel().fetchCarouseldata(mContext, (mCarouselInfoData == null) ? getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) : mCarouselInfoData.name, mStartIndex, pageSize, true,mCarouselInfoData.modified_on, new MenuDataModel.CarouselContentListCallback() {
            @Override
            public void onCacheResults(List<CardData> dataList) {
                if (dataList == null || dataList.isEmpty()) {
                    LoggerD.debugLog("empty or null dataList returning back.");
                    if (!mIsLoadingMorePages)
                        showNoDataMessage();
                    else {
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.GONE);
                        }
                        if (mGridFooterTextViewLoading != null) {
                            mGridFooterTextViewLoading.setVisibility(View.GONE);
                        }
                    }
                    return;
                }
                if (dataList != null && dataList.size() < totalPageCount) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mAdapterMovieList != null && dataList != null) {
                        mAdapterMovieList.add(dataList);
                    }
                    if (mFooterPbBar != null) {
                        mFooterPbBar.setVisibility(View.GONE);
                    }
                    if (mGridFooterTextViewLoading != null) {
                        mGridFooterTextViewLoading.setVisibility(View.GONE);
                    }
                    return;
                }

                if (dataList == null) {
                    showNoDataMessage();
                    return;
                }
                List<CardData> cardDataList = new ArrayList<>(dataList);
                mAdapterMovieList = new AdapterMovieList(mContext, cardDataList);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, cardDataList);
                mAdapterMoviesGrid.setCarouselInfoData(mCarouselInfoData);
                if (mCarouselInfoData != null) {
                    mAdapterMovieList.showTitle(mCarouselInfoData.showTitle);
                    mAdapterMoviesGrid.showTitle(mCarouselInfoData.showTitle);
                    mAdapterMovieList.setBgColor(mCarouselInfoData.bgColor);
                    mAdapterMoviesGrid.setBgColor(mCarouselInfoData.bgColor);
                }
                mGridView.setAdapter(mAdapterMoviesGrid);
                mListView.setAdapter(mAdapterMovieList);

                mGridView.setOnScrollListener(mScrollListener);
                mListView.setOnScrollListener(mScrollListener);
            }

            @Override
            public void onOnlineResults(List<CardData> dataList) {
                if (dataList == null || dataList.isEmpty()) {
                    LoggerD.debugLog("empty or null dataList returning back.");
                    if (!mIsLoadingMorePages) {
                        showNoDataMessage();
                    } else {
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.GONE);
                        }
                        if (mGridFooterTextViewLoading != null) {
                            mGridFooterTextViewLoading.setVisibility(View.GONE);
                        }
                    }
                    return;
                }

                if (dataList != null
                        && dataList.size() < totalPageCount) {
                    mIsLoadingMoreAvailable = false;
                }

                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (dataList != null && mAdapterMovieList != null) {
                        mAdapterMovieList.add(dataList);
                    }
                    if (mFooterPbBar != null) {
                        mFooterPbBar.setVisibility(View.GONE);
                    }
                    if (mGridFooterTextViewLoading != null) {
                        mGridFooterTextViewLoading.setVisibility(View.GONE);
                    }
                    return;
                }

                if (dataList == null) {
                    showNoDataMessage();
                    return;
                }
                List<CardData> cardDataList = new ArrayList<>(dataList);
                mAdapterMovieList = new AdapterMovieList(mContext, cardDataList);
                mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, cardDataList);
                mAdapterMoviesGrid.setCarouselInfoData(mCarouselInfoData);
                if (mCarouselInfoData != null) {
                    mAdapterMovieList.showTitle(mCarouselInfoData.showTitle);
                    mAdapterMoviesGrid.showTitle(mCarouselInfoData.showTitle);
                }
                mGridView.setAdapter(mAdapterMoviesGrid);
                mListView.setAdapter(mAdapterMovieList);

                mGridView.setOnScrollListener(mScrollListener);
                mListView.setOnScrollListener(mScrollListener);
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                if (mFooterPbBar != null) {
                    mFooterPbBar.setVisibility(View.GONE);
                }
                if (mGridFooterTextViewLoading != null) {
                    mGridFooterTextViewLoading.setVisibility(View.GONE);
                }
            }
        });
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
                  /*  if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }*/
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

    private void hideToolbar() {
        if (mToolbar == null) {
            return;
        }
        mToolbar.setVisibility(View.GONE);
    }

    private void initUI() {

        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.setVisibility(View.VISIBLE);
        mToolbar.addView(mInflateView);
        mToolbar.setContentInsetsAbsolute(0, 0);

        mAdapterMovieList = new AdapterMovieList(mContext, getDummyList());
        mAdapterMoviesGrid = new AdapterMoviesForGrid(mContext, getDummyList());
        mAdapterMoviesGrid.setCarouselInfoData(mCarouselInfoData);
        mGridView.setAdapter(mAdapterMoviesGrid);
        mGridView.setNumColumns(Util.getNumColumns(mContext,mCarouselInfoData));
        mGridView.setOnItemClickListener(mOnItemClickListener);
        mGridView.setOnItemLongClickListener(mOnItemLongClickListener);
        mListView.setAdapter(mAdapterMovieList);
        if (ApplicationController.sIsGridView) {
            switchToGridLayout();
        }
        mListView.setOnItemClickListener(mOnItemClickListener);
        mFooterView = getActivity().getLayoutInflater().inflate(R.layout.view_footer_layout,
                mListView, false);
        mFooterPbBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mFooterPbBar.setIndeterminate(false);
            mFooterPbBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mFooterPbBar.setVisibility(View.GONE);
        mListView.addFooterView(mFooterView);
        mToolbarCloseButton.setOnClickListener(mCloseAction);
        mToolbarTitle.setVisibility(View.VISIBLE);
        mImageViewFilterIcon.setVisibility(View.GONE);
        if (PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType)
                && isFilterSupports() && isFilterEnabled) {
            mToolbarTitle.setVisibility(View.GONE);
            mImageViewFilterIcon.setOnClickListener(mFilterIconClickListener);
            mImageViewFilterIcon.setVisibility(View.GONE);
            mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
        }
        if (ApplicationController.ENABLE_FILTER_THROUGH_VIEW
                && isFilterSupports() && isFilterEnabled) {
            mToolbarTitle.setVisibility(View.VISIBLE);
            mImageViewFilterIcon.setVisibility(View.GONE);
            mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
            mImageViewFilterIcon.setOnClickListener(mFilterIconClickListener);
        }
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mContentType)) {
            mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
            mToolbarCloseButton.setVisibility(View.VISIBLE);
        }
        if ((mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.title)) || !TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE))) {
            mToolbarTitle.setText(mCarouselInfoData != null ? mCarouselInfoData.title : getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
        }



        if (mFragmentType!=null&&mFragmentType.equals(PARAM_FRAGMENT_TYPE_ARTIST_PROFILE)){
            if (profileAPIListAndroid!=null&&profileAPIListAndroid.displayName!=null&&!TextUtils.isEmpty(profileAPIListAndroid.displayName)){
                mToolbarTitle.setText(profileAPIListAndroid.displayName);
                mImageViewFilterIcon.setVisibility(View.GONE);
                mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
            }
        }else {
            if (mCarouselInfoData == null) {
                return;
            }
            String imageLink = getImageLink(mCarouselInfoData.images);
            if (imageLink == null || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0 || !isFilterEnabled) {
                channelImageView.setVisibility(View.GONE);
            } else if (isFilterEnabled) {
                channelImageView.setVisibility(View.VISIBLE);
                if (imageLink.contains(APIConstants.PARAM_SCALE_WRAP)) {
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


//            if (imageLink.contains(APIConstants.PARAM_SCALE_WRAP)) {
//                channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
//                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
//            } else {
//                channelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
//                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
//            }
                PicassoUtil.with(mContext)
                        .load(imageLink,channelImageView);
            }
            if (!isFilterEnabled)
                channelImageView.setVisibility(View.GONE);

            if ((mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.title)) || !TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE))) {
                mToolbarTitle.setText(mCarouselInfoData != null ? mCarouselInfoData.title : getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
            }

            if (!TextUtils.isEmpty(mCarouselInfoData.bgSectionColor)) {
                try {
                    mInflateView.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgSectionColor));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (mCarouselInfoData!=null&&mCarouselInfoData.bgColor!=null){
            mainBackgroundLayout.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            mToolbar.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
            mErrorTextView.setTextColor(mContext.getResources().getColor(R.color.black));
            mToolbarTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            mToolbarCloseButton.setImageResource(R.drawable.actionbar_back_icon_icon);
            mInflateView.setBackgroundColor(Color.parseColor(mCarouselInfoData.bgColor));
        }
    }

    private boolean isFilterSupports() {
        return !(mCarouselInfoData != null
                && APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(mCarouselInfoData.showAllLayoutType));
    }

    private List<CardData> getDummyList() {
        List<CardData> dummyList = new ArrayList<>();
        Collections.addAll(dummyList, new CardData[8]);
        return dummyList;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        ScopedBus.getInstance().post(new ChangeMenuVisibility(false, MainActivity.SECTION_OTHER));
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
    }


    public static FragmentCarouselViewAll newInstance(Bundle args) {
        FragmentCarouselViewAll fragmentCarouselViewAll = new FragmentCarouselViewAll();
        fragmentCarouselViewAll.setArguments(args);
        return fragmentCarouselViewAll;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mContentType) && mCarouselInfoData != null) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_MOVIES_LIST, Analytics.NUMBER_OF_CARDS, 1L);
            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_MOVIE, mCarouselInfoData.title, true);
        }
    }


    private void mixpanelEventHungamaContent(CardData cardData) {

        if (cardData == null) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        if (cardData.generalInfo != null) {
            if (!TextUtils.isEmpty(cardData.generalInfo.type)) {
                params.put(Analytics.CONTENT_TYPE, cardData.generalInfo.type);
            }
            if (!TextUtils.isEmpty(cardData.generalInfo.title)) {
                Analytics.createEventGA(Analytics.CATEGORY_SDK, Analytics.EVENT_ACTION_HUNGAMA_CONTENT, cardData.generalInfo.title, 1l);
                params.put(Analytics.PROPERTY_CONTENT_NAME, cardData.generalInfo.title);
            }
            params.put(Analytics.PARTNER_CONTENT_ID, cardData.generalInfo.partnerId);
            params.put(Analytics.PROPERTY_CONTENT_ID, cardData._id);
        }

        if (cardData.publishingHouse != null
                && cardData.publishingHouse.publishingHouseName != null
                && !TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName)) {
            params.put(Analytics.PARTNER_NAME, cardData.publishingHouse.publishingHouseName);
        }


        Analytics.trackEvent(Analytics.EventPriority.LOW, Analytics.EVENT_HUNGAMA_CONTENT, params);
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_HUNGMA_SDK_USED);
        AppsFlyerTracker.aftrackEvent(Analytics.EVENT_ACTION_HUNGAMA_CONTENT, new HashMap<String, Object>(params), Analytics.EventPriority.HIGH);
    }


    private HashMap<Integer, ArrayList<String>> filterMap = new HashMap<>();
    ;
    private PopUpWindow mFilterMenuPopupWindow;
    private View mFilterMenuPopup;


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
                && TextUtils.isEmpty(genreValues)) {
            mToolbarCloseButton.performClick();
            return;
        }

        mStartIndex = 1;
        mFilteredLanguages = langValues;
        mFilteredGenres = genreValues;
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

        FilterRequest.Params requestParams = new FilterRequest.Params("movie");
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

        if (languagesList != null && genresDataList != null && languagesList.size() == 0 && genresDataList.size() == 0) {
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
        mGridView.setNumColumns(Util.getNumColumns(mContext,mCarouselInfoData));
        super.onConfigurationChanged(newConfig);
    }


}
