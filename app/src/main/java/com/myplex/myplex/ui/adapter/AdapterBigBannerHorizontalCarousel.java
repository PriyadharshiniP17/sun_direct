package com.myplex.myplex.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.PartnerDetailsResponse;
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
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import java.util.Date;
import java.util.List;

public class AdapterBigBannerHorizontalCarousel extends RecyclerView.Adapter<AdapterBigBannerHorizontalCarousel.CarouselDataViewHolder>{

    private static final String TAG = AdapterBigBannerHorizontalCarousel.class.getSimpleName();

    private final Context mContext;
    private List<CardData> mListMovies;
    private boolean isContinueWatchingSection;
    //    private List<CarouselInfoData> mListCarouselInfo;
    private int mParentPosition;
    //adapter view click listener
    private int mPageSize;
    private String mPageName;
    private ProgressDialog mProgressDialog;
    private OnItemRemovedListener mOnItemRemovedListener;
    private RecyclerView recyclerViewReference;

    private View.OnClickListener mOnItemRemoveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeItem(view);
        }
    };
    private boolean isGenericLayout;
    private GenericListViewCompoment parentViewHolder;

    private boolean showTitle;
    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    private void removeItem(View view) {
        LoggerD.debugLogAdapter("removeItem view data mParentPosition- " + mParentPosition + " getTag- " + view.getTag());
        try {
            if (view != null
                    && view.getTag() != null
                    && view.getTag() instanceof Integer) {
                int pos = (int) view.getTag();
                Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
                mListMovies.remove(pos);
                notifyDataSetChanged();
            }
            if (mOnItemRemovedListener != null) {
                mOnItemRemovedListener.onItemRemoved(mParentPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AdapterBigBannerHorizontalCarousel(Context context, List<CardData> itemList,RecyclerView recyclerView) {
        mContext = context;
        mListMovies = itemList;
        recyclerViewReference = recyclerView;

    }

//    public boolean isContainingDummies(){
//        return mIsDummyData;
//    }

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

    public void addData(List<CardData> listMovies) {
        if (listMovies == null) {
            return;
        }

        //Log.d(TAG, "addData");
        if (mListMovies == null) {
            mListMovies = listMovies;
            notifyDataSetChanged();
            return;
        }
        mListMovies.addAll(listMovies);
        notifyDataSetChanged();

    }

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder;
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_big_banner_horizontal_carousel, parent, false);
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

//            holder.mImageViewProvideLogo.setVisibility(View.GONE);
            holder.mTextViewVODInfo1.setVisibility(View.GONE);
            holder.mTextViewVODInfo2.setVisibility(View.GONE);

            if (carouselData.generalInfo != null
                    && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                    && carouselData.publishingHouse != null
                    && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)) {
                holder.mImageViewProvideLogo.setVisibility(View.GONE);
//                Picasso.with(mContext).load(APIConstants.getErosNowMusicLogoUrl()).into(holder.mImageViewProvideLogo);
            } else {
                holder.mImageViewProvideLogo.setVisibility(View.VISIBLE);
            }
            StringBuilder genres = new StringBuilder();
            if (carouselData != null
                    && carouselData.content != null
                    && carouselData.content.genre != null
                    && carouselData.content.genre.size() > 0) {
                for (CardDataGenre genre : carouselData.content.genre) {
                    if (genres.length() != 0) {
                        genres.append(", ");
                    }
                    genres.append(genre.name);
                }
            }
            /*if (genres.length() > 0) {
                holder.mTextViewVODInfo2.setVisibility(View.VISIBLE);
                holder.mTextViewVODInfo2.setText(genres);
            }*/
            if (carouselData.generalInfo != null) {
                if (carouselData.generalInfo.title != null) {
                    holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
                    holder.mTextViewMovieTitle.setText(carouselData.generalInfo.title);
                }
                if (carouselData.generalInfo.briefDescription != null
                        && !carouselData.isMovie()) {
                    holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                    holder.mTextViewVODInfo1.setText(carouselData.generalInfo.briefDescription);
                }
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                        && !TextUtils.isEmpty(carouselData.globalServiceName)) {
                    holder.mTextViewMovieTitle.setText(carouselData.globalServiceName);
                }
                if (APIConstants.TYPE_NEWS.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    if (carouselData.generalInfo.altDescription != null && carouselData.generalInfo.altDescription.get(0) != null
                            && carouselData.generalInfo.altDescription.get(0).description != null
                            && !TextUtils.isEmpty(carouselData.generalInfo.altDescription.get(0).description)) {
                        holder.mTextViewMovieTitle.setText(carouselData.generalInfo.altDescription.get(0).description);
                    } else if (!TextUtils.isEmpty(carouselData.generalInfo.briefDescription)) {
                        holder.mTextViewMovieTitle.setText(carouselData.generalInfo.briefDescription);
                    }
                }
            }

            String imageLink = getImageLink(carouselData);
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.black);
            }

        }
        /*if (carouselData != null
                && carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type))) {
            holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
        }*/
        if(carouselData == null){
            holder.mImageViewPlayIcon.setVisibility(View.GONE);
        } else {
            holder.mImageViewPlayIcon.setVisibility(View.GONE);
            if(!Util.isFreeContent(carouselData)){
                holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
            } else {
                holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }

        if (carouselData != null&&carouselData.generalInfo!=null) {
            showViewRatingOnThumbnail(holder,carouselData);
        }

        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }

        holder.mThumbnailDelete.setVisibility(View.GONE);
        holder.mContinueWatchingProgress.setVisibility(View.GONE);
        if (isContinueWatchingSection) {
            holder.mThumbnailDelete.setVisibility(View.VISIBLE);
            holder.mThumbnailDelete.setTag(mListMovies.indexOf(carouselData));
            holder.mThumbnailDelete.setOnClickListener(mOnItemRemoveClickListener);
            holder.mContinueWatchingProgress.setVisibility(View.GONE);
            if (carouselData != null && carouselData.elapsedTime > 0) {
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

                    holder.mContinueWatchingProgress.setVisibility(View.VISIBLE);
                    holder.mContinueWatchingProgress.setProgress(percent);
                    holder.mTextViewVODInfo1.setVisibility(View.GONE);
                    if (carouselData.generalInfo.type != null) {
                        String contentType = "";
                        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                            contentType = HomePagerAdapter.getPageLivetv();
                        } else if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(carouselData.generalInfo.type)
                                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type)
                                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                                || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                                || (APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                                && isSonyLiveContent(carouselData))) {
                            contentType = HomePagerAdapter.getPageTvshows();
                        } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)) {
                            contentType = HomePagerAdapter.getPageMovies();
                        } else if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)) {
                            contentType = HomePagerAdapter.getPageYoutube();
                        } else if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)) {
                            contentType = HomePagerAdapter.getPageMusicVideos();
                        } else if (APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)) {
                            contentType = HomePagerAdapter.getPageMovieTrailer();
                        } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                                && isApalyaContent(carouselData)) {
                            contentType = HomePagerAdapter.getPageVideos();
                        }

                        holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                        holder.mTextViewVODInfo1.setText(contentType);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        if (carouselData != null && isGenericLayout) {
            String title = null;
            String info1 = "";
            String info2 = null;
            StringBuilder genres = new StringBuilder();
            StringBuilder languages = new StringBuilder();
            if (carouselData != null
                    && carouselData.content != null
                    && carouselData.content.genre != null
                    && carouselData.content.genre.size() > 0) {
                for (CardDataGenre genre : carouselData.content.genre) {
                    if (genres.length() != 0) {
                        genres.append(", ");
                    }
                    genres.append(genre.name);
                }
            }
            if (carouselData != null
                    && carouselData.content != null
                    && carouselData.content.language != null
                    && carouselData.content.language.size() > 0) {
                for (String language : carouselData.content.language) {
                    if (languages.length() != 0) {
                        languages.append(", ");
                    }
                    languages.append(language);
                }
            }
            LoggerD.debugHooqVstbLog("genres & languages- " + genres + " & " + languages);

            if (carouselData.generalInfo != null) {
                title = carouselData.generalInfo.title;
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                        && !TextUtils.isEmpty(carouselData.globalServiceName)) {
                    info2 = carouselData.globalServiceName;
//                    info1 = HomePagerAdapter.getPageLivetv();
                }
                if (APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    info1 = HomePagerAdapter.getPageLivetv();
                    info2 = String.valueOf(genres);
                } else if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                        || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(carouselData.generalInfo.type)
                        || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type)
                        || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                        || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                        || (APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                        && isSonyLiveContent(carouselData))) {
                    info1 = HomePagerAdapter.getPageTvshows();
                    info2 = String.valueOf(genres);
                } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    info1 = HomePagerAdapter.getPageMovies();
                    info2 = String.valueOf(genres);
                } else if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    info2 = HomePagerAdapter.getPageYoutube();
                    info1 = Util.getDurationWithFormat(carouselData.content.duration);
                } else if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    info2 = HomePagerAdapter.getPageMusicVideos();
                    holder.mTextViewVODInfo1.setVisibility(View.INVISIBLE);
                } else if (APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    info2 = HomePagerAdapter.getPageMovieTrailer();
                    holder.mTextViewVODInfo1.setVisibility(View.INVISIBLE);
                } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                        && isApalyaContent(carouselData)) {
                    holder.mTextViewVODInfo1.setVisibility(View.INVISIBLE);
                    info2 = HomePagerAdapter.getPageVideos();
                }
            }

            if(TextUtils.isEmpty(title)){
                holder.mTextViewMovieTitle.setVisibility(View.GONE);
            } else {
                holder.mTextViewMovieTitle.setText(title);
                holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
            }

            if(!TextUtils.isEmpty(info1)){
                holder.mTextViewVODInfo1.setText(info1);
                holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
            }

            if(TextUtils.isEmpty(info2)){
                holder.mTextViewVODInfo2.setVisibility(View.GONE);
            } else {
                holder.mTextViewVODInfo2.setText(info2);
                holder.mTextViewVODInfo2.setVisibility(View.VISIBLE);
            }

        }
        holder.mTextViewMovieTitle.setVisibility(View.GONE);
        if (showTitle) {
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
        }

