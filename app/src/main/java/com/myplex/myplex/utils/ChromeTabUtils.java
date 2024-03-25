package com.myplex.myplex.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsService;

import com.myplex.myplex.R;

import java.util.List;

public class ChromeTabUtils {

    public static void openUrl(Context context, String url) {
        if (isChromeTabSupported(context) && !TextUtils.isEmpty(url)) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    //.addDefaultShareMenuItem()
                    .setDefaultShareMenuItemEnabled(false)
                    .setToolbarColor(context.getResources().getColor(R.color.app_newtheme_color_70))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.back_icon))
                    /*.setStartAnimations(this, android.R.anim.fade_in, android.R.anim.fade_out)
                    .setExitAnimations(this, android.R.anim.fade_in, android.R.anim.fade_out)*/
                    .build();
            customTabsIntent.launchUrl(context, Uri.parse(url));

        } else {
            if (!TextUtils.isEmpty(url)){
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }catch (Exception e) {
                    Log.e("Chrome",e.getMessage());
                }
            }
        }
    }

    private static boolean isChromeTabSupported(Context context) {
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                return true;
            }
        }
        return false;
    }
}
