package com.myplex.myplex.ui.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.model.CardDataPackages;
import com.myplex.myplex.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AdapterMyPacks extends RecyclerView.Adapter<AdapterMyPacks.MySubscribedPacksViewHolder> {

    private static final String TAG = AdapterMyPacks.class.getSimpleName();

    private final Context mContext;
    private List<CardDataPackages> mPackagesList;
    private boolean mIsDummyData = false;

    public AdapterMyPacks(Context context, List<CardDataPackages> itemList) {
        mContext = context;
        mPackagesList = itemList;
    }

    public void setData(List<CardDataPackages> listMovies) {
        if (listMovies == null) {
            return;
        }
        mIsDummyData = false;
        mPackagesList = listMovies;
        notifyDataSetChanged();
    }

    public List<CardDataPackages> getData(){
        return mPackagesList;
    }

    public void addData(List<CardDataPackages> listMovies) {
        if (listMovies == null) {
            return;
        }

        //Log.d(TAG, "addData");
        if (mPackagesList == null) {
            mPackagesList = listMovies;
            notifyDataSetChanged();
            return;
        }
        mPackagesList.addAll(listMovies);
        notifyDataSetChanged();

    }

    @Override
    public MySubscribedPacksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        MySubscribedPacksViewHolder customItemViewHolder = null;
        view = LayoutInflater.from(mContext).inflate(R.layout.subscribed_packs_list_item, parent, false);
        customItemViewHolder = new MySubscribedPacksViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(MySubscribedPacksViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder" + position);
        bindGenreViewHolder(holder, mPackagesList.get(position));
    }


    private void bindGenreViewHolder(final MySubscribedPacksViewHolder holder, final CardDataPackages currentPackage) {

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

        if (currentPackage == null) {
            return;
        }
        if (currentPackage.displayName != null) {
            holder.mPackTitle.setText(currentPackage.displayName);
        }
        if (currentPackage.priceCharged!= null) {
            if (currentPackage.currencyCode!=null){
                holder.mPackPrice.setText(currentPackage.currencyCode+" "+currentPackage.priceCharged);
            }else {
                holder.mPackPrice.setText(currentPackage.priceCharged);
            }

        }
        if(currentPackage.duration != null){
            holder.mPackDuration.setText("For "+currentPackage.duration+" days");
        }

        if (currentPackage.validityStartDate != null) {
            holder.mPackStartDate.setText("Date of Purchase: "
                    + convertDateFormat("MM/dd/yyyy hh:mm:ss", "dd/MM/yyyy", currentPackage.validityStartDate));
        }

        //if(currentPackage. != null){
            holder.mPackStatus.setText("Active");
        //}

        if(currentPackage.country != null){
            holder.mCountry.setText(currentPackage.country);
        }

        if(currentPackage.paymentMode != null){
            holder.mPaymentMode.setText(currentPackage.paymentMode);
        }

        if(currentPackage.validityEndDate != null){
            holder.mExpiryDate.setText(
                    convertDateFormat("MM/dd/yyyy hh:mm:ss", "dd/MM/yyyy", currentPackage.validityEndDate));
        }



       /* if (currentPackage.isExpired) {
            holder.mPackageSubscribeText.setText("Upgrade");
            GradientDrawable drawable = (GradientDrawable) holder.mLayoutSubscribeButton.getBackground();
            drawable.setColor(getColor(mContext, R.color.green_upgrade_btn_color));
            drawable.setStroke((int) mContext.getResources().getDimension(R.dimen.margin_gap_2), getColor(mContext, R.color.green_upgrade_btn_color));
//            holder.mLayoutSubscribeButton.setBackgroundColor(getColor(mContext,R.color.green_upgrade_btn_color));
        } else if (currentPackage.subscribed) {
            holder.mPackageSubscribeText.setText("Unsubscribe");
            GradientDrawable drawable = (GradientDrawable) holder.mLayoutSubscribeButton.getBackground();
            drawable.setColor(getColor(mContext, R.color.red_unsubscribe_btn_color));
            drawable.setStroke((int) mContext.getResources().getDimension(R.dimen.margin_gap_2), getColor(mContext, R.color.red_unsubscribe_btn_color));
//            holder.mLayoutSubscribeButton.setBackgroundColor(getColor(mContext,R.color.red_unsubscribe_btn_color));
        } else {
            GradientDrawable drawable = (GradientDrawable) holder.mLayoutSubscribeButton.getBackground();
            drawable.setColor(getColor(mContext, R.color.app_theme_color));
            drawable.setStroke((int) mContext.getResources().getDimension(R.dimen.margin_gap_2), getColor(mContext, R.color.red_highlight_color));
        }*/

/*
        if (currentPackage.unsubscription) {
            holder.mPackageSubscribeText.setText("Unsubscribe");
            GradientDrawable drawable = (GradientDrawable) holder.mLayoutSubscribeButton.getBackground();
            drawable.setColor(UiUtil.getColor(mContext, R.color.red_unsubscribe_btn_color));
            drawable.setStroke((int) mContext.getResources().getDimension(R.dimen.margin_gap_2), UiUtil.getColor(mContext, R.color.red_unsubscribe_btn_color));
            holder.mLayoutSubscribeButton.setVisibility(View.VISIBLE);
//            holder.mLayoutSubscribeButton.setBackgroundColor(getColor(mContext,R.color.red_unsubscribe_btn_color));
        } else {
            holder.mLayoutSubscribeButton.setVisibility(View.GONE);
        }
        holder.mPackageSubscribeText.setTag(currentPackage);
*/


        //holder.setClickListener(mItemClickListener);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        if (mPackagesList == null) return 0;

        return mPackagesList.size();
    }

    private static final int TYPE_ITEM = 1;

    @Override
    public int getItemViewType(int position) {

        return TYPE_ITEM;

        /*if (mItemList.get(position) instanceof Movie) {
            type = TYPE_MOVIE;
        } else if (mItemList.get(position) instanceof RelatedMoviesItem) {
            type = TYPE_RELATED_ITEMS;
        }*/

//        return type;
    }

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class MySubscribedPacksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListener clickListener;
        /*TextView mPackageTitle;
        TextView mPackageDescription,mPackageOrderId;
        TextView mPackageSubscribeText;
        RelativeLayout mLayoutSubscribeButton;
        TextView mPackEnddate;*/

        TextView mPackTitle;
        TextView mPackPrice;
        TextView mPackDuration;
        TextView mPackStartDate;
        TextView mPackStatus;
        TextView mCountry;
        TextView mPaymentMode;
        TextView mExpiryDate;

        public MySubscribedPacksViewHolder(View view) {
            super(view);

            mPackTitle=view.findViewById(R.id.pack_title_tv);
            mPackPrice=view.findViewById(R.id.pack_rate_tv);
            mPackDuration=view.findViewById(R.id.pack_duration_tv);
            mPackStartDate=view.findViewById(R.id.pack_purchase_date_tv);
            mPackStatus=view.findViewById(R.id.status_tv);
            mCountry=view.findViewById(R.id.country_tv);
            mPaymentMode=view.findViewById(R.id.payment_mode_tv);
            mExpiryDate=view.findViewById(R.id.expiry_date_tv);

            /*mPackageSubscribeText = (TextView) view.findViewById(R.id.carddetailpack_subscribe_text);
            mPackageTitle = (TextView) view.findViewById(R.id.carddetailpack_name);
            mPackageDescription = (TextView) view.findViewById(R.id.carddetailpack_description);
            mPackageOrderId = (TextView) view.findViewById(R.id.carddetailpack_orderId);
            mLayoutSubscribeButton = (RelativeLayout) view.findViewById(R.id.carddetailpack_subscribe_text_layout);
            mPackEnddate = (TextView) view.findViewById(R.id.carddetailpack_enddate);*/
//            view.setOnClickListener(this);
           // mLayoutSubscribeButton.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (this.clickListener != null && mPackagesList != null) {
                this.clickListener.onClick(v, getAdapterPosition(), mPackagesList.get(getAdapterPosition()));
            }
        }

    }

    // Overriding this method to get access to recyclerView on which current MovieAdapter has been attached.
    // In the future we will use that reference for scrolling to newly added relatedMovies item
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView mRecyclerViewMovies = recyclerView;
        recyclerView.getRecycledViewPool().setMaxRecycledViews(TYPE_ITEM, 100);
        recyclerView.setItemViewCacheSize(100);
    }

    private ItemClickListener mItemClickListener;

    public void setOnItemClickListenerWithMovieData(ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface ItemClickListener {
        void onClick(View view, int position, CardDataPackages cardDataPackage);
    }

    public String convertDateFormat(String from, String to, String date) {
        SimpleDateFormat sdfFrom = new SimpleDateFormat(from);
        SimpleDateFormat sdfTo = new SimpleDateFormat(to);
        Date mDate = null;
        String convertedDate = "";

        try {
            mDate = sdfFrom.parse(date);
            convertedDate = sdfTo.format(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }
}