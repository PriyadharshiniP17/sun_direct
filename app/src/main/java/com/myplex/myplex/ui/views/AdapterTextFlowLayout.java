package com.myplex.myplex.ui.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.chip.Chip;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.OnItemRemovedListener;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

public class AdapterTextFlowLayout extends RecyclerView.Adapter<AdapterTextFlowLayout.CarouselDataViewHolder> {
    private static final String TAG = AdapterTextFlowLayout.class.getSimpleName();
    private final Context mContext;
    private List<CardData> mListMovies;
    private boolean isContinueWatchingSection;
    //    private List<CarouselInfoData> mListCarouselInfo;
    private int mParentPosition;
    //adapter view click listener
    private CarouselInfoData mCarouselInfoData;
    private RecyclerView mRecyclerViewMovies;
    private int mPageSize;
    private String mPageName;
    private ProgressDialog mProgressDialog;
    private OnItemRemovedListener mOnItemRemovedListener;
    private RecyclerView recyclerViewReference;
    private String mBgColor;
    private boolean showTitle;
    private GenericListViewCompoment parentViewHolder;
    private ItemClickListenerWithData clickListener;
    private boolean isGenericLayout;

    public AdapterTextFlowLayout(Context context, List<CardData> listCarouselData, RecyclerView mRecyclerViewCarousel) {
        mContext = context;
        mListMovies = listCarouselData;
        recyclerViewReference = mRecyclerViewCarousel;
    }

    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
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

    private ItemClickListenerWithData mOnItemClickListenerWithData;

    public void setOnItemClickListenerWithMovieData(ItemClickListenerWithData onItemClickListenerWithData) {
        this.mOnItemClickListenerWithData = onItemClickListenerWithData;
    }

    public void setParentPosition(int position) {
        this.mParentPosition = position;
    }

    public void setParentViewHolder(GenericListViewCompoment holder) {
        this.parentViewHolder = holder;
    }

    public void setBgColor(String mBgColor) {
        this.mBgColor = mBgColor;
    }

    public void setCarouselInfoData(String name, int pageSize) {
        if (!TextUtils.isEmpty(mPageName)) {
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
//        new MenuDataModel().fetchCarouseldataInAsynTask(mContext,
    }

    public void setRemoveItemListener(OnItemRemovedListener mOnItemRemovedListener) {
        this.mOnItemRemovedListener = mOnItemRemovedListener;
    }

    public void isContinueWatchingSection(boolean b) {
        this.isContinueWatchingSection = b;
    }


    @NonNull
    @Override
    public CarouselDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        AdapterTextFlowLayout.CarouselDataViewHolder customItemViewHolder;
        view = LayoutInflater.from(mContext).inflate(R.layout.text_flow_channel_item, parent, false);
        customItemViewHolder = new AdapterTextFlowLayout.CarouselDataViewHolder(view);

        return customItemViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull CarouselDataViewHolder holder, int position) {
        bindGenreViewHolder(holder, mListMovies.get(position));
    }

    private void bindGenreViewHolder(CarouselDataViewHolder holder, CardData carouselData) {
        {
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

            if (carouselData != null) {

//            holder.mImageViewProvideLogo.setVisibility(View.GONE);

                if(carouselData.globalServiceName!=null && !TextUtils.isEmpty(carouselData.globalServiceName)){
                    holder.channel_chip_text.setText(carouselData.globalServiceName);
                }


                if (carouselData.generalInfo != null
                        && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                        && carouselData.publishingHouse != null
                        && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)) {

//                Picasso.with(mContext).load(APIConstants.getErosNowMusicLogoUrl()).into(holder.mImageViewProvideLogo);
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
                if (carouselData != null) {
                    if (carouselData.globalServiceName != null) {

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

                if (carouselData != null && carouselData.generalInfo != null) {
//                    showViewRatingOnThumbnail(holder, carouselData);
                }

                holder.setClickListener(mOnItemClickListenerWithData);
                if (mOnItemClickListenerWithData == null) {
                    holder.setClickListener(mOnItemClickListenerDefault);
                }

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

                        }
                    } catch (Exception e) {
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
                    } else if (APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)) {
                        info2 = HomePagerAdapter.getPageMovieTrailer();
                    } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                            && isApalyaContent(carouselData)) {
                        info2 = HomePagerAdapter.getPageVideos();
                    }
                }
            }
        }
    }

    private boolean isSonyLiveContent(CardData data) {
        if (data == null
                || data.publishingHouse == null) {
            return false;
        }

        if (APIConstants.TYPE_SONYLIV.equalsIgnoreCase(data.publishingHouse.publishingHouseName)) {
            return true;
        }
        return false;
    }


    private boolean isApalyaContent(CardData data) {
        if (data == null
                || data.publishingHouse == null) {
            return false;
        }

        if (APIConstants.TYPE_APALYA_VIDEOS.equalsIgnoreCase(data.publishingHouse.publishingHouseName)) {
            return true;
        }
        return false;
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
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, carouselData.generalInfo.title));
                return;
            }

            showDetailsFragment(carouselData);

        }

    };

    private void showDetailsFragment(CardData carouselData) {

        if (carouselData == null) {
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

        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
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

        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    public void setClickListener(ItemClickListenerWithData itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        if (mListMovies == null) return 0;
        return mListMovies.size();
    }

    public class CarouselDataViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {
        private ItemClickListenerWithData clickListener;
        final Chip channel_chip_text;

        public CarouselDataViewHolder(View view) {
            super(view);
            channel_chip_text=view.findViewById(R.id.channel_chip_text);
            if (channel_chip_text != null) {
                channel_chip_text.setOnClickListener(this);
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
}



