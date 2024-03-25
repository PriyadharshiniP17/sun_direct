package com.myplex.myplex;


import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;


import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.FragmentOTPVerification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class BOtppageTest {


    private MainActivity mActivity;


    @Rule
    //Activity Test Rule for the activity which contains my login fragment
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void init() {
        mActivityRule.getActivity().getSupportFragmentManager().beginTransaction().add(R.id.main_content_container, new FragmentOTPVerification()).commit();
    }

    @Test
    public void OnNonsundirectNumberTest() throws InterruptedException {

        Thread.sleep(2000);

        onView(withId(R.id.otp_mobile_no)).perform(typeText("88979 94633"), click());
        onView(allOf(withId(R.id.otp_btn_1), withText("Continue"), isDescendantOfA(withId(R.id.otp_layout_btns)))).perform(click());
        Thread.sleep(3000);
        onView(withText("OK")).check(matches(isDisplayed())).perform(click());
        Thread.sleep(3000);


        onView(withId(R.id.otp_mobile_no)).perform(replaceText("8886003303"), click());

        onView(allOf(withId(R.id.otp_btn_1), withText("Continue"), isDescendantOfA(withId(R.id.otp_layout_btns)))).perform(click());
        Thread.sleep(15000);
        onView(withId(R.id.otp_text_enter_otp)).perform(typeText("899999"), click());
        Thread.sleep(3000);
        onView(withText("Go")).check(matches(isDisplayed())).perform(click());
        Thread.sleep(3000);
        onView(withText("OK")).check(matches(isDisplayed())).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.otp_text_enter_otp)).perform(replaceText("123456"), click());
        Thread.sleep(3000);
        onView(withText("Go")).check(matches(isDisplayed())).perform(click());

        Thread.sleep(5000);



      /*  onView(withId(R.id.media_player_fowrard_icon)).perform(click());
        Thread.sleep(2000);*/





     /*   onView(withId(R.id.otp_text_enter_otp)).perform(typeText("123456"), click());
        onView(withText("Go")).check(matches(isDisplayed())).perform(click());
        Thread.sleep(50000);*/

    }
 /*   @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();

    }*/
  /*  @Test
    public void viewPageTest() throws InterruptedException {

        onView(withId(R.id.viewpager)).perform(pagerSwipeRight(), click());
        Thread.sleep(9000);
    }*/

    private GeneralSwipeAction pagerSwipeRight() {
        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER, Press.FINGER);
    }
}