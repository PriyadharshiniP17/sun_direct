package com.myplex.myplex.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.LocationInfo;
import com.myplex.myplex.R;
import com.myplex.myplex.gcm.MyGcmListenerService;
import com.myplex.myplex.ui.activities.UrlGatewayActivity;

import org.json.JSONObject;


public class NotificationDebugActivity extends Activity {
    private static final String TAG = NotificationDebugActivity.class.getSimpleName();
    private Context mContext;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_debug_activity);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        if (locationInfo != null) {
            ((TextView) findViewById(R.id.btn_show_location)).setText("Address\n" +
                    "Postal Code- " + locationInfo.postalCode +
                    "Country Code- " + locationInfo.country +
                    "Area- " + locationInfo.area);
        }
        mContext = this;
//		_idTextView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Util.showFeedback(v);
//				Intent intent = new Intent();
//				intent.putExtra("_aid", "40201");
//				intent.putExtra("mp_message", "debugging notifications");
//				intent.setClass(mContext, PushIntentService.class);
//				mContext.startService(intent);
//			}
//		});
    }

    public void onClick(View v) {
        JSONObject jsonObject = new JSONObject();
        Intent intent = new Intent();
        intent.putExtra(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_WZRK_DEFAULT, APIConstants.PARAM_CLEVERTAP_NOTIFICATION_WZRK_DEFAULT);
        switch (v.getId()) {

            case R.id.webPage_launch:
                intent.putExtra("action", "launchWebPage");
                intent.putExtra("mp_message", "launch book my show");
                intent.putExtra(APIConstants.NOTIFICATION_PARAM_URL, "https://in.bookmyshow.com/");
                break;

            case R.id.send_push_message:
                intent.putExtra("mp_message", "Watch Aajtak Live Channel at 99762");
                intent.putExtra("mp_title", "Catch now");
                intent.putExtra("_id", "99762");
                intent.putExtra("action", "autoplay");
                intent.putExtra("type", "video");
                intent.putExtra("imageUrl", "https://telugu.filmibeat.com/img/2016/01/13-1452654658-kvp.jpg");
                break;
            case R.id.send_a_id:
/*                intent.putExtra("mp_message", "watch aajtak live tv ! " + i++);
                intent.putExtra("_id", "73942");
                intent.putExtra("action","autoplay");
                intent.putExtra("type","video");*/
                intent.putExtra("mp_message", "Watch Movie MIB 3 at 225297");
                intent.putExtra("mp_title", "Catch now");
                intent.putExtra("_id", "225297");
                intent.putExtra("action", "autoplay");
                intent.putExtra("type", "video");
                intent.putExtra("nid", "12314");
                intent.putExtra("imageUrl", "https:\\/\\/telugu.filmibeat.com\\/img\\/2016\\/01\\/13-1452654658-kvp.jpg");
                break;

            case R.id.send_a_aid:
                intent.putExtra("mp_message", "Watch Booo Show at 244472");
                intent.putExtra("mp_title", "Catch now");
                intent.putExtra("_id", "244472");
                intent.putExtra("action", "autoplay");
                intent.putExtra("type", "video");
                break;

            case R.id.send_page_notification:

                intent.putExtra("mp_message", "Watch Booo Show E1 at 244481");
                intent.putExtra("_id", "244481");
                intent.putExtra("action", "autoplay");
                intent.putExtra("type", "video");
                break;

            case R.id.send_movie_page_notification:

                intent.putExtra("mp_message", "Watch 1983 World Cup Final India vs WI at 243843");
                intent.putExtra("_id", "244481");
                break;

            case R.id.send_music_page_notification:

                intent.putExtra("mp_message", "free videos watch and download! send_music_page_notification");
                intent.putExtra("page", APIConstants.TYPE_MUSIC);
                break;

            case R.id.send_tv_shows_page_notification:

                intent.putExtra("mp_message", "free videos watch and download! send_tv_shows_page_notification");
                intent.putExtra("page", APIConstants.TYPE_PAGE_TV_SHOWS);
                break;

            case R.id.send_videos_page_notification:

                intent.putExtra("mp_message", "free videos watch and download! send_videos_page_notification");
                intent.putExtra("page", APIConstants.TYPE_PAGE_VIDEOS);
                break;

            case R.id.send_vurl_notification:

                intent.putExtra("mp_message", "catch up exciting video here!");
                intent.putExtra("vurl", "http://myplexv2betamovies.s3.amazonaws.com/533/533.high.mp4");
                break;
            case R.id.send_yuid_notification:
                intent.putExtra("mp_message", "watch youvraj 6 sixes!");
                intent.putExtra("yuid", "FqJc7D51Lrg");
                break;
            case R.id.send_news_id_notification:
                intent.putExtra("mp_message", "\\u0c26\\u0c3e\\u0c30\\u0c3f \\u0c24\\u0c2a\\u0c4d\\u0c2a\\u0c3f\\u0c28 \\u0c1a\\u0c02\\u0c26\\u0c4d\\u0c30\\u0c2c\\u0c3e\\u0c2c\\u0c41 \\u0c39\\u0c46\\u0c32\\u0c3f\\u0c15\\u0c3e\\u0c2a\\u0c4d\\u0c1f\\u0c30\\u0c4d: \\u0c2a\\u0c26\\u0c3f \\u0c28\\u0c3f\\u0c2e\\u0c3f\\u0c37\\u0c3e\\u0c32\\u0c41 \\u0c32\\u0c47\\u0c1f\\u0c4d");
                intent.putExtra("ver", "32");
//                intent.putExtra("imageUrl", "http:\\/\\/telugu-cdn.oneindia.com\\/img\\/2015\\/11\\/20-1448014686-gali-janardhan-reddy-bangalore-house-605.jpg");
                intent.putExtra("imageUrl", "https:\\/\\/telugu.filmibeat.com\\/img\\/2016\\/01\\/13-1452654658-kvp.jpg");
//                intent.putExtra("_aid", "70498");
                intent.putExtra("_ll", "te");
//                intent.putExtra("type","videoandtext");
                intent.putExtra("action", "autoplay");
                break;
            case R.id.send_news_video_url_notification:

                intent.putExtra("mp_message", "\\u0c26\\u0c3e\\u0c30\\u0c3f \\u0c24\\u0c2a\\u0c4d\\u0c2a\\u0c3f\\u0c28 \\u0c1a\\u0c02\\u0c26\\u0c4d\\u0c30\\u0c2c\\u0c3e\\u0c2c\\u0c41 \\u0c39\\u0c46\\u0c32\\u0c3f\\u0c15\\u0c3e\\u0c2a\\u0c4d\\u0c1f\\u0c30\\u0c4d: \\u0c2a\\u0c26\\u0c3f \\u0c28\\u0c3f\\u0c2e\\u0c3f\\u0c37\\u0c3e\\u0c32\\u0c41 \\u0c32\\u0c47\\u0c1f\\u0c4d");
                intent.putExtra("_id", "70862");
                intent.putExtra(mContext.getResources().getString(R.string.notification_tags), "Salman Khan, Entertainment");
                intent.putExtra("imageUrl", "http:\\/\\/telugu-cdn.oneindia.com\\/img\\/2015\\/11\\/20-1448014686-gali-janardhan-reddy-bangalore-house-605.jpg");
                intent.putExtra(mContext.getResources().getString(R.string.notification_video_url), "http:\\/\\/220.226.22.115:1935\\/vod\\/mp4:TV9News_201120151500.mp4\\/playlist.m3u8?wowzaplaystart=113753&wowzaplayduration=45749");
                intent.putExtra("type", "videoandtext");
                intent.putExtra("_ll", "te");
                intent.putExtra("action", "autoplay");
                break;
            case R.id.send_id_yuid_notification:
                intent.putExtra("mp_message", "\\u0c26\\u0c3e\\u0c30\\u0c3f \\u0c24\\u0c2a\\u0c4d\\u0c2a\\u0c3f\\u0c28 \\u0c1a\\u0c02\\u0c26\\u0c4d\\u0c30\\u0c2c\\u0c3e\\u0c2c\\u0c41 \\u0c39\\u0c46\\u0c32\\u0c3f\\u0c15\\u0c3e\\u0c2a\\u0c4d\\u0c1f\\u0c30\\u0c4d: \\u0c2a\\u0c26\\u0c3f \\u0c28\\u0c3f\\u0c2e\\u0c3f\\u0c37\\u0c3e\\u0c32\\u0c41 \\u0c32\\u0c47\\u0c1f\\u0c4d");
                intent.putExtra("_id", "70429");
                intent.putExtra(mContext.getResources().getString(R.string.notification_tags), "Salman Khan, Entertainment");
                intent.putExtra("imageUrl", "http:\\/\\/telugu.filmibeat.com\\/img\\/2016\\/01\\/13-1452654658-kvp.jpg");
                intent.putExtra("yuid", "nFRwoj0A_hg");
                intent.putExtra("_ll", "te");
                intent.putExtra("type", "videoandtext");
                break;
            case R.id.test_languages_supported_in_device:
                intent.setClass(mContext, LanguageDebugActivity.class);
                mContext.startActivity(intent);
                return;
            case R.id.send_a_vod_id:
                intent.putExtra("mp_message", "watch Vasanth Ki Dusri Shaadi!");
                intent.putExtra("_id", "15533");
                intent.putExtra("action", "autoplay");
                intent.putExtra("type", "video");
                break;
            case R.id.send_a_vodchannel_id:
                intent.putExtra("mp_message", "watch Badi Dooooor Se Aaye Hai all shows");
                intent.putExtra("_id", "14674");
                intent.putExtra("type", "video");
                break;
            case R.id.send_a_vodcategory_id:

                intent.putExtra("mp_message", "watch Comedy Videos!");
                intent.putExtra("_id", "6400");
                intent.putExtra("type", "video");
                break;
            case R.id.btn_launch_detailsPage:
                intent = new Intent(this, UrlGatewayActivity.class);
                startActivity(intent);
                return;

            case R.id.btn_launch_screen:
                intent = new Intent(this, UrlGatewayActivity.class);
//                intent.setData(Uri.parse("http://www.sundirectplay.in/notification/screen/music/"));
//                http://www.sundirectplay.com/notification/screen/music/
                startActivity(intent);
                return;

            case R.id.tv_detail_114130:
                intent = new Intent(this, UrlGatewayActivity.class);


                startActivity(intent);
                return;
//            android:id="@+id/movi_detail"
//            android:id="@+id/live_28103"
//            android:id="@+id/tv_detail_114130"

            case R.id.live_84730:
                intent = new Intent(this, UrlGatewayActivity.class);
                intent.setData(Uri.parse("https://www.sundirectplay.in/live/84730/"));
//                intent.setData(Uri.parse("http://www.sundirectplay.in/notification/screen/music/"));
//                http://www.sundirectplay.com/notification/screen/music/

//                https://www.sundirectplay.in/tv/detail/114130/
//                https://www.sundirectplay.in/live/28103/
//                https://www.sundirectplay.in/tv/detail/84186/
//                https://www.sundirectplay.in/tv/detail/114130/114138
//                https://www.sundirectplay.in/movie/detail/45424/

                startActivity(intent);
                return;

            //            android:id="@+id/movi_detail"
//            android:id="@+id/live_28103"
//            android:id="@+id/tv_detail_114130"

            case R.id.movi_detail:
                intent = new Intent(this, UrlGatewayActivity.class);
                intent.setData(Uri.parse("https://www.sundirectplay.in/movie/detail/45154/"));
//                intent.setData(Uri.parse("http://www.sundirectplay.in/notification/screen/music/"));
//                http://www.sundirectplay.com/notification/screen/music/

//                https://www.sundirectplay.in/tv/detail/114130/
//                https://www.sundirectplay.in/live/28103/
//                https://www.sundirectplay.in/tv/detail/84186/
//                https://www.sundirectplay.in/tv/detail/114130/114138
//                https://www.sundirectplay.in/movie/detail/45424/

                startActivity(intent);
                return;

            case R.id.btn_show_location:
                return;

            default:
                break;
        }
        sendMessage(intent);
//        intent.setClass(mContext, MyGcmListenerService.class);
//        intent.setAction("com.google.firebase.MESSAGING_EVENT");
//        mContext.startService(intent);


    }


    public void sendMessage(final Intent data) {
        MyGcmListenerService service = new MyGcmListenerService();
        service.setContext(this);
        service.processMessageData(data.getExtras());
    }

}