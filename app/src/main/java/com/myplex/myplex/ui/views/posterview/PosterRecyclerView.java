package com.myplex.myplex.ui.views.posterview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.PicassoUtil;

/**
 * Created by Luka on 24.1.2018.
 */
public class PosterRecyclerView extends RelativeLayout {

    private static final String TAG = "ParallaxBackgroundRV";
    private int backgroundColor;

    private float imageScale;
    /**
     * The end of image movement.
     * When this threshold is reached the @mBackground stops moving
     */
    public int backgroundThreshold;
    /**
     * The End background backgroundImageAlpha.
     * This is the backgroundImageAlpha of the image on the end of its path
     */
    public float endBackgroundAlpha;
    /**
     * The Start offset.
     * The starting offset of the recylcer view
     */
    public int startOffset;
    /**
     * The Start background offset.
     *
     * @gBackround stargin offset. The scrollState from which the image stars moving
     */
    public int startBackgroundOffset;
    /**
     * The image view that holds the @mBackground image.
     */
    public ImageView mBackground;

    private FrameLayout mainLayout;
    private int endBackgroundOffsetMargin;
    private View rootView;
    public RecyclerView mRecyclerView;
    private RecyclerView.Adapter adapter;
    private IScrollListener mScrollListener;
    private LinearLayoutManager layout;
    private Drawable gDrawable;
    private int scrollOfset = 0;
    private int backgroundImageAlpha;
    private boolean enableBacgroundAlpha = true;
    private boolean enableBackgroundMove = true;

    private boolean readFromState = false;

