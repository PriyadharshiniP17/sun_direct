/*
 * Copyright (C) 2014 Pedro Vicente Gómez Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pedrovgs;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.customview.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Custom view created to handle DraggableView using fragments. With this custom view the client
 * code can configure the
 * top and bottom fragment and other elements like: top fragment height, top fragment margin right,
 * top fragment x
 * scale factor, top fragment y scale factor, top fragment margin bottom and enable or disable
 * horizontal alpha effect.
 *
 */
public class DraggablePanel extends FrameLayout {

    private static final int DEFAULT_TOP_FRAGMENT_HEIGHT = 200;
    private static final int DEFAULT_TOP_FRAGMENT_MARGIN = 0;
    private static final float DEFAULT_SCALE_FACTOR = 2;
    private static final boolean DEFAULT_ENABLE_HORIZONTAL_ALPHA_EFFECT = true;
    private static final boolean DEFAULT_ENABLE_CLICK_TO_MAXIMIZE = false;
    private static final boolean DEFAULT_ENABLE_CLICK_TO_MINIMIZE = false;
    private static final boolean DEFAULT_ENABLE_TOUCH_LISTENER = true;
    private static final boolean DEFAULT_TOP_FRAGMENT_RESIZE = false;

    public static final int ON_DRAG_START = 10;
    public static final int ON_DRAG_STOPPED = 11;

    public DraggableView draggableView;
    private DraggableListener draggableListener;

    private FragmentManager fragmentManager;
    private Fragment topFragment;
    private Fragment bottomFragment;
    private int topFragmentHeight;
    public int topFragmentWidth;
    private int bottomFragmentHeight;
    private int topFragmentMarginRight;
    public int topFragmentMarginBottom;
    private float xScaleFactor;
    private float yScaleFactor;
    private boolean enableHorizontalAlphaEffect;
    private boolean enableClickToMaximize;
    private boolean enableClickToMinimize;
    private boolean enableTouchListener;
    private boolean isFullScreen;
    private int screenWidth;
    private View mDraggablePanelView;

    public void setOnVisibilityChangedListener(OnVisibilityChanged onVisibilityChangedListener) {
        this.onVisibilityChangedListener = onVisibilityChangedListener;
    }

    private OnVisibilityChanged onVisibilityChangedListener;

    public DraggablePanel(Context context) {
        super(context);
    }

