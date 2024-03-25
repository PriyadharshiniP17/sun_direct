package com.myplex.myplex.ui.component;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.util.PrefUtils;
import com.myplex.util.StringManager;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

import static android.view.View.GONE;

public class EPGDropDownTitleViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final TextView mEPGTitleDate;
    private final boolean isHeaderEnabled;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private final TextView mTextViewChannelNameLang;
    private Context mContext;
    private static final String TAG = EPGDropDownTitleViewComponent.class.getSimpleName();


    public EPGDropDownTitleViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener, boolean isHeaderEnabled) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        this.isHeaderEnabled = isHeaderEnabled;
        view.setOnClickListener(mDateLayoutClickListener);
        mEPGTitleDate = (TextView) view.findViewById(R.id.header_drop_down_title);
        mTextViewChannelNameLang = (TextView) view.findViewById(R.id.header_sub_title_textLang);
    }

    public static EPGDropDownTitleViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener, boolean isHeaderEnabled) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_channel_title_date,
                parent, false);
        EPGDropDownTitleViewComponent briefDescriptionComponent = new EPGDropDownTitleViewComponent(context, data, view, listener, isHeaderEnabled);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
        setDateToCalender(viewData.title);
    }


    public Spannable setDateToCalender(String date) {
        Spannable cs = new SpannableString(date);
        if (date.contains("Today")) {
            cs.setSpan(new SuperscriptSpan(), 10, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 10, 12, 0);
        } else {
            cs.setSpan(new SuperscriptSpan(), 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 7, 9, 0);
        }
        if (mEPGTitleDate != null) {
            mEPGTitleDate.setVisibility(View.VISIBLE);
            mEPGTitleDate.setText(cs);
        }
        if(PrefUtils.getInstance().getVernacularLanguage()&& !TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.UPCOMING_PROGRAMS))){
            mTextViewChannelNameLang.setText(StringManager.getInstance().getString(APIConstants.UPCOMING_PROGRAMS));
            mTextViewChannelNameLang.setVisibility(View.VISIBLE);
        }else{
            mTextViewChannelNameLang.setVisibility(GONE);
        }
        return cs;
    }

    private View.OnClickListener mDateLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mListener == null || !isHeaderEnabled) return;
            mListener.onShowPopup();
        }
    };
}
