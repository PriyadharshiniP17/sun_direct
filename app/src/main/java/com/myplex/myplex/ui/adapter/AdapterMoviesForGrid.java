package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FavouriteCheckRequest;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.api.request.content.WatchListRequest;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.TextureItem;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.events.RefreshPotraitUI;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by apalya on 12/20/2016.
 */

public class AdapterMoviesForGrid extends BaseAdapter {

    private static final String TAG = AdapterMovieList.class.getSimpleName();
    private Context mContext;
    private List<CardData> mListMovies;
    private boolean showTitle = true;
    private String mBgColor;
    private CarouselInfoData carouselInfoData;
    private String layoutType = APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER;
    private boolean deleteEnabled = false;
    private List<TextureItem> texture;

    public void setTabName(String[] tabName) {
        this.tabName = tabName;
    }

    private String[] tabName;

    private int requestType;

    public AdapterMoviesForGrid(Context context, List<CardData> moviesList) {
        mContext = context;
        mListMovies = moviesList;
    }

    public AdapterMoviesForGrid(Context context, List<CardData> moviesList, boolean showTitle, String layoutType, int requestType) {
        mContext = context;
        mListMovies = moviesList;
        this.showTitle = showTitle;
        this.layoutType = layoutType;
        this.requestType = requestType;
    }

    @Override
    public int getCount() {
        if (mListMovies != null && mListMovies.size() >= 1)
            return mListMovies.size();
        else
            return 0;
    }

    @Override
    public CardData getItem(int position) {
        return mListMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void add(List<CardData> vodCardList) {
        if (mListMovies != null) {
            mListMovies.addAll(vodCardList);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_movie_grid, null, false);
            mViewHolder = new ViewHolder();
            mViewHolder.mThumbnailMovie = (ImageView) convertView.findViewById(R.id.thumbnail_movie);
            mViewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.textview_movies_title);
            mViewHolder.mTextViewMovieprice = (TextView) convertView.findViewById(R.id.textview_price);
            mViewHolder.mRatingBarViewMovieRating = convertView.findViewById(R.id.RRratingbar);
            mViewHolder.mTextViewMovieGenre = (TextView) convertView.findViewById(R.id.textview_genre);
            mViewHolder.mTextViewMovieLanguage = (TextView) convertView.findViewById(R.id.textview_lanuage);

            mViewHolder.mImageViewPlayIcon = (ImageView) convertView.findViewById(R.id.thumbnail_movie_play);
            mViewHolder.mRippleOverlay = convertView.findViewById(R.id.overlay_ripple);
            mViewHolder.mImageViewPartner = (ImageView) convertView.findViewById(R.id.iv_partener_logo_right);
            mViewHolder.mImageViewRemove = (ImageView) convertView.findViewById(R.id.iv_delete);
            mViewHolder.ratingCountText = convertView.findViewById(R.id.rating_count_text);
            mViewHolder.thumbnailRatingIcon = convertView.findViewById(R.id.rating_icon);
            mViewHolder.viewsCountText = convertView.findViewById(R.id.views_count_text);
            mViewHolder.thumbnailViewsIcon = convertView.findViewById(R.id.views_icon);
            mViewHolder.viewRatingParentLayout = convertView.findViewById(R.id.view_rating_parent);
            mViewHolder.mCardView=convertView.findViewById(R.id.vod_item_main_background);
            mViewHolder.userBadgeImage = convertView.findViewById(R.id.content_badge);

//            mViewHolder.mImageViewRupeeIcon = (ImageView) convertView.findViewById(R.id.thumbnail_rupee_icon);
            mViewHolder.mImageViewFree = (ImageView) convertView.findViewById(R.id.iv_free_logo_left);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        int count = Util.getNumColumns(mContext);
        int width = (ApplicationController.getApplicationConfig().screenWidth / count) - (int) Util.convertDpToPixel(20, mContext);
        int height = width * 9 / 16;
        int placeholderid = R.drawable.black;
        if (isBigLayoutType()) {
             width = (ApplicationController.getApplicationConfig().screenWidth / 3) - (int) Util.convertDpToPixel(20, mContext);
            placeholderid = R.drawable.movie_thumbnail_placeholder;
            mViewHolder.mTextViewTitle.setVisibility(View.GONE);
            height = width * 3/ 2;
        }
        mViewHolder.mThumbnailMovie.getLayoutParams().height = height;
        mViewHolder.mThumbnailMovie.requestLayout();
        mViewHolder.mRippleOverlay.getLayoutParams().height = height;
        mViewHolder.mRippleOverlay.getLayoutParams().width = width;
        //Picasso.with(mContext).load(placeholderid).error(placeholderid).placeholder(placeholderid).resize(width, height).into(mViewHolder.mThumbnailMovie);
        PicassoUtil.with(mContext).load(placeholderid, mViewHolder.mThumbnailMovie, placeholderid, width, height);

        final CardData cardData = mListMovies.get(position);
        if (null == cardData) {
            return convertView;
        }
        height = width * 9 / 16;
        String imageType = APIConstants.IMAGE_TYPE_COVERPOSTER;
        if (isBigLayoutType()) {
            width = (ApplicationController.getApplicationConfig().screenWidth / 3) - (int) Util.convertDpToPixel(20, mContext);
            mViewHolder.mTextViewTitle.setVisibility(View.GONE);
            imageType = APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER;
            height = width * 3 / 2;
        }
        SDKLogger.debug("imageType- " + imageType);
        mViewHolder.mThumbnailMovie.getLayoutParams().height = height;
        mViewHolder.mThumbnailMovie.requestLayout();
        final String imageLink = getImageLink(cardData);
        if (imageLink == null
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            mViewHolder.mThumbnailMovie.getLayoutParams().height = height;
            mViewHolder.mThumbnailMovie.requestLayout();
            //Picasso.with(mContext).load(placeholderid).error(placeholderid).placeholder(placeholderid).resize(width, height).into(mViewHolder.mThumbnailMovie);
            PicassoUtil.with(mContext).load(placeholderid, mViewHolder.mThumbnailMovie, placeholderid, width, height);
        } else if (imageLink != null) {
            PicassoUtil.with(mContext).load(imageLink, mViewHolder.mThumbnailMovie, placeholderid, width, height);
            //Picasso.with(mContext).load(imageLink).error(placeholderid).placeholder(placeholderid).resize(width, height).into(mViewHolder.mThumbnailMovie);
        }
        if (showTitle) {
            mViewHolder.mTextViewTitle.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.mTextViewTitle.setVisibility(View.GONE);
        }

        if (mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
            mViewHolder.mTextViewTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));

