package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.TextureItem;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.FixedAspectRatioRelativeLayout;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.List;

import autoscroll.RecyclingPagerAdapter;

/**
 * Created by samir on 1/14/2016.
 */
public class BigWeeklyTrendingAdapterNew extends RecyclingPagerAdapter {

    public static final int PAGE_COUNT_FACTOR = 100000;
    private LayoutInflater mInflater;
    private List<TextureItem> texture;
    public interface OnItemClickListener {
        void onItemClicked(CardData cardData);
    }

    private Context mContext;

    private List<CardData> mItems;
    List<CardDataPackages> userPackages;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private OnItemClickListener mOnItemClickListener;

    private int size;

    private boolean isInfiniteLoop = false;

    public BigWeeklyTrendingAdapterNew(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        userPackages = PrefUtils.getInstance().getPackages();
        SDKLogger.debug("constructor");
    }

    /**
     * @return the isInfiniteLoop
     */
    public boolean isInfiniteLoop() {
        return isInfiniteLoop;
    }

    /**
     * @param isInfiniteLoop the isInfiniteLoop to set
     */
    public BigWeeklyTrendingAdapterNew setInfiniteLoop(boolean isInfiniteLoop) {
        this.isInfiniteLoop = isInfiniteLoop;
        return this;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getCount() {
        return isInfiniteLoop ? mItems.size() * PAGE_COUNT_FACTOR : mItems.size();
    }

    public int getRealCount() {
        return mItems.size();
    }

    /**
     * get really position
     *
     * @param position
     * @return
     */
    private int getPosition(int position) {
        return isInfiniteLoop ? position % size : position;
    }

    public void setItems(List<CardData> mItems) {
        this.mItems = mItems;
        size = mItems.size();
    }
    public void setTextureData(List<TextureItem> texture) {
        this.texture = texture;
    }

/*
    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.size();
    }
*/

    @Override
    public View getView(int position, View convertView, ViewGroup collection) {
        final ViewHolder mViewHolder;
        convertView = mInflater.inflate(R.layout.big_weekly_trending_adapter_new, collection, false);
        mViewHolder = new ViewHolder();
        injectViews(mViewHolder, convertView, position);
        //TextView textView = (TextView) layout.findViewById(R.id.slider_title);
        final CardData sliderModel = getPosition(position) >= mItems.size() ? null : mItems.get(getPosition(position));
        mViewHolder.imageView.setTag(sliderModel);
        String imageLink = null;
        imageLink = getImageLink(sliderModel);
      //  mViewHolder.rentLayout.setVisibility(View.GONE);
      /*  if(cardData != null && cardData.generalInfo !=null && cardData.generalInfo.contentRights != null && cardData.generalInfo.contentRights.size()>0 && cardData.generalInfo.contentRights.get(0)!= null) {
            if (cardData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                mViewHolder.rentLayout.setVisibility(View.VISIBLE);
            }
        }*/
      /*  if(sliderModel != null && sliderModel.generalInfo !=null && sliderModel.generalInfo.contentRights != null && sliderModel.generalInfo.contentRights.size()>0 && sliderModel.generalInfo.contentRights.get(0)!= null) {
            if (sliderModel.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                mViewHolder.rentLayout.setVisibility(View.VISIBLE);
                if (sliderModel.packages != null && userPackages != null) {
                    for (CardDataPackages pack :
                            sliderModel.packages) {
                        for (CardDataPackages userPack : userPackages) {
                            if (pack != null && pack.packageId != null && userPack != null && userPack.packageId != null) {
                                if (pack.packageId.equalsIgnoreCase(userPack.packageId)) {
                                    mViewHolder.rentLayout.setVisibility(View.GONE);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }*/
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            mViewHolder.imageView.setImageResource(R.drawable
                    .black);
        } else if (imageLink != null) {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView, R.drawable.black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView, R.drawable.black);
            }
        }
        ImageView imageViewPlayIcon = (ImageView) convertView.findViewById(R.id.banner_play_icon);
        if (imageViewPlayIcon != null) {
          //  imageViewPlayIcon.setVisibility(View.GONE);
        }

          ImageView mImageViewFree= (ImageView) convertView.findViewById(R.id.iv_free_logo_left);


        if (sliderModel != null) {
            if (sliderModel.generalInfo != null
                    && sliderModel.generalInfo.title != null) {

    if(!sliderModel.generalInfo.isSellable){
            if(sliderModel.generalInfo.contentRights!=null){
                if(sliderModel.generalInfo.contentRights.contains("avod")){
                    mViewHolder.mImageViewFree.setVisibility(View.VISIBLE);
                }}else
                {
                    mViewHolder.mImageViewFree.setVisibility(View.GONE);
                }}
                if(sliderModel!=null && sliderModel.generalInfo != null && mViewHolder.userBadgeImage != null){
                    if(!TextUtils.isEmpty(sliderModel.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE){
                        mViewHolder.userBadgeImage.setVisibility(View.VISIBLE);
                        PicassoUtil.with(mContext).load(sliderModel.generalInfo.accessLabelImage,mViewHolder.userBadgeImage);
                    }else {
                        mViewHolder.userBadgeImage.setVisibility(View.GONE);
                    }
                }



                if (sliderModel.generalInfo != null && !TextUtils.isEmpty(sliderModel.generalInfo.title)){
                    mViewHolder.mTextViewMovieTitle.setText(sliderModel.generalInfo.title);
                }

                if (texture != null && texture.size() > 0) {
                    List<String> texturelist = new ArrayList<String>();
                    for (int i = 0; i < texture.size(); i++) {
                        texturelist.add(texture.get(i).metadata);
                    }
                    if (texturelist.contains("title")) {
                        mViewHolder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
                    }
                    if (texturelist.contains("language")) {
                        mViewHolder.mTextViewMovieLanguage.setVisibility(View.VISIBLE);
                    }
                    if (texturelist.contains("genre")) {
                        mViewHolder.mTextViewMovieGenre.setVisibility(View.VISIBLE);
                    }
                    if (texturelist.contains("rating")) {
                        mViewHolder.mRatingMovie.setVisibility(View.VISIBLE);
                    }
                    if (texturelist.contains("price")) {
                        mViewHolder.mTextViewMoviePrice.setVisibility(View.VISIBLE);

                    }
                    if (sliderModel.packages != null && sliderModel.packages.size() > 0) {
                        if (sliderModel.packages.get(0).priceDetails != null && sliderModel.packages.get(0).priceDetails.size() > 0) {
                            if(!sliderModel.generalInfo.isSellable){
                                if(sliderModel.generalInfo.contentRights!=null){
                                    if(sliderModel.generalInfo.contentRights.contains("avod")){
                                        mViewHolder.mTextViewMoviePrice.setText("Free ");
                               }else{ mViewHolder.mTextViewMoviePrice.setText("");
                                    }
                                }else{  mViewHolder.mTextViewMoviePrice.setText(" ");
                                }
                            }else{  mViewHolder.mTextViewMoviePrice.setText("₹ " + sliderModel.packages.get(0).priceDetails.get(0).price);
                            }

                        }
                    }
                    if(!sliderModel.generalInfo.isSellable){
                        if(sliderModel.generalInfo.contentRights!=null){
                            if(sliderModel.generalInfo.contentRights.contains("avod")){
                                mViewHolder.mTextViewMoviePrice.setText("Free ");
                            }
                        }
                    }
                    StringBuilder genres = new StringBuilder();
                    if (sliderModel != null
                            && sliderModel.content != null
                            && sliderModel.content.genre != null
                            && sliderModel.content.genre.size() > 0) {
                        for (CardDataGenre genre : sliderModel.content.genre) {
                            if (genres.length() != 0) {
                                genres.append(" | ");
                            }
                            genres.append(genre.name);
                        }
                    }
                   // mViewHolder.mTextViewGenres.setText(genres);
                    if(TextUtils.isEmpty(genres)){
                        mViewHolder.mTextViewMovieGenre.setText("");
                    }else{
                    mViewHolder.mTextViewMovieGenre.setText(" | " + genres);}
                    /*if (sliderModel.content != null&& sliderModel.content.contentRating!=null) {
                     mViewHolder.mRatingMovie.setRating(Float.parseFloat(sliderModel.content.contentRating));
                    }else{
                        mViewHolder.mRatingMovie.setRating((float) 4.5);
                    }*/
                        if (sliderModel.content.language != null && sliderModel.content.language.size() > 0) {
                    StringBuilder languageBuilder = new StringBuilder();
                    for (String language : sliderModel.content.language) {
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
                    }
                }
            }


            }
            String partnerImageLink = sliderModel.getPartnerImageLink(mContext);
            SDKLogger.debug("partnerImage " + partnerImageLink);
            if (!TextUtils.isEmpty(partnerImageLink)) {
                mViewHolder.iv_partner.setVisibility(View.VISIBLE);
                //Picasso.with(mContext).load(partnerImageLink).resize(holder.mImageViewPartner.getLayoutParams().width, holder.mImageViewPartner.getLayoutParams().height).placeholder(R.drawable.epg_thumbnail_default).centerInside().into(holder.mImageViewPartner);
                PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,mViewHolder.iv_partner,R.drawable.black,mViewHolder.iv_partner.getLayoutParams().width, mViewHolder.iv_partner.getLayoutParams().height);
            }else{
                mViewHolder.iv_partner.setVisibility(View.GONE);
            }

        }
        /*if (sliderModel != null
                && sliderModel.generalInfo != null
                && sliderModel.generalInfo.type != null
                && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_TRAILER.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(sliderModel.generalInfo.type))) {
            imageViewPlayIcon.setVisibility(View.VISIBLE);
        }*/
       /* if(sliderModel.ottApp != null && sliderModel.ottApp.offerDescription != null){
            textView.setText(sliderModel.ottApp.offerDescription);
        }
*/
        mViewHolder.imageView.setVisibility(View.VISIBLE);
        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                    }

                    mOnItemClickListener.onItemClicked(data);
                }
            }
        });
        SDKLogger.debug("sliderModel- " + sliderModel);
        mViewHolder.mContainer.setVisibility(View.GONE);
     //   mViewHolder.fixedAspectRatioRelativeLayout.getLayoutParams().height = convertView.getResources().getDimensionPixelSize(R.dimen.big_weekly_item_width);
     //   mViewHolder.imageView.getLayoutParams().height = convertView.getResources().getDimensionPixelSize(R.dimen.big_weekly_item_width);
        try {
            if (sliderModel != null
                    && sliderModel.isAdType()) {
               // if (mVmaxAdView == null) {
                    if (sliderModel != null
                            && sliderModel.generalInfo != null
                            && sliderModel.generalInfo._id != null) {
                        /*TODO: generalInfo._id contains 2 column':' seperated ids for portrait banners, i.e, 3:4 ratio banners, since this adapter applies for both 16:9 ratio banners for portrait banners in tab condition and normal banners so that we need to pick sencond value after ':' column otherwise if it doesn't contain column ':' it will pick actual value wich will be applicable for previous banners
                         */
                    }
               // }
                mViewHolder.mContainer.setVisibility(View.VISIBLE);

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private void injectViews(ViewHolder mViewHolder, View convertView, int position) {
        mViewHolder.imageView = (ImageView) convertView.findViewById(R.id.slider_image);


        mViewHolder.mTextViewMoviePrice = (TextView) convertView.findViewById(R.id.textview_price);
        mViewHolder.mRatingMovie = (RatingBar) convertView.findViewById(R.id.ratingbarbutton);
        mViewHolder.mTextViewMovieTitle = (TextView) convertView.findViewById(R.id.textview_movies_title);
        mViewHolder.mTextViewMovieGenre = (TextView)convertView.findViewById(R.id.textview_genre);
        mViewHolder.mTextViewMovieLanguage = (TextView)convertView.findViewById(R.id.textview_lanuage);
        mViewHolder.mImageViewFree=convertView.findViewById(R.id.iv_free_logo_left);
        mViewHolder.userBadgeImage = convertView.findViewById(R.id.content_badge);
        mViewHolder.iv_partner=convertView.findViewById(R.id.iv_partener_logo_right);
        mViewHolder.fixedAspectRatioRelativeLayout=convertView.findViewById(R.id.fixed_layout);
        mViewHolder.mContainer = (RelativeLayout) convertView.findViewById(R.id.ad_container);
        mViewHolder.rentLayout = convertView.findViewById(R.id.iv_rent);
        mViewHolder.rl_root = convertView.findViewById(R.id.rl_root);
        if(DeviceUtils.isTablet(mContext)) {
//            int Height =  (int) mContext.getResources().getDimensionPixelSize(R.dimen.big_weekly_item_height);
          //  int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mViewHolder.imageView.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_height_land);
                mViewHolder.imageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_width_land);
                mViewHolder.rl_root.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_width_land);
                mViewHolder.fixedAspectRatioRelativeLayout.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.big_weekly_item_tab_width_land);

            }
        }
    }

    private String getImageLink(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_BANNER, APIConstants.IMAGE_TYPE_THUMBNAIL,
                APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)){
                        return imageItem.link;
                    } else if(imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }

                } else {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)){
                        return imageItem.link;
                    } else if(imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }
        }
        return null;
    }



//            vmaxAdView.setTestDevices(VmaxAdView.TEST_via_ADVID, "efee1d0d-27e6-4095-8aaa-5b603d87d145");
//     vmaxAdView.setTestDevices(VmaxAdView.TEST_via_ADVID,"ae1ee71d-8d1d-4e8f-bf30-d7fb321918c2");


    private void setUpVmaxView(RelativeLayout layout) {
        String background = PrefUtils.getInstance().getVmaxLayoutBgColor();
        if (background == null) {
            background = "#000000";
        }
        layout.setBackgroundColor(Color.parseColor(background));
    }


    class ViewHolder {
        public ImageView imageView;
        public RelativeLayout mContainer;
        public TextView mTextViewMovieTitle;
        public TextView mTextViewMovieGenre;
        public TextView mTextViewMovieLanguage;
        public TextView mTextViewMoviePrice;
        public RatingBar mRatingMovie;
        public ImageView mImageViewFree;
        public ImageView userBadgeImage;
        public ImageView iv_partner;
        public FixedAspectRatioRelativeLayout fixedAspectRatioRelativeLayout;
        private LinearLayout rentLayout;
        private RelativeLayout rl_root;

    }

}
