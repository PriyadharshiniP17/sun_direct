package com.myplex.myplex.ui.views;


import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.CommentsMessagePost;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.utils.Util;

public class FeedBackDialog {
	private final Context mContext;
	private final String mMessageHint = new String();
    private Button mOkButton;
    private Button mCancelButton;
    private EditText mMessageBox;
    private ProgressBar mProgressBar;
    private String mHeader = new String();
    private String mMessageHeader;
    private SeekBar mRatingBar;
    private int submitProgress =0;
    private int enabledisablePlayerLogsClickCount = 0;
    private View.OnClickListener mListenerPlayerLogs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enabledisablePlayerLogsClickCount++;
            if(enabledisablePlayerLogsClickCount % 6 == 0){

                if (!ApplicationController.SHOW_PLAYER_LOGS) {
                    ApplicationController.SHOW_PLAYER_LOGS = true;
//                        PrefUtils.getInstance().setPrefIsExoplayerEnabled(true);
//                        PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(true);
                    if (ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
                        SDKUtils.captureLogsToSDCard(mContext);
                    }
                    AlertDialogUtil.showToastNotification("Player logs are enabled");
                }else{
                    if (ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
                        SDKUtils.deleteLogFile(mContext);
                    }
                    ApplicationController.SHOW_PLAYER_LOGS = false;
//                        PrefUtils.getInstance().setPrefIsExoplayerEnabled(false);
//                        PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(false);
                    AlertDialogUtil.showToastNotification("Player logs are disabled");
                }
                PrefUtils.getInstance().setPlayerLogs(ApplicationController.SHOW_PLAYER_LOGS);
            }
        }
    };

    public interface MessagePostCallback {
        void sendMessage(boolean status);
    }

    public FeedBackDialog(Context context){
		this.mContext = context;
	}
    private MessagePostCallback mListener;
	public void showDialog(final CardData mData, MessagePostCallback listener){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);

        Analytics.mixpanelInitiatingFeedback();
        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialogView = inflater.inflate(R.layout.feedbacklayout, null);
        TextView mHeadingTextView  = (TextView)dialogView.findViewById(R.id.feedback_heading);
        mHeadingTextView.setText("Rate " + mContext.getString(R.string.app_name));
        mHeadingTextView.setOnClickListener(mListenerPlayerLogs);
        mMessageHeader = mContext.getResources().getString(R.string.feedbackmessageheading);

        TextView mMessageHeadingTextView = (TextView)dialogView.findViewById(R.id
                .feedback_messageheading);
        mMessageHeadingTextView.setText("Rate " + mContext.getString(R.string.app_name));
        if(mData._id.equalsIgnoreCase("0"))
        {
            mMessageHeadingTextView.setText("Share your experience");
        }
        else
        {
            mMessageHeadingTextView.setText(mMessageHeader);
        }

        mCancelButton = (Button)dialogView.findViewById(R.id.feedback_cancel_button);
        mOkButton = (Button)dialogView.findViewById(R.id.feedback_ok_button);

        mMessageBox = (EditText)dialogView.findViewById(R.id.feedback_messagebox);
        mMessageBox.setHint(mMessageHint);
        mProgressBar = (ProgressBar)dialogView.findViewById(R.id.feedback_progressbar);

        mRatingBar = (SeekBar)dialogView.findViewById(R.id.feedback_ratingbar);
        mRatingBar.setProgress(5);
        mRatingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)dialogView.findViewById(R.id.feedback_ratingtext_high)).setText(""+progress);
                submitProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        this.mListener= listener;
        mOkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!Util.isNetworkAvailable(mContext)) {
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                    return;
                }
//                MessagePost post = new MessagePost();
                mProgressBar.setVisibility(View.VISIBLE);
                mOkButton.setText("submitting");
                mOkButton.setEnabled(false);
                mOkButton.setTextColor(mContext.getResources().getColor(R.color.white_50));

                String contentId = null;
                if (mData != null
                        && mData.generalInfo != null
                        && mData.generalInfo.type != null) {
                    if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                            && mData.globalServiceId != null) {
                        contentId = mData.globalServiceId;
                    } else if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                            && mData._id != null) {
                        contentId = mData._id;
                    }
                } else if (mData != null && mData._id != null) {
                    contentId = mData._id;
                }

                Analytics.mixpanelProvidedFeedback(mRatingBar.getProgress());

                CommentsMessagePost.Params commentsPostParams = new CommentsMessagePost.Params(contentId, APIConstants.RATING, mMessageBox.getEditableText().toString(), mRatingBar.getProgress());

                CommentsMessagePost commentsPostRequest = new CommentsMessagePost
                        (commentsPostParams, new APICallback<BaseResponseData>() {
                            @Override
                            public void onResponse(APIResponse<BaseResponseData> response) {
                                alertDialog.cancel();
                                if (response == null
                                        || response.body() == null) {
                                    if (mListener != null) {
                                        mListener.sendMessage(false);
                                        return;
                                        //Analytics.COMMENT_POSTED = mMessageBox.getEditableText().toString();
                                    }
                                }
                                if (response.body().code >= 200 && response.body().code < 300) {
                                    if (mListener != null) {
                                        mListener.sendMessage(true);
                                        //Analytics.COMMENT_POSTED = mMessageBox.getEditableText().toString();
                                    }
                                    return;
                                }
                                if (mListener != null) {
                                    mListener.sendMessage(false);
                                    //Analytics.COMMENT_POSTED = mMessageBox.getEditableText().toString();
                                }

                            }

                            @Override
                            public void onFailure(Throwable t, int errorCode) {
                                alertDialog.dismiss();
                                if (mListener != null) {
                                    mListener.sendMessage(false);
                                    //Analytics.COMMENT_POSTED = mMessageBox.getEditableText().toString();
                                }
                            }
                        });

                APIService.getInstance().execute(commentsPostRequest);

            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(alertDialog != null){
                    alertDialog.dismiss();
                }
                if(mListener != null){
                    mListener.sendMessage(false);
                }
            }
        });
        if(alertDialog != null){
            alertDialog.show();
        }
	}
	
}
