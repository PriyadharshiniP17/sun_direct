package com.myplex.myplex.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.views.AboutDialogWebView;
import com.myplex.myplex.utils.Util;

/**
 * Created by Neosoft on 7/10/2017.
 */

public class AboutSectionsFragment extends BaseFragment implements View.OnClickListener {

    TextView mTextAccount;
    TextView mTextAccountNumber;
    TextView mTextAccountName;
    TextView mTextAboutApp;
    TextView mTextTnc;
    TextView mTextPrivacyPolicy;
    TextView mTextContatctUs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about_section, container, false);
        initUI(rootView);
        return rootView;
    }

    private void initUI(View rootView) {

        mTextAccount = (TextView) rootView.findViewById(R.id.text_account);
        mTextAccountName = (TextView) rootView.findViewById(R.id.text_account_name);
        mTextAccountNumber = (TextView) rootView.findViewById(R.id.text_account_number);
        mTextAboutApp = (TextView) rootView.findViewById(R.id.text_about_app);
        mTextTnc = (TextView) rootView.findViewById(R.id.text_tnc);
        mTextPrivacyPolicy = (TextView) rootView.findViewById(R.id.text_privacy_policy);
        mTextContatctUs = (TextView) rootView.findViewById(R.id.text_contact_us);

        mTextAccountName.setText(PrefUtils.getInstance().getPrefEmailID());
        mTextAccountName.setVisibility(View.GONE);
        mTextAccountNumber.setVisibility(View.GONE);
        if (Util.checkUserLoginStatus()) {
            mTextAccountNumber.setVisibility(View.VISIBLE);
            String accNum = PrefUtils.getInstance().getPrefMsisdnNo();
            mTextAccountNumber.setText(accNum);
        }
        mTextAboutApp.setOnClickListener(this);
        mTextTnc.setOnClickListener(this);
        mTextPrivacyPolicy.setOnClickListener(this);
        mTextContatctUs.setOnClickListener(this);
    }



    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackClicked();
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_about_app:
                showAboutDialog();
                break;
            case R.id.text_tnc:
                Intent tnc = new Intent(mContext, LiveScoreWebView.class);
                CleverTap.eventPageViewed(CleverTap.PAGE_TERMS_AND_CONDITIONS);
                AppsFlyerTracker.eventBrowseHelp();
                tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                if(!TextUtils.isEmpty(PrefUtils.getInstance().getTncUrl())){
                    tnc.putExtra("url", PrefUtils.getInstance().getTncUrl());
                }else{
                    tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                }
                mContext.startActivity(tnc);
                break;
            case R.id.text_privacy_policy:
                Intent ppc = new Intent(mContext, LiveScoreWebView.class);
                CleverTap.eventPageViewed(CleverTap.PAGE_PRIVACY_POLICY);
                AppsFlyerTracker.eventBrowseHelp();
                ppc.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));
                if(!TextUtils.isEmpty(PrefUtils.getInstance().getPrivacy_policy_url())){
                    ppc.putExtra("url", PrefUtils.getInstance().getPrivacy_policy_url());
                }else{
                    ppc.putExtra("url", APIConstants.getFAQURL() + APIConstants.PRIVACY_POLICY_URL);
                }
                mContext.startActivity(ppc);
                break;
            case R.id.text_contact_us:
                Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.contact_us));
                if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                    return;
                }
                mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                mContext.startActivity(mIntent);
                break;
        }
    }

    private void showAboutDialog() {
        AboutDialogWebView aboutDialogWebView = new AboutDialogWebView(mContext);
        aboutDialogWebView.showDialog();
    }
}
