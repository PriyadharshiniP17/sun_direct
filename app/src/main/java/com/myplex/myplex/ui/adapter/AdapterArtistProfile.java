package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.myplex.ui.views.ArtistProfileBannerAndDescriptionItem;
import com.myplex.myplex.ui.views.ArtistProfileCarouselItem;
import com.myplex.myplex.ui.views.UiCompoment;

import java.util.List;

public class AdapterArtistProfile extends RecyclerView.Adapter<UiCompoment> {

    private static final String TAG = AdapterArtistProfile.class.getSimpleName();
    private static final byte ITEM_TYPE_ARTIST_PROFILE_BANNER_DESCRIPTION_ITEM = 31;
    private static final byte ITEM_TYPE_ARTIST_CAROUSEL_ITEM =32;

    List<ProfileAPIListAndroid> profileAPIListAndroids;
    Context mContext;
    private RecyclerView mRecyclerViewCarouselInfo;
    private Handler mHandler;

    public AdapterArtistProfile(List<ProfileAPIListAndroid> profileAPIListAndroid, Context context){
        profileAPIListAndroids=profileAPIListAndroid;
        mContext=context;
        mHandler = new Handler(context.getMainLooper());
    }

    @NonNull
    @Override
    public UiCompoment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UiCompoment viewHolder = null;
        switch (viewType) {
            case ITEM_TYPE_ARTIST_PROFILE_BANNER_DESCRIPTION_ITEM:
                 viewHolder= ArtistProfileBannerAndDescriptionItem.createView(mContext,parent,profileAPIListAndroids,mRecyclerViewCarouselInfo);
                break;
            case ITEM_TYPE_ARTIST_CAROUSEL_ITEM:
                viewHolder= ArtistProfileCarouselItem.createView(mContext,parent,profileAPIListAndroids,mRecyclerViewCarouselInfo);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UiCompoment holder, int position) {
        holder.bindItemViewHolder(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (profileAPIListAndroids!=null){
            if (APIConstants.LAYOUT_TYPE_ARTIST_BANNER_DESCRIPTION.equalsIgnoreCase(profileAPIListAndroids.get(position).layoutType)){
                return ITEM_TYPE_ARTIST_PROFILE_BANNER_DESCRIPTION_ITEM;
            }else {
                return ITEM_TYPE_ARTIST_CAROUSEL_ITEM;
            }
        }
        return ITEM_TYPE_ARTIST_CAROUSEL_ITEM;
    }

    @Override
    public int getItemCount() {
        if (profileAPIListAndroids==null){
            return 0;
        }
        return profileAPIListAndroids.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViewCarouselInfo = recyclerView;
        recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_TYPE_ARTIST_CAROUSEL_ITEM, 50);
        recyclerView.setItemViewCacheSize(300);
    }

    private UiCompoment.UiComponentListenerInterface uiCompomentListenerInterface = new UiCompoment.UiComponentListenerInterface() {
        @Override
        public void notifyDataChanged(int position) {
            notifyItemChanged(position);
        }

        @Override
        public void notifyItemNeedToBeRemoved(final CarouselInfoData carouselInfoData, final int position) {

        }

        @Override
        public void notifyItemNeedToBeRemoved(final ProfileAPIListAndroid carouselInfoData, final int position) {
            safelyNotifyItemRemoved(carouselInfoData,position);
        }
    };

    public void safelyNotifyItemRemoved(final ProfileAPIListAndroid carouselInfoData,final int mParentPosition) {
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (profileAPIListAndroids != null) {
                        profileAPIListAndroids.remove(carouselInfoData);
                        if (!mRecyclerViewCarouselInfo.isComputingLayout()) {
                            //   mRecyclerViewCarouselInfo.getRecycledViewPool().clear();
                            Log.e("Notify","Notify item removed");
                            notifyItemRemoved(mParentPosition);
                            Log.e("Notify","Notify item range changed");
                            notifyItemRangeChanged(mParentPosition, profileAPIListAndroids.size());
                        } else {
                            safelyNotifyItemRemoved(carouselInfoData,mParentPosition);
                        }
                    }

                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
