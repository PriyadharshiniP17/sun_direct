package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;

/**
 * Created by Phani on 12/12/2015.
 */
public class ActivityViewAllCarousel extends BaseActivity {
    private Toolbar mToolbar;
    private View mInflateView;
    private Context mContext;
    private BaseFragment mCurrentFragment;

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            //showOverFlowSettings(v);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        mToolbar =  findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);
        Bundle args = new Bundle();

        CarouselInfoData carouselInfoData = CacheManager.getCarouselInfoData();
        CacheManager.setCarouselInfoData(carouselInfoData);

        if (getIntent() != null && getIntent().hasExtra(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, getIntent().getStringExtra(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE));
        }
        mCurrentFragment.setContext(mContext);
        mCurrentFragment.setBaseActivity(this);
        overlayFragment(mCurrentFragment);
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void overlayFragment(BaseFragment fragment) {
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
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment.onBackClicked()){
            return;
        }
        finish();
        super.onBackPressed();
    }

}
