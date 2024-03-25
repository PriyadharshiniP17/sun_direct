package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.PreviewProperties;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.views.AdCustomBannerLayout;
import com.myplex.myplex.ui.views.AdNativeVideoLayout;
import com.myplex.myplex.ui.views.AnimationRecyclerViewItem;
import com.myplex.myplex.ui.views.AnimationViewPagerItemBigHorizontal;
import com.myplex.myplex.ui.views.BannerHorizontalItem3D;
import com.myplex.myplex.ui.views.BigBannerHorizontalCarousel;
import com.myplex.myplex.ui.views.BigBannerVerticalCarousel;
import com.myplex.myplex.ui.views.BigHorizontalItem;
import com.myplex.myplex.ui.views.BigWeeklyTrendingItemNew;
import com.myplex.myplex.ui.views.CatchupItem;
import com.myplex.myplex.ui.views.ContinueWatchingItem;
import com.myplex.myplex.ui.views.ContinueWatchingItemLive;
import com.myplex.myplex.ui.views.ExtraSmallCoverItem;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.ui.views.LargeHorizontalItem;
import com.myplex.myplex.ui.views.LiveSquareHorizontalItem;
import com.myplex.myplex.ui.views.LiveTVProgramItem;
import com.myplex.myplex.ui.views.LongBigHorizontalItem;
import com.myplex.myplex.ui.views.MediumHorizontalItem;
import com.myplex.myplex.ui.views.MediumVerticalItem;
import com.myplex.myplex.ui.views.NestedCarouselItem;
import com.myplex.myplex.ui.views.NewsNestedCarouselItem;
import com.myplex.myplex.ui.views.NextProgramItem;
import com.myplex.myplex.ui.views.PartnerHorizontalItem;
import com.myplex.myplex.ui.views.PortraitViewPagerItem;
import com.myplex.myplex.ui.views.PosterListLargeItem;
import com.myplex.myplex.ui.views.PromoBannerItem;
import com.myplex.myplex.ui.views.RecentSearchItem;
import com.myplex.myplex.ui.views.RoundedArtistHorizontalItem;
import com.myplex.myplex.ui.views.SearchLanguageItem;
import com.myplex.myplex.ui.views.SingleBannerItem;
import com.myplex.myplex.ui.views.SingleBannerrPlayerItem;
import com.myplex.myplex.ui.views.SmallHorizontalItem;
import com.myplex.myplex.ui.views.SmallSquareHorizontalItem;
import com.myplex.myplex.ui.views.SquareBannerPlayerItem;
import com.myplex.myplex.ui.views.SquareListBigItem;
import com.myplex.myplex.ui.views.SquareViewPagerItem;
import com.myplex.myplex.ui.views.TextFlowLayoutItem;
import com.myplex.myplex.ui.views.UiCompoment;
import com.myplex.myplex.ui.views.ViewPagerItem;
import com.myplex.myplex.ui.views.VmaxImageAdItem;
import com.myplex.myplex.ui.views.VmaxVideoAdViewItem;
import com.myplex.myplex.ui.views.WeeklyTrendingSeasonItem;
import com.myplex.myplex.ui.views.posterview.StartSnapHelper;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import autoscroll.AutoScrollViewPager;
import viewpagerindicator.CircleIndicator;


public class AdapterCarouselInfo extends RecyclerView.Adapter<UiCompoment> {

