package com.myplex.myplex.ui.fragment;

import static android.view.View.GONE;
import static com.myplex.myplex.ApplicationController.getAppContext;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.LanguageListRequest;
import com.myplex.api.request.user.UpdateProfileRequest;
import com.myplex.model.LanguageListResponse;
import com.myplex.model.Term;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankit on 7/10/2017.
 */

public class AppLanguageFragment extends BaseFragment {
    String[] appLanguage;
    private CheckedObjectViewAdapter textViewAdapter;
    private String selectedLanguages = "";
    private ArrayList<selectLanguage> langaugeArray;
    private ArrayList<selectLanguage> selectedArray = new ArrayList<>();
    public int selectedPosition;
    public String selectedLanguage = "";
    private ProgressBar mProgressBar;
    private ProgressDialog mProgressDialog;
    private boolean defaultValue = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_app_langugae, container, false);

        initUI(rootView, inflater);

        return rootView;
    }

    private void initUI(View rootView, LayoutInflater inflater) {
        mContext = getActivity();
        langaugeArray = new ArrayList<>();
        final GridView listView = (GridView) rootView.findViewById(R.id.listview);
        Button save = rootView.findViewById(R.id.save_button);
        getLanguages();
        /*String appLang = PrefUtils.getInstance().getAppLanguageToShow();
        appLanguage = appLang.split(",");
        int selected = PrefUtils.getInstance().getAppLanguage();
        for (int i = 0; i < appLanguage.length; i++) {
            if(selected != i) {
                langaugeArray.add(new selectLanguage(appLanguage[i], appLanguage[i], false,false));
            }else{
                langaugeArray.add(new selectLanguage(appLanguage[i], appLanguage[i], true,true));
            }
        }*/
        // Instantiating array adapter to populate the listView
        // The layout android.R.layout.simple_list_item_single_choice creates radio button for each listview item
        textViewAdapter = new CheckedObjectViewAdapter(inflater, langaugeArray);
        listView.setAdapter(textViewAdapter);
        SDKLogger.debug("############" + "getAppLanguage " + PrefUtils.getInstance().getAppLanguage());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                SDKLogger.debug("############"+"Items " +  appLanguage[position] );
                PrefUtils.getInstance().saveAppLanguage(position);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.isNetworkAvailable(mBaseActivity)) {
                    AlertDialogUtil.showToastNotification(mBaseActivity.getString(R.string.network_error));
                    return;
                }
                if (selectedPosition > -1) {
                    PrefUtils.getInstance().saveAppLanguage(selectedPosition);
                }
                String languages = "";
                for (int i = 0; i < langaugeArray.size(); i++) {
                    if (langaugeArray.get(i).isSelected()) {
                        selectedArray.add(langaugeArray.get(i));
                    }
                }
                for (int i = 0; i < selectedArray.size(); i++) {
                    languages += selectedArray.get(i).getLanguageToShow();
                    if (i != selectedArray.size() - 1)
                        languages = languages + ",";
                }
                if (languages.isEmpty()) {
                    Toast.makeText(mContext, "Select atleast one language", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!languages.isEmpty()) {
                    PrefUtils.getInstance().setAppLanguageToSendServer(languages);
                    PrefUtils.getInstance().setAppLanguageFirstTime("false");
                    //eventLangugaeChanged(selectedLanguage);
                    updateUserProfile(languages);

                }
                Toast.makeText(mContext, "App language changed successfully", Toast.LENGTH_LONG).show();
                setLanguage();
            }
        });
        Typeface amazonEmberRegular = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        save.setTypeface(amazonEmberRegular);
    }

    //Calling UpdateProfile API to send the selected language to (user/v2/profile/) profile API
    private void updateUserProfile(String languages) {
        UpdateProfileRequest.Params params = new UpdateProfileRequest.Params("", "", "", "", "", "", languages);
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(params, new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null && response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if (responseData.status != null && responseData.status.equals(APIConstants.SUCCESS)) {
                        if (languages != null) {
                            PrefUtils.getInstance().setPrefLanguageSelected(languages);
                        }
                        if (getActivity() != null)
                            getActivity().finish();
                    } else {
                        AlertDialogUtil.showToastNotification(response.message());
                    }
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(updateProfileRequest);
    }

    private void getLanguages() {
        showProgressBar(true);
        LanguageListRequest languageRequest = new LanguageListRequest(new APICallback<LanguageListResponse>() {
            @Override
            public void onResponse(APIResponse<LanguageListResponse> response) {
                dismissProgressBar(true);
                if (response == null || null == response.body()) {
                    return;
                }
                List<Term> terms = response.body().getLanguages().get(0).getTerms();
                if (terms != null && terms.size() > 0) {
                    // int selected = PrefUtils.getInstance().getAppLanguage();
                    String selected_language;
                    if (PrefUtils.getInstance().getAppLanguageFirstTime() != null && PrefUtils.getInstance().getAppLanguageFirstTime().equalsIgnoreCase("true")) {
                        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
                        if (subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null) {
                            selected_language = subscribed_languages.get(0);
                        }
                        else{
                            selected_language=PrefUtils.getInstance().getAppLanguageToSendServer();
                        }


                        for (int i = 0; i < terms.size(); i++) {
                       /* if (selected == -1) {
                            langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                    terms.get(i).getHumanReadable(), false, false));
                        } else {
                            if (selected != i) {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), false, false));
                            } else {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), true, false));
                            }
                        }*/
                            if (selected_language.equalsIgnoreCase(terms.get(i).getHumanReadable())) {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), true, false));
                            } else {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), false, false));
                            }
                        }
                    } else {
                        selected_language = PrefUtils.getInstance().getAppLanguageToSendServer();
                        for (int i = 0; i < terms.size(); i++) {
                       /* if (selected == -1) {
                            langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                    terms.get(i).getHumanReadable(), false, false));
                        } else {
                            if (selected != i) {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), false, false));
                            } else {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), true, false));
                            }
                        }*/
                            if (selected_language.equalsIgnoreCase(terms.get(i).getHumanReadable())) {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), true, false));
                            } else {
                                langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                        terms.get(i).getHumanReadable(), false, false));
                            }
                        }
                    }
                }
                textViewAdapter.addData(langaugeArray);
                textViewAdapter.notifyDataSetChanged();
            }
                /*String language = PrefUtils.getInstance().getAppLanguageToSendServer();
                String[] langList = null ;
                if(language != null) {
                    langList = language.split(",");
                }*//*
                if(terms!= null && terms.size()>0 ) {
                     int selected = PrefUtils.getInstance().getAppLanguage();
                    for (int i = 0; i < terms.size(); i++) {
                        boolean isSelected = false;
                        if(langList != null) {
                            for (String lang :
                                    langList) {
                                if (terms.get(i).getHumanReadable().equals(lang)) {
                                    isSelected = true;
                                }
                            }
                        }
                        if(language == null) {
                            langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                    terms.get(i).getHumanReadable(), true, false));
                        }else{
                            langaugeArray.add(new selectLanguage(terms.get(i).getTerm(),
                                    terms.get(i).getHumanReadable(), isSelected, false));
                        }
                    }
                }
                textViewAdapter.addData(langaugeArray);
                textViewAdapter.notifyDataSetChanged();
            }*/


            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar(true);
            }
        });
        APIService.getInstance().execute(languageRequest);
    }

    private void setLanguage() {
        if (getActivity() != null && isAdded()) {
            getActivity().finish();
        }
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }

    public class selectLanguage {
        private String language;
        private String languageToShow;
        private boolean isSelected;
        public boolean defaultSelected;


        public selectLanguage(String language, String languageToShow, boolean isSelected, boolean defaultSelected) {
            this.language = language;
            this.languageToShow = languageToShow;
            this.isSelected = isSelected;
            this.defaultSelected = defaultSelected;

        }

        public String getLanguageToShow() {
            return languageToShow;
        }

        public void setLanguageToShow(String languageToShow) {
            this.languageToShow = languageToShow;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

    public class CheckedObjectViewAdapter extends BaseAdapter {

        private ArrayList<selectLanguage> names;
        private Context context;
        private LayoutInflater inflter;


        public CheckedObjectViewAdapter(LayoutInflater inflater, ArrayList<selectLanguage> names) {

            this.names = names;
            inflter = inflater;
            if (names != null) {
                for (int i = 0; i < names.size(); i++) {
                    if (names.get(i).isSelected()) {
                        selectedPosition = i;
                        selectedLanguage = names.get(i).getLanguage();
                    }
                }
            }
        }

        public void addData(ArrayList<selectLanguage> names) {

            this.names = names;
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i).isSelected()) {
                    selectedPosition = i;
                    selectedLanguage = names.get(i).getLanguage();
                    if (names.get(i).defaultSelected) {
                        if (TextUtils.isEmpty(PrefUtils.getInstance().getForceAcceptLanguage())) {
                            ApplicationController.IS_VERNACULAR_TO_BE_SHOWN = false;
                        }
                        PrefUtils.getInstance().setVernacularLanguage(false);
                    } else {
                        if (TextUtils.isEmpty(PrefUtils.getInstance().getForceAcceptLanguage())) {
                            ApplicationController.IS_VERNACULAR_TO_BE_SHOWN = true;
                        }
                        PrefUtils.getInstance().setVernacularLanguage(true);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = inflter.inflate(R.layout.app_language_item, null);
            if (names == null || names.get(position) == null) {
                return null;
            }
            final CheckedTextView simpleCheckedTextView = view.findViewById(R.id.text1);
            simpleCheckedTextView.setText(names.get(position).getLanguageToShow().substring(0, 1).toUpperCase() + names.get(position).getLanguageToShow().substring(1).toLowerCase());
            simpleCheckedTextView.setChecked(names.get(position).isSelected());

            if(!names.get(position).isSelected()){
                simpleCheckedTextView.setBackground(mContext.getResources().getDrawable(R.drawable.language_rounded_corner));
                simpleCheckedTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            }else {
                simpleCheckedTextView.setBackground(mContext.getResources().getDrawable(R.drawable.language_rounded_corner_selected));
                simpleCheckedTextView.setTextColor(mContext.getResources().getColor(R.color.black));
            }

            if (!defaultValue && selectedLanguage.isEmpty()) {
                langaugeArray.get(0).setSelected(true);
                defaultValue = true;
            }
            // perform on Click Event Listener on CheckedTextView
            simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (simpleCheckedTextView.isChecked()) {
                        for (int i = 0; i < names.size(); i++) {
                            if (i == position) {
                                if (defaultValue) {
                                    langaugeArray.get(0).setSelected(false);
                                    defaultValue = false;
                                }
                            }
                        }  // simpleCheckedTextView.setChecked(false);
                        // set cheek mark drawable and set checked property to false
                       /* names.get(position).setSelected(false);
                        simpleCheckedTextView.setChecked(false);
                        notifyDataSetChanged();
                        langaugeArray.get(position).setSelected(false);*/
                    } else {
                        for (int i = 0; i < names.size(); i++) {
                            names.get(i).isSelected = false;
                        }
                        names.get(position).setSelected(true);
                        simpleCheckedTextView.setChecked(true);
                        notifyDataSetChanged();
                        /*names.get(position).setSelected(true);
                        simpleCheckedTextView.setChecked(true);
                        notifyDataSetChanged();
                        langaugeArray.get(position).setSelected(true);*/
                    }
                    if (!names.get(position).defaultSelected) {
                        if (TextUtils.isEmpty(PrefUtils.getInstance().getForceAcceptLanguage())) {
                            ApplicationController.IS_VERNACULAR_TO_BE_SHOWN = true;
                        }
                        PrefUtils.getInstance().setVernacularLanguage(true);
                    } else {
                        if (TextUtils.isEmpty(PrefUtils.getInstance().getForceAcceptLanguage())) {
                            ApplicationController.IS_VERNACULAR_TO_BE_SHOWN = false;
                        }
                        PrefUtils.getInstance().setVernacularLanguage(false);
                    }
                    selectedPosition = position;
                    selectedLanguage = names.get(position).getLanguage();
                }
            });
            return view;
        }
    }

    public void showProgressBar(boolean shouldUseProgressBar) {

        if (mContext == null) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        if (shouldUseProgressBar) {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
            ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
            final int version = Build.VERSION.SDK_INT;
            if (version < 21) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
            }
        }

    }

    public void dismissProgressBar(boolean shouldUseProgressBar) {
        try {
            if (!isAdded()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing() && !shouldUseProgressBar) {
                mProgressDialog.dismiss();
            }
            if (mProgressBar != null
                    && mProgressBar.getVisibility() == View.VISIBLE
                    && shouldUseProgressBar) {
                mProgressBar.setVisibility(GONE);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}