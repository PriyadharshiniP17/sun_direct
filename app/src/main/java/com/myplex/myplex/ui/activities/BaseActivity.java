package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adobe.mobile.Config;
import com.myplex.model.CardData;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.events.MessageEvent;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.utils.DeviceUtils;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


/**
 * Created by Apalya on 12/10/2015.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    public void onStart() {
        try {
            super.onStart();
            ScopedBus.getInstance().register(this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
//        startDownloadKeys(new DownloadKeys());

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.setContext(this.getApplicationContext());
        if (DeviceUtils.isTabletOrientationEnabled(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Config.collectLifecycleData(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Config.pauseCollectingLifecycleData();
    }

    @Override
    protected void onDestroy() {
        ScopedBus.getInstance().unregister(this);
        super.onDestroy();
    }

    // Called in Android UI's main thread
    public void onEventMainThread(MessageEvent event) {
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public abstract void setOrientation(int value);

    public abstract int getOrientation();


    public abstract void hideActionBar();

    public abstract void showActionBar();

    public void pushFragment(BaseFragment fragment) {
    }

    public void removeFragment(BaseFragment fragment) {
    }

    public void overlayFragment(BaseFragment fragment) {
    }


    public void showDetailsFragment(Bundle args, CardData carouselData) {
        //Log.d(TAG, "showDetailsFragment()" + args);
    }


    /*
        This Event can be fired from any class or Fragment.
        This will handel all the Downloading ogin required for Both MPD and Direct Download
        Only parameters will change for both the downloads.
     */
    public void onEventMainThread(ContentDownloadEvent event) {
        if (event == null || event.cardData == null) {
            return;
        }
////        if (PrefUtils.getInstance().getBoolean("PREF_LOGIN_USER_STATUS", false)) {
//        cardData = event.cardData;
//
//        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
//        mediaLinkparams = new MediaLink.Params(cardData._id, SDKUtils.getInternetConnectivity(this), null, locationInfo);
//        mMedialLink = new MediaLink(mediaLinkparams, mMediaLinkFetchListener);
//        APIService.getInstance().execute(mMedialLink);
        /*} else {
            AlertDialogUtil.showAlertDialog(this, getResources().getString(R.string.action_login_user), "Alert", "Cancel", "Ok", new AlertDialogUtil.DialogListener() {
                @Override
                public void onDialog1Click() {
                }

                @Override
                public void onDialog2Click() {
                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }*/
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}
