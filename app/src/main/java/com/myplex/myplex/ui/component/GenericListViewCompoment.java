package com.myplex.myplex.ui.component;

import android.view.View;

/**
 * Created by ramaraju on 11/24/2016.
 */

public abstract class GenericListViewCompoment extends UiCompoment implements View.OnClickListener {

    private ItemClickListener clickListener;
    public GenericListViewCompoment(View view) {
        super(view);
        itemView.setOnClickListener(this);
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

