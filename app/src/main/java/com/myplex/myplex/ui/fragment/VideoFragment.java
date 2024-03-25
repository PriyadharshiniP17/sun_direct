package com.myplex.myplex.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.myplex.myplex.R;

import com.myplex.myplex.ui.adapter.VideosAdapter;

/**
 * Created by Apalya on 12/3/2015.
 */
public class VideoFragment extends Fragment {
    private ListView mVideoListView;
    private VideosAdapter mVideosAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video,container,false);
        mVideoListView = (ListView)rootView.findViewById(R.id.epg_listView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVideosAdapter = new VideosAdapter(getActivity());
        mVideoListView.setBackgroundColor(Color.DKGRAY);
        mVideoListView.setAdapter(mVideosAdapter);

    }
}