//            }
//        });
        String partnerImageLink = carouselData.getPartnerImageLink(mContext);
        SDKLogger.debug("partnerImage " + partnerImageLink);
        if (!TextUtils.isEmpty(partnerImageLink)) {
            holder.mImageViewPartner.setVisibility(View.VISIBLE);
            //Picasso.with(mContext).load(partnerImageLink).resize(holder.mImageViewPartner.getLayoutParams().width, holder.mImageViewPartner.getLayoutParams().height).placeholder(R.drawable.epg_thumbnail_default).centerInside().into(holder.mImageViewPartner);
            PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,holder.mImageViewPartner,R.drawable.black,holder.mImageViewPartner.getLayoutParams().width, holder.mImageViewPartner.getLayoutParams().height);
        }else{
            holder.mImageViewPartner.setVisibility(View.GONE);
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


    public void setParentPosition(int position) {
        this.mParentPosition = position;
    }

    public void isGenericLayout(boolean b) {
        this.isGenericLayout = b;
    }

    public void setParentViewHolder(GenericListViewCompoment holder) {
        this.parentViewHolder = holder;
    }

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mThumbnailDelete;
        //        private final ImageView mImageViewRupeeIcon;
        private ItemClickListenerWithData clickListener;

        final ImageView mImageViewMovies;
        final ImageView mImageViewPlayIcon;
        final TextView mTextViewMovieTitle;
        final ImageView mImageViewProvideLogo;
        final TextView mTextViewVODInfo1;
        final TextView mTextViewVODInfo2;
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
            mImageViewMovies = (ImageView) view.findViewById(R.id.imageview_thumbnail_voditem);
            mImageViewPlayIcon = (ImageView)view.findViewById(R.id.thumbnail_movie_play);
            mTextViewVODInfo1 = (TextView) view.findViewById(R.id.vod_info1);
            mTextViewVODInfo2 = (TextView) view.findViewById(R.id.vod_info2);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.thumbnail_provider_app);
