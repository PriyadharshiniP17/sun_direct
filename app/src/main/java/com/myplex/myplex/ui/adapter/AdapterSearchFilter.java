package com.myplex.myplex.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.model.SearchFilterResponse;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;


import java.util.List;

/**
 * Created by Apparao on 08/03/2018.
 */

public class AdapterSearchFilter extends RecyclerView.Adapter<AdapterSearchFilter.MyViewHolder> {
    private final List<SearchFilterResponse> filterItems;
    public OnItemClickListener onItemClickListener;
    private Context mContext;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onClick(SearchFilterResponse item);
    }

    public AdapterSearchFilter(Context mContext,List<SearchFilterResponse> filterItems) {
        this.filterItems = filterItems;
        this.mContext = mContext;
    }

    @Override
    public AdapterSearchFilter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_search_with_filter, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AdapterSearchFilter.MyViewHolder holder, final int position) {
        if (holder != null) {
            holder.tvTitle.setText(filterItems.get(position).displayName);
            if (filterItems.get(position).isChecked) {
                holder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.filter_selected_color_text));
                holder.itemView.setBackgroundResource(R.drawable.rounded_filter_selected);
            }else {
                holder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.recent_search_unselected_text_color));
                holder.itemView.setBackgroundResource(R.drawable.rounded_filter_un_selected);
            }
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SDKLogger.debug(position >= 0 ? filterItems.get(position).displayName : position + "");
                    for (int i = 0; i < filterItems.size(); i++) {
                        if (i == position) {
                            filterItems.get(i).isChecked = true;
                        } else {
                            filterItems.get(i).isChecked = false;
                        }
                    }
                    if(onItemClickListener!=null){
                        onItemClickListener.onClick(filterItems.get(position));
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filterItems.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;


        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) (view.findViewById(R.id.tvFilterItem));
        }
    }
}