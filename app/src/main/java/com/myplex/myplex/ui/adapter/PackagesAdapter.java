package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.StringEscapeUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.views.SubscriptionPacksDialog;

import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class PackagesAdapter extends BaseAdapter{
    private Context mContext;
    private List<CardDataPackages> mPackagesList;
    private SubcriptionEngine mSubscriptionEngine;

    private View.OnClickListener mPackSubscribeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getTag() instanceof CardDataPackages){
                CardDataPackages packageitem = (CardDataPackages) v.getTag();
                if(packClickListener != null){
                    packClickListener.sendMessage();
                }

            }
        }
    };
    private SubscriptionPacksDialog.MessagePostCallback packClickListener;

    public PackagesAdapter(Context context, List<CardDataPackages> packgesList) {
        mContext = context;
        mPackagesList = packgesList;
        mSubscriptionEngine = new SubcriptionEngine(mContext);
    }
    @Override
    public int getCount() {
        if(mPackagesList == null){
            return 0;
        }
        return mPackagesList.size();
    }

    @Override
    public CardDataPackages getItem(int position) {
        return mPackagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.carddetailpackages_listitem_popup,null,false);
            mViewHolder = new ViewHolder();
            mViewHolder.mPackageSubscribeText = (TextView)convertView.findViewById(R.id.carddetailpack_subscribe_text);
            mViewHolder.mPackageTitle = (TextView)convertView.findViewById(R.id.carddetailpack_name);
            mViewHolder.mPackageDescription = (TextView)convertView.findViewById(R.id.carddetailpack_description);
            mViewHolder.mPackageOfferDescription = (TextView)convertView.findViewById(R.id.carddetailpack_offer_description);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        CardDataPackages currentPackage = mPackagesList.get(position);
        if (null == currentPackage) {
            return convertView;
        }

        if (TextUtils.isEmpty(currentPackage.displayName)) {
            mViewHolder.mPackageTitle.setVisibility(View.GONE);
        } else {
            mViewHolder.mPackageTitle.setVisibility(View.VISIBLE);
            mViewHolder.mPackageTitle.setText(currentPackage.displayName);
        }

        if (TextUtils.isEmpty(currentPackage.bbDescription)) {
            mViewHolder.mPackageDescription.setVisibility(View.GONE);
        } else {
            mViewHolder.mPackageDescription.setVisibility(View.VISIBLE);
            mViewHolder.mPackageDescription.setText(currentPackage.bbDescription);
        }

//        if (TextUtils.isEmpty(currentPackage.cpDescripton)) {
            mViewHolder.mPackageOfferDescription.setVisibility(View.GONE);
//        } else {
//            mViewHolder.mPackageOfferDescription.setVisibility(View.VISIBLE);
//            mViewHolder.mPackageOfferDescription.setText(currentPackage.cpDescripton);
//        }
//
//        if (TextUtils.isEmpty(currentPackage.cpDescriptionV2)) {
//            mViewHolder.mPackageOfferDescription.setVisibility(View.VISIBLE);
//            mViewHolder.mPackageOfferDescription.setText(currentPackage.cpDescriptionV2);
//        }

        /*for (CardDataPackagePriceDetailsItem priceDetails : currentPackage.priceDetails) {
                mViewHolder.mPackageSubscribeText.setText("Rs " + priceDetails.price+"/-");
        }*/
        if(!TextUtils.isEmpty(currentPackage.actionButtonText)){
            mViewHolder.mPackageSubscribeText.setText(StringEscapeUtils.unescapeJava(currentPackage.actionButtonText));
        }

        if(!TextUtils.isEmpty(currentPackage.actionButtonTextV2)){
            mViewHolder.mPackageSubscribeText.setText(StringEscapeUtils.unescapeJava(currentPackage.actionButtonTextV2));
        }
        mViewHolder.mPackageSubscribeText.setTag(currentPackage);
        mViewHolder.mPackageSubscribeText.setOnClickListener(mPackSubscribeListener);
        if(position < getCount()-1){
            convertView.findViewById(R.id.divider_view).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.divider_view).setVisibility(View.GONE);
        }
        return convertView;
    }

    public void setPackClickListener(SubscriptionPacksDialog.MessagePostCallback packClickListener) {
        this.packClickListener = packClickListener;
    }

    public class ViewHolder{
        TextView mPackageTitle;
        TextView mPackageDescription;
        TextView mPackageOfferDescription;
        TextView mPackageSubscribeText;
    }


}
