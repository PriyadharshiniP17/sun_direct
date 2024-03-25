package com.myplex.myplex.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.RequestMySubscribedPacks;
import com.myplex.api.request.content.RequestUnSubscribePack;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.MySubscribedPacksResponseData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterMyPacks;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Srikanth on 12/3/2015.
 */

public class FragmentMyPacks extends BaseFragment {
    private static final String TAG = FragmentMyPacks.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecylerViewMoviesCarousel;
    private AdapterMyPacks mAdapterMyPacks;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView;
    private ImageView channelImageView;
    private RelativeLayout mRootLayout;
    private Toolbar mToolbar;
    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().finish();
        }
    };
    private ProgressDialog mProgressDialog;
    private RelativeLayout mRelativeNoPack;
    private ImageView mImageRetry;
    private TextView mTExtNoPacks;
    private ImageView mNoPacksImage;
    private LinearLayout browseAllPacksLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_single_recycler, container, false);
        mRecylerViewMoviesCarousel = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        rootView.findViewById(R.id.red_view).setVisibility(View.GONE);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        setUpToolBar();
        mRelativeNoPack = (RelativeLayout) rootView.findViewById(R.id.retry_layout);
        mImageRetry = (ImageView) rootView.findViewById(R.id.imageview_error_retry);
        mNoPacksImage = (ImageView) rootView.findViewById(R.id.imageview_no_packages_available);
        mTExtNoPacks = (TextView) rootView.findViewById(R.id.textview_error_retry);
        browseAllPacksLayout=rootView.findViewById(R.id.browse_all_packs_button_layout);
        mRecylerViewMoviesCarousel.setItemAnimator(null);
        mAdapterMyPacks = new AdapterMyPacks(mContext, loadDummyInfo());
        mRecylerViewMoviesCarousel.setAdapter(mAdapterMyPacks);
        mRecylerViewMoviesCarousel.addItemDecoration(new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_4)));

        browseAllPacksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ip=new Intent(mContext, SubscriptionWebActivity.class);
                ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM,true);
                startActivity(ip);
            }
        });

        loadSubscribedPacks();
        final LinearLayoutManager layoutManager
                = new LinearLayoutManager(mContext);
        mRecylerViewMoviesCarousel.setLayoutManager(layoutManager);
        mAdapterMyPacks.setOnItemClickListenerWithMovieData(new AdapterMyPacks.ItemClickListener() {
            @Override
            public void onClick(View view, final int position, final CardDataPackages cardDataPackage) {
//                unSubscribeForPack(cardDataPackage);
                AlertDialogUtil.showAlertDialog(mContext, mContext.getString(R.string.cancel_subscription_alert), "", false,
                        myplexAPISDK.getApplicationContext().getString(R.string.cancel),
                        myplexAPISDK.getApplicationContext().getString(R.string.un_subscribe)
                        , new AlertDialogUtil.DialogListener() {

                            @Override
                            public void onDialog1Click() {

                            }

                            @Override
                            public void onDialog2Click() {
                                unSubscribeForPack(cardDataPackage, position);
                            }
                        });
            }
        });
        //Log.d(TAG, "onCreateView");
        return rootView;
    }

    private void unSubscribeForPack(CardDataPackages cardDataPackage, final int position) {
        if (cardDataPackage == null) {
            return;
        }
        AlertDialogUtil.showProgressAlertDialog(mContext);
        RequestUnSubscribePack.Params params = new RequestUnSubscribePack.Params(cardDataPackage.packageId, "");
        RequestUnSubscribePack unSubscribePack = new RequestUnSubscribePack(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                AlertDialogUtil.dismissProgressAlertDialog();
                if (response == null || response.body() == null) {
                    //Log.d(TAG, "unSubscribeForPack: null response");
                    return;
                }
                if (response.body().code == 200 || response.body().code == 201) {
                    if (mAdapterMyPacks != null
                            && mAdapterMyPacks.getData() != null) {
                        mAdapterMyPacks.getData().remove(position);
                        mAdapterMyPacks.notifyDataSetChanged();
                    }
                    if (response.body().display) {
//                    AlertDialogUtil.showToastNotification("Un subscription is successfull");
                        AlertDialogUtil.showToastNotification(response.body().message);
                        return;
                    }
                    return;
                }
                AlertDialogUtil.showToastNotification("Un subscription failed message: " + response.body().message);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "unSubscribeForPack: errorCode" + errorCode);
                AlertDialogUtil.dismissProgressAlertDialog();
            }
        });
        APIService.getInstance().execute(unSubscribePack);
    }

    private void toggleNoPacks(boolean isPackAvailable) {
        if (isPackAvailable) {
            mRelativeNoPack.setVisibility(View.GONE);
            mRecylerViewMoviesCarousel.setVisibility(View.VISIBLE);
        } else {
            mRelativeNoPack.setVisibility(View.VISIBLE);
            mRecylerViewMoviesCarousel.setVisibility(View.GONE);
            mImageRetry.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_empty_subscribe));
            mTExtNoPacks.setText(R.string.empty_subscription);
            mTExtNoPacks.setTextColor(mContext.getResources().getColor(R.color.white_60));
            mTExtNoPacks.setTextSize(mContext.getResources().getDimension(R.dimen.textsize_8));
        }
    }

    private void setUpToolBar() {
        mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        channelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        channelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbar.setBackgroundColor(UiUtil.getColor(mContext, R.color.list_item_bkg));
        mToolbarTitle.setText(R.string.subscribed_packs);
        mHeaderImageView.setOnClickListener(mCloseAction);
    }

    private List<CardDataPackages> loadDummyInfo() {
        //Log.d(TAG, "loadDummyInfo");
        List<CardDataPackages> packages = new ArrayList<>();
        Collections.addAll(packages, new CardDataPackages[1]);
        return packages;
    }

    private void loadSubscribedPacks() {
        //Content list call

        RequestMySubscribedPacks mRequestRequestContentList = new RequestMySubscribedPacks(new APICallback<MySubscribedPacksResponseData>() {
            @Override
            public void onResponse(APIResponse<MySubscribedPacksResponseData> response) {
                if (response == null || response.body() == null || response.body().results == null) {
                    toggleNoPacks(false);
                    return;
                }
                toggleNoPacks(true);
                mAdapterMyPacks.setData(response.body().results);
                mRecylerViewMoviesCarousel.setAdapter(mAdapterMyPacks);
                mNoPacksImage.setVisibility(View.GONE);
                browseAllPacksLayout.setVisibility(View.VISIBLE);
                if (mAdapterMyPacks.getData() != null && mAdapterMyPacks.getData().isEmpty()) {
                    mNoPacksImage.setVisibility(View.VISIBLE);
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + response.body().results.size());
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                toggleNoPacks(false);
                //Log.d(TAG, "fetchData: onResponse: t- " + t);
            }
        });
     //   APIService.getInstance().execute(mRequestRequestContentList);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        ScopedBus.getInstance().post(new ChangeMenuVisibility(menuVisible, MainActivity
                .SECTION_OTHER));
        if (menuVisible) {
            //Log.d(TAG, "setMenuVisibility() from movies- " + menuVisible);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");
    }

    public static FragmentMyPacks newInstance() {
        FragmentMyPacks fragmentMovies = new FragmentMyPacks();
        return fragmentMovies;
    }


    public void showProgressBar() {

        if (mContext == null) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
        mProgressDialog.setContentView(R.layout.layout_progress_dialog);
        ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(com.myplex.sdk.R.id.imageView1);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void dismissProgressBar() {
        try {
            if (!isAdded()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
