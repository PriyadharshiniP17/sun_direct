package com.myplex.myplex.ui.views;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager2.widget.ViewPager2;


import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.RequestState;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.util.StringManager;
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
import com.myplex.myplex.ui.adapter.RecyclerAnimationAdapter;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.Util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AnimationRecyclerViewItem extends GenericListViewCompoment {

    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = AnimationRecyclerViewItem.class.getSimpleName();
    private RecyclerView mRecyclerViewCarouselInfo;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    RecyclerAnimationAdapter imageSliderAdapter = null;
    String mBgColor;

    private long prePos = -1;
    private Handler pauseHandler = new Handler();
    private Handler startHandler = new Handler();
    public static  int PAUSE_COUNT_FACTOR = 100;
    public static  int RESUME_COUNT_FACTOR = 3000;

    private AnimationRecyclerViewItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo,
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

    public static AnimationRecyclerViewItem createView(Context context, ViewGroup parent,
                                                       List<CarouselInfoData> carouselInfoData,
                                                       RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup, String mPageTitle, String mBgColor) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_anim_recyclerview, parent, false);

        return new AnimationRecyclerViewItem(context, view,
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
            holder.animation3DRecyclerView.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(carouselInfoData.name)
                    && carouselInfoData.requestState == RequestState.NOT_LOADED) {
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
            }
        } else {
            holder.mLayoutViewAll.setVisibility(View.GONE);
            holder.mImageViewAll.setVisibility(View.GONE);
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
            if (carouselInfoData!=null&&carouselInfoData.showTitle) {
                if (carouselInfoData.title != null) {
                    mTextViewGenreMovieTitle.setText(carouselInfoData.title);
                } else {
                    mTextViewGenreMovieTitle.setVisibility(View.GONE);
                }
            }
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
            if (holder.animation3DRecyclerView.getTag() != null
                    && holder.animation3DRecyclerView.getTag() instanceof RecyclerAnimationAdapter) {
                imageSliderAdapter = (RecyclerAnimationAdapter) holder.animation3DRecyclerView.getTag();
            } else {
                imageSliderAdapter = new RecyclerAnimationAdapter(mContext);
                imageSliderAdapter.setItems(carouselInfoData.listCarouselData);
            }
            holder.animation3DRecyclerView.setTag(imageSliderAdapter);
            //imageSliderAdapter.setInfiniteLoop(false);


            imageSliderAdapter.setOnItemClickListener(new RecyclerAnimationAdapter.OnItemClickListener() {
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
            //holder.animation3DRecyclerView.setIndicatorsToShow(carouselInfoData.listCarouselData.size());

            final CenterLayoutManager layoutManager = new CenterLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false);
            holder.animation3DRecyclerView.setLayoutManager(layoutManager);
            //layoutManager.smoothScrollToPosition(holder.animation3DRecyclerView,null,2);


            SnapHelper helper = new LinearSnapHelper();
            holder.animation3DRecyclerView.setOnFlingListener(null);
            helper.attachToRecyclerView(holder.animation3DRecyclerView);

            holder.animation3DRecyclerView.setAdapter(imageSliderAdapter);
            holder.animation3DRecyclerView.setVisibility(View.VISIBLE);

            //holder.mDotsIndicator.setRecyclerView(holder.animation3DRecyclerView,helper,imageSliderAdapter.getItemCount());

            holder.pageIndicatorView.setCount(imageSliderAdapter.getItemCount());

            holder.animation3DRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) {
                        long position = getCurrentItem();
                        if (prePos != position) {
                            prePos = position;

                            if (DeviceUtils.isTablet(mContext)) {
                                PAUSE_COUNT_FACTOR=100;
                                RESUME_COUNT_FACTOR=3100;
                            }else {
                                PAUSE_COUNT_FACTOR=400;
                                RESUME_COUNT_FACTOR=3400;
                            }

                            if (pauseHandler != null) {
                                pauseHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.animation3DRecyclerView.pauseAutoScroll(true);
                                    }
                                }, PAUSE_COUNT_FACTOR);
                            }
                            if (startHandler != null) {
                                startHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.animation3DRecyclerView.startAutoScroll();
                                    }
                                },RESUME_COUNT_FACTOR);
                            }

                        }
                    }
                }
            });


            holder.animation3DRecyclerView.startAutoScroll();
            holder.animation3DRecyclerView.setLoopEnabled(true);
            holder.animation3DRecyclerView.setCanTouch(true);
            imageSliderAdapter.setOnItemClickListener(onItemClickListener);
            imageSliderAdapter.setPagerIndicator(holder.pageIndicatorView);

            holder.animation3DRecyclerView.addOnItemTouchListener(new
                    RecyclerTouchListener(mContext, holder.animation3DRecyclerView, new ClickListener() {
                @Override
                public void onClick(View view, int contentPosition) {
                    if (carouselInfoData.listCarouselData!=null&&!carouselInfoData.listCarouselData.isEmpty()){
                        int actualPosition=getActualPosition(contentPosition,carouselInfoData.listCarouselData.size());
                        CardData cardData=carouselInfoData.listCarouselData.get(actualPosition);
                        if (cardData == null || cardData.isAdType()) {
                            return;
                        }
                        showDetailsFragment(cardData, -1,carouselInfoData.title,position, contentPosition);
                    }
                }
            }));

            //holder.animation3DRecyclerView.pauseAutoScroll(true);



            /*final int speedScroll = 2500;
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    if(count < imageSliderAdapter.getItemCount()-1) {
                        layoutManager.smoothScrollToPosition(holder.animation3DRecyclerView,null,count++);
                    }else {
                        count=1;
                        layoutManager.smoothScrollToPosition(holder.animation3DRecyclerView,null,1);
                    }
                    handler.postDelayed(this, speedScroll);
                    pageIndicatorView.setSelection(count);
                }
            };

            handler.postDelayed(runnable,speedScroll);*/
        }
    }

    private long getCurrentItem() {
        return ((LinearLayoutManager) holder.animation3DRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
    }

    private int getActualPosition(int position,int itemCount) {
        return position >= itemCount ? position % itemCount : position;
    }

    interface ClickListener{
        void onClick(View view,int position);
    }

    public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        Context mContext;
        RecyclerView mRecyclerView;
        ClickListener mClickListener;

        GestureDetector gestureDetector;



        public RecyclerTouchListener(Context mContext,RecyclerView mRecyclerView, ClickListener mClickListener){
            this.mClickListener=mClickListener;
            this.mRecyclerView=mRecyclerView;
            this.mContext=mContext;

            gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && mClickListener != null) {
                        mClickListener.onClick(child, mRecyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if (child != null && mClickListener != null && gestureDetector.onTouchEvent(e)) {
                mClickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public RecyclerAnimationAdapter.OnItemClickListener onItemClickListener=new RecyclerAnimationAdapter.OnItemClickListener() {
        @Override
        public void onItemClicked(CardData cardData, int contentPosition) {
            if (cardData == null || cardData.isAdType()) {
                return;
            }

            if (cardData != null
                    && cardData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                    && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                return;
            }
            showDetailsFragment(cardData, -1,"",position, contentPosition);
        }
    };

    public int Dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private class DepthPageTransformer implements ViewPager2.PageTransformer {
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
