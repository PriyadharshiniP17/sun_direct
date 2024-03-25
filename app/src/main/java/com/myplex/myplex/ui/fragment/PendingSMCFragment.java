package com.myplex.myplex.ui.fragment;

import static com.myplex.myplex.media.exoVideo.PlayerState.finish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.request.user.SignOut;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.model.ItemClickListener;
import com.myplex.myplex.model.PendingSMCItemClickListener;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.PendingSMCAdapter;
import com.myplex.myplex.utils.FragmentSignIn;
import com.myplex.myplex.utils.FragmentSignUp;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;


// This class implemented for unRegistered SMC in Settings screen

public class PendingSMCFragment extends BaseFragment {
  private RecyclerView pendingSMCRecycleView;
  private PendingSMCAdapter pendingSMCAdapter;
    private LinearLayoutManager mLinearLayoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.pending_smc_fragment, container, false);
        pendingSMCRecycleView =(RecyclerView)rootView.findViewById(R.id.pendingSMC);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        pendingSMCRecycleView.setLayoutManager(mLinearLayoutManager);
        pendingSMCAdapter= new PendingSMCAdapter(PrefUtils.getInstance().getPendingSMC());
        pendingSMCRecycleView.setAdapter(pendingSMCAdapter);
        pendingSMCAdapter.setItemClickListener(new PendingSMCItemClickListener() {
            @Override
            public void onClick(View view, int position, String parentPosition) {
              showBottomSheetDialog(parentPosition);
            }

        });
        return rootView;

        }
    BottomSheetDialog bottomSheetDialog;

    private void showBottomSheetDialog(String text) {
        bottomSheetDialog = new BottomSheetDialog(mContext, R.style.NoBackgroundDialogTheme);
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.pendingsmc_bottom_sheet_dialog, null);
        bottomSheetDialog.setContentView(view);
        ImageView closeIcon=view.findViewById(R.id.back_navigation);
        AppCompatButton continueSMCRegister=view.findViewById(R.id.second_smc_register_button);
        TextView secondText = view.findViewById(R.id.second_time_register_suggestion);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        continueSMCRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignUpScreenFragment();
                bottomSheetDialog.dismiss();

            }
        });
        bottomSheetDialog.show();

    }
    private void showSignUpScreenFragment() {
       makeSignOutRequest();
    }

    private void makeSignOutRequest() {
        SignOut deviceUnRegRequest = new SignOut(new APICallback() {
            @Override
            public void onResponse(APIResponse response) {
                if (response != null) {
                    SDKLogger.debug(response.toString());
                    launchLoginActivity("MY ACCOUNT", "MY ACCOUNT");
                    Toast.makeText(mContext, response.message(), Toast.LENGTH_SHORT).show();
                }
                SDKLogger.debug("SUCCESS");
                if (response != null && response.isSuccess()) {
                    CleverTap.eventLogOut();
                    doLogout();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Error deregestering device " + errorCode);
                Toast.makeText(mContext,
                        "No network",
                        Toast.LENGTH_SHORT).show();

            }
        });
        APIService.getInstance().execute(deviceUnRegRequest);

    }
    public void doLogout() {
        PrefUtils.getInstance().setPrefLoginStatus("");
        PrefUtils.getInstance().setPrefMsisdnNo("");
        PrefUtils.getInstance().setPrefUserId(0);
        PrefUtils.getInstance().setPrefFullName("");
        PrefUtils.getInstance().setIsToShowForm(false);
        PrefUtils.getInstance().setUserGenderRange("");
        PrefUtils.getInstance().setUserGender("");
        PrefUtils.getInstance().setUserAgeRange("");
        PrefUtils.getInstance().setAdVideoCount(1);
        PrefUtils.getInstance().setUSerCountry("");
        PrefUtils.getInstance().setUserState("");
        PrefUtils.getInstance().setUserCity("");
        PrefUtils.getInstance().setPopup(false);
        PrefUtils.getInstance().setString("PROFILE_IMAGE_URL","");
        PrefUtils.getInstance().setAppLanguageToSendServer("");
        PrefUtils.getInstance().setSubscribedLanguage(null);
        PrefUtils.getInstance().setPackages(null);
        Util.setUserIdInMyPlexEvents(mContext);
        ApplicationController.clearPackagesList();
        ComScoreAnalytics.getInstance().setEventLogout();
        myplexAPI.clearCache(APIConstants.BASE_URL);
        MenuDataModel.clearCache();
        PrefUtils.getInstance().setDefaultServiceName(getResources().getString(R.string.serviceName));
    }
    private void launchLoginActivity(String source, String sourceDetails) {
        if (mContext == null) {
            return;
        }
        mBaseActivity.finish();
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra("isFromSplash", false);
        intent.putExtra("isFromPendingSMC", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mBaseActivity.finishAffinity();
        startActivity(intent);

    }
}