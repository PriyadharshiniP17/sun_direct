package com.myplex.myplex.recievers;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.myplex.api.APIConstants;
import com.myplex.myplex.utils.DownloadUtil;


public class DownloadManagerIntentService extends JobIntentService {

	@Override
	protected void onHandleWork(@NonNull final Intent intent) {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				new DownloadUtil().actionDownloadComplete(getApplicationContext(), intent);
			}
		}, APIConstants.DOWNLOAD_REFRESH_DELAY);
	}
}
