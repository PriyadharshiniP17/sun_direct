package com.myplex.myplex.ui.component;

import android.content.Context;
import android.os.Bundle;
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
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

public class EPGViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = EPGViewComponent.class.getSimpleName();
    RelativeLayout mLiveIndicatorImage;
    ImageView mChannelImg;
    TextView mChannelEpgTime;
    TextView mChannelProgText;
    ImageView mReminderImage;
    ProgressBar mProgressBar;
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
                args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DETAILS);
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_EPG);

                ((BaseActivity)mContext).showDetailsFragment(args, programData);
//                ((BaseActivity)mContext).pushFragment(CardDetails.newInstance(args));
            }

        }
    };



    public EPGViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        mChannelImg = itemView.findViewById(R.id.imageview_thumbnail);
        mChannelEpgTime = itemView.findViewById(R.id
                .textview_duration);
        mChannelProgText = itemView.findViewById(R.id
                .textview_title);
        mReminderImage = itemView.findViewById(R.id
                .imageview_play_alarm_download);
        mLiveIndicatorImage = itemView.findViewById(R.id
                .live_indicator);
        mProgressBar = itemView.findViewById(R.id.customProgress);
//        mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static EPGViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_program_guide_channel,
                parent, false);
        EPGViewComponent briefDescriptionComponent = new EPGViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        this.mData = mValues.get(position).cardData;
        if (mData == null) {
            return;
        }
        if(mData.globalServiceId != null){
            //Log.d(TAG,"globalServiceId - " + mData.globalServiceId + " position - " + position);
        }
        if (mData.images != null
                && mData.images.values != null && mData.images.values.size() > 0) {
            for (CardDataImagesItem imageItem : mData.images.values) {

                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
                        && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                    if (imageItem.link == null
                            || imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
                        mChannelImg.setImageResource(R.drawable
                                .black);
                    } else if (imageItem.link != null) {
                        PicassoUtil.with(mContext).load(imageItem.link,mChannelImg,R.drawable.black);
                    }
                    break;
                }
            }
        }
        mLiveIndicatorImage.setVisibility(View.GONE);
        if (null != mData.startDate
                && null != mData.endDate) {
            mChannelEpgTime.setText(Util.getTimeHHMM(Util.getDate(mData.startDate)) + " - " + Util.getTimeHHMM(Util.getDate(mData.endDate)));
            mChannelEpgTime.requestLayout();
            Date startDate = Util.getDate(mData.startDate);
            Date endDate = Util.getDate(mData.endDate);
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
//                                mHandlerUIUpdate.sendEmptyMessageDelayed(0, 5000);
                            }
                        }).start();
                    }
                }
                mLiveIndicatorImage.setVisibility(View.VISIBLE);
                mReminderImage.setImageResource(R.drawable.thumbnail_overlay_play_icon);
                mReminderImage.setTag(mData);
                mReminderImage.setOnClickListener(mPlayListener);
                mChannelEpgTime.setText("Playing Now!");
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setMax(getTotalDuration(startDate, endDate, true));
                mProgressBar.setProgress(getTotalDuration(startDate, endDate, false));

            } else {
                mLiveIndicatorImage.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.INVISIBLE);
                AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(mData);
                mReminderImage.setImageResource(R.drawable.thumbnail_overlay_reminder_icon_default);
                if(alarmData != null
                        && alarmData.title != null
                        && alarmData.title.equalsIgnoreCase(mData.generalInfo.title)){
                    mReminderImage.setImageResource(R.drawable.thumbnail_overlay_reminder_icon_highlight);
                }
                ReminderListener reminderListener = new ReminderListener(mContext, EPGViewComponent
                        .this,mEpgDatePosition);
                mReminderImage.setTag(mData);
                mReminderImage.setOnClickListener(reminderListener);
            }
        }
        if (null != mData.generalInfo
                && null != mData.generalInfo.title) {
           /* if (title.length() > 19) {
                title = title.substring(0, 12) + "..." + title.substring(16, 18);
            }*/
            mChannelProgText.setText(mData.generalInfo.title);
            mChannelProgText.requestLayout();
            mChannelProgText.postInvalidate();
        }
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


}
