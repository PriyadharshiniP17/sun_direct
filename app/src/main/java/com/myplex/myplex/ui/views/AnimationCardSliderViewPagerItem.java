package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import cardsliderviewpager.viewpager2.ViewPager2;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.RequestState;
import com.myplex.util.StringManager;
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
import com.myplex.myplex.ui.adapter.CardSliderViewPagerAdapter;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.utils.Util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AnimationCardSliderViewPagerItem extends GenericListViewCompoment {

    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = AnimationCardSliderViewPagerItem.class.getSimpleName();
    private RecyclerView mRecyclerViewCarouselInfo;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    CardSliderViewPagerAdapter imageSliderAdapter = null;
    String mBgColor;

    private AnimationCardSliderViewPagerItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo,
                                             RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup,
                                             String mPageTitle, String mBgColor) {
        super(view);
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        this.mBgColor=mBgColor;
    }

    public static AnimationCardSliderViewPagerItem createView(Context context, ViewGroup parent,
                                                              List<CarouselInfoData> carouselInfoData,
                                                              RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup, String mPageTitle, String mBgColor) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_anim_card_slider_view_pager, parent, false);

        return new AnimationCardSliderViewPagerItem(context, view,
                carouselInfoData, mRecyclerViewCarouselInfo, mMenuGroup, mPageTitle,mBgColor);
    }

    @Override
    public void bindItemViewHolder(final int position) {

        this.position = position;

        if (mListCarouselInfo == null) {
            return;
        }
        final CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);

        if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
            holder.cardSliderViewPager.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(carouselInfoData.name)
                    && carouselInfoData.requestState == RequestState.NOT_LOADED) {
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
            }
        } else {
            holder.mLayoutViewAll.setVisibility(View.GONE);
            holder.mImageViewAll.setVisibility(View.GONE);
            holder.mLayoutViewAll.setOnClickListener(mViewAllClickListener);
            holder.mLayoutViewAll.setTag(carouselInfoData);
            if(carouselInfoData.showTitle){
                holder.mLayoutCarouselTitle.setVisibility(View.GONE);
            }else {
                holder.mLayoutCarouselTitle.setVisibility(View.GONE);
            }

            if (carouselInfoData.enableShowAll) {
                holder.mLayoutCarouselTitle.setTag(carouselInfoData);
                //holder.mLayoutCarouselTitle.setOnClickListener(mViewAllClickListener);
                holder.mImageViewAll.setVisibility(View.VISIBLE);
                holder.mLayoutViewAll.setVisibility(View.VISIBLE);
                holder.mTextViewViewAllOtherLang.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.MORE))) {
                    holder.mTextViewViewAllOtherLang.setText(StringManager.getInstance().getString(APIConstants.MORE));
                }else{
                    holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
                }

                if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN){
                    holder.mTextViewViewAll.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.MORE))) {
                        holder.mTextViewViewAllOtherLang.setVisibility(View.VISIBLE);
                    }else{
                        holder.mTextViewViewAll.setVisibility(View.VISIBLE);
                    }
                }else{
                    if (!TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.MORE))) {
                        holder.mTextViewViewAllOtherLang.setVisibility(View.VISIBLE);
                    }else{
                        holder.mTextViewViewAll.setVisibility(View.VISIBLE);
                        holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
                    }
                    holder.mTextViewViewAll.setVisibility(View.VISIBLE);

                }

                if (!TextUtils.isEmpty(carouselInfoData.showAll)) {
                    holder.mTextViewViewAll.setText(carouselInfoData.showAll);
                }
            }
            holder.mTextViewViewAll.setVisibility(View.GONE);
            holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
            holder.mTextViewGenreMovieTitle.setText(carouselInfoData.title == null ? "" : carouselInfoData.title);
            if(carouselInfoData.altTitle != null && !carouselInfoData.altTitle.isEmpty()){
                holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.VISIBLE);
                holder.mTextViewGenreMovieTitleOtherLang.setText(carouselInfoData.altTitle );

            }
            if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN ){
                holder.mTextViewGenreMovieTitle.setVisibility(View.GONE);
                if(carouselInfoData.altTitle == null || carouselInfoData.altTitle.isEmpty()){
                    holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.GONE);

                    holder.mTextViewGenreMovieTitle.setVisibility(View.VISIBLE);
                }else{
                    holder.mTextViewGenreMovieTitleOtherLang.setTextColor(mContext.getResources().getColor(R.color.gray_text));
                    holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.VISIBLE);
                }
            }else{
                holder.mTextViewGenreMovieTitle.setVisibility(View.VISIBLE);

                if(carouselInfoData.altTitle != null && !carouselInfoData.altTitle.isEmpty()){
                    holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.VISIBLE);
                }else{
                    holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.GONE);
                    holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
                }
            }

            if (mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
                holder.mTextViewGenreMovieTitleOtherLang.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
                holder.mTextViewGenreMovieTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            }

            imageSliderAdapter = null;
            if (holder.cardSliderViewPager.getTag() != null
                    && holder.cardSliderViewPager.getTag() instanceof CardSliderViewPagerAdapter) {
                imageSliderAdapter = (CardSliderViewPagerAdapter) holder.cardSliderViewPager.getTag();
            } else {
                imageSliderAdapter = new CardSliderViewPagerAdapter(mContext);
                imageSliderAdapter.setItems(carouselInfoData.listCarouselData);
            }
            holder.cardSliderViewPager.setTag(imageSliderAdapter);

            imageSliderAdapter.setOnItemClickListener(new CardSliderViewPagerAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(final CardData cardData, int contentPosition) {

                    if (cardData == null || cardData.isAdType()) {
                        return;
                    }

                    String publishingHouse = cardData == null
                            || cardData.publishingHouse == null
                            || TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName) ? null : cardData.publishingHouse.publishingHouseName;

                    if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                        HungamaPartnerHandler.launchDetailsPage(cardData, mContext, carouselInfoData, null,contentPosition);
                        return;
                    }
                    if (cardData != null
                            && cardData.generalInfo != null
                            && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                            && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                        mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                        return;
                    }
                    showDetailsFragment(cardData, -1,carouselInfoData.title,position, contentPosition);
                }
            });
            //holder.mListPlayerRecyclerCarousel.setIndicatorsToShow(carouselInfoData.listCarouselData.size());

            /*final CenterLayoutManager layoutManager = new CenterLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,
                    false);
            holder.mListPlayerRecyclerCarousel.setLayoutManager(layoutManager);
            layoutManager.smoothScrollToPosition(holder.mListPlayerRecyclerCarousel,null,1);

            SnapHelper helper = new LinearSnapHelper();
            holder.mListPlayerRecyclerCarousel.setOnFlingListener(null);
            helper.attachToRecyclerView(holder.mListPlayerRecyclerCarousel);
*/

            holder.cardSliderViewPager.setAdapter(imageSliderAdapter);
            //holder.mRecyclerViewCircleIndicator.attachToRecyclerView(holder.mListPlayerRecyclerCarousel,helper);
            /*int pagerPadding = (int) mContext.getResources().getDimension(R.dimen.margin_gap_8);
            holder.mListPlayerRecyclerCarousel.setClipToPadding(false);
            holder.mListPlayerRecyclerCarousel.setPadding(pagerPadding, 0, pagerPadding, 0);
            holder.mListPlayerRecyclerCarousel.setClipChildren(false);*/

            holder.cardSliderViewPager.setVisibility(View.VISIBLE);


            if (carouselInfoData.listCarouselData != null
                    && carouselInfoData.listCarouselData.size() > 1) {
                holder.cardSliderViewPager.setOffscreenPageLimit(3);
                holder.cardSliderViewPager.setCurrentItem(1);
            }

            //holder.cardSliderViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

