package com.myplex.myplex.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.model.PreferredLanguageItem;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apparao on 2/26/2018.
 */

public class AdapterPreferredLanguages extends RecyclerView.Adapter<AdapterPreferredLanguages.MyViewHolder> {
    private final List<PreferredLanguageItem> preferredLanguageItems;
    private String itemType;
    public OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void storeSelectedLanguagesinPrefs() {
        List<PreferredLanguageItem> selectedLanguageItems = new ArrayList<>();
        for (int i = 0; i < preferredLanguageItems.size(); i++) {
            if (preferredLanguageItems.get(i).isDefault() || preferredLanguageItems.get(i).isChecked) {
                selectedLanguageItems.add(preferredLanguageItems.get(i));
            }
        }
        if (selectedLanguageItems.size() > 0) {
            PrefUtils.getInstance().setPreferredLanguages(selectedLanguageItems);
        }
    }

    public List<PreferredLanguageItem> getSelectedLanguages(){
        List<PreferredLanguageItem> selectedLanguages = new ArrayList<>();
        if(preferredLanguageItems == null){
            return null;
        }
        for (int i = 0; i < preferredLanguageItems.size(); i++) {
            if (preferredLanguageItems.get(i).isDefault() || preferredLanguageItems.get(i).isChecked) {
                selectedLanguages.add(preferredLanguageItems.get(i));
            }
        }
        return selectedLanguages;
    }

    public void storeDefaultLanguesinPrefs() {
        List<PreferredLanguageItem> selectedLanguageItems = new ArrayList<>();
        for (int i = 0; i < preferredLanguageItems.size(); i++) {
            if (preferredLanguageItems.get(i).isDefault()) {
                selectedLanguageItems.add(preferredLanguageItems.get(i));
            }
        }
        if (selectedLanguageItems.size() > 0) {
            PrefUtils.getInstance().setPreferredLanguages(selectedLanguageItems);
        }
    }

    interface OnItemClickListener {
        public void onClick(String itemType, PreferredLanguageItem item, int position);
    }

    public AdapterPreferredLanguages(List<PreferredLanguageItem> preferredLanguageItems) {
        this.preferredLanguageItems = preferredLanguageItems;
    }

    @Override
    public AdapterPreferredLanguages.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_preferred_languages, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AdapterPreferredLanguages.MyViewHolder holder, final int position) {
        if (holder == null)
            return;
        if (preferredLanguageItems.get(position).isDefault())
            preferredLanguageItems.get(position).isChecked = preferredLanguageItems.get(position).isDefault();
        if (preferredLanguageItems.get(position).isChecked) {
            holder.ivCheckBox.setImageResource(R.drawable.tick_selected);
            holder.viewTint.setBackgroundResource(R.color.filter_background_color);
            holder.viewTint.setAlpha(0.5f);
        } else {
            holder.viewTint.setBackgroundResource(R.color.color_000000);
            holder.ivCheckBox.setImageResource(R.drawable.tick_unselected);
            holder.viewTint.setAlpha(0.5f);
        }
        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                int width = holder.itemView.getMeasuredWidth();
                if (width > 0) {
                    holder.imageLanguages.getLayoutParams().height = (int) (width * 2 / 9);
                    holder.viewTint.getLayoutParams().height = (int) (width * 2 / 9);
                    SDKLogger.debug(((int) width * 2 / 9) + " ");
                    PicassoUtil.with(holder.imageLanguages.getContext()).load(preferredLanguageItems.get(position).getImage(), holder.imageLanguages, R.drawable.language_banner_placeholder);
                }
            }
        });
        holder.tvTitle.setText(preferredLanguageItems.get(position).getHumanReadable());
        holder.rlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!preferredLanguageItems.get(position).isDefault()) {
                    preferredLanguageItems.get(position).isChecked = !preferredLanguageItems.get(position).isChecked;

                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(itemType, preferredLanguageItems.get(position), position);
                    }
                    notifyItemChanged(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return preferredLanguageItems.size();
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public ImageView ivCheckBox;
        public RelativeLayout rlRoot;
        public ImageView imageLanguages;
        public View viewTint;

        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) (view.findViewById(R.id.tvFilterItem));
            ivCheckBox = (ImageView) (view.findViewById(R.id.ivFilterItem));
            rlRoot = (RelativeLayout) (view.findViewById(R.id.rowItem));
            imageLanguages = (ImageView) (view.findViewById(R.id.image_languages));
            viewTint = view.findViewById(R.id.view_tint);
        }
    }
}