    private static final String TAG = AdapterCarouselInfo.class.getSimpleName();
    private static final byte ITEM_TYPE_BANNER = 31;
    private static final byte ITEM_TYPE_HORIZONTAL_LIST_SMALL_ITEM = 32;
    private static final byte ITEM_TYPE_HORIZONTAL_LIST_MED_ITEM = 33;
    private static final byte ITEM_TYPE_HORIZONTAL_LIST_BIG_ITEM = 34;
    public static final byte ITEM_TYPE_VMAX_IMAGE_ADVERTISE = 35;
    public static final byte ITEM_TYPE_VMAX_VIDEO_ADVERTISE = 36;
    private static final byte ITEM_TYPE_LIVE_PROGRAM_ITEM = 37;
    static final int ITEM_TYPE_PROMO_BANNER = 38;
    static final int ITEM_TYPE_EMPTY_FOOTER = 39;
    private static final int ITEM_TYPE_CONTINUE_WATCHING = 40;
    private static final int ITEM_TYPE_SQUARE_LIST_SMALL_ITEM = 41;
    private static final int ITEM_TYPE_SINGLE_BANNER_ITEM = 42;
    private static final int ITEM_TYPE_HORIZONTAL_LIST_LARGE_ITEM = 43;
    private static final int LAYOUT_TYPE_EXCLUSIVE_HORIZONTA_LIST_BIG_ITEM = 44;
    private static final int ITEM_TYPE_NESTED_CAROUSEL = 45;
    private static final int ITEM_TYPE_PORTRAIT_BANNER = 46;
    private static final int ITEM_TYPE_SQUARE_BANNER = 47;
    private static final int ITEM_RECENT_SEARCH = 48;
    private static final int ITEM_TYPE_PORTRAIT_BANNER_PLAYER = 49;
    private static final int ITEM_TYPE_SQUARE_BANNER_PLAYER = 50;
    private static final byte ITEM_SQUARE_LIST_BIG = 51;
    public static final int ITEM_TYPE_MEDIUM_NATIVE_AD = 52;
    public static final int ITEM_TYPE_SMALL_NATIVE_AD = 53;
    public static final int ITEM_TYPE_MEDIUM_VERTICAL_ITEM=54;
    private static final int ITEM_TYPE_AUTOPLAY_SINGLE_BANNER = 55;
    private static final int ITEM_TYPE_3D_CAROUSEL = 56;
    private static final int ITEM_BIG_BANNER_VERTICAL_CAROUSEL=57;
    private static final int ITEM_WEEKLY_TRENDING_BIG_ITEM=67;
    private static final int ITEM_WEEKLY_TRENDING_MEDIUM_ITEM=68;
    private static final int ITEM_BIG_BANNER_VERTICAL_DOUBLE_TITLE_VERTICAL_CAROUSEL=58;
    private static final int ITEM_BIG_BANNER_HORIZONTAL_CAROUSEL=59;
    private static final int ITEM_BIG_HORIZONTAL_3D_CAROUSEL=60;
    private static final int ITEM_ROUNDED_ARTIST_CAROUSEL=61;
    private static final int ITEM_AD_CUSTOM_BANNER_LAYOUT=62;
    private static final int ITEM_NEWS_NESTED_CAROUSEL_ITEM=63;
    private static final int ITEM_TYPE_NATIVE_IMAGE=64;
    private static final int ITEM_TYPE_NATIVE_VIDEO=65;
    private static final int ITEM_TYPE_TRENDING_SEARCH=66;
    private static final int ITEM_TYPE_PARTNER=69;
    private static final int ITEM_TYPE_LIVE_CHANNEL_SMALL_ITEM=70;
    private static final int ITEM_TYPE_EXTRA_SMALL_COVERP=71;
    private static final int ITEM_TYPE_LONG_PORTRAIT_MEDIUM=72;
    private static final int ITEM_TYPE_NEXT_PROGRAM=75;
    private static final int ITEM_TYPE_CATCHUP=74;
    static final int ITEM_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM = 73;
    static final int ITEM_TYPE_TEXT_FLOW_LAYOUT = 76;
    static final int ITEM_TYPE_SEARCH_LANGUAGE = 77;
    static final int ITEM_TYPE_WEEKLY_TRENDING_SEASON_ITEM = 78;
    static final int ITEM_TYPE_CONTINUE_WATCHING_EPG = 79;
    private final PreviewProperties mPreviewProperties;

    private Context mContext;
    private List<CarouselInfoData> mListCarouselInfo;
    private RecyclerView mRecyclerViewCarouselInfo;
    private String mMenuGroup;
    private String mPageTitle;
    private Handler mHandler;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private CallbackListener callbackListener;
    private CarouselInfoData mCarouselInfoData;
    private List<CarouselInfoData> mDummyCarouselInfoData = new ArrayList<>();
    private boolean enableAutoRotate;
    private RelativeLayout layout;
    private String mBgColor, isFromApp;
    private boolean isFromSearch;
    private String globalServiceId;
    private List<CardData> textList=new ArrayList<>();

    private String seasonId;

    public void setCarouselInfoDataSection(CarouselInfoData mCarouselInfoData) {
        this.mCarouselInfoData = mCarouselInfoData;
    }

    public void setBgColor(String mBgColor) {
        this.mBgColor = mBgColor;
    }

    public interface CallbackListener {
        boolean isPageVisible();
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            CarouselInfoData carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;

            if (carouselInfoData != null && carouselInfoData.title != null) {
                gaBrowse(carouselData, carouselInfoData.title);
                //Log.d(TAG, "carouselSectionName: " + carouselInfoData.title);
            }

            Analytics.gaEventBrowsedCategoryContentType(carouselData);

