package com.myplex.myplex;

import android.app.Instrumentation;
import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.myplex.ui.activities.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.os.SystemClock.sleep;
import static android.os.SystemClock.uptimeMillis;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * Created by Apalya on 4/1/2019.
 */
@RunWith(AndroidJUnit4.class)

public class CMediaControllersTest {




    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);




    @Test
    public void swipePage() throws InterruptedException {


        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(2000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        Thread.sleep(2000);


        onView(withId(R.id.viewpager))
                // .perform(swipeLeft())
                .check(matches(hasDescendant(withText("More")))).perform(click());

        Thread.sleep(9000);

        onView(withId(R.id.media_player_fowrard_icon)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.media_player_play_pause_icon)).perform(click());
        Thread.sleep(2000);


        onView(withId(R.id.media_player_play_pause_icon)).perform(click());
        Thread.sleep(2000);


        onView(withId(R.id.media_player_rewind_icon)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.carddetailbriefdescription_download_img)).perform(click());
        Thread.sleep(4000);

        UiDevice downLoad = UiDevice.getInstance(getInstrumentation());
        downLoad.pressBack();
        Thread.sleep(4000);


        onView(withId(R.id.carddetailbriefdescription_favourite_img)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(4000);

        onView(withId(R.id.carddetailbriefdescription_favourite_img)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(4000);

        onView(withId(R.id.carddetailbriefdescription_share_img)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(4000);

        UiDevice share = UiDevice.getInstance(getInstrumentation());
        share.pressBack();
        Thread.sleep(4000);

        onView(withId(R.id.media_player_rewind_icon)).perform(click());
        Thread.sleep(2000);


        onView(withId(R.id.mediacontroller_progress)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.playerfullscreenimage)).perform(doubleClick());
        Thread.sleep(5000);

        onView(withId(R.id.media_player_fowrard_icon)).perform(click());
        Thread.sleep(4000);

        onView(withId(R.id.media_player_play_pause_icon)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.media_player_play_pause_icon)).perform(click());
        Thread.sleep(2000);


        onView(withId(R.id.media_player_fowrard_icon)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.media_player_rewind_icon)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.media_player_rewind_icon)).perform(click());
        Thread.sleep(4000);
        onView(withId(R.id.playerquality)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(3000);

        onView(withText("High")).check(matches(isDisplayed())).perform(doubleClick());
        Thread.sleep(8000);

      /*  onView(withId(R.id.media_player_play_pause_icon)).perform(click());
        Thread.sleep(6000);*/

        onView(withId(R.id.playerquality)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(3000);

        onView(withText("Medium")).check(matches(isDisplayed())).perform(doubleClick());
        Thread.sleep(2000);

        onView(withId(R.id.playerquality)).check(matches(isDisplayed())).perform(click());
        Thread.sleep(3000);

        onView(withText("Auto")).check(matches(isDisplayed())).perform(doubleClick());
        Thread.sleep(4000);


        gestureBrightNessTest(1000, 100, 0);
        Thread.sleep(6000);

        gestureBrightNessTest(100, 1000, 0);
        Thread.sleep(9000);

        gestureBrightNessTest(1000, 100, 0);
        Thread.sleep(6000);

        gestureVolumeTest(1000, 100, 0);
        Thread.sleep(3000);

        gestureVolumeTest(100, 1000, 0);
        Thread.sleep(3000);


    }

    private GeneralSwipeAction pagerSwipeRight() {
        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER, Press.FINGER);
    }

    public static ViewAction swipeUpBright() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_LEFT,
                GeneralLocation.BOTTOM_RIGHT, Press.FINGER);
    }


    static void gestureVolumeTest(int start, int end, int delay) {
        long downTime = uptimeMillis();
        long eventTime = uptimeMillis();
        Instrumentation inst = getInstrumentation();

        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 2000, start, 0);
        inst.sendPointerSync(event);
        eventTime = uptimeMillis() + delay;
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 2000, end, 0);
        inst.sendPointerSync(event);
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 2000, end, 0);
        inst.sendPointerSync(event);
        sleep(2000);
    }

    static void gestureBrightNessTest(int start, int end, int delay) {
        long downTime = uptimeMillis();
        long eventTime = uptimeMillis();
        Instrumentation inst = getInstrumentation();

        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 500, start, 0);
        inst.sendPointerSync(event);
        eventTime = uptimeMillis() + delay;
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 200, end, 0);
        inst.sendPointerSync(event);
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 200, end, 0);
        inst.sendPointerSync(event);
        sleep(4000);
    }

    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("position " + childPosition + " of parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) return false;
                ViewGroup parent = (ViewGroup) view.getParent();

                return parentMatcher.matches(parent)
                        && parent.getChildCount() > childPosition
                        && parent.getChildAt(childPosition).equals(view);
            }
        };
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    public static Matcher<View> atPositionOnView(final int position, final Matcher<View> itemMatcher,
                                                 @NonNull final int targetViewId) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has view id " + itemMatcher + " at position " + position);
            }

            @Override
            public boolean matchesSafely(final RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                View targetView = viewHolder.itemView.findViewById(targetViewId);
                return itemMatcher.matches(targetView);
            }
        };
    }
}

