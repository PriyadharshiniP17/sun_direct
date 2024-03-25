package com.myplex.myplex.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.model.FilterItem;
import com.myplex.myplex.R;

import java.util.List;

/**
 * Created by Apparao on 2/20/2018.
 */

public class AdapterFilterItems extends RecyclerView.Adapter<AdapterFilterItems.MyViewHolder> {
    private final List<FilterItem> filterItems;
    private String itemType;
    public OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    interface OnItemClickListener {
        public void onClick(String itemType, FilterItem item, int position);
    }

    public AdapterFilterItems(List<FilterItem> filterItems) {
        this.filterItems = filterItems;
    }

    @Override
    public AdapterFilterItems.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_filter, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AdapterFilterItems.MyViewHolder holder, final int position) {
        if (holder != null) {
            if (filterItems.get(position).isChecked())
                holder.ivCheckBox.setImageResource(R.drawable.filter_tick_selection_highlight_icon);
            else
                holder.ivCheckBox.setImageResource(R.drawable.filter_tick_selection_default_icon);
            holder.tvTitle.setText(filterItems.get(position).getTitle());
            holder.rlRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    filterItems.get(position).setIsChecked(!filterItems.get(position).isChecked());
                    if(onItemClickListener!=null){
                        onItemClickListener.onClick(itemType,filterItems.get(position),position);
                    }
                    notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filterItems.size();
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public ImageView ivCheckBox;
        public RelativeLayout rlRoot;

        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) (view.findViewById(R.id.tvFilterItem));
            ivCheckBox = (ImageView) (view.findViewById(R.id.ivFilterItem));
            rlRoot = (RelativeLayout) (view.findViewById(R.id.rowItem));
        }
    }
}
