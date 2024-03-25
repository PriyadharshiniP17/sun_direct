package com.myplex.myplex.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.RequestState;
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
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.adapter.BigWeeklyTrendingAdapterNew;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.StringManager;

import java.io.Serializable;
import java.sql.Array;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BigWeeklyTrendingItemNew extends GenericListViewCompoment {

    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = BigWeeklyTrendingItemNew.class.getSimpleName();
    private final RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselInfo;
    private View view;
    private CarouselInfoData carouselInfoData;
    private final RecyclerView.ItemDecoration mHorizontalMoviesDivieder;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    private AdapterCarouselInfo.CallbackListener callbackListener;
    BigWeeklyTrendingAdapterNew imageSliderAdapter = null;
    long timer;
    private Typeface mBoldTypeFace;

    public static BigWeeklyTrendingItemNew bigWeeklyTrendingItemNew;
    private BigWeeklyTrendingItemNew(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo,
                                     RecyclerView mRecyclerViewCarouselInfo,String mMenuGroup,
                                     String mPageTitle,CarouselInfoData parentCarouselInfo) {
        super(view);
        this.bigWeeklyTrendingItemNew = this;
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        this.parentCarouselInfo = parentCarouselInfo;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
    }

    public static BigWeeklyTrendingItemNew createView(Context context, ViewGroup parent,
                                                      List<CarouselInfoData> carouselInfoData,
                                                      RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup, String mPageTitle, CarouselInfoData parentCarouselInfo) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_big_weekly_trending_new, parent, false);
      /*  if (!DeviceUtils.isTablet(context)) {
            int Height = (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
            view.setMinimumHeight(Height);
        }*/
        return new BigWeeklyTrendingItemNew(context, view,
                carouselInfoData, mRecyclerViewCarouselInfo, mMenuGroup,  mPageTitle,parentCarouselInfo);
    }

    @Override
    public void bindItemViewHolder(final int position) {

//        HashMap<String, Array[]> hashMap = new HashMap<>();
        this.position = position;
        if (DeviceUtils.isTablet(mContext)) {
            DisplayMetrics dm = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            holder.viewPagerCustomDurationNew.setScrollDurationFactor(5);
            Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth + " dm.density- " + dm.density + " real width- " + (ApplicationController.getApplicationConfig().screenWidth / dm.density));
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                int Height =  (int) mContext.getResources().getDimensionPixelSize(R.dimen.big_weekly_item_tab_height_land);
                holder.viewPagerCustomDurationNew.getLayoutParams().height = Height;
//                holder.mPreviewLayout.getLayoutParams().height = Height;
//                holder.leftGradient.getLayoutParams().height = Height;
//                holder.rightGradient.getLayoutParams().height = Height;
                /*holder.mViewPagerContainer.getLayoutParams().height = Height +
                        (int) mContext.getResources().getDimension(R.dimen.title_image_gap) +
                        (int) mContext.getResources().getDimension(R.dimen._30sdp)+50;*/

//                holder.gradientContainer.getLayoutParams().height = Height;
//                holder.mViewPagerContainer.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + holder.viewPagerCustomDurationNew.getLayoutParams().width);
            } else {
                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                //  int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
                int Height =  (int) mContext.getResources().getDimensionPixelSize(R.dimen.big_weekly_item_tab_height);

//                holder.mPreviewLayout.getLayoutParams().height = Height;
                holder.viewPagerCustomDurationNew.getLayoutParams().height = Height;
                holder.mViewPagerContainer.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_height)+
                (int) mContext.getResources().getDimension(R.dimen.title_image_gap)  +
                        (int) mContext.getResources().getDimension(R.dimen._30sdp);
