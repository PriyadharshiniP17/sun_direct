package com.myplex.myplex.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.EpisodeDisplayData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.RequestState;
import com.myplex.model.ShowDisplayTabs;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterSeasonItemCarousel;
import com.myplex.myplex.ui.adapter.EpisodeItemAdapter;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.OnItemRemovedListener;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.StringManager;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class WeeklyTrendingSeasonItem extends GenericListViewCompoment {


    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = BigHorizontalItem.class.getSimpleName();
    private final RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselData;
    private View view;
    private CarouselInfoData carouselInfoData;
    private final RecyclerView.ItemDecoration mHorizontalMoviesDivieder;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    AdapterSeasonItemCarousel adapterSmallHorizontalCarousel = null;
    EpisodeItemAdapter episodeItemAdapter = null;
    private final CacheManager mCacheManager = new CacheManager();
    private boolean mIsLoadingMorePages = false;
    private boolean mIsLoadingMoreAvailable = true;
   private int  mStartIndex=1;
    String mBgColor;
    private boolean isFromSearch;
    private String globalServiceId;
    private int selectedTabIndex;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    List<EpisodeDisplayData> seasonsData = new ArrayList<>();
    List<CardData> cardData = new ArrayList<>();
    List<CardData> episodesData = new ArrayList<>();
    List<EpisodeDisplayData> episodeDisplayData = new ArrayList<>();
    private Typeface mBoldTypeFace;

    private String seasonId;
    public WeeklyTrendingSeasonItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInfo
            , String mMenuGroup, String mPageTitle, String mBgColor, boolean isFromSearch, String globalServiceId, String seasonId) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
      /*  int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) mContext
                .getResources().getDimension(R.dimen.thumbnail_gap), mContext.getResources().getDisplayMetrics());*/
        int margin = (int) mContext.getResources().getDimension(R.dimen.thumbnail_gap);
        mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        this.mBgColor=mBgColor;
        this.isFromSearch = isFromSearch;
        this.globalServiceId = globalServiceId;
        this.seasonId = seasonId;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");

    }

    public static WeeklyTrendingSeasonItem createView(Context context, ViewGroup parent,
                                                      List<CarouselInfoData> carouselInfoData, RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup,
                                                      String mPageTitle, String mBgColor, boolean isFromSearch, String globalServiceId, String seasonId) {
        View view = LayoutInflater.from(context).inflate(R.layout.season_item, parent, false);
        return new WeeklyTrendingSeasonItem(context, view, carouselInfoData, mRecyclerViewCarouselInfo, mMenuGroup,  mPageTitle,mBgColor, isFromSearch,globalServiceId, seasonId);
    }

    @Override
    public void bindItemViewHolder(int position) {

        this.position = position;
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
       // CarouselInfoData carouselInfoData = null;
        if(position < mListCarouselInfo.size())
            carouselInfoData = mListCarouselInfo.get(position);
        if (carouselInfoData == null) {
            return;
        }
        seasonTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabIndex = tab.getPosition();
                // startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
                if(seasonsData != null && seasonsData.get(selectedTabIndex) != null) {
                    mStartIndex=1;
                    mIsLoadingMorePages=false;
                    mIsLoadingMoreAvailable=true;
                    fetchTVSeries(seasonsData.get(selectedTabIndex), cardData.get(selectedTabIndex));
                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        episodeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
               // selectedTabIndex = tab.getPosition();
                fetchTVEpisodes(episodeDisplayData.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        holder.mLayoutViewAll.setTag(carouselInfoData);
        holder.mLayoutViewAll.setOnClickListener(mViewAllClickListener);
        holder.mImageViewAll.setVisibility(View.GONE);
        if(holder.mTextViewViewAll!=null){
            holder.mTextViewViewAll.setTag(carouselInfoData);
            holder.mTextViewViewAll.setOnClickListener(mViewAllClickListener);
        }
        holder.mLayoutViewAll.setVisibility(View.GONE);
        if (carouselInfoData.enableShowAll) {
            holder.mLayoutViewAll.setVisibility(View.VISIBLE);
            holder.mImageViewAll.setVisibility(View.VISIBLE);
            if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN){
                holder.mTextViewViewAllOtherLang.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.MORE))) {
                    holder.mTextViewViewAllOtherLang.setText(StringManager.getInstance().getString(APIConstants.MORE));
                }else{
                    holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
                }
            }else{
                holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(carouselInfoData.showAll)) {

                    holder.mTextViewViewAll.setVisibility(View.VISIBLE);

                holder.mTextViewViewAll.setText(carouselInfoData.showAll);
            }
        } else
            holder.mLayoutViewAll.setVisibility(View.GONE);
      //  holder.mLayoutViewAll.setVisibility(View.VISIBLE);
        holder.mImageViewAll.setVisibility(View.VISIBLE);
        if (carouselInfoData!=null&&carouselInfoData.showTitle) {
            if (carouselInfoData.title != null) {
                mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            } else {
                mTextViewGenreMovieTitle.setVisibility(View.GONE);
            }
        }
        if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN && carouselInfoData.altTitle != null && !carouselInfoData.altTitle.isEmpty()){
            holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.VISIBLE);
            holder.mTextViewGenreMovieTitleOtherLang.setText(carouselInfoData.altTitle);
        }else{
            holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.GONE);
            holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
        }

        if (mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
            holder.mTextViewGenreMovieTitleOtherLang.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            holder.mTextViewGenreMovieTitle.setTextColor(mContext.getResources().getColor(R.color.carousel_heading_text_color));
        }

        //holder.mLayoutCarouselTitle.setBackgroundColor(UiUtil.getColor(mContext, R.color.app_theme_color));
        /*if (!TextUtils.isEmpty(carouselInfoData.bgColor)) {
            try {
                holder.mLayoutCarouselTitle.setBackgroundColor(Color.parseColor(carouselInfoData.bgColor));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        if(isFromSearch)
            mLayoutViewAll.setVisibility(View.GONE);
        else
            mLayoutViewAll.setVisibility(View.VISIBLE);
        String imageLink = carouselInfoData.getLogoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        holder.mChannelImageView.setVisibility(View.GONE);
        if (imageLink == null || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.mChannelImageView.setVisibility(View.GONE);
        } else if (imageLink != null) {
            holder.mChannelImageView.setVisibility(View.VISIBLE);
            if (imageLink.contains(APIConstants.PARAM_SCALE_WRAP)) {
                holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                if (DeviceUtils.isTablet(mContext)) {
                    holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    holder.mChannelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
                }
            }
            PicassoUtil.with(mContext).load(imageLink,holder.mChannelImageView);
        }
        adapterSmallHorizontalCarousel = null;
        if (carouselInfoData.listCarouselData == null) {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " listCarouselData is null");
            carouselInfoData.listCarouselData = Util.getDummyCardData();
        }
        adapterSmallHorizontalCarousel = new AdapterSeasonItemCarousel(mContext, carouselInfoData.listCarouselData, holder.mRecyclerViewCarousel);
        episodeItemAdapter=new EpisodeItemAdapter(recyclerViewEpisodes ,mContext,null,mBgColor);
        LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " requestState- " + carouselInfoData.requestState);
        holder.mRecyclerViewCarousel.setItemAnimator(null);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.setItemPrefetchEnabled(false);
        holder.mRecyclerViewCarousel.setLayoutManager(linearLayoutManager);
        recyclerViewEpisodes.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        if (!holder.mRecyclerViewCarousel.isComputingLayout()) {
            holder.mRecyclerViewCarousel.removeItemDecoration(mHorizontalMoviesDivieder);
            holder.mRecyclerViewCarousel.addItemDecoration(mHorizontalMoviesDivieder);

        }
        if (!holder.recyclerViewEpisodes.isComputingLayout()) {
            holder.recyclerViewEpisodes.removeItemDecoration(mHorizontalMoviesDivieder);
            holder.recyclerViewEpisodes.addItemDecoration(mHorizontalMoviesDivieder);

        }
        holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
        holder.recyclerViewEpisodes.setFocusableInTouchMode(false);
        adapterSmallHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
        episodeItemAdapter.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
        adapterSmallHorizontalCarousel.setParentPosition(position);
        episodeItemAdapter.setParentPosition(position);
        adapterSmallHorizontalCarousel.setParentViewHolder(holder);
        adapterSmallHorizontalCarousel.setBgColor(mBgColor);
        adapterSmallHorizontalCarousel.setCarouselInfoData(carouselInfoData.name, carouselInfoData.pageSize);
        adapterSmallHorizontalCarousel.isContinueWatchingSection(carouselInfoData.layoutType != null
                && APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType));
        adapterSmallHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
        holder.mRecyclerViewCarousel.setAdapter(adapterSmallHorizontalCarousel);
        holder.recyclerViewEpisodes.setAdapter(episodeItemAdapter);
        if (TextUtils.isEmpty(carouselInfoData.name)) {
            return;
        }
        switch (carouselInfoData.requestState) {
            case NOT_LOADED:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
               // startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
                fetchTVSeasons(seasonId);
                break;
            case IN_PROGRESS:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                if (carouselInfoData != null
                        && (carouselInfoData.listCarouselData == null
                        || carouselInfoData.listCarouselData.isEmpty())) {
                     removeItemFromParent(carouselInfoData);
                    return;
                }
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                if (carouselInfoData != null
                        && APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType)) {
                    if (carouselInfoData.listCarouselData != null)
                        carouselInfoData.listCarouselData.clear();
                    removeItemFromParent(carouselInfoData);
                    return;
                }
                holder.mTextViewErrorRetry.setVisibility(View.VISIBLE);
                holder.mRecyclerViewCarousel.setVisibility(View.INVISIBLE);
                holder.mTextViewErrorRetry.setTag(position);
                holder.mTextViewErrorRetry.setOnClickListener(mRetryListener);
                break;
            default:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
        }
        //Implemented the pagination(load more) for the horizontal scrolling of web series
        holder.mRecyclerViewCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState==RecyclerView.SCROLL_STATE_DRAGGING ){
                    try {

                        if (!mIsLoadingMorePages) {
                            if (!mIsLoadingMoreAvailable) {
                                return;
                            }
                            mIsLoadingMorePages = true;

                            mStartIndex++;
                            fetchTVSeries(seasonsData.get(selectedTabIndex), cardData.get(selectedTabIndex));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        if(spanString.length()>0) {
            spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spanString;
    }


    private void addCarouselData(final List<CardData> carouselList, final CarouselInfoData carouselInfoData) {
        if (carouselList == null && carouselList.size() == 0) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
            removeItemFromParent(carouselInfoData);
        } else {
            //  LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
            if (carouselList != null && carouselInfoData != null) {
                mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                recyclerViewEpisodes.setVisibility(View.GONE);
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                carouselInfoData.requestState = RequestState.SUCCESS;
                carouselInfoData.listCarouselData = carouselList;
                adapterSmallHorizontalCarousel.setData(carouselInfoData.listCarouselData);
                episodesData = carouselList;
            }
        }

    }

    private void addEpisodeData(final List<CardData> carouselList, final CarouselInfoData carouselInfoData) {
        if (carouselList == null && carouselList.size() == 0) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
            removeItemFromParent(carouselInfoData);
        } else {
            //  LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
            if (carouselList != null && carouselInfoData != null) {
                recyclerViewEpisodes.setVisibility(View.VISIBLE);
                mRecyclerViewCarousel.setVisibility(View.GONE);
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                carouselInfoData.requestState = RequestState.SUCCESS;
                carouselInfoData.listCarouselData = carouselList;
                episodeItemAdapter.setData(carouselInfoData.listCarouselData);
                if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null) {
                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.queueListCardData(carouselList);
                }
                Collections.reverse(carouselList);
                episodesData = carouselList;
            }
        }

    }

    private OnItemRemovedListener mOnItemRemovedListener = new OnItemRemovedListener() {
        @Override
        public void onItemRemoved(int mParentPosition) {
            if (mListCarouselInfo == null) {
                LoggerD.debugLog("removeItem: invalid operation of removal carousel info is empty");
                return;
            }
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(mParentPosition);
            removeItemFromParent(carouselInfoData);
        }
    };

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here


            if (carouselData == null || carouselData._id == null) return;
            ApplicationController.IS_FROM_CONTINUE_WATCHING = false;

            if(carouselData.generalInfo!=null && carouselData.generalInfo._id!=null) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("contentTitle", carouselData.generalInfo._id);
                editor.commit();
                if(adapterSmallHorizontalCarousel!=null)
                    adapterSmallHorizontalCarousel.notifyDataSetChanged();
            }


//            CarouselInfoData carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;
            CarouselInfoData carouselInfoData = null;
            try {
                carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;
            } catch (Exception e) {
                e.printStackTrace();
            }

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
            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;
            ((MainActivity)mContext).HideSearchView();
            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, carouselInfoData, null,parentPosition);
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
          /*  if (carouselData != null
                    && carouselData.generalInfo != null
                    && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type) || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }*/
            APIConstants.IS_FROM_RECOMMENDATIONS=true;
            if (carouselInfoData == null) {
                showDetailsFragment(carouselData, position,"",parentPosition);
            } else {
                showDetailsFragment(carouselData, carouselInfoData,parentPosition);
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
        args.putString(CardDetails.PARAM_SEASON_GLOBAL_SERVICE_ID, seasonId);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
        if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null) {
            if(episodesData != null)
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.queueListCardData(episodesData);
        }
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition) {

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
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
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
        args.putString(CardDetails.PARAM_SEASON_GLOBAL_SERVICE_ID, seasonId);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {

            if (view.getTag() instanceof CarouselInfoData) {
                CleverTap.eventPageViewed(CleverTap.PAGE_VIEW_ALL);
                final CarouselInfoData carouselData = (CarouselInfoData) view.getTag();
                if (carouselData == null || carouselData.name == null) {
                    return;
                }
                int carouselPosition=-1;
                if (carouselData != null && mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
                    for(int i=0;i<mListCarouselInfo.size();i++){
                        if(mListCarouselInfo.get(i)!=null&&!TextUtils.isEmpty(mListCarouselInfo.get(i).name)&&mListCarouselInfo.get(i).name.equalsIgnoreCase(carouselData.name))
                            carouselPosition = i;
                    }
                }
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 500);
                view.setEnabled(false);
                if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_TVSHOWS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_TV_SHOWS, carouselData.title, true);
                } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_VIDEOS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_VIDEOS, carouselData.title, true);
                } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_MUSIC_VIDEOS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_MUSIC_VIDEOS, carouselData.title, true);
                } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_KIDS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(HomePagerAdapter.getPageKids(), carouselData.title, true);
                } else if (carouselData != null && mPageTitle != null) {
                    Analytics.browseViewAllEvent("browsed " + carouselData.title.toLowerCase(), carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(mPageTitle.toLowerCase(), carouselData.title, true);
                }
                if (carouselData != null)
                    LoggerD.debugLog("carouselData.showAllLayoutType- " + carouselData.showAllLayoutType);
                if (APIConstants.LAYOUT_TYPE_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                    showRelatedVODListFragment(carouselData,carouselPosition);
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)
                        || APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                    showCarouselViewAllFragment(carouselData,carouselPosition);
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)
                        && APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL.equalsIgnoreCase(carouselData.layoutType)) {
                    Bundle args = new Bundle();
                    args.putSerializable(SmallSquareItemsFragment.PARAM_CAROUSEL_INFO_DATA, carouselData);
                    args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);
                    ((MainActivity) mContext).pushFragment(SmallSquareItemsFragment.newInstance(args));
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                    showVODListFragment(carouselData,carouselPosition);
                    return;
                } else if (carouselData != null && carouselData.showAllLayoutType != null && carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
                    try {
                        ((MainActivity) mContext).redirectToPage(carouselData.showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                showVODListFragment(carouselData,carouselPosition);
            }
        }
    };

    private void showCarouselViewAllFragment(CarouselInfoData movieData, int carouselPosition) {
        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(movieData);

        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);

        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mMenuGroup);
        if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, APIConstants.TYPE_MOVIE);
        }
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    private void showRelatedVODListFragment(CarouselInfoData parentCarouselInfoData, int carouselPosition) {
        //TODO show RelatedVodListFragment from main activity context

      Bundle args = new Bundle();
        //args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        if (carouselPosition >= 0) {
            if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
                CarouselInfoData  carouselInfoData= mListCarouselInfo.get(carouselPosition);
                CacheManager.setCarouselInfoData(carouselInfoData);

            }
        }
        args.putBoolean(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL, true);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));


    }

    private void showVODListFragment(CarouselInfoData carouselInfoData, int carouselPositoin) {
        //TODO show VODListFragment from MainActivity with bundle

        MenuDataModel.setSelectedMoviesCarouselList(carouselInfoData.name + "_" + 1, carouselInfoData.listCarouselData);
        Bundle args = new Bundle();
        CacheManager.setCarouselInfoData(carouselInfoData);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPositoin);
        args.putBoolean(ApplicationController.PARTNER_LOGO_BOTTOM_VISIBILTY, !APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM.equalsIgnoreCase(carouselInfoData.layoutType));
        if (!APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselInfoData.showAllLayoutType)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_CAROUSEL_GRID, true);
        }

        if (!TextUtils.isEmpty(mMenuGroup)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mMenuGroup);
        }
        ((MainActivity) mContext).pushFragment(FragmentVODList.newInstance(args));
    }

    private View.OnClickListener mRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
            carouselInfoData.requestState = RequestState.IN_PROGRESS;
            //startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
        }
    };

    private void removeItemFromParent(CarouselInfoData carouselInfoData) {
        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        notifyItemRemoved(carouselInfoData);
    }
    public void setSearchMoviedata(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        this.searchMovieData = searchMovieDataOnOTTApp;
    }


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

    public void fetchTVSeasons(String contentId){
        mCacheManager.getRelatedVODListTypeExclusion(contentId, 1, false, APIConstants.TYPE_TVSEASON,
                15,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                           // showNoDataMessage();
                            return;
                        }
                      //  mListSeasons = dataList;
                      //  updateSeasons(dataList);
                        showDisplayTabs(dataList);
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            // showNoDataMessage();
                            return;
                        }
                        //  mListSeasons = dataList;
                        //  updateSeasons(dataList);
                       showDisplayTabs(dataList);
                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                       // showNoDataMessage();
                    }
                });
    }

    public void showDisplayTabs(List<CardData> dataList){
        seasonsData.clear();
        cardData.clear();
        seasonsData = getSeasons(dataList);
        cardData = dataList;
        if(seasonsData.size() > 0) {
            seasonTabLayout.removeAllTabs();
            seasonTabLayout.setVisibility(View.VISIBLE);
            for (int i = 0; i < seasonsData.size(); i++) {
                seasonTabLayout.addTab(seasonTabLayout.newTab().setText(robotoStringFont(seasonsData.get(i).episodeTabName)));
                View root = seasonTabLayout.getChildAt(0);
                //TODO : enable below code for tab dividers and change color code accordingly
                if (root instanceof LinearLayout) {
                    ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(mContext.getResources().getColor(R.color.dots_color_inactive));
                    drawable.setSize(3, 1);
                    ((LinearLayout) root).setDividerPadding(20);
                    ((LinearLayout) root).setDividerDrawable(drawable);
                }
            }
        }
        EpisodeDisplayData currentSeasonTab = seasonsData.get(0);
        CardData currentSeasonCard = dataList.get(0);
        for(int i = 0; i< seasonsData.size(); i++){
            EpisodeDisplayData season = seasonsData.get(i);
            if(season.seasonId.equalsIgnoreCase(globalServiceId)){
                currentSeasonTab = season;
                seasonTabLayout.getTabAt(i).select();
                break;
            }
        }
        for(CardData seasonCard : dataList){
            if(seasonCard._id.equals(globalServiceId)){
                currentSeasonCard = seasonCard;
                break;
            }
        }
        fetchTVSeries(currentSeasonTab, currentSeasonCard);
    }

    public void showDisplayEpisodes(List<EpisodeDisplayData> episodeDisplayDataList, int position){
        if(episodeDisplayDataList.size() > 0) {
            episodeTabLayout.setVisibility(View.VISIBLE);
            episodeTabLayout.removeAllTabs();
            for (int i = 0; i < episodeDisplayDataList.size(); i++) {
                episodeTabLayout.addTab(episodeTabLayout.newTab().setText(robotoStringFont(episodeDisplayDataList.get(i).episodeTabName)));
                View root = episodeTabLayout.getChildAt(0);
                //TODO : enable below code for tab dividers and change color code accordingly
                if (root instanceof LinearLayout) {
                    ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(mContext.getResources().getColor(R.color.dots_color_inactive));
                    drawable.setSize(3, 1);
                    ((LinearLayout) root).setDividerPadding(20);
                    ((LinearLayout) root).setDividerDrawable(drawable);
                }
            }
            fetchTVEpisodes(episodeDisplayDataList.get(position));
        }
    }

    public void fetchTVEpisodes(EpisodeDisplayData episodeDisplayData1){
        mCacheManager.getRelatedVODList(episodeDisplayData1.seasonId, episodeDisplayData1.startIndex, false, episodeDisplayData1.count,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            // showNoDataMessage();
                            holder.mTextViewErrorRetry.setVisibility(View.VISIBLE);
                            recyclerViewEpisodes.setVisibility(View.GONE);
                            mRecyclerViewCarousel.setVisibility(View.GONE);
                            holder.mTextViewErrorRetry.setText("No Episodes");
                            return;
                        }
                        addEpisodeData(dataList, carouselInfoData);

                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            // showNoDataMessage();
                            holder.mTextViewErrorRetry.setVisibility(View.VISIBLE);
                            recyclerViewEpisodes.setVisibility(View.GONE);
                            mRecyclerViewCarousel.setVisibility(View.GONE);
                            holder.mTextViewErrorRetry.setText("No Episodes");
                            return;
                        }
                        addEpisodeData(dataList, carouselInfoData);

                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        // showNoDataMessage();
                    }
                });
    }

    public void fetchTVSeries(EpisodeDisplayData episodeDisplayData1, CardData cardData){
        this.episodeDisplayData.clear();
        this.episodeDisplayData = getEpisodesData(cardData);
//        final int pageSize = (carouselInfoData == null) ? getArguments().getInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT) : (carouselInfoData.pageSize > 0) ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
        if(episodeDisplayData.size() > 0 ){
            showDisplayEpisodes(episodeDisplayData, 0);
        } else {
            mCacheManager.getRelatedVODList(episodeDisplayData1.seasonId, mStartIndex, false, episodeDisplayData1.count,
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(List<CardData> dataList) {
                            if (dataList == null || dataList.isEmpty()) {
                                // showNoDataMessage();
                                recyclerViewEpisodes.setVisibility(View.GONE);
                                mRecyclerViewCarousel.setVisibility(View.GONE);
                                return;
                            }
                            if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null) {
                                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.queueListCardData(dataList);
                            }
                            if (carouselInfoData!=null && episodeDisplayData1!=null  && (dataList != null && dataList.size() <  episodeDisplayData1.count)) {
                                mIsLoadingMoreAvailable = false;
                            }
                            if (mIsLoadingMorePages) {
                                mIsLoadingMorePages = false;
                                if (adapterSmallHorizontalCarousel != null) {
                                    adapterSmallHorizontalCarousel.addData(dataList);
                                }
                                return;
                            }
                            addCarouselData(dataList, carouselInfoData);

                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            if (dataList == null || dataList.isEmpty()) {
                                // showNoDataMessage();
                                recyclerViewEpisodes.setVisibility(View.GONE);
                                if(mStartIndex>1) {
                                    mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                                }else{
                                    mRecyclerViewCarousel.setVisibility(View.GONE);
                                }
                                return;
                            }
                            if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null) {
                                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.queueListCardData(dataList);
                                }
                            if (carouselInfoData!=null  && episodeDisplayData1!=null && (dataList != null && dataList.size() <  episodeDisplayData1.count)) {
                                mIsLoadingMoreAvailable = false;
                            }
                            if (mIsLoadingMorePages) {
                                mIsLoadingMorePages = false;
                                if (adapterSmallHorizontalCarousel != null) {
                                    adapterSmallHorizontalCarousel.addData(dataList);
                                }
                                return;
                            }
                            addCarouselData(dataList, carouselInfoData);

                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                                return;
                            }
                            if (mIsLoadingMorePages) {
                                mIsLoadingMorePages = false;
                                return;
                            }
                            // showNoDataMessage();
                        }
                    });
        }
    }



    private List<EpisodeDisplayData> getEpisodesData(CardData dataList) {
        List<EpisodeDisplayData> seasons = new ArrayList<>();
        if (dataList.generalInfo.showDisplayTabs != null
                &&dataList.generalInfo.showDisplayTabs.showDisplayType!=null
                &&dataList.generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) {
            ShowDisplayTabs showDisplayTabs = dataList.generalInfo.showDisplayTabs;
            if (showDisplayTabs.showDisplayfrequency != null) {
                int totalTabsSize = (int) Math.ceil((Double.parseDouble(showDisplayTabs.showDisplayEnd) -
                        Double.parseDouble(showDisplayTabs.showDisplayStart))/Double.parseDouble(showDisplayTabs.showDisplayfrequency));
                int frequency = Integer.parseInt(showDisplayTabs.showDisplayfrequency);
                int showDisplayEnd = Integer.parseInt(showDisplayTabs.showDisplayEnd);
                int showDisplayStart=Integer.parseInt(showDisplayTabs.showDisplayStart);
                int firstItem=0;
                int secondItem=0;
                for (int p = 0; p < totalTabsSize; p++) {
                    EpisodeDisplayData episodeDisplayData=new EpisodeDisplayData();
                    if (p==0){
                        firstItem = showDisplayStart;
                    }else {
                        firstItem = secondItem;
                    }
                    secondItem = firstItem + frequency;
                    episodeDisplayData.startIndex=p+1;
                    episodeDisplayData.count=(secondItem-firstItem);
                    episodeDisplayData.seasonId = dataList._id;
                   /* if (p == totalTabsSize - 1) {
                        episodeDisplayData.episodeTabName = "Latest Episodes";
                    } else {*/
                        int secondItemToDisplay = secondItem - 1;
                        episodeDisplayData.episodeTabName
                                = showDisplayTabs.showDisplayText + " " + firstItem + "-" + secondItemToDisplay;
                   /* }*/
                    seasons.add(episodeDisplayData);
                }
            }
        }
        Collections.reverse(seasons);
        return seasons;
    }

    private List<EpisodeDisplayData> getSeasons(List<CardData> dataList) {
        Collections.reverse(dataList);
        List<EpisodeDisplayData> seasons = new ArrayList<>();
            String seasonText = "Season ";
            for (CardData cardData : dataList) {
                EpisodeDisplayData episodeDisplayData = new EpisodeDisplayData();
                if (cardData != null) {
                    if(cardData != null && cardData.content != null && cardData.content.serialNo != null)
                        episodeDisplayData.episodeTabName = seasonText + cardData.content.serialNo;
                    episodeDisplayData.showDisplayTabs=cardData.generalInfo.showDisplayTabs;
                    episodeDisplayData.seasonId=cardData._id;
                    episodeDisplayData.count = 10;
                    seasons.add(episodeDisplayData);
                }
            }
//        Collections.reverse(seasons);
        return seasons;
    }

}
