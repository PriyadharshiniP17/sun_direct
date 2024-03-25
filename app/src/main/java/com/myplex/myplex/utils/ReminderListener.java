package com.myplex.myplex.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.myplex.model.AlarmData;
import com.myplex.model.CardData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventNotifyEpgAdapter;
import com.myplex.myplex.ui.adapter.AdapterChannelEpgForRecyclerView;
import com.myplex.util.SDKUtils;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class ReminderListener implements View.OnClickListener{
    private AdapterChannelEpgForRecyclerView mAdapterChannelEpgForRecyclerView = null;
    private BaseAdapter mAdapter;
    private int mDatePosition;
    private Context mContext;

    public ReminderListener(Context context, Object adapter,int datePosition){
        mContext = context;
        if(adapter instanceof BaseAdapter){
            mAdapter = (BaseAdapter) adapter;
        } else if(adapter instanceof AdapterChannelEpgForRecyclerView){
            mAdapterChannelEpgForRecyclerView = (AdapterChannelEpgForRecyclerView) adapter;
        }
        if(datePosition>0) {
            mDatePosition = datePosition;
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof CardData) {
            final ImageView reminderImage = (ImageView) v;
          //  final LinearLayout reminderImage = (LinearLayout) v;
            final CardData programData = (CardData) v.getTag();
            handleProgramData(reminderImage, programData);
        }
    }
    boolean isAvailable ;
    private void handleProgramData(final ImageView reminderImage, final CardData programData) {
        if (programData == null
                || programData.generalInfo == null
                || programData.generalInfo.title == null) {
            return;
        }

        ArrayList<String> nxtDateList = Util.showNextDates();
        String title = mContext.getString(R.string.reminder_title);
        String positiveButtonText = mContext.getString(R.string.confirm);
        String negativeButtonText = mContext.getString(R.string.cancel);
        String alertMessage = "";
        if (mDatePosition < nxtDateList.size()) {
            String date = nxtDateList.get(mDatePosition);
            isAvailable = false;
            String dateString;
            if (date.contains("Today")) {
                dateString = alertMessage + " " + "Today";
            } else {
                dateString = alertMessage + " on " + date;
            }
            AlarmData previousAlarm = Util.getReminderProgmaNameIfExistAtThisTime(programData);
            String alreadyAvailableProgramName = null;
            if (previousAlarm != null
                    && previousAlarm.title != null) {
                alreadyAvailableProgramName = previousAlarm.title;
            }
            alertMessage = mContext.getResources().getString(R.string.reminder_message) + " " + programData.generalInfo.title + ", starting at " + Util.getTimeHHMM(Util
                    .getDate(programData.startDate)) + dateString + mContext.getResources().getString(R.string.reminder_message_part_press_confirm);
            if (alreadyAvailableProgramName != null) {
                if (alreadyAvailableProgramName.equalsIgnoreCase(programData.generalInfo.title)) {
                    alertMessage = mContext.getResources().getString(R.string

                            .reminder_message_already_available_same_program) + " " +
                            alreadyAvailableProgramName + ", starting at " +
                            Util.getTimeHHMM(Util.getDate(programData.startDate)) + " " + dateString +
                            ".";
                    positiveButtonText = "Cancel Reminder";
                    negativeButtonText = "Ok";
                    isAvailable = true;
                } else {
                    alertMessage = mContext.getResources().getString(R.string.reminder_message_already_available)
                            + " " +
                            alreadyAvailableProgramName + ", would you like to replace " +
                            "previous reminder with " + programData.generalInfo.title +
                            ", starting at " + Util.getTimeHHMM(Util.getDate(programData
                            .startDate)) + " " + dateString + ".";
                }
            }
            int positionOfDayInDate;
            SpannableString cs = new SpannableString(alertMessage);
            if (!date.contains("Today")) {
                positionOfDayInDate = alertMessage.indexOf(date) + 7;
                cs.setSpan(new SuperscriptSpan(), positionOfDayInDate, positionOfDayInDate + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                cs.setSpan(new RelativeSizeSpan(0.7f), positionOfDayInDate, positionOfDayInDate + 2, 0);
            }

            AlertDialogUtil.showReminderAlertDialog(mContext, cs.toString(), title, negativeButtonText, positiveButtonText, new AlertDialogUtil.DialogListener() {
                @Override
                public void onDialog1Click() {

                }

                @Override
                public void onDialog2Click() {
                    if (isAvailable) {
                        Util.updateAalarmTimes(programData, true);
                        Util.cancelReminder(mContext, programData.generalInfo.title, programData.globalServiceId,
                                Util.getDate(programData.startDate), mContext.getString(R.string.notification_livetv_message), programData.generalInfo.type);
                        if (reminderImage != null) {
                            reminderImage.setColorFilter(SDKUtils.getColor(mContext, R.color.white), PorterDuff.Mode.MULTIPLY);
                        }
                    } else {
                        if (reminderImage != null) {
                            reminderImage.setColorFilter(SDKUtils.getColor(mContext, R.color.theme_app_color), PorterDuff.Mode.MULTIPLY);
                        }
                        Util.updateAalarmTimes(programData, false);
                        Util.setReminder(mContext, programData.generalInfo.title, programData.globalServiceId,
                                Util.getDate(programData.startDate), mContext.getString(R.string.notification_livetv_message), programData.generalInfo.type, programData.startDate);
                        AlertDialogUtil.showToastNotification("Reminder Set");
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        if (mAdapterChannelEpgForRecyclerView != null) {
                            mAdapterChannelEpgForRecyclerView.notifyDataSetChanged();
                        }
                        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_EPG_REMINDER_SET);
                        EventBus.getDefault().post(new EventNotifyEpgAdapter());
                    }
                }
            });
        }
    }
}
