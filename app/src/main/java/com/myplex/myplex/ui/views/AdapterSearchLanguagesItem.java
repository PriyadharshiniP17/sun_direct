package com.myplex.myplex.ui.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.adapter.OnItemRemovedListener;

import java.util.List;

public class AdapterSearchLanguagesItem extends RecyclerView.Adapter<AdapterSearchLanguagesItem.CarouselDataViewHolder>{
    private static final String TAG = AdapterSearchLanguagesItem.class.getSimpleName();
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
    private boolean isGenericLayout;

    private Typeface mBoldTypeFace;
    public AdapterSearchLanguagesItem(Context context, List<CardData> listCarouselData, RecyclerView mRecyclerViewCarousel) {
        mContext = context;
        mListMovies = listCarouselData;
        recyclerViewReference = mRecyclerViewCarousel;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
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
    public void setBgColor(String mBgColor){
        this.mBgColor=mBgColor;
    }

    public void setCarouselInfoData(String name, int pageSize) {
        if(!TextUtils.isEmpty(mPageName)){
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
//        new MenuDataModel().fetchCarouseldataInAsynTask(mContext, mPageName, 1, mPageSize, true,this);
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
       CarouselDataViewHolder customItemViewHolder=null;
        view = LayoutInflater.from(mContext).inflate(R.layout.search_language_item_list, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull CarouselDataViewHolder holder, int position) {
        bindGenreViewHolder(holder,mListMovies.get(position));

    }

    private void bindGenreViewHolder(@NonNull final CarouselDataViewHolder holder, final CardData carouselData) {

        /*if (carouselData != null && carouselData.globalServiceName!=null) {
            *//*if (carouselData.generalInfo.title!=null && !TextUtils.isEmpty(carouselData.generalInfo.title)) {*//*
                holder.language.setText(carouselData.globalServiceName);
          *//*  }*//*
            if(carouselData.generalInfo.altTitle!=null && carouselData.generalInfo.altTitle.get(0).title!=null && !TextUtils.isEmpty(carouselData.generalInfo.altTitle.get(0).title)){
                holder.regional_language_name.setText(carouselData.generalInfo.altTitle.get(0).title);
            }
             holder.search_language_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }*/
        if(carouselData != null && carouselData.generalInfo != null && carouselData.generalInfo.altTitle!=null && carouselData.generalInfo.altTitle.size()>0 && carouselData.generalInfo.altTitle.get(0) != null && carouselData.generalInfo.altTitle.get(0).title!=null && !TextUtils.isEmpty(carouselData.generalInfo.altTitle.get(0).title)){
            holder.regional_language_name.setText(carouselData.generalInfo.altTitle.get(0).title);
        }
        if(carouselData != null && carouselData.generalInfo != null && carouselData.generalInfo.title!=null &&  !TextUtils.isEmpty(carouselData.generalInfo.title)){
            holder.language.setTypeface(mBoldTypeFace);
            holder.language.setText(carouselData.generalInfo.title);

        }
        holder.setClickListener(mOnItemClickListenerWithData);

    }
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
    public void isGenericLayout(boolean b) {
        this.isGenericLayout = b;
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


    public class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView language,regional_language_name;
        RelativeLayout search_language_layout;
        private ItemClickListenerWithData clickListener;
        public CarouselDataViewHolder(@NonNull View itemView) {
            super(itemView);
            language = (TextView) (itemView.findViewById(R.id.default_language));
            regional_language_name=itemView.findViewById(R.id.regional_language_name);
            search_language_layout=itemView.findViewById(R.id.search_language_layout);

            itemView.setOnClickListener(this);
        }
        public void setClickListener(ItemClickListenerWithData itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            if (this.clickListener != null && mListMovies != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                if(mListMovies.size() != 0)
                    this.clickListener.onClick(view, getAdapterPosition(), mParentPosition, mListMovies.get(getAdapterPosition()));
            }

        }
    }
}
