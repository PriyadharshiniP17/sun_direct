package com.myplex.myplex.ui.component;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;

import java.util.List;

public abstract class CustomDownloadDialogClass extends Dialog  implements
        View.OnClickListener  {
    public MainActivity activity;
    public Activity c;
    public Dialog d;
    public LinearLayout ll_play_now,ll_DownloadPage,ll_Delect_Download;

    public CustomDownloadDialogClass(Activity a, String bd) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }
    public CustomDownloadDialogClass(Activity a){
        super(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_dialog_download_popup);

        ll_play_now=findViewById(R.id.ll_play_now);
        ll_DownloadPage=findViewById(R.id.ll_DownloadPage);
        ll_Delect_Download=findViewById(R.id.ll_delect_download);

        ll_play_now.setOnClickListener(this);
        /*ll_play_now.setOnClickListener(mPlayNowClick);*/
        ll_DownloadPage.setOnClickListener(this);
        ll_Delect_Download.setOnClickListener(this);

    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }




}