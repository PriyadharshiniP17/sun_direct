package com.myplex.myplex.ui.views;

import static com.myplex.myplex.utils.Util.getDummyCardData;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.SimilarContentRequest;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardResponseData;
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
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.OnItemRemovedListener;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WrapperLinearLayoutManager;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.StringManager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by ramaraju on 07/03/2019.
 */


public class BigHorizontalItem extends GenericListViewCompoment {


    /*private final String fromPremiumPage;*/
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
    private  boolean isFromSearch;
    private AdapterBigHorizontalCarousel adapterBigHorizontalCarousel = null;
    private String mBgcolor, isFromApp, globalServiceId;
    private Typeface mBoldTypeFace;

    public BigHorizontalItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInfo,
                             String mMenuGroup, String mPageTitle,String mBgcolor, boolean isFromSearch, String isFromApp, String globalServiceId) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
      /*  int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int)mContext.getResources().getDimension(R.dimen.thumbnail_gap),
                mContext.getResources().getDisplayMetrics());*/
        int margin = (int) mContext.getResources().getDimension(R.dimen.thumbnail_gap);
        mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
//        mHorizontalMoviesDivieder = new HorizontalItemDecorator(25 ,margin);
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        this.mBgcolor=mBgcolor;
        this.isFromSearch = isFromSearch;
        this.isFromApp = isFromApp;
        this.globalServiceId = globalServiceId;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");

        /*
        this.fromPremiumPage = fromPremiumPage;*/
    }

    public static BigHorizontalItem createView(Context context, ViewGroup parent,
                                               List<CarouselInfoData> carouselInfoData, RecyclerView mRecyclerViewCarouselInfo,
                                               String mMenuGroup, String mPageTitle, String mBgcolor, boolean isFromSearch, String isFromApp, String globalServiceId) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_carousel_linear_recycler, parent, false);
        return new BigHorizontalItem(context, view, carouselInfoData, mRecyclerViewCarouselInfo, mMenuGroup, mPageTitle,mBgcolor,isFromSearch,isFromApp,globalServiceId);
    }


    @Override
    public void bindItemViewHolder(int position) {
        this.position = position;

        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        CarouselInfoData carouselInfoData = null;
        if (mListCarouselInfo != null
                && position < mListCarouselInfo.size()) {
            carouselInfoData = mListCarouselInfo.get(position);
        }
        /* final CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);*/
        if (carouselInfoData == null)  {
            return;
        }
        if(mListCarouselInfo.size()-1 == position) {
            // LoggerD.debugLog("position..." + position);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, (int)mContext.getResources().getDimension(R.dimen.margin_gap_64));
            if(parentLayout != null) {
                holder.parentLayout.setLayoutParams(params);
            }
        }

        holder.mLayoutViewAll.setTag(carouselInfoData);
        holder.mLayoutViewAll.setOnClickListener(mViewAllClickListener);

        holder.mLayoutViewAll.setVisibility(View.VISIBLE);
        holder.mImageViewAll.setVisibility(View.VISIBLE);
        if (carouselInfoData.enableShowAll) {
            holder.mLayoutCarouselTitle.setTag(carouselInfoData);
//            holder.mLayoutCarouselTitle.setOnClickListener(mViewAllClickListener);
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
        } else
            holder.mLayoutViewAll.setVisibility(View.GONE);
        holder.mTextViewViewAll.setVisibility(View.VISIBLE);
        holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
        if (carouselInfoData!=null&&carouselInfoData.showTitle) {
            if (carouselInfoData.title != null) {
                mTextViewGenreMovieTitle.setText(carouselInfoData.title);
                mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
            } else {
                mTextViewGenreMovieTitle.setVisibility(View.GONE);
            }
        }
        if(globalServiceId!=null){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.setMargins((int) mContext.getResources().getDimension(R.dimen.margin_gap_12), (int) mContext.getResources().getDimension(R.dimen.title_image_gap), 0, 0);
            params.addRule(RelativeLayout.BELOW, packs_description_div.getId());
            holder.mRecyclerViewCarousel.setLayoutParams(params);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins((int) mContext.getResources().getDimension(R.dimen.margin_gap_16), 0, 0, 0);
            holder.mLayoutCarouselTitle.setLayoutParams(params1);
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

        /*if(isFromSearch){
            holder.mLayoutCarouselTitle.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.mTextViewGenreMovieTitle.setTextColor(mContext.getResources().getColor(R.color.red4));
            holder.mTextViewGenreMovieTitleOtherLang.setTextColor(mContext.getResources().getColor(R.color.red4));
            holder.mTextViewGenreMovieTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            holder.mTextViewGenreMovieTitleOtherLang.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }*/
       /* if(isFromSearch) {
            holder.mLayoutViewAll.setVisibility(View.GONE);
        } else*/
//            holder.mLayoutViewAll.setVisibility(View.VISIBLE);

        //holder.mLayoutCarouselTitle.setBackgroundColor(UiUtil.getColor(mContext, R.color.app_theme_color));
        if (mBgcolor != null && !TextUtils.isEmpty(mBgcolor)) {
            try {
                //holder.mLayoutCarouselTitle.setBackgroundColor(Color.parseColor(mBgcolor.bgColor));
                holder.mTextViewGenreMovieTitle.setTextColor(mContext.getResources().getColor(R.color.black));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String imageLink = carouselInfoData.getLogoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        holder.mChannelImageView.setVisibility(View.GONE);
        if (TextUtils.isEmpty(imageLink) || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.mChannelImageView.setVisibility(View.GONE);
        } else {
            holder.mChannelImageView.setVisibility(View.VISIBLE);
            PicassoUtil.with(mContext).load(imageLink,holder.mChannelImageView);
            if (imageLink.contains(APIConstants.PARAM_SCALE_WRAP)) {
                holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                if (DeviceUtils.isTablet(mContext)) {
                    holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    holder.mChannelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_48);
                }
            }
//            if (DeviceUtils.isTablet(mContext)) {
//                holder.mChannelImageView.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_36);
//            }
        }


        if (carouselInfoData.listCarouselData == null) {
            carouselInfoData.listCarouselData = getDummyCardData();
        }
        if (holder.mRecyclerViewCarousel.getTag() instanceof AdapterBigHorizontalCarousel) {
            adapterBigHorizontalCarousel = (AdapterBigHorizontalCarousel) holder.mRecyclerViewCarousel.getTag();
            adapterBigHorizontalCarousel.setData(carouselInfoData.listCarouselData);
            adapterBigHorizontalCarousel.setParentPosition(position);
            //   holder.mRecyclerViewCarousel.setListCardData(carouselInfoData.listCarouselData);
        } else {
            adapterBigHorizontalCarousel = new AdapterBigHorizontalCarousel(mContext, carouselInfoData.listCarouselData,
                    holder.mRecyclerViewCarousel,mPageTitle, this.isFromApp);
            holder.mRecyclerViewCarousel.setItemAnimator(null);
            holder.mRecyclerViewCarousel.removeItemDecoration(mHorizontalMoviesDivieder);
            holder.mRecyclerViewCarousel.addItemDecoration(mHorizontalMoviesDivieder);
            holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
            adapterBigHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
            adapterBigHorizontalCarousel.setParentPosition(position);
            adapterBigHorizontalCarousel.setCarouselInfoData(carouselInfoData.name, carouselInfoData.pageSize);
            holder.mRecyclerViewCarousel.setTag(adapterBigHorizontalCarousel);
            //  holder.mRecyclerViewCarousel.setListCardData(carouselInfoData.listCarouselData);
            holder.mRecyclerViewCarousel.setItemViewCacheSize(3);
        }

        WrapperLinearLayoutManager linearLayoutManager = new WrapperLinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false);
        linearLayoutManager.setItemPrefetchEnabled(false);
        holder.mRecyclerViewCarousel.setLayoutManager(linearLayoutManager);

        adapterBigHorizontalCarousel.showTitle(false);
        holder.mRecyclerViewCarousel.setAdapter(adapterBigHorizontalCarousel);
        adapterBigHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
        // adapterBigHorizontalCarousel.setLayoutType(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM);

        adapterBigHorizontalCarousel.isContinueWatchingSection(carouselInfoData.layoutType != null
                && APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType));

        if (TextUtils.isEmpty(carouselInfoData.name)) {
            return;
        }
        switch (carouselInfoData.requestState) {
            case NOT_LOADED:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                //for search results no need to call carousel API specifically
                if(!isFromSearch) {
                    startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
                }
                break;
            case IN_PROGRESS:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                if (carouselInfoData != null
                        && (carouselInfoData.listCarouselData == null
                        || carouselInfoData.listCarouselData.isEmpty())) {
                    removeItemFromParent(carouselInfoData, position);
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
                    removeItemFromParent(carouselInfoData, position);
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
            boolean isCacheRequest = false;
            //Log.d(TAG, "similar name- " + carouselInfoData.name);
            //Log.d(TAG, "similar title- " + carouselInfoData.title);
            if(carouselInfoData != null && carouselInfoData.name != null && carouselInfoData.name.equalsIgnoreCase("similarContents")) {
                //Log.d(TAG, "fetch...: name- " + carouselInfoData.name);
                fetchSimilarContent(carouselInfoData);
            } else {
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
            }
            return null;
        }

    }

    private void fetchSimilarContent(CarouselInfoData carouselInfoData) {
        if(globalServiceId != null) {
            String contentId = globalServiceId;
            SimilarContentRequest.Params contentListparams = new SimilarContentRequest.Params(contentId, APIConstants.LEVEL_DEVICE_MAX, 1, APIConstants.PAGE_INDEX_COUNT);

            SimilarContentRequest mRequestContentList = new SimilarContentRequest(contentListparams,
                    new APICallback<CardResponseData>() {
                        @Override
                        public void onResponse(APIResponse<CardResponseData> response) {
                            if (response != null && response.body() != null && response.body().results != null && !response.body().results.isEmpty()) {
                                addCarouselData(response.body().results, carouselInfoData);
                            } else
                                addCarouselData(null, carouselInfoData);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            addCarouselData(null, carouselInfoData);
                        }
                    });
            APIService.getInstance().execute(mRequestContentList);
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
        if(holder.mRecyclerViewCarousel == null){
            return;
        }
        if (carouselList == null || carouselList.size() == 0) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
            removeItemFromParent(carouselInfoData,position);
        } else {
            LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
            carouselInfoData.requestState = RequestState.SUCCESS;
            //carouselList.add(0,getDummyCard());
            carouselInfoData.listCarouselData = carouselList;
            adapterBigHorizontalCarousel.setData(carouselInfoData.listCarouselData);
            // holder.mRecyclerViewCarousel.setListCardData(carouselInfoData.listCarouselData);
            /*if (!holder.mRecyclerViewCarousel.isComputingLayout()){
                adapterBigHorizontalCarousel.notifyDataSetChanged();
            }*/

            holder.mRecyclerViewCarousel.post(new Runnable()
            {
                @Override
                public void run() {
                    try {
                        if (!holder.mRecyclerViewCarousel.isComputingLayout()){
                            adapterBigHorizontalCarousel.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
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
            removeItemFromParent(carouselInfoData, mParentPosition);
        }
    };

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

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
          /*  if (carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                showRelatedVODListFragment(carouselData,new CarouselInfoData(), parentPosition);
                return;
            }*/
            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;


            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                int contentPosition = position;
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, carouselInfoData, null);
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
            /*if (carouselData != null
                    && carouselData.generalInfo != null
                    && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type) || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }*/


            if(carouselData.isTVSeries())
                fetchTVSeasons(carouselData, carouselInfoData,parentPosition,position);
            else {
                if (carouselInfoData == null) {
                    showDetailsFragment("", carouselData, position, parentPosition,position);
                } else {
                    showDetailsFragment("", carouselData, carouselInfoData,parentPosition,position);
                }
            }

        }

    };

    CacheManager cacheManager = new CacheManager();
    private void fetchTVSeasons(CardData carouselData, CarouselInfoData carouselInfoData,int parentPosition,int position) {

        cacheManager.getRelatedVODListTypeExclusion(carouselData._id, 1, false, APIConstants.TYPE_TVSEASON,
                10,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            return;
                        }
                        fetchFirstEpisode(carouselData._id, dataList.get(dataList.size()-1), carouselInfoData,parentPosition,position);
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            return;
                        }
                        fetchFirstEpisode(carouselData._id, dataList.get(dataList.size()-1), carouselInfoData,parentPosition,position);
                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        fetchFirstEpisode(carouselData._id, carouselData, carouselInfoData,parentPosition,position);
                    }
                });
    }


    private void fetchFirstEpisode(String seasonListId, CardData carouselData, CarouselInfoData carouselInfoData,int parentPosition,int position){
        cacheManager.getRelatedVODList(carouselData._id, 1, false, 1,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            // showNoDataMessage();
                            return;
                        }
                        if (carouselInfoData == null) {
                            showDetailsFragment(seasonListId, dataList.get(0), position, parentPosition,position);
                        } else {
                            showDetailsFragment(seasonListId, dataList.get(0), carouselInfoData,parentPosition,position);
                        }
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            // showNoDataMessage();

                            return;
                        }
                        if (carouselInfoData == null) {
                            showDetailsFragment(seasonListId, dataList.get(0), position, parentPosition,position);
                        } else {
                            showDetailsFragment(seasonListId, dataList.get(0), carouselInfoData,parentPosition,position);
                        }
                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        if (carouselInfoData == null) {
                            showDetailsFragment(seasonListId, carouselData, position, parentPosition,position);
                        } else {
                            showDetailsFragment(seasonListId, carouselData, carouselInfoData,parentPosition,position);
                        }
                    }
                });
    }


    private void showDetailsFragment(String seasonGlobalServiceId, CardData carouselData, CarouselInfoData carouselInfoData, int parentPosition,int contentPosition) {

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

        String partnerId = carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        args.putInt(CleverTap.PROPERTY_CONTENT_POSITION, contentPosition);
        args.putString(CardDetails.PARAM_SEASON_GLOBAL_SERVICE_ID, seasonGlobalServiceId);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void showDetailsFragment(String seasonGlobalServiceId, CardData carouselData, int position, int parentPosition,int contentPosition) {

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
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, "");
        if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        args.putInt(CleverTap.PROPERTY_CONTENT_POSITION, contentPosition);
        args.putString(CardDetails.PARAM_SEASON_GLOBAL_SERVICE_ID, seasonGlobalServiceId);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
           // ((MainActivity) mContext).hideSystemUI();
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
                  //  showRelatedVODListFragment(new CardData(),carouselData,carouselPosition);
                    showVODListFragment(carouselData,carouselPosition);
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
                        && carouselData.showAllLayoutType != null && carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
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
    private void showRelatedVODListFragment(CardData cardData,CarouselInfoData parentCarouselInfoData, int carouselPosition) {
        //TODO show RelatedVodListFragment from main activity context

        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        if (carouselPosition >= 0) {
            if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
                CarouselInfoData  carouselInfoData= mListCarouselInfo.get(carouselPosition);
                CacheManager.setCarouselInfoData(carouselInfoData);

            }
        }
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));


    }

  /*  private void showVODListFragment(CarouselInfoData carouselInfoData, int carouselPositoin) {
        //TODO show VODListFragment from MainActivity with bundle

        MenuDataModel.setSelectedMoviesCarouselList(carouselInfoData.name + "_" + 1, carouselInfoData.listCarouselData);
        Bundle args = new Bundle();
        CacheManager.setCarouselInfoData(carouselInfoData);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPositoin);
        args.putBoolean(ApplicationController.PARTNER_LOGO_BOTTOM_VISIBILTY, (!APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM.equalsIgnoreCase(carouselInfoData.layoutType))
                || (APIConstants.LAYOUT_TYPE_BROWSE_GRID.equalsIgnoreCase(carouselInfoData.showAllLayoutType)));
        if (!APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselInfoData.showAllLayoutType)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_CAROUSEL_GRID, true);
        }

        if (!TextUtils.isEmpty(mMenuGroup)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mMenuGroup);
        }
        ((MainActivity) mContext).pushFragment(FragmentVODList.newInstance(args));
    }*/
    private View.OnClickListener mRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
            carouselInfoData.requestState = RequestState.IN_PROGRESS;
            startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
        }
    };

    private void removeItemFromParent(final CarouselInfoData carouselInfoData, final int mParentPosition) {
        if(mRecyclerViewCarouselInfo == null){
            return;
        }
        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        mRecyclerViewCarouselInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!mRecyclerViewCarouselInfo.isComputingLayout())
                    notifyItemRemoved(carouselInfoData);
                else
                    removeItemFromParent(carouselInfoData,mParentPosition);
            }
        },500);
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
