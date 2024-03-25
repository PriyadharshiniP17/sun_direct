package com.myplex.sdk;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Samir on 12/10/2015.
 */
@RunWith(AndroidJUnit4.class)
public class DeviceRegistrationTest extends AndroidTestCase {

    public DeviceRegistrationTest(){

    }
    @Test
    public void test() {

       /* Log.d("APIService", "DeviceRegistrationTest started");
        /*Log.d("APIService", "DeviceRegistrationTest started");
        final DeviceRegistration.Params params = new DeviceRegistration.Params("ApalyaAndroid");

        DeviceRegistration deviceRegistration = new DeviceRegistration(getContext(),params, new
                APICallback<BaseReponseData>() {
            @Override
            public void onResponse(APIResponse<BaseReponseData> response) {
                Log.d("APIServsice", "onResponse");
                Assert.assertNotNull(response);
            }

            @Override
            public void onFailure(Throwable t,int errorCode) {
                Log.d("APIService", "onFailure");

            }
        });

        APIService.getInstance().execute(deviceRegistration);
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }
    }*/
    }
}