            if (carouselInfoData != null) {
                Analytics.setVideosCarouselName(carouselInfoData.title);
            }
            /*if (carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                showRelatedVODListFragment(carouselData, parentPosition);
                return;
            }
*/
            String publishingHouse = carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;
            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, carouselInfoData, null);
                return;
            }
            LoggerD.debugLog("pacakge name- " + mContext.getPackageName());
            if (carouselData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !TextUtils.isEmpty(carouselData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, carouselData.generalInfo.title, 1l);
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, carouselData.generalInfo.title));
                return;
            }

            if (carouselInfoData == null) {
                showDetailsFragment(carouselData, position,"",parentPosition);
            } else {
                showDetailsFragment(carouselData, carouselInfoData,parentPosition);
            }
        }
    };

    private void gaBrowse(CardData movieData, String carouselSectionName) {

        if (movieData.generalInfo == null || movieData.generalInfo.title == null || carouselSectionName == null) {
            return;
        }

        if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_TVSHOWS, carouselSectionName, movieData.generalInfo.title);
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_VIDEOS, carouselSectionName, movieData.generalInfo.title);
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_MUSIC_VIDEOS, carouselSectionName, movieData.generalInfo.title);
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup)||APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_KIDS, carouselSectionName, movieData.generalInfo.title);
        } else if (mPageTitle != null) {
            Analytics.gaBrowseCarouselSection("browsed " + mPageTitle.toLowerCase(), carouselSectionName, movieData.generalInfo.title);
        }
    }


    public AdapterCarouselInfo(Context context, List<CarouselInfoData> listCarouselInfo) {
        mContext = context;
        mListCarouselInfo = listCarouselInfo;
        mHandler = new Handler(context.getMainLooper());
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
    }
    public AdapterCarouselInfo(Context context, List<CarouselInfoData> listCarouselInfo, boolean isFromSearch) {
        mContext = context;
        mListCarouselInfo = listCarouselInfo;
        mHandler = new Handler(context.getMainLooper());
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
        this.isFromSearch = isFromSearch;
    }
    public AdapterCarouselInfo(Context context, List<CarouselInfoData> listCarouselInfo, String isFromApp) {
        mContext = context;
        mListCarouselInfo = listCarouselInfo;
        mHandler = new Handler(context.getMainLooper());
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
        this.isFromApp = isFromApp;
    }
    public AdapterCarouselInfo(Context context, List<CarouselInfoData> listCarouselInfo, boolean isFromSearch, String globalServiceId) {
        Log.d(TAG, "AdapterCarouselInfo: globalServiceId "+ globalServiceId);
        mContext = context;
        mListCarouselInfo = listCarouselInfo;
        mHandler = new Handler(context.getMainLooper());
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
        this.isFromSearch = isFromSearch;
        this.globalServiceId = globalServiceId;
    }

    public AdapterCarouselInfo(Context context, List<CarouselInfoData> listCarouselInfo, boolean isFromSearch, String globalServiceId, String seasonId) {
        Log.d(TAG, "AdapterCarouselInfo: globalServiceId "+ globalServiceId);
        mContext = context;
        mListCarouselInfo = listCarouselInfo;
        mHandler = new Handler(context.getMainLooper());
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
        this.isFromSearch = isFromSearch;
        this.globalServiceId = globalServiceId;
        this.seasonId = seasonId;
    }

    private UiCompoment.UiComponentListenerInterface uiCompomentListenerInterface = new UiCompoment.UiComponentListenerInterface() {
        @Override
        public void notifyDataChanged(int position) {
            notifyItemChanged(position);
        }

        @Override
        public void notifyItemNeedToBeRemoved(final CarouselInfoData carouselInfoData,final int position) {
            if (mListCarouselInfo != null) {
                mListCarouselInfo.remove(carouselInfoData);
                if(mRecyclerViewCarouselInfo != null) {
                    try {
                                if(!mRecyclerViewCarouselInfo.isComputingLayout()) {
                                    notifyItemRangeChanged(position, mListCarouselInfo.size());
                                    notifyItemRemoved(position);
                                }else{
//                                    notifyItemNeedToBeRemoved(carouselInfoData,position);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

            }
        }

        @Override
        public void notifyItemNeedToBeRemoved(ProfileAPIListAndroid carouselInfoData, int position) {

        }
    };



    @Override
    public UiCompoment onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        UiCompoment viewHolder = null;
       // //Log.d(TAG, "onCreateViewHolder viewType- " + viewType+".."+mMenuGroup);
        switch (viewType) {
            case ITEM_TYPE_BANNER:
              /*  viewHolder = BannerNewHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);*/
                viewHolder = ViewPagerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
              /*  viewHolder = BigBannerHorizontalCarousel.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);*/
                break;
            case ITEM_TYPE_PORTRAIT_BANNER:
                viewHolder = PortraitViewPagerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,callbackListener,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_AUTOPLAY_SINGLE_BANNER:
                viewHolder = SingleBannerrPlayerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,
                        mMenuGroup,mPageTitle,mCarouselInfoData,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_SQUARE_BANNER:
                viewHolder = SquareViewPagerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,callbackListener,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            /*case ITEM_TYPE_SINGLE_BANNER_ITEM:
                viewHolder = SingleBannerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;*/
            case ITEM_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM :
                viewHolder = SingleBannerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);

                break;
            case ITEM_TYPE_SQUARE_LIST_SMALL_ITEM:
                viewHolder = SmallSquareHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo, isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_HORIZONTAL_LIST_LARGE_ITEM:
                viewHolder = LargeHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_PROMO_BANNER:
                viewHolder = PromoBannerItem.createView(mContext,parent,mListCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_HORIZONTAL_LIST_SMALL_ITEM:
                Log.d(TAG, "onCreateViewHolder: " + parent );
                viewHolder = SmallHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch, globalServiceId );
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_NEWS_NESTED_CAROUSEL_ITEM:
                viewHolder = NewsNestedCarouselItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_HORIZONTAL_LIST_MED_ITEM:
                viewHolder = MediumHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor, isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_MEDIUM_VERTICAL_ITEM:
                viewHolder = MediumVerticalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
              /*  viewHolder = MediumHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);*/
                break;
            case ITEM_TYPE_VMAX_VIDEO_ADVERTISE: //TODO:: Remove commented case block to visible ads
//                view = LayoutInflater.from(mContext).inflate(R.layout.listitem_vmax_video_ads, parent, false);
                viewHolder = VmaxVideoAdViewItem.createView(mContext,mCarouselInfoData,mRecyclerViewCarouselInfo);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_VMAX_IMAGE_ADVERTISE: //TODO:: Remove commented case block to visible ads
//                view = LayoutInflater.from(mContext).inflate(R.layout.listitem_vmax_image_ads, parent, false);
                viewHolder = VmaxImageAdItem.createView(mContext,mCarouselInfoData,mRecyclerViewCarouselInfo);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_LIVE_PROGRAM_ITEM:
                viewHolder = LiveTVProgramItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_EMPTY_FOOTER:
                view = new LinearLayout(mContext);
                int pagerHeight = 50;
                int vmaxAdHeightInDp = 50;
                if (DeviceUtils.isTablet(mContext)) {
                    vmaxAdHeightInDp = 90;
                    pagerHeight = 60;
                }
                if (Util.checkUserLoginStatus()) {
                    pagerHeight = (int) (vmaxAdHeightInDp + pagerHeight);
                }
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pagerHeight, mContext.getResources().getDisplayMetrics());
                SDKLogger.debug("empty view height: " + height + " pagerHeight: " + pagerHeight);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                viewHolder = new EmptyFooterViewItem(view);
                break;
            case ITEM_TYPE_CONTINUE_WATCHING_EPG:
                viewHolder = ContinueWatchingItemLive.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_CONTINUE_WATCHING:
                viewHolder = ContinueWatchingItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case LAYOUT_TYPE_EXCLUSIVE_HORIZONTA_LIST_BIG_ITEM:
                viewHolder = PosterListLargeItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_NESTED_CAROUSEL:
                viewHolder = NestedCarouselItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_RECENT_SEARCH:
                viewHolder = RecentSearchItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_PORTRAIT_BANNER_PLAYER:
                viewHolder = BannerHorizontalItem3D.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
               /* viewHolder = PortraitBannerPlayerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);*/
                break;
            case ITEM_TYPE_SQUARE_BANNER_PLAYER:
                viewHolder = SquareBannerPlayerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
                /*viewHolder = SquareViewPagerItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,callbackListener,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;*/
            case ITEM_SQUARE_LIST_BIG:
                viewHolder = SquareListBigItem.createView(mContext, parent, mListCarouselInfo, mRecyclerViewCarouselInfo, false,
                        "", null, null, "", null, null);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_3D_CAROUSEL:
                viewHolder = AnimationRecyclerViewItem.createView
                        (mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_BIG_HORIZONTAL_3D_CAROUSEL:
                viewHolder = AnimationViewPagerItemBigHorizontal.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_BIG_BANNER_HORIZONTAL_CAROUSEL:
                viewHolder = BigBannerHorizontalCarousel.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_BIG_BANNER_VERTICAL_CAROUSEL:
                viewHolder = BigBannerVerticalCarousel.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_WEEKLY_TRENDING_BIG_ITEM:
              /*  viewHolder = BigWeeklyTrendingItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);*/
                viewHolder = BigWeeklyTrendingItemNew.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mCarouselInfoData);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_WEEKLY_TRENDING_MEDIUM_ITEM:
                viewHolder = MediumHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_BIG_BANNER_VERTICAL_DOUBLE_TITLE_VERTICAL_CAROUSEL:
                viewHolder = BigBannerVerticalCarousel.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_HORIZONTAL_LIST_BIG_ITEM:
                viewHolder = BigHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor, isFromSearch,isFromApp,globalServiceId);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_ROUNDED_ARTIST_CAROUSEL:
                viewHolder = RoundedArtistHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup, mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_PARTNER:
                viewHolder = PartnerHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup, mPageTitle,isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_LIVE_CHANNEL_SMALL_ITEM:
             /*   viewHolder = SmallSquareHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo, isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);*/
                viewHolder = LiveSquareHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch );
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_LONG_PORTRAIT_MEDIUM :
                viewHolder = LongBigHorizontalItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor, isFromSearch);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_EXTRA_SMALL_COVERP:
                    viewHolder = ExtraSmallCoverItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch, globalServiceId );
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_AD_CUSTOM_BANNER_LAYOUT:
                viewHolder = AdCustomBannerLayout.createView(mContext,parent,mListCarouselInfo,
                        mCarouselInfoData,mMenuGroup);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_NATIVE_IMAGE:
            case ITEM_TYPE_NATIVE_VIDEO:
                viewHolder = AdNativeVideoLayout.createView(mContext,parent,
                        mCarouselInfoData,mListCarouselInfo,mPageTitle);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_NEXT_PROGRAM:
                viewHolder = NextProgramItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch,globalServiceId );
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_CATCHUP:
                viewHolder = CatchupItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,isFromSearch,globalServiceId );
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_TEXT_FLOW_LAYOUT:
                viewHolder = TextFlowLayoutItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,true,globalServiceId ,textList);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_SEARCH_LANGUAGE:
                viewHolder = SearchLanguageItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,true,globalServiceId );
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_WEEKLY_TRENDING_SEASON_ITEM:
                viewHolder = WeeklyTrendingSeasonItem.createView(mContext,parent,mListCarouselInfo,mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,mBgColor,true,globalServiceId, seasonId);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
        }
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(UiCompoment holder, int position) {
        holder.bindItemViewHolder(position);
    }

    @Override
    public int getItemCount() {
        if (mListCarouselInfo == null) {
            return 0;
        }
        return mListCarouselInfo.size();
    }

    @Override
    public int getItemViewType(int position) {
       // //Log.d(TAG, "getItemViewType- " + mListCarouselInfo.get(position).layoutType);
        if (mListCarouselInfo != null) {
            if (APIConstants.LAYOUT_TYPE_MEDIUM_AD.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_AD_CUSTOM_BANNER_LAYOUT;
            } else if (APIConstants.LAYOUT_TYPE_SMALL_AD.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_AD_CUSTOM_BANNER_LAYOUT;
            } else if (APIConstants.LAYOUT_TYPE_AD_BANNER_BTW_CAROUSELS_LARGE
                    .equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)||
                    APIConstants.LAYOUT_TYPE_AD_BANNER_BTW_CAROUSELS_MEDIUM
                            .equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)||
                    APIConstants.LAYOUT_TYPE_AD_BANNER_BTW_CAROUSELS_SMALL
                            .equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_AD_CUSTOM_BANNER_LAYOUT;
            }else if(APIConstants.LAYOUT_TYPE_NATIVE_VIDEO_AD
                    .equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)||
                    APIConstants.LAYOUT_TYPE_NATIVE_IMAGE_AD
                            .equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)){
                return ITEM_TYPE_NATIVE_VIDEO;
            }
            if (APIConstants.LAYOUT_TYPE_BANNER.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_BANNER;
            if (APIConstants.LAYOUT_TYPE_PORTRAIT_BANNER.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                if (DeviceUtils.isTablet(mContext) || isLowEndDevice()) {
                    return ITEM_TYPE_PORTRAIT_BANNER_PLAYER;
                } else {
                    if(isPreviewPlaybackEnabled() && isPreviewPlaybackEnabledForClientOS()){
                        return ITEM_TYPE_PORTRAIT_BANNER_PLAYER;
                    }else{

                        //commented as an another layout type for home page banner is coming on resume to the home page
                        //return ITEM_TYPE_PORTRAIT_BANNER;

                        //And giving required layout type after on resume to the home page
                        return ITEM_TYPE_PORTRAIT_BANNER_PLAYER;
                    }

                }
            }
            if (APIConstants.LAYOUT_TYPE_SQUARE_BANNER.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                if (DeviceUtils.isTablet(mContext) || isLowEndDevice()) {
                    return ITEM_TYPE_BANNER;
                } else {
                    if (isPreviewPlaybackEnabled() && isPreviewPlaybackEnabledForClientOS()) {
                        return ITEM_TYPE_SQUARE_BANNER_PLAYER;
                    } else {
                        return ITEM_TYPE_SQUARE_BANNER;
                    }
                }
            }
            if (APIConstants.LAYOUT_TYPE_PROMO_BANNER.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_PROMO_BANNER;
            if (APIConstants.LAYOUT_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM;

            else if (APIConstants.LAYOUT_TYPE_VMAX_IMAGE_ADS.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_VMAX_IMAGE_ADVERTISE;

            else if (APIConstants.LAYOUT_TYPE_VMAX_VIDEO_ADS.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_VMAX_VIDEO_ADVERTISE;

            else if (APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_CONTINUE_WATCHING;
            else if (APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING_EPG.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_CONTINUE_WATCHING_EPG;

            else if (APIConstants.LAYOUT_TYPE_NEWS_NESTED_CAORUSEL.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_NEWS_NESTED_CAROUSEL_ITEM;

            else if (APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_HORIZONTAL_LIST_SMALL_ITEM;
            else if (APIConstants.LAYOUT_TYPE_WEEKLY_TRENDING_SMALL_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_HORIZONTAL_LIST_SMALL_ITEM;
            else if (APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)
                    || APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_GENERIC_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_HORIZONTAL_LIST_MED_ITEM;

            else if (APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_HORIZONTAL_LIST_BIG_ITEM;
            else if (APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_LIVE_PROGRAM_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_LIVE_PROGRAM_ITEM;
          /*  else if (APIConstants.LAYOUT_TYPE_EMPTY_FOOTER.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_EMPTY_FOOTER;*/
            else if (APIConstants.LAYOUT_TYPE_EXCLUSIVE_HORIZONTA_LIST_BIG_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return LAYOUT_TYPE_EXCLUSIVE_HORIZONTA_LIST_BIG_ITEM;
            if (APIConstants.LAYOUT_TYPE_SINGLE_BANNER_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType))
                return ITEM_TYPE_SINGLE_BANNER_ITEM;
            else if (APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_LARGE_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_HORIZONTAL_LIST_LARGE_ITEM;
            }
            else if (APIConstants.LAYOUT_TYPE_SQUARE_LIST_SMALL_TIEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_SQUARE_LIST_SMALL_ITEM;
            }
            else if (APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_NESTED_CAROUSEL;
            }else if(APIConstants.LAYOUT_TYPE_RESENT_SEARCH.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)){
                return ITEM_RECENT_SEARCH;
            }else if(APIConstants.LAYOUT_TYPE_PREVIEW_CAROUSAL.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)){
                return ITEM_SQUARE_LIST_BIG;
            }else if(APIConstants.LAYOUT_TYPE_AUTOPLAY_SINGLE_BANNER.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_AUTOPLAY_SINGLE_BANNER;
            }else if (APIConstants.LAYOUT_TYPE_WEEKLY_TRENDING_MEDIUM_LAYOUT.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_WEEKLY_TRENDING_MEDIUM_ITEM;
            }else if (APIConstants.LAYOUT_TYPE_TEXT_FLOW_LAYOUT.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_TEXT_FLOW_LAYOUT;
            }else if (APIConstants.LAYOUT_TYPE_SEARCH_LANGUAGES.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_SEARCH_LANGUAGE;
            }
            else if (APIConstants.LAYOUT_TYPE_WEEKLY_TRENDING_BIG_LAYOUT.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_WEEKLY_TRENDING_BIG_ITEM;
            }else if (APIConstants.LAYOUT_TYPE_BIG_BANNER_VERTICAL_LAYOUT_DOUBLE_TITLE.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_BIG_BANNER_VERTICAL_DOUBLE_TITLE_VERTICAL_CAROUSEL;
            }else if (APIConstants.LAYOUT_TYPE_3D_CAROUSEL.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_3D_CAROUSEL;
            }else if (APIConstants.LAYOUT_TYPE_BIG_HORIZONTAL_3D_CAROUSEL.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_BIG_HORIZONTAL_3D_CAROUSEL;
            }else if (APIConstants.LAYOUT_TYPE_ROUNDED_ARTIST_CAROUSEL.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_ROUNDED_ARTIST_CAROUSEL;
            }
            else if (APIConstants.LAYOUT_TYPE_PARTNERS.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_PARTNER;
            }
            else if (APIConstants.LAYOUT_TYPE_LIVE_SMALL_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_LIVE_CHANNEL_SMALL_ITEM;
            }
            else if (APIConstants.LAYOUT_TYPE_LONG_PORTRAIT_MEDIUM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_LONG_PORTRAIT_MEDIUM;
            }
            else if (APIConstants.LAYOUT_TYPE_EXTRA_SMALL_COVER_P.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_EXTRA_SMALL_COVERP;
            }
            else if (APIConstants.LAYOUT_TYPE_NEXT_PROGRAM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_NEXT_PROGRAM;
            } else if (APIConstants.LAYOUT_TYPE_CATCHUP.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_CATCHUP;
            } else if (APIConstants.LAYOUT_TYPE_WEEKLY_TRENDING_SEASON_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position).layoutType)) {
                return ITEM_TYPE_WEEKLY_TRENDING_SEASON_ITEM;
            }

        }
        return ITEM_TYPE_HORIZONTAL_LIST_SMALL_ITEM;
    }

    private boolean isPreviewPlaybackEnabled() {
        return mPreviewProperties != null
                && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_ENABLE;
    }

    private boolean isPreviewPlaybackEnabledForClientOS() {
        try {
            if (mPreviewProperties != null
                    && mPreviewProperties.previewVideoConfig != null
                    && !TextUtils.isEmpty(mPreviewProperties.previewVideoConfig.PREVIEW_DISABLE_ANDROID_OS)) {
                int sdk_version = Build.VERSION.SDK_INT;
                //Log.e("BlackListing","sdk_version"+sdk_version);
                if (sdk_version > 18) {
                    String sdk_versionString = String.valueOf(sdk_version);
                    List<String> list = Arrays.asList(mPreviewProperties.previewVideoConfig.PREVIEW_DISABLE_ANDROID_OS.split(","));
                    if (list != null && list.size() > 0) {
                        //Log.e("BlackListing", "sdk_version" + list.toString());
                        if (list.contains(sdk_versionString)) {
                            //Log.e("BlackListing","Found");
                            return false;
                        }
                    }
                    return true;
                }


            }
            //Log.e("BlackListing","Not Found");
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean isLowEndDevice() {
        int currentDeviceHeight = ApplicationController.getApplicationConfig().screenHeight;
        int minHeight;
        try {
            minHeight = 0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            minHeight = 0;
        }
        return currentDeviceHeight < minHeight;
    }

    public void setMenuGroupName(String mMenuGroup, String mPageTitle) {
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
    }


    public void onFullScreen(boolean b) {

        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        if (b) {
            for (int i = 0; i < mListCarouselInfo.size(); i++) {
                CarouselInfoData carouselInfoData = mListCarouselInfo.get(i);
                if (APIConstants.LAYOUT_TYPE_BANNER.equalsIgnoreCase(carouselInfoData.layoutType)) {
                    if (mRecyclerViewCarouselInfo.findViewHolderForAdapterPosition(i) instanceof ViewPagerViewHolder) {
                        ViewPagerViewHolder viewPagerViewHolder = (ViewPagerViewHolder) mRecyclerViewCarouselInfo.findViewHolderForAdapterPosition(i);
                        viewPagerViewHolder.mViewPager.stopAutoScroll();
                    }
                }
            }
        } else {
            for (int i = 0; i < mListCarouselInfo.size(); i++) {
                CarouselInfoData carouselInfoData = mListCarouselInfo.get(i);
                if (APIConstants.LAYOUT_TYPE_BANNER.equalsIgnoreCase(carouselInfoData.layoutType)) {
                    if (mRecyclerViewCarouselInfo.findViewHolderForAdapterPosition(i) instanceof ViewPagerViewHolder) {
                        ViewPagerViewHolder viewPagerViewHolder = (ViewPagerViewHolder) mRecyclerViewCarouselInfo.findViewHolderForAdapterPosition(i);
                        viewPagerViewHolder.mViewPager.startAutoScroll();
                    }
                }
            }
        }
    }


    public void setSearchMoviedata(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        this.searchMovieData = searchMovieDataOnOTTApp;
    }

    public void setCallBackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }


    public static class GenericViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListener clickListener;

        final RecyclerView mRecyclerViewCarousel;
        final TextView mTextViewGenreMovieTitle,mTextViewGenreMovieTitleOtherLang;
        final TextView mTextViewViewAll,mTextViewViewAllOtherLang;
        final ViewGroup mLayoutViewAll;
        final ImageView mChannelImageView;
        final RelativeLayout mLayoutCarouselTitle;
        TextView mTextViewErrorRetry;
        private List<CarouselInfoData> carouselInfoDataList;
        GenericViewHolder(View view) {
            super(view);
            itemView.setOnClickListener(this);
            mTextViewGenreMovieTitle = (TextView) view.findViewById(R.id.textview_genre_title);
            mTextViewViewAll = (TextView) view.findViewById(R.id.textview_view_all);
            mTextViewGenreMovieTitleOtherLang = (TextView)view.findViewById(R.id.textview_other_lang_title) ;
            mTextViewViewAllOtherLang = (TextView)view.findViewById(R.id.textview_view_all_other_lang) ;
            mRecyclerViewCarousel = (RecyclerView) view.findViewById(R.id.recycler_view_movie);
            SnapHelper snapHelper = new StartSnapHelper();
            snapHelper.attachToRecyclerView(mRecyclerViewCarousel);
            mLayoutViewAll = (ViewGroup) view.findViewById(R.id.layout_view_all);
            mChannelImageView = (ImageView) view.findViewById(R.id.toolbar_tv_channel_Img);
            mLayoutCarouselTitle = (RelativeLayout) view.findViewById(R.id.layout_carousel_title);
            mTextViewErrorRetry = (TextView) view.findViewById(R.id.textview_error_retry);

        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (clickListener == null) return;
            this.clickListener.onClick(v, getAdapterPosition());
        }
    }

    /**
     * ViewHolder of the sliding bar view pager
     */
    class ViewPagerViewHolder extends GenericViewHolder {

        private final AutoScrollViewPager mViewPager;
        private final TextView mDescriptionTxt;
        private final LinearLayout mOfferDecriptionLayout;
        private final RelativeLayout mViewPagerContainer;
        private final ImageView mPreviewLayout;
        private ImageView mRightArrow;
        private ImageView mLeftArrow;
        private View leftGradient, rightGradient;
        private RelativeLayout gradientContainer;
        //        private final LinearLayout mIndicatorContainer;
        final CircleIndicator mIndicator;

        ViewPagerViewHolder(View itemView) {
            super(itemView);
//            mIndicator = (CirclePageIndicator)itemView.findViewById(R.id.indicator);
            mViewPager = (AutoScrollViewPager) itemView.findViewById(R.id.pager_ottapps);
            mDescriptionTxt = (TextView) itemView.findViewById(R.id.slider_title);
            mOfferDecriptionLayout = (LinearLayout) itemView.findViewById(R.id.offer_description_layout);
            mPreviewLayout = (ImageView) itemView.findViewById(R.id.previewLayout);
            mViewPagerContainer = (RelativeLayout) itemView.findViewById(R.id.pager_ottapps_layout);
            mIndicator = (CircleIndicator) itemView.findViewById(R.id.view_pager_indicator);
            mRightArrow = (ImageView) itemView.findViewById(R.id.viewpager_right_arrow);
            mLeftArrow = (ImageView) itemView.findViewById(R.id.viewpager_left_arrow);
            leftGradient = itemView.findViewById(R.id.left_gradient_view);
            rightGradient = itemView.findViewById(R.id.right_gradient_view);
            gradientContainer = (RelativeLayout) itemView.findViewById(R.id.grdient_container);

        }
    }


    class EmptyFooterViewItem extends GenericListViewCompoment {

        public EmptyFooterViewItem(View view) {
            super(view);
        }

        @Override
        public void bindItemViewHolder(int position) {

        }
    }

    // Overriding this method to get access to recyclerView on which current MovieAdapter has been attached.
    // In the future we will use that reference for scrolling to newly added relatedMovies item
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViewCarouselInfo = recyclerView;
        recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_TYPE_HORIZONTAL_LIST_BIG_ITEM, 50);
        recyclerView.setItemViewCacheSize(300);
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }


    public void setCarouselInfoData(List<CarouselInfoData> carouselList) {
        if (carouselList == null) {
            return;
        }
        mListCarouselInfo = carouselList;
      /*  if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxBannerAdId())
                && PrefUtils.getInstance().getPrefEnableVmaxFooterBannerAd()) {*/
            SDKLogger.debug("Adding empty view");
           // addEmptyViewAtFooter();
       // }
        for (CarouselInfoData carouselInfoData :
                mListCarouselInfo) {
            SDKLogger.debug("position- " + mListCarouselInfo.indexOf(carouselInfoData) + " layoutType- " + carouselInfoData.layoutType + " carousleInfoData- " + carouselInfoData);
        }
        Log.e("Notify","Notify Data set changed");
        notifyDataSetChanged();
    }

    private void showDetailsFragment(CardData carouselData, CarouselInfoData carouselInfoData, int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
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
            if (carouselData.generalInfo != null
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !carouselData.isTVSeries()
                    && !carouselData.isVODChannel()
                    && !carouselData.isVODYoutubeChannel()
                    && !carouselData.isVODCategory()
                    && !carouselData.isTVSeason()) {
                CacheManager.setCardDataList(carouselInfoData.listCarouselData);
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


    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
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
        if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }


    private void removeItemFromParent(CarouselInfoData carouselInfoData, int mParentPosition) {
        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + " no carousel title");
            return;
        }
        safelyNotifyItemRemoved(carouselInfoData);
    }

    public void safelyNotifyItemRemoved(final CarouselInfoData carouselInfoData) {
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LoggerD.debugDownload("DeletionCarouselInfo: removal " + carouselInfoData.title);
                    if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
                        int mParentPosition = mListCarouselInfo.indexOf(carouselInfoData);
                        mListCarouselInfo.remove(carouselInfoData);
                        if (!mRecyclerViewCarouselInfo.isComputingLayout()) {
                         //   mRecyclerViewCarouselInfo.getRecycledViewPool().clear();
                            Log.e("Notify","Notify item removed");
                            notifyItemRemoved(mParentPosition);
                            Log.e("Notify","Notify item range changed");
                            notifyItemRangeChanged(mParentPosition, mListCarouselInfo.size());
                        } else {
                            safelyNotifyItemRemoved(carouselInfoData);
                        }
                    }

                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public void safelyNotifyItemAdded(final CarouselInfoData carouselInfoData, int position) {
       try {
           mHandler.post(new Runnable() {
               @Override
               public void run() {
                   LoggerD.debugDownload("DeletionCarouselInfo: removal " + carouselInfoData.title);
                   if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
                       int mParentPosition = mListCarouselInfo.indexOf(carouselInfoData);
                       // mListCarouselInfo.remove(carouselInfoData);
                       if(mParentPosition == -1)
                           mListCarouselInfo.add(position, carouselInfoData);
                       else
                           return;
                       if (!mRecyclerViewCarouselInfo.isComputingLayout()) {
                           //   mRecyclerViewCarouselInfo.getRecycledViewPool().clear();
                           Log.e("Notify","Notify item removed");
                           Log.e("Notify","Notify item range changed");
                           notifyItemRangeChanged(position, mListCarouselInfo.size());
                           //   notifyItemChanged(position, mListCarouselInfo.size());
                       } else {
                           //safelyNotifyItemAdded(carouselInfoData, position);
                       }
                   }

               }
           });
       } catch (IllegalStateException e) {
           e.printStackTrace();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    private void addEmptyViewAtFooter() {
        CarouselInfoData dummyFooterView = new CarouselInfoData();
        dummyFooterView.layoutType = APIConstants.LAYOUT_TYPE_EMPTY_FOOTER;
        if (mListCarouselInfo == null)
            mListCarouselInfo = new ArrayList<>();
        mListCarouselInfo.add(dummyFooterView);
    }



}
