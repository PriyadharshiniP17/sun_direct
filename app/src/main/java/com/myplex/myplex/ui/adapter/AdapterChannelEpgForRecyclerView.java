package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class AdapterChannelEpgForRecyclerView extends RecyclerView.Adapter<AdapterChannelEpgForRecyclerView.ChannelEPGViewHolder> {
    private static final String TAG = AdapterChannelEpgForRecyclerView.class.getSimpleName();
    private static final int MAX_COUNT_FOR_PREF_IDS = 60;
    private Context mContext;
    private List<CardData> mListChannelEPGData;
    private int mEpgDatePosition;

    private Handler mHandlerUIUpdate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d(TAG, "mHandlerUIUpdate handleMessage: updating UI");
            if (getItemCount() == 0) return;
            notifyItemChanged(0);
        }
    };

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
                args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DETAILS);
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_EPG);

                ((BaseActivity)mContext).showDetailsFragment(args, programData);
//                ((BaseActivity)mContext).pushFragment(CardDetails.newInstance(args));
            }

        }
    };


    public AdapterChannelEpgForRecyclerView(Context context, List<CardData> channelEPGList, int datePos) {
        mContext = context;
        mListChannelEPGData = channelEPGList;
        mEpgDatePosition = datePos;
    }

    @Override
    public ChannelEPGViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChannelEPGViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_program_guide_channel, parent, false));
    }

    @Override
    public void onBindViewHolder(ChannelEPGViewHolder mViewHolder, int position) {

        CardData cardData = mListChannelEPGData.get(position);
        if (null == cardData) {
            return;
        }
        if(cardData.globalServiceId != null){
            //Log.d(TAG,"globalServiceId - " + cardData.globalServiceId + " position - " + position);
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
                        /*Picasso.with(mContext).load(imageItem.link).error(R.drawable
                                .epg_thumbnail_default).placeholder(R.drawable
                                .epg_thumbnail_default).into(mViewHolder.mChannelImg);*/
                        PicassoUtil.with(mContext).load(imageItem.link, mViewHolder.mChannelImg, R.drawable.black);
                    }
                    break;
                }
            }
        }
        mViewHolder.mLiveIndicatorImage.setVisibility(View.GONE);
        if (null != cardData.startDate
                && null != cardData.endDate) {
            mViewHolder.mChannelEpgTime.setText(Util.getTimeHHMM(Util.getDate(cardData.startDate)) + " - " + Util.getTimeHHMM(Util.getDate(cardData.endDate)));
            mViewHolder.mChannelEpgTime.requestLayout();
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
                                mHandlerUIUpdate.sendEmptyMessageDelayed(0, 5000);
                            }
                        }).start();
                    }
                }
                mViewHolder.mLiveIndicatorImage.setVisibility(View.VISIBLE);
                mViewHolder.mReminderImage.setImageResource(R.drawable.thumbnail_overlay_play_icon);
                mViewHolder.mReminderImage.setTag(cardData);
                mViewHolder.mReminderImage.setOnClickListener(mPlayListener);
                mViewHolder.mChannelEpgTime.setText("Playing Now!");
                mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
                mViewHolder.mProgressBar.setMax(getTotalDuration(startDate, endDate, true));
                mViewHolder.mProgressBar.setProgress(getTotalDuration(startDate, endDate, false));

            } else {
                mViewHolder.mLiveIndicatorImage.setVisibility(View.GONE);
                mViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(cardData);
                mViewHolder.mReminderImage.setImageResource(R.drawable.thumbnail_overlay_reminder_icon_default);
                if(alarmData != null
                        && alarmData.title != null
                        && alarmData.title.equalsIgnoreCase(cardData.generalInfo.title)){
                    mViewHolder.mReminderImage.setImageResource(R.drawable.thumbnail_overlay_reminder_icon_highlight);
                }
                ReminderListener reminderListener = new ReminderListener(mContext, AdapterChannelEpgForRecyclerView
                        .this,mEpgDatePosition);
                mViewHolder.mReminderImage.setTag(cardData);
                mViewHolder.mReminderImage.setOnClickListener(reminderListener);
            }
        }
        if (null != cardData.generalInfo
                && null != cardData.generalInfo.title) {
           /* if (title.length() > 19) {
                title = title.substring(0, 12) + "..." + title.substring(16, 18);
            }*/
            mViewHolder.mChannelProgText.setText(cardData.generalInfo.title);
            mViewHolder.mChannelProgText.requestLayout();
            mViewHolder.mChannelProgText.postInvalidate();
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if(mListChannelEPGData == null){
            return 0;
        }
        return mListChannelEPGData.size();
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

    public class ChannelEPGViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        RelativeLayout mLiveIndicatorImage;
        ImageView mChannelImg;
        TextView mChannelEpgTime;
        TextView mChannelProgText;
        ImageView mReminderImage;
        ProgressBar mProgressBar;

        public ChannelEPGViewHolder(View itemView) {
            super(itemView);
            mChannelImg = (ImageView) itemView.findViewById(R.id.imageview_thumbnail);
            mChannelEpgTime = (TextView) itemView.findViewById(R.id
                    .textview_duration);
            mChannelProgText = (TextView) itemView.findViewById(R.id
                    .textview_title);
            mReminderImage = (ImageView) itemView.findViewById(R.id
                    .imageview_play_alarm_download);
            mLiveIndicatorImage = (RelativeLayout) itemView.findViewById(R.id
                    .live_indicator);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.customProgress);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListChannelEPGData == null || mListChannelEPGData.isEmpty()
                    || getAdapterPosition() >= mListChannelEPGData.size()){
                return;
            }

            try {
                CardData programData = mListChannelEPGData.get(getAdapterPosition());
                if (null != programData.startDate
                        && null != programData.endDate) {
                    Date startDate = Util.getDate(programData.startDate);
                    Date endDate = Util.getDate(programData.endDate);
                    Date currentDate = new Date();
                    if (!(currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || !currentDate.after(endDate)) {
                        view.findViewById(R.id.imageview_play_alarm_download).performClick();
                        return;
                    }
                }
                showDetailsFragment(programData);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private void showDetailsFragment(CardData programData) {
        //Log.d(TAG, "onItemClick");

        if (programData == null || programData.globalServiceId == null) {
            return;
        }
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
                args.putBoolean(CardDetails
                        .PARAM_AUTO_PLAY_MINIMIZED, false);
            }
        }
        args.putInt(CardDetails.PARAM_EPG_DATE_POSITION,mEpgDatePosition);
        String adProvider = null;
        boolean adEnabled = false;
        if (programData != null
                && programData.content != null){
            if (!TextUtils.isEmpty(programData.content.adProvider)) {
                adProvider = programData.content.adProvider;
            }
            adEnabled = programData.content.adEnabled;
        }
        args.putString(CardDetails.PARAM_AD_PROVIDER, adProvider);
        args.putBoolean(CardDetails.PARAM_AD_ENBLED, adEnabled);

        ((MainActivity)mContext).showDetailsFragment(args, programData);
//            mBaseActivity.pushFragment(CardDetails.newInstance(args));

    }
}
