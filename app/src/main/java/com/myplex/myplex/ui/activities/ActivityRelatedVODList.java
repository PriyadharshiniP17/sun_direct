package com.myplex.myplex.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.utils.Util;

public class ActivityRelatedVODList extends BaseActivity {
    private BaseFragment mRelatedVODListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_vodlist);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Util.prepareDisplayinfo(this);

        Intent intent = getIntent();
        Bundle args = new Bundle();
        CardData relatedVODData = null;
        if (intent != null && intent.hasExtra(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA)) {
            relatedVODData = (CardData) intent.getSerializableExtra(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA);
        }

        CarouselInfoData carouselInfoData = CacheManager.getCarouselInfoData();
        CacheManager.setCarouselInfoData(carouselInfoData);

        if (intent != null && intent.hasExtra(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL, intent.getBooleanExtra(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL,false));
        }

//        TODO Migration from ActivtyRelatedVodList to FragmentRelatedVODList is done.
//        mRelatedVODListFragment = (FragmentRelatedVODList) FragmentRelatedVODList.instantiate(this,null,args);
        mRelatedVODListFragment.setBaseCardData(relatedVODData);
        mRelatedVODListFragment.setContext(this);
        mRelatedVODListFragment.setBaseActivity(this);
        overlayFragment(mRelatedVODListFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if(mRelatedVODListFragment.onBackClicked()){
            return;
        }
        finish();
        super.onBackPressed();
    }


    public void overlayFragment(BaseFragment fragment) {
        if(fragment == null)
            return;

        fragment.setContext(this);
        try{
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if(fragment instanceof FragmentRelatedVODList){
                transaction.add(R.id.content, fragment);
            }

            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
