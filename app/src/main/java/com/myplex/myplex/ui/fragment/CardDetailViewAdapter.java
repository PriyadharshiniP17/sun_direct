package com.myplex.myplex.ui.fragment;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.util.PrefUtils;
import com.myplex.util.StringManager;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.component.BriefDescriptionComponent;
import com.myplex.myplex.ui.component.DummyItemView;
import com.myplex.myplex.ui.component.EPGDropDownTitleViewComponent;
import com.myplex.myplex.ui.component.EPGViewComponent;
import com.myplex.myplex.ui.component.EpisodeViewComponent;
import com.myplex.myplex.ui.component.EpisodesTabViewItem;
import com.myplex.myplex.ui.component.ErrorMessageViewComponent;
import com.myplex.myplex.ui.component.FooterProgressLoadingComponent;
import com.myplex.myplex.ui.component.PackagesViewComponent;
import com.myplex.myplex.ui.component.PlayerLogsTitleViewComponent;
import com.myplex.myplex.ui.component.PlayerLogsViewComponent;
import com.myplex.myplex.ui.component.RecomendedForYouViewComponent;
import com.myplex.myplex.ui.component.RelatedMediaComponent;
import com.myplex.myplex.ui.component.SeasonDropDownViewComponent;
import com.myplex.myplex.ui.component.SimilarContentEPGViewComponent;
import com.myplex.myplex.ui.component.SimilarContentVODComponent;
import com.myplex.myplex.ui.component.SimilarContentViewComponent;
import com.myplex.myplex.ui.component.TitleViewComponent;
import com.myplex.myplex.ui.component.UiCompoment;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.DetailsViewDataItem;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.StickHeaderItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static android.view.View.GONE;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_BRIEF_DESCRIPTION;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_DUMMY_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_EPG_DROPDOWN_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_EPG_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_EPISODES_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_ERROR_MESSAGE_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_FOOTER_LOADING_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_PACKAGES_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_PLAYER_LOGS_TITLE_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_PLAYER_LOGS_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_RECOMMENDED_FOR_YOU_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_RELATED_MEDIA_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_SEASONS_TABS_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_SEASON_DROPDOWN_VIEW;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_SIMILAR_CONTENT_VIEW_CAROUSEL;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_SIMILAR_CONTENT_VIEW_EPG;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_SIMILAR_CONTENT_VIEW_VOD;
import static com.myplex.myplex.ui.fragment.dummy.DetailsViewContent.CARDDETAIL_TITLE_SECTION_VIEW;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DetailsViewDataItem} and makes a call to the
 * specified {@link }.
 * TODO: Replace the implementation with code for your data type.
 */
public class CardDetailViewAdapter extends RecyclerView.Adapter<UiCompoment> implements StickHeaderItemDecoration.StickyHeaderInterface {

    private final List<DetailsViewDataItem> mValues;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mCardDetailViewFactoryListener;
    private final CardData mTVShowData;
    private Context mContext;
    private int headerDisplayPosition;
    private TextView mTextViewHeaderTitle, mTextViewHeaderSubTitleVernLang;
    private boolean isHeaderEnabled;
    private ImageView mTodayEPGButton;
    private String mSearchQuery;
    private CarouselInfoData mCarouselInfoData;
    private String mSeasonName;
    private TextView mTextViewHeaderDropDownText;
    private String sourceDetails,source;
    private FragmentManager childFragmentManager;
    private RecyclerView parentRecyclerView;

    public CardDetailViewAdapter(Context mContext, List<DetailsViewDataItem> items, CardDetailViewFactory.CardDetailViewFactoryListener
            cardDetailViewFactoryListener, int headerDisplayPosition, CardData mTVShowData, String seasonName
            , String sourceDetails, String source, FragmentManager childFragmentManager,RecyclerView parentRecyclerView) {
        this.mContext = mContext;
        this.mCardDetailViewFactoryListener = cardDetailViewFactoryListener;
        this.headerDisplayPosition = headerDisplayPosition;
        this.isHeaderEnabled = headerDisplayPosition != -1;
        this.mValues = items;
        this.mTVShowData = mTVShowData;
        this.mSeasonName = seasonName;
        this.sourceDetails = sourceDetails;
        this.source = source;
        this.parentRecyclerView=parentRecyclerView;
        this.childFragmentManager=childFragmentManager;
    }

    public void updateHeaderDisplayPosition(int headerDisplayPosition){
        this.headerDisplayPosition = headerDisplayPosition;
        this.isHeaderEnabled = headerDisplayPosition != -1;
    }

