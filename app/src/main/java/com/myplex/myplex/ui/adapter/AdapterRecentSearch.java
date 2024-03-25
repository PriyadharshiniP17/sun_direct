package com.myplex.myplex.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListener;
import java.util.ArrayList;
import java.util.List;

public class AdapterRecentSearch extends RecyclerView.Adapter<AdapterRecentSearch.CarouselDataViewHolder> {

    private List<String> recentSearchList;
    private Context mContext;
    private int parentPos;

    public AdapterRecentSearch(Context context, ArrayList<String> recentSearchList) {
        this.mContext = context;
        this.recentSearchList = recentSearchList;

    }

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder = null;
        view = LayoutInflater.from(mContext).inflate(R.layout.recent_search_child, parent, false);
        customItemViewHolder = new AdapterRecentSearch.CarouselDataViewHolder(view);
        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        holder.setClickListener(itemClickListener);
        holder.recentSearchItemTextView.setText(recentSearchList.get(position));
    }

    @Override
    public int getItemCount() {
        return recentSearchList.size();
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


    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListener clickListener;
        private final TextView recentSearchItemTextView;
        public CarouselDataViewHolder(View view) {
            super(view);
            recentSearchItemTextView = view.findViewById(R.id.searchItem);
            view.setLongClickable(true);
            view.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (this.clickListener != null && recentSearchList != null) {
                this.clickListener.onClick(v, getAdapterPosition(), parentPos,recentSearchList.get(getAdapterPosition()));
            }
        }

    }
}
