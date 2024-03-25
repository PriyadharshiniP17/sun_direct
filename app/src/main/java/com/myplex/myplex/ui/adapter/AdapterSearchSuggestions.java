package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.model.SearchFilterResponse;
import com.myplex.myplex.R;

public class AdapterSearchSuggestions extends RecyclerView.Adapter<AdapterSearchSuggestions.CarouselDataViewHolder>{
    public OnItemClickListener onItemClickListener;
    private Context mContext;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        public void onClick(SearchFilterResponse item);
    }

    public AdapterSearchSuggestions() {
    }

    @NonNull
    @Override
    public AdapterSearchSuggestions.CarouselDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_search_suggestions, parent, false);
        return new AdapterSearchSuggestions.CarouselDataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSearchSuggestions.CarouselDataViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class CarouselDataViewHolder extends RecyclerView.ViewHolder {
        TextView suggestionText;
        public CarouselDataViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionText=itemView.findViewById(R.id.search_suggestion_text);
        }
    }
}
