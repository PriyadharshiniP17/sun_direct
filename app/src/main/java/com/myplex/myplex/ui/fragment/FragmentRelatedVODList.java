package com.myplex.myplex.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterRelatedVODList;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Apalya on 15-Dec-15.
 */
public class FragmentRelatedVODList extends BaseFragment implements CacheManager.CacheManagerCallback {

    public static final String PARAM_SELECTED_VOD_DATA = "selectedVODCardData";
    private static final String TAG = FragmentRelatedVODList.class.getSimpleName();
    public static final String PARAM_BG_SECTION_COLOR = "bg_section_color";
    public static final String PARAM_BG_SECTION_LOGO_URL = "logo_url";
    public static final String PARAM_SEARCH_QUERY = "search_query";
    private ListView mListViewRelatedVOD;
    private TextView mToolbarTitle;
    private TextView mTextViewErroFetch;
    private ImageView mHeaderImageView;
    private ImageView channelImageView;
    private AdapterRelatedVODList mAdapterRelatedVODList;
    private RelativeLayout mRootLayout;
    private CardData mRelatedVODData;
    private String contentId;
    private final CacheManager mCacheManager = new CacheManager();
    private int mStartIndex = 1;
    private View mFooterView;
    private ProgressBar mFooterPbBar;
    private boolean mIsLoadingMorePages = false;
    private boolean mIsLoadingMoreAvailable = true;
    private View rootView;
    private LayoutInflater mInflater;
    private Toolbar mToolbar;
    private View mCustomToolBarLayout;

