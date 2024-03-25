package com.myplex.myplex.ui.views;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.BannerAdapter3D;
import com.myplex.myplex.ui.adapter.ViewPagerImageSliderAdapter;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.LinesIndicatorDecoration;
import com.myplex.myplex.utils.Util;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ramaraju on 11/26/2016.
 */

public class BannerHorizontalItem3D extends GenericListViewCompoment {

    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = BannerHorizontalItem3D.class.getSimpleName();
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselData;
    private GenericListViewCompoment holder = this;
    static int height;
    public static BannerHorizontalItem3D bannerHorizontalItem3D;

    private BannerHorizontalItem3D(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInfo) {
        super(view);
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
    }

    public static BannerHorizontalItem3D createView(Context context, ViewGroup parent,
                                                    List<CarouselInfoData> carouselInfoData,
                                                    RecyclerView mRecyclerViewCarouselInfo) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_banner_3d, parent, false);
        //int height = ApplicationController.getApplicationConfig().screenHeight - (int)context.getResources().getDimension(R.dimen._168sdp);
        //int Height = (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
        //  height = ApplicationController.getApplicationConfig().screenHeight - (int) context.getResources().getDimension(R.dimen._178sdp);
        if(DeviceUtils.isTablet(context)) {
           // height = ApplicationController.getApplicationConfig().screenHeight - ((int) context.getResources().getDimension(R.dimen.margin_gap_64) + (int) context.getResources().getDimension(R.dimen._100sdp));
            int widthRatio = 16;
            int heightRatio = 6;

            if(DeviceUtils.getScreenOrientation(context) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ){
                widthRatio = 16;
                heightRatio = 14;

            }

            height = (((ApplicationController.getApplicationConfig().screenHeight * heightRatio) / widthRatio));
        }
        else
            height = ApplicationController.getApplicationConfig().screenHeight - ((int) context.getResources().getDimension(R.dimen.margin_gap_64)+(int) context.getResources().getDimension(R.dimen._100sdp));
        //view.setMinimumHeight(height);

        bannerHorizontalItem3D = new BannerHorizontalItem3D(context, view,
                carouselInfoData, mRecyclerViewCarouselInfo);
        return bannerHorizontalItem3D;
    }

    @Override
    public void bindItemViewHolder(int position) {
        this.position = position;
        holder = this;

        if (mListCarouselInfo == null) {
            return;
        }
        if (mListCarouselInfo.size() <= position) {
            return;
        }
        if(DeviceUtils.isTablet(mContext)) {
            // height = ApplicationController.getApplicationConfig().screenHeight - ((int) context.getResources().getDimension(R.dimen.margin_gap_64) + (int) context.getResources().getDimension(R.dimen._100sdp));
            int widthRatio = 16;
            int heightRatio = 6;

            if(DeviceUtils.getScreenOrientation(mContext) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ){
                widthRatio = 16;
                heightRatio = 14;

            }

            height = (((ApplicationController.getApplicationConfig().screenHeight * heightRatio) / widthRatio));
        }
        holder.mViewPager3D.getLayoutParams().height = height;

        int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
        //   int c = (int) (((double) 9 / 16) * displayWidth);
        int displayHeight = ApplicationController.getApplicationConfig().screenHeight - (int) mContext.getResources().getDimension(R.dimen._205sdp);
       // holder.mPreviewLayout.getLayoutParams().height = displayHeight;
        holder.mPreviewLayout.getLayoutParams().width = displayWidth;

        parentCarouselData = mListCarouselInfo.get(position);

        if (parentCarouselData.listCarouselData == null || parentCarouselData.listCarouselData.isEmpty()) {
            Log.e(TAG, "carouselInfoData.listCarouselData");
            holder.mPreviewLayout.setVisibility(View.VISIBLE);

            holder.mViewPager3D.setVisibility(View.GONE);
            if (parentCarouselData.bgColor != null) {
                if (parentCarouselData.bgColor.equals("ComingSoon")) {
                    holder.mPreviewLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                }
            }
            if (!TextUtils.isEmpty(parentCarouselData.name)) {
                startAsyncTaskInParallel(new CarouselRequestTask(parentCarouselData.name, parentCarouselData.pageSize > 0 ? parentCarouselData.pageSize : APIConstants.PAGE_INDEX_COUNT, position, parentCarouselData.modified_on));
            }
        } else {
            BannerAdapter3D imageSliderAdapter;

            if (holder.mViewPager3D.getTag() instanceof BannerAdapter3D) {
                imageSliderAdapter = (BannerAdapter3D) holder.mViewPager3D.getTag();
            } else {
                imageSliderAdapter = new BannerAdapter3D(mContext, parentCarouselData.listCarouselData, true, parentCarouselData);
                double autoScrollTiming = 0;
              /*  if (PrefUtils.getInstance().getAutoScrollViewPagerTiming() != null) {
                    autoScrollTiming = Double.valueOf(PrefUtils.getInstance().getAutoScrollViewPagerTiming());
                }*/
                /*if (autoScrollTiming > 0) {
                    holder.mViewPager.setInterval((int) autoScrollTiming * 1000);
                } else {
                    holder.mViewPager.setInterval(5000);
                }
                if (parentCarouselData.listCarouselData != null && parentCarouselData.listCarouselData.size() < 2) {
                    holder.mViewPager.pauseAutoScroll();
                }*/
                holder.mViewPager3D.setTag(imageSliderAdapter);
                imageSliderAdapter.setOnItemClickListener(new ViewPagerImageSliderAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClicked(CardData cardData) {
                        if (cardData == null) {
                            return;
                        }
                        //  cardData.trackingId=parentCarouselData.trackingId;

                        if (cardData.generalInfo != null
                                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type))) {
                           /* if (cardData.generalInfo.title != null) {
                                if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                        || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                                       Analytics.gaBrowseTVShows(cardData.generalInfo.title);
                                } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)) {
                                       Analytics.gaBrowseVideos(cardData.generalInfo.title);
                                }
                            }*/
                            stopScroll();
                            showRelatedVODListFragment(cardData, -1);
                            return;
                        }
                        stopScroll();
//                        showDetailsFragment(cardData, parentCarouselData);
                        //Fixed the meta data is not displaying when clicked on watch now banners
                        if(cardData!=null ) {
                            if (cardData.globalServiceId != null)
                                getEPGData(cardData.globalServiceId);
                            else if (cardData._id != null) {
                                if (cardData.isLive()) {
                                    getEPGData(cardData._id);
                                } else {
                                    showDetailsFragment(cardData);
                                }
                            }
                        }
                    }
                });
            }
            CardData sliderModel = parentCarouselData.listCarouselData.get(0);
            if (!getImageLink2(sliderModel).isEmpty() && getImageLink2(sliderModel) != null){
                Glide.with(holder.bannerBg.getContext())
                        .load(getImageLink2(sliderModel))
                        .placeholder(R.drawable.black)
                        .error(R.drawable.black)
                        .dontAnimate()
                        // .apply(RequestOptions.bitmapTransform(new BlurTransformation(50)))
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                             startScroll();
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                stopScroll();
                            }
                        });

                Log.d(TAG, "OnOnlineResults: name- " + "hiii  "+getImageLink2(sliderModel));
                holder.mViewPager3D.setAdapter(imageSliderAdapter);
                setAlignment();
            }




            //if (Util.isValidContextForGlide(mContext))
            /*Glide.with(holder.bannerBg.getContext())
                    .load(getImageLink2(sliderModel))
                    .placeholder(R.drawable.black)
                    .error(R.drawable.black)
                    .dontAnimate()
                    // .apply(RequestOptions.bitmapTransform(new BlurTransformation(50)))
                    .into(holder.bannerBg);
            holder.mViewPager3D.setAdapter(imageSliderAdapter);
            setAlignment();*/

          /*  Glide.with(holder.bannerBg.getContext())
                    .load(getImageLink2(sliderModel))
                    .placeholder(R.drawable.black)
                    .error(R.drawable.black)
                    .dontAnimate()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // log exception
                            Log.e("TAG", "Error loading image", e);
                            return false; // important to return false so the error placeholder can be placed
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.mViewPager3D.setAdapter(imageSliderAdapter);
                            setAlignment();
                            return false;
                        }
                    })
                    .into(holder.bannerBg);*/
