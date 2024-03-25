package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.myplex.model.SettingsData;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends BaseAdapter {

    private static final int EDIT_FULL_NAME = 1;
    private static final int EDIT_STREAM_QUALITY = 2;
    private Context mContext;
	private LayoutInflater mInflater;
	private List<SettingsData> mSettingsList = new ArrayList<>();

    public SettingsAdapter(Context context, List<SettingsData> objects) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		this.mSettingsList = objects;
	}

    @Override
    public int getCount() {
        if(mSettingsList == null){
            return 0;
        }
        return mSettingsList.size();
    }

    @Override
    public SettingsData getItem(int position) {
        if(mSettingsList == null){
            return null;
        }
        return mSettingsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsData data = getItem(position);
        if (getItem(position).type == SettingsData.SECTION) {
            convertView = mInflater.inflate(R.layout.listitem_settings, null);
            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.settingsLayout);
            TextView txt = (TextView) convertView.findViewById(R.id.settingsgroup_txtview);
            EditText editTxt = (EditText) convertView.findViewById(R.id.settingsgroup_edittxt);
            editTxt.setVisibility(View.GONE);
            txt.setVisibility(View.VISIBLE);
            ImageView profPic = (ImageView) convertView.findViewById(R.id.image_res);
            profPic.setVisibility(View.GONE);
            txt.setText(data.mSettingName);
            txt.setTextColor(mContext.getResources().getColor(R.color.gray_text));
            convertView.setTag(mSettingsList.get(position));
            if(data.mSettingName.contains(SettingsFragment.DEVICE_SETTINGS)){
                convertView.setEnabled(true);
            }else{
                convertView.setEnabled(false);
            }
        } else {
            if(data.viewtype == SettingsData.VIEWTYPE_TOGGLEBUTTON){
                convertView = mInflater.inflate(R.layout.setting_item_togglebutton, null);
                Switch txt = (Switch) convertView.findViewById(R.id.settingitem_togglebutton);
                View view_item = convertView.findViewById(R.id.view_item);
                view_item.setBackgroundColor(mContext.getResources().getColor(R.color.settings_list_item_bg_color_70));
                txt.setBackgroundColor(mContext.getResources().getColor(R.color.settings_list_item_bg_color_70));
                txt.setText(data.mSettingName);
                txt.setTextColor(mContext.getResources().getColor(R.color.white_100));
                if(data.mSettingName.equalsIgnoreCase(SettingsFragment.SHOW_PLAYERLOGS)){
                    txt.setChecked(ApplicationController.SHOW_PLAYER_LOGS);
                }else if (data.mSettingName.equalsIgnoreCase(SettingsFragment.EXO_ENABLED)){
                    txt.setChecked(PrefUtils.getInstance().getPrefIsExoplayerEnabled());
                }else if (data.mSettingName.equalsIgnoreCase(SettingsFragment.EXO_ENABLED_DVR)){
                    txt.setChecked(PrefUtils.getInstance().getPrefIsExoplayerDvrEnabled());
                }
                txt.setOnCheckedChangeListener(mActionListener);
                txt.setTag(data);
                convertView.setTag(data);
            }else{
                convertView = mInflater.inflate(R.layout.listitem_settings, null);
                View view_item = convertView.findViewById(R.id.view_item);
                view_item.setBackgroundColor(mContext.getResources().getColor(R.color.settings_list_item_bg_color_70));
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.settings_list_item_bg_color_70));
                ImageView profPic = (ImageView) convertView.findViewById(R.id.image_res);
                TextView txt = (TextView) convertView.findViewById(R.id.settingsgroup_txtview);
                final EditText editTxt = (EditText) convertView.findViewById(R.id
                        .settingsgroup_edittxt);
                if(data.mSettingName.contains(SettingsFragment.MSISDN)){
                    convertView.setEnabled(false);
                }else{
                    convertView.setEnabled(true);
                }
                txt.setText(data.mSettingName);
                txt.setTextColor(mContext.getResources().getColor(R.color.gray_text));

                if(data.imageId == 0){
                    profPic.setVisibility(View.GONE);
                    editTxt.setVisibility(View.GONE);
                    txt.setVisibility(View.VISIBLE);
                }else{
                    profPic.setVisibility(View.VISIBLE);
                    editTxt.setVisibility(View.VISIBLE);
                    editTxt.setText(data.mSettingName);
                    editTxt.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER
                                    || keyCode == EditorInfo.IME_ACTION_DONE) {
                                if (v != null) {
                                    InputMethodManager imm = (InputMethodManager)
                                            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                }

                                SettingsData fullNameData = mSettingsList.get(1);
                                PrefUtils.getInstance().setPrefFullName(editTxt.getText().toString());
                                fullNameData.mSettingName = PrefUtils.getInstance().getPrefFullName();
                                mSettingsList.remove(1);
                                mSettingsList.add(1, fullNameData);
                                editTxt.clearFocus();
                                v.clearFocus();
                                notifyDataSetChanged();

                            }
                            return false;
                        }
                    });
                    txt.setVisibility(View.GONE);
                }
                convertView.setTag(mSettingsList.get(position));
            }
        }


        return convertView;
    }

    public CompoundButton.OnCheckedChangeListener mActionListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            try {
                SettingsData obj = (SettingsData)buttonView.getTag();
                if(obj != null){
                   if(obj.mSettingName.equalsIgnoreCase("Show player logs")){
                        ApplicationController.SHOW_PLAYER_LOGS=isChecked;
                    }else if (obj.mSettingName.equalsIgnoreCase(SettingsFragment.SHOW_NOTIFICATIONS)){
                    }else if (obj.mSettingName.equalsIgnoreCase(SettingsFragment.EXO_ENABLED)){
                       PrefUtils.getInstance().setPrefIsExoplayerEnabled(isChecked);
                    }else if (obj.mSettingName.equalsIgnoreCase(SettingsFragment.EXO_ENABLED_DVR)){
                       PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(isChecked);
                    }
                }
            } catch (Exception e) {
                Log.d("SettingsAdapter", e.toString());
            }

        }
    };
}