//                holder.leftGradient.getLayoutParams().height = Height;
//                holder.rightGradient.getLayoutParams().height = Height;
//                holder.gradientContainer.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            }
        } else {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            DisplayMetrics dm = new DisplayMetrics();
//            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
//            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
//            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

//                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
//                int Height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_275);
//                holder.viewPagerCustomDurationNew.getLayoutParams().height = Height;
//                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + holder.viewPagerCustomDurationNew.getLayoutParams().width);
            } else {
//            DisplayMetrics dm = new DisplayMetrics();
//            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
//            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
//            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
                //mViewHolder.imageView.getLayoutParams().height = convertView.getResources().getDimensionPixelSize(R.dimen.big_weekly_item_width);
//                int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
                holder.viewPagerCustomDurationNew.getLayoutParams().height = mContext.getResources().getDimensionPixelSize(R.dimen.margin_168);
//                holder.leftGradient.getLayoutParams().height = Height;
//                holder.rightGradient.getLayoutParams().height = Height;
//                holder.gradientContainer.getLayoutParams().height = Height;
//                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            }
        }
        if (mListCarouselInfo == null) {
            return;
        }
        carouselInfoData = mListCarouselInfo.get(position);

        if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
            //Log.d(TAG, "carouselInfoData.listCarouselData");
            holder.mPreviewLayout.setVisibility(View.VISIBLE);
            holder.mViewPagerContainer.setVisibility(View.GONE);
            holder.viewPagerCustomDurationNew.setVisibility(View.GONE);
            holder.mOfferDecriptionLayout.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(carouselInfoData.name)
                    && carouselInfoData.requestState == RequestState.NOT_LOADED) {
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
            }
        } else {
            imageSliderAdapter = null;
            if (holder.viewPagerCustomDurationNew.getTag() != null
                    && holder.viewPagerCustomDurationNew.getTag() instanceof BigWeeklyTrendingAdapterNew) {
                imageSliderAdapter = (BigWeeklyTrendingAdapterNew) holder.viewPagerCustomDurationNew.getTag();
            } else {
                imageSliderAdapter = new BigWeeklyTrendingAdapterNew(mContext);
// no need to add if(buildconfig.flavor)
                imageSliderAdapter.setTextureData(carouselInfoData.texture);
                imageSliderAdapter.setItems(carouselInfoData.listCarouselData);
                if(carouselInfoData != null && carouselInfoData.listCarouselData != null &&carouselInfoData.listCarouselData.size()==1){
                    imageSliderAdapter.setInfiniteLoop(false);
                }else{
                    imageSliderAdapter.setInfiniteLoop(true);
                }

            }
            holder.viewPagerCustomDurationNew.setTag(imageSliderAdapter);
            holder.viewPagerCustomDurationNew.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    if (PrefUtils.getInstance().getBannerAutoScrollFrequency() != null)
                        timer = Integer.parseInt(PrefUtils.getInstance().getBannerAutoScrollFrequency()) * 1000;
                    else
                        timer = 2500;
                    SDKLogger.debug("TimeStamp "+ Calendar.getInstance().getTime()+" , Position "+holder.viewPagerCustomDurationNew.getCurrentItem());
//                    holder.viewPagerCustomDurationNew.setInterval(((int) timer));
                    currentPage = position;
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                    autoScrollHandler.postDelayed(autoScrollRunnable, timer);

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        int pageCount=0;
                        if (carouselInfoData != null && carouselInfoData.listCarouselData != null && carouselInfoData.listCarouselData.size() > 0){
                            pageCount= carouselInfoData.listCarouselData.size();
                        }
                        /*if (currentPage == 0) {
                            holder.viewPagerCustomDurationNew.setCurrentItem(pageCount-1, true);
                        } else if (currentPage == pageCount - 1) {
                            holder.viewPagerCustomDurationNew.setCurrentItem(0, true);
                        }*/
                    }

                }
            });
            holder.mLayoutViewAll.setVisibility(View.INVISIBLE);
            if (carouselInfoData!=null&&carouselInfoData.showTitle) {
                if (carouselInfoData.title != null) {
                    mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                    mTextViewGenreMovieTitle.setText(carouselInfoData.title);
                    mTextViewGenreMovieTitle.setTextColor(mContext.getResources().getColor(R.color.yellow_rent_availble));
                } else {
                    mTextViewGenreMovieTitle.setVisibility(View.GONE);
                }
            }
            holder.mLayoutViewAll.setTag(carouselInfoData);
            holder.mLayoutViewAll.setOnClickListener(mViewAllClickListener);
            holder.mImageViewAll.setVisibility(View.INVISIBLE);
            if(holder.mTextViewViewAll!=null){
                holder.mTextViewViewAll.setTag(carouselInfoData);
                holder.mTextViewViewAll.setOnClickListener(mViewAllClickListener);
                holder.mTextViewViewAll.setTextColor(mContext.getResources().getColor(R.color.yellow_rent_availble));

            }
            holder.mLayoutViewAll.setVisibility(View.INVISIBLE);
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
                holder.mLayoutViewAll.setVisibility(View.INVISIBLE);
            imageSliderAdapter.setOnItemClickListener(new BigWeeklyTrendingAdapterNew.OnItemClickListener() {
                @Override
                public void onItemClicked(final CardData cardData) {

                    if (cardData == null) {
                        return;
                    }

                    String publishingHouse = cardData == null
                            || cardData.publishingHouse == null
                            || TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName) ? null : cardData.publishingHouse.publishingHouseName;

                    if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                        HungamaPartnerHandler.launchDetailsPage(cardData, mContext, carouselInfoData, null);
                        return;
                    }

                    /*if (cardData.generalInfo != null
                            && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type))) {
                        if (cardData.generalInfo != null && cardData.generalInfo.title != null) {
                            if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                                Analytics.gaBrowseTVShows(cardData.generalInfo.title);
                            } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)) {
                                Analytics.gaBrowseVideos(cardData.generalInfo.title);
                            }
                        }
                        showRelatedVODListFragment(cardData, -1);
                        return;
                    }*/
                    if (cardData != null
                            && cardData.generalInfo != null
                            && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                            && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                        stopScroll();
                        mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                        return;
                    }
                    stopScroll();
                    showDetailsFragment(cardData, -1,carouselInfoData.title,position);
                }
            });

            holder.viewPagerCustomDurationNew.setAdapter(imageSliderAdapter);
