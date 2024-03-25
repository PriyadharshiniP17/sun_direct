package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Phani on 12/12/2015.
 */
public class AdapterVODList extends BaseAdapter {
    private static final String TAG = AdapterVODList.class.getSimpleName();
    private Context mContext;
    private List<CardData> mListVODData;
    private String page,continueLayoutType;
    private boolean showTitle = true;
    private boolean isSmallSquareItem;
    private boolean bottomPartnerLogoVisibility;
    private String bgColor;
    public ImageView mThumbnailDelete;
    private int mParentPosition;
    private OnItemRemovedListener mOnItemRemovedListener;



    public AdapterVODList(Context context, List<CardData> listVODData,String pageName,String layoutType) {
        mContext = context;
        mListVODData = listVODData;
        page=pageName;
        continueLayoutType = layoutType;
    }

    @Override
    public int getCount() {
        return mListVODData.size();
    }

    @Override
    public CardData getItem(int position) {
        return mListVODData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(!isSmallSquareItem) {
                convertView = inflater.inflate(R.layout.griditem_vodlist, null, false);
            } else {
                convertView = inflater.inflate(R.layout.listitem_movie_viewall_item, null, false);
                /*if(convertView.getLayoutParams().width>0)
                convertView.getLayoutParams().height=convertView.getLayoutParams().width;*/
            }
            mViewHolder = new ViewHolder();
            if(isSmallSquareItem)
                mViewHolder.rlParent = (RelativeLayout) convertView.findViewById(R.id.rl_parent);
            mViewHolder.mImageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageview_thumbnail_voditem);
            mViewHolder.mImageViewProvideLogo = (ImageView) convertView.findViewById(R.id.thumbnail_provider_app);
            mViewHolder.mImageViewPlayIcon = (ImageView) convertView.findViewById(R.id.thumbnail_movie_play);
            mViewHolder.mContinueWatchingProgress = (ProgressBar) convertView.findViewById(R.id.continue_watching_progress);
            mViewHolder.mTextViewInfo1 = (TextView) convertView.findViewById(R.id.vod_info1);
            mViewHolder.channelName=convertView.findViewById(R.id.channel_name);
            mViewHolder.channelNumber=convertView.findViewById(R.id.channel_number);
            mViewHolder.ll_channer_label =  convertView.findViewById(R.id.ll_channer_label);
            mViewHolder.mRippleOverlay = (FrameLayout) convertView.findViewById(R.id.overlay_ripple);
            mViewHolder.mChannelImage = (ImageView) convertView.findViewById(R.id.channelImage);
            mViewHolder.mImageViewPartnerLogo=(ImageView) convertView.findViewById(R.id.iv_partener_logo_right);
            mViewHolder.iv_partner = (ImageView) convertView.findViewById(R.id.iv_partener_logo_right);
            mViewHolder.mCardView=convertView.findViewById(R.id.vod_item_main_background);
            mThumbnailDelete = (ImageView)convertView.findViewById(R.id.thumbnail_movie_delete_icon);
            if(mViewHolder.mContinueWatchingProgress!=null) {
                final CardData cardData = mListVODData.get(position);
                if (continueLayoutType.equalsIgnoreCase("continueWatchingEPG") || continueLayoutType.equalsIgnoreCase("continueWatching")) {
                    mThumbnailDelete.setVisibility(View.VISIBLE);
                    mThumbnailDelete.setTag(mListVODData.indexOf(cardData));
                    mThumbnailDelete.setOnClickListener(mOnItemRemoveClickListener);
                    mViewHolder.mContinueWatchingProgress.setVisibility(View.VISIBLE);
                } else {
                    mThumbnailDelete.setVisibility(View.GONE);
                    mViewHolder.mContinueWatchingProgress.setVisibility(View.GONE);
                }
            }
            if(!isSmallSquareItem){
                mViewHolder.ratingCountText = convertView.findViewById(R.id.rating_count_text);
                mViewHolder.thumbnailRatingIcon = convertView.findViewById(R.id.rating_icon);
                mViewHolder.viewsCountText = convertView.findViewById(R.id.views_count_text);
                mViewHolder.thumbnailViewsIcon = convertView.findViewById(R.id.views_icon);
                mViewHolder.viewRatingParentLayout = convertView.findViewById(R.id.view_rating_parent);
            }
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        int count = Util.getNumColumns(mContext);
        if(isSmallSquareItem)
            count++;

