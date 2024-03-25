package com.myplex.myplex.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.github.pedrovgs.LoggerD;
import com.myplex.model.CardDataSubtitleItem;
import com.myplex.model.CardDataSubtitles;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;

/**
 * Created by Apalya on 8/8/2018.
 */

public class SubtitlesDialog implements RadioGroup.OnCheckedChangeListener {
    private static final int RADIO_BUTTON_ID = 1000;
    private String subtitleName;
    private final MyplexVideoViewPlayer myplexVideoViewPlayer;
    private final Context mContext;
    private SubTitleSelectionListener subTitleSelectionListener;
    private AlertDialog alertDialog;
    private int checkedItemId = RADIO_BUTTON_ID;
    private int selectedItemId = RADIO_BUTTON_ID;

    public SubtitlesDialog(Context context, MediaController.MediaPlayerControl mPlayer, SubTitleSelectionListener subTitleSelectionListener) {
        this.mContext = context;
        myplexVideoViewPlayer = (MyplexVideoViewPlayer) mPlayer;
        this.subTitleSelectionListener = subTitleSelectionListener;
    }

    public void show() {
        try {
            if (!(mContext instanceof Activity)) {
                LoggerD.debugLog("context is not a activity instance");
                return;
            }
            if (myplexVideoViewPlayer == null) {
                LoggerD.debugLog("invalid player instance");
                return;
            }
            if (mContext == null || ((Activity) mContext).isFinishing()) {
                LoggerD.debugLog("invalid context");
                return;
            }
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);
            dialogBuilder.setTitle(mContext.getString(R.string.text_subtitle_title));
            dialogBuilder.setCancelable(true);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ScrollView customView = new ScrollView(mContext);
            RadioGroup radioGroup = (RadioGroup) inflater.inflate(R.layout.radiobutton_dialog, null);
            dialogBuilder.setPositiveButton(mContext.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (subTitleSelectionListener != null && !TextUtils.isEmpty(subtitleName)) {
                        subTitleSelectionListener.onSubtitleChanged(subtitleName);
                        selectedItemId = checkedItemId;
                    }
                }
            });

            CardDataSubtitles cardDataSubtitles = myplexVideoViewPlayer.getSubtitles();
            radioGroup.setPadding(16, 0, 0, 0);
            RadioButton radioButton = new RadioButton(mContext);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 16, 16, 16);
            radioButton.setPadding(16, 16, 16, 16);
            radioButton.setLayoutParams(params);
            radioButton.setText(mContext.getString(R.string.subtitle_opt_none));
            radioButton.setId(RADIO_BUTTON_ID);
            radioButton.setTextSize(14);
            radioButton.setTextColor(UiUtil.getColor(mContext, R.color.download_item_subtitle_text_color));
            radioButton.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.bg_app_setting_radio_button));
            radioGroup.addView(radioButton);
            for (int i = 0; i < cardDataSubtitles.values.size(); i++) {
                CardDataSubtitleItem cardDataSubtitleItem = cardDataSubtitles.values.get(i);
                radioButton = new RadioButton(mContext);
                params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 16, 16, 16);
                radioButton.setPadding(16, 16, 16, 16);
                radioButton.setLayoutParams(params);
                radioButton.setText(cardDataSubtitleItem.language);
                radioButton.setId(RADIO_BUTTON_ID + i + 1);
                if (PrefUtils.getInstance().getSubtitle() != null && PrefUtils.getInstance().getSubtitle().equalsIgnoreCase(cardDataSubtitleItem.language))
                    checkedItemId = selectedItemId = RADIO_BUTTON_ID + i + 1;
                radioButton.setTextSize(14);
                radioButton.setTextColor(UiUtil.getColor(mContext, R.color.download_item_subtitle_text_color));
                radioButton.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.bg_app_setting_radio_button));
                radioGroup.addView(radioButton);
            }
            radioGroup.setOnCheckedChangeListener(this);
            radioGroup.check(selectedItemId);
            LoggerD.debugLog("checkedItemId- " + checkedItemId + " radioButtonName- " + ((RadioButton) radioGroup.findViewById(checkedItemId)).getText());
            customView.addView(radioGroup);
            alertDialog = dialogBuilder.create();
            alertDialog.setView(customView);
            alertDialog.show();
            Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (pbutton != null)
                pbutton.setTextColor(UiUtil.getColor(mContext, R.color.download_item_subtitle_text_color));
        } catch (Throwable t) {
            t.printStackTrace();
            LoggerD.debugLog("exception " + t.getMessage());
        }
    }

    public void dismiss() {
        if (alertDialog == null) return;
        alertDialog.dismiss();
    }

    public void setSubTitleSelectionListener(SubTitleSelectionListener subTitleSelectionListener) {
        this.subTitleSelectionListener = subTitleSelectionListener;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        RadioButton radioBtn = (RadioButton) radioGroup.findViewById(i);
        subtitleName = radioBtn.getText().toString();
        LoggerD.debugLog("subtitleName- " + subtitleName);
        checkedItemId = i;
        LoggerD.debugLog("checkedItemId- " + checkedItemId);
    }

    public interface SubTitleSelectionListener {
        void onSubtitleChanged(String msg);
    }

    public String getSubtitleName() {
        return subtitleName;
    }


}