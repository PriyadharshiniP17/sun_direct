package com.myplex.myplex.ui.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.PreferredLanguagesRequest;
import com.myplex.model.PreferredLanguageData;
import com.myplex.model.PreferredLanguageItem;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterPreferredLanguages;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FragmentPreferredLanguages extends BaseFragment {
    private LayoutInflater mInflater;
    private Toolbar mToolbar;
    private View mCustomToolbarLayout;
    private RecyclerView mPreferredLangauesList;
    private AdapterPreferredLanguages adapterPreferredLanguages;
    private List<PreferredLanguageItem> preferredLanguages;
    private Button mSkipButton, mDoneButton;
    private String altTitle;

    public interface OnPreferredLanguagesActionPerformedListener {
        void onSkipClicked();

        void onDoneClicked();
    }

    private OnPreferredLanguagesActionPerformedListener onPreferredLanguagesActionPerformedListener;

    public void setOnPreferredLanguagesActionPerformedListener(OnPreferredLanguagesActionPerformedListener onPreferredLanguagesActionPerformedListener) {
        this.onPreferredLanguagesActionPerformedListener = onPreferredLanguagesActionPerformedListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mInflater = LayoutInflater.from(mContext);
        View rootView = mInflater.inflate(R.layout.fragment_preferred_languages, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mCustomToolbarLayout = mInflater.inflate(R.layout.custom_toolbar_preferred_languages, null, false);
        TextView mToolbarTitleOtherLang = (TextView) mCustomToolbarLayout.findViewById(R.id.toolbar_header_title_lang);
        mToolbar.addView(mCustomToolbarLayout);

        if(PrefUtils.getInstance().getVernacularLanguage()){

            if ( !TextUtils.isEmpty(altTitle) ){
                mToolbarTitleOtherLang.setText(altTitle);
                mToolbarTitleOtherLang.setVisibility(View.VISIBLE);
            }else{
                mToolbarTitleOtherLang.setVisibility(View.GONE);
            }


        }else{
            mToolbarTitleOtherLang.setVisibility(View.GONE);
        }
        mPreferredLangauesList = (RecyclerView) rootView.findViewById(R.id.preferred_languages_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        preferredLanguages = new ArrayList<PreferredLanguageItem>();
        mPreferredLangauesList.setLayoutManager(layoutManager);
        mPreferredLangauesList.addItemDecoration(new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_8)));
        adapterPreferredLanguages = new AdapterPreferredLanguages(preferredLanguages);
        mPreferredLangauesList.setAdapter(adapterPreferredLanguages);
        mSkipButton = (Button) rootView.findViewById(R.id.button_skip);
        mDoneButton = (Button) rootView.findViewById(R.id.button_done);
        PrefUtils.getInstance().setPreferredLanguageSelectionFragmentshown(true);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showProgressBar(true);
        }
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onPreferredLanguagesActionPerformedListener != null) {
                    onPreferredLanguagesActionPerformedListener.onSkipClicked();
                    //adapterPreferredLanguages.storeDefaultLanguesinPrefs();
                }
            }
        });
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(didMakeAnyChangesToLanguages()){
                    if (onPreferredLanguagesActionPerformedListener != null) {
                        onPreferredLanguagesActionPerformedListener.onSkipClicked();
                    }
                    return;
                }
                if (onPreferredLanguagesActionPerformedListener != null) {
                    CleverTap.eventPreferedLanguage();
                    CleverTap.updateUserProfileWithPreferredLanguages(mContext);
                    adapterPreferredLanguages.storeSelectedLanguagesinPrefs();
                    onPreferredLanguagesActionPerformedListener.onDoneClicked();
                }
            }
        });

        PreferredLanguagesRequest.Params params = new PreferredLanguagesRequest.Params(PrefUtils.getInstance().getPrefClientkey());
        PreferredLanguagesRequest preferredLanguagesRequest = new PreferredLanguagesRequest(params, new APICallback<PreferredLanguageData>() {
            @Override
            public void onResponse(APIResponse<PreferredLanguageData> response) {
                if (response == null || response.body() == null ||response.body().languages == null
                        || response.body().languages.isEmpty() || response.body().languages.get(0).terms == null) {
                    return;
                }
                SDKLogger.debug("success");
                SDKLogger.debug(response.toString());
                List<PreferredLanguageItem> preferredLanguageItemsSelected = PrefUtils.getInstance().getPreferredLanguageItems();
                if (preferredLanguageItemsSelected != null && preferredLanguageItemsSelected.size() > 0) {
                    for (int i = 0; i < preferredLanguageItemsSelected.size(); i++) {
                        for (int j = 0; j < response.body().languages.get(0).terms.size(); j++) {
                            if (preferredLanguageItemsSelected.get(i).getTerm().equalsIgnoreCase(response.body().languages.get(0).terms.get(j).getTerm()) && preferredLanguageItemsSelected.get(i).isChecked && !preferredLanguageItemsSelected.get(i).isDefault()) {
                                response.body().languages.get(0).terms.get(j).isChecked = true;
                            }
                        }
                    }
                }
                setData(response.body().languages.get(0).terms);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("failed");
                if (onPreferredLanguagesActionPerformedListener != null) {
                    onPreferredLanguagesActionPerformedListener.onSkipClicked();
                }
            }
        });
        APIService.getInstance().execute(preferredLanguagesRequest);
        return rootView;
    }

    private boolean didMakeAnyChangesToLanguages() {
        List<PreferredLanguageItem> languageItemList =  adapterPreferredLanguages.getSelectedLanguages();
        List<PreferredLanguageItem> savedLanguageItems = PrefUtils.getInstance().getPreferredLanguageItems();
        if(savedLanguageItems == null || languageItemList == null){
            return false;
        }
        if(languageItemList.size() != savedLanguageItems.size())
            return false;

        for(int i=0; i< languageItemList.size();i++){

            //Check for nulls before proceeding
            if(languageItemList.get(i) == null
                    || languageItemList.get(i).getHumanReadable() == null
                    || savedLanguageItems.get(i) == null
                    || savedLanguageItems.get(i).getHumanReadable() == null){
                return false;
            }
            //Comparing the humanReadable to know if the same language is saved or not
            if(!languageItemList.get(i).getHumanReadable()
                    .equalsIgnoreCase(savedLanguageItems.get(i).getHumanReadable())){
                return false;
            }
        }
        return true;
    }

    private void setData(List<PreferredLanguageItem> languages) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).dismissProgressBar(true);
        }
        preferredLanguages.addAll(languages);
        adapterPreferredLanguages.notifyDataSetChanged();
    }

    public static FragmentPreferredLanguages newInstance() {
        FragmentPreferredLanguages fragmentPreferredLanguages = new FragmentPreferredLanguages();
        return fragmentPreferredLanguages;
    }

    public void setAltTitle(String altTitle){
        this.altTitle = altTitle;
    }
}
