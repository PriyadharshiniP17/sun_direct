package com.myplex.myplex.ui.component;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.util.PrefUtils;
import com.myplex.util.StringManager;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

import static android.view.View.GONE;

public class SeasonDropDownViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final boolean isHeaderEnabled;
    private final TextView mHeaderDropDownTitle;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = SeasonDropDownViewComponent.class.getSimpleName();
    private TextView mTextViewSeasons, mTextViewSeasonsLang;
    private ImageView mEpisodeDropDownIcon;


    public SeasonDropDownViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener, boolean isHeaderEnabled) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        this.mData = mValues.get(position).cardData;
        this.isHeaderEnabled = isHeaderEnabled;
        //if (isHeaderEnabled) {
            view.setOnClickListener(mDateLayoutClickListener);
        //}
        mTextViewSeasons = view.findViewById(R.id.header_title_text);
        mTextViewSeasonsLang = view.findViewById(R.id.header_sub_title_textLang);
        if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN){
            mTextViewSeasonsLang.setVisibility(View.VISIBLE);
        }else{
            mTextViewSeasonsLang.setVisibility(GONE);
        }
        mEpisodeDropDownIcon = view.findViewById(R.id.drop_down_button);
        mHeaderDropDownTitle = view.findViewById(R.id.header_drop_down_title);

    }

    public static SeasonDropDownViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener, boolean isHeaderEnabled) {
        View view = LayoutInflater.from(context).inflate(R.layout.carddetailsdescription_episodes_season_title,
                parent, false);
        SeasonDropDownViewComponent briefDescriptionComponent = new SeasonDropDownViewComponent(context, data, view, listener, isHeaderEnabled);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
        if (viewData == null || viewData.title == null) return;
        this.mData = viewData.cardData;
        mTextViewSeasons.setText(viewData.title);
        if(PrefUtils.getInstance().getVernacularLanguage()){
            String[] arr = viewData.title.split(" ");
            if (arr != null && arr.length > 1 && !TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.SEASON))) {
                mTextViewSeasonsLang.setVisibility(View.VISIBLE);
                mTextViewSeasonsLang.setText(StringManager.getInstance().getString(APIConstants.SEASON) + " " + arr[1]);
            }else{
                mTextViewSeasonsLang.setVisibility(GONE);
            }
        }
        mHeaderDropDownTitle.setVisibility(GONE);
        mEpisodeDropDownIcon.setVisibility(GONE);
        if (mData != null) {
            if (mData.isLive() || mData.isProgram()
                    || mData.isTVSeries()) {
                mEpisodeDropDownIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private View.OnClickListener mDateLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mListener == null) return;
            mListener.onShowPopup();
        }
    };
}