    private View.OnClickListener mOnClickCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            getActivity().finish();
            //showOverFlowSettings(v);
            mBaseActivity.removeFragment(FragmentRelatedVODList.this);
        }
    };

    private AlertDialogUtil.NeutralDialogListener mNeutralDialogListener = new AlertDialogUtil.NeutralDialogListener() {
        @Override
        public void onDialogClick(String buttonText) {
            if (null != buttonText &&
                    buttonText.equalsIgnoreCase(mContext.getString(R.string.play_button_retry))) {
                fetchRelatedVODData(true);

            }
        }
    };
    private ProgressDialog mProgressDialog;
    private String mContentType;
    private String mSearchQuery;
    private String mNotificationCarouselTitle;

    private boolean isHooqContent(CardData cardData) {
        if (cardData == null
                || cardData.publishingHouse == null) {
            return false;
        }

        if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)) {
            return true;
        }
        return false;
    }

    private AdapterView.OnItemClickListener mOnItemClickVODList = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            // launch card details
            if (position >= mAdapterRelatedVODList.getCount()) {
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
            CardData cardData = mAdapterRelatedVODList.getItem(position);

            if (cardData == null
                    || cardData._id == null) {
                return;
            }

/*            if (cardData.generalInfo != null
                    && cardData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
               *//* if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                        || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                    Analytics.gaBrowseTVShows(cardData.generalInfo.title);
                } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)) {
                    Analytics.gaBrowseVideos(cardData.generalInfo.title);
                }
                //Log.d(TAG, "type: " + cardData.generalInfo.type + " title: " + cardData.generalInfo.title);
                pushRelatedVODListFragment(cardData);
                return;*//*

            }*/
            CacheManager.setSelectedCardData(cardData);
            if (cardData != null
                    && cardData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                    && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                return;
            }
            showCardDetailsFragment(cardData);
        }
    };

    private void pushRelatedVODListFragment(CardData cardData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));
    }


    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
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
                        dismissProgressBar();
                        mFooterPbBar.setVisibility(View.VISIBLE);
                    }
                    mStartIndex++;
                    if (mRelatedVODData != null
                            && mRelatedVODData.generalInfo != null
                            && APIConstants.TYPE_TVSERIES.equalsIgnoreCase(mRelatedVODData.generalInfo.type)) {
                        fetchTVEpisodes();
                        return;
                    }
                    fetchRelatedVODData(true);
                }
            }
        }
    };
    private String mToolbarIconUrl;
    private String mToolbarBGColor;
    private boolean mIsFromViewAll;
    private TextView mTextViewSeason;
    private RelativeLayout mLayoutTVSeasons;
    private List<CardData> mListSeasons;
    private PopUpWindow mPopUpWindowSeasons;
    private int mSelectedSeasonPosition;
    private List<String> mListSeasonNames;
    private ImageView mImageButtonAllSeasons;


    private void showCardDetailsFragment(CardData cardData) {
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails
                .PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (mRelatedVODData != null
                && mRelatedVODData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, mRelatedVODData.generalInfo.type);
        }
        int partnerType = CardDetails.Partners.APALYA;
        partnerType = Util.getPartnerTypeContent(cardData);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, partnerType);
        String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_LIVE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)) {
            if (mAdapterRelatedVODList != null)
                CacheManager.setCardDataList(mAdapterRelatedVODList.getAllItems());
        }
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, selectedCarouselPosition);

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        if (mCarouselInfoData != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mCarouselInfoData.title);

        if (mSearchQuery != null) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSearchQuery);
        }

        if(!TextUtils.isEmpty(getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE))){
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS,getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
            args.putString(Analytics.PROPERTY_SOURCE,Analytics.VALUE_SOURCE_NOTIFICATION);
        }
        if (cardData.generalInfo != null
                && cardData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
            //Log.d(TAG, "type: " + cardData.generalInfo.type + " title: " + cardData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
        }
        if (mListSeasonNames != null) {
            String seasonName = mListSeasonNames.get(mSelectedSeasonPosition);
            args.putString(CardDetails.PARAM_SEASON_NAME, seasonName);
        }
        mBaseActivity.showDetailsFragment(args, cardData);
    }


    public static FragmentRelatedVODList newInstance(Bundle args) {
        FragmentRelatedVODList fragmentRelatedVODList = new FragmentRelatedVODList();
        fragmentRelatedVODList.setArguments(args);
        return fragmentRelatedVODList;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        mBaseActivity = (BaseActivity) getActivity();
        readBundleData();

        if (!mIsFromViewAll) {
            if (mRelatedVODData == null) {
                mBaseActivity.removeFragment(this);
                return null;
            }
        }

        mInflater = LayoutInflater.from(mContext);
        rootView = mInflater.inflate(R.layout.fragment_related_vodlist, container, false);

        mListViewRelatedVOD = (ListView) rootView.findViewById(R.id.listview_related_vods);
        mTextViewErroFetch = (TextView) rootView.findViewById(R.id.error_message);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        int statusBarHeight = Util.getStatusBarHeight(mContext);
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        layoutParams1.topMargin = statusBarHeight;
        mToolbar.setLayoutParams(layoutParams1);
        mToolbar.setContentInsetsAbsolute(0, 0);

        //Set up Toolbar title
        mCustomToolBarLayout = mInflater.inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mCustomToolBarLayout.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mCustomToolBarLayout.findViewById(R.id.toolbar_settings_button);
        channelImageView = (ImageView) mCustomToolBarLayout.findViewById(R.id.toolbar_tv_channel_Img);
        mRootLayout = (RelativeLayout) mCustomToolBarLayout.findViewById(R.id.custom_toolbar_layout);
        mToolbar.addView(mCustomToolBarLayout);

        mLayoutTVSeasons = (RelativeLayout) rootView.findViewById(R.id.layout_season_drop_down);
        mTextViewSeason = (TextView) rootView.findViewById(R.id.header_title_text);
        mImageButtonAllSeasons = (ImageView) rootView.findViewById(R.id.drop_down_button);
        if (mRelatedVODData != null) {
            if (mRelatedVODData.globalServiceId != null) {
                contentId = mRelatedVODData.globalServiceId;
            } else {
                contentId = mRelatedVODData._id;
            }
        }
        mStartIndex = 1;
        initUI();
        if (mRelatedVODData != null
                && mRelatedVODData.generalInfo != null
                && APIConstants.TYPE_TVSERIES.equalsIgnoreCase(mRelatedVODData.generalInfo.type)) {
            contentId = mRelatedVODData._id;
            prepareTVSeriesUI();
            fetchTVSeasons();
            return rootView;
        }
        fetchRelatedVODData(true);

        return rootView;
    }

    int selectedCarouselPosition = -1;

    private void readBundleData() {
        Bundle args = getArguments();
        if (args == null) return;
        //Log.d(TAG, "onCreateView()");
        mRelatedVODData = null;
        if (args.containsKey(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA)) {
            mRelatedVODData = (CardData) args.getSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA);
        }
        mContentType = null;
        if (args.containsKey(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE)) {
            mContentType = (String) args.getSerializable(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE);
        }
        mSearchQuery = null;
        if (args.containsKey(PARAM_SEARCH_QUERY)) {
            mSearchQuery = (String) args.getSerializable(PARAM_SEARCH_QUERY);
        }
        selectedCarouselPosition = args.getInt(CleverTap.PROPERTY_CAROUSEL_POSITION, -1);
        mCarouselInfoData = null;
        mToolbarIconUrl = null;
        mToolbarBGColor = null;

        mCarouselInfoData = CacheManager.getCarouselInfoData();

        if (mCarouselInfoData != null && mCarouselInfoData.images != null) {
            mToolbarIconUrl = getImageLink(mCarouselInfoData.images);
        }
        if (mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.bgColor)) {
            mToolbarBGColor = mCarouselInfoData.bgColor;
        }
        if (args.containsKey(FragmentRelatedVODList.PARAM_BG_SECTION_LOGO_URL)) {
            mToolbarIconUrl = args.getString(FragmentRelatedVODList.PARAM_BG_SECTION_LOGO_URL);
        }
        if (args.containsKey(FragmentRelatedVODList.PARAM_BG_SECTION_COLOR)) {
            mToolbarBGColor = args.getString(FragmentRelatedVODList.PARAM_BG_SECTION_COLOR);
        }

        if (mCarouselInfoData == null) {
            if (TextUtils.isEmpty(mToolbarIconUrl) || TextUtils.isEmpty(mToolbarBGColor)) {
                if (mRelatedVODData != null
                        && mRelatedVODData.publishingHouse != null
                        && mRelatedVODData.publishingHouse.publishingHouseName != null
                        && mRelatedVODData.publishingHouse.publishingHouseName.equalsIgnoreCase(APIConstants.TYPE_HOOQ)) {
                    mToolbarIconUrl = PrefUtils.getInstance().getPrefHooqLogoImageUrl();
                    mToolbarBGColor = PrefUtils.getInstance().getPrefHooqBgsectionColor();
                }
            }
        }

        mIsFromViewAll = args.containsKey(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL);

        if (args.containsKey(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE)) {
            mFragmentType = args.getString(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE);
        }

        if (args.containsKey(FragmentCarouselViewAll.PARAM_LANGUAGE_FILTER_VALUE)) {
            mFilteredLanguages = args.getString(FragmentCarouselViewAll.PARAM_LANGUAGE_FILTER_VALUE);
        }

        if (args.containsKey(FragmentCarouselViewAll.PARAM_GENRE_FILTER_VALUE)) {
            mFilteredGenres = args.getString(FragmentCarouselViewAll.PARAM_GENRE_FILTER_VALUE);
        }
        mNotificationCarouselTitle = args.getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE);

        args.clear();
    }

    private DatesAdapter mPopupListAdapter;
    private ListView mPopUpListView;

    private void showSeasonsPopUpWindow(View view) {
        mPopUpWindowSeasons.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }

    private List<String> getDummySeasons() {
        int size = 1;
        List<String> dummyList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dummyList.add("Loading...");
        }
        return dummyList;
    }

    private void fetchTVEpisodes() {
        if (mRelatedVODData == null
                || mListSeasons == null
                || mListSeasons.isEmpty()) {
            return;
        }
        CardData seasonData = mListSeasons.get(mSelectedSeasonPosition);
        //Update selected season text on drop down header
        updateDropDownTitle();
        if (!mIsLoadingMorePages) {
            showProgressBar();
        }
        mCacheManager.getRelatedVODList(seasonData._id, mStartIndex, true,
                FragmentRelatedVODList.this);
    }

    private void prepareSeasonsPopup() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_window, null);
        mPopUpWindowSeasons = new PopUpWindow(layout);
        // TODO Add dummy data for seasons
