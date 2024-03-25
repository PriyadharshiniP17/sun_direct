package com.myplex.myplex.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.myplex.api.APIConstants;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;

import java.util.ArrayList;
import java.util.List;


public class UrlGatewayActivity extends Activity {

    private static final String TAG = UrlGatewayActivity.class.getSimpleName();
    public static final String PATH_WATCH = "watch";
    private static final String PATH_PAGE = "page";
    private static final String PATH_LIVE = "live";
    private static final String PATH_DETAIL = "detail";
    public static final String PATH_ADD_TO_WATCH_LIST = "add-to-watchlist";
    private static final String PATH_CONTENT_TYPE = "content-type";
    private static final String PATH_PROMO = "promo";
    private static final String PATH_AD = "ad";


    public enum SchemeType {
        http, https, ideamoviesandtv, sundirect
    }

    ;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            parseFirebaseDeepLink();

        } catch (Throwable e) {
            e.printStackTrace();
            //Log.d(TAG, "exception in handleExternalUrl " + e);

            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            startMainActivity(intent);
        }

    }

    private boolean handleExternalUrl(Uri uri) {

        //Log.d(TAG, "uri:" + getIntent().getData());
        String deepLinkType=null;
        boolean intentHandled = false;
		Intent intent = new Intent(this, MainActivity.class);

//        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (getIntent() == null || getIntent().getData() == null) {
            startMainActivity(intent);
            return intentHandled;
        }

//        Uri uri = getIntent().getData();

        if (uri == null || uri.getScheme() == null) {
            startMainActivity(intent);
            return intentHandled;
        }

        SchemeType scheme = SchemeType.valueOf(uri.getScheme());

//		String url = uri.toString();
        List<String> list = new ArrayList<>();
        String utm_source=uri.getQueryParameter("utm_source");
        if (uri.getPathSegments() != null) {
            list = uri.getPathSegments();
        }


        switch (scheme) {
            case http:
            case ideamoviesandtv:
            case https:
            case sundirect:
            default:



                if (list != null) {
                    int indexOfPathQuery = 0;
                    String firstPathSegment = null;
                    try {
                        if (list.contains(PATH_PAGE)) {
                            indexOfPathQuery = list.indexOf(PATH_PAGE);
                        } else if (list.contains(PATH_WATCH)) {
                            indexOfPathQuery = list.indexOf(PATH_WATCH);
                        } else if (list.contains(PATH_DETAIL)) {
                            indexOfPathQuery = list.indexOf(PATH_DETAIL);
                        } else if (list.contains(PATH_LIVE)) {
                            indexOfPathQuery = list.indexOf(PATH_LIVE);
                        } else if (list.contains(PATH_ADD_TO_WATCH_LIST)) {
                            indexOfPathQuery = list.indexOf(PATH_LIVE);
                        }
                        firstPathSegment = list.get(indexOfPathQuery);
                        if(list.size() == 1) {
                            intent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, firstPathSegment);
                            startMainActivity(intent);
                            return true;
                        }
                        String secondPathSegment = list.get(indexOfPathQuery + 1);
                        intentHandled = true;
                        ComScoreAnalytics.getInstance().setEventDeepLink(uri.toString(),firstPathSegment);
                        switch (firstPathSegment) {
                            case PATH_WATCH:
                            case PATH_DETAIL:
                            case PATH_LIVE:
                                if (list.contains(PATH_PROMO)
                                        && list.contains(PATH_AD)) {
                                    intent.putExtra(CleverTap.SOURCE_PROMO_VIDEO_AD, true);
                                }
                                intent.putExtra(APIConstants.AFFILIATE_VALUE,utm_source);
                                intent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, secondPathSegment);
                                break;
                            case PATH_PAGE:
                                intent.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, secondPathSegment);
                                break;
                            case PATH_ADD_TO_WATCH_LIST:
                                if (list.contains(PATH_CONTENT_TYPE)) {
                                    String fourthPathSegment = list.get(list.indexOf(PATH_CONTENT_TYPE) + 1);
                                    intent.putExtra(APIConstants.NOTIFICATION_PARAM_ADD_TO_WATCHLIST_CONTENT_TYPE, fourthPathSegment);
                                }
                                intent.putExtra(APIConstants.NOTIFICATION_PARAM_ADD_TO_WATCHLIST, secondPathSegment);
                                break;
                        }
                        intent.putExtra(APIConstants.MESSAGE_TYPE, APIConstants.NOTIFICATION_PARAM_MESSAGE_TYPE_INAPP);

                    } catch (Exception e) {
                        Log.d(TAG, "Invalid content id:" + firstPathSegment);
                        ////Log.d(TAG, e.getMessage());
                    }
                }
                break;
        }
        startMainActivity(intent);
        return intentHandled;
    }

    private void startMainActivity(Intent intent) {
        if (intent == null) {
            intent = new Intent(this, LoginActivity.class);
        }

/**    Added for branch deeplinks to transfer deeplink data to LoginActivity or splash activity,
 * this makes deeplink data available to LoginActivity or splash activity though it is not configured with intent schemes.*/
		/*if (intent != null
				&& getIntent() != null) {
			intent.setData(getIntent().getData());
		}*/
        startActivity(intent);
        finish();
    }

    void parseFirebaseDeepLink(){
        if (FirebaseDynamicLinks.getInstance()!=null) {
            FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(this, new com.google.android.gms.tasks.OnSuccessListener<PendingDynamicLinkData>() {
                @Override
                public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                    if (pendingDynamicLinkData != null) {
                        Uri uri = Uri.parse(pendingDynamicLinkData.getLink().toString());
                        handleExternalUrl(uri);
                    }else {
                        handleExternalUrl(getIntent().getData());
                    }
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    LoggerD.debugOTP(e.toString());
                }
            });
        }else {
            handleExternalUrl(getIntent().getData());
        }
    }

}