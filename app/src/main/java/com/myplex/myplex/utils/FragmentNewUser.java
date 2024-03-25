package com.myplex.myplex.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.BaseFragment;

public class FragmentNewUser extends BaseFragment {
    private Context mContext;
    private View rootView;
    private AppCompatButton getNewConnectionButton,subScribeToAppsButton,exploreOfferingsButton;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        rootView = inflater.inflate(R.layout.fragment_newuser, container, false);
        getNewConnectionButton=rootView.findViewById(R.id.get_new_connection);
        getNewConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewConnectionFragment();

            }
        });
        subScribeToAppsButton=rootView.findViewById(R.id.subscribe_to_apps);
        exploreOfferingsButton=rootView.findViewById(R.id.explore_offerings);
        return rootView;
    }

    private void getNewConnectionFragment() {
        if (mBaseActivity != null) {
            Bundle args = new Bundle();
            mBaseActivity.pushFragment(FragmentGetNewConnection.newInstance(args));
        }
    }

    public static FragmentNewUser newInstance(Bundle args) {
        FragmentNewUser fragmentNewUser = new FragmentNewUser();
        fragmentNewUser.setArguments(args);
        return fragmentNewUser;
    }
    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }
}
