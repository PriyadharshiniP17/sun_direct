package com.myplex.myplex;

import android.app.Instrumentation;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.myplex.myplex.ui.activities.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static android.os.SystemClock.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


/**
 * Created by Apalya on 4/1/2019.
 */
@RunWith(JUnit4.class)
public class EScreenTest {


    private MainActivity mActivity;


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
    }

    @Test
    public void testSwipeOverVerticalPager() throws InterruptedException {
        Thread.sleep(12000);
        swiper(1000, 100, 0);
        Thread.sleep(3000);
        swiper(100, 1000, 0);
        Thread.sleep(6000);

        onView(withId(R.id.tabs)).perform(swipeLeft()).perform(swipeLeft()).perform(swipeLeft()).perform(swipeLeft());
        Thread.sleep(3000);

        onView(withId(R.id.drawerLayout)).perform((ViewAction) DrawerActions.open());
        onView(withId(R.id.drawerLayout)).check(matches(isOpen()));
        Thread.sleep(2000);

        onView(withId(R.id.recycleviewDrawerList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));
        Thread.sleep(2000);


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

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

}


