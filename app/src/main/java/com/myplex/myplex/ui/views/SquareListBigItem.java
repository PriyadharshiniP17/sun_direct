package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.InlineSearch;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.SearchFilterResponse;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.adapter.AdapterSquareItemBig;
import com.myplex.myplex.ui.fragment.ArtistProfileFragment;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;


/**
 * Created by apalya on 4/22/2017.
 */

public class SquareListBigItem extends GenericListViewCompoment {

    private final List<SearchFilterResponse> searchSuggestionsFilters;
    private boolean isFromSearch = false;
    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = SquareListBigItem.class.getSimpleName();
    private final RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private  RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselData;
    private String searchQuery;
    private String searchKey;
    private String searchFields;
    private String publishingHouseId;
    private String firstFilterSelectedValue;
    private String secondFilterSelectedValue;
    private String thirdFilterSelectedValue;
    private static final int PARAM_PAGE_COUNT = 10;
    private int mStartIndex = 1;
    private InlineSearch inlineSearchRequest;
    //private SearchFilterData searchFilterData;
    private Typeface mBoldTypeFace;
    private SquareListBigItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInfo,
                              String mSearchQuery,  List<SearchFilterResponse> searchSuggestionsFilters,String publishingHouseId,
                              String mFirstFilterSelectedValue,
                              String mSecondFilterSelectedValue,
                              String mThirdFilterSelectedValue,
                              boolean isFromSearch) {
        super(view);
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_2));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        this.searchQuery = mSearchQuery;
        this.searchSuggestionsFilters = searchSuggestionsFilters;
        this.publishingHouseId = publishingHouseId;
        this.firstFilterSelectedValue = mFirstFilterSelectedValue;
        this.secondFilterSelectedValue = mSecondFilterSelectedValue;
        this.thirdFilterSelectedValue = mThirdFilterSelectedValue;
        this.isFromSearch = isFromSearch;
        /*this.searchFilterData = searchFilterData;*/
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");


    }

    public static SquareListBigItem createView(Context context, ViewGroup parent,
                                               List<CarouselInfoData> carouselInfoData, RecyclerView mRecyclerViewCarouselInfo, boolean isFragmentFromSearch,
                                               String searchQuery,
                                               List<SearchFilterResponse> searchSuggestionsFilters,
                                               String publishingHouseId,
                                               String mFirstFilterSelectedValue,
                                               String mSecondFilterSelectedValue,
                                               String mThirdFilterSelectedValue) {

        View view = LayoutInflater.from(context).inflate(R.layout.listitem_carousel_linear_recycler, parent, false);
        return new SquareListBigItem(context, view, carouselInfoData, mRecyclerViewCarouselInfo, searchQuery,
                searchSuggestionsFilters, publishingHouseId,mFirstFilterSelectedValue, mSecondFilterSelectedValue, mThirdFilterSelectedValue, isFragmentFromSearch);
    }

    CarouselInfoData carouselInfoData;
    @Override
    public void bindItemViewHolder(int position) {

        this.position = position;
        GenericListViewCompoment holder = this;

        //Log.d(TAG, "bindSmallHorizontalItemViewHolder");

        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        if (mListCarouselInfo.size() > position) {
            carouselInfoData = mListCarouselInfo.get(position);
        }


        if (carouselInfoData == null) {
            return;
        }

        if(isFromSearch){
            searchKey = fetchSearchKey(searchSuggestionsFilters,carouselInfoData.title);
            searchFields = fetchSearchFields(searchSuggestionsFilters,carouselInfoData.title);
        }

//        holder.mTextViewGenreMovieTitle.setText(carouselInfoData.title == null ? "" : carouselInfoData.title);

       /* if (carouselInfoData.title != null && !carouselInfoData.title.isEmpty())
            corouselTitleChangeForSearch(mContext, isFromSearch, holder.mTextViewGenreMovieTitle, holder.mTitleImage, carouselInfoData.title, carouselInfoData, holder.titleLayout);
*/        //holder.mLayoutViewAll.setTag(carouselInfoData);
            if(holder.mChannelImageView != null){
                holder.mChannelImageView.setVisibility(GONE);
            }
        if (carouselInfoData!=null&&carouselInfoData.showTitle) {
            if (carouselInfoData.title != null) {
                mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            } else {
                mTextViewGenreMovieTitle.setVisibility(View.GONE);
            }
        }
            holder.mLayoutViewAll.setVisibility(GONE);
        /*if(!TextUtils.isEmpty(carouselInfoData.bgColor)){
            try {
                holder.mLayoutCarouselTitle.setBackgroundColor(Color.parseColor(carouselInfoData.bgColor));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }*/

        if(isFromSearch){
            if(carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()){
                notifyItemRemoved(carouselInfoData);
                return;
            }
        }
        AdapterSquareItemBig squareItemBig;
        if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
            squareItemBig = new AdapterSquareItemBig(mContext, getDummyCarouselData(), true, carouselInfoData.enableShowAll, isFromSearch);
            final RecyclerView.LayoutManager layoutManager;
            layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            layoutManager.setItemPrefetchEnabled(false);
            holder.mRecyclerViewCarousel.addItemDecoration(mHorizontalDividerDecoration);
            holder.mRecyclerViewCarousel.setItemAnimator(null);
            holder.mRecyclerViewCarousel.setLayoutManager(layoutManager);
            holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
            squareItemBig.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
            holder.mRecyclerViewCarousel.setTag(squareItemBig);
            squareItemBig.setParentPosition(position);
            if (carouselInfoData != null && !TextUtils.isEmpty(carouselInfoData.name)) {
                startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData.name, carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT, position,carouselInfoData.modified_on));
            }
        } else {
            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false);
            layoutManager1.setItemPrefetchEnabled(false);
            if (holder.mRecyclerViewCarousel.getTag() instanceof AdapterBigHorizontalCarousel) {
                squareItemBig = (AdapterSquareItemBig) holder.mRecyclerViewCarousel.getTag();
                List<CardData> movies = new ArrayList<>();
                if(carouselInfoData.listCarouselData.size()<6){
                    for(int i=0;i<4;i++){
                        movies.add(carouselInfoData.listCarouselData.get(i));
                    }
                }else{
                    for(int i=0;i<6;i++){
                        movies.add(carouselInfoData.listCarouselData.get(i));
                    }
                }

                if(squareItemBig.isContainingDummies()){
                    squareItemBig.setData(movies);
                }else{
                    squareItemBig.setData(movies);
                }
            } else {
                holder.mRecyclerViewCarousel.setItemAnimator(null);
                holder.mRecyclerViewCarousel.setLayoutManager(layoutManager1);
                holder.mRecyclerViewCarousel.removeItemDecoration(mHorizontalDividerDecoration);
                holder.mRecyclerViewCarousel.addItemDecoration(mHorizontalDividerDecoration);
                holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
                squareItemBig = new AdapterSquareItemBig(mContext, carouselInfoData.listCarouselData, false, carouselInfoData.enableShowAll, isFromSearch);
                squareItemBig.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
                holder.mRecyclerViewCarousel.setTag(squareItemBig);
                squareItemBig.setParentPosition(position);
            }
        }
        holder.mRecyclerViewCarousel.setAdapter(squareItemBig);
        /*holder.tv_search_view_all.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (carouselInfoData != null && carouselInfoData.enableShowAll) {

                    Bundle args = new Bundle();
                    if (carouselInfoData != null) {
                        args.putSerializable(FragmentCarouselViewAll.PARAM_CAROUSEL_DATA, carouselInfoData);
                    }
                   *//* Intent intent = new Intent(mContext, ViewAllActivity.class);
                    intent.putExtra("carouselInfo", args);
                    intent.putExtra(APIConstants.PUBLISHING_HOUSE_ID, publishingHouseId);
                    intent.putExtra(APIConstants.COMING_FROM_SEARCH, true);
                    //Log.d(TAG, "view all search key :" + searchKey + " search fields :" + searchFields);
                    if (searchQuery != null) {
                        intent.putExtra(APIConstants.SEARCH_QUERY, searchQuery);
                    }
                    if (searchKey != null) {
                        intent.putExtra(APIConstants.SEARCH_CONTENT_TYPE, searchKey);
                    }
                    intent.putExtra(SearchFragmentCarouselInfo.SEARCH_FILTER_DATA, searchFilterData);
                    mContext.startActivity(intent);*//*
                }
            }
        });*/
    }

    private String getImageLink(List<CardDataImagesItem> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    private void startAsyncTaskInParallel(CarouselRequestTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private class CarouselRequestTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
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
            if(DeviceUtils.isTablet(mContext)){
                imageType = ApplicationConfig.XHDPI;
            }else{
                imageType = ApplicationConfig.MDPI;
            }
            if (isFromSearch && searchQuery != null && !searchQuery.isEmpty() && searchKey != null && !searchKey.isEmpty()
                    && searchFields != null && !searchFields.isEmpty()) {

                InlineSearch.Params inlineSearchParams = new InlineSearch.Params(searchQuery, searchKey,
                        "dynamic", mCount, mStartIndex,searchFields);

                inlineSearchRequest = new InlineSearch(inlineSearchParams, new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {

                        if (!isCancelled()) {
                            if (response == null || response.body() == null) {

                                return;
                            }

                            if (response.body().results != null /*&& response.body().results.size() > 0*/)
                                addCarouselData(response.body().results, position);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
//                mIsLoadingMorePages = false;
                        if (!isCancelled()) {
                            if (t != null) {
                                //Log.d(TAG, "" + t.getMessage());
                            }
                        }
                    }
                });
                APIService.getInstance().execute(inlineSearchRequest);
            } else {
                new MenuDataModel().fetchCarouseldata(mContext, mPageName, 1, mCount, isCacheRequest, imageType,  new MenuDataModel.CarouselContentListCallback() {
                    @Override
                    public void onCacheResults(List<CardData> dataList) {
                        //Log.d(TAG, "OnCacheResults: name- " + mPageName);

                    if (dataList != null && !dataList.isEmpty()) {
                        addCarouselData(dataList, mPosition);
                    }
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnOnlineResults: name- " + mPageName);

                    if (dataList != null && !dataList.isEmpty()) {
                        addCarouselData(dataList, mPosition);
                    }
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {

                    }
                });
            }
            return null;
        }

        protected void onPreExecute() {
            // Perform setup - runs on user interface thread
        }

        protected void onPostExecute(Void result) {
            // Update user interface
        }
    }
    private List<CardData> cards ;
    private void addCarouselData(final List<CardData> carouselList, final int position) {
        cards = carouselList;
//        if (carouselList == null || mListCarouselInfo == null || position >= mListCarouselInfo.size()) {
//            return;
//        }
        if (carouselList != null && carouselList.size() == 0) {
            notifyItemRemoved(carouselInfoData);
        } else {
            try {
                carouselInfoData = mListCarouselInfo.get(position);
                if (carouselInfoData.listCarouselData == null) {
                    carouselInfoData.listCarouselData = carouselList;
                }
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
        for (int i = 0; i < 5; i++) {
            mDummyCarouselData.add(new CardData());
        }
        return mDummyCarouselData;
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;
            //Log.d(TAG, "square list big item single click listener");
            if (isFromSearch) {
                //creatSearchEventandSearchString(carouselData,searchQuery);
                if (position < carouselInfoData.pageSize - 1) {
                    parentCarouselData = carouselInfoData;
                    showDetailsFragment(carouselData, parentPosition);
                } else if (carouselInfoData.enableShowAll && mLayoutViewAll.getVisibility() == View.VISIBLE) {
                    Bundle args = new Bundle();
                    if (carouselInfoData != null) {
                        args.putSerializable(FragmentCarouselViewAll.PARAM_CAROUSEL_DATA, carouselInfoData);
                    }
                   /* Intent intent = new Intent(mContext, ViewAllActivity.class);
                    intenhttps://github.com/ApalyaTechnologies/sunott_android/pull/1532t.putExtra("carouselInfo", args);
                    mContext.startActivity(intent);*/
                } else {
                    parentCarouselData = carouselInfoData;
                    showDetailsFragment(carouselData, parentPosition);
                }
            } else {
                Bundle args = new Bundle();
                String language = null;
                if (carouselData.content.language != null && carouselData.content.language.size() > 0) {
                    language = carouselData.content.language.get(0);
                }
                args.putString("TYPE",carouselData.generalInfo.type);
                args.putString("ID",carouselData.generalInfo._id);
                args.putString("NAME", carouselData.generalInfo.title);
                args.putString("DESCRIPTION", carouselData.generalInfo.type);
                args.putString("FULL_DESCRIPTION", carouselData.generalInfo.description);
                args.putSerializable("CARD_DATA",carouselData);
                if (DeviceUtils.isTablet(mContext)) {
                    args.putString("IMAGE_URL", Util.getSquareImageLink(carouselData, true));
                } else {
                    args.putString("IMAGE_URL", Util.getSquareImageLink(carouselData, false));
                }

                ((BaseActivity) mContext).pushFragment(ArtistProfileFragment.newInstance(args));
            }
        }

    };

    private String fetchSearchKey(List<SearchFilterResponse> searchSuggetionsFilters, String title){
        //Log.d(TAG, "fetchsearchKeyandFields position :" + position);
        if (searchSuggetionsFilters != null && searchSuggetionsFilters.size() > 0) {
            for (int i = 0; i < searchSuggetionsFilters.size(); i++) {
                if (searchSuggetionsFilters.get(i).displayName.equalsIgnoreCase(title))
                    return searchSuggetionsFilters.get(i).key;
            }
        }
        return "";
    }

    private String fetchSearchFields(List<SearchFilterResponse> searchSuggetionsFilters,String title){
        //Log.d(TAG, "fetchsearchKeyandFields position :" + position);
        if (searchSuggetionsFilters != null && searchSuggetionsFilters.size() > 0) {
            for (int i = 0; i < searchSuggetionsFilters.size(); i++) {
                if (searchSuggetionsFilters.get(i).displayName.equalsIgnoreCase(title))
                    return searchSuggetionsFilters.get(i).searchFields;
            }
        }
        return "";
    }

    private void showDetailsFragment(CardData carouselData, int parentPosition) {

        if (parentPosition >= 0) {
            if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
                parentCarouselData = mListCarouselInfo.get(parentPosition);
            }
        }
      /*  carouselData.carousalName = carouselInfoData.name;
        carouselData.comedy_startIndex = 1;
        carouselData.comedy_mCount = carouselInfoData.pageSize;
        carouselData.comedy_modifiedOn = carouselInfoData.modified_on;
        if (DeviceUtils.isTablet(mContext)) {
            carouselData.comedy_imageType = "hdpi";
        } else {
            carouselData.comedy_imageType = "mdpi";
        }
        ScopedBus.getInstance().post(new ContentDetailEvent(carouselData, parentCarouselData));*/

    }
}

