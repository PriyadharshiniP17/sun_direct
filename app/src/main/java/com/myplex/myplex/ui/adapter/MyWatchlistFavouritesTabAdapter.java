package com.myplex.myplex.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.github.pedrovgs.LoggerD;

/**
 * Created by Ramraju on 2/20/2018.
 */

public class MyWatchlistFavouritesTabAdapter extends FragmentStatePagerAdapter {

    int requestType;

    public void setTabName(String[] tabName) {
        this.tabName = tabName;
    }

    private String[] tabName;

    public MyWatchlistFavouritesTabAdapter(FragmentManager fm,int requestType) {
        super(fm);
        this.requestType=requestType;
    }

    @Override
    public Fragment getItem(int position) {
        FragmentWatchlistFavourites mFragment = null;
        switch (position) {
            case 0:
                mFragment = new FragmentWatchlistFavourites().newInstance(FragmentWatchlistFavourites.TYPE_MOVIES,requestType);
                break;
            case 1:
                mFragment = new FragmentWatchlistFavourites().newInstance(FragmentWatchlistFavourites.TYPE_TV_SHOWS,requestType);
                break;
            case 2:
                mFragment = new FragmentWatchlistFavourites().newInstance(FragmentWatchlistFavourites.TYPE_MUSIC,requestType);
                break;
            case 3:
                mFragment = new FragmentWatchlistFavourites().newInstance(FragmentWatchlistFavourites.TYPE_NEWS,requestType);
                break;

        }
        if (mFragment != null) {
            mFragment.setTabName(tabName);
        }
        return mFragment;
    }

    @Override
    public int getCount(){
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Movies";
            case 1:
                return "Programs";
            case 2:
                return "Music";
            case 3:
                return "News";
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Yet another bug in FragmentStatePagerAdapter that destroyItem is called on fragment that hasnt been added. Need to catch
        try {
            super.destroyItem(container, position, object);
        } catch (IllegalStateException ex) {
            LoggerD.debugDownload("Exception destroyItem- " + ex.getMessage());
            ex.printStackTrace();
        }catch(Exception e){
            LoggerD.debugDownload("Exception destroyItem- " + e.getMessage());
            e.printStackTrace();
        }
    }

}