        int spacing;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spacing = 15;
        }else
            spacing = 20;

        int width = (ApplicationController.getApplicationConfig().screenWidth / count) - (int)Util.convertDpToPixel(spacing,mContext);
        final CardData vodData = mListVODData.get(position);
        if (isSmallSquareItem) {
            String imageLink = vodData.getImageLink(APIConstants.IMAGE_TYPE_THUMBNAIL);
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                mViewHolder.mImageViewThumbnail.setImageResource(R.drawable
                        .black);
            } else {
                imageLink = imageLink.replace("epgimages/", "epgimagesV3/");
                Glide.with(mViewHolder.mImageViewThumbnail.getContext()).load(imageLink).placeholder(R.drawable.black).into(mViewHolder.mImageViewThumbnail);
//                PicassoUtil.with(mContext).load(imageLink, mViewHolder.mImageViewThumbnail, R.drawable.black);
            }


            if(vodData == null){
                mViewHolder.mImageViewPartnerLogo.setVisibility(View.GONE);
            } else {
                String partnerImageLink=getPartnerImageLink(vodData);
                SDKLogger.debug("partnerImage "+partnerImageLink);
                if(!TextUtils.isEmpty(partnerImageLink)){
                    mViewHolder.mImageViewPartnerLogo.setVisibility(View.VISIBLE);
                    //Picasso.with(mContext).load(partnerImageLink).placeholder(R.drawable.epg_thumbnail_default).resize(mViewHolder.mImageViewPartnerLogo.getLayoutParams().width,mViewHolder.mImageViewPartnerLogo.getLayoutParams().height).centerInside().into(mViewHolder.mImageViewPartnerLogo);
                    PicassoUtil.with(mContext).loadCenterInside(partnerImageLink, mViewHolder.mImageViewPartnerLogo, R.drawable.black,mViewHolder.mImageViewPartnerLogo.getLayoutParams().width,mViewHolder.mImageViewPartnerLogo.getLayoutParams().height);
                }else{
                    mViewHolder.mImageViewPartnerLogo.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

        if (vodData != null&&vodData.generalInfo!=null&&!isSmallSquareItem) {
            showViewRatingOnThumbnail(mViewHolder,vodData);
        }

        float ratio= ((float)9)/16;
        if (vodData != null && vodData.generalInfo != null && vodData.generalInfo.type != null
                && vodData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM) && !DeviceUtils.isTablet(mContext)) {
                ratio=((float)3)/4;
        }
/*        Picasso.with(mContext).load(R.drawable.placeholder_live_channel).error(R.drawable
                .placeholder_live_channel).placeholder(R.drawable
                .placeholder_live_channel).resize(width, width ).onlyScaleDown().into(mViewHolder.mImageViewThumbnail);*/
        PicassoUtil.with(mContext).loadOnlyScaleDown(R.drawable.black, mViewHolder.mImageViewThumbnail, R.drawable.black,width, width);
        if (vodData == null) {
            return convertView;
        }
        mViewHolder.mImageViewPartnerLogo.setVisibility(View.GONE);
        mViewHolder.mChannelImage.setVisibility(View.GONE);
        mViewHolder.mRippleOverlay.getLayoutParams().height = (int)(width * ratio);
        mViewHolder.mRippleOverlay.getLayoutParams().width = width;

        mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
        mViewHolder.channelName.setVisibility(View.GONE);
        mViewHolder.channelNumber.setVisibility(View.GONE);
        mViewHolder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
        if (vodData == null) {
            mViewHolder.mImageViewPlayIcon.setVisibility(View.GONE);
        } else {
            mViewHolder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
            if (!Util.isFreeContent(vodData)) {
                mViewHolder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
            } else {
               // mViewHolder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }
        mViewHolder.mImageViewThumbnail.getLayoutParams().height=(int)(width*ratio);
        mViewHolder.mImageViewThumbnail.requestLayout();
        String imageLink = getImageLink(vodData, mViewHolder.mImageViewThumbnail);
        if (imageLink != null) {
            imageLink = imageLink.replace("epgimages/", "epgimagesV3/");
            /*Picasso.with(mContext).load(imageLink).error(R.drawable
                    .epg_thumbnail_default).placeholder(R.drawable
                    .epg_thumbnail_default).resize(width, (int)(width * ratio)).onlyScaleDown().into(mViewHolder.mImageViewThumbnail);*/
            PicassoUtil.with(mContext).loadOnlyScaleDown(imageLink,mViewHolder.mImageViewThumbnail,R.drawable.black,width,(int)(width * ratio));
        }
        if (vodData.generalInfo != null && APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(vodData.generalInfo.type)) {
            if (!showTitle) {
                mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
                mViewHolder.channelName.setVisibility(View.GONE);
                mViewHolder.channelNumber.setVisibility(View.GONE);
            }
            return convertView;
        }
        if (vodData.generalInfo != null
                && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(vodData.generalInfo.type)
                && vodData.publishingHouse != null
                && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(vodData.publishingHouse.publishingHouseName)) {
            mViewHolder.mImageViewProvideLogo.setVisibility(View.GONE);
        } else {
            mViewHolder.mImageViewProvideLogo.setVisibility(View.GONE);
        }

        if (vodData.generalInfo != null && APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(vodData.generalInfo.type)) {

            if (vodData.generalInfo.briefDescription != null && vodData.content!=null && vodData.globalServiceName!=null) {
                mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
                mViewHolder.channelName.setVisibility(View.VISIBLE);
                mViewHolder.channelNumber.setVisibility(View.VISIBLE);
                mViewHolder.mTextViewInfo1.setText(vodData.generalInfo.briefDescription);
                mViewHolder.channelName.setText(vodData.globalServiceName);
                mViewHolder.channelNumber.setText(vodData.content.channelNumber);
            }
            if (!showTitle) {
                mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
                mViewHolder.channelName.setVisibility(View.GONE);
                mViewHolder.channelNumber.setVisibility(View.GONE);
            }
            return convertView;
        }

        if (vodData.generalInfo != null && vodData.generalInfo.type != null
                && vodData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
            String channelImageLink = getChannelImage(vodData, mViewHolder.mChannelImage);

            mViewHolder.mImageViewPartnerLogo.setVisibility(View.VISIBLE);

            mViewHolder.mChannelImage.setVisibility(View.VISIBLE);
            if (channelImageLink != null) {
                channelImageLink = channelImageLink.replace("epgimages/", "epgimagesV3/");
                mViewHolder.mChannelImage.setVisibility(View.VISIBLE);
                /*Picasso.with(mContext).load(channelImageLink).error(R.drawable
                        .epg_thumbnail_default).placeholder(R.drawable
                        .epg_thumbnail_default).into(mViewHolder.mChannelImage);*/
                PicassoUtil.with(mContext).load(channelImageLink,mViewHolder.mChannelImage,R.drawable.black);
            }
            String partnerImageLink=getPartnerImageLink(vodData);
            SDKLogger.debug("partnerImage "+partnerImageLink);
            if (!TextUtils.isEmpty(partnerImageLink)){
                mViewHolder.mImageViewPartnerLogo.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,mViewHolder.mImageViewPartnerLogo,R.drawable.black,mViewHolder.mImageViewPartnerLogo.getLayoutParams().width,mViewHolder.mImageViewPartnerLogo.getLayoutParams().height);
            }else{
                mViewHolder.mImageViewPartnerLogo.setVisibility(View.GONE);
            }
            if(vodData.generalInfo != null && vodData.generalInfo.title != null && vodData.globalServiceName != null) {
                showTitle=true;
                mViewHolder.mTextViewInfo1.setText(vodData.generalInfo.title);
                mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
                mViewHolder.channelName.setText(vodData.globalServiceName);
                mViewHolder.channelName.setVisibility(View.VISIBLE);
                mViewHolder.channelNumber.setText(vodData.content.channelNumber+".");
                mViewHolder.channelNumber.setVisibility(View.VISIBLE);
            }
            if(vodData.content!=null && vodData.content.channelNumber!=null && vodData.content.channelNumber!=null){
                mViewHolder.channelNumber.setText(vodData.content.channelNumber+".");
                mViewHolder.channelNumber.setVisibility(View.VISIBLE);
            }
            if (vodData.startDate != null && vodData.endDate != null) {
                mViewHolder.mContinueWatchingProgress.setVisibility(View.GONE);
//                mViewHolder.mContinueWatchingProgress.setMax(Util.getTotalDuration(vodData.startDate, vodData.endDate, true));
            } else {
//                mViewHolder.mContinueWatchingProgress.setMax(0);
            }
            if (vodData.startDate != null && vodData.startDate.length() > 0) {
//                mViewHolder.mContinueWatchingProgress.setProgress(Util.getTotalDuration(vodData.startDate, vodData.startDate, false));
            } else {
//                mViewHolder.mContinueWatchingProgress.setProgress(0);
            }
        }else if(APIConstants.LAYOUT_TYPE_WEEKLY_TRENDING_SMALL_ITEM.equalsIgnoreCase(continueLayoutType)){
            if(vodData.generalInfo != null && vodData.generalInfo.title != null) {
                showTitle=true;
                mViewHolder.channelName.setVisibility(View.VISIBLE);
                mViewHolder.channelName.setText(vodData.generalInfo.title);
              /*  mViewHolder.mTextViewInfo1.setVisibility(View.VISIBLE);
                mViewHolder.mTextViewInfo1.setText(vodData.generalInfo.title);*/
//                mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
            }

        }else{
            String partnerImageLink = getPartnerImageLink(vodData);
            SDKLogger.debug("partnerImage " + partnerImageLink);
            if(vodData.generalInfo != null && vodData.generalInfo.title != null && showTitle) {
                mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
                mViewHolder.mTextViewInfo1.setText(vodData.generalInfo.title);

            }
            if (bottomPartnerLogoVisibility && mViewHolder.iv_partner != null && !TextUtils.isEmpty(partnerImageLink)) {
                mViewHolder.iv_partner.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,mViewHolder.iv_partner,R.drawable.black,mViewHolder.iv_partner.getLayoutParams().width, mViewHolder.iv_partner.getLayoutParams().height);
            }else{
                mViewHolder.iv_partner.setVisibility(View.INVISIBLE);
            }
        }
        if (bgColor!=null&&!TextUtils.isEmpty(bgColor)){
            mViewHolder.mTextViewInfo1.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            if (mViewHolder.mCardView!=null){
                mViewHolder.mCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
        }
        if (vodData.generalInfo != null
                && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(vodData.generalInfo.type)
                && vodData.publishingHouse != null
                && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(vodData.publishingHouse.publishingHouseName)) {
            if (!TextUtils.isEmpty(vodData.globalServiceName)) {
                //mViewHolder.mTextViewInfo2.setVisibility(View.VISIBLE);
                //mViewHolder.mTextViewInfo2.setTextColor(mContext.getResources().getColor(R.color.white_50));
                //mViewHolder.mTextViewInfo2.setText(vodData.globalServiceName);
            }
        }
        if (!showTitle) {
            mViewHolder.mTextViewInfo1.setVisibility(View.GONE);
        }

        if(vodData.generalInfo!=null && vodData.generalInfo.title!=null) {
            LoggerD.debugLog("updatePlayerStatus: " +
                    "carouselData.elapsedTime: " + vodData.elapsedTime +
                    "id- " + vodData.generalInfo.title);
        }
            try {
                int position1 = vodData.elapsedTime;
                int duration = Util.calculateDurationInSeconds(vodData.content.duration);
                int percent = 0;
                if (duration > 0) {
                    // use long to avoid overflow
                    percent = (int) (100L * position1 / duration);
                }
                LoggerD.debugLog("updatePlayerStatus duration percent- " + percent);
//                        S2 | E1 | Pal Pal Dil Ke Paas
                int watchedMinutes = position1 / 60;
                int remainingduration = ((duration / 60) - watchedMinutes);
                LoggerD.debugLog("remainingDuration in minutes- " + remainingduration + " mins to go");
                mViewHolder.mContinueWatchingProgress.setProgress(percent);
//                mViewHolder.mContinueWatchingProgress.setProgress(vodData.elapsedTime);
                if (vodData.isMovie()) {
                    if (remainingduration > 0) {
                    }
                } else {
                    String title = vodData.getTitle();
                    if (vodData.isTVEpisode()) {
                        try {
                            String[] splitText = title.split(" " + Pattern.quote("|") + " ");
                            for (String token :
                                    splitText) {
                                LoggerD.debugDownload("\t token- " + token);
                            }
                            if (!TextUtils.isEmpty(vodData.globalServiceName)) {
                                if (splitText.length >= 2) {
                                    LoggerD.debugDownload("splitText- " + splitText + "\n1. " + splitText[0] + " \n2. " + splitText[1]);
                                }
                            } else if (splitText.length >= 2) {
                                LoggerD.debugDownload("splitText- " + splitText + "\n1. " + splitText[0] + " \n2. " + splitText[1]);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            LoggerD.debugDownload("\t exception message- " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mViewHolder.mContinueWatchingProgress.setProgress(0);
            }


        return convertView;
    }

    private String getImageLink(CardData mData, ImageView mPreviewImage) {

        String imageLink = null;
        if (mData == null || mData.images == null || mData.images.values == null || mData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_SQUARE_BANNER,APIConstants.IMAGE_TYPE_THUMBNAIL,APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : mData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)) {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) ||(APIConstants.IMAGE_TYPE_SQUARE_BANNER.equalsIgnoreCase(imageItem.type))
                            || (APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageItem.type))) {
                        if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
//                            mPreviewImage.setScaleType(null);
                            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            mPreviewImage.requestLayout();
                            mPreviewImage.invalidate();
                            return imageItem.link;
//                        mPreviewImage.setScaleType(Scale);
//                        android:scaleType="centerCrop"
                        } if (APIConstants.IMAGE_TYPE_SQUARE_BANNER.equalsIgnoreCase(imageItem.type) || APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageItem.type)) {
//                            mPreviewImage.setScaleType(null);
                            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            mPreviewImage.requestLayout();
                            mPreviewImage.invalidate();
                            return imageItem.link;
//                        mPreviewImage.setScaleType(Scale);
//                        android:scaleType="centerCrop"
                        }
                        if (DeviceUtils.isTablet(mContext)) {
                            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        } else {
                            mPreviewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        mPreviewImage.invalidate();
//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                        return imageItem.link;
                    }
                }
            }
        }
        return imageLink;
    }
    private String getChannelImage(CardData mData, ImageView mPreviewImage) {

        String imageLink = null;
        if (mData == null || mData.images == null || mData.images.values == null || mData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL, APIConstants.IMAGE_TYPE_THUMBNAIL,};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : mData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)) {
                    if (APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.HDPI.equalsIgnoreCase(imageItem.profile))) {
                        if (APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageItem.type)) {
//                            mPreviewImage.setScaleType(null);
                            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            mPreviewImage.requestLayout();
                            mPreviewImage.invalidate();
                            return imageItem.link;
//                        mPreviewImage.setScaleType(Scale);
//                        android:scaleType="centerCrop"
                        }
                        if (DeviceUtils.isTablet(mContext)) {
                            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        } else {
                            mPreviewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        mPreviewImage.invalidate();
//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                        return imageItem.link;
                    }
                }
            }
        }
        return imageLink;
    }
    public void addData(List<CardData> vodData) {
        if (mListVODData != null && vodData != null) {
            //not getting the all contents which are added in see all page with duplicates also except continue watching
            if(continueLayoutType!=null && (continueLayoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING) || continueLayoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING_EPG) || continueLayoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_CATCHUP))){
            for(CardData item: vodData){
                if(!checkLoadMoreDataPresent(item._id,mListVODData)){
                    mListVODData.add(item);
                }
            }
//            mListVODData.addAll(vodData);
            }else{
                mListVODData.addAll(vodData);
            }
            notifyDataSetChanged();
        }
    }

    public boolean checkLoadMoreDataPresent(String loadDataId, List<CardData> mTrailerCardData) {
        Iterator<CardData> iterator = mTrailerCardData.iterator();
        while (iterator.hasNext()) {
            CardData previousList = iterator.next();
            if (previousList._id.equalsIgnoreCase(loadDataId)) {
                return true;
            }
        }
        return false;
    }

    public List<CardData> getAllItems() {
        return mListVODData;
    }

    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public void setBgColor(String mBgColor){
        this.bgColor=mBgColor;
    }

    public void setIsSmallSquareItem(boolean isSmallSquareItem) {
        this.isSmallSquareItem = isSmallSquareItem;
    }

    public void setBottomPartnerLogoVisibility(boolean canShow) {
        this.bottomPartnerLogoVisibility=canShow;
    }

    public class ViewHolder {
        ImageView mImageViewThumbnail, mImageViewProvideLogo, mImageViewPlayIcon;
        TextView mTextViewInfo1,channelName,channelNumber;
        ImageView mChannelImage;
        ImageView mImageViewPartnerLogo;
        RelativeLayout rlParent;
        public ProgressBar mContinueWatchingProgress;
        ImageView iv_partner;
        CardView mCardView;
        public FrameLayout mRippleOverlay;
        private ImageView thumbnailRatingIcon;
        private ImageView thumbnailViewsIcon;
        private TextView ratingCountText;
        private TextView viewsCountText;
        private RelativeLayout viewRatingParentLayout;
        private LinearLayout ll_channer_label;
    }

    private String getPartnerImageLink(CardData carouselData) {
        if (mContext != null) {
            PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
            String partnerName = (carouselData.publishingHouse != null && !TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName)) ? carouselData.publishingHouse.publishingHouseName : carouselData.contentProvider;
            if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null && carouselData != null) {
                for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                    if (partnerDetailsResponse != null
                            && partnerDetailsResponse.partnerDetails != null
                            && partnerDetailsResponse.partnerDetails.get(i) != null
                            && !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
                            && partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                        return partnerDetailsResponse.partnerDetails.get(i).imageURL;
                    }
                }
            }
        }
        return null;
    }

    public void showViewRatingOnThumbnail(AdapterVODList.ViewHolder holder, CardData cardData){
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
    private View.OnClickListener mOnItemRemoveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeItem(view);
        }
    };
    public void removeItem(View view) {
        LoggerD.debugLogAdapter("removeItem view data mParentPosition- " + mParentPosition + " getTag- " + view.getTag());
        try {
            if (view != null
                    && view.getTag() != null
                    && view.getTag() instanceof Integer) {
                int pos = (int) view.getTag();
                if(mListVODData.get(pos).generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM) || mListVODData.get(pos).generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)){
                    Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListVODData.get(pos).globalServiceId,mListVODData.get(pos).getType());
                }else{
                    Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListVODData.get(pos)._id,mListVODData.get(pos).getType());
                }
                mListVODData.remove(pos);
                notifyDataSetChanged();
            }
            if (mOnItemRemovedListener != null) {
                mOnItemRemovedListener.onItemRemoved(mParentPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
