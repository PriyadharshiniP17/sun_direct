package com.myplex.myplex.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.OTTAppRequest;
import com.myplex.model.CardDataOttImagesItem;
import com.myplex.model.OTTApp;
import com.myplex.model.OTTAppData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AppsListAdapter;
import com.myplex.myplex.ui.adapter.OTTAppsImageSliderAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import autoscroll.AutoScrollViewPager;

/**
 * Created by Apalya on 12/3/2015.
 */
public class AppsListFragment extends Fragment {
    private static final String TAG = AppsListFragment.class.getSimpleName();

    private ListView mAppsListView;
    private Context mContext;
    private AutoScrollViewPager mViewPager;
    List<OTTApp> mAppsList;
    private TextView mDescriptionTxt;
    private LinearLayout mOfferDecriptionLayout;
    private LinearLayout mPreviewLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_apps,container,false);
        mAppsListView = (ListView) rootView.findViewById(R.id.listView_movie_app);
        mViewPager = (AutoScrollViewPager)rootView.findViewById(R.id.pager_ottapps);
        mDescriptionTxt = (TextView)rootView.findViewById(R.id.slider_title);
        mOfferDecriptionLayout = (LinearLayout)rootView.findViewById(R.id.offer_description_layout);
        mPreviewLayout  = (LinearLayout)rootView.findViewById(R.id.previewLayout);
        mAppsListView.setAdapter(new AppsListAdapter(mContext, getAppAppList()));
        Bundle args = getArguments();
        fetchData(args);
        return rootView;
    }



    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible && mViewPager!=null){
            mViewPager.startAutoScroll();
        }else if(!menuVisible && mViewPager!=null) {
            mViewPager.stopAutoScroll();
        }
        ScopedBus.getInstance().post(new ChangeMenuVisibility(false, MainActivity.SECTION_OTHER));
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
    }

    private void fetchData(final Bundle args) {
        String contentType = "";
        if(args.containsKey(APIConstants.PARAM_APP_FRAG_TYPE)){
            if(args.getInt(APIConstants.PARAM_APP_FRAG_TYPE) == APIConstants.PARAM_APP_FRAG_MOVIE){
               contentType = "movie";
            }else if(args.getInt(APIConstants.PARAM_APP_FRAG_TYPE) == APIConstants.PARAM_APP_FRAG_MUSIC){
               contentType = "music";
            }
        }

        final List<OTTAppsImageSliderAdapter.SliderModel> sliderItems   = new ArrayList<>();

        OTTAppRequest.Params ottAppreqParams = new OTTAppRequest.Params(contentType);
        OTTAppRequest ottAppRequest = new OTTAppRequest(ottAppreqParams, new APICallback<OTTAppData>() {
            @Override
            public void onResponse(APIResponse<OTTAppData> response) {
                   if(response == null || response.body() == null || response.body().results == null)
                       return;
                mAppsList = response.body().results;

                if(mAppsList == null) return;
                mAppsList = response.body().results;

                for (OTTApp app : mAppsList) {
                    if(app.images != null && app.images.values != null){
                        for (CardDataOttImagesItem image : app.images.values){
                            if(image.type != null && image.type.equalsIgnoreCase("thumbnail")){
                                app.imageUrl = image.link;
                            }else if (image.type != null && image.type.equalsIgnoreCase("coverposter")){
                                OTTAppsImageSliderAdapter.SliderModel model = new OTTAppsImageSliderAdapter.SliderModel();
                                model.imageUrl = image.link;
                                model.ottApp = app;
                                model.siblingOrder = image.siblingOrder;
                                model.contentId = image.contentId;
                                model.partnerContentId = image.partnerContentId;
                                sliderItems.add(model);
                            }
                        }
                    }
                }

                mAppsListView.setAdapter(new AppsListAdapter(mContext, mAppsList));



                if(!sliderItems.isEmpty()){
                    mPreviewLayout.setVisibility(View.GONE);
                    mViewPager.setVisibility(View.VISIBLE);
                    OTTAppsImageSliderAdapter ottAppsImageSliderAdapter = new OTTAppsImageSliderAdapter(mContext, new OTTAppsImageSliderAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClicked(OTTAppsImageSliderAdapter.SliderModel sliderModel) {
                            handleOTTAppClicked(sliderModel);
                        }
                    });
                    bannerImagesOrder(sliderItems);
                    ottAppsImageSliderAdapter.setItems(sliderItems);
                    ottAppsImageSliderAdapter.setInfiniteLoop(true);
                    mViewPager.setAdapter(ottAppsImageSliderAdapter);
                    mViewPager.setInterval(3000);
                    mDescriptionTxt.setText(sliderItems.get(0).ottApp.offerDescription);
                    mOfferDecriptionLayout.setVisibility(View.VISIBLE);
                }else {
                    mPreviewLayout.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.GONE);
                    mOfferDecriptionLayout.setVisibility(View.GONE);
                }
            }



            @Override
            public void onFailure(Throwable t, int errorCode) {
                mPreviewLayout.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.GONE);
                mOfferDecriptionLayout.setVisibility(View.GONE);

            }
        });
        APIService.getInstance().execute(ottAppRequest);
    }

    private void bannerImagesOrder(List<OTTAppsImageSliderAdapter.SliderModel> sliderItems) {
        Collections.sort(sliderItems, new Comparator<OTTAppsImageSliderAdapter.SliderModel>() {

            @Override
            public int compare(OTTAppsImageSliderAdapter.SliderModel lhs, OTTAppsImageSliderAdapter.SliderModel rhs) {
                if(lhs == null
                        || rhs == null){
                   return -1;
                }
                if(lhs.siblingOrder == null
                        || rhs.siblingOrder == null){
                    return -1;
                }
            int lhsSiblingOrder = Integer.parseInt(lhs.siblingOrder);
                int rhsSiblingOrder = Integer.parseInt(rhs.siblingOrder);
                return rhsSiblingOrder > lhsSiblingOrder ? 1 : -1;
            }
        });
    }

    private void launchPlayStore(String androidAppUrl) {
        Uri appStoreLink = Uri.parse(androidAppUrl);
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, appStoreLink));
    }

    private List<OTTApp> getAppAppList() {
        if(mAppsList == null){
            mAppsList = new ArrayList<>();
            for(int i = 0; i < 1 ; i++){
                mAppsList.add(new OTTApp());
            }
        }
        return mAppsList;
    }


    private void handleOTTAppClicked(final OTTAppsImageSliderAdapter.SliderModel sliderModel){

        OTTApp appData = null;
        if(sliderModel == null){
            return;
        }
        appData = sliderModel.ottApp;
        PackageManager pm = mContext.getPackageManager();
        try {
            final Intent launchIntent = pm.getLaunchIntentForPackage(appData.androidPackageName);
            if (launchIntent != null) {
                //app is installed launching the app
                mContext.startActivity(launchIntent);
                return;
            }
            String confirmMessage = mContext.getString(R.string
                    .confirm_download_app);
            String alertMessage = appData.confirmationMessage;
            if (alertMessage == null) {
                alertMessage = "Watch free movies on " + appData.title;
            }
            final OTTApp finalAppData = appData;
            AlertDialogUtil.showAlertDialog(mContext, alertMessage,"" ,
                    mContext.getString(R.string.cancel), confirmMessage, new AlertDialogUtil.DialogListener() {
                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {

                            String url = finalAppData.androidAppUrl;

                            if(finalAppData.installType != null && finalAppData.installType.equalsIgnoreCase("ottConfirm")){
                                url = APIConstants.getOTTAppDownloadUrl(finalAppData.appName);
                            }
                            if (url != null) {
                                //app is not installed launching the playstore
                                if(ConnectivityUtil.isConnected(mContext)){
                                    launchPlayStore(url);
                                }else {
                                    Toast.makeText(mContext,getResources().getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause()");
        mViewPager.stopAutoScroll();
        if (getArguments().containsKey(APIConstants.PARAM_APP_FRAG_TYPE)) {
            if (getArguments().getInt(APIConstants.PARAM_APP_FRAG_TYPE) == APIConstants.PARAM_APP_FRAG_MOVIE) {
                Analytics.gaBrowse(Analytics.TYPE_MOVIES, 1l);
            }
        }
    }
}
