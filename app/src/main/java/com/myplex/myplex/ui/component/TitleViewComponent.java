package com.myplex.myplex.ui.component;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.model.CardData;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

public class TitleViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final TextView titleView;
    private final TextView mTitleOtherLanguageView;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = TitleViewComponent.class.getSimpleName();


    public TitleViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view,
                              CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        titleView = view.findViewById(R.id.carddetail_title_text);
        mTitleOtherLanguageView = view.findViewById(R.id.title_other_language);

    }

    public static TitleViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.carddetaildescriptiontitlesectionview,
                parent, false);
        TitleViewComponent briefDescriptionComponent = new TitleViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
        titleView.setText(viewData.title);
        mTitleOtherLanguageView.setVisibility(View.GONE);
        if (PrefUtils.getInstance().getVernacularLanguage()) {
            mTitleOtherLanguageView.setVisibility(View.VISIBLE);
            mTitleOtherLanguageView.setText(viewData.vernacularTitle);
        }
        if(viewData.mBgColor!=null&&!TextUtils.isEmpty(viewData.mBgColor)){
            titleView.setTextColor(mContext.getResources().getColor(R.color.description_people_also_watch_text_light_color));
            mTitleOtherLanguageView.setTextColor(mContext.getResources().getColor(R.color.description_people_also_watch_text_light_color));
        }else {
            titleView.setTextColor(mContext.getResources().getColor(R.color.description_people_also_watch_text_dark_color));
            mTitleOtherLanguageView.setTextColor(mContext.getResources().getColor(R.color.description_people_also_watch_text_dark_color));
        }
    }
}
