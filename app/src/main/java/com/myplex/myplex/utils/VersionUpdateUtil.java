package com.myplex.myplex.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.model.VersionData;;
import com.myplex.util.PrefUtils;

import java.util.Date;

public class VersionUpdateUtil {

	private static long savedSystemTime;
	public static long DAY_MILLIS = 86400000;

	private static String TAG = VersionUpdateUtil.class.getSimpleName();
    private Context mContext;
    private VersionUpdateCallbackListener mListener;
	private long current_time;
    private VersionData mVersionData;

	public interface OnUpdateClickedListener {
		void onUpdateClicked();
	}

	private OnUpdateClickedListener onUpdateClickedListener;

	public void setOnUpdateClickedListener(OnUpdateClickedListener onUpdateClickedListener) {
		this.onUpdateClickedListener = onUpdateClickedListener;
	}

	public interface VersionUpdateCallbackListener {
		boolean showUpgradeDialog();
		void triggerInAppUpdate(boolean isMandatory);
	}

	public VersionUpdateUtil(Context mContext, VersionData data, VersionUpdateCallbackListener listener) {
		this.mListener = listener;
        this.mVersionData = data;
        this.mContext = mContext;
	}

	public void checkIfUpgradeAvailable() {

		Date date = new Date(); // or simply new Date();

		// converting it back to a milliseconds representation:
		current_time = date.getTime();

		if(savedSystemTime == 0)
            savedSystemTime = PrefUtils.getInstance().getLastVersionUpdatedDate();

		if (savedSystemTime == 0) {
            PrefUtils.getInstance().setLastVersionUpdatedDate(current_time);
            savedSystemTime = PrefUtils.getInstance().getLastVersionUpdatedDate();
		}
		if (mVersionData != null && !TextUtils.isEmpty(mVersionData.type)
				&& !(mVersionData.type.equalsIgnoreCase("Mandatory"))) {
			if ((current_time < (savedSystemTime + DAY_MILLIS))) {
				//Log.d(TAG, "Ignore version check");
				return;
			}
		}

        onVersionCheck();

	}
	private void onVersionCheck() {

        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        ApplicationInfo ai = null;
        try {
            packageInfo = packageManager.getPackageInfo(
                    mContext.getPackageName(), 0);
            ai = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (mVersionData == null ) {
			//Log.d(TAG, "Ignore version upgrade bad response");
			return;
		}

		if (mVersionData.version <= packageInfo.versionCode) {
			//Log.d(TAG, "Install version is already updated");
			return;
		}

		if (mListener != null && !mListener.showUpgradeDialog()) {
			return;
		}

		PrefUtils.getInstance().setLastVersionUpdatedDate(current_time);
		final Intent appIntent = new Intent(Intent.ACTION_VIEW);
		if (!TextUtils.isEmpty(mVersionData.type)
				&& (mVersionData.type.equalsIgnoreCase("Mandatory"))
				&& mVersionData.message != null) {
				mListener.triggerInAppUpdate(true);

		} else if (!TextUtils.isEmpty(mVersionData.type)
				&& mVersionData.message != null) {
//            mContext.getResources().getString(R.string)
			mListener.triggerInAppUpdate(false);

		}

	}

}
