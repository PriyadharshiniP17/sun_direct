package com.myplex.myplex.ui.activities;

import static com.myplex.api.APIConstants.ABOUT_US;
import static com.myplex.api.APIConstants.CONTACT_US_TITLE_FOR_MORE;
import static com.myplex.api.APIConstants.FAQS;
import static com.myplex.api.APIConstants.PRIVACY_POLICY;
import static com.myplex.api.APIConstants.RATE_US;
import static com.myplex.api.APIConstants.REFER_FRIEND;
import static com.myplex.api.APIConstants.TERMS_AND_CONDITIONS;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListener;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.util.PrefUtils;


public  class MoreSectionFragment extends BaseFragment {
   RecyclerView recyclerView;
   MoreRecyclerAdapter adapter;
   private LinearLayoutManager mLinearLayoutManager;

   String moreData[] = {ABOUT_US,TERMS_AND_CONDITIONS,PRIVACY_POLICY/*,CONTACT_US_TITLE_FOR_MORE*/,FAQS};

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);}
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_more_section, container, false);
      recyclerView =(RecyclerView)view.findViewById(R.id.more_recycler_view);
      mLinearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
      recyclerView.setLayoutManager(mLinearLayoutManager);
      adapter= new MoreRecyclerAdapter(requireContext(),moreData);
      recyclerView.setAdapter(adapter);
      adapter.setItemClickListener(new ItemClickListener() {
         @Override
         public void onClick(View view, int position, int parentPosition, String name) {
            switch (name) {
/*
               case CONTACT_US_TITLE_FOR_MORE:{
                  Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                  mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.contact_us));

                  if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                     return;
                  }
                  mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                  startActivity(mIntent);
                  break;
               }
*/
               case PRIVACY_POLICY: {
                  Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                  mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));

                  if (TextUtils.isEmpty(PrefUtils.getInstance().getPrivacy_policy_url())) {
                     return;
                  }
                  mIntent.putExtra("url", PrefUtils.getInstance().getPrivacy_policy_url());
                  startActivity(mIntent);
                  break;
               }
               case TERMS_AND_CONDITIONS: {
                  Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                  mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.tnc));

                  if (TextUtils.isEmpty(PrefUtils.getInstance().getTncUrl())) {
                     return;
                  }
                  mIntent.putExtra("url", PrefUtils.getInstance().getTncUrl());
                  startActivity(mIntent);
                  break;
               }

               case FAQS: {
                  Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                  mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.faq));

                  if (TextUtils.isEmpty(PrefUtils.getInstance().getFaq_url())) {
                     return;
                  }
                  mIntent.putExtra("url", PrefUtils.getInstance().getFaq_url());
                  startActivity(mIntent);
                  break;
               }

               case ABOUT_US: {
                  Intent tnc = new Intent(mContext, LiveScoreWebView.class);
                  Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                  mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.more));

                  if (TextUtils.isEmpty(PrefUtils.getInstance().getAboutapp_url())) {
                     return;
                  }
                  mIntent.putExtra("url", PrefUtils.getInstance().getAboutapp_url());
                  startActivity(mIntent);
                  break;
               }
               case RATE_US:{
                  Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                  mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.rate));

                  if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                     return;
                  }
                  mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                  startActivity(mIntent);
                  break;
               }
            }
         }
      });
      return view;
   }
}
