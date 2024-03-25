package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.util.StringEscapeUtils;
import com.myplex.myplex.R;

import java.util.List;


public class AdapterPackages extends BaseAdapter {

    private static final String TAG = AdapterPackages.class.getSimpleName();

    private final Context mContext;
    private List<CardDataPackages> mPackagesList;
    private boolean mIsDummyData = false;
    private int mSelectedPackagePosition;

    public AdapterPackages(Context context, List<CardDataPackages> itemList) {
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

    private static final int TYPE_ITEM = 1;

    @Override
    public int getCount() {
        if (mPackagesList == null) return 0;

        return mPackagesList.size();
    }

    @Override
    public Object getItem(int i) {
        if (mPackagesList == null) {
            return null;
        }
        return mPackagesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MySubscribedPacksViewHolder mViewHolder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listitem_package, null, false);
            mViewHolder = new MySubscribedPacksViewHolder();
//            mViewHolder.mPackageSubscribeText = (TextView) view.findViewById(R.id.carddetailpack_subscribe_text);
            mViewHolder.mPackageTitle = (TextView) view.findViewById(R.id.carddetailpack_name);
            mViewHolder.mPackageOriginalPrice = (TextView) view.findViewById(R.id.carddetailpack_original_price);
            mViewHolder.mPackageOfferPrice = (TextView) view.findViewById(R.id.carddetailpack_offer_price);
            mViewHolder.mPackagePercentText = (TextView) view.findViewById(R.id.carddetailpack_offer_percent_text);
            mViewHolder.mPackagePerMonthText= (TextView) view.findViewById(R.id.carddetailpack_offer_per_month_text);
            mViewHolder.mPackageDescription = (TextView) view.findViewById(R.id.carddetailpack_description);
//            mViewHolder.mLayoutSubscribeButton = (RelativeLayout) view.findViewById(R.id.carddetailpack_subscribe_text_layout);
            mViewHolder.mRadioButtonImageView = (ImageView) view.findViewById(R.id.radio_indicator);
            mViewHolder.mPackageOfferDescription = (TextView)view.findViewById(R.id.carddetailpack_offer_description);
            mViewHolder.mLayoutDiscount = view.findViewById(R.id.discount_layout);
            view.setTag(mViewHolder);
        } else {
            mViewHolder = (MySubscribedPacksViewHolder) view.getTag();
        }

        CardDataPackages currentPackage = mPackagesList.get(i);
        if (currentPackage == null) {
            return view;
        }


        if (mSelectedPackagePosition == i) {
            mViewHolder.mRadioButtonImageView.setImageResource(R.drawable.radio_btn_selected);
        } else {
            mViewHolder.mRadioButtonImageView.setImageResource(R.drawable.radio_btn_default);
        }

        if (TextUtils.isEmpty(currentPackage.displayName)) {
            mViewHolder.mPackageTitle.setVisibility(View.GONE);
        } else {
            mViewHolder.mPackageTitle.setVisibility(View.VISIBLE);
            mViewHolder.mPackageTitle.setText(currentPackage.displayName+":");
        }

        if (TextUtils.isEmpty(currentPackage.bbDescription)) {
            mViewHolder.mPackageDescription.setVisibility(View.GONE);
        } else {
            mViewHolder.mPackageDescription.setVisibility(View.VISIBLE);
            mViewHolder.mPackageDescription.setText(currentPackage.bbDescription);
        }

        if (getCount() == 1) {
            mViewHolder.mPackageDescription.setMaxLines(25);
            mViewHolder.mPackageDescription.setTextSize(14);
        } else {
            mViewHolder.mPackageDescription.setTextSize(12);
            mViewHolder.mPackageDescription.setMaxLines(5);
        }
        mViewHolder.mPackageDescription.setEllipsize(TextUtils.TruncateAt.END);
//        if (TextUtils.isEmpty(currentPackage.cpDescripton)) {
            mViewHolder.mPackageOfferDescription.setVisibility(View.GONE);
//        } else {
//            mViewHolder.mPackageOfferDescription.setVisibility(View.VISIBLE);
//            mViewHolder.mPackageOfferDescription.setText(currentPackage.cpDescripton);
//        }
//
//
//        if (!TextUtils.isEmpty(currentPackage.cpDescriptionV2)) {
//            mViewHolder.mPackageOfferDescription.setVisibility(View.VISIBLE);
//            mViewHolder.mPackageOfferDescription.setText(currentPackage.cpDescriptionV2);
//        }
/*

        if (currentPackage.unsubscription) {
            mViewHolder.mPackageSubscribeText.setText("Unsubscribe");
//            GradientDrawable drawable = (GradientDrawable) mViewHolder.mLayoutSubscribeButton.getBackground();
            drawable.setColor(getColor(mContext, R.color.red_unsubscribe_btn_color));
            drawable.setStroke((int) mContext.getResources().getDimension(R.dimen.margin_gap_2), getColor(mContext, R.color.red_unsubscribe_btn_color));
//            mViewHolder.mLayoutSubscribeButton.setVisibility(View.VISIBLE);
//            holder.mLayoutSubscribeButton.setBackgroundColor(getColor(mContext,R.color.red_unsubscribe_btn_color));
        } else {
//            mViewHolder.mLayoutSubscribeButton.setVisibility(View.GONE);
        }
*/


        if(i < getCount()-1){
            view.findViewById(R.id.divider_view).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.divider_view).setVisibility(View.GONE);
        }

//        mViewHolder.mPackageSubscribeText.setTag(currentPackage);


        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    public void setSelectedPackagePosition(int mSelectedPackagePosition) {
        this.mSelectedPackagePosition = mSelectedPackagePosition;
    }

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class MySubscribedPacksViewHolder {
        TextView mPackageTitle;
        TextView mPackageDescription;
        TextView mPackageSubscribeText;
        TextView mPackageOfferPrice;
        TextView mPackageOriginalPrice;
        TextView mPackagePercentText;
        TextView mPackagePerMonthText;
//        RelativeLayout mLayoutSubscribeButton;
        ImageView mRadioButtonImageView;
        TextView mPackageOfferDescription;
        View mLayoutDiscount;

    }

    public interface ItemClickListener {
        void onClick(View view, int position, CardDataPackages cardDataPackage);
    }
}