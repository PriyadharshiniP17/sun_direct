package com.myplex.myplex.ui.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.ui.activities.ActivityMyPacks;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.views.FeedBackDialog;
import com.myplex.myplex.utils.Util;

import java.util.List;

/**
 * Created by Neosoft on 7/10/2017.
 */

public class HelpSectionFragment extends BaseFragment implements View.OnClickListener {

    TextView mTextFaq,mTextSupport,mTextRefundPolicy;
    TextView mTextShareApp;
    TextView mTextRateApp;
    TextView mTextFeedback;
    TextView mTextCancelSubscription;
    Intent mIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help_section, container, false);

        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mTextSupport = (TextView) rootView.findViewById(R.id.text_support);
        mTextFaq = (TextView) rootView.findViewById(R.id.text_faq);
        mTextShareApp = (TextView) rootView.findViewById(R.id.text_share_app);
        mTextRateApp = (TextView) rootView.findViewById(R.id.text_rate_app);
        mTextFeedback = (TextView) rootView.findViewById(R.id.text_feedback);
        mTextCancelSubscription = (TextView) rootView.findViewById(R.id.text_cancel_subscription);
        mTextRefundPolicy = (TextView) rootView.findViewById(R.id.text_refund_policy);
        mTextRefundPolicy.setVisibility(View.GONE);
        mTextFaq.setOnClickListener(this);
        mTextShareApp.setOnClickListener(this);
        mTextRateApp.setOnClickListener(this);
        mTextFeedback.setOnClickListener(this);
        mTextCancelSubscription.setOnClickListener(this);
        mTextSupport.setOnClickListener(this);
        mTextRefundPolicy.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_support:
                mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.ticket_support));
                /*CleverTap.eventPageViewed(CleverTap.PAGE_SUPPORT);
                AppsFlyerTracker.eventBrowseHelp();*/
                if(TextUtils.isEmpty(PrefUtils.getInstance().getSupportPageURL())){
                    return;
                }
                mIntent.putExtra("url", PrefUtils.getInstance().getSupportPageURL());
                mContext.startActivity(mIntent);
                break;
                case R.id.text_refund_policy:
                mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.refund_policy));
                if(TextUtils.isEmpty(PrefUtils.getInstance().getPrefRefundPolicyUrl())){
                    return;
                }
                mIntent.putExtra("url", PrefUtils.getInstance().getPrefRefundPolicyUrl());
                mContext.startActivity(mIntent);
                break;
            case R.id.text_faq:
                mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.faq));
                CleverTap.eventPageViewed(CleverTap.PAGE_HELP);
                AppsFlyerTracker.eventBrowseHelp();
                if(TextUtils.isEmpty(PrefUtils.getInstance().getFaq_url())){
                   return;
                }else{
                    mIntent.putExtra("url", PrefUtils.getInstance().getFaq_url());
                }

                mContext.startActivity(mIntent);
                break;
            case R.id.text_share_app:
                String msg = mContext.getString(R.string.share_message);
                if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefShareMessageForMenu())) {
                    msg = PrefUtils.getInstance().getPrefShareMessageForMenu();
                }
                String url = APIConstants.getShareUrlForMenu(mContext);
                if (!TextUtils.isEmpty(url)) {
                    msg = msg + "\n" + Uri.parse(url);
                }
                Util.shareData(mContext, 3, null, msg);
                break;
            case R.id.text_rate_app:
                openAppRating(getActivity());
                break;
            case R.id.text_feedback:
                CardData profileData = new CardData();
                profileData._id = "0";
                FeedBackDialog feedBackDialog = new FeedBackDialog(mContext);
                feedBackDialog.showDialog(profileData, new FeedBackDialog.MessagePostCallback() {
                    @Override
                    public void sendMessage(boolean status) {
                        if (status) {
                            AlertDialogUtil.showToastNotification("Thanks for your feedback.");
                        } else {
                            String message = "Unable to post your review.";
                            if (ConnectivityUtil.isConnected(mContext) && !Util.checkUserLoginStatus()) {
                                message = "Please register to share your feedback.";
                            }
                            AlertDialogUtil.showToastNotification(message);
                        }
                    }
                });
                break;
            case R.id.text_cancel_subscription:
                CleverTap.eventPageViewed(CleverTap.PAGE_MYPACKS);
                mIntent = new Intent(mContext, ActivityMyPacks.class);
                startActivity(mIntent);
                break;
        }
    }

    public static void openAppRating(Context context) {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(APIConstants.HTTP_MARKET_URL + appId));
            context.startActivity(webIntent);
        }
    }

}
