/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package viewpagerindicator;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.myplex.myplex.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/**
 * Draws circles (one for each view). The current view position is filled and
 * others are only stroked.
 *
 * Thanks to : https://github.com/JakeWharton/Android-ViewPagerIndicator
 */
public class CircleIndicator extends View implements PageIndicator {
    private static final int INVALID_POINTER = -1;

    private float mRadius;
    private final Paint mPaintPageFill = new Paint(ANTI_ALIAS_FLAG);
    private final Paint mPaintCurrentPageStroke = new Paint(ANTI_ALIAS_FLAG);
    private final Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
    private final Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
    private ViewPager mViewPager;
    private int mCurrentPage;
    private int mSnapPage;
    private int mScrollState;
    private int mOrientation;
    private boolean mCentered;
    private boolean mSnap = true;

    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;
    private int mPageCount;


    public CircleIndicator(Context context) {
        this(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.vpiCirclePageIndicatorStyle);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;

        //Load defaults from resources
        final Resources res = getResources();
        final int defaultPageColor = res.getColor(R.color.vf_default_circle_indicator_page_color);
        final int defaultFillColor = res.getColor(R.color.vf_default_circle_indicator_fill_color);
        final int defaultOrientation = res.getInteger(R.integer.vf_default_circle_indicator_orientation);
        final int defaultStrokeColor = res.getColor(R.color.vf_default_circle_indicator_stroke_color);
        final float defaultStrokeWidth = res.getDimension(R.dimen.vf_default_circle_indicator_stroke_width);
        final float defaultRadius = res.getDimension(R.dimen.vf_default_circle_indicator_radius);
        final boolean defaultCentered = res.getBoolean(R.bool.vf_default_circle_indicator_centered);
        final boolean defaultSnap = res.getBoolean(R.bool.vf_default_circle_indicator_snap);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, 0);

