package com.myplex.myplex.ui.views;

import static android.view.View.GONE;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.chip.Chip;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ChannelsEPGResponseData;
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
import com.myplex.myplex.ui.adapter.AdapterCatchupCarousel;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.OnItemRemovedListener;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

public class TextFlowLayoutItem extends GenericListViewCompoment {
    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = BigHorizontalItem.class.getSimpleName();
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselData;
    private View view;
    private CarouselInfoData carouselInfoData;
    private final RecyclerView.ItemDecoration mHorizontalMoviesDivieder;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private final RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    AdapterCatchupCarousel adapterSmallHorizontalCarousel = null;
    String mBgColor;
    private boolean isFromSearch;
    private String globalServiceId;
    private AdapterTextFlowLayout adapterTextFlowLayout;
    private List<CardData> textList;
    private Typeface mBoldTypeFace;

    public TextFlowLayoutItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInfo
            , String mMenuGroup, String mPageTitle, String mBgColor, boolean isFromSearch, String globalServiceId, List<CardData> textList) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        /*int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) mContext
                .getResources().getDimension(R.dimen.thumbnail_gap), mContext.getResources().getDisplayMetrics());*/
        int margin = (int) mContext.getResources().getDimension(R.dimen.thumbnail_gap);
        mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        this.mBgColor = mBgColor;
        this.isFromSearch = isFromSearch;
        this.globalServiceId = globalServiceId;
        this.textList=textList;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");

    }
    public static TextFlowLayoutItem createView(Context context, ViewGroup parent,
                                                List<CarouselInfoData> carouselInfoData, RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup,
                                                String mPageTitle, String mBgColor, boolean isFromSearch, String globalServiceId, List<CardData> textList) {
        View view = LayoutInflater.from(context).inflate(R.layout.text_flow_layout, parent, false);
        return new TextFlowLayoutItem(context, view, carouselInfoData, mRecyclerViewCarouselInfo, mMenuGroup,  mPageTitle,mBgColor, isFromSearch, globalServiceId,textList);
    }


    @Override
    public void bindItemViewHolder(int position) {

        this.position = position;
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        CarouselInfoData carouselInfoData = null;
        if(position < mListCarouselInfo.size())
            carouselInfoData = mListCarouselInfo.get(position);
        if (carouselInfoData == null) {
            return;
        }


        if (mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
            holder.mTextViewGenreMovieTitleOtherLang.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            holder.mTextViewGenreMovieTitle.setTextColor(mContext.getResources().getColor(R.color.carousel_heading_text_color));
        }

        //holder.mLayoutCarouselTitle.setBackgroundColor(UiUtil.getColor(mContext, R.color.app_theme_color));
        if (!TextUtils.isEmpty(carouselInfoData.bgColor)) {
            try {
                holder.mLayoutCarouselTitle.setBackgroundColor(Color.parseColor(carouselInfoData.bgColor));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adapterTextFlowLayout = null;
        if (carouselInfoData.listCarouselData == null) {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " listCarouselData is null");
            carouselInfoData.listCarouselData = Util.getDummyCardData();
        }
        if (holder.channelItems.getTag() instanceof AdapterTextFlowLayout) {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " getTag");
            adapterTextFlowLayout = (AdapterTextFlowLayout) holder.channelItems.getTag();
            adapterTextFlowLayout.setData(carouselInfoData.listCarouselData);
        } else {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " create adapter");
            adapterTextFlowLayout = new AdapterTextFlowLayout(mContext, carouselInfoData.listCarouselData, holder.channelItems);
            textList=carouselInfoData.listCarouselData;
            if (carouselInfoData!=null&&carouselInfoData.showTitle) {
                if (carouselInfoData.title != null) {
                    mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                    mTextViewGenreMovieTitle.setText(carouselInfoData.title);
                } else {
                    mTextViewGenreMovieTitle.setVisibility(View.GONE);
                }
            }


        }
        if (carouselInfoData != null)
            adapterTextFlowLayout.showTitle(carouselInfoData.showTitle);
        LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " requestState- " + carouselInfoData.requestState);
        holder.channelItems.setItemAnimator(null);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.setItemPrefetchEnabled(false);
        holder.channelItems.setLayoutManager(linearLayoutManager);
        if (!holder.channelItems.isComputingLayout()) {
            holder.channelItems.removeItemDecoration(mHorizontalMoviesDivieder);
            holder.channelItems.addItemDecoration(mHorizontalMoviesDivieder);

        }
        holder.channelItems.setFocusableInTouchMode(false);
        holder.channelItems.setTag(adapterTextFlowLayout);
        adapterTextFlowLayout.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
        adapterTextFlowLayout.setParentPosition(position);
        adapterTextFlowLayout.setParentViewHolder(holder);
        adapterTextFlowLayout.setBgColor(mBgColor);
        adapterTextFlowLayout.setCarouselInfoData(carouselInfoData.name, carouselInfoData.pageSize);
        adapterTextFlowLayout.isContinueWatchingSection(carouselInfoData.layoutType != null
                && APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType));
        adapterTextFlowLayout.setRemoveItemListener(mOnItemRemovedListener);