//            mImageViewRupeeIcon = (ImageView)view.findViewById(R.id.thumbnail_rupee_icon);
            mThumbnailDelete = (ImageView)view.findViewById(R.id.thumbnail_movie_delete_icon);
            mContinueWatchingProgress = (ProgressBar) view.findViewById(R.id.continue_watching_progress);
            mImageViewPartner= (ImageView) view.findViewById(R.id.iv_partener_logo_right);
            ratingCountText = view.findViewById(R.id.rating_count_text);
            thumbnailRatingIcon = view.findViewById(R.id.rating_icon);
            viewsCountText = view.findViewById(R.id.views_count_text);
            thumbnailViewsIcon = view.findViewById(R.id.views_icon);
            viewRatingParentLayout = view.findViewById(R.id.view_rating_parent);
            view.setOnClickListener(this);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
                    CardData data = mListMovies.get(getAdapterPosition());
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
            if(mImageViewMovies!=null){
                mImageViewMovies.setOnClickListener(this);
            }
            if(mTextViewMovieTitle!=null){
                mTextViewMovieTitle.setOnClickListener(this);
            }
            if (mImageViewPlayIcon!=null){
                mImageViewPlayIcon.setOnClickListener(this);
            }
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

    private void addCarouselData(final List<CardData> carouselList) {
        LoggerD.debugLog(TAG + " addCarouselData: mPageName- " + mPageName);
        if (carouselList == null) {
            return;
        }
        try {

            mListMovies = carouselList;
//            mIsDummyData = false;
            notifyDataSetChanged();
//            CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
//            carouselInfoData.listCarouselData = carouselList;
//            notifyItemChanged(position);
        } catch (IllegalStateException e) {
            //Occurs while we try to modify data of recycler view while it is scrolling
            /*mRecyclerViewCarouselInfo.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position);
                }
            });*/
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void isContinueWatchingSection(boolean b) {
        this.isContinueWatchingSection = b;
    }

    public void setRemoveItemListener(OnItemRemovedListener mOnItemRemovedListener) {
        this.mOnItemRemovedListener = mOnItemRemovedListener;
    }

    public final ItemClickListenerWithData mOnItemClickListenerDefault = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            Analytics.gaEventBrowsedCategoryContentType(carouselData);

            /*if (carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                //Log.d(TAG,"type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                showRelatedVODListFragment(carouselData);
                return;
            }*/

            if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
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

        if(APIConstants.TYPE_SONYLIV.equalsIgnoreCase(data.publishingHouse.publishingHouseName)){
            return true;
        }
        return false;
    }


    private boolean isApalyaContent(CardData data) {
        if(data == null
                || data.publishingHouse == null){
            return false;
        }

        if(APIConstants.TYPE_APALYA_VIDEOS.equalsIgnoreCase(data.publishingHouse.publishingHouseName)){
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

        if(carouselData == null){
            return;
        }

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
                && !carouselData.isTVSeries()
                && !carouselData.isVODChannel()
                && !carouselData.isVODYoutubeChannel()
                && !carouselData.isVODCategory()
                && !carouselData.isTVSeason()) {
            CacheManager.setCardDataList(mListMovies);
        }

        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));

        if (carouselData.generalInfo != null
                && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
            args.putBoolean(CardDetails.PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL, true);
        }
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);

        ((BaseActivity) mContext).showDetailsFragment(args,carouselData);
    }


    private String getImageLink(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_BANNER,
                APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }

                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
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

    public void showViewRatingOnThumbnail(CarouselDataViewHolder holder, CardData cardData){
        if(cardData.stats != null && cardData.generalInfo.displayStatistics&&!cardData.isYoutube()){
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
