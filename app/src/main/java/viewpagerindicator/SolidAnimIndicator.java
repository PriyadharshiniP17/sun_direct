package viewpagerindicator;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;

import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.myplex.myplex.R;

public class SolidAnimIndicator extends LinearLayout implements PageIndicator {

    private final static int DEFAULT_INDICATOR_WIDTH = 5;
    private int mIndicatorMargin;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mCurrentPage = 0;
    private int mAnimatorResId = R.animator.scale_with_alpha;
    private int mIndicatorBackground = R.drawable.black_radius_solid;
    private int mIndicatorSelectedBackground = R.drawable.black_radius_solid_red;
    private AnimatorSet mAnimationOut;
    private AnimatorSet mAnimationIn;
    private ViewPager mViewPager;
    private int mPageCount;

    public SolidAnimIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public SolidAnimIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        handleTypedArray(context, attrs);
        mAnimationOut = (AnimatorSet) AnimatorInflater.loadAnimator(context, mAnimatorResId);
        mAnimationOut.setInterpolator(new LinearInterpolator());
        mAnimationIn = (AnimatorSet) AnimatorInflater.loadAnimator(context, mAnimatorResId);
        mAnimationIn.setInterpolator(new ReverseInterpolator());
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {
          /*  TypedArray typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.SolidAnimIndicator);

            mIndicatorWidth =
                    typedArray.getDimensionPixelSize(R.styleable.SolidAnimIndicator_ci_width, -1);

            mIndicatorHeight =
                    typedArray.getDimensionPixelSize(R.styleable.SolidAnimIndicator_ci_height, -1);

            mIndicatorMargin =
                    typedArray.getDimensionPixelSize(R.styleable.SolidAnimIndicator_ci_margin, -1);

            mAnimatorResId = typedArray.getResourceId(R.styleable.SolidAnimIndicator_ci_animator,
                    R.animator.scale_with_alpha);

            mIndicatorBackground = typedArray.getResourceId(R.styleable.SolidAnimIndicator_ci_drawable,
                    R.drawable.black_radius_solid);

            typedArray.recycle();*/
        }

        mIndicatorWidth =
                (mIndicatorWidth == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorWidth;

        mIndicatorHeight =
                (mIndicatorHeight == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorHeight;

        mIndicatorMargin =
                (mIndicatorMargin == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorMargin;
    }

       /**
     * Bind the indicator to a ViewPager.
     *
     * @param viewPager
     */
    @Override
    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        invalidateIndicators();
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
        invalidateIndicators();
    }

    /**
     * Bind the indicator to a ViewPager.
     *
     * @param pagesRealCount
     */

    public void setPageCount(int pagesRealCount) {
        this.mPageCount = pagesRealCount;
        invalidateIndicators();
    }

    @Override
    public void setCurrentItem(int item) {
        mCurrentPage = item;
        invalidateIndicators();
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
        invalidateIndicators();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int index = position % mPageCount;

        if (getChildAt(mCurrentPage) == null)
            return;

        mAnimationIn.setTarget(getChildAt(mCurrentPage));
        mAnimationIn.start();

        mAnimationOut.setTarget(getChildAt(index));
        mAnimationOut.start();

        getChildAt(mCurrentPage).setBackgroundResource(mIndicatorBackground);
        getChildAt(index).setBackgroundResource(mIndicatorSelectedBackground);

        mCurrentPage = index;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void invalidateIndicators() {
        removeAllViews();

        if (mViewPager == null) {
            return;
        }

        int count = getRealCount();
        if (count < 2) {
            return;
        }

        mViewPager.addOnPageChangeListener(this);

        for (int i = 0; i < count; i++) {
            View indicator = new View(getContext());
            indicator.setBackgroundResource(mIndicatorBackground);
            addView(indicator, mIndicatorWidth, mIndicatorHeight);
            LayoutParams lp = (LayoutParams) indicator.getLayoutParams();
            lp.leftMargin = mIndicatorMargin;
            lp.rightMargin = mIndicatorMargin;
            indicator.setLayoutParams(lp);

            mAnimationOut.setTarget(indicator);
            mAnimationOut.start();
        }

        mAnimationOut.setTarget(getChildAt(mCurrentPage));
        getChildAt(mCurrentPage).setBackgroundResource(mIndicatorSelectedBackground);
        mAnimationOut.start();
    }

    private int getRealCount() {
        return mPageCount;
    }

    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float value) {
            return Math.abs(1.0f - value);
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public void cleanup() {
        mViewPager.clearOnPageChangeListeners();
    }

}