//        mPopupListAdapter = new DatesAdapter(mContext,new ArrayList<String>(0));
        mPopupListAdapter =
                new DatesAdapter(mContext, DatesAdapter.AdapterType.SEASONS, getDummySeasons());
        mPopUpListView = (ListView) layout.findViewById(R.id.popup_listView);
        mPopUpListView.setAdapter(mPopupListAdapter);
        mPopUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPopUpWindowSeasons.dismissPopupWindow();
                mSelectedSeasonPosition = position;
                mStartIndex = 1;
                fetchTVEpisodes();
            }
        });
    }

    private void prepareTVSeriesUI() {
        prepareSeasonsPopup();
        mLayoutTVSeasons.setVisibility(View.VISIBLE);
        mTextViewSeason.setVisibility(View.VISIBLE);
        mLayoutTVSeasons.setOnClickListener(mTodayEPGListener);
        mTextViewSeason.setOnClickListener(mTodayEPGListener);
        mImageButtonAllSeasons.setOnClickListener(mTodayEPGListener);
    }

    private void fetchTVSeasons() {
        if (contentId == null) {
            return;
        }
        mCacheManager.getRelatedVODListTypeExclusion(contentId, 1, true, APIConstants.TYPE_TVSEASON,
                15,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            showNoDataMessage();
                            return;
                        }
                        mListSeasons = dataList;
                        mIsLoadingMoreAvailable = true;
                        if (dataList != null && dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                            mIsLoadingMoreAvailable = false;
                        }
                        updateSeasons(dataList);
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            showNoDataMessage();
                            return;
                        }
                        mListSeasons = dataList;
                        mIsLoadingMoreAvailable = true;
                        if (dataList != null && dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                            mIsLoadingMoreAvailable = false;
                        }
                        updateSeasons(dataList);
                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                        if (mFooterPbBar != null) {
                            mFooterPbBar.setVisibility(View.GONE);
                        }
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
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

    private void updateSeasons(List<CardData> dataList) {
        if (dataList == null
                || dataList.isEmpty()) {
            return;
        }
//        dataList = sortDataList(dataList);
        mListSeasonNames = prepareSeasonNames(dataList);
        mPopupListAdapter = new DatesAdapter(mContext, DatesAdapter.AdapterType.SEASONS, mListSeasonNames);
        mPopUpListView.setAdapter(mPopupListAdapter);
        fetchTVEpisodes();
    }

    private List<String> prepareSeasonNames(List<CardData> dataList) {
        List<String> seasons = new ArrayList<>();
        String seasonText = "Season ";
        for (Iterator<CardData> iterator = dataList.iterator(); iterator.hasNext(); ) {
            CardData seasonData = iterator.next();
            if (seasonData.content != null
                    && seasonData.content.serialNo != null) {
                seasons.add(seasonText + seasonData.content.serialNo);
            }
        }
        return seasons;
    }

    private List<CardData> sortDataList(List<CardData> dataList) {
        Collections.sort(dataList, new Comparator<CardData>() {
            @Override
            public int compare(CardData lhs, CardData rhs) {
                if (lhs == null
                        || lhs.content == null
                        || lhs.content.serialNo == null
                        || rhs == null
                        || rhs.content == null
                        || rhs.content.serialNo == null) {
                    return -1;
                }
                try {
                    int lhsSerialNo = Integer.parseInt(lhs.content.serialNo);
                    int rhsSerialNo = Integer.parseInt(rhs.content.serialNo);
                    return lhsSerialNo > rhsSerialNo ? 1 : -1;
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return -1;
            }
        });
        return dataList;
    }

    private final View.OnClickListener mTodayEPGListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPopUpWindowSeasons != null
                    && mPopUpWindowSeasons.isPopupVisible()) {
                mPopUpWindowSeasons.dismissPopupWindow();
            } else {
                showSeasonsPopUpWindow(v);
            }
        }
    };

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

    private String mFragmentType;
    private boolean isFilterContent;
    private String mFilteredLanguages;
    private String mFilteredGenres;

    private void fetchRelatedVODData(boolean isCachRequest) {
        if (FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER.equalsIgnoreCase(mFragmentType)
                || isFilterContent) {
            fetchFilterDataWithLanguageAndGenre();
            return;
        }
        if (!mIsFromViewAll) {
            mCacheManager.getRelatedVODList(contentId, mStartIndex, isCachRequest,
                    FragmentRelatedVODList.this);
            return;
        }
        fetchCarouselData();
        if (mCarouselInfoData != null) {
            Analytics.setVideosCarouselName(mCarouselInfoData.title);
        }
    }


    private void updateDropDownTitle() {
        if (mTextViewSeason != null
                && !mListSeasonNames.isEmpty()) {
            mTextViewSeason.setText(mListSeasonNames.get(mSelectedSeasonPosition));
        }
    }

    private void initUI() {

        updateChannelImage();
        mAdapterRelatedVODList = new AdapterRelatedVODList(mContext, getDummyVODList(),
                mRelatedVODData != null
                        && mRelatedVODData.generalInfo != null
                        && mRelatedVODData.generalInfo.type != null ?
                        mRelatedVODData.generalInfo.type : null);
        mFooterView = mInflater.inflate(R.layout.view_footer_layout, mListViewRelatedVOD, false);
        mFooterPbBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mFooterPbBar.setIndeterminate(false);
            mFooterPbBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, com.myplex.sdk.R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
        }
        mFooterPbBar.setVisibility(View.GONE);
        mListViewRelatedVOD.addFooterView(mFooterView);
        mListViewRelatedVOD.setAdapter(mAdapterRelatedVODList);
        mListViewRelatedVOD.setOnItemClickListener(mOnItemClickVODList);
        mHeaderImageView.setOnClickListener(mOnClickCloseAction);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void updateChannelImage() {
        if (!TextUtils.isEmpty(mToolbarIconUrl)) {
            channelImageView.setVisibility(View.VISIBLE);
            PicassoUtil.with(mContext)
                    .load(mToolbarIconUrl,channelImageView);

            if (mToolbarIconUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
                channelImageView.getLayoutParams().width =  RelativeLayout.LayoutParams.WRAP_CONTENT;
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


//            if (mToolbarIconUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
//                channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
//                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
//            } else {
//                channelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
//                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
//            }
        } else {
            channelImageView.setImageResource(R.drawable.app_icon);
        }

        if (!TextUtils.isEmpty(mToolbarBGColor)) {
            try {
                mCustomToolBarLayout.setBackgroundColor(Color.parseColor(mToolbarBGColor));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ((mRelatedVODData != null
                && mRelatedVODData.generalInfo != null
                && mRelatedVODData.generalInfo.title != null) || !TextUtils.isEmpty(mNotificationCarouselTitle)) {
            mToolbarTitle.setText((TextUtils.isEmpty(mNotificationCarouselTitle)) ? mRelatedVODData.generalInfo.title.substring(0, 1).toUpperCase() +
                    mRelatedVODData.generalInfo.title.substring(1) : mNotificationCarouselTitle);
        } else if ((mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.title)) || !TextUtils.isEmpty(mNotificationCarouselTitle)) {
            mToolbarTitle.setText((TextUtils.isEmpty(mNotificationCarouselTitle)) ? mCarouselInfoData.title : mNotificationCarouselTitle);
        }

    }

    private List<CardData> getDummyVODList() {
        List dummyList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dummyList.add(new CardData());
        }
        return dummyList;
    }

    private void fillData(List<CardData> vodListData) {
        if (vodListData != null
                && vodListData.isEmpty()) {
            showNoDataMessage();
            return;
        }
        List<CardData> vodCardDataList= new ArrayList<>(vodListData);
        mListViewRelatedVOD.setVisibility(View.VISIBLE);
        mTextViewErroFetch.setVisibility(View.GONE);
        mAdapterRelatedVODList = new AdapterRelatedVODList(mContext, vodCardDataList,
                mRelatedVODData != null
                        && mRelatedVODData.generalInfo != null
                        && mRelatedVODData.generalInfo.type != null ?
                        mRelatedVODData.generalInfo.type : null);
        mListViewRelatedVOD.setAdapter(mAdapterRelatedVODList);
        mListViewRelatedVOD.setOnScrollListener(mScrollListener);
        mAdapterRelatedVODList.setParentCardData(mRelatedVODData);
        mAdapterRelatedVODList.notifyDataSetChanged();
    }

    @Override
    public void OnCacheResults(List<CardData> dataList) {
        dismissProgressBar();
        if (mFooterPbBar != null) {
            mFooterPbBar.setVisibility(View.GONE);
        }
        if (dataList == null) {
            if (mListViewRelatedVOD != null) {
                mListViewRelatedVOD.setVisibility(View.INVISIBLE);
            }
            showNoDataMessage();
            return;
        }
        mIsLoadingMoreAvailable = true;
        if (mCacheManager.isLastPage()) {
            mIsLoadingMoreAvailable = false;
        }

        if (mIsLoadingMorePages) {
            mIsLoadingMorePages = false;
            if (mAdapterRelatedVODList != null && dataList != null) {
                mAdapterRelatedVODList.addAll(dataList);
            }
            return;
        }
        fillData(dataList);
    }

    @Override
    public void OnOnlineResults(List<CardData> dataList) {
        dismissProgressBar();
        if (mFooterPbBar != null) {
            mFooterPbBar.setVisibility(View.GONE);
        }
        if (dataList == null) {
            if (mListViewRelatedVOD != null) {
                mListViewRelatedVOD.setVisibility(View.INVISIBLE);
            }
            showNoDataMessage();
            return;
        }
        mIsLoadingMoreAvailable = true;
        if (mCacheManager.isLastPage()) {
            mIsLoadingMoreAvailable = false;
        }
        if (mIsLoadingMorePages) {
            mIsLoadingMorePages = false;
            if (mAdapterRelatedVODList != null && dataList != null) {
                mAdapterRelatedVODList.addAll(dataList);
            }
            return;
        }
        fillData(dataList);
    }

    @Override
    public void OnOnlineError(Throwable error, int errorCode) {
        dismissProgressBar();
        mIsLoadingMorePages = false;
        if (mFooterPbBar != null) {
            mFooterPbBar.setVisibility(View.GONE);
        }
        if (errorCode == APIRequest.ERR_NO_NETWORK) {
            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                    "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
            return;
        }
        showNoDataMessage();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void showNoDataMessage() {
        if (mTextViewErroFetch != null) {
            if (mTextViewErroFetch != null) {
                if (mRelatedVODData != null && mRelatedVODData.generalInfo != null && !APIConstants.TYPE_VODCHANNEL.equals(mRelatedVODData.generalInfo.type)) {
                    mTextViewErroFetch.setText(mContext.getString(R.string.error_fetch_videos));
                }
                if (mListViewRelatedVOD != null)
                    mListViewRelatedVOD.setVisibility(View.GONE);
                mTextViewErroFetch.setVisibility(View.VISIBLE);
            }
        }
    }


    private CarouselInfoData mCarouselInfoData;

    private void fetchCarouselData() {
        //Log.d(TAG, "fetchCarouselData:");
        if (mCarouselInfoData == null) {
            return;
        }
        final int pageSize = (mCarouselInfoData == null) ? getArguments().getInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT) : mCarouselInfoData.pageSize > 0 ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
        new MenuDataModel().fetchCarouseldata(mContext, (mCarouselInfoData == null) ? getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) : mCarouselInfoData.name, mStartIndex, pageSize, true, mCarouselInfoData.modified_on,new MenuDataModel.CarouselContentListCallback() {
            @Override
            public void onCacheResults(List<CardData> dataList) {
                if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                    showNoDataMessage();
                    return;
                }

                if (dataList != null && dataList.size() < pageSize) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mAdapterRelatedVODList != null) {
                        mAdapterRelatedVODList.addAll(dataList);
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

                if (dataList != null && dataList.size() < pageSize) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mAdapterRelatedVODList != null) {
                        mAdapterRelatedVODList.addAll(dataList);
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

    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }

    private void hideToolbar() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.GONE);
        }
    }


    public void showProgressBar() {

        if (mContext == null) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
        mProgressDialog.setContentView(R.layout.layout_progress_dialog);

    }

    public void dismissProgressBar() {
        try {
            if (!isAdded()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(TAG, "dismiss pop up window");
            if (mPopUpWindowSeasons != null) {
                mPopUpWindowSeasons.dismissPopupWindow();
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    private void fetchFilterDataWithLanguageAndGenre() {
//        updateToolbarTitle();
        isFilterContent = true;
        mIsLoadingMoreAvailable = true;
        RequestContentList.Params contentListparams = new RequestContentList.Params(mContentType, mStartIndex, APIConstants.PAGE_INDEX_COUNT, mFilteredLanguages, mFilteredGenres);
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
                        if (dataList == null || dataList.isEmpty()) {
                            return;
                        }
                        if (dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                            mIsLoadingMoreAvailable = false;
                        }
                        if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                            showNoDataMessage();
                            return;
                        }
//                dataList = getFilteredResultsData(dataList);

                        if (mIsLoadingMorePages) {
                            mIsLoadingMorePages = false;
                            if (mAdapterRelatedVODList != null) {
                                mAdapterRelatedVODList.addAll(dataList);
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
}
