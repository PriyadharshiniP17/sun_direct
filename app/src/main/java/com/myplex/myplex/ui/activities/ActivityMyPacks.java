package com.myplex.myplex.ui.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentMyPacks;
import com.myplex.myplex.utils.Util;

public class ActivityMyPacks extends BaseActivity {
    private static final String TAG = ActivityMyPacks.class.getSimpleName();
    private BaseFragment mCurrentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_vodlist);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Util.prepareDisplayinfo(this);
        Bundle args = new Bundle();
        mCurrentFragment = FragmentMyPacks.newInstance();
        mCurrentFragment.setContext(this);
        mCurrentFragment.setBaseActivity(this);
        overlayFragment(mCurrentFragment);
        //Log.d(TAG,"onCreate");
    }

    @Override
    public void setOrientation(int REQUEST_ORIENTATION) {
        setRequestedOrientation(REQUEST_ORIENTATION);
    }

    @Override
    public int getOrientation() {
        return getRequestedOrientation();
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment.onBackClicked()){
            return;
        }
        finish();
        super.onBackPressed();
    }


    public void overlayFragment(BaseFragment fragment) {
        //Log.d(TAG,"overlayFragment");
        if(fragment == null)
            return;

        fragment.setContext(this);
        try{
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.content, fragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

        } catch (Throwable e) {
            //Log.d(TAG,"overlayFragment Throwable- " + e);
            e.printStackTrace();
        }
    }
}
