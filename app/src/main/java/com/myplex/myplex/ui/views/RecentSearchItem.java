package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.util.PrefUtils;
import com.myplex.util.StringManager;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterRecentSearch;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentSearchItem extends GenericListViewCompoment {


    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = RecentSearchItem.class.getSimpleName();
    private final RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselData;
    private View view;
    private CarouselInfoData carouselInfoData;
    private final RecyclerView.ItemDecoration mVerticalItemDecorator;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    AdapterRecentSearch mAdapterRecentSearch = null;
    private Typeface mBoldTypeFace;


    private RecentSearchItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInfo, String mMenuGroup, String mPageTitle) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        mVerticalItemDecorator = new VerticalSpaceItemDecoration(margin);
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
    }

    public static RecentSearchItem createView(Context context, ViewGroup parent,
                                                 List<CarouselInfoData> carouselInfoData, RecyclerView mRecyclerViewCarouselInfo,String mMenuGroup, String mPageTitle) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_carousel_linear_recycler, parent, false);
        return new RecentSearchItem(context, view, carouselInfoData, mRecyclerViewCarouselInfo, mMenuGroup,  mPageTitle);
    }
    @Override
    public void bindItemViewHolder(final int position) {
        this.position = position;
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        final CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
        if (carouselInfoData == null) {
            return;
        }
        if (carouselInfoData!=null&&carouselInfoData.showTitle) {
            if (carouselInfoData.title != null) {
                mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            } else {
                mTextViewGenreMovieTitle.setVisibility(View.GONE);
            }
        }
        holder.mLayoutViewAll.setTag(carouselInfoData);
        holder.mLayoutViewAll.setOnClickListener(mViewAllClickListener);
        holder.mLayoutViewAll.setVisibility(View.GONE);
        if (carouselInfoData.enableShowAll) {
            holder.mLayoutViewAll.setVisibility(View.VISIBLE);
            if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN){
                holder.mTextViewViewAllOtherLang.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.MORE))) {
                    holder.mTextViewViewAllOtherLang.setText(StringManager.getInstance().getString(APIConstants.MORE));
                }else{
                    holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
                }

            }else{
                holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(carouselInfoData.showAll)) {
                holder.mTextViewViewAll.setVisibility(View.VISIBLE);
                holder.mTextViewViewAll.setText(carouselInfoData.showAll);
            }
        }
        if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN && carouselInfoData.altTitle != null && !carouselInfoData.altTitle.isEmpty()){
            holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.VISIBLE);
            holder.mTextViewGenreMovieTitleOtherLang.setText(carouselInfoData.altTitle );

        }else{
            holder.mTextViewGenreMovieTitleOtherLang.setVisibility(View.GONE);
            holder.mTextViewViewAllOtherLang.setVisibility(View.GONE);
        }

        //holder.mLayoutCarouselTitle.setBackgroundColor(UiUtil.getColor(mContext, R.color.app_theme_color));
        /*if (!TextUtils.isEmpty(carouselInfoData.bgColor)) {
            try {
                holder.mLayoutCarouselTitle.setBackgroundColor(Color.parseColor(carouselInfoData.bgColor));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        holder.clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.getInstance().setString("PREF_SEARCH_STRING", "");
                notifyItemChanged();
            }
        });
        String imageLink = carouselInfoData.getLogoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        holder.mChannelImageView.setVisibility(View.GONE);
        if (imageLink == null || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.mChannelImageView.setVisibility(View.GONE);
        } else if (imageLink != null) {
            LoggerD.debugLog("carousel title imagelink- " + imageLink);
            holder.mChannelImageView.setVisibility(View.VISIBLE);
            if (imageLink.contains(APIConstants.PARAM_SCALE_WRAP)) {
                LoggerD.debugLog("carousel title-" + carouselInfoData.title + " imageLink- " + imageLink + " contains wrap");
                holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                if (DeviceUtils.isTablet(mContext)) {
                    holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    holder.mChannelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
                }
            }
            PicassoUtil.with(mContext).load(imageLink,holder.mChannelImageView);
        }
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ArrayList<String>  recentSearchList = null;
                String str1 = PrefUtils.getInstance().getString("PREF_SEARCH_STRING");
                if (str1 != null && !str1.equals("")) {
                    recentSearchList = new ArrayList<>(Arrays.asList(str1.split(",")));
                    //Collections.reverse(recentSearchList);
                }else{
                    removeItemFromParent(carouselInfoData);
                    return;
                }
                mAdapterRecentSearch = null;
                holder.clearHistory.setVisibility(View.VISIBLE);
                holder.mTextViewGenreMovieTitle.setVisibility(View.VISIBLE);
                if (holder.mRecyclerViewCarousel.getTag() instanceof AdapterRecentSearch) {
                    LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " getTag");
                    mAdapterRecentSearch = (AdapterRecentSearch) holder.mRecyclerViewCarousel.getTag();
                } else {
                    LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " create adapter");
                    mAdapterRecentSearch = new AdapterRecentSearch(mContext,recentSearchList);
                }
                mAdapterRecentSearch.showTitle(carouselInfoData.showTitle);
                LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " requestState- " + carouselInfoData.requestState);
                holder.mRecyclerViewCarousel.setItemAnimator(null);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, true);
                linearLayoutManager.setItemPrefetchEnabled(false);
                holder.mRecyclerViewCarousel.setLayoutManager(linearLayoutManager);
                holder.mRecyclerViewCarousel.removeItemDecoration(mVerticalItemDecorator);
                holder.mRecyclerViewCarousel.addItemDecoration(mVerticalItemDecorator);
                holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
                holder.mRecyclerViewCarousel.setTag(mAdapterRecentSearch);
                mAdapterRecentSearch.setOnItemClickListenerWithMovieData(mItemClickListener);
                mAdapterRecentSearch.setParentPosition(position);
                holder.mRecyclerViewCarousel.setAdapter(mAdapterRecentSearch);
            }
        };
        handler.postDelayed(r,250);

    }

    private com.myplex.myplex.model.ItemClickListener mItemClickListener = new com.myplex.myplex.model.ItemClickListener() {
       @Override
       public void onClick(View view, int position, int parentPosition, String name) {
           try {
              // ((MainActivity)mContext).setTextQuery(name);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   };

    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            PrefUtils.getInstance().setString("PREF_SEARCH_STRING", "");
            notifyItemChanged();
        }
    };
    private void removeItemFromParent(final CarouselInfoData carouselInfoData) {
        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        try {
            notifyItemRemoved(carouselInfoData);
        } catch (IllegalStateException e) {
            mRecyclerViewCarouselInfo.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(carouselInfoData);
                }
            });
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setSearchMoviedata(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        this.searchMovieData = searchMovieDataOnOTTApp;
    }
}
