package com.myplex.myplex.ui.views;

import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.views.circleindicator.DotsIndicator;
import com.myplex.myplex.ui.views.circleindicator.RecyclerViewCircleIndicator;
import com.myplex.myplex.ui.views.posterview.PosterRecyclerView;
import com.myplex.myplex.ui.views.posterview.StartSnapHelper;
import com.myplex.myplex.utils.ViewPagerCustomDurationNew;
import com.rd.PageIndicatorView;

import autoscroll.AutoScrollRecyclerView;
import autoscroll.AutoScrollViewPager;
import cardsliderviewpager.CardSliderViewPager;
import viewpagerindicator.CircleIndicator;

/**
 * Created by ramaraju on 11/24/2016.
 */

public abstract class GenericListViewCompoment extends UiCompoment implements View.OnClickListener {

    final TextView mTextViewGenreMovieTitleOtherLang;
    final TextView mTextViewViewAllOtherLang;
    final TextView mTextViewViewAll;
    ItemClickListener clickListener;
    final RecyclerView mRecyclerViewCarousel, recyclerViewEpisodes;
    final TextView mTextViewGenreMovieTitle;
    /*final TextView mClearTextTitle;*/
    final LinearLayout mLayoutViewAll;
    final RelativeLayout parentLayout;
    final RelativeLayout mLayoutViewAllRelative;
    final ImageView mChannelImageView;
    final RelativeLayout mLayoutCarouselTitle;
    final RelativeLayout mPreviewLayoutLL;
    final AutoScrollViewPager mViewPager;
    final TextView mDescriptionTxt;
    final LinearLayout mOfferDecriptionLayout;
    final ImageView mPreviewLayout;
    final CircleIndicator mIndicator;
    final RecyclerViewCircleIndicator mRecyclerViewCircleIndicator;
    final ListPlayerRecyclerView mListPlayerRecyclerCarousel;
    final AutoScrollRecyclerView animation3DRecyclerView;
    final TextView mTextViewErrorRetry;
    final ImageView mRightArrow;
    final ImageView mLeftArrow;
    final View leftGradient, rightGradient;
    final RelativeLayout gradientContainer;
    final RelativeLayout mViewPagerContainer;
    final ImageView mImageViewPartner;
    final ImageView iv_movie;
    final PosterRecyclerView mPosterRecyclerView;
    final ImageView mImageView;
    final AutoPlayRecyclerView mAutoPlayRecyclerView;
    final AutoPlayRecyclerViewSquare mAutoPlayRecyclerViewSquare;
    final SingleBannerAutoplayRecyclerview singleBannerPlayerRecycler;
    final ProgressBar mProgressBar;
    final TextView artistProfileHeadingText;
    final ImageView artistProfileBannerImage;
    final TextView artistProfileReadMoreText;
    final ImageView artistProfileSharImage;
    final ViewPager2 mViewPager2;
    final CarouselRecyclerview mViewPager3D;
    final ImageView mImageViewAll;
    final DotsIndicator mDotsIndicator;
    final TextView mTitle;
    final TextView mTitleLang;
    final  RelativeLayout mRelativeNavigationItem;
    final ImageView mImage;
    final  TextView countText;
    final LinearLayout countLayout;
    final TextView see_all_text;
    final PageIndicatorView pageIndicatorView;
    final CardSliderViewPager cardSliderViewPager;
    final FrameLayout adView;
    final TemplateView nativeAdTemplateView;
    final AppCompatImageView bannerBg;
    final TextView clearHistory;
    final TabLayout tabLayout, episodeTabLayout,seasonTabLayout;
    final ChipGroup channelChip;
    final RecyclerView channelItems;
    final View packs_description_div;
    final View packs_title_div;