    public DraggablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttrs(attrs);
    }

    public DraggablePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeAttrs(attrs);
    }

    /**
     * Configure the FragmentManager used to attach top and bottom fragment inside the view.
     */
    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * Configure the Fragment that will work as draggable element inside this custom view. This
     * Fragment has to be configured before initialize the view.
     *
     * @param topFragment used as draggable element.
     */
    public void setTopFragment(Fragment topFragment) {
        this.topFragment = topFragment;
    }

    /**
     * Configure the Fragment that will work as secondary element inside this custom view. This
     * Fragment has to be configured before initialize the view.
     *
     * @param bottomFragment used as secondary element.
     */
    public void setBottomFragment(Fragment bottomFragment) {
        this.bottomFragment = bottomFragment;
    }

    /**
     * Configure the height associated to the top Fragment used inside the view as draggable element.
     *
     * @param topFragmentHeight in pixels.
     */
    public void setTopViewHeight(int topFragmentHeight) {
        this.topFragmentHeight = topFragmentHeight;
        if (draggableView != null) {
            draggableView.setTopViewHeight(topFragmentHeight);
        }
        LoggerD.debugLog("MiniPlayerTab: topFragmentHeight- " + topFragmentHeight);

    }

    public void setTopFragmentViewWidth(int topViewWidth) {
        this.topFragmentWidth = topViewWidth;
    }

    public void setBottomFragmentHeight(int bottomFragmentHeight) {
        this.bottomFragmentHeight = bottomFragmentHeight;
        if (draggableView != null) {
            draggableView.setBottomViewHeight(bottomFragmentHeight);
        }
    }

    public void setTopViewWidth(int widthInPortraitMode) {
        if (draggableView != null) {
            draggableView.setTopViewWidth(widthInPortraitMode);
        }
        LoggerD.debugLog("MiniPlayerTab: widthInPortraitMode- " + widthInPortraitMode);
    }

    public void ensureTopViewWidth() {
        if (draggableView != null) {
            draggableView.ensureTopViewWidth();
        }
    }

    /**
     * Return if user can maximize minimized view on click.
     */
    public boolean isClickToMaximizeEnabled() {
        return enableClickToMaximize;
    }

    /**
     * Enable or disable click to maximize view when dragged view is minimized
     * If your content have a touch/click listener (like YoutubePlayer), you
     * need disable it to active this feature.
     *
     * @param enableClickToMaximize to enable or disable the click.
     */
    public void setClickToMaximizeEnabled(boolean enableClickToMaximize) {
        if(draggableView != null){
            draggableView.setClickToMaximizeEnabled(enableClickToMaximize);
        }
        this.enableClickToMaximize = enableClickToMaximize;
    }

    /**
     * Return if user can minimize maximized view on click.
     */
    public boolean isClickToMinimizeEnabled() {
        return enableClickToMinimize;
    }

    /**
     * Enable or disable click to minimize view when dragged view is maximized
     * If your content have a touch/click listener (like YoutubePlayer), you
     * need disable it to active this feature.
     *
     * @param enableClickToMinimize to enable or disable the click.
     */
    public void setClickToMinimizeEnabled(boolean enableClickToMinimize) {
        this.enableClickToMinimize = enableClickToMinimize;
    }

    /**
     *
     * Slide the view based on scroll of the nav drawer.
     * "setEnableTouch" user prevents click to expand while the drawer is moving.
     * It's only possible to maximize the view when @slideOffset is equals to 0.0,
     * in other words, closed.
     *
     * @param slideOffset Value between 0 and 1, represent the value of slide:
     * 0.0 is equal to close drawer and 1.0 equals open drawer.
     * @param drawerPosition Represent the position of nav drawer on X axis.
     * @param width Width of nav drawer
     */
    public void slideHorizontally(float slideOffset, float drawerPosition, int width) {
        draggableView.slideHorizontally(slideOffset, drawerPosition, width);
    }

    /**
     * Configure the horizontal scale factor applied when the top fragment is dragged to the bottom
     * of the custom view.
     */
    public void setXScaleFactor(float xScaleFactor) {
        this.xScaleFactor = xScaleFactor;
    }

    /**
     * Configure the vertical scale factor applied when the top fragment is dragged to the bottom of
     * the custom view.
     */
    public void setYScaleFactor(float yScaleFactor) {
        this.yScaleFactor = yScaleFactor;
    }

    /**
     * Configure the top Fragment margin right applied when the view has been minimized.
     *
     * @param topFragmentMarginRight in pixels.
     */
    public void setTopFragmentMarginRight(int topFragmentMarginRight) {
        this.topFragmentMarginRight = topFragmentMarginRight;
    }

    /**
     * Configure the top Fragment margin bottom applied when the view has been minimized.
     *
     * @param topFragmentMarginBottom in pixels.
     */
    public void setTopFragmentMarginBottom(int topFragmentMarginBottom) {
        this.topFragmentMarginBottom = topFragmentMarginBottom;
        if (mDraggablePanelView != null) {
            draggableView.setTopViewMarginBottom(topFragmentMarginBottom);
        }
    }

    /**
     * Configure the DraggableListener that is going to be invoked when the view be minimized,
     * maximized, closed to the left or right.
     */
    public void setDraggableListener(DraggableListener draggableListener) {
        this.draggableListener = draggableListener;
    }

    /**
     * Configure the disabling of the alpha effect applied when the view is being dragged
     * horizontally.
     *
     * @param enableHorizontalAlphaEffect to enable or disable the effect.
     */
    public void setEnableHorizontalAlphaEffect(boolean enableHorizontalAlphaEffect) {
        this.enableHorizontalAlphaEffect = enableHorizontalAlphaEffect;
    }

    /**
     * Configure the top Fragment to resize instead of scale it.
     */
    public void setTopFragmentResize(boolean topViewResize) {
        if (draggableView == null) {
            return;
        }
        draggableView.setTopViewResize(topViewResize);
    }

    /**
     * Close the custom view applying an animation to close the view to the left side of the screen.
     */
    public void closeToLeft() {
        if (draggableView == null) {
            return;
        }
        draggableView.closeToLeft();
    }

    /**
     * Close the custom view applying an animation to close the view to the right side of the screen.
     */
    public void closeToRight() {
        if (draggableView == null) {
            return;
        }
        draggableView.closeToRight();
    }

    /**
     * Maximize the custom view applying an animation to return the view to the initial position.
     */
    public void maximize() {
        if (draggableView == null) {
            return;
        }
        draggableView.maximize();
    }

    /**
     * Minimize the custom view applying an animation to put the top fragment on the bottom right
     * corner of the screen.
     */
    public void minimize() {
        if (draggableView == null) {
            return;
        }
        draggableView.minimize();
    }

    /**
     * Apply all the custom view configuration and inflate the main widgets. The view won't be
     * visible if this method is not called.
     * <p/>
     * FragmentManager, top Fragment and bottom Fragment have to be configured before initialize this
     * view. If not, this method will throw and IllegalStateException.
     */
    public void initializeView() {
        LoggerD.debugLog("initializeView");
        checkFragmentConsistency();
        checkSupportFragmentManagerConsistency();
        if (mDraggablePanelView == null)
            mDraggablePanelView = LayoutInflater.from(getContext()).inflate(R.layout.draggable_panel, null);
        if (indexOfChild(mDraggablePanelView) == -1)
            addView(mDraggablePanelView);
        draggableView = (DraggableView) findViewById(R.id.draggable_view);
        draggableView.setTopViewHeight(topFragmentHeight);
        draggableView.setTopViewWidth(topFragmentWidth);
        draggableView.setFragmentManager(fragmentManager);
        draggableView.attachTopFragment(topFragment);
        draggableView.setXTopViewScaleFactor(xScaleFactor);
        draggableView.setYTopViewScaleFactor(yScaleFactor);
        draggableView.setTopViewMarginRight(topFragmentMarginRight);
        draggableView.setTopViewMarginBottom(topFragmentMarginBottom);
        draggableView.attachBottomFragment(bottomFragment);
        draggableView.setBottomViewHeight(bottomFragmentHeight);
        draggableView.setDraggableListener(draggableListener);
        draggableView.setHorizontalAlphaEffectEnabled(enableHorizontalAlphaEffect);
        draggableView.setClickToMaximizeEnabled(enableClickToMaximize);
        draggableView.setClickToMinimizeEnabled(enableClickToMinimize);
        draggableView.setTouchEnabled(enableTouchListener);
    }

    /**
     * Checks if the top Fragment is maximized.
     *
     * @return true if the view is maximized.
     */
    public boolean isMaximized() {
        if (draggableView == null) {
            return false;
        }
        return draggableView.isMaximized();
    }

    /**
     * Checks if the top Fragment is minimized.
     *
     * @return true if the view is minimized.
     */
    public boolean isMinimized() {
        if (draggableView == null) {
            return false;
        }
        return draggableView.isMinimized();
    }

    /**
     * Checks if the top Fragment closed at the right place.
     *
     * @return true if the view is closed at right.
     */
    public boolean isClosedAtRight() {
        if (draggableView == null) {
            return false;
        }
        return draggableView.isClosedAtRight();
    }

    /**
     * Checks if the top Fragment is closed at the left place.
     *
     * @return true if the view is closed at left.
     */
    public boolean isClosedAtLeft() {
        if (draggableView == null) {
            return false;
        }
        return draggableView.isClosedAtLeft();
    }

    /**
     * Initialize the xml configuration based on styleable attributes
     *
     * @param attrs to analyze.
     */
    private void initializeAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.draggable_panel);
        this.topFragmentHeight =
                attributes.getDimensionPixelSize(R.styleable.draggable_panel_top_fragment_height,
                        DEFAULT_TOP_FRAGMENT_HEIGHT);
        this.xScaleFactor =
                attributes.getFloat(R.styleable.draggable_panel_x_scale_factor, DEFAULT_SCALE_FACTOR);
        this.yScaleFactor =
                attributes.getFloat(R.styleable.draggable_panel_y_scale_factor, DEFAULT_SCALE_FACTOR);
        this.topFragmentMarginRight =
                attributes.getDimensionPixelSize(R.styleable.draggable_panel_top_fragment_margin_right,
                        DEFAULT_TOP_FRAGMENT_MARGIN);
        this.topFragmentMarginBottom =
                attributes.getDimensionPixelSize(R.styleable.draggable_panel_top_fragment_margin_bottom,
                        DEFAULT_TOP_FRAGMENT_MARGIN);
        this.enableHorizontalAlphaEffect =
                attributes.getBoolean(R.styleable.draggable_panel_enable_horizontal_alpha_effect,
                        DEFAULT_ENABLE_HORIZONTAL_ALPHA_EFFECT);
        this.enableClickToMaximize =
                attributes.getBoolean(R.styleable.draggable_panel_enable_click_to_maximize_panel,
                        DEFAULT_ENABLE_CLICK_TO_MAXIMIZE);
        this.enableClickToMinimize =
                attributes.getBoolean(R.styleable.draggable_panel_enable_click_to_minimize_panel,
                        DEFAULT_ENABLE_CLICK_TO_MINIMIZE);
        this.enableTouchListener =
                attributes.getBoolean(R.styleable.draggable_panel_enable_touch_listener_panel,
                        DEFAULT_ENABLE_TOUCH_LISTENER);
        attributes.recycle();
    }

    /**
     * Validate FragmentManager configuration. If is not initialized, this method will throw an
     * IllegalStateException.
     */
    private void checkSupportFragmentManagerConsistency() {
        if (fragmentManager == null) {
            throw new IllegalStateException(
                    "You have to set the support FragmentManager before initialize DraggablePanel");
        }
    }

    /**
     * Validate top and bottom Fragment configuration. If are not initialized, this method will throw
     * an IllegalStateException.
     */
    private void checkFragmentConsistency() {
        if (topFragment == null || bottomFragment == null) {
            throw new IllegalStateException(
                    "You have to set top and bottom fragment before initialize DraggablePanel");
        }
    }

    public void setDisableDraggableViewOnTouch(boolean disableDraggableView) {
        if (draggableView != null) {
            draggableView.setDisableDraggableViewOnTouch(disableDraggableView);
        }
    }

    public void setDraggableViewEnabled(boolean draggableViewEnabled) {
        if (draggableView != null) {
            draggableView.setEnabled(draggableViewEnabled);
        }
    }

    public void setDraggableViewCallbackListener(ViewDragHelper.Callback draggableCallbackListener){
        if(draggableView != null){
            draggableView.setDraggableViewCallbackListener(draggableCallbackListener);
        }
    }

    public boolean isDraggedMinimumRange() {
        return draggableView != null ? draggableView.isDraggedMinimumRange() : false;
    }

    public void forceLayoutUpdate() {
        LoggerD.debugLog("forceLayoutUpdate:");
        if (draggableView != null) {
            draggableView.forceLayoutUpdate();
        }
    }

    public void removeLayoutUpdates() {
        LoggerD.debugLog("forceLayoutUpdate:");
        if (draggableView != null) {
            draggableView.removeLayoutUpdates();
        }
    }

    public void setFullScreen(boolean fullScreen) {
        this.isFullScreen = fullScreen;
        if(draggableView != null){
            draggableView.setFullScreen(fullScreen);
        }
    }

    public void setScreenWidth(int screenWidth){
        this.screenWidth = screenWidth;
        if(draggableView != null){
            draggableView.setScreenWidth(screenWidth);
        }
        setTopViewHeight((screenWidth * 9)/ 16);
    }

    public void slideToMiddle() {
        if(draggableView == null){
            return;
        }
        draggableView.slideToMiddle();
    }

    public interface OnVisibilityChanged {
        void onDraggablePanelVisibilityChanged(boolean isVisible);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (onVisibilityChangedListener != null) {
            onVisibilityChangedListener.onDraggablePanelVisibilityChanged(visibility == VISIBLE);
        }
    }


    public void setPlayerSeekbarView(View mPlayerSeekbarView) {
        if(draggableView != null){
            draggableView.setPlayerSeekbarView(mPlayerSeekbarView);
        }
    }

    public boolean isDragViewHit(int x, int y) {
        if (draggableView == null) {
            return false;
        }
        return draggableView.isViewHit(draggableView, x, y);
    }

    public boolean isViewAtTop(){
        if(draggableView != null){
            return draggableView.isViewAtTop();
        }
        return false;
    }
}