//            holder.viewPagerCustomDurationNew.setCycle(true);
//            int pagerPadding = (int) mContext.getResources().getDimension(R.dimen.margin_gap_8);
//            SDKLogger.debug("pagerPadding- " + pagerPadding);
            holder.viewPagerCustomDurationNew.setClipToPadding(false);
            // holder.viewPagerCustomDurationNew.setPadding(pagerPadding, 0, pagerPadding, 0);
            int thumbnailGap = (int) mContext.getResources().getDimension(R.dimen.margin_gap_8);

            //  holder.viewPagerCustomDurationNew.setOffscreenPageLimit(3);

            if (DeviceUtils.isTablet(mContext)) {
//                holder.viewPagerCustomDurationNew.setPageMargin(thumbnailGap);
                SDKLogger.debug("banner_image_padding: dimen- " + mContext.getResources().getDimension(R.dimen.banner_image_padding));
                int pageMargin = 20;
//                holder.viewPagerCustomDurationNew.setPageMargin(20);
                int pagePadding = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_width_land);
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    pagerPadding = (int) (getPagerPadding() + mContext.getResources().getDimension(R.dimen.banner_image_padding));
//                    SDKLogger.debug("pagerPadding- " + pagerPadding);
                    holder.viewPagerCustomDurationNew.setClipToPadding(false);
                    // holder.viewPagerCustomDurationNew.setPadding(pagerPadding, 0, pagerPadding, 0);
