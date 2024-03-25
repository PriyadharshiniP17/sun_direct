package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.AlarmData;
import com.myplex.model.CardData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.model.ChannelItem;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.ProgramGuideChannelActivity;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Aplaya on 12/3/2015.
 */
public class EpgListAdapter extends BaseAdapter {
    private Context mContext;
    private List<CardData> mChannelListItems;
    private String serverDateFormat;
    private String time;
    private int pageIndex;
    private int datePos;
    private int currentServingPageIndex = 0;
    private int serPageIndex;
    private int initialProgsSize;

    public EpgListAdapter(Context activity, List<CardData> items, String serverDateFormat, String time, int datePos) {
        mContext = activity;
        mChannelListItems = items;
        this.serverDateFormat = serverDateFormat;
        this.time = time;
        this.datePos = datePos;
        initialProgsSize = mChannelListItems.size();

    }

    @Override
    public int getCount() {
        return mChannelListItems.size();
    }

    @Override
    public CardData getItem(int position) {
        return mChannelListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_epg_channel_layout2, null);
            mViewHolder = new ViewHolder();
            mViewHolder.channelImg = (ImageView) convertView.findViewById(R.id.channel_thumbnail_img);
            mViewHolder.arrowImg = (ImageView) convertView.findViewById(R.id.channel_arrow_img);
            mViewHolder.channel_next_title_txt = (TextView) convertView.findViewById(R.id.channel_nxt_title_txt);
            mViewHolder.channel_status_txt = (TextView) convertView.findViewById(R.id.channel_status_txt);
            mViewHolder.channel_title_txt = (TextView) convertView.findViewById(R.id.channel_title_txt);
            mViewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.customProgress);
            mViewHolder.onNow_txt = (TextView) convertView.findViewById(R.id.channel_status_txt);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        CardData cardData = mChannelListItems.get(position);
       // EPG.globalPageIndex = pageIndex;
        if (cardData.generalInfo != null && cardData.generalInfo.title != null && cardData.generalInfo.title.length() > 0) {
            mViewHolder.channel_title_txt.setText(cardData.generalInfo.title);
        } else {
            pageIndex = (position/10)+1;
            sendServerReq(pageIndex);
            mViewHolder.channel_title_txt.setText(mContext.getResources().getString(R.string.no_info_available));
        }
        if (cardData.nextProgram != null && cardData.nextProgram.length() > 0) {
            mViewHolder.channel_next_title_txt.setText(mContext.getResources().getString(R.string.next_prog_txt, cardData.nextProgram));
        } else {
            mViewHolder.channel_next_title_txt.setText(mContext.getResources().getString(R.string.no_info_available));
        }
        if (cardData.startDate != null && cardData.endDate != null) {
            mViewHolder.mProgressBar.setMax(Util.getTotalDuration(cardData.startDate, cardData.endDate, true));
        } else {
            mViewHolder.mProgressBar.setMax(0);
        }
        if (cardData.startDate != null && cardData.startDate.length() > 0) {
            mViewHolder.mProgressBar.setProgress(Util.getTotalDuration(cardData.startDate, cardData.startDate, false));
        } else {
            mViewHolder.mProgressBar.setProgress(0);
        }
        if (cardData.startDate != null && cardData.endDate != null) {
            Date startDate = Util.getUTCDate(cardData.startDate);
            Date endDate = Util.getUTCDate(cardData.endDate);
            Date currentIndexTime = new Date();
            String startTime = Util.getTimeHHMM(startDate);
            String endTime = Util.getTimeHHMM(endDate);
            mViewHolder.onNow_txt.setVisibility(View.VISIBLE);

            if (startDate.getTime() <= currentIndexTime.getTime() && endDate.getTime() >= currentIndexTime.getTime()) {
                mViewHolder.onNow_txt.setText("Playing Now!");
                mViewHolder.arrowImg.setImageResource(R.drawable.tv_guide_open_arrow_icon);
            } else{
                if (endDate.getTime() >= currentIndexTime.getTime()) {
                    AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(cardData);
                    mViewHolder.arrowImg.setImageResource(R.drawable.epg_set_reminder_icon);
                    if(alarmData != null
                            && alarmData.title != null
                            && alarmData.title.equalsIgnoreCase(cardData.generalInfo.title)){
                        mViewHolder.arrowImg.setImageResource(R.drawable.epg_set_reminder_icon_active);
                    }
                    ReminderListener reminderListener = new ReminderListener(mContext, EpgListAdapter
                            .this,datePos);
                    mViewHolder.arrowImg.setTag(cardData);
                    mViewHolder.arrowImg.setOnClickListener(reminderListener);
                }
                mViewHolder.onNow_txt.setText(startTime + " - " + endTime);

            }
        } else {
            mViewHolder.onNow_txt.setVisibility(View.INVISIBLE);
        }
        mViewHolder.arrowImg.setTag(cardData);
       // mViewHolder.arrowImg.setOnClickListener(mChannelArrowClickListener);
        return convertView;

    }


    public void addData(List<CardData> dataList) {
        mChannelListItems = dataList;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        ImageView channelImg;
        ImageView arrowImg;
        TextView channel_status_txt;
        TextView channel_title_txt;
        TextView channel_next_title_txt;
        TextView onNow_txt;
        ProgressBar mProgressBar;

    }

    private void updatePrograms(int pageIndex, List<CardData> cardDataList) {
        if (cardDataList == null) {
            return;
        }
        System.out.println("phani updateProg "+pageIndex);
        int endLimit = pageIndex * 10;
        int startIndex = endLimit - 10;
        for (int i = 0; i < cardDataList.size(); i++) {
            try {
                mChannelListItems.set(startIndex, cardDataList.get(i));
                startIndex++;
                notifyDataSetChanged();
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }
          //  if(mChannelListItems.size()<startIndex)

        }

          /*  int emptyListCount = mChannelListItems.size()-cardDataList.size();
            List<CardData>dummy = new ArrayList<>();
            for(CardData cardData : mChannelListItems){
                dummy.add(cardData);
            }
            if(emptyListCount>0){
                for(int i =mChannelListItems.size()-1 ;i>=startIndex;i--){
                    mChannelListItems.remove(i);
                }
            }*/



        // mChannelListItems = dummy;
       /* HashMap<Integer,ChannelItem> images = null;
        if(EPG.genreFilterValues.length()>0||EPG.langFilterValues.length()>0){
             HashMap<Integer, ChannelItem>  channelImgData = EPG.getInstance(Util.getCurrentDate(datePos)).fetchAllChannels();
             images =  fetchChannels(mChannelListItems,channelImgData);
            if(images!=null && images.size()>0)
                ScopedBus.getInstance().post(new ChannelsUpdatedEvent(images));
        }*/



    }

    public HashMap<Integer,ChannelItem> fetchChannels(List<CardData> cardList,HashMap<Integer, ChannelItem> channelImgData){
        HashMap<Integer,ChannelItem> channelItemHashMap = new HashMap<>();
        for(int i =0;i<cardList.size();i++){
            CardData cardData = cardList.get(i);
            if(cardData.content!=null){
                for(int j =0 ;j<channelImgData.size();j++){
                    ChannelItem channelItem = channelImgData.get(j);
                    String id = channelItem.getChannelId();
                    if(cardList.get(i).globalServiceId.equals(id)){
                        channelItemHashMap.put(i, channelItem);
                        break;
                    }
                }
            }else {
                channelItemHashMap.put(i,null);
            }
        }

        return channelItemHashMap;
    }
   private void sendServerReq(int pageIndex){
       //new ServerReqAsync().execute();

       if(currentServingPageIndex != pageIndex){
           currentServingPageIndex = pageIndex;
           System.out.println("phani adapter ser req "+pageIndex);
           new ServerReqAsync().execute();
       }

   }

  private class ServerReqAsync extends AsyncTask<Void,Void,List<CardData>>{
       List<CardData> cardList;

      @Override
      protected List<CardData> doInBackground(Void... params) {
          boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && datePos - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;
          EPG.getInstance(Util.getCurrentDate(datePos)).findPrograms(APIConstants.PAGE_INDEX_COUNT, serverDateFormat, time, Util.getCurrentDate(datePos), currentServingPageIndex, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly,"", new EPG.CacheManagerCallback() {

              @Override
              public void OnlineResults(List<CardData> dataList, int pageIndex) {
                  if (dataList != null && dataList.size() > 0) {
                      // updatePrograms(pageIndex, dataList);
                      serPageIndex = pageIndex;
                      Message msg = Message.obtain();
                      msg.obj = dataList;
                      mHandler.sendMessage(msg);
                      cardList = dataList;
                  } else {
                      cardList = null;
                  }
              }

              @Override
              public void OnlineError(Throwable error, int errorCode) {
                  cardList = null;
              }
          });

          return cardList;
      }

  }
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<CardData> cardDataList = (List<CardData>) msg.obj;
            try {
                updatePrograms(serPageIndex, cardDataList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void launchTVGuideChannelActivity(CardData channelData){
        if (channelData == null || channelData.globalServiceId == null) {
            return;
        }
            Intent intent = new Intent(mContext, ProgramGuideChannelActivity.class);
            intent.putExtra(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA,channelData);
            mContext.startActivity(intent);

    }


}
