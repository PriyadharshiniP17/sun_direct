package com.myplex.myplex.recievers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadManagerReceiver extends BroadcastReceiver {

	public static final String TAG = "DownloadManagerReceiver";
	public static final int JOB_ID = 1;
	private static final String EXO_DOWNLOAD_COMPLETE = "exodownload.download_finished";
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			//Log.d(TAG, "DownloadManagerReceiver" + intent);
			//Log.d(TAG, "DownloadManagerReceiver" + intent.getExtras());
			String action = intent.getAction();

			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
				try {
					intent.setClass(context, DownloadManagerIntentService.class);
					DownloadManagerIntentService.enqueueWork(context,DownloadManagerIntentService.class,JOB_ID,intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (EXO_DOWNLOAD_COMPLETE.equals(action)) {
				try {
					intent.setClass(context, DownloadManagerIntentService.class);
					DownloadManagerIntentService.enqueueWork(context,DownloadManagerIntentService.class,JOB_ID,intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (DownloadManager.COLUMN_REASON.equalsIgnoreCase(action)) {
				try {
					intent.setClass(context, DownloadManagerIntentService.class);
					DownloadManagerIntentService.enqueueWork(context,DownloadManagerIntentService.class,JOB_ID,intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
