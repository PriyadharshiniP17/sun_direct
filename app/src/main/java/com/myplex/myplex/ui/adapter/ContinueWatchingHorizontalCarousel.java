package com.myplex.myplex.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.model.TextureItem;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.utils.FixedAspectRatioRelativeLayout;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


public class ContinueWatchingHorizontalCarousel extends RecyclerView.Adapter<ContinueWatchingHorizontalCarousel.CarouselDataViewHolder> {

    private static final String TAG = ContinueWatchingHorizontalCarousel.class.getSimpleName();

    private final Context mContext;
    private List<CardData> mListMovies;
//    private boolean mIsDummyData = false;
    private int mParentPosition;
    private String mPageName;
    private int mPageSize;
    private ProgressDialog mProgressDialog;
    private GenericListViewCompoment parentViewHolder;
    private RecyclerView recyclerViewReference;
    //adapter view click listener
    private List<TextureItem> texture;
    public ContinueWatchingHorizontalCarousel(Context context, List<CardData> itemList,RecyclerView recyclerView) {
        mContext = context;
        mListMovies = itemList;
        recyclerViewReference = recyclerView;
//        ScopedBus.getInstance().register(this);
    }

/*
    public boolean isContainingDummies(){
        return mIsDummyData;
    }
*/

    private boolean isContinueWatchingSection;
    private boolean isToShowGridLayout;
    private OnItemRemovedListener mOnItemRemovedListener;