            if (mViewHolder.mCardView!=null){
                mViewHolder.mCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
        }

        if(cardData.generalInfo != null && mViewHolder.userBadgeImage != null){
            if(!TextUtils.isEmpty(cardData.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE){
                mViewHolder.userBadgeImage.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).load(cardData.generalInfo.accessLabelImage,mViewHolder.userBadgeImage);
            }else {
                mViewHolder.userBadgeImage.setVisibility(View.GONE);
            }
        }

        if (texture != null && texture.size() > 0) {
            List<String> texturelist = new ArrayList<String>();
            for (int i = 0; i < texture.size(); i++) {
                texturelist.add(texture.get(i).metadata);
            }

            if (texturelist.contains("title")) {
                mViewHolder.mTextViewTitle.setVisibility(View.GONE);
            } else {
                mViewHolder.mTextViewTitle.setVisibility(View.GONE);
            }
            if (texturelist.contains("language")) {
                mViewHolder.mTextViewMovieLanguage.setVisibility(View.GONE);
            } else {
                mViewHolder.mTextViewMovieLanguage.setVisibility(View.GONE);
            }
            if (texturelist.contains("genre")) {
                mViewHolder.mTextViewMovieGenre.setVisibility(View.GONE);
            } else {
                mViewHolder.mTextViewMovieGenre.setVisibility(View.GONE);
            }
            if (texturelist.contains("rating")) {
                mViewHolder.mRatingBarViewMovieRating.setVisibility(View.GONE);
            } else {
                mViewHolder.mRatingBarViewMovieRating.setVisibility(View.GONE);
            }
            if (texturelist.contains("price")) {
                mViewHolder.mTextViewMovieprice.setVisibility(View.GONE);
            } else {
                mViewHolder.mTextViewMovieprice.setVisibility(View.GONE);
            }

        }
        if (cardData != null && null != cardData.generalInfo) {
            if (null != cardData.generalInfo.title) {
                if (isBigLayoutType()) {
                    mViewHolder.mTextViewTitle.setVisibility(View.GONE);
                } else {
                    mViewHolder.mTextViewTitle.setVisibility(View.VISIBLE);
                }
                mViewHolder.mTextViewTitle.setText(cardData.generalInfo.title);
            }

            if (null != cardData && null != cardData.content && null != cardData.content.language && cardData.content.language.size() > 0) {
                StringBuilder languageBuilder = new StringBuilder();
                for (String language : cardData.content.language) {
                    if (languageBuilder.length() > 0) {
                        languageBuilder.append("| ");
                    }
                    if (!TextUtils.isEmpty(language)) {
                        String lang = language.substring(0, 1).toUpperCase() + language.substring(1);
                        languageBuilder.append(lang);
                    }
                }
                if (languageBuilder.length() != 0) {
                    mViewHolder.mTextViewMovieLanguage.setText(languageBuilder.toString());
                    if (cardData.content != null && cardData.content.genre.size() > 0) {
                        mViewHolder.mTextViewMovieGenre.setText("| " + cardData.getGenre());
                    } else {
                        mViewHolder.mTextViewMovieGenre.setVisibility(View.GONE);
                    }
                }
            }
            if (null != cardData.packages && cardData.packages.size() > 0) {
                if (cardData.packages.get(0).priceDetails != null && cardData.packages.get(0).priceDetails.size() > 0) {
                    if (!cardData.generalInfo.isSellable) {
                        if (cardData.generalInfo.contentRights != null) {
                            if (cardData.generalInfo.contentRights.contains("avod")) {
                                mViewHolder.mTextViewMovieprice.setText("Free ");
                            } else {
                                mViewHolder.mTextViewMovieprice.setText(" ");
                            }
                        } else {
                            mViewHolder.mTextViewMovieprice.setText(" ");
                        }
                    } else {
                        mViewHolder.mTextViewMovieprice.setText("₹ " + cardData.packages.get(0).priceDetails.get(0).price);
                    }
                }
            }
            if (cardData != null && cardData.generalInfo != null) {
                if (!cardData.generalInfo.isSellable) {
                    if (cardData.generalInfo.contentRights != null) {
                        if (cardData.generalInfo.contentRights.contains("avod")) {
                            mViewHolder.mTextViewMovieprice.setText("Free ");
                        }
                    }
                }
            }
            try{
                if (cardData.content != null && cardData.content.contentRating != null) {
                    mViewHolder.mRatingBarViewMovieRating.setRating(Float.parseFloat(cardData.content.contentRating));
                } else {
                    mViewHolder.mRatingBarViewMovieRating.setRating((float) 4.5);
                }
            }catch (NumberFormatException numberFormatException){
                numberFormatException.printStackTrace();
            }
        }


