package com.myplex.myplex.ui.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterGenres;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;

import java.util.List;

public class SmallSquareItemsFragment extends BaseFragment {
    public static final String PARAM_CAROUSEL_INFO_DATA = "carousel_info_data";
    public static final String PARAM_HINDI_VISIBLE = "hindi_title";
    private RecyclerView recyclerView;
    private AdapterGenres mAdapterGenres;
    private Toolbar toolbar;
    private List<CarouselInfoData> mListCarouselInfo;
    CarouselInfoData mCarouselInfoData;
    private boolean isAltTitleVisible = false;
    private View mInflateView;
    private TextView mToolbarTitle,mToolbarTitleOtherLang;
    private ImageView mToolbarCloseButton;
    private ImageView mToolbarListGridLayoutSwitch;
    private ImageView channelImageView;
    private RelativeLayout mRootLayout;
    private ImageView mImageViewFilterIcon;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view  = inflater.inflate(R.layout.fragment_small_square_items,null);
        ((MainActivity) mBaseActivity).disableNavigation();
        setUpViews(view);
        fetchGenreDetails();
        return view;
    }

    public static SmallSquareItemsFragment newInstance(Bundle args) {
        SmallSquareItemsFragment smallSquareItemsFragment = new SmallSquareItemsFragment();
        smallSquareItemsFragment.setArguments(args);
        return smallSquareItemsFragment;
    }


    private void setUpViews(View view ) {

        if (view != null) {
            mCarouselInfoData = (CarouselInfoData) getArguments().getSerializable(PARAM_CAROUSEL_INFO_DATA);
            if(getArguments().containsKey(PARAM_HINDI_VISIBLE)){
                isAltTitleVisible = (boolean)getArguments().getSerializable(PARAM_HINDI_VISIBLE);
            }else{
                isAltTitleVisible = false;
            }

            recyclerView =  view.findViewById(R.id.genres_recycler_view);
            toolbar =  view.findViewById(R.id.toolbar);
            mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar_carousel_view_all, null, false);
            mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
            mToolbarCloseButton = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
            mToolbarTitleOtherLang = (TextView) mInflateView.findViewById(R.id.toolbar_header_title_lang);
            channelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
            mImageViewFilterIcon = (ImageView) mInflateView.findViewById(R.id.toolbar_filter_icon);
            mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
            mToolbarListGridLayoutSwitch = (ImageView) mInflateView.findViewById(R.id.toolbar_list_grid_converter);
            channelImageView.setVisibility(View.GONE);
            mToolbarListGridLayoutSwitch.setVisibility(View.GONE);
            toolbar.addView(mInflateView);

            toolbar.setContentInsetsAbsolute(0, 0);
            if (mCarouselInfoData != null && mCarouselInfoData.title != null) {
                mToolbarTitle.setText(mCarouselInfoData.title);
            }
            if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN){

                if (mCarouselInfoData != null && !TextUtils.isEmpty(mCarouselInfoData.altTitle) ){
                    mToolbarTitleOtherLang.setText(mCarouselInfoData.altTitle);
                    if(isAltTitleVisible) {
                        mToolbarTitleOtherLang.setVisibility(View.VISIBLE);
                    }else{
                        mToolbarTitleOtherLang.setVisibility(View.GONE);
                    }
                }else{
                    mToolbarTitleOtherLang.setVisibility(View.GONE);
                }


            }else{
                mToolbarTitleOtherLang.setVisibility(View.GONE);
            }
            mToolbarCloseButton.setOnClickListener(mCloseAction);
        }
    }

    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseActivity == null || mBaseActivity.isFinishing()) {
                return;
            }
            if (mBaseActivity instanceof MainActivity) {
                ((MainActivity) mBaseActivity).removeFilterFragment();
            }
            ((MainActivity) mBaseActivity).enableNavigation();
            mBaseActivity.removeFragment(SmallSquareItemsFragment.this);
        }
    };

    private void fetchGenreDetails() {
        if (mCarouselInfoData != null && mCarouselInfoData.listNestedCarouselInfoData != null) {
            addCarouselData(mCarouselInfoData.listNestedCarouselInfoData);
            return;
        }
        new MenuDataModel().fetchMenuList(mCarouselInfoData.name, 1, APIConstants.PARAM_CAROUSEL_API_VERSION,new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                addCarouselData(dataList);

            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                addCarouselData(dataList);

            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {

            }
        });

    }

    private void addCarouselData(List<CarouselInfoData> dataList) {
        boolean isGenreScreen = mCarouselInfoData != null && mCarouselInfoData.title != null && mCarouselInfoData.title.contains(APIConstants.GENRE);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), DeviceUtils.isTablet(mContext)?(Util.getNumColumns(mContext)+1):Util.getNumColumns(mContext));
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapterGenres = new AdapterGenres(getContext(),dataList,isGenreScreen);
        if (mCarouselInfoData != null)
        mAdapterGenres.setInfoForAnalyticsEvents(mCarouselInfoData);
        recyclerView.setAdapter(mAdapterGenres);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (recyclerView != null && recyclerView.getLayoutManager() != null) {
            ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanCount(DeviceUtils.isTablet(mContext) ? (Util.getNumColumns(mContext) + 1) : Util.getNumColumns(mContext));
        }
        super.onConfigurationChanged(newConfig);
    }
}
