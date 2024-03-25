package com.myplex.myplex.ui.component;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by apalya on 11/24/2016.
 */

public  abstract class UiCompoment extends RecyclerView.ViewHolder {

    public int position;


    public interface UiCompomentListenerInterface{
        void notifyDataChanged(int position);
    }

    private UiCompomentListenerInterface uiCompomentListenerInterface;

    public void setUiCompomentListenerInterface(UiCompomentListenerInterface uiCompomentListenerInterface) {
        this.uiCompomentListenerInterface = uiCompomentListenerInterface;
    }

    public UiCompoment(View itemView) {
        super(itemView);
            }


    public  abstract void bindItemViewHolder(int position);

    public void notifyItemChanged(){
        if(uiCompomentListenerInterface != null){
            uiCompomentListenerInterface.notifyDataChanged(position);
        }
    }
}