    private View.OnClickListener mOnItemRemoveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeItem(view);
        }
    };

    private void removeItem(View view) {
        LoggerD.debugLogAdapter("removeItem view data mParentPosition- " + mParentPosition + " getTag- " + view.getTag());
        try {
            if (view != null
                    && view.getTag() != null
                    && view.getTag() instanceof Integer) {
                int pos = (int) view.getTag();
                if(mListMovies.get(pos).generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM) || mListMovies.get(pos).generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)) {
//                    Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos).globalServiceId,mListMovies.get(pos).getType());
                 if(mListMovies.get(pos).globalServiceId!=null) {
                     Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos).globalServiceId,mListMovies.get(pos).getType());
                 }else{
                     Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
                 }
                }else{
                    Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
                }
                mListMovies.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos,mListMovies.size());
            }
            if (mOnItemRemovedListener != null) {
                mOnItemRemovedListener.onItemRemoved(mParentPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                }
            }
        });
    }

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder = null;
        if (isToShowGridLayout){
            view = LayoutInflater.from(mContext).inflate(R.layout.griditem_carousel_grid_recycler_continue_watching, parent, false);
        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_carousel_grid_recycler_continue_watching, parent, false);
        }

        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListMovies.get(position));
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CardData carouselData) {

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

        if (carouselData != null) {

            holder.mTextViewVODInfo1.setVisibility(View.INVISIBLE);
            holder.mTextViewVODInfo2.setVisibility(View.GONE);
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);


                    if(carouselData.content!=null){
                        if(carouselData.generalInfo!=null){
                            if(carouselData.generalInfo.title!=null){
//                    holder.mTextViewMovieTitle.setText(carouselData.generalInfo.title);
                            }
                            else{
                                holder.mTextViewMovieTitle.setVisibility(View.GONE);
                            }
                            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
                            if(!isContinueWatchingSection) {
                                holder.mTextViewMovieTitle.setText(carouselData.content.channelNumber + "." + carouselData.globalServiceName);
                            }
                            }else{
                            holder.mTextViewMovieTitle.setVisibility(View.GONE);
                        }

                    if (texture != null && texture.size() > 0) {
                        List<String> texturelist = new ArrayList<String>();
                        for (int i = 0; i < texture.size(); i++) {
                            texturelist.add(texture.get(i).metadata);
                        }
                        if (texturelist.contains("title")) {
                            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
                        }else{
                            holder.mTextViewMovieTitle.setVisibility(View.GONE);}
                        if (texturelist.contains("language")) {
                            holder.mTextViewMovieLanguage.setVisibility(View.VISIBLE);
                        }
                        if (texturelist.contains("genre")) {
                            holder.mTextViewMovieGenre.setVisibility(View.VISIBLE);
                        }
                        if (texturelist.contains("rating")) {
                            holder.mRatingBarViewMovieRating.setVisibility(View.VISIBLE);
                        }else{holder.mRatingBarViewMovieRating.setVisibility(View.GONE);}
                        if (texturelist.contains("price")) {
                            holder.mTextViewMovieprice.setVisibility(View.VISIBLE);
                        }
                        if(texturelist.contains("rating") || texturelist.contains("price")){ }else{
                            holder.mLLRatingBar.setVisibility(View.GONE);
                        }
                        if(texturelist.contains("genre")|| texturelist.contains("price")){}else{
                            holder.mRRlayoutgener.setVisibility(View.GONE);
                        }
                        if (carouselData.content!=null) {
                            if(carouselData.content.genre!=null){
                            holder.mTextViewMovieGenre.setText(" | " + carouselData.content.genre.get(0).name);}
                        }
                         if (texturelist.contains("rating")&& carouselData.content != null&& carouselData.content.contentRating!=null) {
                           holder.mRatingBarViewMovieRating.setRating(Float.parseFloat(carouselData.content.contentRating));
                        }else{
                            holder.mRatingBarViewMovieRating.setRating((float) 4.5);
                        }

                        if (carouselData.content.language != null && carouselData.content.language.size() > 0) {
                            StringBuilder languageBuilder = new StringBuilder();
                            for (String language : carouselData.content.language) {
                                if (languageBuilder.length() > 0) {
                                    languageBuilder.append("| ");
                                }
                                if (!TextUtils.isEmpty(language)) {
                                    String lang = language.substring(0, 1).toUpperCase() + language.substring(1);
                                    languageBuilder.append(lang);
                                }
                            }
                            if (languageBuilder.length() != 0) {
                                holder.mTextViewMovieLanguage.setText(languageBuilder.toString());
                            }
                        }
                    }else
                    {
                        holder.mTextViewMovieLanguage.setVisibility(View.GONE);
                        holder.mTextViewMovieGenre.setVisibility(View.GONE);
                        holder.mRatingBarViewMovieRating.setVisibility(View.GONE);
                        holder.mTextViewMovieprice.setVisibility(View.GONE);
                    }

                }



            holder.mThumbnailDelete.setVisibility(View.VISIBLE);

            holder.mContinueWatchingProgress.setVisibility(View.GONE);
            if (carouselData.generalInfo != null) {
                if(APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                        && carouselData.publishingHouse != null
                        && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)){
                    holder.mImageViewProvideLogo.setVisibility(View.GONE);
//                    Picasso.with(mContext).load(APIConstants.getErosNowMusicLogoUrl()).into(holder.mImageViewProvideLogo);
                    if (!TextUtils.isEmpty(carouselData.globalServiceName)) {
                        holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                        holder.mTextViewVODInfo1.setText(carouselData.globalServiceName);
                    }
                } else {
                    holder.mImageViewProvideLogo.setVisibility(View.GONE);
                }

                if ((APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                        || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type))) {
                    holder.mTextViewMovieTitle.setText(carouselData.generalInfo.briefDescription);
                } else {
                    if(carouselData!=null && carouselData.content!=null && carouselData.content.channelNumber!=null && carouselData.globalServiceName!=null)
                        holder.mTextViewMovieTitle.setText(carouselData.content.channelNumber+"."+carouselData.globalServiceName);
                    else{
                        holder.mTextViewMovieTitle.setVisibility(View.GONE);
                    }
                }

                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                        && !TextUtils.isEmpty(carouselData.globalServiceName)) {
                    if(carouselData!=null && carouselData.content!=null && carouselData.content.channelNumber!=null && carouselData.globalServiceName!=null)
                        holder.mTextViewMovieTitle.setText(carouselData.content.channelNumber+"."+carouselData.globalServiceName);
                }

            }
          /*  if (isContinueWatchingSection) {*/
                holder.mThumbnailDelete.setVisibility(View.VISIBLE);
                holder.mThumbnailDelete.setTag(mListMovies.indexOf(carouselData));
                holder.mThumbnailDelete.setOnClickListener(mOnItemRemoveClickListener);
                holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
                holder.mContinueWatchingProgress.setVisibility(View.GONE);
                if (carouselData != null && carouselData.elapsedTime > 0) {
                    holder.mImageViewContinueWatching.setVisibility(View.GONE);
                    holder.mImageViewContinueWatching.setImageResource(R.drawable.thumbnail_play_icon);
                    LoggerD.debugLog("updatePlayerStatus: " +
                            "carouselData.elapsedTime: " + carouselData.elapsedTime +
                            "id- " + carouselData.generalInfo.title);
                    try {
                        int position = carouselData.elapsedTime;
                        int duration = Util.calculateDurationInSeconds(carouselData.content.duration);
                        int percent = 0;
                        if (duration > 0) {
                            // use long to avoid overflow
                            percent = (int) (100L * position / duration);
                        }
                        LoggerD.debugLog("updatePlayerStatus duration percent- " + percent);
//                        S2 | E1 | Pal Pal Dil Ke Paas
                        int watchedMinutes = position / 60;
                        int remainingduration = ((duration / 60) - watchedMinutes);
                        LoggerD.debugLog("remainingDuration in minutes- " + remainingduration + " mins to go");
                        holder.mContinueWatchingProgress.setVisibility(View.VISIBLE);
                        holder.mContinueWatchingProgress.setProgress(percent);
//                        holder.mContinueWatchingProgress.setProgress(carouselData.elapsedTime);
                        if (carouselData.isMovie() || carouselData.isTVSeries() || carouselData.isTVSeason() || carouselData.isTVEpisode()) {
//                            holder.mTextViewMovieTitle.setText(carouselData.getTitle());
                            holder.mTextViewMovieTitle.setVisibility(View.GONE);
                            if (remainingduration > 0) {
//                                holder.mTextViewVODInfo1.setText(remainingduration + " mins to go");
                                holder.mTextViewVODInfo1.setVisibility(View.GONE);
                            }
                        } else {
                            String title = carouselData.getTitle();
                            holder.mTextViewMovieTitle.setText(title);
                            if (carouselData.isTVEpisode()) {
                                try {
                                    String[] splitText = title.split(" " + Pattern.quote("|") + " ");
                                    for (String token :
                                            splitText) {
                                        LoggerD.debugDownload("\t token- " + token);
                                    }
                                    if (!TextUtils.isEmpty(carouselData.globalServiceName)) {
                                        holder.mTextViewMovieTitle.setText(carouselData.globalServiceName);
                                        if (splitText.length >= 2) {
                                            LoggerD.debugDownload("splitText- " + splitText + "\n1. " + splitText[0] + " \n2. " + splitText[1]);
                                            holder.mTextViewVODInfo1.setText(splitText[0] + " | " + splitText[1]);
                                            holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                                        }
                                    } else if (splitText.length >= 2) {
                                        LoggerD.debugDownload("splitText- " + splitText + "\n1. " + splitText[0] + " \n2. " + splitText[1]);
                                        holder.mTextViewMovieTitle.setText(splitText[0] + " | " + splitText[1]);
                                        holder.mTextViewVODInfo1.setText("");
                                        holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LoggerD.debugDownload("\t exception message- " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        holder.mContinueWatchingProgress.setProgress(0);
                    }
                }
//            }
            if (carouselData != null&&carouselData.generalInfo!=null) {
                showViewRatingOnThumbnail(holder,carouselData);
            }
            String imageLink = getImageLink(carouselData);
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .black);
            } else {
//                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.black);
                Glide.with(holder.mImageViewMovies.getContext()).load(imageLink).placeholder(R.drawable.black).into(holder.mImageViewMovies);
            }


            if(carouselData == null){
                holder.mImageViewPlayIcon.setVisibility(View.GONE);
            } else {
               // holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
                if(!Util.isFreeContent(carouselData)){
                    holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
                } else {
                    holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_play_icon);
                }
            }
            /*if (carouselData != null
                    && carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type))) {
                holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
            }*/
        }
        holder.rentLayout.setVisibility(View.GONE);
        /*if(carouselData != null && carouselData.generalInfo !=null && carouselData.generalInfo.contentRights != null && carouselData.generalInfo.contentRights.size()>0 && carouselData.generalInfo.contentRights.get(0)!= null) {
            if (carouselData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                holder.rentLayout.setVisibility(View.VISIBLE);
            }
        }*/
        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }

       /* holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
        if(carouselData!=null && carouselData.content!=null && carouselData.content.channelNumber!=null && carouselData.globalServiceName!=null)
        holder.mTextViewMovieTitle.setText(carouselData.content.channelNumber+"."+carouselData.globalServiceName);
        showTitle = false;
        if (showTitle) {
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
        }
        holder.mTextViewVODInfo1.setVisibility(View.GONE);*/
