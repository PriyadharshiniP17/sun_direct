package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.AlarmData;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class AdapterChannelEpg extends BaseAdapter {
    private static final String TAG = AdapterChannelEpg.class.getSimpleName();
    private static final int MAX_COUNT_FOR_PREF_IDS = 60;
    private Context mContext;
    private List<CardData> mChannelEPGlistData;
    private int mEpgDatePosition;
    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof CardData) {
                CardData programData = (CardData) v.getTag();
                CacheManager.setSelectedCardData(programData);

                Bundle args = new Bundle();
                args.putString(CardDetails.PARAM_CARD_ID, programData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != programData.startDate
                        && null != programData.endDate) {
                    Date startDate = Util.getDate(programData.startDate);
                    Date endDate = Util.getDate(programData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                    }
                }
                args.putInt(CardDetails.PARAM_EPG_DATE_POSITION,mEpgDatePosition);
                args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_LIVE);
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_EPG);

                ((BaseActivity)mContext).showDetailsFragment(args, programData);
//                ((BaseActivity)mContext).pushFragment(CardDetails.newInstance(args));
            }

        }
    };


    public AdapterChannelEpg(Context context, List<CardData> channelEPGList, int datePos) {
        mContext = context;
        mChannelEPGlistData = channelEPGList;
        mEpgDatePosition = datePos;
    }

    @Override
    public int getCount() {
        return mChannelEPGlistData.size();
    }

    @Override
    public Object getItem(int position) {
        return mChannelEPGlistData.get(position);
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
            convertView = inflater.inflate(R.layout.listitem_program_guide_channel, null, false);
            mViewHolder = new ViewHolder();
            mViewHolder.mChannelImg = (ImageView) convertView.findViewById(R.id.imageview_thumbnail);
            mViewHolder.mChannelEpgTime = (TextView) convertView.findViewById(R.id
                    .textview_duration);
            mViewHolder.mChannelProgText = (TextView) convertView.findViewById(R.id
                    .textview_title);
            mViewHolder.mReminderImage = (ImageView) convertView.findViewById(R.id
                    .imageview_play_alarm_download);
            mViewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.customProgress);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        CardData cardData = mChannelEPGlistData.get(position);
        if (null == cardData) {
            return convertView;
        }
        if(cardData.globalServiceId != null){
            //Log.d(TAG,"globalServiceId - " + cardData.globalServiceId + " position - " + position + "title- " + cardData.generalInfo.title);
        }
        if (cardData.images != null
                && cardData.images.values != null && cardData.images.values.size() > 0) {
            for (CardDataImagesItem imageItem : cardData.images.values) {

                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
                        && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                    if (imageItem.link == null
                            || imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
                        mViewHolder.mChannelImg.setImageResource(R.drawable
                                .black);
                    } else if (imageItem.link != null) {
                       /* Picasso.with(mContext).load(imageItem.link).error(R.drawable
                                .epg_thumbnail_default).placeholder(R.drawable
                                .epg_thumbnail_default).into(mViewHolder.mChannelImg);*/
                        PicassoUtil.with(mContext).load(imageItem.link, mViewHolder.mChannelImg, R.drawable.black);
                    }
                    break;
                }
            }
        }
        if (null != cardData.startDate
                && null != cardData.endDate) {
            mViewHolder.mChannelEpgTime.setText(Util.getTimeHHMM(Util.getDate(cardData.startDate)) + " - " + Util.getTimeHHMM(Util.getDate(cardData.endDate)));
            Date startDate = Util.getDate(cardData.startDate);
            Date endDate = Util.getDate(cardData.endDate);
            Date currentDate = new Date();

            if (((currentDate.after(startDate)
                    && currentDate.before(endDate))
                    || currentDate.after(endDate))) {
                //milliseconds

                if (mEpgDatePosition - PrefUtils.getInstance().getPrefNoOfPastEpgDays() >= 0 || !PrefUtils.getInstance().getPrefEnablePastEpg()) {
                    final long programEndDurationInMs = endDate.getTime() - currentDate.getTime();
                    //Log.d(TAG, "handler will update the ui after programEndDurationInMs: " + programEndDurationInMs + "from now");
                    if (programEndDurationInMs > 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mHandlerUIUpdate.sendEmptyMessageDelayed(0, programEndDurationInMs);
                            }
                        }).start();
                    }
                }
                mViewHolder.mReminderImage.setImageResource(R.drawable.epg_currently_playing_icon);
                mViewHolder.mReminderImage.setTag(cardData);
                mViewHolder.mReminderImage.setOnClickListener(mPlayListener);
                mViewHolder.mChannelEpgTime.setText("Playing Now!");
                mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
                mViewHolder.mProgressBar.setMax(getTotalDuration(startDate, endDate, true));
                mViewHolder.mProgressBar.setProgress(getTotalDuration(startDate, endDate, false));

            } else {
                mViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(cardData);
                mViewHolder.mReminderImage.setImageResource(R.drawable.epg_set_reminder_icon);
                if(alarmData != null
                        && alarmData.title != null
                        && alarmData.title.equalsIgnoreCase(cardData.generalInfo.title)){
                    mViewHolder.mReminderImage.setImageResource(R.drawable.epg_set_reminder_icon_active);
                }
                ReminderListener reminderListener = new ReminderListener(mContext, AdapterChannelEpg
                        .this,mEpgDatePosition);
                mViewHolder.mReminderImage.setTag(cardData);
                mViewHolder.mReminderImage.setOnClickListener(reminderListener);
            }
        }
        if (null != cardData.generalInfo
                && null != cardData.generalInfo.title) {
            mViewHolder.mChannelProgText.setText(cardData.generalInfo.title);
            //Log.d(TAG,"globalServiceId - " + cardData.globalServiceId + " position - " + position + "title- " + cardData.generalInfo.title);
        }
        return convertView;
    }

    public class ViewHolder {
        ImageView mChannelImg;
        TextView mChannelEpgTime;
        TextView mChannelProgText;
        ImageView mReminderImage;
        ProgressBar mProgressBar;
    }

    public static int getTotalDuration(Date startDate, Date endDate, boolean isTotalDuration) {
        Date date = new Date();
        long diff;
        if (isTotalDuration) {
            diff = endDate.getTime() - startDate.getTime();
        } else {
            diff = date.getTime() - startDate.getTime();
        }
        double diffInHours = diff / ((double) 1000 * 60 * 60);

        int min = (int) Math.round(diffInHours * 60);

        return min;
    }

    private Handler mHandlerUIUpdate = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d(TAG, "mHandlerUIUpdate handleMessage: updating UI");
            notifyDataSetChanged();
        }
    };
}
