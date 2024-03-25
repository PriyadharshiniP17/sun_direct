package com.myplex.myplex.ui.component;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.model.CardData;
import com.myplex.model.CardDataPackages;
import com.myplex.util.StringEscapeUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

public class PackagesViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = PackagesViewComponent.class.getSimpleName();
    TextView packageSubscribeText;
    RelativeLayout packageSubscribeTextLayout;
    TextView packageTitle;
    TextView packageDescription;
    TextView packageOfferDescription;

    public PackagesViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        packageSubscribeText = view.findViewById(R.id
                .carddetailpack_subscribe_text);
        packageSubscribeTextLayout = view.findViewById(R.id
                .carddetailpack_subscribe_text_layout);
        packageTitle = view.findViewById(R.id.carddetailpack_name);
        packageDescription = view.findViewById(R.id
                .carddetailpack_description);
        packageOfferDescription = view.findViewById(R.id
                .carddetailpack_offer_description);
    }

    public static PackagesViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.carddetailpackages_listitem,
                parent, false);
        PackagesViewComponent briefDescriptionComponent = new PackagesViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        CardDataPackages currentPackage = mValues.get(position).cardDataPackageItem;
        packageSubscribeTextLayout.setVisibility(View.VISIBLE);
        if (currentPackage.displayName != null) {
            packageTitle.setText(currentPackage.displayName);
        }
        if (currentPackage.bbDescription != null) {
            packageDescription.setText(currentPackage.bbDescription);
        }
//            if (currentPackage.cpDescripton != null) {
//                packageOfferDescription.setVisibility(View.VISIBLE);
//                packageOfferDescription.setText(currentPackage.cpDescripton);
//            } else {
        packageOfferDescription.setVisibility(View.GONE);
//            }
//            if (!TextUtils.isEmpty(currentPackage.cpDescriptionV2)) {
//                packageOfferDescription.setVisibility(View.VISIBLE);
//                packageOfferDescription.setText(currentPackage.cpDescriptionV2);
//            }
        /*for (CardDataPackagePriceDetailsItem priceDetails : currentPackage.priceDetails) {
                mViewHolder.mPackageSubscribeText.setText("Rs " + priceDetails.price+"/-");
        }*/
        packageSubscribeText.setTag(currentPackage);
        if (!TextUtils.isEmpty(currentPackage.actionButtonText)) {
            packageSubscribeText.setText(StringEscapeUtils.unescapeJava(currentPackage.actionButtonText));
        }
        if (!TextUtils.isEmpty(currentPackage.actionButtonTextV2)) {
            packageSubscribeText.setText(StringEscapeUtils.unescapeJava(currentPackage.actionButtonTextV2));
        }
    }

}
