package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.myplex.myplex.R;
import com.myplex.myplex.model.ChannelItem;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Phani on 12/29/2015.
 */
public class ChannelEpgListAdapter extends BaseAdapter {
    private Context mContext;
    private Map<Integer,ChannelItem> channelsList;
    private LayoutInflater inflater;


    public ChannelEpgListAdapter(Context context, Map<Integer, ChannelItem> channelsList) {
        mContext = context;
        this.channelsList = channelsList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return channelsList.size();
    }

    @Override
    public String getItem(int position) {
        if(channelsList.get(position) == null && channelsList.get(position).getChannelImgUrl().trim().length()<0)
            return null ;
        return channelsList.get(position).getChannelImgUrl();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
       // View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_channel_layout,null,false);

           // convertView = inflater.inflate(R.layout.view_channel_layout,null);
            mViewHolder = new ViewHolder();
            mViewHolder.channelImage = (ImageView) convertView.findViewById(R.id.channel_thumbnail_img);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }



        if (channelsList.get(position) == null || channelsList.get(position).getChannelImgUrl() == null || channelsList.get(position).getChannelImgUrl().isEmpty() ) {
            PicassoUtil.with(mContext).load(R.drawable.live_tv_channel_placeholder,mViewHolder.channelImage,R.drawable.live_tv_channel_placeholder);
        }else {
            PicassoUtil.with(mContext).load(getItem(position),mViewHolder.channelImage,R.drawable.live_tv_channel_placeholder);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView channelImage;
    }

    public void updateChannelImages(HashMap<Integer,ChannelItem> channels) {
        channelsList = channels;
        notifyDataSetChanged();
    }
}
