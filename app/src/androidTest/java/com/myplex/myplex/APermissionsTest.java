package com.myplex.myplex;


import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;



import com.myplex.myplex.ui.activities.LoginActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Created by Apalya on 3/28/2019.
 */
@RunWith(AndroidJUnit4.class)
public class APermissionsTest {


    private UiDevice mDevice;

    @Rule
    public ActivityTestRule<LoginActivity> rule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void setUp()  {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void runTimePermissions() {
        allowPermissionsIfNeeded();
    }


    private void allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = mDevice.findObject(
                    new UiSelector().className("android.widget.Button")
                            .resourceId("com.android.packageinstaller:id/permission_allow_button"));
            // get allow_button Button by id , because on another device languages it is not "Allow"
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                    allowPermissionsIfNeeded();//allow second Permission
                } catch (UiObjectNotFoundException e) {
                }
            }
        }
    }
}


