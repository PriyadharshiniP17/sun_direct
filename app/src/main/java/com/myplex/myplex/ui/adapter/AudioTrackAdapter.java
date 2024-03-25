package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.myplex.myplex.R;
import com.myplex.myplex.media.MediaController2;
import com.myplex.myplex.utils.LangUtil;

import java.util.List;

public class AudioTrackAdapter extends RecyclerView.Adapter<AudioTrackAdapter.ViewHolder>{
    private List<String> listdata;
    private MediaController2.AudioTrackListner audioTrackListner;
    private  int audioPosition;
    private Context mContext;

    // RecyclerView recyclerView;
    public AudioTrackAdapter(List<String> listdata, MediaController2.AudioTrackListner audioTrackListner, int audioPosition, Context context) {
        this.listdata = listdata;
        this.audioTrackListner = audioTrackListner;
        this.audioPosition = audioPosition;
        this.mContext = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.audio_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String myListData = listdata.get(position);
        holder.textView.setText(LangUtil.getSubtitleTrackName(myListData));
        if(audioPosition == position)
            holder.textView.setTextColor(mContext.getResources().getColor(R.color.yellow));
        else
            holder.textView.setTextColor(mContext.getResources().getColor(R.color.white));
        holder.relativeLayout.setTag(position);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() instanceof Integer) {
                   // Toast.makeText(view.getContext(), "click on item: " +(int) view.getTag(), Toast.LENGTH_LONG).show();
                    audioTrackListner.getSelectedItem((int) view.getTag());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relativeLayout);
        }
    }
}
