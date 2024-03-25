package com.myplex.myplex;

import android.app.Instrumentation;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import android.view.MotionEvent;
import com.myplex.myplex.ui.activities.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


import static android.os.SystemClock.sleep;
import static android.os.SystemClock.uptimeMillis;
import static androidx.test.espresso.Espresso.onView;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.core.AllOf.allOf;


/**
 * Created by Apalya on 4/1/2019.
 */

public class DNavigationTest {
    private MainActivity mActivity;


    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mActivity = mActivityTestRule.getActivity();

    }

    @Test
    public void controllersTest() throws InterruptedException {

     /*   onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.close());
        onView(withId(R.id.drawerLayout)).check(matches(isClosed()));
        Thread.sleep(3000);

        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        swiper(1000, 100, 0);
        Thread.sleep(3000);
        swiper(100, 1000, 0);
        Thread.sleep(3000);


        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.close());
        onView(withId(R.id.drawerLayout)).check(matches(isClosed()));
        Thread.sleep(3000);

        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(7, click()));
        Thread.sleep(3000);


        swiper(1000, 100, 0);
        Thread.sleep(3000);
        swiper(100, 1000, 0);
        Thread.sleep(6000);

        UiDevice languages = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        languages.pressBack();
        Thread.sleep(2000);


        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(8, click()));
        Thread.sleep(3000);


        swiper(1000, 100, 0);
        Thread.sleep(3000);
        swiper(100, 1000, 0);
        Thread.sleep(3000);

        UiDevice genres = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        genres.pressBack();
        Thread.sleep(2000);


        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(9, click()));
        Thread.sleep(3000);

        onView(withId(R.id.view_pager_my_downloads)).perform(swipeLeft()).perform(swipeLeft()).perform(swipeLeft());

        Thread.sleep(3000);

        onView(withId(R.id.view_pager_my_downloads)).perform(swipeRight()).perform(swipeRight()).perform(swipeRight());

        Thread.sleep(3000);

        UiDevice myWatchList = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        myWatchList.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(10, click()));
        Thread.sleep(3000);

        onView(withId(R.id.view_pager_my_downloads)).perform(swipeLeft()).perform(swipeLeft()).perform(swipeLeft());

        Thread.sleep(3000);

        onView(withId(R.id.view_pager_my_downloads)).perform(swipeRight()).perform(swipeRight()).perform(swipeRight());

        Thread.sleep(3000);

        UiDevice myDownloads = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        myDownloads.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(11, click()));
        Thread.sleep(5000);

        swiper(1000, 100, 0);
        Thread.sleep(3000);
        swiper(100, 1000, 0);
        Thread.sleep(6000);

        UiDevice moreApps = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        moreApps.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(3000);
        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(12, click()));
        Thread.sleep(3000);

        onView(withId(R.id.videoQuality)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.radio_button_quality_hd)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.videoQuality)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.radio_button_quality_sd)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.videoQuality)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.radio_button_quality_low)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.videoQuality)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.radio_button_quality_auto)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.checkbox_notification_toggle)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.checkbox_notification_toggle)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.checkbox_auto_play_toggle)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.checkbox_auto_play_toggle)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.checkbox_download_on_wifi_toggle)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.checkbox_download_on_wifi_toggle)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.parentControl)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.radio_button_parental_all)).perform(click());
        Thread.sleep(3000);
        ;

        onView(withId(R.id.txt_pin_entry)).perform(typeText("5555"));
        onView(withId(R.id.negative_button)).perform(click());

        onView(withId(R.id.parentControl)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.radio_button_parental_all)).perform(click());
        Thread.sleep(2000);
        ;

        onView(withId(R.id.txt_pin_entry)).perform(typeText("5555"));
        onView(withId(R.id.positive_button)).perform(click());
        Thread.sleep(2000);
        ;


        onView(withId(R.id.txt_pin_entry)).perform(typeText("5555"));
        onView(withId(R.id.positive_button)).perform(click());
        Thread.sleep(2000);
        ;


        onView(withId(R.id.parentControl)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.radio_button_parental_adult)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.txt_pin_entry)).perform(typeText("5555"));
        onView(withId(R.id.positive_button)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.parentControl)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.radio_button_parental_off)).perform(click());
        Thread.sleep(2000);
        ;


        onView(withId(R.id.txt_pin_entry)).perform(typeText("5555"));
        onView(withId(R.id.positive_button)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.text_about)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.text_about_app)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.about_ok_txt)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.text_tnc)).perform(click());

        Thread.sleep(2000);

        Thread.sleep(6000);
        swiper(1000, 100, 0);
        Thread.sleep(3000);
        swiper(100, 1000, 0);
        Thread.sleep(2000);

        onView(withId(R.id.toolbar_settings_button)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.text_privacy_policy)).perform(click());

        Thread.sleep(3000);
        swiper(1000, 100, 0);
        swiper(1000, 100, 0);
        swiper(1000, 100, 0);


        Thread.sleep(3000);
        swiper(100, 1000, 0);
        swiper(100, 1000, 0);
        swiper(100, 1000, 0);


        Thread.sleep(2000);


        onView(withId(R.id.toolbar_settings_button)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.toolbar_settings_button)).perform(click());
        Thread.sleep(2000);

        UiDevice policy = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        policy.pressBack();
        Thread.sleep(2000);


        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(2000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(13, click()));
        Thread.sleep(2000);
        onView(withId(R.id.text_faq)).perform(click());

        Thread.sleep(2000);
        swiper(1000, 100, 0);
        swiper(1000, 100, 0);
        swiper(1000, 100, 0);
        swiper(1000, 100, 0);


        swiper(100, 1000, 0);
        swiper(100, 1000, 0);
        swiper(100, 1000, 0);
        swiper(100, 1000, 0);


        Thread.sleep(2000);

        onView(withId(R.id.toolbar_settings_button)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.text_share_app)).perform(click());
        Thread.sleep(2000);


        UiDevice shareApp = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        shareApp.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.text_rate_app)).perform(click());
        Thread.sleep(2000);

        UiDevice rateApp = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        rateApp.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.text_feedback)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.feedback_ratingbar)).perform(swipeRight());
        Thread.sleep(2000);

        onView(withId(R.id.feedback_ok_button)).perform(click());
        Thread.sleep(2000);


        onView(withId(R.id.text_cancel_subscription)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.toolbar_settings_button)).perform(click());
        Thread.sleep(2000);

        UiDevice filterLanguages = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        filterLanguages.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(2000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));
        Thread.sleep(2000);


        onView(withId(R.id.action_filter)).perform(click());
        Thread.sleep(2000);

        onView(withText("Telugu")).perform(click());
        Thread.sleep(1000);
        onView(withText("Hindi")).perform(click());
        Thread.sleep(1000);
        onView(withText("Kannada")).perform(click());
        Thread.sleep(1000);
        onView(withText("Marathi")).perform(click());
        Thread.sleep(1000);
        onView(withText("English")).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.tvReset)).perform(click());
        Thread.sleep(2000);

        onView(withText("Kannada")).perform(click());
        Thread.sleep(1000);
        onView(withText("Marathi")).perform(click());
        Thread.sleep(1000);
        onView(withText("English")).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.btApply)).perform(click());
        Thread.sleep(2000);

        UiDevice subscription = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        subscription.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.action_filter)).perform((click()));
        Thread.sleep(3000);

        onView(withId(R.id.pager)).perform(swipeLeft());
        Thread.sleep(3000);


        onView(withText("Romance")).perform(click());
        Thread.sleep(3000);

        onView(withText("Drama")).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.tvReset)).perform(click());
        Thread.sleep(2000);


        onView(withText("Romance")).perform(click());
        Thread.sleep(3000);

        onView(withText("Drama")).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.btApply)).perform(click());
        Thread.sleep(2000);

        UiDevice genresBack = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        genresBack.pressBack();
        Thread.sleep(2000);

        onView(withId(R.id.search_button)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.search_src_text)).perform(typeText("abc"));
        Thread.sleep(3000);


        UiDevice search = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        search.pressBack();
        Thread.sleep(2000);
*/
        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(2000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        Thread.sleep(2000);


        onView(withId(R.id.viewpager))
                // .perform(swipeLeft())
                .check(matches(hasDescendant(withText("More")))).perform(click());

        Thread.sleep(9000);


        UiDevice exoTest = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        exoTest.pressBack();
        Thread.sleep(9000);

        UiDevice exoTestHome = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        exoTestHome.pressBack();
        Thread.sleep(9000);


    }


    static void swiper(int start, int end, int delay) {
        long downTime = uptimeMillis();
        long eventTime = uptimeMillis();
        Instrumentation inst = getInstrumentation();

        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 500, start, 0);
        inst.sendPointerSync(event);
        eventTime = uptimeMillis() + delay;
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 500, end, 0);
        inst.sendPointerSync(event);
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 500, end, 0);
        inst.sendPointerSync(event);
        sleep(2000);
    }


}