        if (cardData == null) {
            mViewHolder.mImageViewPlayIcon.setVisibility(View.GONE);
        } else {
            mViewHolder.mImageViewPlayIcon.setVisibility(View.GONE);
            if (!Util.isFreeContent(cardData)) {
                mViewHolder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
            } else {
                mViewHolder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }
        String partnerImageLink = cardData.getPartnerImageLink(mContext);
        SDKLogger.debug("partnerImage " + partnerImageLink);
        if (partnerImageLink != null && !TextUtils.isEmpty(partnerImageLink)) {
            mViewHolder.mImageViewPartner.setVisibility(View.VISIBLE);
/*            Picasso.with(mContext).load(partnerImageLink)
                    .resize(mViewHolder.mImageViewPartner.getLayoutParams().width,
                            mViewHolder.mImageViewPartner.getLayoutParams().height)
                    .placeholder(R.drawable.epg_thumbnail_default).centerInside()
                    .into(mViewHolder.mImageViewPartner);*/
            PicassoUtil.with(mContext).loadCenterInside(partnerImageLink, mViewHolder.mImageViewPartner, R.drawable.black, mViewHolder.mImageViewPartner.getLayoutParams().width,
                    mViewHolder.mImageViewPartner.getLayoutParams().height);
        } else {
            mViewHolder.mImageViewPartner.setVisibility(View.INVISIBLE);
        }


        if (cardData != null && cardData.generalInfo != null) {
            if (!cardData.generalInfo.isSellable) {
                if (cardData.generalInfo.contentRights != null) {
                    if (cardData.generalInfo.contentRights.contains("avod")) {
                        mViewHolder.mImageViewFree.setVisibility(View.VISIBLE);
                    } else {
                        mViewHolder.mImageViewFree.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            mViewHolder.mImageViewFree.setVisibility(View.GONE);
        }


        if (cardData != null && cardData.generalInfo != null) {
            showViewRatingOnThumbnail(mViewHolder, cardData);
        }


        if (mViewHolder.mImageViewRemove != null && isDeleteEnabled()) {
            mViewHolder.mImageViewRemove.setVisibility(View.VISIBLE);
            mViewHolder.mImageViewRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (requestType == APIConstants.WATCHLIST_FETCH_REQUEST) {
                        removeFromWatchList(cardData, position);
                    } else {
                        removeFavorite(cardData, position);
                    }
                }
            });
        } else if (mViewHolder.mImageViewRemove != null) {
            mViewHolder.mImageViewRemove.setVisibility(View.GONE);
        }

        return convertView;
    }


