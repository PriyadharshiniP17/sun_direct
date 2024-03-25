package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataRelatedCastItem;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;

import java.util.List;

public class AdapterPillarItem extends RecyclerView.Adapter<AdapterPillarItem.CarouselDataViewHolder> {

    private static final String TAG = AdapterPillarItem.class.getSimpleName();

    private final Context mContext;
    private List<CardDataRelatedCastItem> mListMovies;
    private boolean isContinueWatchingSection;
    //    private List<CarouselInfoData> mListCarouselInfo;
    private int mParentPosition;
    //adapter view click listener
    private RecyclerView mRecyclerViewMovies;
    private int mPageSize;
    private String mPageName;
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
                Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,"");
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

    public AdapterPillarItem(Context context, List<CardDataRelatedCastItem> itemList, RecyclerView recyclerView) {
        mContext = context;
        mListMovies = itemList;
        recyclerViewReference = recyclerView;
    }

//    public boolean isContainingDummies(){
//        return mIsDummyData;
//    }

    public void setData(final List<CardDataRelatedCastItem> listMovies) {
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

    public void addData(List<CardDataRelatedCastItem> listMovies) {
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
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_rounded_artist_grid_item, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListMovies.get(position));
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CardDataRelatedCastItem carouselData) {

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

        if (carouselData != null) {
//            holder.mImageViewProvideLogo.setVisibility(View.GONE);
            if (carouselData.name != null) {

                holder.mTitle.setVisibility(View.VISIBLE);
                holder.mTitle.setText(carouselData.name);
            }
                if (carouselData.roles != null&&carouselData.roles.size()!=0) {
                    holder.mDescriptionTv.setVisibility(View.VISIBLE);
                    holder.mDescriptionTv.setText(carouselData.roles.get(0));
                }


            String imageLink = getImageLink(carouselData);
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageView.setImageResource(R.drawable
                        .black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageView, R.drawable.black);
            }

        }

//        holder.setClickListener(mOnItemClickListenerWithData);
//        if (mOnItemClickListenerWithData == null) {
//            holder.setClickListener(mOnItemClickListenerDefault);
//        }

        //holder.setIsRecyclable(false);
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

        //        private final ImageView mImageViewRupeeIcon;
        private ItemClickListenerWithData clickListener;
        private TextView mTitle;
        private TextView mDescriptionTv;
        private ImageView mImageView;


        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view, true, R.color.list_item_bkg);
            mImageView = view.findViewById(R.id.imageview_thumbnail_voditem);
            mTitle = view.findViewById(R.id.textview_title_show);
            mDescriptionTv = view.findViewById(R.id.vod_info1);
            view.setOnClickListener(this);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
                    CardDataRelatedCastItem data = mListMovies.get(getAdapterPosition());
                    if (data == null) {
                        return false;
                    }
                    String title = data.name;
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
            if (this.clickListener != null && mListMovies != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                //this.clickListener.onClick(v, getAdapterPosition(), mParentPosition, mListMovies.get(getAdapterPosition()));
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
        if (!TextUtils.isEmpty(mPageName)) {
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
//        new MenuDataModel().fetchCarouseldataInAsynTask(mContext, mPageName, 1, mPageSize, true,this);
    }

    private void addCarouselData(final List<CardDataRelatedCastItem> carouselList) {
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
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, carouselData.generalInfo.title));
                return;
            }

            //showDetailsFragment(carouselData);

        }

    };


    public final String getImageLink(CardDataRelatedCastItem carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_SQUARE};

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
}
