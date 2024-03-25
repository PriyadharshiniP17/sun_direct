package com.myplex.myplex.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.DetailsPageDailogAdapter;
import com.myplex.myplex.ui.fragment.epg.EPG;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

/**
 * Created by Uday Kumar V on  03/03/22.
 */
public class DetailsPageDialogFragment extends DialogFragment {
    ViewPager2 viewPager;
    List<CardData> carouselInfoDataList;
    List<EPG.EPGProgram> epgProgramList;
    TextView tvCategoryName;
    TextView dateTxt;
    ImageView iVClose;
    String categoryTitle, dayMonthYear;
    int selectedPosition = 0;
   static Context mContext;
    @SuppressLint("StaticFieldLeak")
    public static DetailsPageDialogFragment instance = null;

    public static DetailsPageDialogFragment getInstance(){
        return instance;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }
    public static DetailsPageDialogFragment newInstance(Context context) {
        mContext = context;
        DetailsPageDialogFragment frag = new DetailsPageDialogFragment();
      /*  Bundle args = new Bundle();
        args.putInt("selectedPosition", selectedPosition);
        args.putSerializable("", carouselInfoDataList);
        frag.setArguments(args);*/
        return frag;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(lp);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable().setColor(getResources().getColor(R.color.black)));
        getDialog().getWindow().setBackgroundDrawableResource(R.color.details_page_background_color);
    }

    public void setData(int selectedPosition, String categoryTitle, List<CardData> carouselInfoDataList) {
        // Required empty public constructor
        this.categoryTitle = categoryTitle;
        this.selectedPosition = selectedPosition;
        this.carouselInfoDataList = carouselInfoDataList;
    }

    public void setData(int selectedPosition, String categoryTitle, List<CardData> carouselInfoDataList, List<EPG.EPGProgram> epgProgramList, String dayMonthYear) {
        // Required empty public constructor
        this.categoryTitle = categoryTitle;
        this.selectedPosition = selectedPosition;
        this.carouselInfoDataList = carouselInfoDataList;
        this.epgProgramList = epgProgramList;
        this.dayMonthYear = dayMonthYear;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vilite_fragment_details_buttom_sheet, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        iVClose = view.findViewById(R.id.iVClose);
        tvCategoryName = view.findViewById(R.id.tvCategoryName);
        dateTxt = view.findViewById(R.id.date_txt);

        if (categoryTitle != null && !categoryTitle.isEmpty())
            tvCategoryName.setText(categoryTitle);

        if (dayMonthYear != null && !dayMonthYear.isEmpty())
            dateTxt.setText(dayMonthYear);

        iVClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bindAdapter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindAdapter();

    }

    private void bindAdapter() {
        if (carouselInfoDataList != null && epgProgramList != null) {
            if(DeviceUtils.isTablet(mContext) ){
                if( ((MainActivity) mContext).getScreenOrientation()== ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                {
                    viewPager.setPadding((int) getResources().getDimension(R.dimen.space_between_details_dialog_land),0,(int) getResources().getDimension(R.dimen.space_between_details_dialog_land),0);
                }else{
                    viewPager.setPadding((int) getResources().getDimension(R.dimen.space_between_details_dialog_portrait),(int) getResources().getDimension(R.dimen._40sdp),(int) getResources().getDimension(R.dimen.space_between_details_dialog_portrait),(int) getResources().getDimension(R.dimen._40sdp));
                }
            }
            DetailsPageDailogAdapter adapter = new DetailsPageDailogAdapter(getActivity(), carouselInfoDataList, epgProgramList);
            adapter.setOnItemClickListener(new DetailsPageDailogAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(final CardData cardData, boolean isFromCatchup) {
                    CarouselInfoData mCarouselInfoData = CacheManager.getCarouselInfoData();
                    if (cardData == null) {
                        return;
                    }
                    dismiss();
                    String publishingHouse = cardData == null
                            || cardData.publishingHouse == null
                            || TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName) ? null : cardData.publishingHouse.publishingHouseName;
                    if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                        HungamaPartnerHandler.launchDetailsPage(cardData, mContext, mCarouselInfoData, null);
                        return;
                    }
                    if (cardData != null
                            && cardData.generalInfo != null
                            && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                            && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                        mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                        return;
                    }
                    showDetailsFragment(cardData, -1, mCarouselInfoData.title, -1, isFromCatchup);
                }
            });
            if(viewPager!=null) {
                viewPager.setAdapter(adapter);
                viewPager.setClipToPadding(false);
                viewPager.setClipChildren(false);
                viewPager.setPageTransformer(new MarginPageTransformer(15));
                //   viewPager.setPadding((int) mContext.getResources().getDimension(R.dimen.margin_gap_16),0,(int) mContext.getResources().getDimension(R.dimen.margin_gap_16),0);
                viewPager.setOffscreenPageLimit(3);
                viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            }
            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            MarginPageTransformer marginPageTransformer = new MarginPageTransformer((int) mContext.getResources().getDimension(R.dimen.margin_gap_16));
   /*     compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = (1)+Math.abs(position);
                page.setScaleX(0.7f + r * 0.16f);
                page.setScaleY(0.85f+ r *0.14f);

            }
        });*/
       /* if(viewPager != null){
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
            layoutParams.setMargins(20,0,20,
                    -1*(int)getResources().getDimension(R.dimen._20sdp));
        }*/
     //   viewPager.setPageTransformer(marginPageTransformer);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(selectedPosition,false);
            }
        }, 50);
    }

    }
    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
          //  Log.d("DetailsPAgeDialogFragment", "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }

        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }
        }

      //  ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carousalTitle);
      /*  if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }*/
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }
    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition, boolean isSupportCatchup) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            //  Log.d("DetailsPAgeDialogFragment", "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }

        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }
        }

        //  ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carousalTitle);
      /*  if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }*/
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putBoolean(CardDetails.PARAM_SUPPORT_CATCHUP, isSupportCatchup);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

}
