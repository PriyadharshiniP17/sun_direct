package com.myplex.myplex.ui.views;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

/**
 * Created by phani on 12/24/2015.
 */
public class PopUpWindow {
    public PopupWindow popupWindow;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private boolean hasSoftKeys;
    private int height;
    public PopUpWindow(View layout) {

        try {
            popupWindow = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PopUpWindow(View layout, int height, androidx.appcompat.widget.Toolbar mToolbar) {
        this.mToolbar = mToolbar;
        hasSoftKeys = true;
        this.height = height;
        try {
            popupWindow = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attachPopupToView(View parentViewToAttach, PopupWindow.OnDismissListener onDismissListener) {

        popupWindow.setOnDismissListener(onDismissListener);
        if (hasSoftKeys) {
//            int[] location = new int[2];
//            Point p = new Point();
//            mToolbar.getLocationOnScreen(location);
//            p.y = location[1] + mToolbar.getHeight()+height;
//            p.x = location[0];
//            popupWindow.showAtLocation(parentViewToAttach, Gravity.NO_GRAVITY, p.x, p.y);
//            popupWindow.showAsDropDown(parentViewToAttach, p.x, p.y);
            popupWindow.showAsDropDown(parentViewToAttach, 0, 0);
            popupWindow.showAtLocation(parentViewToAttach, Gravity.CENTER, 0, 0);
        } else {
            popupWindow.showAsDropDown(parentViewToAttach, 0, 0);
            popupWindow.showAtLocation(parentViewToAttach, Gravity.CENTER, 0, 0);

        }
    }

    public void dismissPopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public boolean isPopupVisible() {
        if (popupWindow != null) {
            return popupWindow.isShowing();
        }
        return false;
    }
}
