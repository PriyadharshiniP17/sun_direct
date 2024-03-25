package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.fragment.EpgFragment;

import java.util.ArrayList;


public class EpgCounterPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = EpgCounterPagerAdapter.class.getSimpleName();
    private ArrayList<String >epgList;
    private String date;
    private Context mContext;
    private int datePos;

    public EpgCounterPagerAdapter(FragmentManager childFragmentManager, ArrayList<String> list, String s, Context context, int datePos) {
        super(childFragmentManager);
        epgList = list;
        date = s;
        mContext = context;
        this.datePos = datePos;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment ;
        Bundle args = new Bundle();
        args.putInt("datePos", datePos);
        args.putString("time", epgList.get(position));
        args.putInt("pos", position);
        args.putStringArrayList("list", epgList);
        args.putInt("pageIndex", EPG.globalPageIndex);
        fragment = Fragment.instantiate(mContext, EpgFragment.class.getName(), args);
        return fragment;
    }

    @Override
    public int getCount() {
        return epgList.size();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return epgList.get(position);
    }

    public void updateCurrentDate(int position) {
        datePos = position;
        notifyDataSetChanged();
    }
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Yet another bug in FragmentStatePagerAdapter that destroyItem is called on fragment that hasnt been added. Need to catch
        try {
            super.destroyItem(container, position, object);
        } catch (IllegalStateException ex) {
            //Log.d(TAG, "Exception destroyItem- " + ex.getMessage());
            ex.printStackTrace();
        }catch(Exception e){
            //Log.d(TAG, "Exception destroyItem- " + e.getMessage());
            e.printStackTrace();
        }
    }
}