/*
            Glide.with(holder.bannerBg.getContext()).load(getImageLink2(sliderModel))
                    .placeholder(R.drawable.black).error(R.drawable.black).dontAnimate().listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d(TAG, "OnOnlineResults: name1- " + "hello");
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "OnOnlineResults: name- " + "hiii");
                            holder.bannerBg.setImageDrawable(resource);
                            holder.mViewPager3D.setAdapter(imageSliderAdapter);
                            setAlignment();

                            return false;
                        }
                    }).submit();
*/
//            holder.mViewPager.setOffscreenPageLimit(imageSliderAdapter.getListCount());
        }
    }

    private void setAlignment() {
        holder.mOfferDecriptionLayout.setVisibility(View.GONE);
        holder.mViewPager3D.setVisibility(View.VISIBLE);
        holder.mPreviewLayout.setVisibility(View.GONE);
        holder.mPreviewLayoutLL.setVisibility(View.GONE);
            /*holder.mIndicator.setViewPager(holder.mViewPager);
            holder.mIndicator.setPageCount(imageSliderAdapter.getListCount());
            holder.mViewPager.setIndicatorPageChangeListener(new LoopingViewPager.IndicatorPageChangeListener() {
                @Override
                public void onIndicatorProgress(int selectingPosition, float progress) {

                }

                @Override
                public void onIndicatorPageChange(int newIndicatorPosition) {
                    holder.mIndicator.onPageChangedReportedByLoopingViewPager(newIndicatorPosition);
                }
            });*/
        int density = mContext.getResources().getDisplayMetrics().densityDpi;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
            case DisplayMetrics.DENSITY_MEDIUM:
            case DisplayMetrics.DENSITY_HIGH:
            case DisplayMetrics.DENSITY_XHIGH:
                //  Toast.makeText(this, " " + density, Toast.LENGTH_SHORT).show();
                holder.mViewPager3D.setIntervalRatio(0.96f); // changed from 1f to 0.85f 0.82f
                break;
            default:
                //   Toast.makeText(this, " " + density, Toast.LENGTH_SHORT).show();
                holder.mViewPager3D.setIntervalRatio(0.96f); // changed from 1f to 0.82f 1.1f
                break;
        }

        holder.mViewPager3D.set3DItem(false);
        holder.mViewPager3D.setAlpha(false);

        if (parentCarouselData.listCarouselData.size() == 1) {
            holder.mViewPager3D.setInfinite(false);
        } else {
            holder.mViewPager3D.setInfinite(true);
        }
        holder.mViewPager3D.setFlat(true);
        holder.mViewPager3D.setItemSelectListener(new CarouselLayoutManager.OnSelected() {
            @Override
            public void onItemSelected(int i) {
                //Log.d("DEBUG", " onItemSelected");
                Log.d(TAG, "onItemSelected: i "+ i);
                selectedPosition = i;
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollTiming);
                CardData sliderModel = parentCarouselData.listCarouselData.get(i);
                if (Util.isValidContextForGlide(mContext))
                    Glide.with(holder.bannerBg.getContext())
                            .load(getImageLink2(sliderModel))
                            .placeholder(R.drawable.black)
                            .error(R.drawable.black)
                            .dontAnimate()
                            // .apply(RequestOptions.bitmapTransform(new BlurTransformation(50)))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(holder.bannerBg);
            }

            });
            //Ref: https://stackoverflow.com/questions/43656253/how-to-add-dots-under-horizontal-recyclerview
            int radius = holder.bannerBg.getContext().getResources().getDimensionPixelSize(R.dimen.dot_radius);
            int inactiveRadius = holder.bannerBg.getContext().getResources().getDimensionPixelSize(R.dimen.inactive_dot_radius);
            int dotsHeight = holder.bannerBg.getContext().getResources().getDimensionPixelSize(R.dimen.dots_height);
            int padding = holder.bannerBg.getContext().getResources().getDimensionPixelSize(R.dimen.padding);
            int colorActive = ContextCompat.getColor(holder.bannerBg.getContext(), R.color.yellow_strip);
            int colorInactive = ContextCompat.getColor(holder.bannerBg.getContext(), R.color.view_pager_unselected);
        /*    holder.mViewPager3D.addItemDecoration(
                    new LinesIndicatorDecoration(mContext));*/

        holder.mViewPager3D.addItemDecoration(
                new LinesIndicatorDecoration(
                        radius,
                        padding,
                        dotsHeight,
                        colorInactive,
                        colorActive,
                        inactiveRadius
                )
        );
        startScroll();
    }
    public void getEPGData(String contentId){
        /*final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);*/
        ChannelListEPG.Params params = new ChannelListEPG.Params(contentId, "", false, false);
        ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
            @Override
            public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        List<CardData> cardDataList = response.body().getResults().get(i).getPrograms();
                        if(cardDataList.size() >0){
                            showDetailsFragment(cardDataList.get(0));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                Log.d(TAG, "onFailure: ");
            }
        });
        APIService.getInstance().execute(channelListEPG);
    }
    private void showDetailsFragment(CardData carouselData) {

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

     /*   if (!APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType)) {
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
        }*/

        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        //    args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
        //  args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }
    //REF: https://stackoverflow.com/questions/14039454/how-can-you-tell-if-a-view-is-visible-on-screen-in-android
    public boolean isCarousalVisible() {
        final View view = holder.mViewPager3D;
        if (view == null)
            return false;

        if (!view.isShown())
            return false;

        final Rect actualPosition = new Rect();
        boolean isGlobalVisible = view.getGlobalVisibleRect(actualPosition);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        final Rect screen = new Rect(0, 0, screenWidth, screenHeight);

        return isGlobalVisible && Rect.intersects(actualPosition, screen);
    }

    private String getImageLink2(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER,APIConstants.IMAGE_TYPE_THUMBNAIL};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.HDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }
        }
        return null;
    }

    private void startAsyncTaskInParallel(CarouselRequestTask task) {
        //mProgressBar.setVisibility(View.VISIBLE);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CarouselRequestTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final String mPageName;
        private int mPosition;
        private int mCount;
        private String modifiedOn;

        public CarouselRequestTask(String pageName, int count, int position, String modifiedOn) {
            mPageName = pageName;
            mPosition = position;
            mCount = count;
            this.modifiedOn = modifiedOn;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            boolean isCacheRequest = true;
            String imageType;
            if (DeviceUtils.isTablet(mContext)) {
                imageType = "hdpi";
            } else {
                imageType = "mdpi";
            }
        /*    new MenuDataModel().fetchCarouseldata(mContext, mPageName, 1, mCount, isCacheRequest,  modifiedOn, new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    Log.e(getClass().getName(), "OnCacheResults: name- " + mPageName);

                    if (dataList != null *//*&& !dataList.isEmpty()*//*) {
                        addCarouselData(dataList, mPosition);
                    }
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    Log.e(getClass().getName(), "OnOnlineResults: name- " + mPageName);

                    if (dataList != null *//*&& !dataList.isEmpty()*//*) {
                        addCarouselData(dataList, mPosition);
                    }
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {

                }
            });*/
            new MenuDataModel().setPortraitBannerRequest(APIConstants.isPortraitBannerLayout(parentCarouselData)).fetchCarouseldata(mContext, parentCarouselData.name, 1, parentCarouselData.pageSize > 0 ? parentCarouselData.pageSize : APIConstants.PAGE_INDEX_COUNT, isCacheRequest, parentCarouselData.modified_on, new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnCacheResults: name- " + parentCarouselData.name);
                    addCarouselData(dataList, mPosition);
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnOnlineResults: name- " + parentCarouselData.name);
                    addCarouselData(dataList, mPosition);
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {
                    addCarouselData(null, mPosition);
                }
            });
            return null;
        }

        protected void onPreExecute() {
            // Perform setup - runs on user interface thread
        }

        protected void onPostExecute(Void result) {
            mProgressBar.setVisibility(View.GONE);
            // Update user interface
        }
    }

    private void addCarouselData(final List<CardData> carouselList, final int position) {
//        if (carouselList == null || mListCarouselInfo == null || position >= mListCarouselInfo.size()) {
//            return;
//        }
        if (carouselList != null && carouselList.size() == 0) {
            notifyItemRemoved(parentCarouselData);
        } else {
            try {
                CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
                carouselInfoData.listCarouselData = carouselList;
                // carouselInfoData.trackingId = mTrackingId;
                notifyItemChanged();
            } catch (IllegalStateException e) {
                //Occurs while we try to modify data of recycler view white it is scrolling
                mRecyclerViewCarouselInfo.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged();
                    }
                });
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final List<CardData> mDummyCarouselData = new ArrayList<>();

    private List<CardData> getDummyCarouselData() {
        if (!mDummyCarouselData.isEmpty()) {
            return mDummyCarouselData;
        }
        for (int i = 0; i < 10; i++) {
            mDummyCarouselData.add(new CardData());
        }
        return mDummyCarouselData;
    }

    private void showDetailsFragment(CardData carouselData, CarouselInfoData parentCarouselData) {
        //ScopedBus.getInstance().post(new ContentDetailEvent(carouselData, parentCarouselData));

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
//        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carousalTitle);
        if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
           /* if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }*/
        }
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
//        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void showRelatedVODListFragment(CardData cardData, int parentPosition) {
        Bundle args = new Bundle();

        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);

        if (parentPosition >= 0) {
            if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
                parentCarouselData = mListCarouselInfo.get(parentPosition);
                if (parentCarouselData != null) {
                    args.putSerializable(FragmentCarouselViewAll.PARAM_CAROUSEL_DATA, parentCarouselData);
                }
            }
        }

        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));
    }


    long autoScrollTiming = 3000;
    public static boolean isStarted = false;
    private int selectedPosition = 0;
    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            selectedPosition++;
            if (parentCarouselData != null && parentCarouselData.listCarouselData != null && parentCarouselData.listCarouselData.size() > 0  &&selectedPosition >= parentCarouselData.listCarouselData.size()) {
                selectedPosition = 0;
                holder.mViewPager3D.scrollToPosition(selectedPosition);//if smoothscroll is called its scrolling reverse all to the position 0
            } else {
                holder.mViewPager3D.smoothScrollToPosition(selectedPosition);
            }
            //Log.d("DEBUG", " select" + selectedPosition + " size " + parentCarouselData.listCarouselData.size() + " holder.mViewPager2.getChildCount() " + holder.mViewPager2.getChildCount());
        }
    };


    public void startScroll() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        //Log.d("DEBUG", " startScroll");
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollTiming);
//        autoScrollRunnable.run();
    }

    public void stopScroll() {
        //Log.d("DEBUG", " stopScroll");
        isStarted = false;
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

}
