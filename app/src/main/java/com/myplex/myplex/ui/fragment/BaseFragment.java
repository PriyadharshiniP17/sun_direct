package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.myplex.model.CardData;
import com.myplex.myplex.events.MessageEvent;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.utils.DeviceUtils;

/**
 * Created by Apalya on 15-Dec-15.
 */
public class BaseFragment extends Fragment {
    protected Context mContext;
    protected CardData mBaseCardData;
    protected BaseActivity mBaseActivity;
    protected Toolbar mToolBar;
    protected AppCompatActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScopedBus.getInstance().register(this);
        mContext = getActivity();
        mBaseActivity = (BaseActivity) getActivity();
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }else{
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            // ScopedBus.getInstance().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public Context getContext() {
        return mContext;
    }

    public void setBaseActivity(BaseActivity mBaseActivity) {
        this.mBaseActivity = mBaseActivity;
    }

    public void setBaseCardData(CardData mBaseCardData) {
        this.mBaseCardData = mBaseCardData;
    }

    public boolean onBackClicked() {
        return false;
    }

    @Override
    public void onStart() {
        try {
            ScopedBus.getInstance().register(this);
            super.onStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        try {
            ScopedBus.getInstance().unregister(this);
            super.onStop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Called in Android UI's main thread
    public void onEventMainThread(MessageEvent event) {
    }

    public void setToolBar(Toolbar mToolBar) {
        this.mToolBar = mToolBar;
    }

    public Toolbar getToolBar() {
        return mToolBar;
    }

    public void setActionBar() {

    }

}