    final ViewPagerCustomDurationNew viewPagerCustomDurationNew;
    public GenericListViewCompoment(View view) {
        super(view);

        itemView.setOnClickListener(this);
        mViewPager = itemView.findViewById(R.id.pager_ottapps);
        viewPagerCustomDurationNew = itemView.findViewById(R.id.viewPagerCustomDurationNew);
        if (mViewPager != null) {
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
        mTextViewGenreMovieTitle = view.findViewById(R.id.textview_genre_title);
        tabLayout = view.findViewById(R.id.tabLayout);
        episodeTabLayout = view.findViewById(R.id.episodeTabLayout);
        seasonTabLayout = view.findViewById(R.id.seasonTabLayout);
        channelChip=view.findViewById(R.id.channel_chip_group);
        channelItems=view.findViewById(R.id.channelItems);
      /*  mClearTextTitle=view.findViewById(R.id.textview_clear_all);*/
        mRecyclerViewCarousel = view.findViewById(R.id.recycler_view_movie);
        recyclerViewEpisodes = view.findViewById(R.id.recycler_view_episodes);
        mLayoutViewAllRelative = view.findViewById(R.id.layout_view_all_poster);
        mLayoutViewAll = view.findViewById(R.id.layout_view_all);
        parentLayout = view.findViewById(R.id.parent_layout);
        mPreviewLayoutLL = view.findViewById(R.id.previewLayoutLL);
        mTextViewGenreMovieTitleOtherLang = (TextView)view.findViewById(R.id.textview_other_lang_title) ;
        mChannelImageView = view.findViewById(R.id.toolbar_tv_channel_Img);
        mLayoutCarouselTitle = view.findViewById(R.id.layout_carousel_title);
        clearHistory = view.findViewById(R.id.clear_history);
        mDescriptionTxt = itemView.findViewById(R.id.slider_title);
        mOfferDecriptionLayout = itemView.findViewById(R.id.offer_description_layout);
        mPreviewLayout = (ImageView)itemView.findViewById(R.id.previewLayout);
        mIndicator = view.findViewById(R.id.view_pager_indicator);
        mRecyclerViewCircleIndicator = view.findViewById(R.id.recyclerIndicator);
        SnapHelper snapHelper = new StartSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerViewCarousel);
        mTextViewErrorRetry = (TextView) view.findViewById(R.id.textview_error_retry);
        mTextViewViewAllOtherLang = (TextView)view.findViewById(R.id.textview_view_all_other_lang) ;
        mTextViewViewAll = (TextView) view.findViewById(R.id.textview_view_all);
        mRightArrow = (ImageView) itemView.findViewById(R.id.viewpager_right_arrow);
        mLeftArrow = (ImageView) itemView.findViewById(R.id.viewpager_left_arrow);
        leftGradient = itemView.findViewById(R.id.left_gradient_view);
        rightGradient = itemView.findViewById(R.id.right_gradient_view);
        gradientContainer = (RelativeLayout) itemView.findViewById(R.id.grdient_container);
        mViewPagerContainer = (RelativeLayout) itemView.findViewById(R.id.pager_ottapps_layout);
        iv_movie = (ImageView) itemView.findViewById(R.id.imageview_thumbnail_voditem);
        mImageViewPartner = (ImageView) itemView.findViewById(R.id.iv_partener_logo_right);
        mPosterRecyclerView = view.findViewById(R.id.poster_rv);
        mImageView = (ImageView) itemView.findViewById(R.id.promo_image);
        mImageViewAll = itemView.findViewById(R.id.arrow_view_all);
        bannerBg = itemView.findViewById(R.id.banner_bg);
        mAutoPlayRecyclerView = (AutoPlayRecyclerView) itemView.findViewById(R.id.playerRecycler);
        mAutoPlayRecyclerViewSquare = itemView.findViewById(R.id.playerRecyclerSquare);
        mProgressBar  = itemView.findViewById(R.id.videoProgress);
        artistProfileBannerImage=itemView.findViewById(R.id.artistProfileBannerImage);
        artistProfileHeadingText=itemView.findViewById(R.id.artistProfileHeadingText);
        artistProfileReadMoreText=itemView.findViewById(R.id.artistProfileReadMoreText);
        artistProfileSharImage=itemView.findViewById(R.id.artistProfileShareImage);
        mListPlayerRecyclerCarousel = view.findViewById(R.id.recycler_view_movie_list);
        animation3DRecyclerView=view.findViewById(R.id.animation_recyclerview);
        singleBannerPlayerRecycler = (SingleBannerAutoplayRecyclerview) itemView.findViewById(R.id.singleBannerPlayerRecycler);
        mViewPager2 = itemView.findViewById(R.id.viewPagerAnim);
        mViewPager3D = itemView.findViewById(R.id.pager_ottapps_1);
        mDotsIndicator=itemView.findViewById(R.id.dots_indicator);
        mTitle = (TextView) view.findViewById(R.id.txtNavDrawerItemTitle);
        mImage = (ImageView) view.findViewById(R.id.txtNavDrawerItemImage);
        mTitleLang = (TextView) view.findViewById(R.id.txtNavDrawerItemAnotherLangTitle);
        mRelativeNavigationItem = (RelativeLayout) view.findViewById(R.id.relative_navigation_item);
        countText=view.findViewById(R.id.count_text);
        countLayout=view.findViewById(R.id.count_layout);
        see_all_text=view.findViewById(R.id.see_all_tv);
        pageIndicatorView=view.findViewById(R.id.pageIndicatorView);
        cardSliderViewPager=view.findViewById(R.id.cardSliderViewPager);
        adView=view.findViewById(R.id.ad_image_view);
        nativeAdTemplateView=view.findViewById(R.id.ad_banner_template_view);
        packs_description_div=view.findViewById(R.id.packs_description_div);
        packs_title_div=view.findViewById(R.id.packs_title_div);

    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (clickListener == null) return;
        this.clickListener.onClick(v, getAdapterPosition());
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }
}

