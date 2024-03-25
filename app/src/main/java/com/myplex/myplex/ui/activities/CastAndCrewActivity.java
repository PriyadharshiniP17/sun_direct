package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataRelatedCastItem;
import com.myplex.model.RelatedCastList;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.adapter.AdapterCastAndCrew;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by THIVIKRAMREDDY on 1/19/2016.
 */
public class CastAndCrewActivity extends BaseActivity {

    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView;
    private ImageView mChannelImageView;
    private RelativeLayout mRootLayout;

    private CardData mCardData;
    public static String CARD_DATA_CAST_AND_CREW_VALUE = "CARD_DATA_CAST_AND_CREW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast_and_crew);
        Context mContext = this;

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);


        if (getIntent().hasExtra(CARD_DATA_CAST_AND_CREW_VALUE)) {
            mCardData = (CardData) getIntent().getSerializableExtra(CARD_DATA_CAST_AND_CREW_VALUE);
        }
        initUI(mContext);
    }

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            //onBackPressed();
            //showOverFlowSettings(v);
        }
    };

    private void initUI(Context mContext) {
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mHeaderImageView.setOnClickListener(mCloseAction);

        List<RelatedCastList> relatedCastLists = new ArrayList<>();
        List<CardDataRelatedCastItem> pillarItemsList = new ArrayList<>();
        List<CardDataRelatedCastItem> roleNamesList = new ArrayList<>();
        if (mCardData != null) {
            if (mCardData.generalInfo != null && mCardData.generalInfo.title != null && !TextUtils.isEmpty(mCardData.generalInfo.title)) {
                mToolbarTitle.setText(mCardData.generalInfo.title);
            }
            for (int p = 0; p < mCardData.relatedCast.values.size(); p++) {
                if (mCardData.relatedCast.values.get(p).images.values != null && mCardData.relatedCast.values.get(p).images.values.size() != 0) {
                    pillarItemsList.add(mCardData.relatedCast.values.get(p));
                } else {
                    roleNamesList.add(mCardData.relatedCast.values.get(p));
                }
            }
            RelatedCastList pillarRelatedCastList = new RelatedCastList();
            RelatedCastList roleNamesRelatedCastList = new RelatedCastList();

            pillarRelatedCastList.values = pillarItemsList;
            pillarRelatedCastList.mLayoutType = APIConstants.LAYOUT_TYPE_PILLAR_LAYOUT;
            roleNamesRelatedCastList.values = roleNamesList;
            roleNamesRelatedCastList.mLayoutType = APIConstants.LAYOUT_TYPE_ROLE_NAME_LAYOUT;

            relatedCastLists.add(pillarRelatedCastList);
            relatedCastLists.add(roleNamesRelatedCastList);

            RecyclerView mRecyclerView = findViewById(R.id.cast_and_crew_recycler_view);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            VerticalSpaceItemDecoration mHorizontalMoviesDivieder = new VerticalSpaceItemDecoration((int) mContext
                    .getResources().getDimension(R.dimen.margin_gap_4));
            mRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);
            mRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);
            mRecyclerView.setItemAnimator(null);
            AdapterCastAndCrew adapterCastAndCrew = new AdapterCastAndCrew(relatedCastLists, mContext);
            mRecyclerView.setAdapter(adapterCastAndCrew);
        } else {
            mToolbarTitle.setText("Cast and Crew");
        }

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

}