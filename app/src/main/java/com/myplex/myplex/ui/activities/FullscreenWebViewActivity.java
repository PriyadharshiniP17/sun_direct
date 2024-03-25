package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.myplex.api.APIConstants;
import com.myplex.model.PromoAdData;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.FullScreenWebViewFragment;

import static com.myplex.myplex.ui.fragment.FullScreenWebViewFragment.PARAM_MEDIA_PLAYBACK_REQUIRE_GESTURE;
import static com.myplex.myplex.ui.fragment.FullScreenWebViewFragment.PARAM_PROMO_AD_DATA;
import static com.myplex.myplex.ui.fragment.FullScreenWebViewFragment.PARAM_SHOW_TOOLBAR;
import static com.myplex.myplex.ui.fragment.FullScreenWebViewFragment.PARAM_WEB_URL;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenWebViewActivity extends BaseActivity {

    private FullScreenWebViewFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_web_view);
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setOrientation(int value) {

    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            mCurrentFragment = FullScreenWebViewFragment.newInstance(getIntent().getStringExtra(PARAM_WEB_URL), (PromoAdData) getIntent().getSerializableExtra(PARAM_PROMO_AD_DATA), getIntent().getBooleanExtra(PARAM_SHOW_TOOLBAR, false), getIntent().getBooleanExtra(PARAM_MEDIA_PLAYBACK_REQUIRE_GESTURE, false));
            transaction.replace(R.id.fragment_webview, mCurrentFragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
            SDKLogger.debug("Some thing went wrong in web view creation");
            finish();
        }
    }

    public final static Intent createIntent(Context context,
                                            PromoAdData promoAdData,
                                            boolean showToolbar, boolean mediaPlaybackRequireGesture, String url,String partnerName) {
        Intent intent = new Intent(context, FullscreenWebViewActivity.class);
        intent.putExtra(PARAM_PROMO_AD_DATA, promoAdData);
        intent.putExtra(PARAM_WEB_URL, url);
        intent.putExtra(PARAM_SHOW_TOOLBAR, showToolbar);
        intent.putExtra(PARAM_MEDIA_PLAYBACK_REQUIRE_GESTURE, mediaPlaybackRequireGesture);
        if (partnerName != null) {
            intent.putExtra(APIConstants.PARTNER_NAME,partnerName);
        }
        return intent;
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment != null && mCurrentFragment.onBackClicked()) {
            return;
        }
        finish();
    }


}
