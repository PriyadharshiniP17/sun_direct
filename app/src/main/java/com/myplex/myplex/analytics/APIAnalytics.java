package com.myplex.myplex.analytics;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.AnalyticsEventsReportRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;

/**
 * Created by Srikanth on 25-Jul-17.
 */

public class APIAnalytics {
    public static final String CATEGORY_SUBSCRIPTION = "subscription";
    public static final String EVENT_SUBSCRIPTION_SENT_ACTIVATION_SMS = "sent activation sms";
    public static final String EVENT_SUBSCRIPTION_SENT_CONFIRMATION_SMS = "sent confirmation sms";
    public static final String EVENT_SUBSCRIPTION_CANCELLED_ACTIVATION = "cancelled activation";

    public static final String CATEGORY_DEKKHO_TV_ADS = "dekkhoTvAds";
    public static final String DEKKHO_TV_AD_EVENT = "dekkho tv ad event";
    public static final String DEKKHO_TV_AD_SKIPPED = "dekkho tv ad skipped";
    public static final String DEKKHO_TV_AD_CLICKED = "dekkho tv ad clicked";


    public static void postAnalyticsEventsRequest(String category, String action, String label, String value) {
        LoggerD.debugLog("postAnalyticsEventsRequest: " +
                "category: " + category +
                "action- " + action +
                "label- " + label +
                "value- " + value
        );
        AnalyticsEventsReportRequest.Params params = new AnalyticsEventsReportRequest.Params(category, action, label,value);
        AnalyticsEventsReportRequest analyticsEventsRequest = new AnalyticsEventsReportRequest(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (null == response || response.body() == null) {
                    onFailure(new Throwable(ApplicationController.getAppContext().getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                LoggerD.debugLog("postAnalyticsEventsRequest: player status update " +
                        "message- " + response.body().message +
                        "status- " + response.body().status);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugLog("postAnalyticsEventsRequest: player status update " +
                        "t- " + t.getMessage() +
                        "errorCode- " + errorCode);
            }
        });
        APIService.getInstance().execute(analyticsEventsRequest);
    }
}