    @Override
    public UiCompoment onCreateViewHolder(ViewGroup parent, int viewType) {
        UiCompoment component = null;
        switch (viewType) {
            case CARDDETAIL_BRIEF_DESCRIPTION:
                component = BriefDescriptionComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_TITLE_SECTION_VIEW:
                component = TitleViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_PACKAGES_VIEW:
                component = PackagesViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_SIMILAR_CONTENT_VIEW_CAROUSEL:
                component = SimilarContentViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_SIMILAR_CONTENT_VIEW_EPG:
                component = SimilarContentEPGViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_SIMILAR_CONTENT_VIEW_VOD:
                component = SimilarContentVODComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_EPG_DROPDOWN_VIEW:
                component = EPGDropDownTitleViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener, isHeaderEnabled);
                break;
            case CARDDETAIL_EPG_VIEW:
                component = EPGViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_SEASON_DROPDOWN_VIEW:
                component = SeasonDropDownViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener, isHeaderEnabled);
                break;
            case CARDDETAIL_EPISODES_VIEW:
                component = EpisodeViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener,
                        mSearchQuery, sourceDetails, mTVShowData, mSeasonName,source);
                break;
            case CARDDETAIL_FOOTER_LOADING_VIEW:
                component = FooterProgressLoadingComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_ERROR_MESSAGE_VIEW:
                component = ErrorMessageViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_PLAYER_LOGS_TITLE_VIEW:
                component = PlayerLogsTitleViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_PLAYER_LOGS_VIEW:
                component = PlayerLogsViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_RELATED_MEDIA_VIEW:
                component = RelatedMediaComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_SEASONS_TABS_VIEW:
                component= EpisodesTabViewItem.createView(mContext,mValues,parent,
                        mCardDetailViewFactoryListener,childFragmentManager
                        ,mTVShowData);
                break;
            case CARDDETAIL_DUMMY_VIEW:
                component= DummyItemView.createView(mContext,mValues,parent,mCardDetailViewFactoryListener);
                break;
            case CARDDETAIL_RECOMMENDED_FOR_YOU_VIEW:
                component = RecomendedForYouViewComponent.createView(mContext, mValues, parent, mCardDetailViewFactoryListener);
                break;
        }
        return component;
    }

    @Override
    public void onBindViewHolder(final UiCompoment holder, int position) {
        holder.bindItemViewHolder(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (CARDDETAIL_BRIEF_DESCRIPTION == mValues.get(position).layoutType)
            return CARDDETAIL_BRIEF_DESCRIPTION;
        if (CARDDETAIL_TITLE_SECTION_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_TITLE_SECTION_VIEW;
        if (CARDDETAIL_PACKAGES_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_PACKAGES_VIEW;
        if (CARDDETAIL_SIMILAR_CONTENT_VIEW_CAROUSEL == mValues.get(position).layoutType)
            return CARDDETAIL_SIMILAR_CONTENT_VIEW_CAROUSEL;
        if (CARDDETAIL_SIMILAR_CONTENT_VIEW_EPG == mValues.get(position).layoutType)
            return CARDDETAIL_SIMILAR_CONTENT_VIEW_EPG;
        if (CARDDETAIL_SIMILAR_CONTENT_VIEW_VOD == mValues.get(position).layoutType)
            return CARDDETAIL_SIMILAR_CONTENT_VIEW_VOD;
        if (CARDDETAIL_EPG_DROPDOWN_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_EPG_DROPDOWN_VIEW;
        if (CARDDETAIL_EPG_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_EPG_VIEW;
        if (CARDDETAIL_SEASON_DROPDOWN_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_SEASON_DROPDOWN_VIEW;
        if (CARDDETAIL_EPISODES_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_EPISODES_VIEW;
        if (CARDDETAIL_FOOTER_LOADING_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_FOOTER_LOADING_VIEW;
        if (CARDDETAIL_ERROR_MESSAGE_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_ERROR_MESSAGE_VIEW;
        if (CARDDETAIL_PLAYER_LOGS_TITLE_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_PLAYER_LOGS_TITLE_VIEW;
        if (CARDDETAIL_RELATED_MEDIA_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_RELATED_MEDIA_VIEW;
        if (CARDDETAIL_PLAYER_LOGS_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_PLAYER_LOGS_VIEW;
        if (CARDDETAIL_SEASONS_TABS_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_SEASONS_TABS_VIEW;
        if (CARDDETAIL_DUMMY_VIEW == mValues.get(position).layoutType)
            return CARDDETAIL_DUMMY_VIEW;
        if (CARDDETAIL_RECOMMENDED_FOR_YOU_VIEW==mValues.get(position).layoutType)
            return CARDDETAIL_RECOMMENDED_FOR_YOU_VIEW;
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addAll(List<DetailsViewDataItem> items) {
        int lastItemsCount = getItemCount();
        mValues.addAll(items);
        notifyItemRangeChanged(lastItemsCount, items.size());
    }

    public void removeAt(int position) {
        if(mValues.size()>position){
            DetailsViewDataItem dataItem =  mValues.get(position);
            if(dataItem.layoutType== CARDDETAIL_SIMILAR_CONTENT_VIEW_EPG){
                if(position>1) {
                    mValues.remove(position);
                    mValues.remove(position-1);
                    notifyItemRangeRemoved(position - 1,2);
                }

            }

        }
    }

    public void removeEPGTitle() {
        try {
            if (mValues.size() > 1) {
                int positionOfEPGTitle = mValues.size();
                DetailsViewDataItem dataItem = mValues.get(positionOfEPGTitle - 2);
                if (dataItem.layoutType == CARDDETAIL_EPG_DROPDOWN_VIEW) {
                    if (positionOfEPGTitle > 1) {
                        mValues.remove(positionOfEPGTitle - 1);
                        mValues.remove(positionOfEPGTitle - 2);
                        notifyItemRangeRemoved(positionOfEPGTitle - 2, 2);

                    }

                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        return itemPosition;
    }

    @Override
    public View getHeaderLayout(int itemPosition, RecyclerView parent) {
        if (!isHeaderEnabled) return null;
        if (itemPosition >= headerDisplayPosition) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.carddetailsdescription_episodes_season_title, parent, false);
            mTextViewHeaderTitle = view.findViewById(R.id.header_title_text);
            mTextViewHeaderSubTitleVernLang = view.findViewById(R.id.header_sub_title_textLang);
            mTextViewHeaderDropDownText = view.findViewById(R.id.header_drop_down_title);
            mTodayEPGButton = view.findViewById(R.id.drop_down_button);
            return view;
        }
        return null;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        if (!isHeaderEnabled || headerDisplayPosition >= mValues.size()) return;
        DetailsViewDataItem viewDataItem = mValues.get(headerDisplayPosition);
        LoggerD.debugLog("viewDataItem::" + viewDataItem);
        if (viewDataItem.title == null || viewDataItem.cardData == null) return;
        if (mTextViewHeaderTitle == null || mTodayEPGButton == null) {
            return;
        }
        if (viewDataItem.cardData.isProgram() || viewDataItem.cardData.isLive()) {
            showEPGHeaderTitle(viewDataItem);
        } else {
            showSeasonHeaderTitle(viewDataItem);
        }
    }

    private void showEPGHeaderTitle(DetailsViewDataItem viewDataItem) {
        mTextViewHeaderTitle.setVisibility(View.VISIBLE);
        mTextViewHeaderTitle.setText(mContext.getResources().getString(R.string.upcoming_programs_text));
        mTextViewHeaderSubTitleVernLang.setVisibility(GONE);
        if (PrefUtils.getInstance().getVernacularLanguage()&& !TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.UPCOMING_PROGRAMS))) {
            mTextViewHeaderSubTitleVernLang.setText(StringManager.getInstance().getString(APIConstants.UPCOMING_PROGRAMS));
            mTextViewHeaderSubTitleVernLang.setVisibility(View.VISIBLE);
        }
        mTextViewHeaderDropDownText.setText(String.valueOf(viewDataItem.title));
        mTextViewHeaderDropDownText.setVisibility(View.VISIBLE);
    }

    private void showSeasonHeaderTitle(DetailsViewDataItem viewDataItem) {
        mTextViewHeaderTitle.setVisibility(View.VISIBLE);
        mTextViewHeaderTitle.setText(viewDataItem.title);
        mTextViewHeaderDropDownText.setVisibility(View.GONE);
        mTextViewHeaderSubTitleVernLang.setVisibility(GONE);
        if (PrefUtils.getInstance().getVernacularLanguage()) {
            String[] arr = viewDataItem.title.split(" ");
            if (arr != null && arr.length > 1 && !TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.SEASON))) {
                mTextViewHeaderSubTitleVernLang.setText(StringManager.getInstance().getString(APIConstants.SEASON) + " " + arr[1]);
                mTextViewHeaderSubTitleVernLang.setVisibility(View.VISIBLE);
            }
        }
        mTodayEPGButton.setVisibility(View.GONE);
        if (viewDataItem.cardData != null && viewDataItem.cardData.isTVSeries()) {
            mTodayEPGButton.setVisibility(GONE);
        }
    }

    @Override
    public boolean isHeader(int itemPosition) {
        if (itemPosition == headerDisplayPosition) {
            return true;
        }
        return false;
    }

    @Override
    public void onHeaderClick(View v) {
        LoggerD.debugLog("onHeaderClick");
        if (mCardDetailViewFactoryListener != null) {
            mCardDetailViewFactoryListener.onShowPopup();
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecyclerView=recyclerView;
    }

    public List<DetailsViewDataItem> getData() {
        return mValues;
    }

}
