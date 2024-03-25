package com.myplex.myplex.animationviewpager.animation;

import android.view.View;

import com.myplex.myplex.animationviewpager.ViewPagerEx;

/**
 * A demo class to show how to use {@link BaseAnimationInterface}
 * to make  your custom animation in {@link ViewPagerEx.PageTransformer} action.
 */
public class DescriptionAnimation implements BaseAnimationInterface {

    @Override
    public void onPrepareCurrentItemLeaveScreen(View current) {
    }

    /**
     * When next item is coming to show, let's hide the description layout.
     * @param next
     */
    @Override
    public void onPrepareNextItemShowInScreen(View next) {
    }


    @Override
    public void onCurrentItemDisappear(View view) {

    }

    /**
     * When next item show in ViewPagerEx, let's make an animation to show the
     * description layout.
     * @param view
     */
    @Override
    public void onNextItemAppear(View view) {

    }

}
