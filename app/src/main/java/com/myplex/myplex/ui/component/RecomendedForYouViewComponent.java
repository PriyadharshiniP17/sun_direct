package com.myplex.myplex.ui.component;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.model.CardData;
import com.myplex.model.CardDataTagsItem;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.adapter.AdapterMedHorizontalCarousel;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RecomendedForYouViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final RecyclerView mRecyclerViewCarouselInfo;
    private final RelativeLayout mMain_layout;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = SimilarContentViewComponent.class.getSimpleName();
    private String mCarouselLayoutType;
    private String mBgColor;

    public RecomendedForYouViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view,
                                       CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        mRecyclerViewCarouselInfo = view.findViewById(R.id.recyclerview);
        mMain_layout=view.findViewById(R.id.main_layout);
    }

    public static RecomendedForYouViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_carouselinfo,
                parent, false);
        RecomendedForYouViewComponent briefDescriptionComponent = new RecomendedForYouViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        this.mData = mValues.get(position).cardData;
        mBgColor=mValues.get(position).mBgColor;
        mCarouselLayoutType=mValues.get(position).carouselLayoutType;
        if (mValues.get(position).mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
            mMain_layout.setBackgroundColor(Color.parseColor(mBgColor));
        }else {
            mMain_layout.setBackgroundColor(mContext.getResources().getColor(R.color.app_bkg));
        }
        HorizontalItemDecorator mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        mRecyclerViewCarouselInfo.addItemDecoration(mHorizontalDividerDecoration);
        mRecyclerViewCarouselInfo.setItemAnimator(null);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCarouselInfo.setLayoutManager(layoutManager);
        loadData(CardData.DUMMY_LIST);
        fetchSimilarContent();
    }

    private void fetchSimilarContent() {
        if(mData!=null&&mData.generalInfo!=null&&mData.generalInfo.type!=null){
            String tags = "";
            if (mData != null && mData.tags != null && mData.tags.values != null && mData.tags.values.size() > 0) {
                for (CardDataTagsItem tag : mData.tags.values) {
                    if (tags.isEmpty()) {
                        tags = tags + tag.name;
                    } else {
                        if (!TextUtils.isEmpty(tags) && tags.length() < 230){
                            tags = tags + "," + tag.name;
                        }
                    }
                }
            }
            RequestContentList.Params contentListparams = new RequestContentList.Params(mData.generalInfo.type,
                    1, APIConstants.PAGE_INDEX_COUNT, null, getGenres(),"1",
                    PrefUtils.getInstance().getPrefpublisherGroupIds_Android(),tags);
            RequestContentList mRequestContentList = new RequestContentList(contentListparams,
                    new APICallback<CardResponseData>() {
                        @Override
                        public void onResponse(APIResponse<CardResponseData> response) {
                            if (response == null
                                    || response.body() == null) {
                                if (mListener != null) {
                                    mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                                }
                                return;
                            }

                            //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message- " + response.body().message);
                            if (response.body().results == null || response.body().results.isEmpty()) {
                                if (mListener != null) {
                                    mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                                }
                                return;
                            }
                            List<CardData> dataList = new ArrayList<>(response.body().results);
                            dataList = removeDuplicates(dataList);
                            if (dataList == null
                                    || dataList.isEmpty()
                                    || dataList.size() < 3) {
                                if (mListener != null) {
                                    mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                                }
                                return;
                            }
                            mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                            loadData(dataList);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + t);

                        }
                    });
            APIService.getInstance().execute(mRequestContentList);
        }



        /*String contentId = mData._id;
        SimilarContentRequest.Params contentListparams = new SimilarContentRequest.Params(contentId, APIConstants.LEVEL_DEVICE_MAX, 1, APIConstants.PAGE_INDEX_COUNT);

        SimilarContentRequest mRequestContentList = new SimilarContentRequest(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if (response == null
                                || response.body() == null) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message- " + response.body().message);
                        if (response.body().results == null || response.body().results.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }
                        List<CardData> dataList = new ArrayList<>(response.body().results);
                        dataList = removeDuplicates(dataList);
                        if (dataList == null
                                || dataList.isEmpty()
                                || dataList.size() < 3) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }
                        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                        loadData(dataList);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + t);
                        if (mListener != null) {
                            mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                        }
                    }
                });
        APIService.getInstance().execute(mRequestContentList);*/

    }

    private String getGenres(){
        ArrayList<String> genresList=new ArrayList<>();
        if(mData.content!=null&&mData.content.genre!=null
                &&mData.content.genre.size()!=0){
            for (int p=0;p<mData.content.genre.size();p++){
                genresList.add(mData.content.genre.get(p).name);
            }
            return TextUtils.join(",",genresList);
        }
        return "";
    }

    public void loadData(List<CardData> dataList){
        if (mCarouselLayoutType!=null){
            if(mCarouselLayoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM)){
                AdapterMedHorizontalCarousel medHorizontalCarousel=new AdapterMedHorizontalCarousel(mContext,dataList);
                mRecyclerViewCarouselInfo.setAdapter(medHorizontalCarousel);
                medHorizontalCarousel.showTitle(true);
                medHorizontalCarousel.setBgColor(mBgColor);
            }else {
                AdapterBigHorizontalCarousel mAdapterCarouselInfo = new AdapterBigHorizontalCarousel(mContext, dataList);
                mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
                mAdapterCarouselInfo.isSimilarContents(true);
            }
        }else {
            AdapterBigHorizontalCarousel mAdapterCarouselInfo = new AdapterBigHorizontalCarousel(mContext, dataList);
            mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
            mAdapterCarouselInfo.isSimilarContents(true);
        }
    }


    public ArrayList<CardData> removeDuplicates(List<CardData> list) {
        Set<CardData> set = new TreeSet<>(new Comparator<CardData>() {
            @Override
            public int compare(CardData o1, CardData o2) {
                if (o1._id.equalsIgnoreCase(o2._id)) {
                    return 0;
                }
                return 1;
            }
        });
        for (int i = 0; i < list.size(); i++) {
            CardData data = list.get(i);
            if (mData != null
                    && mData._id.equalsIgnoreCase(data._id)) {
                list.remove(i);
                break;
            }
        }

        set.addAll(list);
        return new ArrayList(set);
    }

}
