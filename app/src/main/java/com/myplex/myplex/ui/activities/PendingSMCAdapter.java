package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListener;
import com.myplex.myplex.model.PendingSMCItemClickListener;
import com.myplex.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

public class PendingSMCAdapter extends RecyclerView.Adapter<PendingSMCAdapter.ViewHolder> {
    List<String> data = new ArrayList<>();

    public PendingSMCAdapter(List<String> moreNames){
       this.data = moreNames;
    }

    private PendingSMCItemClickListener itemClickListener;


    public void setItemClickListener(PendingSMCItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PendingSMCAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.pending_smc_adapter, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      holder.textView.setText(data.get(position));
      holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              itemClickListener.onClick(view,position,data.get(position));
          }
      });
    }
    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.pending_smclist);
            textView =itemView.findViewById(R.id.pending_smc_numbers);
        }
    }
}


