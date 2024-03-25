package com.myplex.myplex.ui.activities;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;

public class CardDetailsActivity extends BaseActivity {
    private boolean mAutoPlay = false;
    private Context mContext;
    private BaseFragment mDetailsFragment;
    private androidx.appcompat.app.ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);
        mContext = this;
        mActionBar = getSupportActionBar();
        mDetailsFragment = new CardDetails();
        CardData baseCardData = null;
        if(getIntent().hasExtra(CardDetails.PARAM_CARD_ID)){
            Bundle args = new Bundle();
            String content_id= getIntent().getStringExtra(CardDetails.PARAM_CARD_ID);
            args.putString(CardDetails.PARAM_CARD_ID,content_id);
            if(getIntent().hasExtra(CardDetails.PARAM_CARD_DATA_TYPE)){
                String content_type= getIntent().getStringExtra(CardDetails.PARAM_CARD_DATA_TYPE);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, content_type);
            }
            if(getIntent().hasExtra(CardDetails.PARAM_AUTO_PLAY)) {
                args.putBoolean(CardDetails.PARAM_AUTO_PLAY, getIntent().getBooleanExtra(CardDetails.PARAM_AUTO_PLAY, false));
            }
            if(getIntent().hasExtra(CardDetails.PARAM_EPG_DATE_POSITION)){
                args.putInt(CardDetails.PARAM_EPG_DATE_POSITION, getIntent().getIntExtra(CardDetails.PARAM_EPG_DATE_POSITION, 0));
            }
            if(getIntent().hasExtra(CardDetails.PARAM_PARTNER_TYPE)){
                args.putInt(CardDetails.PARAM_PARTNER_TYPE, getIntent().getIntExtra(CardDetails.PARAM_PARTNER_TYPE, CardDetails.Partners.APALYA));
            }
            if(getIntent().hasExtra(APIConstants.NOTIFICATION_PARAM_NID)){
                String nid = getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_NID);
                args.putString(APIConstants.NOTIFICATION_PARAM_NID, nid);
            }
            if(getIntent().hasExtra(APIConstants.NOTIFICATION_PARAM_TITLE)){
                args.putString(APIConstants.NOTIFICATION_PARAM_TITLE, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_TITLE));
            }
            mDetailsFragment.setArguments(args);
        }

        mDetailsFragment.setBaseCardData(baseCardData);
        mDetailsFragment.setContext(mContext);
        mDetailsFragment.setBaseActivity(this);
        overlayFragment(mDetailsFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_settings, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void overlayFragment(BaseFragment fragment) {
        if(fragment == null)
            return;

        fragment.setContext(this);
        try{
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if(fragment instanceof CardDetails){
				/*transaction.setCustomAnimations(R.animator.fragment_slide_left_enter,
						R.animator.fragment_slide_left_exit,
						R.animator.fragment_slide_right_enter,
						R.animator.fragment_slide_right_exit);*/
                transaction.add(R.id.content_detail, fragment);
            }

            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOrientation(int REQUEST_ORIENTATION) {
        setRequestedOrientation(REQUEST_ORIENTATION);
    }

    @Override
    public int getOrientation() {
        return getRequestedOrientation();
    }

    @Override
    public void hideActionBar() {
        if(mActionBar != null){
            mActionBar.hide();
        }
    }

    @Override
    public void showActionBar() {
        if(mActionBar != null){
            mActionBar.show();
        }
    }

    @Override
    public void onBackPressed() {
        if(mDetailsFragment.onBackClicked()){
            return;
        }
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
