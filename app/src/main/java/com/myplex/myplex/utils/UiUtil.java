package com.myplex.myplex.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myplex.model.ApplicationConfig;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;

import java.util.Random;

/**
 * Created by Apalya on 10/27/2015.
 */
public class UiUtil {

    private static final long ALPHA_ANIM_DURATION = 350;
    public static final int SPACING_TABLET_BETWEEN_CONTENT_LAYOUT_NAV_BAR = 10;
    public static final int SPACING_MOBILE_BETWEEN_CONTENT_LAYOUT_NAV_BAR = 40;
    private static int cardColor = -1;
    private static int pos;

    public static void setRandomColor(ImageView imageView) {

        Random rnd = new Random();
        int Low = 100;
        int High = 196;

        cardColor = Color.argb(255, rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low);

        imageView.setBackgroundColor(cardColor);
    }


    public static void showFeedback(View v) {
        if (v == null) {
            return;
        }
        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(v.getContext().getResources().getColor(R.color.red_highlight_color));
                        break;
                    default:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;
                }
                return false;
            }
        });
    }


    public static void showFeedback(View v, final boolean reSurfaceOwnBg, final int bgcolor) {
        if (v == null) {
            return;
        }
        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(SDKUtils.getColor(v.getContext(), R.color.red_highlight_color));
                        break;
                    default:
                        try {
                            if (reSurfaceOwnBg && bgcolor != 0) {
                                v.setBackgroundColor(SDKUtils.getColor(v.getContext(), bgcolor));
                                return false;
                            }
                            v.setBackgroundColor(Color.TRANSPARENT);
                        } catch (Exception e) {
                            e.printStackTrace();
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                        break;
                }
                return false;
            }
        });
    }

    public static String findDpi(int density) {
        String value = ApplicationConfig.MDPI;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                value = ApplicationConfig.LDPI;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                value = ApplicationConfig.MDPI;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                value = ApplicationConfig.HDPI;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                value = ApplicationConfig.XHDPI;
                break;
            default:
                value = ApplicationConfig.XHDPI;
                break;
        }
        return value;
    }

    public static String getScreenDensity(Context mContext) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;
        String density = ApplicationConfig.HDPI;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                Log.e("DISPLAY PROFILE", "LOW");
                density = ApplicationConfig.LDPI;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                Log.e("DISPLAY PROFILE", "MEDIUM");
                density = ApplicationConfig.MDPI;
                break;
            case DisplayMetrics.DENSITY_TV:
                Log.e("DISPLAY PROFILE", "TV");
                density = ApplicationConfig.HDPI;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                Log.e("DISPLAY PROFILE", "HIGH");
                density = ApplicationConfig.HDPI;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                Log.e("DISPLAY PROFILE", "XHIGH");
                density = ApplicationConfig.XHDPI;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                Log.e("DISPLAY PROFILE", "XXHIGH");
                density = ApplicationConfig.XXHDPI;
                break;
        }
        return density;
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 48;
        }
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        if (result > 100) {
            result = 48;
        }
        return result;
    }

    public static int getNavBarHeight(Context context) {
        if (context == null) {
            return 56;
        }

        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        if (result > 100) {
            result = 56;
        }
        return result;
    }

    public static boolean hasSoftKeys(Context mContext) {
        boolean hasSoftwareKeys = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display d = ((Activity)mContext).getWindowManager().getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        } else {
            boolean hasMenuKey = ViewConfiguration.get(mContext).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }

    public static void applyShowAlphaAnimation(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                ObjectAnimator fadeAltAnim = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
                fadeAltAnim.setDuration(ALPHA_ANIM_DURATION);
                fadeAltAnim.start();
            }
        });
    }

    public static void applyHideAlphaAnimation(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
//                view.setVisibility(View.VISIBLE);
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0.0f);
                fadeAnim.setDuration(ALPHA_ANIM_DURATION);
                fadeAnim.start();
                fadeAnim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });
    }

    public static Drawable convertStringToDrawable(Context mContext, String s) {
        try {
            int id = mContext.getResources().getIdentifier(s, "drawable", mContext.getPackageName());
            return mContext.getResources().getDrawable(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static float convertSpToPixels(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static final int getColor(Context context, int id) {
        if (context == null) {
            return Color.parseColor("#00000000");
        }
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }


    public static SpannableStringBuilder getFormattedFont(String stringToSpan, Context mContext) {
        SpannableStringBuilder SS = new SpannableStringBuilder(stringToSpan);
        /*Typeface typeFace = Typeface.createFromAsset(mContext.getAssets(),
                "fonts/FuturaBT-ExtraBlack.ttf");
        SS.setSpan(new CustomTypefaceSpan("", typeFace), 0, SS.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
*/
        return SS;
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            if ((activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                    || (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ) {
                usableHeight = metrics.widthPixels;
            }
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if ((activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                    || (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ) {
                realHeight = metrics.widthPixels;
            }
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