//            }
//        });
        String partnerImageLink=getPartnerImageLink(carouselData);
        SDKLogger.debug("partnerImage "+partnerImageLink);
        //Log.d(TAG, "bindGenreViewHolder: Srikanth"+partnerImageLink);
        if (!TextUtils.isEmpty(partnerImageLink)){
            holder.mImageViewPartner.setVisibility(View.VISIBLE);
            PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,holder.mImageViewPartner,R.drawable.black,holder.mImageViewPartner.getLayoutParams().width,holder.mImageViewPartner.getLayoutParams().height);
        }else{
            holder.mImageViewPartner.setVisibility(View.GONE);
        }
        if (carouselData != null ) {
            showViewRatingOnThumbnail(holder,carouselData);
        }
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        if (mListMovies == null) return 0;

        return mListMovies.size();
    }

    private static final int TYPE_ITEM = 1;

    @Override
    public int getItemViewType(int position) {

        return TYPE_ITEM;

        /*if (mItemList.get(position) instanceof Movie) {
            type = TYPE_MOVIE;
        } else if (mItemList.get(position) instanceof RelatedMoviesItem) {
            type = TYPE_RELATED_ITEMS;
        }*/

//        return type;
    }
    public void isContinueWatchingSection(boolean b) {
        this.isContinueWatchingSection = b;
    }

    public void isToShowGridLayout(boolean a){
     this.isToShowGridLayout=a;
    }

    public void setRemoveItemListener(OnItemRemovedListener mOnItemRemovedListener) {
        this.mOnItemRemovedListener = mOnItemRemovedListener;
    }

    public void setParentPosition(int position) {
        this.mParentPosition = position;
    }

    public void setParentViewHolder(GenericListViewCompoment holder) {
        this.parentViewHolder = holder;
    }
    private boolean showTitle = true;
    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }


    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //        private final ImageView mImageViewRupeeIcon;
        private ItemClickListenerWithData clickListener;
        final ImageView mImageViewPlayIcon;
        final ImageView mImageViewProvideLogo;
        final ImageView mImageViewMovies;
        final TextView mTextViewMovieTitle;
        final TextView mTextViewMovieprice;
        final RatingBar mRatingBarViewMovieRating;
        final RelativeLayout mLLRatingBar;
        final RelativeLayout mRRlayoutgener;
        final TextView mTextViewMovieGenre;
        final TextView mTextViewMovieLanguage;
        final TextView mTextViewVODInfo1;
        final TextView mTextViewVODInfo2;
        final ImageView mImageViewContinueWatching;
        final FixedAspectRatioRelativeLayout fixedLayout;
        private LinearLayout rentLayout;
        public ImageView mThumbnailDelete;
        public ProgressBar mContinueWatchingProgress;
        public ImageView mImageViewPartner;

        private ImageView thumbnailRatingIcon;
        private ImageView thumbnailViewsIcon;
        private TextView ratingCountText;
        private TextView viewsCountText;
        private RelativeLayout viewRatingParentLayout;


        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view, true, R.color.list_item_bkg);
            mTextViewMovieTitle = (TextView) view.findViewById(R.id.textview_title_show);
            mTextViewMovieprice = (TextView) view.findViewById(R.id.textview_price);
            mRatingBarViewMovieRating = view.findViewById(R.id.RRratingbar);
            mLLRatingBar = view.findViewById(R.id.LLRatingBar);
            mRRlayoutgener = view.findViewById(R.id.RRlayoutgener);
            mTextViewMovieGenre = (TextView) view.findViewById(R.id.textview_genre);
            mTextViewMovieLanguage = (TextView) view.findViewById(R.id.textview_lanuage);
            mImageViewMovies = (ImageView) view.findViewById(R.id.imageview_thumbnail_voditem);
            mImageViewPlayIcon = (ImageView)view.findViewById(R.id.thumbnail_movie_play);
            mTextViewVODInfo1 = (TextView) view.findViewById(R.id.vod_info1);
            mTextViewVODInfo2 = (TextView) view.findViewById(R.id.vod_info2);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.thumbnail_provider_app);
