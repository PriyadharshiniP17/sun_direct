package com.myplex.myplex.ui.component;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.adapter.AdapterSmallHorizontalCarousel;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SimilarContentVODComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final RecyclerView mRecyclerViewCarouselInfo;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = SimilarContentVODComponent.class.getSimpleName();


    public SimilarContentVODComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        mRecyclerViewCarouselInfo = view.findViewById(R.id.recyclerview);
    }

    public static SimilarContentVODComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_carouselinfo,
                parent, false);
        SimilarContentVODComponent briefDescriptionComponent = new SimilarContentVODComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
        this.mData = viewData.cardData;
        HorizontalItemDecorator mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_2));
        mRecyclerViewCarouselInfo.addItemDecoration(mHorizontalDividerDecoration);
        mRecyclerViewCarouselInfo.setItemAnimator(null);
        AdapterSmallHorizontalCarousel adapterSmallHorizontalCarousel = new AdapterSmallHorizontalCarousel(mContext, CardData.DUMMY_LIST);
        mRecyclerViewCarouselInfo.setAdapter(adapterSmallHorizontalCarousel);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCarouselInfo.setLayoutManager(layoutManager);
        fetchRelatedVideos();
    }

    private void fetchRelatedVideos() {
        if (mData == null
                || TextUtils.isEmpty(mData.globalServiceId)) {
            //Log.d(TAG, "fetchRelatedVideos: globalServiceId is NA");
            return;
        }
        new CacheManager().getRelatedVODListTypeExclusion(mData.globalServiceId, 1, true, APIConstants.TYPE_TVSEASON,
                APIConstants.PAGE_INDEX_COUNT,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message - ");
                        List<CardData> dataList1 = new ArrayList<>(dataList);
                        dataList1 = removeDuplicates(dataList1);
                        if (dataList1 == null
                                || dataList1.isEmpty()
                                || dataList.size() < 3) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                        AdapterSmallHorizontalCarousel mAdapterCarouselInfo = new AdapterSmallHorizontalCarousel(mContext, dataList1);
                        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        List<CardData> dataList1 = new ArrayList<>(dataList);
                        dataList1 = removeDuplicates(dataList1);
                        if (dataList1 == null || dataList1.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                        AdapterSmallHorizontalCarousel mAdapterCarouselInfo = new AdapterSmallHorizontalCarousel(mContext, dataList1);
                        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);

                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                        if (mListener != null) {
                            mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                        }
                    }
                });
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
