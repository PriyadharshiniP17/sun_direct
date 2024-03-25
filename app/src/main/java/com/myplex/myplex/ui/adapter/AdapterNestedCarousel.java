package com.myplex.myplex.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentLanguageInfo;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.util.AlertDialogUtil;

import java.util.List;


public class AdapterNestedCarousel extends RecyclerView.Adapter<AdapterNestedCarousel.CarouselDataViewHolder> {

    private static final String TAG = AdapterNestedCarousel.class.getSimpleName();

    private final Context mContext;
    private final CarouselInfoData mCarouselInfoData;
    private List<CarouselInfoData> mListMovies;
    //    private boolean mIsDummyData = false;
    private int mParentPosition;
    private String mPageName;
    private int mPageSize;
    private ProgressDialog mProgressDialog;
    private GenericListViewCompoment parentViewHolder;
    private RecyclerView recyclerViewReference;
    //adapter view click listener

    public AdapterNestedCarousel(Context context, CarouselInfoData carouselInfoData, RecyclerView recyclerView) {
        mContext = context;
        mCarouselInfoData = carouselInfoData;
        mListMovies = carouselInfoData.listNestedCarouselInfoData;
        recyclerViewReference = recyclerView;
//        ScopedBus.getInstance().register(this);
    }

/*
    public boolean isContainingDummies(){
        return mIsDummyData;
    }
*/

    private boolean isContinueWatchingSection;
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
//                Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
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



    public void setData(final List<CarouselInfoData> listMovies) {
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

    public void addData(List<CarouselInfoData> listMovies) {
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

        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_nested_carousel_item, parent, false);
        CarouselDataViewHolder customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListMovies.get(position));
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CarouselInfoData carouselData) {

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

        if (carouselData != null) {

            String imageLink = carouselData.getLogoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.black);
            }
        }
        holder.mImageViewProvideLogo.setVisibility(View.GONE);

        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }

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

        private ItemClickListenerWithData clickListener;
        final ImageView mImageViewProvideLogo;
        final ImageView mImageViewMovies;

        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view, true, R.color.list_item_bkg);

            mImageViewMovies = (ImageView) view.findViewById(R.id.imageview_thumbnail_voditem);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.iv_partener_logo_right);

            view.setOnClickListener(this);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
                    CarouselInfoData data = mListMovies.get(getAdapterPosition());
                    if (data == null) {
                        return false;
                    }
                    String title = data.title;
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

    private void addCarouselData(final List<CarouselInfoData> carouselList) {
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

    final ItemClickListenerWithData mOnItemClickListenerDefault = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CarouselInfoData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData.name == null) {
                return;
            }

            LoggerD.debugLog("carouselData.showAllLayoutType- " + carouselData.showAllLayoutType);
            if (APIConstants.LAYOUT_TYPE_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                showRelatedVODListFragment(carouselData);
                return;
            } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                showCarouselViewAllFragment(carouselData);
                return;
            } else if (carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
                try {
                    ((MainActivity) mContext).redirectToPage(carouselData.showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            } else if(APIConstants.LAYOUT_TYPE_BROWSE_CATEGORY_SCREEN.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                showLanguagesGridScreen(carouselData,false);
                return;
            } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                showLanguagesGridScreen(carouselData, true);
                return;
            }

            showVODListFragment(carouselData);
        }

    };

    private void showLanguagesGridScreen(CarouselInfoData carouselInfoData,boolean isGenre) {
        Bundle args = new Bundle();
        if (isGenre) {
            args.putBoolean(FragmentCarouselInfo.PARAM_IS_GENRE_ONLY, true);
            args.putString(FragmentCarouselInfo.PARAM_GENRE, carouselInfoData.title);
            args.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, null);
            if(mCarouselInfoData != null)
                args.putString(FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE, mCarouselInfoData.title);
        } else {
            args.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
            // args.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, null);
            args.putString(FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE, null);

            if(mCarouselInfoData != null)
                args.putString(FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE, mCarouselInfoData.title);
        }

        CacheManager.setCarouselInfoData(carouselInfoData);
        args.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.title);
        FragmentLanguageInfo fragment = FragmentLanguageInfo.newInstance(args);
        ((MainActivity) mContext).pushFragment(fragment);
    }

    private void showVODListFragment(CarouselInfoData carouselInfoData) {
        //TODO show VODListFragment from MainActivity with bundle

//        MenuDataModel.setSelectedMoviesCarouselList(carouselInfoData.name + "_" + 1, carouselInfoData.listCarouselData);

        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(carouselInfoData);

        if (!APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselInfoData.showAllLayoutType)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_CAROUSEL_GRID, true);
        }
/*
        if (!TextUtils.isEmpty(mMenuGroup)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mMenuGroup);
        }*/
        ((MainActivity) mContext).pushFragment(FragmentVODList.newInstance(args));
    }

    private void showRelatedVODListFragment(CarouselInfoData parentCarouselInfoData) {
        //TODO show RelatedVodListFragment from main activity context

        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(parentCarouselInfoData);


        args.putBoolean(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL, true);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));


    }

    private void showCarouselViewAllFragment(CarouselInfoData carouselInfoData) {
        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(carouselInfoData);

        /*if (mCarouselInfoData != null) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mCarouselInfoData.name);
            if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mCarouselInfoData.name)) {
                args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, APIConstants.TYPE_MOVIE);
            }
        }*/

        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    public interface ItemClickListenerWithData {
        void onClick(View view, int position, int parentPosition, CarouselInfoData movieData);
    }
}