//        holder.channelItems.setAdapter(adapterTextFlowLayout);
        if (TextUtils.isEmpty(carouselInfoData.name)) {
            return;
        }
        switch (carouselInfoData.requestState) {
            case NOT_LOADED:
//                holder.channelItems.setVisibility(View.VISIBLE);
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
             //   startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
                break;
            case IN_PROGRESS:
//                holder.channelItems.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                if (carouselInfoData != null
                        && (carouselInfoData.listCarouselData == null
                        || carouselInfoData.listCarouselData.isEmpty())) {
                    removeItemFromParent(carouselInfoData);
                    return;
                }
//                holder.channelItems.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                if (carouselInfoData != null
                        && APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType)) {
                    if (carouselInfoData.listCarouselData != null)
                        carouselInfoData.listCarouselData.clear();
                    removeItemFromParent(carouselInfoData);
                    return;
                }
//                holder.channelItems.setVisibility(View.INVISIBLE);
                break;
            default:
//                holder.channelItems.setVisibility(View.VISIBLE);
                break;
        }
        startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
    }

    private View.OnClickListener mRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
            carouselInfoData.requestState = RequestState.IN_PROGRESS;
            startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
        }
    };
    private void removeItemFromParent(CarouselInfoData carouselInfoData) {
        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        notifyItemRemoved(carouselInfoData);
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
//                    showRelatedVODListFragment(carouselData,carouselPosition);
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)
                        || APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselData.showAllLayoutType)) {
//                    showCarouselViewAllFragment(carouselData,carouselPosition);
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)
                        && APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL.equalsIgnoreCase(carouselData.layoutType)) {
                    Bundle args = new Bundle();
                    args.putSerializable(SmallSquareItemsFragment.PARAM_CAROUSEL_INFO_DATA, carouselData);
                    args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);
                    ((MainActivity) mContext).pushFragment(SmallSquareItemsFragment.newInstance(args));
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)) {
//                    showVODListFragment(carouselData,carouselPosition);
                    return;
                } else if (carouselData != null && carouselData.showAllLayoutType != null && carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
                    try {
                        ((MainActivity) mContext).redirectToPage(carouselData.showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

//                showVODListFragment(carouselData,carouselPosition);
            }
        }
    };
    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;
            if(mListCarouselInfo.size()!=0 && mListCarouselInfo.size()>1) {
                CarouselInfoData carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;

                if (carouselInfoData != null && carouselInfoData.title != null) {
                    gaBrowse(carouselData, carouselInfoData.title);
                    //Log.d(TAG, "carouselSectionName: " + carouselInfoData.title);
                }
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
           /* if (carouselInfoData == null) {
                showDetailsFragment(carouselData, position,"",parentPosition);
            } else {
                showDetailsFragment(carouselData, carouselInfoData,parentPosition);
            }*/

        }

    };
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
        protected Void doInBackground(final Void... params1) {
            boolean isCacheRequest = false;
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

    public void getEPGData(String contentId){
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
        args.putBoolean(CatchupItem.IS_CATCH_UP, true);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
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
    private void addCarouselData(final List<CardData> carouselList, final CarouselInfoData carouselInfoData) {
        if (carouselList == null || carouselList.size() == 0) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
            removeItemFromParent(carouselInfoData);
        } else {
            LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
            carouselInfoData.requestState = RequestState.SUCCESS;
            carouselInfoData.listCarouselData = carouselList;
            adapterTextFlowLayout.setData(carouselInfoData.listCarouselData);
            ((MainActivity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Your code
                    Chip chip;
                    channelChip.removeAllViews();
                    if(carouselList!=null && carouselList.size()>0) {
                        for (int i = 0; i < carouselList.size(); i++) {
                            chip = (Chip) LayoutInflater.from(mContext).inflate(R.layout.text_flow_channel_item, null, false);
//                    chip.setText(textList.get(i).getChannelName());
                            chip.setText(carouselList.get(i).generalInfo.title);
                            channelChip.addView(chip);
                            chip.setTag(carouselList.get(i));
                            chip.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    hideSoftInputKeyBoard(v);
                                    if (v.getTag() instanceof CardData) {
                                        CardData cardData = (CardData) v.getTag();
                                        if(cardData!=null ) {
                                            if (cardData.globalServiceId != null)
                                                getEPGData(cardData.globalServiceId);
                                            else if (cardData._id != null) {
                                                if (cardData.isLive()) {
                                                    getEPGData(cardData._id);
                                                } else {
                                                    showDetailsFragment(cardData);
                                                }
//                                            showDetailsFragment(cardData);
                                                //showDetailsFragment(cardData);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });

        }

    }
    void hideSoftInputKeyBoard(View view) {
        if (view != null && mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}

