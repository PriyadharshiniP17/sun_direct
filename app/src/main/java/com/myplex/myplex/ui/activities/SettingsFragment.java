package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.myplex.api.APIConstants;
import com.myplex.model.MsisdnData;
import com.myplex.model.SettingsData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.debug.DebugActivity;
import com.myplex.myplex.ui.adapter.SettingsAdapter;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends BaseFragment {

    private View mRootView;
    private ListView mSettingsListView;
    private SettingsAdapter mListAdapter;
    private List<SettingsData> mSettingsList;

    public static String RATING_POSTED = null; //analytics useful to getdata from MessagePost to CardDetailViewFactory
    public static String FEEDBACK_POSTED = null; //analytics useful to getdata from MessagePost to CardDetailViewFactory
    private String FEEDBACK = "feedback";
    private String TANDC = "terms & conditions";
    private String PRIVACYPOLIY = "privacy policy";
    private String HELP = "help";
    private String SUPPORT = "Support";
    private String DOWNLOAD_OR_STREAM_MSG = "movie rental options: ";
    public static final String DRM_STATUS_STRING = "WVDRM status";
    public static final String DRM_LEVAL_STRING = "WVDRM statusKey";
    public static final String ROOT_STATUS_STRING = "root status";
    public static final String DERIGISTER_DEVICE = "Deregister device";
    public static final String SENSOR_SCROLL = "Sensor Scroll";
    public static final String SHOW_PLAYERLOGS = "Show player logs";
    public static final String SYNC_PURCHASES = "Sync Purchases";
    public static final String MSISDN = "Mobile No :";
    public static final String REFERAL = "Referal";
    public static final String LANGUAGE_SETTINGS = "language settings";
    public static final String SHOW_NOTIFICATIONS = "show notifications";
    public static final String EXO_ENABLED = "exo enabled";
    public static final String EXO_ENABLED_DVR = "exo enabled for dvr";
    public static final String DEBUG_MODE = "debug mode";
    public static final String PROFILE = "Profile";
    public static String FULL_NAME = "Name";
    public static String DEVICE_SETTINGS = "Device Settings";
    public static String PLAYBACK = "Playback";
    private int debug_mode_counter = 5;


    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mBaseActivity == null) {
            return null;
        }
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        mSettingsListView = (ListView) rootView.findViewById(R.id.settings_list);
        PreapreSettingsData();

        mSettingsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                if (mSettingsList == null) {
                    return;
                }
                SettingsData data = mSettingsList.get(position);
                if (data == null) {
                    return;
                }
                switch (data.type) {
                    case SettingsData.SECTION: {
                        if (data.mSettingName.equalsIgnoreCase(DEVICE_SETTINGS)) {

                            debug_mode_counter--;
                            if (debug_mode_counter == 0) {
                                ApplicationController.SHOW_DEUBUG_SETTINGS = true;
                                PreapreSettingsData();
                                mListAdapter.notifyDataSetChanged();
                                return;
                            } else if (debug_mode_counter < 3
                                    && debug_mode_counter > 0) {
                                Toast.makeText(mContext, "Click more " + debug_mode_counter +
                                        " times to developer options.", Toast.LENGTH_SHORT).show();
                            } else if (debug_mode_counter < 0) {
                                Toast.makeText(mContext, "No need you are already a developer.", Toast
                                        .LENGTH_SHORT)
                                        .show();
                            }
                            return;
                        }
                        return;
                    }
                    case SettingsData.ITEM: {
                        if (data.mSettingName.contains(DEBUG_MODE)) {
                            Intent i = new Intent(mContext, DebugActivity.class);
                            startActivity(i);
                            return;
                        } else if (data.mSettingName.contains(PLAYBACK)) {
                            final CharSequence quality[] = new CharSequence[]{
                                    "Auto",
                                    "Very High",
                                    "High",
                                    "Medium",
                                    "Low"};
                            // Sets dialog for popup dialog list
                            AlertDialog dialog;
                            ListAdapter itemlist = new ArrayAdapter(mContext, R.layout.alert_network_type, quality);

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Choose Stream Type");
                            builder.setAdapter(itemlist, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    String qualityType = quality[item].toString();
                                    SettingsData playBackItem = mSettingsList.get(2);
                                    playBackItem.mSettingName = qualityType;
                                    mSettingsList.remove(2);
                                    mSettingsList.add(2, playBackItem);
                                    PrefUtils.getInstance().setPrefPlayBackQuality(qualityType);
                                    PreapreSettingsData();
                                    mListAdapter.notifyDataSetChanged();
                                }
                            });

                            dialog = builder.create();
                            dialog.show();
                            return;
                        }
                    }
                }

            }
        });
        return rootView;
    }

    private void PreapreSettingsData() {
        mSettingsList = new ArrayList<>();
        mSettingsList.add(new SettingsData(SettingsData.SECTION, PROFILE, 0, SettingsData.VIEWTYPE_NORMAL));
        FULL_NAME = PrefUtils.getInstance().getPrefFullName() == null ? FULL_NAME : PrefUtils.getInstance().getPrefFullName();
        mSettingsList.add(new SettingsData(SettingsData.ITEM, FULL_NAME, -1, SettingsData.VIEWTYPE_NORMAL));
        String msisdnMsg = MSISDN + " (Not Available)";
        MsisdnData mData = (MsisdnData) SDKUtils.loadObject(APIConstants.msisdnPath);
        if (mData != null) {
            msisdnMsg = MSISDN + "( " + mData.msisdn + "," + mData.operator + " )";
        }
        mSettingsList.add(new SettingsData(SettingsData.ITEM, msisdnMsg, 0, SettingsData.VIEWTYPE_NORMAL));

        mSettingsList.add(new SettingsData(SettingsData.SECTION, DEVICE_SETTINGS, 0, SettingsData.VIEWTYPE_NORMAL));
        String playBackName = PrefUtils.getInstance().getPrefPlayBackQuality() != null ? PrefUtils.getInstance().getPrefPlayBackQuality() : "Auto";
        mSettingsList.add(new SettingsData(SettingsData.ITEM, PLAYBACK + "\t\t\t\t\t" + playBackName, 0, SettingsData.VIEWTYPE_NORMAL));
        String version = Util.getAppVersionName(mContext);
        mSettingsList.add(new SettingsData(SettingsData.SECTION, mContext.getString(R.string
                .app_name) + " V " + version, 0, SettingsData.VIEWTYPE_NORMAL));

        if (ApplicationController.SHOW_DEUBUG_SETTINGS) {
            mSettingsList.add(new SettingsData(SettingsData.ITEM, DEBUG_MODE, 0, SettingsData.VIEWTYPE_NORMAL));
            mSettingsList.add(new SettingsData(SettingsData.ITEM, SHOW_PLAYERLOGS, 0, SettingsData.VIEWTYPE_TOGGLEBUTTON));
            mSettingsList.add(new SettingsData(SettingsData.ITEM, EXO_ENABLED, 0, SettingsData.VIEWTYPE_TOGGLEBUTTON));
            mSettingsList.add(new SettingsData(SettingsData.ITEM, EXO_ENABLED_DVR, 0, SettingsData.VIEWTYPE_TOGGLEBUTTON));
        }

        mListAdapter = new SettingsAdapter(mContext, mSettingsList);
        mSettingsListView.setAdapter(mListAdapter);
    }
}