    /**
     * Instantiates a new Google play recycler view.
     *
     * @param context the context
     */
    public PosterRecyclerView(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Google play recycler view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public PosterRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PosterRecyclerView,
                0, 0);
        try {
            int startingOffsetAttr = typedArray.getDimensionPixelSize(R.styleable.PosterRecyclerView_start_offset, 0);
            int endBgOffsetAttr = typedArray.getDimensionPixelSize(R.styleable.PosterRecyclerView_end_background_offset, 0);
            int startBackgroundAttr = typedArray.getDimensionPixelSize(R.styleable.PosterRecyclerView_start_background_offset, 0);
            imageScale = (typedArray.getFloat(R.styleable.PosterRecyclerView_image_scale, 1) > 1) ? 1 : typedArray.getFloat(R.styleable.PosterRecyclerView_image_scale, 1);
            gDrawable = typedArray.getDrawable(R.styleable.PosterRecyclerView_image);
            endBackgroundAlpha = typedArray.getFloat(R.styleable.PosterRecyclerView_end_background_alpha, 0);
            backgroundThreshold = typedArray.getInt(R.styleable.PosterRecyclerView_background_step, 2);
            backgroundColor = typedArray.getColor(R.styleable.PosterRecyclerView_background_color, 2);
            //Convert dp to px
//            endBackgroundOffsetMargin = (int) convertDpToPixel(endBgOffsetAttr, getContext());
//            startOffset = (int) convertDpToPixel(startingOffsetAttr, getContext());
//            SDKLogger.debug("startOffset: " + startOffset + " startingOffsetAttr: " + startingOffsetAttr );
//            startBackgroundOffset = (int) convertDpToPixel(startBackgroundAttr, getContext());
            endBackgroundOffsetMargin = endBgOffsetAttr;
            startOffset = startingOffsetAttr;
            startBackgroundOffset = startBackgroundAttr;
            SDKLogger.debug("startOffset: " + startOffset + " endBackgroundOffsetMargin: " + endBackgroundOffsetMargin + " startBackgroundOffset: " + startBackgroundOffset);

        } catch (Exception e) {
            typedArray.recycle();
        }
        init(getContext());
        typedArray.recycle();
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.poster_bg_rv, this);
        mRecyclerView = rootView.findViewById(R.id.parallax_bg_rv);
        mainLayout = findViewById(R.id.container_rv);
        mBackground = rootView.findViewById(R.id.gp_background);
        mBackground.setImageDrawable(gDrawable);
        mBackground.setScaleX(imageScale);
        mBackground.setScaleY(imageScale);

        layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        layout.setItemPrefetchEnabled(false);
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.addOnScrollListener(onScrollListener);
//        setMarginsToChild(mBackground, startBackgroundOffset);
        mainLayout.setBackgroundColor(backgroundColor);
        mRecyclerView.addItemDecoration(new PaddingItemDecoration(startOffset));
        enableDefaultSnapHelper();
    }

    private void drawImage() {
        mBackground = new ImageView(getContext());
        mBackground.setId(generateViewId());
        mBackground.setImageDrawable(gDrawable);
        mBackground.setScaleX(imageScale);
        mBackground.setScaleY(imageScale);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mainLayout.addView(mBackground, lp);
    }

    private void drawRecylcerView() {

    }

    /**
     * Sets adatper.
     *
     * @param adatper the adatper
     */
    public void setAdapter(RecyclerView.Adapter adatper) {
        this.adapter = adatper;
        mRecyclerView.setAdapter(adatper);
    }

    /**
     * Gets adapter.
     *
     * @return the adapter
     */
    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public LinearLayoutManager getLayout() {
        return layout;
    }

    public RecyclerView getmRecyclerView() {
        return mRecyclerView;
    }


    /**
     * Sets google play recycler view scroll listener.
     *
     * @param mScrollListener the google play recycler view scroll listener
     */
    public void setmScrollListener(IScrollListener mScrollListener) {
        this.mScrollListener = mScrollListener;
    }

    /**
     * Converts dp into pixels
     *
     * @param dp
     * @return
     */
    private int dpToPixels(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /**
     * Sets left scrollState to child
     *
     * @param v
     * @param margin
     */
    private void setMarginsToChild(View v, int margin) {
        LayoutParams params = (LayoutParams) mBackground.getLayoutParams();
        params.leftMargin = margin;
        v.setLayoutParams(params);
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (mScrollListener != null)
                mScrollListener.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mScrollListener != null)
                mScrollListener.onScrolled(recyclerView, dx, dy);
            int marginToAnimate = scrollOfset / backgroundThreshold;
            //No need to change values
            int leftThreshold = Math.abs(endBackgroundOffsetMargin - startOffset);//-8-0
//            int leftThreshold = Math.abs(startBackgroundOffset - endBackgroundOffsetMargin);//-8-0
            SDKLogger.debug("startBackgroundOffset: " + startBackgroundOffset +
                    " leftThreshold: " + leftThreshold +
                    " endBackgroundOffsetMargin: " + endBackgroundOffsetMargin +
                    " startOffset: " + startOffset +
                    " scrollOfset: " + scrollOfset +
                    " dx: " + dx +
                    " startBackgroundOffset - marginToAnimate: " + (startBackgroundOffset - marginToAnimate));
//            getState();
            if (scrollOfset < leftThreshold) {
                if (enableBackgroundMove) {
//                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mBackground.getLayoutParams();
//                    params.leftMargin = startBackgroundOffset - marginToAnimate;
//                    params.rightMargin = startBackgroundOffset - marginToAnimate;
//                    mBackground.setLayoutParams(params);
                    mBackground.setTranslationX(startBackgroundOffset - marginToAnimate);
                    mBackground.postInvalidate();
                    // Move the object at the X position 500
                }
                if (enableBacgroundAlpha) {
                    backgroundImageAlpha = calculateAlpha(startOffset - recyclerView.computeHorizontalScrollOffset());
                    mBackground.setAlpha(backgroundImageAlpha);
                }
            }
            //Ge the scroll offset of the first itemViewHolder
            scrollOfset += dx;
        }
    };

    /**
     * Calculates the alpha from the distance of scroll
     *
     * @param currentMargin
     * @return
     */
    private int calculateAlpha(float currentMargin) {
        int endOffset = (endBackgroundOffsetMargin < 0 ? -endBackgroundOffsetMargin : endBackgroundOffsetMargin);
        float calc = ((currentMargin + endOffset) / (startOffset + endOffset));
        if (calc <= endBackgroundAlpha)
            return (int) (255 * endBackgroundAlpha);
        return (int) (255 * calc);
    }

    public void setImageUrl(String imageLink) {

        if (TextUtils.isEmpty(imageLink)
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            mBackground.setImageResource(R.drawable
                    .carousel_bg_placeholder_xhdpi);
        } else {
            PicassoUtil.with(getContext()).load(imageLink, mBackground, R.drawable.carousel_bg_placeholder_xhdpi);
        }
    }


    /**
     * Padding decorator
     */
    private class PaddingItemDecoration extends RecyclerView.ItemDecoration {
        private final int size;

        public PaddingItemDecoration(int size) {
            this.size = size;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left += size;
            }
        }
    }

    /**
     * The interface recycler view scroll listener.
     */
    public interface IScrollListener {

        /**
         * On scroll state changed.
         *
         * @param recyclerView the recycler view
         * @param newState     the new state
         */
        void onScrollStateChanged(RecyclerView recyclerView, int newState);

        /**
         * On scrolled.
         *
         * @param recyclerView the recycler view
         * @param dx           the dx
         * @param dy           the dy
         */
        void onScrolled(RecyclerView recyclerView, int dx, int dy);

    }

    /**
     * Debug method
     */
    public void getState() {
      /*  //Log.d(TAG, "state -> " +
                "startOffset: " + startOffset +
                " endBackgroundOffsetMargin: " + endBackgroundOffsetMargin +
                " startBackgroundOffset: " + startBackgroundOffset +
                " endBackgroundAlpha: " + endBackgroundAlpha +
                " currentBackgroundLeft " + mBackground.getLeft() +
                " scrollOffset " + scrollOfset
        );*/
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        State state = new State();
        state.scrollState = scrollOfset;
        state.backgroundOffset = mBackground.getLeft();
        state.backgroundAlpha = backgroundImageAlpha;
        for (int i = 0; i < getChildCount(); i++) {
            ss.childrenStates.append(i, state);
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            State stateO = (State) ss.childrenStates.get(i);
            scrollOfset = stateO.scrollState;
            this.setgBackgroundPosition(stateO.backgroundOffset);
            mBackground.setAlpha(stateO.backgroundAlpha);
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private void setgBackgroundPosition(int left) {
        LayoutParams params = (LayoutParams) mBackground.getLayoutParams();
        params.leftMargin = left;
        mBackground.setLayoutParams(params);
    }

    /**
     * Enables or disables the alpha animation
     *
     * @param enableBacgroundAlpha
     */
    public void setEnableBacgroundAlpha(boolean enableBacgroundAlpha) {
        this.enableBacgroundAlpha = enableBacgroundAlpha;
    }

    /**
     * Enables or disables the background animation
     *
     * @param enableBackgroundMove
     */
    public void setEnableBackgroundMove(boolean enableBackgroundMove) {
        this.enableBackgroundMove = enableBackgroundMove;
    }

    /**
     * Sets the background color of the main view
     *
     * @param color
     */
    public void setViewBackgroundColor(int color) {
        this.mainLayout.setBackgroundColor(color);
    }

    /**
     * Sets a custom snap helper to recyclerView
     *
     * @param snapHelper
     */
    public void setSnapHelper(SnapHelper snapHelper) {
        snapHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * Enables the default snap helper, that mimics the play recylcer view
     */
    public void enableDefaultSnapHelper() {
        SnapHelper snapHelper = new StartSnapHelper();
//        linearSnapHelper.startingOffset = startOffset;
//        linearSnapHelper.itemPadding = dpToPixels(16);
        setSnapHelper(snapHelper);
    }

    private class State {
        public int scrollState;
        public int backgroundOffset;
        public int backgroundAlpha;
    }
}