package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.model.CardDataPlayerOptionItem;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.LangUtil;

import java.util.List;

public class AdapterPlayerOption extends RecyclerView.Adapter<AdapterPlayerOption.AdapterPlayerOptionsViewHolder> {

    private static final String TAG = AdapterPlayerOption.class.getSimpleName();

    private final Context mContext;

    //Subtitles and Audio
    public static final int SUBTITLE = 1;
    public static final int AUDIO = 2;

    List<CardDataPlayerOptionItem> cardDataPlayerOptionItems;
    PlayerOptionListener playerOptionListener;
    int currentSelectedPosition;
    public AdapterPlayerOption(Context context, List<CardDataPlayerOptionItem> cardDataPlayerOptionItems,PlayerOptionListener playerOptionListener, int currentSelectedPosition) {
        mContext = context;
        this.cardDataPlayerOptionItems = cardDataPlayerOptionItems;
        this.playerOptionListener = playerOptionListener;
        this.currentSelectedPosition = currentSelectedPosition;

    }


    @Override
    public AdapterPlayerOptionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_player_options, parent, false);
        return new AdapterPlayerOption.AdapterPlayerOptionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterPlayerOptionsViewHolder holder, int position) {
        bindPlayerOptionViewHolder(holder, cardDataPlayerOptionItems.get(position), position);
    }


    private void bindPlayerOptionViewHolder(final AdapterPlayerOptionsViewHolder holder, final CardDataPlayerOptionItem playerOptionItem, int position) {

        if (playerOptionItem != null) {

            if (currentSelectedPosition == position){
                holder.iv_selected.setVisibility(View.VISIBLE);
                holder.tv_title.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.tv_title.setTypeface(null, Typeface.BOLD);
            }
            else{
                holder.iv_selected.setVisibility(View.GONE);
                holder.tv_title.setTextColor(mContext.getResources().getColor(R.color.white_60));
                holder.tv_title.setTypeface(null, Typeface.NORMAL);

            }
            if(playerOptionItem.getName()!=null)
                holder.tv_title.setText((playerOptionItem.getType() == SUBTITLE ? playerOptionItem.getName() : LangUtil.getSubtitleTrackName(playerOptionItem.getName())));

            holder.cl_root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentSelectedPosition = position;
                    playerOptionListener.onClickItem(holder.getAdapterPosition());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if (cardDataPlayerOptionItems == null) return 0;

        return cardDataPlayerOptionItems.size();
    }

    class AdapterPlayerOptionsViewHolder extends RecyclerView.ViewHolder {

        LinearLayout cl_root;
        TextView tv_title;
        ImageView iv_selected;


        public AdapterPlayerOptionsViewHolder(View view) {
            super(view);
            cl_root = view.findViewById(R.id.ll_root);
            tv_title = view.findViewById(R.id.tv_title);
            iv_selected = view.findViewById(R.id.iv_selected);
        }
    }

    public interface PlayerOptionListener {
        void onClickItem(int position);
    }
}
