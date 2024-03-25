package com.myplex.myplex.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.github.pedrovgs.LoggerD;
import com.myplex.myplex.BuildConfig;
import com.myplex.myplex.ui.fragment.FragmentDownloaded;

/**
 * Created by Neosoft on 7/20/2017.
 */

public class MyDownloadsTabPagerAdapter extends FragmentStatePagerAdapter {

    public MyDownloadsTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment mFragment = null;
        switch (position) {
            case 0:
                mFragment = new FragmentDownloaded().newInstance(FragmentDownloaded.TYPE_MOVIES);
                break;
            case 1:
                mFragment = new FragmentDownloaded().newInstance(FragmentDownloaded.TYPE_TV_SHOWS);
                break;
            case 2:
                mFragment = new FragmentDownloaded().newInstance(FragmentDownloaded.TYPE_VIDEOS);
                break;
        }
        return mFragment;
    }

    @Override
    public int getCount() {

        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Movies";
            case 1:
                return "TV Shows";
            case 2:
                return "Videos";
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
