package com.myplex.myplex.ui.views;


import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.CommentsMessagePost;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.Util;

public class CommentDialog {
	private final Context mContext;
	private final String mMessageHint = new String();
    private Button mOkButton;
    private Button mCancelButton;
    private EditText mMessageBox;
    private ProgressBar mProgressBar;

    public interface MessagePostCallback{
        void sendMessage(boolean status);
    }

    public CommentDialog(Context context){
		this.mContext = context;
	}
    private MessagePostCallback mListener;
	public void showDialog(final CardData mData, MessagePostCallback listener){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);

        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialogView = inflater.inflate(R.layout.commentlayout, null);
        mCancelButton = (Button)dialogView.findViewById(R.id.feedback_cancel_button);
        mOkButton = (Button)dialogView.findViewById(R.id.feedback_ok_button);

        mMessageBox = (EditText)dialogView.findViewById(R.id.feedback_messagebox);
        mMessageBox.setHint(mMessageHint);
        mProgressBar = (ProgressBar)dialogView.findViewById(R.id.feedback_progressbar);

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
                mOkButton.setText(mContext.getString(R.string.comment_ok_adding));
                mOkButton.setEnabled(false);
                mOkButton.setTextColor(mContext.getResources().getColor(R.color.white_50));
                String contentId = null;
                if (mData != null
                        && mData.generalInfo != null
                        && mData.generalInfo.type != null
                        && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                        && mData.globalServiceId != null) {
                    contentId = mData.globalServiceId;
                } else if (mData._id != null) {
                    contentId = mData._id;
                }

                CommentsMessagePost.Params commentsPostParams = new CommentsMessagePost.Params
                        (contentId,APIConstants.COMMENT,mMessageBox.getEditableText()
                                .toString());

                CommentsMessagePost commentsPostRequest = new CommentsMessagePost
                        (commentsPostParams, new APICallback<BaseResponseData>() {
                            @Override
                            public void onResponse(APIResponse<BaseResponseData> response) {
                                alertDialog.cancel();
                                if(response == null
                                        || response.body() == null){
                                    if (mListener != null) {
                                        mListener.sendMessage(false);
                                        return;
                                        //Analytics.COMMENT_POSTED = mMessageBox.getEditableText().toString();
                                    }
                                }
                                if(response.body().code >= 200 &&  response.body().code < 300){
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
