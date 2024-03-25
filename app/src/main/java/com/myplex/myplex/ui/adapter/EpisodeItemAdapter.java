package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;


import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EpisodeItemAdapter extends RecyclerView.Adapter<EpisodeItemAdapter.CarouselDataViewHolder> {

    private static final int DESCRIPTION_COLLAPSED = 1;
    private static final int DESCRIPTION_EXPANDED = 0;
    /*private CardData mData;*/
    private  CardDetailViewFactory.CardDetailViewFactoryListener mListener = null;
    private Context mContext;
    private static final String TAG = EpisodeItemAdapter.class.getSimpleName();
    ImageView downloadingGIFAnim;
    private String mCurrentSeasonName;
    private RecyclerView recyclerViewReference;
    private CardData mTVShowCardData;
    private boolean isToExpand;
    private int expandPosition;
    private String mSearchQuery;
    private String sourceDetails, source;
    private List<CardData> mListMovies;
    private int mParentPosition;
    private String mBgColor;

    public EpisodeItemAdapter(CardDetailViewFactory.CardDetailViewFactoryListener listener, Context mContext,
                              List<CardData> mListMovies,String mBgColor) {
        this.mListener = listener;
        this.mContext = mContext;
        this.mListMovies = mListMovies;
        this.mBgColor=mBgColor;
    }
    public EpisodeItemAdapter(RecyclerView recyclerView, Context mContext,
                              List<CardData> mListMovies,String mBgColor) {
        recyclerViewReference = recyclerView;
        this.mContext = mContext;
        this.mListMovies = mListMovies;
        this.mBgColor=mBgColor;
    }

    public void setListData(List<CardData> cardData) {
        if (mListMovies != null) {
            mListMovies.addAll(cardData);
            notifyDataSetChanged();
        }
    }


    @NonNull
    @Override
    public EpisodeItemAdapter.CarouselDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        CarouselDataViewHolder customItemViewHolder;
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_related_vods_tvshows, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeItemAdapter.CarouselDataViewHolder holder, int position) {
        CardData mData = mListMovies.get(position);
        if (mData == null) {
            return;
        }
        if (mData != null) {
            if (mData.generalInfo != null) {
                if (mData.isVODChannel()
                        || mData.isTVSeason()
                        && ApplicationController.SHOW_PLAYER_LOGS
                        && mData.generalInfo.title != null) {
                    holder.mEpiosdeTitle.setText(mData.generalInfo.title);
                } else if (mData.generalInfo.title != null) {
                    holder.mEpiosdeTitle.setText(mData.generalInfo.title);
                }
            }

            CardDataImagesItem imageItem = mData.getImageItem();
            if (imageItem != null) {
                if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
                    holder.mThumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    holder.mThumbnailView.invalidate();
                } else {
                    holder.mThumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.mThumbnailView.invalidate();
                }
                holder.mThumbnailView.setImageResource(R.drawable
                        .black);
                if (!TextUtils.isEmpty(imageItem.link)) {
                    PicassoUtil.with(mContext).load(imageItem.link, holder.mThumbnailView, R.drawable.black);
                }
            }

            if (mData.content != null) {

                if (mData.isVODCategory()
                        || mData.isVODYoutubeChannel()
                        || mData.isTVSeason()
                        || mData.isTVSeries()
                        || mData.isTVEpisode()) {
                    if (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)) {
                        holder.mEpiosodeDuration.setVisibility(View.GONE);
                        if (mData.content != null
                                && mData.content.serialNo != null) {
                            holder.mEpiosodeDuration.setVisibility(View.VISIBLE);
                            if (!TextUtils.isEmpty(mData.content.duration) && !TextUtils.isEmpty(Util.getDDMMYYYYUTC(mData.content.releaseDate))) {
                                holder.mEpiosodeDuration.setText( Util.getDDMMYYYYUTC(mData.content.releaseDate));
                            } else if (!TextUtils.isEmpty(mData.content.duration)) {
                                holder.mEpiosodeDuration.setText(mData.getDurationWithFormat());
                            } else {
                               // holder.mEpiosodeDuration.setText("Episode " + mData.content.serialNo);
                            }
                        }
                    } else {
                        if (!TextUtils.isEmpty(mData.content.duration)) {
                            holder.mEpiosodeDuration.setText(mData.getDurationWithFormat());
                            holder.mEpiosodeDuration.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(mData.content.duration)) {
                        holder.mEpiosodeDuration.setText(Util.getDDMMYYYYUTC(mData.content.releaseDate));
                        holder.mEpiosodeDuration.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
                holder.mainBackGround.setBackgroundColor(Color.parseColor(mBgColor));
                holder.mEpiosdeTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
                holder.mEpiosodeDuration.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_sub_heading_text_color));
            }
            //showCollapseAndExpand(mData, position);
            holder.setClickListener(mOnItemClickListenerWithData);
        }


        //showDownloadButtonUI();

    }

    public void setData(final List<CardData> listMovies) {
        if (listMovies == null || recyclerViewReference == null) {
            return;
        }
        mListMovies = listMovies;
        recyclerViewReference.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!recyclerViewReference.isComputingLayout()) {
                        notifyDataSetChanged();
                    } else {
                        setData(listMovies);
                    }
                } catch (Exception e) {
                    //  Crashlytics.logException(e);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return mListMovies.size();
    }

    public void setParentPosition(int position) {
        this.mParentPosition = position;
    }


    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListenerWithData clickListener;
        TextView mDownloadPercentText;
        LinearLayout mDownloadInProgressLinearLayout;
        TextView mEpiosdeDescription;
        private ImageView mCollapseOrExpandBtn;
        ImageView mDownloadBtn;
        ImageView mThumbnailView;
        TextView mEpiosodeDuration;
        TextView mEpiosdeTitle;
        ImageView mReminderImage;
        ProgressBar mProgressBar;
        RelativeLayout mainBackGround;

        public CarouselDataViewHolder(View itemView) {
            super(itemView);
            mainBackGround=itemView.findViewById(R.id.layout_item_episode);
            mThumbnailView = (ImageView) itemView.findViewById(R.id.imageview_thumbnail);
            mEpiosodeDuration = (TextView) itemView.findViewById(R.id
                    .textview_duration);
            mEpiosdeTitle = (TextView) itemView.findViewById(R.id
                    .textview_title);
            mEpiosdeDescription = (TextView) itemView.findViewById(R.id
                    .textview_description);
            mReminderImage = (ImageView) itemView.findViewById(R.id
                    .imageview_play_alarm_download);
            mDownloadBtn = (ImageView) itemView.findViewById(R.id
                    .download_btn_image);
            mCollapseOrExpandBtn = (ImageView) itemView.findViewById(R.id
                    .expand_btn_image);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.customProgress);
            mDownloadInProgressLinearLayout = (LinearLayout) itemView.findViewById(R.id.download_btn_layout);
            mDownloadPercentText = (TextView) itemView.findViewById(R.id
                    .download_btn_status_percent_text);
            downloadingGIFAnim = (ImageView) itemView.findViewById(R.id.downloading_gif_anim);
            if (downloadingGIFAnim != null)
                downloadingGIFAnim.setBackgroundColor(Color.TRANSPARENT); //for gif without background

            itemView.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListenerWithData itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (this.clickListener != null && mListMovies != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                this.clickListener.onClick(v, getAdapterPosition(), mParentPosition, mListMovies.get(getAdapterPosition()));
            }
        }
    }

    private ItemClickListenerWithData mOnItemClickListenerWithData;

    public void setOnItemClickListenerWithMovieData(ItemClickListenerWithData onItemClickListenerWithData) {
        this.mOnItemClickListenerWithData = onItemClickListenerWithData;
    }

    public void showCollapseAndExpand(CardData cardData, int position, CarouselDataViewHolder holder) {
        holder.mCollapseOrExpandBtn.setVisibility(View.GONE);
        String description = cardData.getBriefDescription();
        holder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
        holder.mEpiosdeDescription.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(description)) {
            holder.mCollapseOrExpandBtn.setVisibility(View.VISIBLE);
            holder.mEpiosdeDescription.setText(cardData.getBriefDescription());
        }
        holder.mCollapseOrExpandBtn.setTag(position);
        if (position == expandPosition) {
            if (isToExpand) {
                holder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_collapse_icon);
                holder.mEpiosdeDescription.setVisibility(View.VISIBLE);
            } else {
                holder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
                holder.mEpiosdeDescription.setVisibility(View.GONE);
            }
        }
        holder.mCollapseOrExpandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof Integer) {
                    int lastExpandPosition = expandPosition;
                    expandPosition = (int) v.getTag();
                    if (lastExpandPosition == expandPosition) {
                        isToExpand = !isToExpand;
                    } else {
                        isToExpand = true;
                    }
//                    notifyItemChanged(expandPosition);
//                    notifyItemChanged(lastExpandPosition);
//                    if (mListener != null) {
//                        mListener.onDataChange();
//                    }
                    if (isToExpand) {
                        holder.mCollapseOrExpandBtn.setTag(DESCRIPTION_EXPANDED);
                        holder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_collapse_icon);
                        holder.mEpiosdeDescription.setVisibility(View.VISIBLE);
                    } else {
                        holder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
                        holder.mCollapseOrExpandBtn.setTag(DESCRIPTION_COLLAPSED);
                        holder.mEpiosdeDescription.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void showContentDuration(CardData mData) {

    }


    @Override
    public void onAttachedToRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

