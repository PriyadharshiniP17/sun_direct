package com.myplex.myplex.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.myplex.myplex.R;
import com.myplex.myplex.gcm.MyGcmListenerService;


/**
 * Created by Apalya on 21-Aug-15.
 */
public class LanguageDebugActivity extends Activity {
private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_languages);
        mContext =this;
    }
    public void makeNotification(View v){

        Intent intent = new Intent();

        switch (v.getId()){
            case R.id.telugu_btn:

                intent.putExtra("mp_message", "\\u0c39\\u0c3e\\u0c1f\\u0c4d \\u0c37\\u0c3e\\u0c1f\\u0c4d: \\u0c1b\\u0c3e\\u0c30\\u0c4d\\u0c2e\\u0c3f\\u0c28\\u0c3f \\u0c2e\\u0c41\\u0c26\\u0c4d\\u0c26\\u0c46\\u0c1f\\u0c4d\\u0c1f\\u0c47\\u0c38\\u0c3f\\u0c28 \\u0c24\\u0c4d\\u0c30\\u0c3f\\u0c37");
                intent.putExtra("_id", "202");
                intent.putExtra("_aid", "70316");
                intent.putExtra("_ll", "te");
                intent.putExtra("imageUrl", "http://i.imgur.com//JKAz0Sx.png");
                break;
            case R.id.bengali_btn:

                intent.putExtra("mp_message", "অশ্বিন, অমিতের ঘূর্ণিতে বিধ্বস্ত শ্রীলঙ্কা, টেস্ট জিতল ভারত");
                intent.putExtra("_id", "dAvTmHjiinI");
                intent.putExtra("_aid", "70403");
                intent.putExtra("_ll", "bn");
                intent.putExtra("imageUrl", "http:\\/\\/www.onlinefmradio.in\\/videos\\/thumbs\\/x2kwt5s.jpg");
                break;
            case R.id.gujarati_btn:

                intent.putExtra("mp_message", "ટ્રાફિક જામમાં ફસાયેલી કારમાં બાળકીનો જન્મ, જૂમી ઉઠ્યો પિતા");
                intent.putExtra("_id", "dAvTmHjiinI");
                intent.putExtra("_aid", "70403");
                intent.putExtra("_ll", "gu");
                intent.putExtra("imageUrl", "http:\\/\\/www.newreleasetoday.com\\/images\\/news_images\\/news_img_f_1454450437.jpg");
                break;
            case R.id.hindi_btn:

                intent.putExtra("mp_message", "\\u0906\\u0902\\u0927\\u094d\\u0930 \\u092a\\u094d\\u0930\\u0926\\u0947\\u0936 \\u0915\\u0947 \\u0905\\u0928\\u0902\\u0924\\u092a\\u0941\\u0930 \\u092e\\u0947\\u0902 \\u091f\\u094d\\u0930\\u0947\\u0928 \\u0939\\u093e\\u0926\\u0938\\u093e, \\u0915\\u093e\\u0902\\u0917\\u094d\\u0930\\u0947\\u0938 \\u0935\\u093f\\u0927\\u093e\\u092f\\u0915 \\u0938\\u092e\\u0947\\u0924 5 \\u0915\\u0940 \\u092e\\u094c\\u0924, \\u0915\\u0908 \\u0918\\u093e\\u092f\\u0932.");
                intent.putExtra("_aid", "40448");
                intent.putExtra("_id", "201");
                intent.putExtra("_ll", "hi");
                intent.putExtra("imageUrl", "http:\\/\\/media2.intoday.in\\/indiatoday\\/images\\/stories\\/ranbir-story- -fb_647_121815110833.jpg");
                break;
            case R.id.kannada_btn:

                intent.putExtra("mp_message", "ಹರ್ಭಜನ್ ಸಿಂಗ್ ದಾಖಲೆ ಮುರಿದ ರವಿಚಂದ್ರನ್ ಅಶ್ವಿನ");
                intent.putExtra("_id", "dAvTmHjiinI");
                intent.putExtra("_aid", "70403");
                intent.putExtra("_ll", "kn");
                intent.putExtra("imageUrl", "http:\\/\\/www.photofurl.com\\/wp-content\\/uploads\\/2010\\/01\\/gun-hindi-film-red-alert-wallpaper.jpg");
                break;
            case R.id.marathi_btn:

                intent.putExtra("mp_message", "ഇന്ത്യയെ കുറിച്ച് നിങ്ങള് ക്ക് ഒരു ചുക്കും " +
                        "അറിയില്ല.");
                intent.putExtra("_id", "dAvTmHjiinI");
                intent.putExtra("_aid", "70403");
                intent.putExtra("_ll", "mr");
                intent.putExtra("imageUrl", "http://i.imgur.com/cZ5koSy.jpg");
                break;
            case R.id.tamil_btn:

                intent.putExtra("mp_message", "அம்மம்மா சரணம் சரணம் உன் பாதங்கள்...!");
                intent.putExtra("_id", "dAvTmHjiinI");
                intent.putExtra("_aid", "70403");
                intent.putExtra("_ll", "ta");
                intent.putExtra("imageUrl", "http:\\/\\/www.onlinefmradio.in\\/videos\\/thumbs\\/x2kwt5s.jpg");
                break;
            default:
                break;
        }
        intent.setClass(mContext, MyGcmListenerService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        mContext.startService(intent);


    }
}
