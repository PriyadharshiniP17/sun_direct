package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class AdapterSearchHistory extends BaseAdapter {

    private List<String> recentSearchList;
    private Context mContext;
    private int parentPos;

    public AdapterSearchHistory(Context context, ArrayList<String> recentSearchList) {
        this.mContext = context;
        this.recentSearchList = recentSearchList;

    }


    private ItemClickListener itemClickListener;
    public void setOnItemClickListenerWithMovieData(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setParentPosition(int position) {
        this.parentPos = position;
    }

    public void showTitle(boolean showTitle) {
    }

    @Override
    public int getCount() {
        return recentSearchList.size();
    }

    @Override
    public Object getItem(int position) {
        return recentSearchList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        CarouselDataViewHolder customItemViewHolder = null;
        view = LayoutInflater.from(mContext).inflate(R.layout.recent_search_child, parent, false);
        customItemViewHolder = new AdapterSearchHistory.CarouselDataViewHolder(view);
        customItemViewHolder.setClickListener(itemClickListener);
        customItemViewHolder.recentSearchItemTextView.setText(recentSearchList.get(position));

        return view;
    }


    class CarouselDataViewHolder  implements View.OnClickListener {

        private ItemClickListener clickListener;
        private final TextView recentSearchItemTextView;
        public CarouselDataViewHolder(View view) {
            recentSearchItemTextView = view.findViewById(R.id.searchItem);
            view.setLongClickable(true);
            view.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
         /*   if (this.clickListener != null && recentSearchList != null) {
                this.clickListener.onClick(v, getAdapterPosition(), parentPos,recentSearchList.get(getAdapterPosition()));
            }*/
        }

    }
}
