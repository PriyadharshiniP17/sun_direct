package com.myplex.myplex.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;


import java.util.Date;

public class VmaxInterstitialAdFragment extends BaseFragment {
    private Context mContext;
    View rootView;
    private boolean isToShowAdForFirst;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        rootView = inflater.inflate(R.layout.fragment_vmax_interstitial_ad, container, false);
        cacheInterstitial();
        return rootView;
    }


    /**
     * Method for adding Interstitial
     */
    public void cacheInterstitial() {
        //if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxInterStitialAdId()) || !PrefUtils.getInstance().getPrefEnableVmaxInterStitialAd()) {
        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxInterStitialOpenAdId()) || !PrefUtils.getInstance().getPrefEnableVmaxInterStitialAppOpenAd()) {
            SDKLogger.debug("Invalid AdSpotId or Interstitial Ads are disabled getPrefVmaxInterStitialAdId- " + PrefUtils.getInstance().getPrefVmaxInterStitialOpenAdId()
                    + " getPrefEnableVmaxInterStitialAd- " + PrefUtils.getInstance().getPrefEnableVmaxInterStitialAppOpenAd());
            return;
        }
        try {

            SDKLogger.debug("loading the ads");
            /** Initializing vmaxBannerAdView with an Adspot,*/

            /** To Fetch Your AdvId you can check your device's Google settings under ads subMenu Or You can Run this app Once and check
             * the logs for 'AdRequested with url' under the tag vmax, from the url your Advid
             * would be one of the parameters in the post request eg. advid=2cf626f0-08ac-4a4d-933c-00ecd0256cf4*/

/** DON'T INCLUDE vmaxBannerAdView.setTestDevices() WHILE GOING LIVE WITH YOUR PROJECT AS THIS SERVES ONLY TEST ADS;*/
//        vmaxInterStitialAdView.setTestDevices(VmaxAdView.TEST_via_ADVID,"<REPLACE WITH YOUR ADVID>");
//        vmaxInterStitialAdView.setTestDevices(VmaxAdView.TEST_via_ADVID, "efee1d0d-27e6-4095-8aaa-5b603d87d145");



        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private boolean checkIsAdShownToday() {

        Date date = new Date(); // or simply new Date();

        // converting it back to a milliseconds representation:
        long current_time = date.getTime();
        SDKLogger.debug("interstrialAd current_time-  " + current_time);


        long savedSystemTime = PrefUtils.getInstance().getLastVMXAdShownDate();

        if (savedSystemTime == 0) {
            isToShowAdForFirst = true;
            PrefUtils.getInstance().setLastVMXAdShownDate(current_time);
            return false;
        }
        savedSystemTime = PrefUtils.getInstance().getLastVMXAdShownDate();
        long timeOffSetMillis = PrefUtils.getInstance().getPrefVmaxInterStitialAdFrequency();
        SDKLogger.debug("savedSystemTime- " + savedSystemTime + " timeOffSetMillis- " + timeOffSetMillis + " current_time- " + current_time);
        if ((current_time < (savedSystemTime + timeOffSetMillis))) {
            SDKLogger.debug("Interstial Ad already shown ignore for now");
            return true;
        }
        PrefUtils.getInstance().setLastVMXAdShownDate(current_time);
        return false;

    }

    public static BaseFragment newInstance(Bundle args) {
        VmaxInterstitialAdFragment vmaxInterstitialAdFragment = new VmaxInterstitialAdFragment();
        vmaxInterstitialAdFragment.setArguments(args);
        return vmaxInterstitialAdFragment;
    }
}
