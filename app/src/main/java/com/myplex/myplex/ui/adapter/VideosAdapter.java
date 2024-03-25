package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myplex.myplex.R;


/**
 * Created by Apalya on 12/3/2015.
 */
public class VideosAdapter extends BaseAdapter {
    private Context mContext;

    public VideosAdapter(Context activity) {
        mContext = activity;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView             = inflater.inflate(R.layout.view_video_layout,null);
            mViewHolder             = new ViewHolder();
            mViewHolder.epgTxt      = (TextView)convertView.findViewById(R.id.epgTxt);
            mViewHolder.nameTxt     = (TextView)convertView.findViewById(R.id.epgName);
            mViewHolder.placeHolderImg = (ImageView)convertView.findViewById(R.id.placeholder_image);
            mViewHolder.replayBtn   = (ImageView)convertView.findViewById(R.id.replayImg);
            convertView.setTag(mViewHolder);
        }else {
            mViewHolder = (ViewHolder)convertView.getTag();
        }
        return convertView;
    }
    public class ViewHolder {
        ImageView placeHolderImg;
        TextView epgTxt;
        TextView nameTxt;
        ImageView replayBtn;
    }
}