//            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
//            compositePageTransformer.addTransformer(new MarginPageTransformer(40));
//            compositePageTransformer.addTransformer((page, position1) -> {
//                float r = 1 - Math.abs(position1);
//                page.setScaleY(0.85f + r * 0.15f);
//            });
            /*DepthPageTransformer depthPageTransformer = new DepthPageTransformer();
            holder.cardSliderViewPager.setPageTransformer(depthPageTransformer);
*/
            /*Handler handler=new Handler();
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    if (holder.mViewPager2.getCurrentItem() == imageSliderAdapter.getItemCount() - 1) { //adapter is your custom ViewPager's adapter
                        holder.mViewPager2.setCurrentItem(0);
                    }
                    else {
                        holder.mViewPager2.setCurrentItem(holder.mViewPager2.getCurrentItem() + 1, true);
                    }
                    //holder.mViewPager2.setCurrentItem(holder.mViewPager2.getCurrentItem()+1,true);
                }
            };
            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(runnable);
                }
            },3500,3500);*/
        }
    }

    public int Dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static class CenterZoomLayoutManager extends LinearLayoutManager {

        private final float mShrinkAmount = 0.9f;
        private final float mShrinkDistance = 0.15f;

        public CenterZoomLayoutManager(Context context) {
            super(context);
        }

        public CenterZoomLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int orientation = getOrientation();
            if (orientation == HORIZONTAL) {
                int scrolled = super.scrollHorizontallyBy(dx, recycler, state);

                float midpoint = getWidth() / 2.f;
                float d0 = 0.f;
                float d1 = mShrinkDistance * midpoint;
                float s0 = 1.f;
                float s1 = 1.f - mShrinkAmount;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    float childMidpoint =
                            (getDecoratedRight(child) + getDecoratedLeft(child)) / 2.f;
                    float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
                    float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
                return scrolled;
            } else {
                return 0;
            }

        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);
            scrollHorizontallyBy(0, recycler, state);
        }
    }

    public class CenterLayoutManager extends LinearLayoutManager {

        private final float mShrinkAmount = 0.9f;
        private final float mShrinkDistance = 0.15f;

        public CenterLayoutManager(Context context) {
            super(context);
        }

        public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public CenterLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

            final float SPEED = 2f;
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext()) {
                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return SPEED / displayMetrics.densityDpi;
                }
            };
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);

        }

        private class CenterSmoothScroller extends LinearSmoothScroller {

            CenterSmoothScroller(Context context) {
                super(context);
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return super.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            return super.scrollVerticallyBy(dy, recycler, state);
        }

        @Override
        public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int scrolled = super.scrollHorizontallyBy(dx, recycler, state);

            float midpoint = getWidth() / 2.f;

            float d0 = 0.f;
            float d1 = mShrinkDistance * midpoint;
            float s0 = 1.f;
            float s1 = 1.f - mShrinkAmount;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                float childMidpoint =
                        (getDecoratedRight(child) + getDecoratedLeft(child)) / 2.f;
                float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                if (scale < 0.75) {
                    scale = 0.78f;
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                } else {
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
            }
            return scrolled;
        }

        //must use this else, carousel effect won't occur unless scroll

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);
            scrollHorizontallyBy(0, recycler, state);
        }
    }

    public class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.5f;
        private static final float MAX_SCALE = 0.6f;
        private static final float MIN_FADE = 0.8f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) {
                view.setAlpha(MIN_FADE);
            } else if (position < 0) {
                view.setAlpha(1 + position * (1 - MIN_FADE));
                view.setTranslationX(-pageWidth * MAX_SCALE * position);
                ViewCompat.setTranslationZ(view, position);
                float scaleFactor = MIN_SCALE
                        + (MAX_SCALE - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else if (position == 0) {
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(MAX_SCALE);
                ViewCompat.setTranslationZ(view, 0);
                view.setScaleY(MAX_SCALE);
            } else if (position <= 1) {
                ViewCompat.setTranslationZ(view, -position);
                view.setAlpha(1 - position * (1 - MIN_FADE));
                view.setTranslationX(pageWidth * MAX_SCALE * -position);

                float scaleFactor = MIN_SCALE
                        + (MAX_SCALE - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else {
                view.setAlpha(MIN_FADE);
            }
        }
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
            new MenuDataModel().setPortraitBannerRequest(APIConstants.isPortraitBannerLayout(carouselInfoData)).fetchCarouseldata(mContext, carouselInfoData.name, 1, carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT, isCacheRequest, carouselInfoData.modified_on, new MenuDataModel.CarouselContentListCallback() {
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

    private CardData getDummyCard(){
        CardData cardData=new CardData();
        cardData.generalInfo=new CardDataGeneralInfo();
        cardData.generalInfo.title="No Info";
        cardData.generalInfo.type=APIConstants.TYPE_ADBANNER_IMAGE;
        return cardData;
    }

    private void addCarouselData(final List<CardData> carouselList, final CarouselInfoData carouselInfoData) {
        if (carouselList == null) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
        } else {
            try {
                LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
                carouselInfoData.requestState = RequestState.SUCCESS;
                /*carouselList.add(0,getDummyCard());
                carouselList.add(carouselList.size(),getDummyCard());*/
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
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                } else if (carouselData != null
                        && carouselData.showAllLayoutType != null) {
                    if (carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
                        try {
                            ((MainActivity) mContext).redirectToPage(carouselData.showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
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

            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;


            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                int contentPosition = position;
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, carouselInfoData, null, parentPosition);
                return;
            }


            if (carouselData != null
                    && carouselData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !TextUtils.isEmpty(carouselData.generalInfo.deepLink)) {
                Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, carouselData.generalInfo.title, 1l);
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, carouselData.generalInfo.title));
                return;
            }

            if (carouselInfoData == null) {
                showDetailsFragment(carouselData, position, "", parentPosition, position);
            } else {
                showDetailsFragment(carouselData, carouselInfoData, parentPosition, position);
            }

        }

    };

    private void showDetailsFragment(CardData carouselData, CarouselInfoData carouselInfoData, int parentPosition, int contentPosition) {

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
        args.putInt(CleverTap.PROPERTY_CONTENT_POSITION, contentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void showDetailsFragment(CardData carouselData, int position, String carousalTitle, int parentPosition, int contentPosition) {

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
        args.putInt(CleverTap.PROPERTY_CONTENT_POSITION, contentPosition);
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
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_KIDS, carouselSectionName, movieData.generalInfo.title);
        } else if (mPageTitle != null) {
            Analytics.gaBrowseCarouselSection("browsed " + mPageTitle.toLowerCase(), carouselSectionName, movieData.generalInfo.title);
        }
    }
}
