package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.ArtistProfileContentList;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.adapter.AdapterMedHorizontalCarousel;
import com.myplex.myplex.ui.adapter.AdapterSmallHorizontalCarousel;
import com.myplex.myplex.ui.adapter.OnItemRemovedListener;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

import static com.myplex.myplex.utils.Util.getDummyCardData;

public class ArtistProfileCarouselItem extends GenericListViewCompoment {

    private String TAG=ArtistProfileCarouselItem.class.toString();
    private List<ProfileAPIListAndroid> profileAPIListAndroids;
    private Context mContext;
    private RecyclerView mRecyclerViewCarouselInfo;
    private GenericListViewCompoment holder = this;
    private final RecyclerView.ItemDecoration mHorizontalMoviesDivieder;
    private String name = "";
    private Typeface mBoldTypeFace;

    public ArtistProfileCarouselItem(View view, Context context, List<ProfileAPIListAndroid> profileAPIListAndroids, RecyclerView mRecyclerViewCarouselInfo) {
        super(view);
        this.mContext = context;
        this.profileAPIListAndroids = profileAPIListAndroids;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");

    }

    public static ArtistProfileCarouselItem createView(Context context, ViewGroup parent, List<ProfileAPIListAndroid> profileAPIList,
                                                       RecyclerView mRecyclerViewCarouselInfo) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_carousel_linear_recycler_carousel_movies, parent, false);
        return new ArtistProfileCarouselItem(view, context, profileAPIList, mRecyclerViewCarouselInfo);
    }

    @Override
    public void bindItemViewHolder(int position) {
        this.position = position;
        if (profileAPIListAndroids == null || profileAPIListAndroids.size() == 0) {
            return;
        }
        final ProfileAPIListAndroid artistProfileCarouselItem = profileAPIListAndroids.get(position);
        if (artistProfileCarouselItem == null) {
            return;
        }
        if (profileAPIListAndroids.get(0).mArtistData!=null&&profileAPIListAndroids.get(0).mArtistData.generalInfo!=null
                &&profileAPIListAndroids.get(0).mArtistData.generalInfo.title!=null){
            name = profileAPIListAndroids.get(0).mArtistData.generalInfo.title;
        }
        String layoutType=profileAPIListAndroids.get(position).layoutType;
        holder.mTextViewViewAll.setVisibility(View.GONE);
        holder.mTextViewGenreMovieTitle.setVisibility(View.GONE);
        holder.mTextViewGenreMovieTitle.setText(artistProfileCarouselItem.displayName == null ? "" : artistProfileCarouselItem.displayName);
        mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
        startAsyncTaskInParallel(new CarouselRequestTask(artistProfileCarouselItem,layoutType));
    }

    private void startAsyncTaskInParallel(CarouselRequestTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CarouselRequestTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.Fr
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final ProfileAPIListAndroid profileAndroid;
        private final String layoutType;

        public CarouselRequestTask(ProfileAPIListAndroid carouselInfoData, String layoutType) {
            this.profileAndroid = carouselInfoData;
            this.layoutType=layoutType;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileAndroid.type,
                    1, profileAndroid.pageCount, name,
                    profileAndroid.publishingHouseId, profileAndroid.orderBy, "-1", profileAndroid.language, profileAndroid.tags);
            ArtistProfileContentList artistProfileMovieList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
                @Override
                public void onResponse(APIResponse<CardResponseData> response) {
                    if (response != null && response.body().results != null) {
                        addCarouselData(response.body().results, profileAndroid,layoutType);
                    }else {
                        addCarouselData(null, profileAndroid,layoutType);
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    addCarouselData(null, profileAndroid,layoutType);
                }
            });
            APIService.getInstance().execute(artistProfileMovieList);
            return null;
        }

    }

    private void addCarouselData(final List<CardData> carouselList, final ProfileAPIListAndroid carouselInfoData, String layoutType) {
        if (mRecyclerViewCarousel == null) {
            return;
        }
        if (carouselList == null || carouselList.size() == 0) {
            removeItemFromParent(carouselInfoData, position);
        } else {
            holder.mTextViewGenreMovieTitle.setVisibility(View.VISIBLE);
            if (carouselInfoData.viewAll && carouselList.size() >= carouselInfoData.pageCount) {
                holder.mTextViewViewAll.setVisibility(View.VISIBLE);
                holder.mTextViewViewAll.setTag(carouselInfoData);
                holder.mTextViewViewAll.setOnClickListener(mViewAllClickListener);
            }else {
                holder.mTextViewViewAll.setVisibility(View.GONE);
            }
            holder.mRecyclerViewCarousel.setItemAnimator(null);
            holder.mRecyclerViewCarousel.removeItemDecoration(mHorizontalMoviesDivieder);
            holder.mRecyclerViewCarousel.addItemDecoration(mHorizontalMoviesDivieder);
            holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            linearLayoutManager.setItemPrefetchEnabled(false);
            holder.mRecyclerViewCarousel.setLayoutManager(linearLayoutManager);
            if (layoutType != null && !TextUtils.isEmpty(layoutType)) {
                if(layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM)){
                    AdapterBigHorizontalCarousel adapterBigHorizontalCarousel = new AdapterBigHorizontalCarousel(mContext,
                            getDummyCardData(), holder.mRecyclerViewCarousel,"ArtistProfile");
                    adapterBigHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
                    adapterBigHorizontalCarousel.setParentPosition(position);
                    holder.mRecyclerViewCarousel.setTag(adapterBigHorizontalCarousel);
                    adapterBigHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
                    adapterBigHorizontalCarousel.showTitle(true);
                    holder.mRecyclerViewCarousel.setAdapter(adapterBigHorizontalCarousel);
                    adapterBigHorizontalCarousel.setData(carouselList);
                }else if (layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM)){
                    AdapterMedHorizontalCarousel adapterMedHorizontalCarousel = new AdapterMedHorizontalCarousel(mContext, getDummyCardData(), holder.mRecyclerViewCarousel);
                    adapterMedHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
                    adapterMedHorizontalCarousel.setParentPosition(position);
                    holder.mRecyclerViewCarousel.setTag(adapterMedHorizontalCarousel);
                    adapterMedHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
                    adapterMedHorizontalCarousel.showTitle(true);
                    holder.mRecyclerViewCarousel.setAdapter(adapterMedHorizontalCarousel);
                    adapterMedHorizontalCarousel.setData(carouselList);
                }else if (layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM)){
                    AdapterSmallHorizontalCarousel adapterSmallHorizontalCarousel = new AdapterSmallHorizontalCarousel(mContext, getDummyCardData(), holder.mRecyclerViewCarousel);
                    adapterSmallHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
                    adapterSmallHorizontalCarousel.setParentPosition(position);
                    holder.mRecyclerViewCarousel.setTag(adapterSmallHorizontalCarousel);
                    adapterSmallHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
                    adapterSmallHorizontalCarousel.showTitle(true);
                    holder.mRecyclerViewCarousel.setAdapter(adapterSmallHorizontalCarousel);
                    adapterSmallHorizontalCarousel.setData(carouselList);
                }
            }else {
                AdapterBigHorizontalCarousel adapterBigHorizontalCarousel = new AdapterBigHorizontalCarousel(mContext,
                        getDummyCardData(), holder.mRecyclerViewCarousel,"ArtistProfile");
                adapterBigHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
                adapterBigHorizontalCarousel.setParentPosition(position);
                holder.mRecyclerViewCarousel.setTag(adapterBigHorizontalCarousel);
                adapterBigHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
                adapterBigHorizontalCarousel.showTitle(true);
                holder.mRecyclerViewCarousel.setAdapter(adapterBigHorizontalCarousel);
                adapterBigHorizontalCarousel.setData(carouselList);
            }

        }
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, CardData carouselData) {
            CacheManager.setSelectedCardData(carouselData);
            Bundle args = new Bundle();
            args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
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
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.ARTIST_PROFILE);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, parentPosition + "");

            ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
        }
    };

    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getTag() instanceof ProfileAPIListAndroid){
                ProfileAPIListAndroid profileAPIListAndroid= (ProfileAPIListAndroid) v.getTag();
                showCarouselViewAllFragment(profileAPIListAndroid);
            }
        }
    };

    private void showCarouselViewAllFragment(ProfileAPIListAndroid profileAPIListAndroid) {
        Bundle args = new Bundle();
        //CacheManager.setCarouselInfoData(carouselInfoData);
        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE,FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_ARTIST_PROFILE);
        args.putSerializable(FragmentCarouselViewAll.PARAM_ARTIST_NAME,name);
        args.putSerializable(CleverTap.PROPERTY_PROFILE_API_LIST, profileAPIListAndroid);
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    private OnItemRemovedListener mOnItemRemovedListener = new OnItemRemovedListener() {
        @Override
        public void onItemRemoved(int mParentPosition) {
            if (profileAPIListAndroids == null) {
                LoggerD.debugLog("removeItem: invalid operation of removal carousel info is empty");
                return;
            }
            ProfileAPIListAndroid carouselInfoData = profileAPIListAndroids.get(mParentPosition);
            removeItemFromParent(carouselInfoData, mParentPosition);
        }
    };

    private void removeItemFromParent(final ProfileAPIListAndroid carouselInfoData, final int mParentPosition) {
        if (mRecyclerViewCarouselInfo == null) {
            return;
        }
        if (carouselInfoData == null) {
            // LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        mRecyclerViewCarouselInfo.post(new Runnable() {
            @Override
            public void run() {
                if (!mRecyclerViewCarouselInfo.isComputingLayout())
                    notifyItemRemoved(carouselInfoData);
                else
                    removeItemFromParent(carouselInfoData, mParentPosition);
            }
        });
    }


}
