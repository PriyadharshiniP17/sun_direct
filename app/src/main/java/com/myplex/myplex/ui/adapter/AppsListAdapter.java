package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myplex.api.APIConstants;
import com.myplex.model.OTTApp;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class AppsListAdapter extends BaseAdapter {
    private Context mContext;
    private List<OTTApp> mOTTAppList;

    public AppsListAdapter(Context context, List<OTTApp> ottAppList) {
        mContext = context;
        mOTTAppList = ottAppList;
    }

    @Override
    public int getCount() {
        return mOTTAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mOTTAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_movie_apps, null, false);
            mViewHolder = new ViewHolder();
            mViewHolder.mAppIcon = (ImageView) convertView.findViewById(R.id.thumbnail__app_icon);
            mViewHolder.mAppOfferDescription = (TextView) convertView.findViewById(R.id
                    .app_offer_description);
            mViewHolder.mAppTitle = (TextView) convertView.findViewById(R.id
                    .app_title);
            mViewHolder.mAppInstallTxt = (TextView)convertView.findViewById(R.id.install_txt);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final OTTApp ottApp = mOTTAppList.get(position);
        if(ottApp == null){
            return convertView;
        }
        if(ottApp.imageUrl != null){
            PicassoUtil.with(mContext).load(ottApp.imageUrl,mViewHolder.mAppIcon,R.drawable
                    .tv_guide_thumbnail_default);
        }
        if(ottApp.description != null){
            mViewHolder.mAppOfferDescription.setText(ottApp.description);
        }

        if(ottApp.title != null){

            mViewHolder.mAppTitle.setText(ottApp.title);
        }
        PackageManager pm = mContext.getPackageManager();

         if(ottApp.androidPackageName == null){
             mViewHolder.mAppInstallTxt.setVisibility(View.INVISIBLE);
         }else {
             mViewHolder.mAppInstallTxt.setVisibility(View.VISIBLE);

         }
         Intent launchIntent = pm.getLaunchIntentForPackage(ottApp.androidPackageName);
        if(launchIntent!=null){
           // mContext.startActivity(launchIntent);
            mViewHolder.mAppInstallTxt.setText(" Open ");

        }else {
            mViewHolder.mAppInstallTxt.setText("Install Now");
        }
        mViewHolder.mAppInstallTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOTTAppClicked(ottApp);
            }
        });


        return convertView;
    }
    private void handleOTTAppClicked(final OTTApp appData){

        PackageManager pm = mContext.getPackageManager();
        try {
            final Intent launchIntent = pm.getLaunchIntentForPackage(appData.androidPackageName);
            if (launchIntent != null) {
                //app is installed launching the app
                mContext.startActivity(launchIntent);
                return;
            }
            String confirmMessage = mContext.getString(R.string
                    .confirm_download_app);
            String alertMessage = appData.confirmationMessage;
            if (alertMessage == null) {
                alertMessage = "Watch free movies on " + appData.title;
            }
            AlertDialogUtil.showAlertDialog(mContext, alertMessage, "",
                    mContext.getString(R.string.cancel), confirmMessage, new AlertDialogUtil.DialogListener() {
                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {

                            String url = appData.androidAppUrl;

                            if (appData.installType != null && appData.installType.equalsIgnoreCase("ottConfirm")) {
                                url = APIConstants.getOTTAppDownloadUrl(appData.appName);
                            }
                            if (url != null) {
                                //app is not installed launching the playstore
                                if(ConnectivityUtil.isConnected(mContext)){
                                    launchPlayStore(url);

                                }else {
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void launchPlayStore(String androidAppUrl) {
        Uri appStoreLink = Uri.parse(androidAppUrl);
        mContext.startActivity(new Intent(Intent.ACTION_VIEW,appStoreLink));
    }

    public class ViewHolder {
        ImageView mAppIcon;
        TextView mAppOfferDescription;
        TextView mAppTitle;
        TextView mAppInstallTxt;
    }
}