//                    holder.viewPagerCustomDurationNew.setPadding((int)(pagePadding/3), 0, (int)(pagePadding/3), 0);
                    String devResolution = UiUtil.getScreenDensity(mContext);
                    if(devResolution.equals(ApplicationConfig.HDPI)){
                        holder.viewPagerCustomDurationNew.setPadding((int)(pagePadding/3), 0, (int)(pagePadding/3), 0);
                    }else
                        holder.viewPagerCustomDurationNew.setPadding((int) getPagerPadding() , 0,(int) getPagerPadding(), 0);

                    if (carouselInfoData.listCarouselData != null
                            && carouselInfoData.listCarouselData.size() > 1) {

                        holder.viewPagerCustomDurationNew.setCurrentItem(1);
//                        holder.viewPagerCustomDurationNew.setPadding(pagePadding/4, 0, pagePadding/4, 0);
//                        holder.viewPagerCustomDurationNew.setPageMargin(pageMargin);
                    }
                } else {
                    pagePadding = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_width);

                    if (carouselInfoData.listCarouselData != null
                            && carouselInfoData.listCarouselData.size() > 1) {

                        holder.viewPagerCustomDurationNew.setCurrentItem(1);
//                        holder.viewPagerCustomDurationNew.setPadding((int)(pagePadding/3), 0, (int)(pagePadding/3), 0);

//                        holder.viewPagerCustomDurationNew.setPageMargin(8);
//                        holder.viewPagerCustomDurationNew.setPadding(15, 0, 15, 0);
//                        int margin = (int) mContext.getResources().getDimensionPixelSize(R.dimen.thumbnail_gap);
                       // holder.viewPagerCustomDurationNew.setPageMargin(8);
                    }

                }

               /* holder.viewPagerCustomDurationNew.setPageTransformer(false, new ViewPager.PageTransformer() {
                    @Override public void transformPage(View page, float position) {
                        if (holder.viewPagerCustomDurationNew.getCurrentItem() == 0) {
                            page.setTranslationX(-pagePadding/4);
                        } else if (holder.viewPagerCustomDurationNew.getCurrentItem() == imageSliderAdapter.getCount() - 1) {
                            page.setTranslationX(pagePadding/4);        } else {
                            page.setTranslationX(0);
                        }
                    }});*/

                holder.viewPagerCustomDurationNew.invalidate();

            } else {
                holder.viewPagerCustomDurationNew.setPageMargin(thumbnailGap);
                if (carouselInfoData.listCarouselData != null
                        && carouselInfoData.listCarouselData.size() > 1) {
                    holder.viewPagerCustomDurationNew.setCurrentItem(1);
                }
            }
//            holder.mIndicator.setViewPager(holder.mViewPager);

//                holder.mDescriptionTxt.setText(mSliderItems.get(0).ottApp.offerDescription);
            SDKLogger.debug("GroupName- " + mMenuGroup + " position- " + position + " screen size width- " + holder.viewPagerCustomDurationNew.getLayoutParams().width + " height- " + holder.viewPagerCustomDurationNew.getLayoutParams().height);
            holder.mOfferDecriptionLayout.setVisibility(View.GONE);
            holder.viewPagerCustomDurationNew.setVisibility(View.VISIBLE);
            holder.mViewPagerContainer.setVisibility(View.VISIBLE);
            holder.mPreviewLayout.setVisibility(View.GONE);
            holder.mIndicator.setPageCount(imageSliderAdapter.getRealCount());
            holder.mIndicator.setViewPager(holder.viewPagerCustomDurationNew);
//            holder.mDotsIndicator.setViewPager(holder.mViewPager,imageSliderAdapter.getRealCount());
           /* double timer;
            if (PrefUtils.getInstance().getBannerAutoScrollFrequency() != null)
                timer = Integer.parseInt(PrefUtils.getInstance().getBannerAutoScrollFrequency()) * 1.5 * 1000;
            else
                timer = 4000;*/
//            holder.viewPagerCustomDurationNew.setAutoScrollDurationFactor(10);
            // holder.viewPagerCustomDurationNew.setOffscreenPageLimit(3);
