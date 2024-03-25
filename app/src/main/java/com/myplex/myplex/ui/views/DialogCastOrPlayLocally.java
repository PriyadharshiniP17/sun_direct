package com.myplex.myplex.ui.views;


import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.myplex.myplex.R;

public class DialogCastOrPlayLocally {
    private final Context mContext;
    private OnDailogClickListener onDailogClickListener;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    public interface OnDailogClickListener {
        void onChoosePlayOnTV();

        void dismissProgressBar();
        void onChoosePlayLocally();
    }

    public DialogCastOrPlayLocally(Context context) {
        this.mContext = context;
    }

    public boolean isShowing(){
        return alertDialog != null && alertDialog.isShowing();
    }
    public void showDialog(final OnDailogClickListener onDailogClickListener) {

        this.onDailogClickListener = onDailogClickListener;
        builder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);

        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialogView = inflater.inflate(R.layout.dialog_cast_or_play_locally, null);
        builder.setView(dialogView);

        alertDialog = builder.create();
        Button cancel = (Button) dialogView.findViewById(R.id.feedback_cancel_button);
        alertDialog.setCancelable(false);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDailogClickListener != null) {
                    onDailogClickListener.dismissProgressBar();
                }
                if (alertDialog != null) {
                    alertDialog.cancel();
                }
            }
        });
        dialogView.findViewById(R.id.cast_locally_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDailogClickListener != null) {
                    onDailogClickListener.onChoosePlayLocally();
                }
                if (alertDialog != null) {
                    alertDialog.cancel();
                }
            }
        });
        dialogView.findViewById(R.id.layout_play_on_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDailogClickListener != null) {
                    onDailogClickListener.onChoosePlayOnTV();
                }
                if (alertDialog != null) {
                    alertDialog.cancel();
                }
            }
        });

        if (alertDialog != null
                && !((Activity) mContext).isFinishing()) {
            alertDialog.show();
        }
    }

}