    private void removeFromWatchList(CardData cardData, int position) {
        String type = cardData.generalInfo.type;
        String _id = cardData._id;
        if (cardData != null
                && cardData.isVODYoutubeChannel()
                && cardData.isVODChannel()
                && cardData.isVODCategory()
                && cardData.isTVSeries()) {
            type = cardData.generalInfo.type;
            _id = cardData.globalServiceId;
        }
        if (cardData.isProgram()) {
            _id = cardData.globalServiceId;
            type = APIConstants.TYPE_LIVE;
        }
        WatchListRequest.Params watchListParams = new WatchListRequest.Params(_id, type);
        WatchListRequest mRequestFavourites = new WatchListRequest(watchListParams,
                new APICallback<FavouriteResponse>() {
                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        if (response.body().code == 402) {
                            PrefUtils.getInstance().setPrefLoginStatus("");
                            return;
                        }

                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            if (response.body().favorite) {
                                //TODO: Handle added to watch list condition
                                SDKLogger.debug("Added to watchlist");
                            } else {
                                //TODO: Handle remove from watchlist condition
                                PrefUtils.getInstance().shouldChangeFavouriteState(cardData._id, false, tabName);
                                PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER, true);
                                ScopedBus.getInstance().post(new RefreshPotraitUI());
                                SDKLogger.debug("Removed from watchlist");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);
        try {
            mListMovies.remove(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private void removeFavorite(CardData cardData, int position) {
        String type = cardData.generalInfo.type;
        String _id = cardData._id;
        if (cardData != null
                && cardData.isVODYoutubeChannel()
                && cardData.isVODChannel()
                && cardData.isVODCategory()
                && cardData.isTVSeries()) {
            type = cardData.generalInfo.type;
            _id = cardData.globalServiceId;
        }
        if (cardData.isProgram()) {
            _id = cardData.globalServiceId;
            type = APIConstants.TYPE_LIVE;
        }
        FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(_id, type);
        FavouriteRequest mRequestFavourites = new FavouriteRequest(favouritesParams,
                new APICallback<FavouriteResponse>() {
                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        if (response.body().code == 402) {
                            PrefUtils.getInstance().setPrefLoginStatus("");
                            return;
                        }

                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            if (response.body().favorite) {
                                //TODO: Handle added to watch list condition
                                SDKLogger.debug("Added to watchlist");
                            } else {
                                //TODO: Handle remove from watchlist condition
                                PrefUtils.getInstance().shouldChangeFavouriteState(cardData._id, false, tabName);
                                PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER, true);
                                ScopedBus.getInstance().post(new RefreshPotraitUI());
                                SDKLogger.debug("Removed from watchlist");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);
        try {
            mListMovies.remove(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private void executeContentDetailRequest(FavouriteCheckRequest.Params contentDetailsParams) {

        final FavouriteCheckRequest contentDetails = new FavouriteCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            if (response.body().favorite) {
                                //TODO: Handle added to watch list condition
                                SDKLogger.debug("Added to watchlist");
                            } else {
                                //TODO: Handle remove from watchlist condition
                                SDKLogger.debug("Removed from watchlist");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        String errorMessage = null;
                        String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                        if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                            reason = mAPIErrorMessage;
                        }
                    }
                });

        APIService.getInstance().execute(contentDetails);
    }

    private int getHeight(int width) {
        if (layoutType != null) {
            if (layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM)) {
                return width * 9 / 16;
            } else if (layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM)) {
                return width * 9 / 16;
            } else if (layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM)) {
                return width * 9 / 16;
            }
        }
        return width * 9 / 16;
    }

    private boolean isBigLayoutType() {
        return layoutType == null ? true : (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(layoutType) ||
                APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(layoutType) ||
                APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(layoutType))
                || (carouselInfoData != null
                && (carouselInfoData.isViewAllBigItemLayout()
                || carouselInfoData.isViewAllBigItemLayoutWithoutFilter()));
    }

    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public void setBgColor(String mBgColor){
        this.mBgColor=mBgColor;
    }

    public void setCarouselInfoData(CarouselInfoData carouselInfoData) {
        if (carouselInfoData != null){
            this.carouselInfoData = carouselInfoData;
        }
        if (carouselInfoData != null && carouselInfoData.texture != null){
            this.texture = carouselInfoData.texture;
        }
    }

    public boolean isDeleteEnabled() {
        return deleteEnabled;
    }

    public void setDeleteEnabled(boolean deleteEnabled) {
        this.deleteEnabled = deleteEnabled;
    }

    public class ViewHolder {
        ImageView mThumbnailMovie;
        TextView mTextViewTitle;
        TextView mTextViewMovieLanguage;
        TextView mTextViewMovieprice;
        TextView mTextViewMovieGenre;
        RatingBar mRatingBarViewMovieRating;
        TextView mTextViewDescription;
        ImageView mOverFlowMenu;
        ImageView mImageViewPlayIcon;
        View mRippleOverlay;
        ImageView mImageViewPartner;
        ImageView mImageViewRemove;
        private ImageView thumbnailRatingIcon;
        private ImageView thumbnailViewsIcon;
        private TextView ratingCountText;
        private TextView viewsCountText;
        CardView mCardView;
        private RelativeLayout viewRatingParentLayout;
        private ImageView userBadgeImage;
        //        public ImageView mImageViewRupeeIcon;
        private ImageView mImageViewFree;
    }

    public static int getTotalDuration(Date startDate, Date endDate, boolean isTotalDuration) {
        Date date = new Date();
        long diff;
        if (isTotalDuration) {
            diff = endDate.getTime() - startDate.getTime();
        } else {
            diff = date.getTime() - startDate.getTime();
        }
        double diffInHours = diff / ((double) 1000 * 60 * 60);

        int min = (int) Math.round(diffInHours * 60);

        return min;
    }


    /*public String getImageLink(CardData movie,String imageType) {
        if(movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()){
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,APIConstants.IMAGE_TYPE_COVERPOSTER};
        String[] profiles = new String[]{ApplicationConfig.XHDPI, ApplicationConfig.MDPI};
        if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageType)) {
            profiles = new String[]{ApplicationConfig.MDPI, ApplicationConfig.XHDPI};
        }
        //for(String imageType : imageTypes){
        for (String profile : profiles) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                //Log.d(TAG, "getImageLink: imageType: " + imageItem.type + " imageLink- " + imageItem.link + " imageprofile: " + imageItem.profile);
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && profile.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }
        //}

        return null;
    }*/

    public final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        if(isBigLayoutType()){
            String[] imageTypes = new String[]{
                    APIConstants.IMAGE_TYPE_PORTRAIT_BANNER,
                    APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,
                    APIConstants.IMAGE_TYPE_THUMBNAIL,
                    APIConstants.IMAGE_TYPE_THUMBNAIL_BANNER
            };
            for (String imageType : imageTypes) {
                for (CardDataImagesItem imageItem : carouselData.images.values) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }

        }else {
            String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER,
                    APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,
                    APIConstants.IMAGE_TYPE_THUMBNAIL, APIConstants.IMAGE_TYPE_THUMBNAIL_BANNER};
            for (String imageType : imageTypes) {
                for (CardDataImagesItem imageItem : carouselData.images.values) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }

        }



        return null;
    }

    public void showViewRatingOnThumbnail(AdapterMoviesForGrid.ViewHolder holder, CardData cardData) {
        if (cardData.stats != null && cardData.generalInfo.displayStatistics && !cardData.isYoutube()) {
            if (TextUtils.isEmpty(cardData.stats.getViewCount())) {
                holder.thumbnailViewsIcon.setVisibility(View.GONE);
                holder.viewsCountText.setVisibility(View.GONE);
            } else {
                holder.viewRatingParentLayout.setVisibility(View.VISIBLE);
                holder.thumbnailViewsIcon.setVisibility(View.VISIBLE);
                holder.viewsCountText.setVisibility(View.VISIBLE);
                holder.viewsCountText.setText(cardData.stats.getViewCount());
            }
        } else {
            holder.thumbnailViewsIcon.setVisibility(View.GONE);
            holder.viewsCountText.setVisibility(View.GONE);
        }
    }
}