//            mImageViewRupeeIcon = (ImageView)view.findViewById(R.id.thumbnail_rupee_icon);
            mThumbnailDelete = (ImageView)view.findViewById(R.id.thumbnail_movie_delete_icon);
            mContinueWatchingProgress = (ProgressBar) view.findViewById(R.id.continue_watching_progress);
            mImageViewContinueWatching = (ImageView) view.findViewById(R.id.thumbnail_movie_play_continue_watching);
            fixedLayout = (FixedAspectRatioRelativeLayout) view.findViewById(R.id.fixed_layout);
            mImageViewPartner= (ImageView) view.findViewById(R.id.iv_partener_logo_right);
            ratingCountText = view.findViewById(R.id.rating_count_text);
            thumbnailRatingIcon = view.findViewById(R.id.rating_icon);
            viewsCountText = view.findViewById(R.id.views_count_text);
            thumbnailViewsIcon = view.findViewById(R.id.views_icon);
            viewRatingParentLayout = view.findViewById(R.id.view_rating_parent);
            rentLayout = view.findViewById(R.id.iv_rent);
            fixedLayout.getLayoutParams().width = itemView.getResources().getDimensionPixelSize(R.dimen.continue_watching_width);
            mImageViewMovies.getLayoutParams().width = itemView.getResources().getDimensionPixelSize(R.dimen.continue_watching_width);
            view.setOnClickListener(this);
            view.setLongClickable(true);
            if(mImageViewMovies!=null){
                mImageViewMovies.setOnClickListener(this);
            }
            if(mTextViewMovieTitle!=null){
                mTextViewMovieTitle.setOnClickListener(this);
            }
            if (mImageViewPlayIcon!=null){
                mImageViewPlayIcon.setOnClickListener(this);
            }
            if (mImageViewContinueWatching!=null){
                mImageViewContinueWatching.setOnClickListener(this);
            }
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
                    CardData data = null;
                    if (getAdapterPosition() != -1) {
                        data = mListMovies.get(getAdapterPosition());
                    }
                    if (data == null) {
                        return false;
                    }
                    String title = data.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        return false;
                    }
                    AlertDialogUtil.showToastNotification(title);
                    return false;
                }
            });
        }

        public void setClickListener(ItemClickListenerWithData itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (this.clickListener != null && mListMovies != null && getAdapterPosition() != -1) {
                if(mListMovies.isEmpty())
                {
                }else {
                    this.clickListener.onClick(v, getAdapterPosition(), mParentPosition, mListMovies.get(getAdapterPosition()));
                }}
        }

    }

    // Overriding this method to get access to recyclerView on which current MovieAdapter has been attached.
    // In the future we will use that reference for scrolling to newly added relatedMovies item
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }

    private ItemClickListenerWithData mOnItemClickListenerWithData;

    public void setOnItemClickListenerWithMovieData(ItemClickListenerWithData onItemClickListenerWithData) {
        this.mOnItemClickListenerWithData = onItemClickListenerWithData;
    }

    public void setCarouselInfoData(String name, int pageSize) {
        if(!TextUtils.isEmpty(mPageName)){
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
//        new MenuDataModel().fetchCarouseldataInAsynTask(mContext, mPageName, 1, mPageSize, true,this);
    }

    public void setTextureData(List<TextureItem> texture) {
        this.texture = texture;
    }


    public final ItemClickListenerWithData mOnItemClickListenerDefault = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            Analytics.gaEventBrowsedCategoryContentType(carouselData);

            if (carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                //Log.d(TAG,"type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                showRelatedVODListFragment(carouselData);
                return;
            }

            if(position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA){
                return;
            }

            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;

            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, null, null);
                return;
            }

            if (carouselData != null
                    && carouselData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !TextUtils.isEmpty(carouselData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink,APIConstants.TYPE_SPORTS,carouselData.generalInfo.title));
                return;
            }
            showDetailsFragment(carouselData);

        }

    };

    private boolean isSonyLiveContent(CardData data) {
        if(data == null
                || data.publishingHouse == null){
            return false;
        }

        if(APIConstants.TYPE_SONYLIV.equals(data.publishingHouse.publishingHouseName)){
            return true;
        }
        return false;
    }

    private void showRelatedVODListFragment(CardData cardData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));
    }

    private void showDetailsFragment(CardData carouselData) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        String _id = carouselData._id;
        if (carouselData.generalInfo != null
                && APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                && carouselData.globalServiceId != null) {
            _id = carouselData.globalServiceId;
        }

        args.putString(CardDetails.PARAM_CARD_ID, _id);

        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);

        if(carouselData != null
                && carouselData.generalInfo != null){
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)){
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
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, isSonyLiveContent(carouselData) ? CardDetails.Partners.SONY : CardDetails.Partners.APALYA);

        if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                && !APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                && !isContinueWatchingSection
                && !carouselData.isTVSeries()
                && !carouselData.isVODChannel()
                && !carouselData.isVODYoutubeChannel()
                && !carouselData.isVODCategory()
                && !carouselData.isTVSeason()) {
            CacheManager.setCardDataList(mListMovies);
        }


        if (carouselData.generalInfo != null
                && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
            args.putBoolean(CardDetails.PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL, true);
        }

        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);

        ((BaseActivity) mContext).showDetailsFragment(args,carouselData);
    }


    public final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_SQUARE_BANNER,APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_THUMBNAIL};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    private String getPartnerImageLink(CardData carouselData) {
        if (mContext != null) {
            PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
            String partnerName=(carouselData.publishingHouse!=null&&!TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName))?carouselData.publishingHouse.publishingHouseName:carouselData.contentProvider;
            if (!TextUtils.isEmpty(partnerName)&&partnerDetailsResponse != null && carouselData != null ) {
                for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                    if (partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                        return partnerDetailsResponse.partnerDetails.get(i).imageURL;
                    }
                }
            }
        }
        return null;
    }

    public void showViewRatingOnThumbnail(ContinueWatchingHorizontalCarousel.CarouselDataViewHolder holder, CardData cardData){
        if(cardData.stats != null && cardData.generalInfo.displayStatistics){
            if (TextUtils.isEmpty(cardData.stats.getViewCount())) {
                holder.thumbnailViewsIcon.setVisibility(View.GONE);
                holder.viewsCountText.setVisibility(View.GONE);
            } else {
                holder.viewRatingParentLayout.setVisibility(View.VISIBLE);
                holder.thumbnailViewsIcon.setVisibility(View.VISIBLE);
                holder.viewsCountText.setVisibility(View.VISIBLE);
                holder.viewsCountText.setText(cardData.stats.getViewCount());
            }
        }else {
            holder.thumbnailViewsIcon.setVisibility(View.GONE);
            holder.viewsCountText.setVisibility(View.GONE);
        }
    }
}