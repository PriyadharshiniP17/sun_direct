package com.myplex.myplex.ui.views;


import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.myplex.model.CardData;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.adapter.PackagesAdapter;

public class SubscriptionPacksDialog {
	private final Context mContext;

    public interface MessagePostCallback{
        void sendMessage();
    }

    public SubscriptionPacksDialog(Context context){
		this.mContext = context;
	}

	public void showDialog(final CardData mData){

        if(mData.packages == null){
            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);

        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialogView = inflater.inflate(R.layout.dialog_subscriptions_packs, null);

        ListViewMaxHeight mPacksListView  = (ListViewMaxHeight) dialogView.findViewById(R.id.carddetail_list_packages);
        Button cancel  = (Button) dialogView.findViewById(R.id.feedback_cancel_button);
        PackagesAdapter packsAdapter = new PackagesAdapter(mContext,mData.packages);
        mPacksListView.setAdapter(packsAdapter);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        packsAdapter.setPackClickListener(new MessagePostCallback() {
            @Override
            public void sendMessage() {
                if (alertDialog != null) {
                    alertDialog.cancel();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertDialog != null) {
                    alertDialog.cancel();
                }
            }
        });
        if(alertDialog != null
                && !((Activity)mContext).isFinishing()){
            alertDialog.show();
        }
	}
	
}
