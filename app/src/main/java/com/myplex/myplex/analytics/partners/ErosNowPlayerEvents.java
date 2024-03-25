package com.myplex.myplex.analytics.partners;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.PlayerEventsRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.media.exoVideo.PlayerEventListenerInterface;
import com.myplex.myplex.model.PlayerEvent;

/**
 * Created by Srikanth on 10-Oct-17.
 */

public class ErosNowPlayerEvents implements PlayerEventListenerInterface {

    public static final String PARAM_MEDIA_URL = "mediaUrl";
    public static final String PARAM_CONTENT_ID = "content_id";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_SECDIFF = "secDiff";
    public static final String PARAM_BITRATE = "bitrate";
    public static final String PARAM_PLATFORM = "platform";

    public ErosNowPlayerEvents() {
        super();
    }

    @Override
    public void onPlayerEvent(final PlayerEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateEvent(event);
            }
        }).start();

    }

    public void updateEvent(PlayerEvent event) {
        LoggerD.debugDownload("postAnalyticsEventsRequest: " + event);
        PlayerEventsRequest.Params params = new PlayerEventsRequest.Params(event._id, event.title,
                event.partnerId, event.partnerName,
                event.mediaUrl, event.action,
                event.bitrate,event.secDiff,
                event.contentType, event.resolution);
        PlayerEventsRequest analyticsEventsRequest = new PlayerEventsRequest(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (null == response || response.body() == null) {
                    onFailure(new Throwable(ApplicationController.getAppContext().getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                LoggerD.debugDownload("postAnalyticsEventsRequest: player status update " +
                        "message- " + response.body().message +
                        "status- " + response.body().status);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugDownload("postAnalyticsEventsRequest: player status update " +
                        "t- " + t.getMessage() +
                        "errorCode- " + errorCode);
            }
        });
        APIService.getInstance().execute(analyticsEventsRequest);
    }

}
