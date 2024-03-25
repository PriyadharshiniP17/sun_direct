package com.myplex.myplex.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;

import com.myplex.api.APIConstants;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;

import java.util.Collections;


/*
    A singleton Class used to add,update, delete dynamic shortcuts
    @param Context for creating a Shortcut Manager
 */
public class DynamicShortCutUtil {

    private static DynamicShortCutUtil _shortCutManager;

    //Get instance of this class to call the methods
    public static DynamicShortCutUtil getInstance(){
        if(_shortCutManager == null){
            _shortCutManager = new DynamicShortCutUtil();
        }
        return _shortCutManager;

    }

    private ShortcutManager getShortcutManager(Context mContext){
        ShortcutManager shortcutManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            shortcutManager = mContext.getSystemService(ShortcutManager.class);
        }
        return shortcutManager;
    }

    public void addContinueWatchingShortCut(Context mContext,String contentId){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            Intent intent = new Intent(mContext,MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID,contentId);
            intent.putExtra(APIConstants.NOTIFICATION_PARAM_ACTION, CardDetails.PARAM_AUTO_PLAY);
            ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, "continue_watching")
                    .setShortLabel("Continue Watching")
                    .setLongLabel("Continue Watching")
                    .setIcon(Icon.createWithResource(mContext, R.drawable.player_icon_play))
                    .setIntent(intent)
                    .build();

            /*  *******************************************
                Use a list if there are more than one items
                *******************************************
            */
            if (getShortcutManager(mContext) != null && !PrefUtils.getInstance().getBoolean(APIConstants.IS_CONTINUE_WATCHING_SHORT_CUT_ADDED,false)) {
                getShortcutManager(mContext).addDynamicShortcuts(Collections.singletonList(shortcut));
                PrefUtils.getInstance().setBoolean(APIConstants.IS_CONTINUE_WATCHING_SHORT_CUT_ADDED,true);
            }else{
                updateContinueWatchingShortCut(mContext,contentId);
            }
        }else{
            Log.d("ShortCutManager","Can't add");
        }
    }

    private void updateContinueWatchingShortCut(Context mContext, String contentId){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            Intent intent = new Intent(mContext,MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID,contentId);
            intent.putExtra(APIConstants.NOTIFICATION_PARAM_ACTION,CardDetails.PARAM_AUTO_PLAY);

            intent.setPackage(mContext.getPackageName());
            ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, "continue_watching")
                    .setShortLabel("Continue Watching")
                    .setLongLabel("Continue Watching")
                    .setIcon(Icon.createWithResource(mContext, R.drawable.player_icon_play))
                    .setIntent(intent)
                    .build();

            //Use a list if there are more than one items
            if (getShortcutManager(mContext) != null) {
                getShortcutManager(mContext).updateShortcuts(Collections.singletonList(shortcut));
            }
        }else{
            Log.d("ShortCutManager","Can't add");
        }
    }

    /*
        Method to delete any added Dynamic ShortCut
        @param Context
        @param contentId
     */
    public void deleteContinueWatchingShortCut(Context mContext, String contentId){

        /*
            Please use
            ShortcutManager(Context).removeDynamicShortcuts()
            to delete any specific shortCuts.
         */
        if (getShortcutManager(mContext) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                getShortcutManager(mContext).removeAllDynamicShortcuts();
                PrefUtils.getInstance().setBoolean(APIConstants.IS_CONTINUE_WATCHING_SHORT_CUT_ADDED, false);
            }
        }

    }

}
