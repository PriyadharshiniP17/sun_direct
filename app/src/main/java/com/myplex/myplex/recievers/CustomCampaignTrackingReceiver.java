package com.myplex.myplex.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;


/*import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.google.android.gms.tagmanager.InstallReferrerReceiver;*/


public class CustomCampaignTrackingReceiver extends BroadcastReceiver {

	private static final String TAG = CustomCampaignTrackingReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		if(intent == null || intent.getExtras() == null) return;
		
		String referrer = intent.getStringExtra("referrer");
		//Log.d(TAG, "Referrer "+referrer);
		if ((!"com.android.vending.INSTALL_REFERRER".equals(intent.getAction()))
				|| (referrer == null)) {
			return;
		}	
		if (TextUtils.isEmpty(referrer)) {
			return;
		}
		/*MixpanelAPI mMixpanel = myplexapplication.getMixPanel();
		final Map<String, String> params = new HashMap<String, String>();
		try {
			ReferralRequestUtil.parseUTM(URLDecoder.decode(referrer, "UTF-8"),
					params);
			if (!params.isEmpty()) {
				if (params.get("utm_source") != null) {
					mMixpanel.getPeople().set("utm_source",
							params.get("utm_source"));
				}
				if (params.get("utm_medium") != null) {
					mMixpanel.getPeople().set("utm_medium",
							params.get("utm_medium"));
				}
				if (params.get("utm_campaign") != null) {
					mMixpanel.getPeople().set("utm_campaign",
							params.get("utm_campaign"));
				}
				if (params.get("utm_content") != null) {
					mMixpanel.getPeople().set("utm_content",
							params.get("utm_content"));
				}
				if (params.get("utm_term") != null) {
					mMixpanel.getPeople().set("utm_term",
                            params.get("utm_term"));
				}
			}
adb shell setprop log.tag.GAv4 DEBUG
    adb logcat -s GAv4
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Log.d(TAG, "Exception " + e);
			e.printStackTrace();
		}
		SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.pref_referrer), referrer);
		InstallReferrerReceiver mixpanelReferrerTracking = new InstallReferrerReceiver();
	    mixpanelReferrerTracking.onReceive(context, intent);*/
//		SharedPrefUtils.writeToSharedPref(context, "referrer", campaign);

		/*try{
			InstallReferrerReceiver mixpanelReferrerTracking = new InstallReferrerReceiver();
			mixpanelReferrerTracking.onReceive(context, intent);

			// Pass along to google
			CampaignTrackingReceiver receiver = new CampaignTrackingReceiver();
			receiver.onReceive(context, intent);

			*//*InstallReferrerBroadcastReceiver clevertapTracker = new InstallReferrerBroadcastReceiver();
			clevertapTracker.onReceive(context,intent);*//*

		}catch (Throwable e){
			e.printStackTrace();
		}*/

	}

}
