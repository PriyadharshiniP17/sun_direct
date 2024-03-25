package com.myplex.myplex.ui.adapter;

import static com.myplex.myplex.ApplicationController.getAppContext;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.myplex.model.ResultNotification;
import com.myplex.myplex.R;
import com.myplex.myplex.model.NotificationListener;
import com.myplex.myplex.model.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>  {

    List<ResultNotification> notificationList;
    Context context;
    Typeface amazonEmberBold;


    NotificationListener notificationListener;
    public NotificationAdapter(Context context, List<ResultNotification> notificationList, NotificationListener notificationListener) {
        this.context = context;
        this.notificationList = notificationList;
        this.notificationListener = notificationListener;
        amazonEmberBold = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        ResultNotification notificationModel = notificationList.get(position);

        holder.tvTitle.setText(notificationModel.getTitle());
        holder.tvSubTitle.setText(notificationModel.getSubject());
        holder.tvDescription.setText(notificationModel.getDescription());
       /* if(notificationModel.getIsVideo() ==1){
            holder.clCardRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationListener.onNotificationClick(notificationList.get(holder.getAdapterPosition()).getVideoId());
                }
            });

        }*/
        if(notificationModel.getDescription().equals("")){
            holder.ibExpandArrow.setVisibility(View.GONE);
            holder.tvDescription.setVisibility(View.GONE);
        }else {
            holder.ibExpandArrow.setVisibility(View.VISIBLE);

            if(notificationModel.isExpanded()){
                holder.ibExpandArrow.setImageDrawable(context.getDrawable(R.drawable.icon_up_arrow));
                holder.tvDescription.setVisibility(View.VISIBLE);
            }else {
                holder.ibExpandArrow.setImageDrawable(context.getDrawable(R.drawable.icon_down_arrow));
                holder.tvDescription.setVisibility(View.GONE);
            }
            holder.ibExpandArrow.setTag(position);
            holder.ibExpandArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    notificationListener.onNotificationClick(position);
                    if(notificationModel.isExpanded()){
                        holder.ibExpandArrow.setImageDrawable(context.getDrawable(R.drawable.icon_down_arrow));
                        holder.tvDescription.setVisibility(View.GONE);
                        notificationModel.setExpanded(false);
                    }else {
                        holder.ibExpandArrow.setImageDrawable(context.getDrawable(R.drawable.icon_up_arrow));
                        holder.tvDescription.setVisibility(View.VISIBLE);
                        notificationModel.setExpanded(true);
                    }
                }
            });
            holder.clCardRoot.setTag(position);

           /* holder.clCardRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    notificationListener.onNotificationClick(position);
                }
            });*/


        }

        if(notificationModel.getImageUrl()!=null && !notificationModel.getImageUrl().equals("")){
            Glide.with(context)
                    .load(notificationModel.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.sundirect_banner_placeholder)
                    .into(holder.ivDp);
        }

        holder.tvTitle.setTypeface(amazonEmberBold);
        holder.tvDescription.setTypeface(amazonEmberBold);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void setData( List<ResultNotification> notificationList){
        this.notificationList = notificationList;
    }
    public void deleteItem(int position) {
        if (position != RecyclerView.NO_POSITION) {
            notificationList.remove(position);
            notifyItemRemoved(position);
        }
    }

  /*  public void addItem(int position, NotificationModel notificationModel) {
        if (position != RecyclerView.NO_POSITION) {

            notificationList.add(position, notificationModel);
            notifyItemInserted(position);
        }
    }

    public void changeItem(int position, NotificationModel notificationModel) {
        if (position != RecyclerView.NO_POSITION) {
            notificationList.set(position, notificationModel);
            notifyItemChanged(position);
        }
    }*/


    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View container = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(container);
    }


    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout clCardRoot;
        ImageView ivDp;
        TextView tvTitle;
        TextView tvSubTitle;
        ImageButton ibExpandArrow;
        TextView tvDescription;


        public NotificationViewHolder(View v) {
            super(v);
            clCardRoot = v.findViewById(R.id.cl_card_root);
            ivDp = v.findViewById(R.id.iv_dp);
            tvTitle = v.findViewById(R.id.tv_title);
            tvSubTitle = v.findViewById(R.id.tv_sub_title);
            ibExpandArrow = v.findViewById(R.id.ib_expand_arrow);
            tvDescription = v.findViewById(R.id.tv_description);
        }

        @NonNull
        @Override
        public String toString() {
            return "NotificationViewHolder{" +
                    "tv_title=" + tvTitle +
                    ", tv_sub_title=" + tvSubTitle +
                    ", tv_description=" + tvDescription +
                    '}';
        }
    }

}