//            holder.viewPagerCustomDurationNew.setInterval(((int) timer));
            SDKLogger.debug("TimeStamp "+ Calendar.getInstance().getTime());
//            holder.viewPagerCustomDurationNew.setStopScrollWhenTouch(true);
//            if (holder.mViewPager != null) {
//                holder.viewPagerCustomDurationNew.startAutoScroll();
//            }
//              holder.mDotsIndicator.setViewPager(holder.mViewPager,imageSliderAdapter.getRealCount());
            holder.mLeftArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.viewPagerCustomDurationNew.setCurrentItem(holder.viewPagerCustomDurationNew.getCurrentItem() - 1);
                }
            });
            holder.mRightArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.viewPagerCustomDurationNew.setCurrentItem(holder.viewPagerCustomDurationNew.getCurrentItem() + 1);
                }
            });
            startScroll();
        }

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
    public void setCallBackListener(AdapterCarouselInfo.CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }


    private void startAsyncTaskInParallel(CarouselRequestTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private class CarouselRequestTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.Fr
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final CarouselInfoData carouselInfoData;

        public CarouselRequestTask(CarouselInfoData carouselInfoData) {
            this.carouselInfoData = carouselInfoData;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            boolean isCacheRequest = true;
            new MenuDataModel().setPortraitBannerRequest(APIConstants.isPortraitBannerLayout(carouselInfoData)).fetchCarouseldata(mContext, carouselInfoData.name, 1, carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT, isCacheRequest, carouselInfoData.modified_on,new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnCacheResults: name- " + carouselInfoData.name);
                    addCarouselData(dataList, carouselInfoData);
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnOnlineResults: name- " + carouselInfoData.name);
                    addCarouselData(dataList, carouselInfoData);
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {
                    addCarouselData(null, carouselInfoData);
                }
            });
            return null;
        }

    }

    private void addCarouselData(final List<CardData> carouselList, final CarouselInfoData carouselInfoData) {
        if (carouselList == null) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
        } else {
            try {
                LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
                carouselInfoData.requestState = RequestState.SUCCESS;
                carouselInfoData.listCarouselData = carouselList;
                notifyItemChanged();
            } catch (IllegalStateException e) {
                if (mRecyclerViewCarouselInfo != null) {
                    mRecyclerViewCarouselInfo.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged();
                        }
                    });
                }
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

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
            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;


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
                Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, carouselData.generalInfo.title, 1L);
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

    private int getPagerPadding() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int margin = metrics.widthPixels / 4;
        return margin;
    }

    int currentPage = 0;

    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                currentPage++;
//                if(mListCarouselInfo == null || mListCarouselInfo.get(currentPage) == null || mListCarouselInfo.get(currentPage).listCarouselData == null)
                if(carouselInfoData == null || carouselInfoData.listCarouselData == null)
                    return;

                if (currentPage >= carouselInfoData.listCarouselData.size()) {
                    currentPage = 0;
                }
                Log.d(TAG, "run: currentPage "+ currentPage);
                holder.viewPagerCustomDurationNew.setCurrentItem(currentPage, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Log.d("DEBUG", " select" + selectedPosition + " size " + parentCarouselData.listCarouselData.size() + " holder.mViewPager2.getChildCount() " + holder.mViewPager2.getChildCount());
        }
    };

    public static boolean isStarted = false;
    public void startScroll() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        //Log.d("DEBUG", " startScroll");
        autoScrollHandler.removeCallbacks(autoScrollRunnable);

        if (PrefUtils.getInstance().getBannerAutoScrollFrequency() != null)
            timer = (long) (Integer.parseInt(PrefUtils.getInstance().getBannerAutoScrollFrequency()) * 1.5 * 1000);
        else
            timer = 4000;
        autoScrollHandler.postDelayed(autoScrollRunnable, timer);
//        autoScrollRunnable.run();
    }

    public void stopScroll() {
        //Log.d("DEBUG", " stopScroll");
        isStarted = false;
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }
}
