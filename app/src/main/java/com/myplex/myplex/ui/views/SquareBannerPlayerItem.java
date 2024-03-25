package com.myplex.myplex.ui.views;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.RequestState;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
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
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.adapter.SquareViewPagerImageSliderAdapter;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.Util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SquareBannerPlayerItem extends GenericListViewCompoment{

    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = SquareBannerPlayerItem.class.getSimpleName();
    private RecyclerView mRecyclerViewCarouselInfo;
    private GenericListViewCompoment holder = this;
    private CarouselInfoData carouselInfoData;
    private AdapterCarouselInfo.CallbackListener callbackListener;
    private String mMenuGroup;
    private String mPageTitle;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    SquareViewPagerImageSliderAdapter imageSliderAdapter = null;
    int height  = (((ApplicationController.getApplicationConfig().screenWidth * 4) / 3));
    private CarouselInfoData parentCarouselInfoData;

    private SquareBannerPlayerItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo,
                                     RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup,
                                     String mPageTitle, CarouselInfoData parentCarouselInfoData) {
        super(view);
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        this.parentCarouselInfoData = parentCarouselInfoData;
    }


    public static SquareBannerPlayerItem createView(Context context, ViewGroup parent,
                                                   List<CarouselInfoData> carouselInfoData,
                                                   RecyclerView mRecyclerViewCarouselInfo,
                                                   String mMenuGroup, String mPageTitle, CarouselInfoData parentCarouselInfoData) {

        View view = LayoutInflater.from(context).inflate(R.layout.square_player_item, parent, false);
        view.getLayoutParams().height = (int) (ApplicationController.getApplicationConfig().screenWidth );
        return new SquareBannerPlayerItem(context, view,
                carouselInfoData, mRecyclerViewCarouselInfo,mMenuGroup,mPageTitle,parentCarouselInfoData);
    }


    @Override
    public void bindItemViewHolder(final int position) {

        this.position = position;
        if (mListCarouselInfo == null) {
            return;
        }
        final CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);

        if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
            //Log.d(TAG, "carouselInfoData.listCarouselData");
            holder.mPreviewLayout.setVisibility(View.VISIBLE);
            holder.mViewPagerContainer.setVisibility(View.GONE);
            holder.mAutoPlayRecyclerViewSquare.setVisibility(View.GONE);
            holder.mOfferDecriptionLayout.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(carouselInfoData.name)
                    && carouselInfoData.requestState == RequestState.NOT_LOADED) {
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
            }
        } else {
            AdapterAutoPlayRecyclerView imageSliderAdapter = null;
            if (holder.mAutoPlayRecyclerViewSquare.getTag() != null
                    && holder.mAutoPlayRecyclerViewSquare.getTag() instanceof AdapterAutoPlayRecyclerView) {
                imageSliderAdapter = (AdapterAutoPlayRecyclerView) holder.mAutoPlayRecyclerViewSquare.getTag();
            } else {
                imageSliderAdapter = new AdapterAutoPlayRecyclerView(mContext);
                if (PrefUtils.getInstance().getWatchlistItemsFromServer()) {
                    if(!PrefUtils.getInstance().getBoolean(APIConstants.UPDATE_PORTRAIT_BANNER,false)){
                        PrefUtils.getInstance().setWatchlistItems(carouselInfoData.listCarouselData,mMenuGroup);
                    }
                }
                List<CardData> items = PrefUtils.getInstance().getWatchListItems(mMenuGroup);
                if (items != null && items.size() > 0) {
                    imageSliderAdapter.setItems(items,carouselInfoData.layoutType);
                } else {
                    PrefUtils.getInstance().setWatchlistItems(carouselInfoData.listCarouselData,mMenuGroup);
                    imageSliderAdapter.setItems(carouselInfoData.listCarouselData,carouselInfoData.layoutType);
                }
                imageSliderAdapter.setTabName(mMenuGroup);
            }
            imageSliderAdapter.setTitleVisibility(carouselInfoData.showTitle);
            imageSliderAdapter.setCarouselPosition(position);
            holder.mAutoPlayRecyclerViewSquare.setTag(imageSliderAdapter);
            imageSliderAdapter.setOnItemClickListener(new AdapterAutoPlayRecyclerView.OnItemClickListener() {
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

                    if (cardData != null
                            && cardData.generalInfo != null
                            && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                            && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                        mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                        return;
                    }
                    showDetailsFragment(cardData, -1, carouselInfoData.title,position);
                }
            });
            CyclicRecyclerViewAdapter cyclicRecyclerViewAdapter = new CyclicRecyclerViewAdapter(imageSliderAdapter);
            holder.mAutoPlayRecyclerViewSquare.setAdapter(cyclicRecyclerViewAdapter);
            holder.mAutoPlayRecyclerViewSquare.setCycle(true);

            int pagerPadding = (int) mContext.getResources().getDimension(R.dimen.margin_gap_8);
            SDKLogger.debug("pagerPadding- " + pagerPadding);
            holder.mAutoPlayRecyclerViewSquare.setClipToPadding(false);
            CustomScrollLinearlayoutManager layoutManager = new CustomScrollLinearlayoutManager(mContext,RecyclerView.HORIZONTAL,false);
            layoutManager.setInitialPrefetchItemCount(8);
            layoutManager.setItemPrefetchEnabled(true);
            holder.mAutoPlayRecyclerViewSquare.setLayoutManager(layoutManager);
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            holder.mAutoPlayRecyclerViewSquare.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(holder.mAutoPlayRecyclerViewSquare);
            holder.mAutoPlayRecyclerViewSquare.setSizeOfChildren(imageSliderAdapter.getItemCount());
            holder.mAutoPlayRecyclerViewSquare.setPageMargin((int) mContext.getResources().getDimension(R.dimen.margin_gap_16));
            SDKLogger.debug("GroupName- " + mMenuGroup + " position- " + position + " screen size width- " + holder.mAutoPlayRecyclerViewSquare.getLayoutParams().width + " height- " + holder.mAutoPlayRecyclerViewSquare.getLayoutParams().height);
            holder.mOfferDecriptionLayout.setVisibility(View.GONE);
            holder.mAutoPlayRecyclerViewSquare.setVisibility(View.VISIBLE);
            holder.mViewPagerContainer.setVisibility(View.VISIBLE);
            holder.mPreviewLayout.setVisibility(View.GONE);
            //holder.mRecyclerViewCircleIndicator.attachToRecyclerView(mAutoPlayRecyclerViewSquare,snapHelper);
            //holder.mAutoPlayRecyclerViewSquare.addItemDecoration(new IndicatorItemDecorator());
            double timer;
            if (PrefUtils.getInstance().getBannerAutoScrollFrequency() != null)
                timer = Integer.parseInt(PrefUtils.getInstance().getBannerAutoScrollFrequency()) * 1.5 * 1000;
            else
                timer = 3000;
            if (carouselInfoData.listCarouselData != null
                    && carouselInfoData.listCarouselData.size() > 1) {
                holder.mAutoPlayRecyclerViewSquare.setCurrentItem(imageSliderAdapter.getItemCount() / 2);
                //holder.mViewPagerRecyclerView.setOffscreenPageLimit(1);
            }
            holder.mAutoPlayRecyclerViewSquare.setAutoScrollDurationFactor(10);
            holder.mAutoPlayRecyclerViewSquare.setInterval(((int) timer));
            holder.mAutoPlayRecyclerViewSquare.setProgressBar(mProgressBar);
            holder.mAutoPlayRecyclerViewSquare.setListCardData(carouselInfoData.listCarouselData);
            SDKLogger.debug("TimeStamp " + Calendar.getInstance().getTime());
            holder.mAutoPlayRecyclerViewSquare.setStopScrollWhenTouch(true);
            if (holder.mAutoPlayRecyclerViewSquare != null) {
                holder.mAutoPlayRecyclerViewSquare.startAutoScroll();
            }
            holder.mLeftArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //holder.mViewPagerRecyclerView.setCurrentItem(holder.mViewPagerRecyclerView.getC - 1);
                }
            });
            holder.mRightArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //  holder.mViewPagerRecyclerView.setCurrentItem(holder.mViewPagerRecyclerView.getCurrentItem() + 1);
                }
            });

            holder.pageIndicatorView.setCount(imageSliderAdapter.getRealCount());
            holder.mAutoPlayRecyclerViewSquare.setPagerIndicator(holder.pageIndicatorView);
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
}