        mCentered = defaultCentered;
        mOrientation = a.getInt(R.styleable.CirclePageIndicator_android_orientation, defaultOrientation);
        mPaintPageFill.setStyle(Style.FILL);
        mPaintPageFill.setColor(a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor));
        mPaintStroke.setStyle(Style.STROKE);
        mPaintStroke.setColor(a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor));
        mPaintStroke.setStrokeWidth(a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth));
        mPaintCurrentPageStroke.setStyle(Style.STROKE);
        mPaintCurrentPageStroke.setColor(a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultFillColor));
        mPaintCurrentPageStroke.setStrokeWidth(a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth));
        mPaintFill.setStyle(Style.FILL);
        mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor));
        mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius);

        Drawable background = a.getDrawable(R.styleable.CirclePageIndicator_android_background);
        if (background != null) {
          setBackgroundDrawable(background);
        }

        a.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    public void setCentered(boolean centered) {
        mCentered = centered;
        invalidate();
    }

    public boolean isCentered() {
        return mCentered;
    }

    public void setPageColor(int pageColor) {
        mPaintPageFill.setColor(pageColor);
        invalidate();
    }

    public int getPageColor() {
        return mPaintPageFill.getColor();
    }

    public void setFillColor(int fillColor) {
        mPaintFill.setColor(fillColor);
        invalidate();
    }

    public int getFillColor() {
        return mPaintFill.getColor();
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case HORIZONTAL:
            case VERTICAL:
                mOrientation = orientation;
                requestLayout();
                break;

            default:
                throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setStrokeColor(int strokeColor) {
        mPaintStroke.setColor(strokeColor);
        invalidate();
    }

    public int getStrokeColor() {
        return mPaintStroke.getColor();
    }

    public void setStrokeWidth(float strokeWidth) {
        mPaintStroke.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public float getStrokeWidth() {
        return mPaintStroke.getStrokeWidth();
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setSnap(boolean snap) {
        mSnap = snap;
        invalidate();
    }

    public boolean isSnap() {
        return mSnap;
    }

    /**
     * Bind the indicator to a ViewPager.
     *
     * @param viewPager
     * @param initialPosition
     */
    @Override
    public void setViewPager(ViewPager viewPager, int initialPosition) {
        mViewPager = viewPager;
        mCurrentPage = initialPosition;
        mViewPager.addOnPageChangeListener(this);
        invalidate();
    }

    /**
     * Bind the indicator to a ViewPager.
     *
     * @param pagesRealCount
     */

    public void setPageCount(int pagesRealCount) {
        this.mPageCount = pagesRealCount;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null)
            return;

        final int count = getRealCount();
        if (count < 2) {
            return;
        }

        int longSize;
        int longPaddingBefore;
        int longPaddingAfter;
        int shortPaddingBefore;
        if (mOrientation == HORIZONTAL) {
            longSize = getWidth();
            longPaddingBefore = getPaddingLeft();
            longPaddingAfter = getPaddingRight();
            shortPaddingBefore = getPaddingTop();
        } else {
            longSize = getHeight();
            longPaddingBefore = getPaddingTop();
            longPaddingAfter = getPaddingBottom();
            shortPaddingBefore = getPaddingLeft();
        }

        final float threeRadius = mRadius * 3;
        final float shortOffset = shortPaddingBefore + mRadius;
        float longOffset = longPaddingBefore + mRadius;
        if (mCentered) {
            longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f)
                    - ((count * threeRadius) / 2.0f);
        }

        float dX;
        float dY;

        float pageFillRadius = mRadius;
        if (mPaintStroke.getStrokeWidth() > 0) {
            pageFillRadius -= mPaintStroke.getStrokeWidth() / 2.0f;
        }

        //Draw stroked circles
        for (int iLoop = 0; iLoop < count; iLoop++) {
            float drawLong = longOffset + (iLoop * threeRadius);
            if (mOrientation == HORIZONTAL) {
                dX = drawLong;
                dY = shortOffset;
            } else {
                dX = shortOffset;
                dY = drawLong;
            }


            // Only paint fill if not completely transparent
            if (mPaintPageFill.getAlpha() > 0) {
                canvas.drawCircle(dX, dY, pageFillRadius, mPaintPageFill);
            }
            if (pageFillRadius != mRadius) {
                // Only paint stroke if a stroke width was non-zero
                    canvas.drawCircle(dX, dY, mRadius, mPaintStroke);
            }

        }

        //Draw the filled circle according to the current scroll
        float cx = mSnapPage % count * threeRadius;
        if (mOrientation == HORIZONTAL) {
            dX = longOffset + cx;
            dY = shortOffset;
        } else {
            dX = shortOffset;
            dY = longOffset + cx;
        }
        canvas.drawCircle(dX, dY, mRadius, mPaintFill);
        canvas.drawCircle(dX, dY, mRadius, mPaintCurrentPageStroke);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (mViewPager == null || getRealCount() == 0) {
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true;
                    }
                }

                if (mIsDragging) {
                    mLastMotionX = x;
                    if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
                        if (mViewPager != null) {
                            mViewPager.fakeDragBy(deltaX);
                        }
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging) {
                    final int count = mViewPager.getAdapter().getCount();
                    final int width = getWidth();
                    final float halfWidth = width / 2f;
                    final float sixthWidth = width / 6f;

                    if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager.setCurrentItem(mCurrentPage - 1);
                        }
                        return true;
                    } else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager.setCurrentItem(mCurrentPage + 1);
                        }
                        return true;
                    }
                }

                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                if (mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = MotionEventCompat.getX(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        return true;
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        mViewPager = view;
        mCurrentPage = 0;
        mViewPager.addOnPageChangeListener(this);
        invalidate();
    }


    @Override
    public void setCurrentItem(int item) {
        mCurrentPage = item;
        invalidate();
    }

    /**
     * Set a page change listener which will receive forwarded events.
     *
     * @param listener
     */
    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {

    }

    @Override
    public void notifyDataSetChanged() {
        mCurrentPage = 0;
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position % mPageCount;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        int index = position % mPageCount;
        mCurrentPage = index;
        if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mSnapPage = position;
            invalidate();
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL) {
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
        }
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureLong(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
            //We were told how big to be
            result = specSize;
        } else {
            //Calculate the width according the views count
            int count = getRealCount();
            result = (int)(getPaddingLeft() + getPaddingRight()
                    + (count * 2 * mRadius) + (count - 1) * mRadius + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int getRealCount() {
        return mPageCount;
    }
    /**
     * Determines the height of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureShort(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = (int)(2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
}
