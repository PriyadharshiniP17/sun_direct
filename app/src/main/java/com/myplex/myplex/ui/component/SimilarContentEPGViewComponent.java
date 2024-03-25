package com.myplex.myplex.ui.component;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.adapter.AdapterLiveTvItem;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimilarContentEPGViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final RecyclerView mRecyclerViewCarouselInfo;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private int positionInAdapter;
    private static final String TAG = SimilarContentEPGViewComponent.class.getSimpleName();


    public SimilarContentEPGViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        mRecyclerViewCarouselInfo = view.findViewById(R.id.recyclerview);

    }

    public static SimilarContentEPGViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_carouselinfo,
                parent, false);
        SimilarContentEPGViewComponent briefDescriptionComponent = new SimilarContentEPGViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
        positionInAdapter = position;
        this.mData = viewData.cardData;
        HorizontalItemDecorator mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_2));
        mRecyclerViewCarouselInfo.addItemDecoration(mHorizontalDividerDecoration);
        mRecyclerViewCarouselInfo.setItemAnimator(null);
        AdapterLiveTvItem adapterLiveTvItem = new AdapterLiveTvItem(mContext, CardData.DUMMY_LIST);
        mRecyclerViewCarouselInfo.setAdapter(adapterLiveTvItem);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCarouselInfo.setLayoutManager(layoutManager);
        fetchEpgData();
    }

    private void fetchEpgData() {

        final Date date = Util.getCurrentDate(0);
        final String time = Util.getCurrentEpgTablePosition();
        final StringBuilder languages = new StringBuilder();
        if (mData != null
                && mData.content != null
                && mData.content.language != null
                && mData.content.language.size() > 0) {
            for (String language : mData.content.language) {
                languages.append(languages.length() == 0 ? language : "," + language);
            }
        }
        final StringBuilder genres = new StringBuilder();
        if (mData != null
                && mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            for (CardDataGenre genre : mData.content.genre) {
                genres.append(genres.length() == 0 ? genre.name : "," + genre.name);
            }
        }
        final String oldLanguage = EPG.langFilterValues;
        final String oldGenre = EPG.genreFilterValues;
        EPG.langFilterValues = languages.toString();
        EPG.genreFilterValues = genres.toString();
        LoggerD.debugLog("TVGuide: fetchEpgData: time- " + time + "" +
                " oldGenre- " + oldGenre + "" +
                " languages- " + languages + "" +
                " oldLanguage- " + oldLanguage);

        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && 0 - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;

        EPG.getInstance(Util.getCurrentDate(0)).findPrograms(11, Util.getServerDateFormat(time, date), time, date, 1, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly, "",new EPG.CacheManagerCallback() {

            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                //System.out.println("phani size "+dataList.size());
                EPG.langFilterValues = oldLanguage;
                EPG.genreFilterValues = oldGenre;
                List list = removeSameProgram(dataList);
                if (list == null
                        || list.isEmpty()
                        || list.size() < 2) {
                    if (mListener != null) {
                        mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                        mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED,positionInAdapter);
                    }
                    return;
                }

                mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                AdapterLiveTvItem mAdapterCarouselInfo = new AdapterLiveTvItem(mContext, list);
                mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                if (mListener != null) {
                    mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                }
            }
        }, true);


    }

    private List<CardData> removeSameProgram(List<CardData> dataList) {
        if (mData == null
                || dataList == null
                || dataList.isEmpty()) {
            return dataList;
        }
        String globalServiceId = mData.globalServiceId;
        if (mData != null && mData.isLive()) {
            globalServiceId = mData._id;
        }
        if (globalServiceId == null) {
            return dataList;
        }
        List<CardData> cardDataList = new ArrayList<>();
        cardDataList.addAll(dataList);
        for (int i = 0; i < cardDataList.size(); i++) {
            if (cardDataList.get(i).globalServiceId != null && cardDataList.get(i).globalServiceId.equalsIgnoreCase(globalServiceId)) {
                cardDataList.remove(i);
                return cardDataList;
            }
        }

        return cardDataList;
    }


}
