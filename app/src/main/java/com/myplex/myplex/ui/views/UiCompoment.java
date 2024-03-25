package com.myplex.myplex.ui.views;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.myplex.model.CarouselInfoData;
import com.myplex.model.ProfileAPIListAndroid;


/**
 * Created by apalya on 11/24/2016.
 */

public  abstract class UiCompoment extends RecyclerView.ViewHolder {

    public int position;

    public interface UiComponentListenerInterface{
        void notifyDataChanged(int position);
        void notifyItemNeedToBeRemoved(CarouselInfoData carouselInfoData, int position);
        void notifyItemNeedToBeRemoved(ProfileAPIListAndroid carouselInfoData, int position);
    }

    private UiComponentListenerInterface uiComponentListenerInterface;

    public void setUiComponentListenerInterface(UiComponentListenerInterface uiCompomentListenerInterface) {
        this.uiComponentListenerInterface = uiCompomentListenerInterface;
    }

    UiCompoment(View itemView) {
        super(itemView);
            }

    public  abstract void bindItemViewHolder(int position);

    public void notifyItemChanged(){
        if(uiComponentListenerInterface != null){
            uiComponentListenerInterface.notifyDataChanged(position);
        }
    }

    public void notifyItemRemoved(CarouselInfoData carouselInfoData){
        if(uiComponentListenerInterface != null){
            uiComponentListenerInterface.notifyItemNeedToBeRemoved(carouselInfoData,position);
        }
    }

    public void notifyItemRemoved(ProfileAPIListAndroid carouselInfoData){
        if(uiComponentListenerInterface != null){
            uiComponentListenerInterface.notifyItemNeedToBeRemoved(carouselInfoData,position);
        }
    }
}
