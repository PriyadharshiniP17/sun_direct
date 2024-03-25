package com.myplex.myplex.ui.fragment.epg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.collect.Maps;
import com.myplex.myplex.R;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import java.time.LocalDate;

//import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Classic EPG, electronic program guide, that scrolls both horizontal, vertical and diagonal.
 * It utilize onDraw() to draw the graphic on screen. So there are some private helper methods calculating positions etc.
 * Listed on Y-axis are channels and X-axis are programs/events. Data is added to EPG by using setEPGData()
 * and pass in an EPGData implementation. A click listener can be added using setEPGClickListener().
 * Created by Kristoffer, http://kmdev.se
 */
public class EPGView extends ViewGroup {

    public final String TAG = getClass().getSimpleName();
    public int vertical_scroll_offset = 0/*2*/;
    public static final int PAST_PROGRAM = 0;
    public static final int CURRENT_PROGRAM = 1;
    public static final int FUTURE_PROGRAM = 2;


    public static int DAY_IN_MILLIS = 1 * 24 * 60 * 60 * 1000;        // 4 days
    public static int DAYS_BACK_MILLIS = 0 * 24 * 60 * 60 * 1000;        // 4 days
    public static int DAYS_FORWARD_MILLIS = 0 * 24 * 60 * 60 * 1000;     // 2 days
    public static final int HOURS_IN_VIEWPORT_MILLIS = 1 * 60 * 60 * 1000;     // 1 hours
    public static final int TIME_LABEL_SPACING_MILLIS = 30 * 60 * 1000;        // 30 minutes

    private final Rect mClipRect;
    private final Rect mDrawingRect;
    private final Rect mMeasuringRect;
    private Rect mGoLiveRect;
    private TextPaint mTextPaint;
    private final Paint mPaint, mBorderPaint, mShadowPaint,mRecordPaint,mStrokePaint,mStrokePaintCurrent;
    public final Scroller mScroller;
    private final GestureDetector mGestureDetector;

    private final int mGoLiveButtonWidth;
    private final int mGoLiveButtonMargin;
    private final int mGoLiveButtonMarginLeft;

    private final int mChannelLayoutMargin;
    public boolean  drawingStatus = false;
    EPG.EPGProgram event;
    int chanelPosition;boolean isClickChannelImage;
    private final int mChannelLayoutPadding;
    private final int mChannelLayoutHeight;
    private final int mChannelImageHeight;
    private final int mChannelLayoutWidth;
    private final int mChannelImageWidth;
    private final int mChannelLayoutBackground;
    private final int mEventLayoutBackground;
    private int mEPGBackground,mSelectedEPGBackground;
    private final int mEventLayoutBackgroundCurrent;
    private final int mEventLayoutNonPlayableBackground;
    private final int mEventLayoutFutureProgramTextColor;
    private final int mEventLayoutTextColor, mChannelNumber;
    private final int mEventLayoutTextSize;
    private final int mEventPopupTextSize;
    private final int mTimeBarLineWidth;
    private final int mTimeBarNowLiveColor;
    private final int mTimeBarGoLiveColorActive;
    private final int mTimeBarGoLiveColorInActive;
    private final int mTimeBarLineColor;
    private final int mTimeBarHeight;
    private final int mTimeBarTextSize;
    private final int mTimeBarDividerColor;
    private final int mDateDividerColor;
    private final int mRecordedEventColor;

    private final int mTimeBarHeadWidth;
    private final int mTimeBarHeadHeight;
    private final int mTimeBarHeadTextSize;

    private final int mResetButtonSize;
    private final int mResetButtonMargin;
    private final Bitmap mResetButtonIcon;

    private final int mEPGTimeBarBackground;
    private final Map<String, Bitmap> mChannelImageCache;
    private final Map<String, SimpleTarget> mChannelImageTargetCache;

    private EPGClickListener mClickListener;
    private int mMaxHorizontalScroll;
    private int mMinHorizontalScroll;
    private int mMaxVerticalScroll;
    private int mLoadMoreScroll;
    private long mMillisPerPixel;
    private long mTimeOffset;
    private long mTimeLowerBoundary, mLandingTimeMillis;
    private long mTimeUpperBoundary;
    private int circleRadius;
    private int circleMargin;

    private float dragX;
    private float dragY;
    private boolean zooming;
    private boolean dragged;

    private boolean goLiveActive = false;
    //private TextView dummyTextView;


    private int L;
    private Context mContext;

    private EPGView ag;
    private View dummyChannelView;
    public boolean isGoLiveToolTipShown = true;

    private String recordButtonText = "";
    private String recordStopButtonText = "";
    private String recordText = "";
    private String recordedText = "";
    private String recordingText = "";
    private String recordScheduledText = "";
    private String enableNdvr = "false";

    Typeface eventTextTypeFace,mRecTypeFace, mBoldTypeFace;
    Bitmap arrowDrawable;
    private int mWidthInPixels = 100;
    private boolean drawingEventsInProgress = false;

    private int selectedTab = 0;


    Canvas canvasTemp;
    private EPGData epgData = null;
    private int mEpgMargin = 2;
    private int mEpgMarginCurrent = 6;
    private final SimpleDateFormat mFullDateTimeFormat = new SimpleDateFormat("EE, MMM dd, hh:mm a", Locale.getDefault());

    public EPGView(Context context) {
        this(context, null);
    }

    public EPGView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setEPGObject(EPGView epgObject) {
        this.ag = epgObject;
    }

    public static EPGView getEPGView(EPGView a) {
        return a.ag;
    }

    public EPGView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);
        if(mWidthInPixels < 100)
            mWidthInPixels = getResources().getDisplayMetrics().widthPixels;
        resetBoundaries();
        mDrawingRect = new Rect();
        mClipRect = new Rect();
        mMeasuringRect = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRecordPaint = new Paint();
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        Log.d(TAG, "EPGView: mEpgMargin "+ mEpgMargin);
        mStrokePaint.setStrokeWidth(mEpgMargin);
        mStrokePaint.setColor(getResources().getColor(R.color.epg_line_color));
        mStrokePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.margin_2));

        mStrokePaintCurrent = new Paint();
        mStrokePaintCurrent.setStyle(Paint.Style.STROKE);
        mStrokePaintCurrent.setStrokeWidth(mEpgMarginCurrent);
        mStrokePaintCurrent.setColor(getResources().getColor(R.color.epg_selected_box));


        //mRecTypeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Bold.ttf");
        //mRecordPaint.setTypeface(mRecTypeFace);
        //eventTextTypeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Regular.ttf");

        mRecTypeFace = Typeface.createFromAsset(context.getAssets(), "font/amazon_ember_cd_regular.ttf");

        eventTextTypeFace = Typeface.createFromAsset(context.getAssets(), "font/amazon_ember_cd_regular.ttf");
        mBoldTypeFace = Typeface.createFromAsset(context.getAssets(), "font/amazon_ember_cd_bold.ttf");
        mPaint.setTypeface(eventTextTypeFace);
        mRecordPaint.setTypeface(mBoldTypeFace);
        mGestureDetector = new GestureDetector(context, new OnGestureListener());
        mChannelImageCache = Maps.newHashMap();
        mChannelImageTargetCache = Maps.newHashMap();

        // Adding some friction that makes the epg less flappy.
        mScroller = new Scroller(context);
        // comment setFriction for smooth scrolling
        // mScroller.setFriction(0.1f);



        mEPGTimeBarBackground = getResources().getColor(R.color.epg_timebar_background);
//        mEPGBackground = getResources().getColor(R.color.epg_background);
        mSelectedEPGBackground = getResources().getColor(R.color.white70);

        mGoLiveButtonMargin = getResources().getDimensionPixelSize(R.dimen.epg_golive_button_margin);
        mGoLiveButtonMarginLeft = getResources().getDimensionPixelSize(R.dimen.epg_golive_button_margin_left);
        mGoLiveButtonWidth = getResources().getDimensionPixelSize(R.dimen.epg_golive_button_width);

        mChannelLayoutMargin = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_margin);
        mChannelLayoutPadding = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_padding);
        mChannelLayoutHeight = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_height);
        mChannelLayoutWidth = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_width);
        mChannelImageWidth = getResources().getDimensionPixelSize(R.dimen.epg_channel_image_width);
        mChannelImageHeight = getResources().getDimensionPixelSize(R.dimen.epg_channel_image_height);
        mChannelLayoutBackground = getResources().getColor(R.color.epg_channel_layout_background);

        mEventLayoutBackground = getResources().getColor(R.color.epg_event_layout_background);
        mEventLayoutBackgroundCurrent = getResources().getColor(R.color.epg_event_layout_background);
        mEventLayoutNonPlayableBackground = getResources().getColor(R.color.epg_event_layout_background); //epg_golive_inactive_bgcolor

        mEventLayoutFutureProgramTextColor = getResources().getColor(R.color.epg_event_future_program_text); //epg_event_future_program_text
        mEventLayoutTextColor = getResources().getColor(R.color.epg_event_layout_text);
        mChannelNumber = getResources().getColor(R.color.light_grey);
        mEventLayoutTextSize = getResources().getDimensionPixelSize(R.dimen.textsize_12);
        mEventPopupTextSize = getResources().getDimensionPixelSize(R.dimen.epg_event_record_popup_text);

        circleRadius = getResources().getDimensionPixelSize(R.dimen.epgcircleradius);
        circleMargin = getResources().getDimensionPixelSize(R.dimen.epgcirclemargin);

        mTimeBarHeadHeight = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_head_height);
        mTimeBarHeadWidth = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_head_width);
        mTimeBarHeadTextSize = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_head_text_size);

        mTimeBarHeight = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_height);
        mTimeBarTextSize = getResources().getDimensionPixelSize(R.dimen.textsize_12);
        mTimeBarLineWidth = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_line_width);
        mTimeBarDividerColor = getResources().getColor(R.color.epg_time_tab_divider_color);
        mDateDividerColor = getResources().getColor(R.color.epg_date_tab_divider_color);
        mTimeBarLineColor = getResources().getColor(R.color.yellow_strip);
        mTimeBarNowLiveColor = getResources().getColor(R.color.epg_now_head_background);
        mTimeBarGoLiveColorActive = getResources().getColor(R.color.epg_golive_active_bgcolor);
        mTimeBarGoLiveColorInActive = getResources().getColor(R.color.epg_golive_inactive_bgcolor);
        mRecordedEventColor = getResources().getColor(R.color.sea_green);

        mResetButtonSize = getResources().getDimensionPixelSize(R.dimen.epg_reset_button_size);
        mResetButtonMargin = getResources().getDimensionPixelSize(R.dimen.epg_reset_button_margin);

        recordText = getResources().getString(R.string.record);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = mResetButtonSize;
        options.outHeight = mResetButtonSize;
        mResetButtonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.fav_btn_active, options);
        mTextPaint = new TextPaint();

        mBorderPaint.setColor(mTimeBarLineColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mChannelLayoutMargin);
        clearEPGImageCache();
        //mPaint.setTypeface(eventTextTypeFace);

        // dummyView = new View(this.mContext);
    }

    public void updateDurationsInMillis(int backward, int forward) {

     //   Log.e("updateDurationsInMillis"," backward : "+backward);
      //  Log.e("updateDurationsInMillis","forward : "+forward);
        DateUtils.getDate(System.currentTimeMillis());


        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
       // Log.e("hour","++++++++++"+hour);
        /*Passing hour value to genrate old epg of crossed hours of the day */
        //  DAYS_BACK_MILLIS = hour * 60 * 60 * 1000;
        // CustomLog.e("DAYS_BACK_MILLIS","++++++++++"+DAYS_BACK_MILLIS);
       /* if (backward >= 0)
            DAYS_BACK_MILLIS = backward * 24 * 60 * 60 * 1000;*/

        //if (forward >= 0) //sravani
        DAYS_FORWARD_MILLIS = hour * 1000;


        if (backward >= 0)
            DAYS_BACK_MILLIS = hour * 60 * 60 * 1000;

      /*  if (forward >= 0)
            DAYS_FORWARD_MILLIS = forward * 24 * 60 * 60 * 1000;*/
        //  DAYS_FORWARD_MILLIS = hour * 60 * 60 * 1000;
        // Log.e("DAYS_FORWARD_MILLIS","++++++++++"+DAYS_FORWARD_MILLIS);
    }

    public void setSelectedTab(int tabPos){
        selectedTab = tabPos;
    }

    public void setWidthInPixels(int width){
        //   CustomLog.e(TAG,"set widht in pixels : "+ width);
        mWidthInPixels = width;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //   Log.e("isCurrent","onDraw");
        if (epgData != null && epgData.hasData()) {
            mTimeLowerBoundary = getTimeFrom(getScrollX());
            mTimeUpperBoundary = getTimeFrom(getScrollX() + getWidth() - mChannelLayoutWidth - mTimeBarLineWidth);

           // //Log.i(TAG, "mTimeLowerboundary : " + mFullDateTimeFormat.format(mTimeLowerBoundary) + "scrollx :: "+getScrollX());
//            Log.e("mTimeUpperBoundary", ": " + mTimeUpperBoundary);


            Rect drawingRect = mDrawingRect;
            drawingRect.left = getScrollX();
            drawingRect.top = getScrollY();
            drawingRect.right = drawingRect.left + getWidth();
            drawingRect.bottom = drawingRect.top + getHeight();

            drawChannelListItems(canvas, drawingRect);
            drawEvents(canvas, drawingRect);
            drawTimebar(canvas, drawingRect);
            drawTimeLine(canvas, drawingRect);

            //  drawResetButton(canvas, drawingRect);

            // If scroller is scrolling/animating do scroll. This applies when doing a fling.
            //added mScroller.getCurrY()-1 for smooth scrolling
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() - 1);
            }
        }
//        if (drawingStatus){
//            highlightSelectedEvent(canvas,event,chanelPosition,isClickChannelImage);
//        }
    }

    public void setActivityContext(Context context) {
        this.mContext = context;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // recalculateAndRedraw(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

//        CustomLog.e("epgfragment","fetch epg success 6");
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    private void drawResetButton(Canvas canvas, Rect drawingRect) {
        // Show button when scrolled 1/3 of screen width from current time
        final long threshold = getWidth() / 3;
        if (Math.abs(getXPositionStart() - getScrollX()) > threshold) {
            drawingRect = calculateResetButtonHitArea();
            mPaint.setColor(mTimeBarLineColor);
            canvas.drawCircle(drawingRect.right - (mResetButtonSize / 2),
                    drawingRect.bottom - (mResetButtonSize / 2),
                    Math.min(drawingRect.width(), drawingRect.height()) / 2,
                    mPaint);

            drawingRect.left += mResetButtonMargin;
            drawingRect.right -= mResetButtonMargin;
            drawingRect.top += mResetButtonMargin;
            drawingRect.bottom -= mResetButtonMargin;
            canvas.drawBitmap(mResetButtonIcon, null, drawingRect, mPaint);
        }
    }

    private void drawTimebarBottomStroke(Canvas canvas, Rect drawingRect) {
        drawingRect.left = getScrollX();
        drawingRect.top = getScrollY() + mTimeBarHeight;
        drawingRect.right = drawingRect.left + getWidth();
        drawingRect.bottom = drawingRect.top + mChannelLayoutMargin;
        // Bottom stroke
        mPaint.setColor(mEPGBackground);
        canvas.drawRect(drawingRect, mPaint);
    }


    private void drawTimebar(Canvas canvas, Rect drawingRect) {
        drawingRect.left = getScrollX()+ mChannelLayoutMargin;// + mChannelLayoutWidth + mChannelLayoutMargin;

        drawingRect.top = getScrollY();
        drawingRect.right = drawingRect.left + getWidth();
        drawingRect.bottom = drawingRect.top + mTimeBarHeight;

        mClipRect.left = getScrollX() + mChannelLayoutMargin; //+ mChannelLayoutWidth;
        mClipRect.top = getScrollY();
        mClipRect.right = getScrollX() + getWidth();
        mClipRect.bottom = mClipRect.top + mTimeBarHeight;

        canvas.save();
        canvas.clipRect(mClipRect);

        // Background
        mPaint.setColor(mEPGTimeBarBackground);
        canvas.drawRect(drawingRect, mPaint);

        // Time stamps
        mPaint.setColor(mEventLayoutTextColor);
        mPaint.setTextSize(mTimeBarTextSize);
       /* canvas.drawText("Channels",
                0+ 100,
                drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTimeBarTextSize / 2)), mPaint);*/
        int size = (HOURS_IN_VIEWPORT_MILLIS / TIME_LABEL_SPACING_MILLIS);
        for (int i = 0; i < size; i++) {
//            // Get time and round to nearest half hour
            final long time = TIME_LABEL_SPACING_MILLIS * (((mTimeLowerBoundary + (TIME_LABEL_SPACING_MILLIS * i)) + (TIME_LABEL_SPACING_MILLIS / 2)) / TIME_LABEL_SPACING_MILLIS);
            float f1 = getXFrom(time);
            mPaint.setStrokeWidth(mChannelLayoutMargin);
            mPaint.setColor(mTimeBarDividerColor);
            mPaint.setTypeface(mBoldTypeFace);
            float f2 = ((float) (((double) f1) - (((double) 2) * 0.5)));
            canvas.drawLine(f2, drawingRect.top, f2, drawingRect.bottom, mPaint);
            mPaint.setColor(mEventLayoutTextColor);
            mPaint.setTextSize(mTimeBarTextSize);

            // //Log.i(TAG,"Lowerboundry = "+ mFullDateTimeFormat.format(mTimeLowerBoundary)+" time : "+ mFullDateTimeFormat.format(time) + " Xfrom "+getXFrom(time) + "short : "+EPGUtil.getShortTime(time));
            canvas.drawText(EPGUtil.getShortTime(time),
                    getXFrom(time),
                    drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTimeBarTextSize / 2)), mPaint);
        }

        canvas.restore();

        drawTimebarDayIndicator(canvas, drawingRect);
        drawTimebarBottomStroke(canvas, drawingRect);
    }
    private void drawTimebarDayIndicator(Canvas canvas, Rect drawingRect) {

        //updateTabOnDateChange();

     /*   drawingRect.left = getScrollX();
        drawingRect.top = getScrollY();
        drawingRect.right = drawingRect.left + mChannelLayoutWidth;
        drawingRect.bottom = drawingRect.top + mTimeBarHeight;

        Background
        mPaint.setColor(mChannelLayoutBackground);
        canvas.drawRect(drawingRect, mPaint);

        // Text

        mPaint.setColor(mTimeBarLineColor);
        mPaint.setTextSize(mTimeBarTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
       // drawGoLive(canvas, drawingRect, false);*/


        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void updateTabOnDateChange() {
/*        long l1 = getTimeFrom(getScrollX() + getWidth() - mChannelLayoutWidth + mTimeBarLineWidth - getNowOffset() + mTimeBarHeadWidth / 2);
        int i1 = Days.daysBetween(new LocalDate(getCurrentTimeInMillis()), new LocalDate(l1)).getDays();
        if (this.L != i1) {
            this.L = i1;
            mClickListener.updateScroll(i1);
        }*/
    }

    private void drawTimeLine(Canvas canvas, Rect drawingRect) {
        long now = getCurrentTimeInMillis();
        //  Log.e("drawTimeLine","+++++++"+ drawingRect.left );
        if (shouldDrawTimeLine(now)) {
            drawingRect.left = getXFrom(now);
            //Rajesh change
            drawingRect.top = drawingRect.bottom;// - this.mTimeBarHeadHeight - (mTimeBarHeadTextSize);
//            drawingRect.top = drawingRect.bottom - this.mTimeBarHeadHeight - (mTimeBarHeadTextSize / 2);
            drawingRect.right = drawingRect.left + mTimeBarLineWidth;
            drawingRect.bottom = this.getScrollY() + getHeight();

            mPaint.setColor(mTimeBarLineColor);
            canvas.drawRect(drawingRect, mPaint);

            drawingRect.left = (getXFrom(now) + mTimeBarLineWidth);
            drawingRect.top = mTimeBarHeight + getScrollY();
            drawingRect.right = (drawingRect.left /*+ mTimeBarLineWidth*/);
            drawingRect.bottom = (drawingRect.top + getHeight());
            mPaint.setColor(mTimeBarLineColor);
            canvas.drawRect(drawingRect, mPaint);
            drawTimeLineHead(canvas, drawingRect);
            return;
        }

        drawingRect.left = getScrollX();
        drawingRect.top = getScrollY();
        drawingRect.right = drawingRect.left + mChannelLayoutWidth;
        drawingRect.bottom = drawingRect.top + mTimeBarHeight;
        drawingRect.left = (getScrollX() + this.mChannelLayoutWidth);
        drawingRect.top = (this.mTimeBarHeight + getScrollY());
        drawingRect.right = (drawingRect.left);
        drawingRect.bottom = (drawingRect.top + getHeight());
        mPaint.setColor(mTimeBarLineColor);
        canvas.drawRect(drawingRect, mPaint);
        // Log.e("drawTimeLine","+++++++"+ drawingRect.bottom  );
        //  drawGoLive(canvas, drawingRect, true);
        return;

    }


/*    private void drawTimeLineHead(Canvas canvas, Rect timelinerect) {
        timelinerect.left = (getXFrom(getCurrentTimeInMillis()) - (mTimeBarHeadWidth / 2));
        timelinerect.bottom = (this.getScrollY() + this.mTimeBarHeight);
        timelinerect.top = timelinerect.bottom - this.mTimeBarHeadHeight - (mTimeBarHeadTextSize);
        timelinerect.right = (timelinerect.left + mTimeBarHeadWidth); //TODO: need to move this to dimensions
        mPaint.setColor(this.mTimeBarLineColor);

        RectF rect = new android.graphics.RectF(((float) timelinerect.left),
                ((float) timelinerect.top),
                ((float) timelinerect.right),
                ((float) timelinerect.bottom));


        mPaint.setColor(this.mEventLayoutTextColor);

        mPaint.setTextAlign(android.graphics.Paint.Align.CENTER);

        mPaint.setTextAlign(Paint.Align.LEFT);


        int halfWidth = mTimeBarHeadTextSize;
        float x = (mTimeBarLineWidth / 2) + (float) (timelinerect.left + ((timelinerect.right - timelinerect.left) / 2));
        float y = timelinerect.bottom;//((float) (timelinerect.top + (((timelinerect.bottom - timelinerect.top)) + (mTimeBarHeadTextSize))));//((float) (drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTimeBarHeadTextSize / 2)))) - 440;
        Path path1 = new Path();


        path1.moveTo(halfWidth + x, y);
        path1.lineTo(x, halfWidth + y);
        path1.lineTo(x - halfWidth, y);
        path1.lineTo(x, y);


        path1.close();
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setAntiAlias(true);
        canvas.drawPath(path1, p);

        return;
    }*/

    private void drawTimeLineHead(Canvas canvas, Rect timelinerect) {
        // CustomLog.e("EPG","timeline head");
        // CustomLog.e("EPG","timeline head x value "+getCurrentTimeInMillis());
        //  Log.e("drawTimeLine","+++++++"+ timelinerect.left );
        timelinerect.left = (getXFrom(getCurrentTimeInMillis()) - (mTimeBarHeadWidth / 2));
        timelinerect.bottom = (this.getScrollY() + ((int) this.mTimeBarHeight));
        timelinerect.top = timelinerect.bottom - this.mTimeBarHeadHeight - (mTimeBarHeadTextSize / 2);
        timelinerect.right = (timelinerect.left + mTimeBarHeadWidth); //TODO: need to move this to dimensions


        mPaint.setColor(this.mTimeBarLineColor);
        // canvas.drawRoundRect(new RectF(((float) timelinerect.left), ((float) timelinerect.top), ((float) timelinerect.right), ((float) timelinerect.bottom)), ((float) 5), ((float) 5), mPaint);
        mPaint.setColor(this.mEventLayoutTextColor);
        mPaint.setTextSize(((float) getResources().getDimensionPixelSize(R.dimen._10sp)));
        mPaint.setTextAlign(Paint.Align.CENTER);
        //   canvas.drawText(getResources().getString(R.string.nowlive), ((float) (timelinerect.left + ((timelinerect.right - timelinerect.left) / 2))), ((float) (timelinerect.top + (((timelinerect.bottom - timelinerect.top) / 2) + (mTimeBarHeadTextSize / 2)))), mPaint);
        mPaint.setTextAlign(Paint.Align.LEFT);
        return;
    }

    private void drawGoLive(Canvas paramCanvas, Rect paramRect, boolean enable) {
        /*paramRect.left = getScrollX() + mGoLiveButtonMarginLeft;
        paramRect.top = getScrollY() + mGoLiveButtonMargin;
        paramRect.right = getScrollX()+mChannelLayoutWidth-mGoLiveButtonMarginLeft;
        paramRect.bottom = (paramRect.top + this.mTimeBarHeight)-mGoLiveButtonMargin - mGoLiveButtonMargin;*/

        paramRect.left = getScrollX() ;
        paramRect.top = getScrollY();
        paramRect.right = getScrollX()+mChannelLayoutWidth+mChannelLayoutMargin;
        paramRect.bottom = (paramRect.top + this.mTimeBarHeight);

        mPaint.setColor(this.mEPGTimeBarBackground);
        paramCanvas.drawRect(paramRect,mPaint);
        mGoLiveRect = paramRect;

/*        mPaint.setColor(enable ? this.mTimeBarGoLiveColorActive : this.mTimeBarGoLiveColorInActive);
        goLiveActive = enable;
        if(enable && !isGoLiveToolTipShown) {
            isGoLiveToolTipShown = true;
            mClickListener.showToolTip();
        }

        paramCanvas.drawRoundRect(new android.graphics.RectF(((float) paramRect.left), ((float) paramRect.top), ((float) paramRect.right), ((float) paramRect.bottom)), ((float) 5), ((float) 5), mPaint);

        mPaint.setColor(this.mEventLayoutBackground);
        mPaint.setTextSize(this.mTimeBarTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mTextPaint.setColor(this.mEventLayoutTextColor);
        mTextPaint.setTextSize(this.mTimeBarTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        String golive = getResources().getString(R.string.golive);

        int xPos = (paramRect.left +(paramRect.width() / 2));
        int yPos = (int) ((paramRect.top+(paramRect.height() / 2)) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2)) ;
        paramCanvas.drawText(golive, xPos, yPos, mTextPaint);

        int cXPos =(int) (xPos - (mTextPaint.measureText(golive)/2)- circleMargin);
        int cYPos = paramRect.top+(paramRect.height() / 2);
        mPaint.setColor(this.mEventLayoutTextColor);
        mPaint.setStyle(Paint.Style.FILL);
        paramCanvas.drawCircle( cXPos, cYPos,circleRadius,mPaint);

        mPaint.setTextAlign(Paint.Align.LEFT);*/
    }


    public void scrollTimeBarToPosition(int p9, boolean p10, boolean p11) {
        if ((epgData != null) && (epgData.hasData())) {

            //  Log.e(TAG,"scrolltimebarPosition called");
            int v2;
            //  this.L = p9;
            int lL = p9;
            if (!p11) {
                v2 = this.getScrollY();
            } else {
                v2 = 0;
            }
            int v5_2;
            Scroller v0_1 = mScroller;
            int v1 = this.getScrollX();
            int v3_6 = (this.getXFrom(((long) ((((lL * 24) * 60) * 60) * 1000)) + getCurrentTimeInMillis() /*+DateHelper.getInstance().a()*/ - (HOURS_IN_VIEWPORT_MILLIS / 2)) - this.getScrollX());
            int v5_1 = this.getNowOffset();
            if (!p10) {
                v5_2 = 0;
            } else {
                v5_2 = 600;
            }

            mScroller.startScroll(v1, v2, (v3_6 - v5_1), 0, v5_2);

            redraw();
            // mClickListener.updateScroll(lL);
        }
        return;
    }

    private void drawEvents(Canvas canvas, Rect drawingRect) {
        if(drawingEventsInProgress)
            return;
        drawingEventsInProgress = true;
        final int firstPos = getFirstVisibleChannelPosition();
        final int lastPos = getLastVisibleChannelPosition();
        // Log.e("firstpos","+++++++++"+firstPos);
        //  Log.e("lastPos","+++++++++"+lastPos);
        for (int pos = firstPos; pos <= lastPos; pos++) {

            // Set clip rectangle

            mClipRect.left = getScrollX() + mChannelLayoutWidth + mChannelLayoutMargin;
            mClipRect.top = getTopFrom(pos);
            mClipRect.right = getScrollX() + getWidth();
            mClipRect.bottom = mClipRect.top + mChannelLayoutHeight + mEpgMargin;

            canvas.save();
            canvas.clipRect(mClipRect);


            // Draw each event
            boolean foundFirst = false;
            List<EPG.EPGProgram>   epgEvents = epgData.getEPGPrograms(pos);
            // String logString = " Low : " + mTimeLowerBoundary + " up : " + mTimeUpperBoundary;
            if(null != epgEvents) {
                int visibleItemsCount = 0;
                int eventsSize = epgEvents.size();
                for (int i = 0; i < eventsSize; i++) {
                    EPG.EPGProgram event = epgEvents.get(i);
                    if (isEventVisible(getEventStartTime(event), getEventEndTime(event))) {
//                        Log.e(TAG,"drawing event "+event.getDisplay().getTitle());
                        drawEvent(canvas, pos, event, drawingRect);
                        visibleItemsCount++;
                        if (visibleItemsCount == 4)
                            foundFirst = true;
                    } else if (foundFirst) {
                        break;
                    }
                }
            }
      /*      for (EPG.EPGProgram event : epgEvents) {
                if (isEventVisible(getEventStartTime(event), getEventEndTime(event))) {
                    drawEvent(canvas, pos, event, drawingRect);
                    visibleItemsCount++;
                    if (visibleItemsCount == 4)
                        foundFirst = true;
                } else if (foundFirst) {
                    break;
                }
            }*/
            canvas.restore();
        }
        drawingEventsInProgress = false;
    }


    public long getCurrentTimeInMillis() {
        return DateHelper.getInstance().getCurrentLocalTime();

        // return System.currentTimeMillis();
    }

    private void drawEvent(final Canvas canvas, final int channelPosition, final EPG.EPGProgram event, final Rect drawingRect) {
        // CustomLog.e(TAG,"drawevent start");
        //  Log.e("isCurrent","111");
        long now = getCurrentTimeInMillis();
        long eventStartTime = getEventStartTime(event);
        long eventEndTime = getEventEndTime(event);
       /* if(eventStartTime < mLandingTimeMillis)
            eventStartTime = mLandingTimeMillis;*/
        setEventDrawingRectangle(channelPosition, eventStartTime, eventEndTime, drawingRect);
        //  Log.e("isCurrent","333");
        int programType = PAST_PROGRAM;
        boolean isfutureProgram = isFutureProgram(eventStartTime);
        boolean iscurrentProgram = isfutureProgram ? false : isCurrent(eventStartTime,eventEndTime);
//        if(isfutureProgram)
//            programType = FUTURE_PROGRAM;
//        else if(iscurrentProgram)
//            programType = CURRENT_PROGRAM;

        switch(programType){
            case FUTURE_PROGRAM:
                //  Log.e("isCurrent","444");
                if (!isPlayableEvent(event)) {
                    mPaint.setColor(mEventLayoutNonPlayableBackground);
                    canvas.drawRect(drawingRect, mPaint);
                    canvas.drawRect(drawingRect, mStrokePaint);
                } else {
                    mPaint.setColor(mEventLayoutBackgroundCurrent);
                    canvas.drawRect(drawingRect, mPaint);
                    canvas.drawRect(drawingRect, mStrokePaint);
                }

                break;
            case CURRENT_PROGRAM:
                // Log.e("isCurrent","555");
                Rect timelineRect = new Rect();
                timelineRect.left = drawingRect.left;
                timelineRect.top = drawingRect.top;
                timelineRect.right = getXFrom(now);
                timelineRect.bottom = drawingRect.bottom;

                mPaint.setColor(mEventLayoutBackground);
                canvas.drawRect(timelineRect, mPaint);
                canvas.drawRect(timelineRect, mStrokePaint);

                Rect timelineRect2 = new Rect();
                timelineRect2.left = getXFrom(now);
                timelineRect2.top = drawingRect.top;
                timelineRect2.right = drawingRect.right;
                timelineRect2.bottom = drawingRect.bottom;

                mPaint.setColor(mEventLayoutBackgroundCurrent);
                canvas.drawRect(timelineRect2, mPaint);
                canvas.drawRect(drawingRect, mStrokePaint);
                break;
            default:
                // Log.e("isCurrent","666");
                mPaint.setColor(mEventLayoutBackground);
                canvas.drawRect(drawingRect, mPaint);
                canvas.drawRect(drawingRect, mStrokePaint);
        }
//        event.getDisplay().getMarkers().get(0).getMarkerType()

        //  Typeface plain = Typeface.createFromAsset(getContext().getAssets(), "Lato-Bold.ttf");

        //   Paint mRecordPaint = new Paint();
        //  EPGView.mRecordPaint.setTypeface(plain);


        //String str = (getrecordtextCustom(event).length() == 0) ? "" : getrecordtextCustom(event);
        String str = getrecordtextCustom(event);
        if (str != null && str.trim().length() != 0) {

            //  EPGView.mRecordPaint = new Paint();
            // EPGView.mRecordPaint.setTypeface(plain);
            Paint.FontMetrics fm = new Paint.FontMetrics();

            str = " " + recordScheduledText;
            // str = " " + getrecordtext(event);
            boolean isFutureProgram = isFutureProgram(event);
            boolean isCurrentProgram = isCurrent(event);

            mRecordPaint.setTextSize(2 * circleMargin);
            mRecordPaint.getFontMetrics(fm);

            int margin = circleRadius;
            int x = drawingRect.left, y = drawingRect.top;

            Rect rectCustom = new Rect(margin + x, margin + y,
                    (int) (x + mRecordPaint.measureText(str) + (5 * margin)),
                    y + (2 * circleMargin) + 3 * margin);
            RectF rectF = new RectF(rectCustom);
            if (isFutureProgram || isCurrentProgram) {
                mRecordPaint.setColor(Color.RED);
                canvas.drawRoundRect(rectF, 2, 2, mRecordPaint);
                mRecordPaint.setColor(Color.WHITE);
                canvas.drawText(str, (float) (x + (3 * margin)), (float) (2 * circleMargin + y + 1.5 * margin), mRecordPaint);
                mPaint.setColor(this.mEventLayoutTextColor);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle((float) (x + (2.5 * margin)), (rectCustom.top + rectCustom.bottom) / 2
                        , circleRadius / 2, mPaint);
            } else {
                final Paint borderPaint = new Paint();
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setColor(getResources().getColor(R.color.white70));
                borderPaint.setAntiAlias(true);
                borderPaint.setDither(true);
                mTextPaint.setTypeface(mBoldTypeFace);
                canvas.drawRoundRect(rectF, 2, 2, borderPaint);
                rectF.inset(mTimeBarLineWidth / 3, mTimeBarLineWidth / 3);
                mRecordPaint.setColor(Color.WHITE);
                canvas.drawText(str, (float) (x + (3 * margin)), (float) (2 * circleMargin + y + 1.5 * margin), mRecordPaint);
                mPaint.setColor(this.mTimeBarLineColor);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle((float) (x + (2.5 * margin)), (rectCustom.top + rectCustom.bottom) / 2
                        , circleRadius / 2, mPaint);
            }


      /*      mRecordPaint.setColor(Color.WHITE);
            canvas.drawText(str, (float) (x + (3 * margin)), (float) (2 * circleMargin + y + 1.5 * margin), mRecordPaint);

            mPaint.setColor(this.mTimeBarLineColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle((float) (x + (2.5 * margin)), (rectCustom.top + rectCustom.bottom) / 2
                    , circleRadius / 2, mPaint);*/
        }
        if (getScrollX()+mChannelLayoutWidth > getXFrom(eventStartTime))
            drawingRect.left += mChannelLayoutWidth + getScrollX() - getXFrom(eventStartTime);
        if (drawingStatus && this.event.equals(event))
            canvas.drawRect(drawingRect, mStrokePaintCurrent);
        // Add left and right inner padding
        drawingRect.left += mChannelLayoutPadding;
        drawingRect.right -= mChannelLayoutPadding;


        // Text
        if (isfutureProgram) {
            mPaint.setColor(mEventLayoutFutureProgramTextColor);
            mTextPaint.setColor(mEventLayoutFutureProgramTextColor);
        } else {
            mPaint.setColor(mEventLayoutTextColor);
            mTextPaint.setColor(mEventLayoutTextColor);
        }
        mPaint.setTextSize(mEventLayoutTextSize);
       mTextPaint.setTypeface(mBoldTypeFace);
        String title ;

        int programType1 = PAST_PROGRAM;
        if(isfutureProgram)
            programType1 = FUTURE_PROGRAM;
        else if(iscurrentProgram)
            programType1 = CURRENT_PROGRAM;
       // LoggerD.debugLog("programType1 "+programType1);
        if(programType1 == PAST_PROGRAM && event.isCatchup() != null && !event.isCatchup().isEmpty() && event.isCatchup().equalsIgnoreCase("false"))
            title = "Program Not Available";
        else
            title = event.getDisplay().getTitle();
        if (title != null) {
            // Move drawing.top so text will be centered (text is drawn bottom>up)
            mTextPaint.getTextBounds(title, 0, title.length(), mMeasuringRect);
            mTextPaint.setTextSize(mEventLayoutTextSize);
            drawingRect.top += (((drawingRect.bottom - drawingRect.top) / 2) + (mMeasuringRect.height() / 2));
//            String eventName = TextUtils.ellipsize(title, mTextPaint, availablewidth, TextUtils.TruncateAt.END).toString();
//            if(eventName.length() < 2 && title.length() > 1)
//                eventName = title.substring(0,1) + "..";
//            canvas.drawText(eventName, drawingRect.left, drawingRect.top , mTextPaint);
            int availablewidth = (drawingRect.right - drawingRect.left)-30;
            canvas.drawText(TextUtils.ellipsize(title, mTextPaint, availablewidth, TextUtils.TruncateAt.END).toString(), drawingRect.left, drawingRect.top , mTextPaint);

            //TODO:: Enable below code to support two lines
    /*        String lineone, linetwo;
            lineone = title.substring(0,
                    mPaint.breakText(title, true, drawingRect.right - drawingRect.left, null));


            if (lineone.length() > 0) {
                linetwo = title.substring(lineone.length());
                if (linetwo != null && linetwo.length() > 0) {
                    canvas.drawText(lineone, drawingRect.left, drawingRect.top - (mEventLayoutTextSize / 2), mPaint);
                    linetwo = linetwo.substring(0,
                            mPaint.breakText(linetwo, true, drawingRect.right - drawingRect.left, null));
                    canvas.drawText(TextUtils.ellipsize(linetwo, mTextPaint, i2, TextUtils.TruncateAt.END).toString(), drawingRect.left, drawingRect.top + (mEventLayoutTextSize / 2), mPaint);
                } else {
                    canvas.drawText(lineone, drawingRect.left, drawingRect.top, mPaint);
                }
            }*/
/*
            mPaint.setColor(mTimeBarNowLiveColor);
            mTextPaint.setColor(mTimeBarNowLiveColor);
            String text = getrecordtext(event);
            //CustomLog.e("EPGEVENTRECORD","title : "+title+" text : "+text);
            if (text.contains(recordedText)) {
                mPaint.setColor(mRecordedEventColor);
                mTextPaint.setColor(mRecordedEventColor);
                text = " ✔ " + text;
            } else {
                mPaint.setColor(mTimeBarNowLiveColor);
                mTextPaint.setColor(mTimeBarNowLiveColor);
                mShadowPaint.setStyle(Paint.Style.STROKE);

            }
*/
            canvasTemp = canvas;
            invalidate();
        }
        //  CustomLog.e(TAG,"drawevent end");
    }


    private void setEventDrawingRectangle(final int channelPosition, final long start, final long end, final Rect drawingRect) {
        //   Log.e("isCurrent","222");
        drawingRect.left = getXFrom(start);
        drawingRect.top = getTopFrom(channelPosition);
        drawingRect.right = getXFrom(end) - mChannelLayoutMargin;
        drawingRect.bottom = getTopFrom(channelPosition + 1);
    }

    private void drawChannelListItems(Canvas canvas, Rect drawingRect) {
        // Background
        mMeasuringRect.left = getScrollX();
        mMeasuringRect.top = getScrollY();
        mMeasuringRect.right = drawingRect.left + mChannelLayoutWidth;
        mMeasuringRect.bottom = mMeasuringRect.top + getHeight();
        mPaint.setColor(mEPGBackground);
        canvas.drawRect(mMeasuringRect, mPaint);

        final int firstPos = getFirstVisibleChannelPosition();
        final int lastPos = getLastVisibleChannelPosition();
        //Log.e("drawChannelListItems "+firstPos, " lastPos : "+lastPos);
        for (int pos = firstPos; pos <= lastPos; pos++) {
            drawChannelItem(canvas, pos, drawingRect);

        }
    }

    private void drawChannelItem(final Canvas canvas, int position, Rect drawingRect) {
//        CustomLog.e("drawChannelItem "+position, " drawChannelItem : ");
        drawingRect.left = getScrollX();
        drawingRect.top = getTopFrom(position);
        drawingRect.right = drawingRect.left + mChannelLayoutWidth;
        drawingRect.bottom = drawingRect.top + mChannelLayoutHeight;
        mPaint.setColor(mChannelLayoutBackground);
        int channelLeft = drawingRect.left;
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setAntiAlias(true);
        //   mShadowPaint.setShadowLayer(5f, 10f, 10f, Color.BLACK);
        mShadowPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.margin_2));
        mShadowPaint.setStyle(Paint.Style.STROKE);
        //  setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // mPaint.setShadowLayer(10.0f, 0./0f, 2.0f, 0xFFFF0000);

        Paint channelItempaint = new Paint();
        channelItempaint.setStyle(Paint.Style.STROKE);
        channelItempaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.margin_2));
        channelItempaint.setColor(getResources().getColor(R.color.epg_line_color));
       // channelItempaint.setStrokeWidth(1);
//        channelItempaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.margin_2));
        canvas.drawRect(drawingRect, mPaint);
        canvas.drawRect(drawingRect, channelItempaint);
        String url, channelNumber;

        try{
            url = position < epgData.getChannelCount() ? epgData.getEPGChannel(position).getDisplay().getImageUrl() : "";
//            Log.e("TAG","channel url : "+url);
        }catch (Exception e){
            url = "";
        }

        if(epgData != null && epgData.getEPGChannel(position)!= null && epgData.getEPGChannel(position).getMetadata() !=null &&
                epgData.getEPGChannel(position).getMetadata().getChannelNumber()!= null) {
            channelNumber = epgData.getEPGChannel(position).getMetadata().getChannelNumber();
        } else
            channelNumber = "";

        // Loading channel image into target for
        final String imageURL = url;
        // imageURL = url;
        if (mChannelImageCache.containsKey(imageURL)) {
            Bitmap image = mChannelImageCache.get(imageURL);
            drawingRect = getDrawingRectForChannelImage(drawingRect, image);
            canvas.drawBitmap(image, null, drawingRect, null);
            mPaint.setColor(this.mChannelNumber);
            mPaint.setTextSize(mTimeBarTextSize);
            canvas.drawText(channelNumber,channelLeft+10, drawingRect.top+10,mPaint);
            // Hided channel number for client requirement
            // canvas.drawText(channelNumber,channelLeft, drawingRect.bottom,mPaint);
        } else {
            final int smallestSide = Math.min(mChannelLayoutHeight, mChannelLayoutWidth);

            if (!mChannelImageTargetCache.containsKey(imageURL)) {

               /* mChannelImageTargetCache.put(imageURL,new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mChannelImageCache.put(imageURL, resource);
                        redraw();
                        mChannelImageTargetCache.remove(imageURL);
                    }
                });*/

/*                mChannelImageTargetCache.put(imageURL, new BaseTarget<Bitmap>() {

                            @Override
                            public void getSize(SizeReadyCallback cb) {
                                cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL);
                            }

                            @Override
                            public void removeCallback(SizeReadyCallback cb) {

                            }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        mChannelImageCache.put(imageURL, resource);
                        redraw();
                        mChannelImageTargetCache.remove(imageURL);
                    }


                });*/
              /*  mChannelImageTargetCache.put(imageURL, new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mChannelImageCache.put(imageURL, resource);
                        redraw();
                        mChannelImageTargetCache.remove(imageURL);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }


                });*/
              /*  mChannelImageTargetCache.put(imageURL, new CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mChannelImageCache.put(imageURL, resource);
                        redraw();
                        mChannelImageTargetCache.remove(imageURL);
                    }

                    @Override
                            public void getSize(SizeReadyCallback cb) {
                                cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL);
                            }

                    @Override
                    public void removeCallback(@NonNull SizeReadyCallback cb) {

                    }
                });*/

                mChannelImageTargetCache.put(imageURL,new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mChannelImageCache.put(imageURL, resource);
                        redraw();
                        mChannelImageTargetCache.remove(imageURL);
                    }
                });
                EPGUtil.loadImageInto(getContext(), imageURL, smallestSide, smallestSide, mChannelImageTargetCache.get(imageURL));
            }

        }
        //mPaint.clearShadowLayer();
    }

    private Rect getDrawingRectForChannelImage(Rect drawingRect, Bitmap image) {
//        drawingRect.left += mChannelLayoutPadding;
//        drawingRect.top += mChannelLayoutPadding;
//        drawingRect.right -= mChannelLayoutPadding;
//        drawingRect.bottom -= mChannelLayoutPadding;

        //final int imageWidth = mChannelLayoutWidth;
        //final int imageHeight = mChannelLayoutHeight;

        final int imageWidth = mChannelImageWidth;
        final int imageHeight = mChannelImageHeight;
        //final float imageRatio = mChannelLayoutHeight / (float) mChannelLayoutWidth;
        final float imageRatio = imageHeight / (float) imageWidth;

        final int rectWidth = drawingRect.right - drawingRect.left;
        final int rectHeight = drawingRect.bottom - drawingRect.top;
        // Log.e("ImageBitmap",rectHeight+ "  ==  "+rectWidth+" == "+ imageRatio);
        // Keep aspect ratio.
        if (imageWidth > imageHeight) {
            final int padding = (int) (rectHeight - (rectWidth * imageRatio)) / 3;
            drawingRect.top += padding;
            drawingRect.bottom -= padding;
        } else if (imageWidth <= imageHeight) {
            final int padding = (int) (rectWidth - (rectHeight / imageRatio)) / 2;
            //  int padding = 100;
            drawingRect.left += padding + 30; // x start
            //  drawingRect.right -= padding; // x endo
            drawingRect.right = drawingRect.left + mChannelImageWidth; // x end

            drawingRect.top = drawingRect.top + padding / 2;
            drawingRect.bottom = drawingRect.top + mChannelImageHeight; // y end
            //  drawingRect.top += padding + 30;
       /*     drawingRect.top = rectWidth; // y start
            drawingRect.bottom = rectHeight; // y end*/
        }

        return drawingRect;
    }

    private boolean shouldDrawTimeLine(long now) {
        return now >= mTimeLowerBoundary && now < mTimeUpperBoundary;
    }

    private boolean isEventVisible(long start, long end) {
        return (start >= mTimeLowerBoundary && start <= mTimeUpperBoundary)
                || (end >= mTimeLowerBoundary && end <= mTimeUpperBoundary)
                || (start <= mTimeLowerBoundary && end >= mTimeUpperBoundary);
    }
    private boolean isEventLeftVisible(long start) {
        return (start >= mTimeLowerBoundary && start <= mTimeUpperBoundary);
    }

    private long calculatedBaseLine() {
        //  return LocalDateTime.now().toDateTime().minusMillis(DAYS_BACK_MILLIS).getMillis();
        return new DateTime(getCurrentTimeInMillis()).minus(DAYS_BACK_MILLIS + (int) (DateHelper.getInstance().getElapsedTime())).getMillis();
 /*       Date whateverDateYouWant = new Date(getCurrentTimeInMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(whateverDateYouWant);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 30;
        calendar.add(Calendar.MINUTE, mod < 30 ? -mod : (30-mod));
        return calendar.getTimeInMillis();*/
    }

    private int getFirstVisibleChannelPosition() {
        final int y = getScrollY();

        int position = (y - mChannelLayoutMargin - mTimeBarHeight)
                / (mChannelLayoutHeight + mChannelLayoutMargin);

        if (position < 0) {
            position = 0;
        }
        return position;
    }

    private int getLastVisibleChannelPosition() {
        final int y = getScrollY();
        final int totalChannelCount = epgData.getChannelCount();
        final int screenHeight = getHeight();
        int position = (y + screenHeight + mTimeBarHeight - mChannelLayoutMargin)
                / (mChannelLayoutHeight + mChannelLayoutMargin);

        if (position > totalChannelCount - 1) {
            position = totalChannelCount - 1;
        }

        // Add one extra row if we don't fill screen with current..
        return (y + screenHeight) > (position * mChannelLayoutHeight) && position < totalChannelCount - 1 ? position + 1 : position;
    }

    private void calculateMaxHorizontalScroll() {

        mMaxHorizontalScroll = (int) (((DAYS_BACK_MILLIS + (DAYS_FORWARD_MILLIS + DateHelper.getInstance().getElapsedTime())) +
                DateHelper.getInstance().getDayTimeInMillis() - HOURS_IN_VIEWPORT_MILLIS) / mMillisPerPixel);

        //   Log.e("mMaxHorizontalScroll","++++++"+mMaxHorizontalScroll);
    }

    /**
     * Added to recalucualte the maxscroll and minscroll  values  of each day based on index as scroll is for 24hours only
     * @param index
     */
    private void recalculateHorizScrollMinMaxValues(int index){
        //  Log.e(TAG,"recal hscroll for index : "+index);
        //  Log.e(TAG,"hscroll: back : "+DAYS_BACK_MILLIS+" elapse : "+DateHelper.getInstance().getElapsedTime()+" daytime : "+DateHelper.getInstance().getDayTimeInMillis());

        /* calculating max horizontal value to which we can scroll horizontally
           Elapsed time will return the time elapsed above an hour (Ex: if time is 1.30, elapsed time will be 30 minutes)
           getdaytimeinMillis will return the difference time between current time to starttime of the next day*/
        mMaxHorizontalScroll = (int) (((DAYS_BACK_MILLIS + ((index * 24*60*60*1000) + DateHelper.getInstance().getElapsedTime())) + DateHelper.getInstance().getDayTimeInMillis() - HOURS_IN_VIEWPORT_MILLIS) / mMillisPerPixel);

        //calculating  pixels for a day
        int dayOffset = (int)(( DAY_IN_MILLIS - HOURS_IN_VIEWPORT_MILLIS)/mMillisPerPixel);

        // deducting 1 day pixel values from maxhorizontal scroll pixel values to get MinScroll value
        mMinHorizontalScroll = mMaxHorizontalScroll - dayOffset;

    }



    public void calculateMaxVerticalScroll() {
        final int maxVerticalScroll = epgData == null ? 0 : getTopFrom(epgData.getChannelCount() + vertical_scroll_offset) + mChannelLayoutHeight;
        //   Log.e("vertical scroll","calculateMaxVerticalScroll "+maxVerticalScroll);
        //   Log.e("getheight scroll","calculateMaxVerticalScroll "+getHeight());
        mMaxVerticalScroll = maxVerticalScroll < getHeight() ? 0 : maxVerticalScroll - getHeight();
//        mMaxVerticalScroll=maxVerticalScroll;
        final int maxLoadmoreScroll = epgData == null ? 0 : getTopFrom(epgData.getChannelCount()) + mChannelLayoutHeight;
        mLoadMoreScroll = maxLoadmoreScroll < getHeight() ? 0 : maxLoadmoreScroll - getHeight();
//        CustomLog.e(TAG,"Max Vertical Scroll : "+mMaxVerticalScroll + " count : "+epgData.getChannelCount() + " chnlheight : "+mChannelLayoutHeight + " fromtop : "+ getTopFrom(epgData.getChannelCount())+" Height : "+getHeight());
    }

    private int getXFrom(long time) {
     /*   CustomLog.e(TAG,"time : "+time+" offset : "+ mTimeOffset + "millis : "+mMillisPerPixel +" chmargin :"+mChannelLayoutMargin
        +" chwidth :"+mChannelLayoutWidth);*/
        return (int) ((time - mTimeOffset) / mMillisPerPixel) + mChannelLayoutMargin
                + mChannelLayoutWidth + mChannelLayoutMargin;
    }
    private int getXFromHighLight(long time) {
      /*  Log.e("getXFromHighLight","time : "+time+" offset : "+ mTimeOffset + "millis : "+mMillisPerPixel +" chmargin :"+mChannelLayoutMargin
                +" chwidth :"+mChannelLayoutWidth);*/
        return (int) ((time - (mTimeOffset- mChannelLayoutWidth)) / mMillisPerPixel) + mChannelLayoutMargin
                + mChannelLayoutWidth  + mChannelLayoutMargin;
    }

    private int getTopFrom(int position) {
        int y = position * (mChannelLayoutHeight + mChannelLayoutMargin)
                + mChannelLayoutMargin + mTimeBarHeight;
        return y;
    }
    private int getleftFrom(int position) {
        int y = position / (mChannelLayoutWidth + mChannelLayoutMargin)
                + mChannelLayoutMargin + mTimeBarLineWidth;
        return y;
    }

    private long getTimeFrom(int x) {
        return (x * mMillisPerPixel) + mTimeOffset;
    }

    private long calculateMillisPerPixel() {
        return HOURS_IN_VIEWPORT_MILLIS / (mWidthInPixels - mChannelLayoutWidth - mChannelLayoutMargin);
    }

    private int getXPositionStart() {
        return getXFrom(getCurrentTimeInMillis() - (HOURS_IN_VIEWPORT_MILLIS) / 2);
    }
    private int getXPositionStart(long start) {
        return getXFrom(start - (HOURS_IN_VIEWPORT_MILLIS) / 2);
    }
    /*to navigate to spinner selected date data
     * we are calculating the start position of the new day
     * (getscrollx value will be changed to new day start position)*/
    private int getXPositionStartOfNewDay(int index) {
        return getXFrom(getCurrentTimeInMillis() + (index * 24 * 60 * 60 *1000));
    }
    private void resetBoundaries() {
        //CustomLog.e("EPG"," calinstance null resetBoundries  ");
        mMillisPerPixel = calculateMillisPerPixel();
        mTimeOffset = calculatedBaseLine();
        //   mLandingTimeMillis = mTimeOffset;
        mTimeLowerBoundary =getTimeFrom(0);
        //   mTimeLowerBoundary = getXFrom(calculatedBaseLine()) + mChannelLayoutMargin;
        mTimeUpperBoundary = getTimeFrom(getScrollX() + getWidth() - mTimeBarLineWidth);
    }

    private Rect calculateChannelsHitArea() {
        mMeasuringRect.top = mTimeBarHeight;
        int visibleChannelsHeight = epgData.getChannelCount() * (mChannelLayoutHeight + mChannelLayoutMargin);
        mMeasuringRect.bottom = visibleChannelsHeight < getHeight() ? visibleChannelsHeight : getHeight();
        mMeasuringRect.left = 0;
        mMeasuringRect.right = mChannelLayoutWidth;
        return mMeasuringRect;
    }

    private Rect calculateProgramsHitArea() {
        mMeasuringRect.top = mTimeBarHeight;
        int visibleChannelsHeight = epgData.getChannelCount() * (mChannelLayoutHeight + mChannelLayoutMargin);
        mMeasuringRect.bottom = visibleChannelsHeight < getHeight() ? visibleChannelsHeight : getHeight();
        mMeasuringRect.left = mChannelLayoutWidth;
        mMeasuringRect.right = getWidth();
        return mMeasuringRect;
    }
    private Rect calculateProgramsHitAreaCheckProgram() {
        mMeasuringRect.top = mTimeBarHeight;
        int visibleChannelsHeight = epgData.getChannelCount() * (mChannelLayoutHeight + mChannelLayoutMargin);
        mMeasuringRect.bottom = getHeight();
        mMeasuringRect.left = mChannelLayoutWidth;
        mMeasuringRect.right = getWidth();
        return mMeasuringRect;
    }
    private Rect calculateResetButtonHitArea() {
        mMeasuringRect.left = getScrollX() + getWidth() - mResetButtonSize - mResetButtonMargin;
        mMeasuringRect.top = getScrollY() + getHeight() - mResetButtonSize - mResetButtonMargin;
        mMeasuringRect.right = mMeasuringRect.left + mResetButtonSize;
        mMeasuringRect.bottom = mMeasuringRect.top + mResetButtonSize;
        return mMeasuringRect;
    }

    private Rect calculateGoLiveButtonHitArea() {
        mMeasuringRect.left = getScrollX() + mGoLiveButtonMarginLeft;
        mMeasuringRect.top = getScrollY() + mGoLiveButtonMargin;
        mMeasuringRect.right = getScrollX() + mChannelLayoutWidth - mGoLiveButtonMarginLeft;
        mMeasuringRect.bottom = (mMeasuringRect.top + this.mTimeBarHeight - mGoLiveButtonMargin - mGoLiveButtonMargin);
        return mMeasuringRect;

    }

    private int getChannelPosition(int y) {
        y -= mTimeBarHeight;
        int channelPosition = (y + mChannelLayoutMargin)
                / (mChannelLayoutHeight + mChannelLayoutMargin);

        return epgData.getChannelCount() == 0 ? -1 : channelPosition;
    }

    private int getProgramPosition(int channelPosition, long time) {
        List<EPG.EPGProgram> events = epgData.getEPGPrograms(channelPosition);

        if (events != null) {
            int size = events.size();
            for (int eventPos = 0; eventPos < size; eventPos++) {
                EPG.EPGProgram event = events.get(eventPos);

                if (getEventStartTime(event) <= time && getEventEndTime(event) >= time) {
                    return eventPos;
                }
            }
        }
        return -1;
    }

    public long getEventStartTime(EPG.EPGProgram event) {
        if (event != null && event.getDisplay().getMarkers() != null) {

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                String markertype = markers.get(i).getMarkerType();
                try {
                    if (markertype.equalsIgnoreCase("startTime"))
                        return (Long.parseLong(markers.get(i).getValue()) + DateHelper.getInstance().getTimezoneOffsetValue());
                } catch (NumberFormatException e) {

                }

            }

        }
        return -1l;
    }

    public long getEventEndTime(EPG.EPGProgram event) {
        if (event != null && event.getDisplay().getMarkers() != null) {
            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                try {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("endTime"))
                        return (Long.parseLong(markers.get(i).getValue()) + DateHelper.getInstance().getTimezoneOffsetValue());
                } catch (NumberFormatException e) {

                }

            }
        }
        return -1l;
    }

    public boolean isCurrent(EPG.EPGProgram event) {

        long now = getCurrentTimeInMillis();
        ////CustomLog.e("EPG"," event start time :"+getEventStartTime(event));
        ////CustomLog.e("EPG"," event end time :"+getEventEndTime(event));
        // //CustomLog.e("EPG"," event now time :"+now);
        return now >= getEventStartTime(event) && now <= getEventEndTime(event);
    }

    public boolean isFutureProgram(EPG.EPGProgram event) {
        long now = getCurrentTimeInMillis();
        return now < getEventStartTime(event);
    }

    public boolean isFutureProgram(long eventstarttime){
        long now = getCurrentTimeInMillis();
        return now < eventstarttime;
    }

    public boolean isCurrent(long eventstarttime,long eventendtime) {

        long now = getCurrentTimeInMillis();
        //CustomLog.e("EPG"," event start time :"+getEventStartTime(event));
        //CustomLog.e("EPG"," event end time :"+getEventEndTime(event));
        // CustomLog.e("EPG"," event now time :"+now);
        return now >= eventstarttime && now <= eventendtime;
    }

    public boolean isPlayableEvent(EPG.EPGProgram event) {
        try {

            if (event == null)
                return true;

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                for (int i = 0; i < size; i++) {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("special")) {
                        if (markers.get(i).getValue().equalsIgnoreCase("playable"))
                            return true;
                        else {
                            return isCurrent(event);
                        }
                    }
                }
            }
            return true;
        } catch (NullPointerException e) {
            return true;
        }
    }

    public boolean isWatchableEvent(EPG.EPGProgram event) {
        try {

            if (enableNdvr.equalsIgnoreCase("false")) //if ndvr is not enabled, all past programs are playable
                return true;

            if (event == null)
                return true;

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                for (int i = 0; i < size; i++) {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("tag")) {
                        String value = markers.get(i).getValue();

                        return value != null && (value.contains(recordedText) ||
                                value.contains(recordingText) ||
                                value.contains(recordScheduledText));
                    }
                }
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public String getrecordtext1111(EPG.EPGProgram event) {
        List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
        if (markers != null) {
            int size = markers.size();

            for (int i = 0; i < size; i++) {
                String markertype = markers.get(i).getMarkerType();
                if (markertype.equalsIgnoreCase("recordingLabel")) {
                    return "RECO";
                }
            }
        }
        return "";
    }

    /**
     * Returns the recording state text
     *
     * @param event
     * @return
     */
    public String getrecordtext(EPG.EPGProgram event) {
        String recordstatetext = "";
        boolean isrecordableevent = false;
        try {

            if (event == null)
                return "";

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                boolean isFutureProgram = isFutureProgram(event);
                boolean isCurrentProgram = isCurrent(event);
                for (int i = 0; i < size; i++) {
                    String markertype = markers.get(i).getMarkerType();
                    String markerValue = markers.get(i).getValue();
                    if (markertype.equalsIgnoreCase("record") ||
                            markertype.equalsIgnoreCase("stoprecord"))
                        isrecordableevent = true;

                    if (markertype.equalsIgnoreCase("stoprecord")) {
                        if (!isFutureProgram && !isCurrentProgram) {
                            mPaint.setColor(mRecordedEventColor);
                            mTextPaint.setColor(mRecordedEventColor);
                            //recordstatetext = UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDED);
                            recordstatetext = recordedText;

                        } else {
                            if (isCurrentProgram)
                                //recordstatetext = UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDING);
                                recordstatetext = recordingText;
                            else
                                //recordstatetext = UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDSCHEDULED );
                                recordstatetext = recordScheduledText;
                        }

                    } else if (markertype.equalsIgnoreCase("record")) {
                        if (isFutureProgram)
                            recordstatetext = recordText;
                    } else if (markertype.equalsIgnoreCase("recordingLabel")) {
                        if (isFutureProgram)
                            recordstatetext = "RecData";
                    }


                    if (markertype.equalsIgnoreCase("tag")) {
                        recordstatetext = markers.get(i).getValue();
                        if (recordstatetext != null && recordstatetext.trim().length() > 0) {

                            if (!isCurrentProgram && !isFutureProgram) { //if it is past program, the tag should be "Recorded"
                                if (recordstatetext.contains(recordingText/*UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDING)*/)
                                        || recordstatetext.contains(recordScheduledText/*UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDSCHEDULED)*/)) {
                                    // recordstatetext = UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDED);
                                    mPaint.setColor(mRecordedEventColor);
                                    mTextPaint.setColor(mRecordedEventColor);
                                    recordstatetext = recordedText;
                                }
                            } else if (isCurrentProgram) {
                                if (recordstatetext.contains(recordScheduledText/*UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDSCHEDULED)*/)) {
                                    recordstatetext = recordingText;
                                }
                            }
                            if (recordstatetext.contains(recordedText/*UiUtils.getRecordConfigTexts(NavigationConstants.STATE_RECORDED)*/)) {
                                mPaint.setColor(mRecordedEventColor);
                                mTextPaint.setColor(mRecordedEventColor);
                            }
                        }

                    }
                }
            }
            // recordstatetext = "";
        } catch (NullPointerException e) {
            recordstatetext = "";
        }

        return recordstatetext;
       /* if(isrecordableevent)
            return recordstatetext;
        else
            return "";*/
    }

    public String getrecordtextCustom(EPG.EPGProgram event) {
        String recordstatetext = "";

        try {

            if (event == null)
                return "";

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();

                for (int i = 0; i < size; i++) {
                    String markertype = markers.get(i).getMarkerType();

                    if (markertype.equalsIgnoreCase("recordingLabel")) {
                        // String markerValue = markers.get(i).getValue();
                        recordstatetext = markers.get(i).getValue();
                    }
                }
            }
            recordingText = event.getDisplay().getTitle();
            // recordstatetext = "";
        } catch (NullPointerException e) {
            recordstatetext = "";
        }

        return recordstatetext;
       /* if(isrecordableevent)
            return recordstatetext;
        else
            return "";*/
    }

    /**
     * Will return the record action to be done
     *
     * @param event
     * @return
     */
    public int getrecordaction(EPG.EPGProgram event) {
       /* int state = -1; //not recordable
        try {

            if (event == null)
                return -1;

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                for (int i = 0; i < size; i++) {
                    String markertype = markers.get(i).getMarkerType();
                    if (markertype.equalsIgnoreCase("record"))
                        return NavigationConstants.ACTION_RECORD;

                    if (markertype.equalsIgnoreCase("stoprecord"))
                        return NavigationConstants.ACTION_STOPRECORD;

                }
            }
        } catch (NullPointerException e) {
        }*/
        return -1;

    }

    /**
     * returns the value of the Marker
     *
     * @param event
     * @return
     */
    public String getRecordMarkerValue(EPG.EPGProgram event) {
        /*try {

            if (event == null)
                return "";

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                for (int i = 0; i < size; i++) {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("record") ||
                            markers.get(i).getMarkerType().equalsIgnoreCase("stoprecord")) {
                        return markers.get(i).getValue();

                    }

                }
            }


        } catch (NullPointerException e) {

        }*/
        return "";
    }


    /**
     * Will update the record marker type and value  (similar to storing local)
     *
     * @param event
     * @param state
     */
    public void updateRecordMarkerTagValue(EPG.EPGProgram event, int state) {
        /*try {

            if (event == null)
                return;

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                for (int i = 0; i < size; i++) {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("record")) {
                        if (markers.get(i).getValue().contains("action=1")) {
                            String newvalue = markers.get(i).getValue();
                            newvalue = newvalue.replace("action=1", "action=0");
                            markers.get(i).setValue(newvalue);
                        }
                        markers.get(i).setMarkerType("stoprecord");
                    } else if (markers.get(i).getMarkerType().equalsIgnoreCase("stoprecord")) {
                        if (markers.get(i).getValue().contains("action=0")) {
                            String newvalue = markers.get(i).getValue();
                            newvalue = newvalue.replace("action=0", "action=1");
                            markers.get(i).setValue(newvalue);
                        }
                        markers.get(i).setMarkerType("record");
                    }

                }
            }

        } catch (NullPointerException e) {

        }
*/
    }

    /**
     * Updates marker type action value based on the record option selected(series,episode,channel,timeslot)
     *
     * @param event
     * @param selectedRecordOptionValue
     */
    public void updateRecordMarkerTagValue(EPG.EPGProgram event, String selectedRecordOptionValue) {
        /*try {

            if (event == null)
                return;

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            if (markers != null) {
                int size = markers.size();
                for (int i = 0; i < size; i++) {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("record")) {
                        if (selectedRecordOptionValue != null && selectedRecordOptionValue.trim().length() > 1) {
                            if (markers.get(i).getValue().contains(selectedRecordOptionValue)) {
                                if (selectedRecordOptionValue.contains("action=1")) {
                                    String newvalue = markers.get(i).getValue();
                                    String newRecordOptionValue = selectedRecordOptionValue.replace("action=1", "action=0");
                                    newvalue = newvalue.replace(newvalue, newRecordOptionValue);
                                    markers.get(i).setValue(newvalue);
                                }
                            }
                        }
                 *//*       if(markers.get(i).getValue().contains("action=1")){
                            String newvalue =  markers.get(i).getValue();
                            newvalue = newvalue.replace("action=1","action=0");
                            markers.get(i).setValue(newvalue);
                        }*//*
                        markers.get(i).setMarkerType("stoprecord");
                    } else if (markers.get(i).getMarkerType().equalsIgnoreCase("stoprecord")) {

                        if (selectedRecordOptionValue != null && selectedRecordOptionValue.trim().length() > 1) {
                            if (markers.get(i).getValue().contains(selectedRecordOptionValue)) {
                                if (selectedRecordOptionValue.contains("action=0")) {
                                    String newvalue = markers.get(i).getValue();
                                    String newRecordOptionValue = selectedRecordOptionValue.replace("action=0", "action=1");
                                    newvalue = newvalue.replace(newvalue, newRecordOptionValue);
                                    markers.get(i).setValue(newvalue);
                                }
                            }
                        }
                 *//*       if(markers.get(i).getValue().contains("action=0")){
                            String newvalue =  markers.get(i).getValue();
                            newvalue =  newvalue.replace("action=0","action=1");
                            markers.get(i).setValue(newvalue);
                        }*//*
                        markers.get(i).setMarkerType("record");
                    }

                }
            }

        } catch (NullPointerException e) {

        }*/

    }

    /**
     * Add click listener to the EPG.
     *
     * @param epgClickListener to add.
     */
    public void setEPGClickListener(EPGClickListener epgClickListener) {
        mClickListener = epgClickListener;
    }

    /**
     * Add data to EPG. This must be set for EPG to able to draw something.
     *
     * @param epgData pass in any implementation of EPGData.
     */
    public void setEPGData(EPGData epgData) {

        this.epgData = epgData;
    }

    //Dummy Views we are Using to Show coach screens
/*    public void setDummyViews(TextView view, View channelView) {
        this.dummyTextView = view;
        if (mGoLiveRect != null) {
            //   dummyTextView.setPadding(getScrollX() +mGoLiveButtonMarginLeft+mGoLiveButtonMarginLeft,getScrollY()+mGoLiveButtonMargin,getScrollX() +mChannelLayoutWidth-mGoLiveButtonMarginLeft,0);
            //  dummyTextView.setPadding(50,50,50,50);
            dummyTextView.setWidth(mChannelLayoutWidth - mGoLiveButtonMarginLeft - mGoLiveButtonMarginLeft);
            dummyTextView.setHeight(mChannelLayoutHeight - mGoLiveButtonMargin - mGoLiveButtonMargin);
        }

        this.dummyChannelView = channelView;
    }*/

    /**
     * This will recalculate boundaries, maximal scroll and scroll to start position which is current time.
     * To be used on device rotation etc since the device height and width will change.
     *
     * @param withAnimation true if scroll to current position should be animated.
     */
    public void recalculateAndRedraw(boolean withAnimation) {
        if (epgData != null && epgData.hasData()) {
            resetBoundaries();

            calculateMaxVerticalScroll();
            calculateMaxHorizontalScroll();

//            mScroller.startScroll(getScrollX(), getScrollY(),
//                    0, getTopFrom(), withAnimation ? 600 : 0);


            redraw();

        }
    }

    /**
     * *Redraw or each spinner data selecton*
     * @param withAnimation
     * @param diffindex difference between selected tab index and today tab index
     */
    public void recalculateAndRedrawNewDay(boolean withAnimation,int diffindex) {
        if (epgData != null && epgData.hasData()) {
            resetBoundaries();

            calculateMaxVerticalScroll();
            //calculateMaxHorizontalScroll();
            recalculateHorizScrollMinMaxValues(diffindex);
            /*Made y value to 0, to fix scroll issue
             * to move it to intial position*/
            mScroller.startScroll(getScrollX(), 0,
                    getXPositionStartOfNewDay(diffindex) - getScrollX() - getNowOffset(),
                    0, withAnimation ? 600 : 0);

            redraw();

        }
    }

    private int getNowOffset() {
        //return (int) (0.375D * getWidth());
        return (int) (0.375D * (getWidth() > getHeight()?getHeight():getWidth()));
        // return 0;
    }

    public void resetScrollOffsetValue(int value) {
        //  vertical_scroll_offset = value;
        vertical_scroll_offset = 0;

    }

    /**
     * Does a invalidate() and requestLayout() which causes a redraw of screen.
     */
    public void redraw() {
        // Log.e("redraw","called");
        invalidate();
        requestLayout();
        calculateMaxVerticalScroll();
    }

    /**
     * Clears the local image cache for channel images. Can be used when leaving epg and you want to
     * free some memory. Images will be fetched again when loading EPG next time.
     */
    public void clearEPGImageCache() {
        mChannelImageCache.clear();
    }

    private boolean mIsScrolling = false;

    //todo scroll to top(pagination in other genre) when user changes genre
    public void scrollToTopPosition() {
        scrollTo(0,0);
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //   Log.e("Direction", "onSingleTap");
            if (epgData == null)
                return true;
            // This is absolute coordinate on screen not taking scroll into account.
            int x = (int) e.getX();
            int y = (int) e.getY();

            // Adding scroll to clicked coordinate
            int scrollX = getScrollX() + x;
            int scrollY = getScrollY() + y;

            if (epgData == null)
                return true;

            int channelPosition = getChannelPosition(scrollY);
            if (channelPosition != -1 && mClickListener != null) {
                // if (calculateResetButtonHitArea().contains(scrollX,scrollY)) {
                if (calculateGoLiveButtonHitArea().contains(scrollX, scrollY)) {
                    // Reset button clicked
                    if (goLiveActive)
                        mClickListener.onResetButtonClicked();
                } else if (calculateChannelsHitArea().contains(x, y)) {
                    // Channel area is clicked

                    int programPosition = 0;
                    if (epgData.getEPGPrograms(channelPosition) != null) {
                        List<EPG.EPGProgram> epgPrograms = epgData.getEPGPrograms(channelPosition);
                        if (epgPrograms != null && epgPrograms.size() > 0 && epgPrograms.get(programPosition) != null) {
                            if (null == epgPrograms || epgPrograms.size() < 1
                                    || null == epgPrograms.get(programPosition))
                                return true;
                            if (isEventFinished(epgPrograms.get(programPosition))) {
                                programPosition = 1;
                            } else {
                                programPosition = 0;
                            }
                        }
                    }
                    //Log.e("isCurrent",canvasTemp.getWidth()+ " "+canvasTemp.getHeight()+" "+canvasTemp.getDensity());
                    if (epgData.getEPGChannel(channelPosition) != null) {
                        epgData.getEPGChannel(channelPosition)
                                .getMetadata();
                        mClickListener.onChannelClicked(channelPosition, epgData.getEPGChannel(channelPosition)
                                , programPosition, "" + epgData.getEPGChannel(channelPosition)
                                        .getMetadata().getId()
                                , epgData.getEPGProgram(channelPosition, programPosition));
                    }
                } /*else if (calculateProgramsHitArea().contains(x, y)) {
                    // Event area is clicked
                    int programPosition = getProgramPosition(channelPosition, getTimeFrom(getScrollX() + x - calculateProgramsHitArea().left));
                    CustomLog.e("programPosition ","+++++++"+programPosition);
                    if (programPosition != -1) {
                        mClickListener.onEventClicked(channelPosition, programPosition, ""+epgData.getEPGChannel(channelPosition).getMetadata().getId().getValue(),epgData.getEPGProgram(channelPosition, programPosition));
                    }
                }*/else{
                    // Log.e("isCurrent",canvasTemp.getWidth()+ " "+canvasTemp.getHeight()+" "+canvasTemp.getDensity());
                    int programPosition = getProgramPosition(channelPosition, getTimeFrom(getScrollX() + x - calculateProgramsHitArea().left));
                    //Log.e("programPosition ","+++++++"+programPosition);
                    int visibleChannelsHeight = epgData.getChannelCount() * (mChannelLayoutHeight + mChannelLayoutMargin);
                   // Log.e("channelPosition",channelPosition+" "+programPosition+" event");
                    if (programPosition != -1 && calculateProgramsHitAreaCheckProgram().contains(x,y)) {
                        mClickListener.onEventClicked(channelPosition, programPosition,
                                ""+epgData.getEPGChannel(channelPosition).getMetadata().getId(),
                                epgData.getEPGProgram(channelPosition, programPosition));
                    }
                   // Log.e("else apart","+++++++");
                }
            }

            return true;
        }
        int scrollDistance;
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {


            mIsScrolling = true;
            int dx = (int) distanceX;
            int dy = (int) distanceY;
            int x = getScrollX();
            int y = getScrollY();
            scrollDistance=getScrollY();
          //  //Log.i(TAG,"onscroll  getscrollx :: "+ x +" dx :: "+dx +" x+dx = "+(x+dx) + " maxHscroll :: "+mMaxHorizontalScroll);

            // Avoid over scrolling
            /*Removed to handle minus valye,cross check tmrw*/
          /*  if (x + dx < 0) {
                dx = 0 - x;
            }*/
            if (y + dy < 0) {
                dy = 0 - y;
            }
//            Log.e("loadmore","y :"+y +" dy : "+dy +" mLoadMoreScroll : "+mLoadMoreScroll);
//            Log.e("loadmore","x :"+x +" dx : "+dx );
            if(dy>0) {
                if (y + dy > mLoadMoreScroll) {
                    // Log.e("loadmore", "Loadmore : hit api"  );
                    mClickListener.loadMore();
                    //   Log.e(TAG,"Load more ");
                }
            }
       /*     if(mMaxHorizontalScroll < 0){
                if (x + dx < mMaxHorizontalScroll) {
                    dx = mMaxHorizontalScroll - x;
                    CustomLog.e(TAG,"onscroll x+dx is less than hscroll dx value - " + dx);
                }
            }else*/ {
                if (x + dx > mMaxHorizontalScroll) {
                    dx = mMaxHorizontalScroll - x;
                }else if(x+dx < mMinHorizontalScroll){
                    dx = mMinHorizontalScroll - x;
                }
            }
            if (y + dy > mMaxVerticalScroll) {
                dy = mMaxVerticalScroll - y;
            }

            float x1 = e1.getX();
            float y1 = e1.getY();

            float x2 = e2.getX();
            float y2 = e2.getY();

            Direction direction = getDirection(x1, y1, x2, y2);

            switch (direction) {
                case up:
                    //case down:
                    EPGView.this.scrollBy(0, dy);

                    break;
                case down:
                    //   Log.e("epgfragment","fetch epg success 7");
                    if (y <= 0)
                        EPGView.getEPGView(EPGView.this).getParent().requestDisallowInterceptTouchEvent(false);
                    EPGView.this.scrollBy(0, dy);

                    return true;
                case left:
                case right:
//                    CustomLog.e("epgfragment","fetch epg success 8");
                    if (y <= 0) {
                        EPGView.getEPGView(EPGView.this).getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    EPGView.this.scrollBy(dx, 0);
                    break;
            }
            return true;


        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float vX, float vY) {
            //   Log.e("OnFling","+++++++=="+e1.toString());
            //   Log.e("OnFling","+++++++=="+e2.toString());

            float x1 = e1.getX();
            float y1 = e1.getY();

            float x2 = e2.getX();
            float y2 = e2.getY();
//            CustomLog.e("OnFling","x1 : "+x1 +"y1 : "+y1 +" x2 :"+x2 +"y2 :"+y2);
            Direction direction = getDirection(x1, y1, x2, y2);
            // Log.e("OnFling","direction "+direction.toString());
            /*To add pagination vertically added below
             * if fling up, scrolled up checking the scrolldistance and mLoadMoreScroll*/

            /*if(direction==Direction.up) {
                if (e2.getAction() == MotionEvent.ACTION_UP) {
                    CustomLog.e("OnFling", "+++++++==" + scrollDistance);
                    CustomLog.e("OnFling", "+++++++==" + mLoadMoreScroll);
                    if (scrollDistance == mLoadMoreScroll) {
                        CustomLog.e("call loadmore", "loadmore");
                        mClickListener.loadMore();
                    }
                }
            }*/
            switch (direction) {
                case up:
                case down:
                    mScroller.fling(getScrollX(), getScrollY(), 0, -(int) vY, mMinHorizontalScroll, mMaxHorizontalScroll, 0, mMaxVerticalScroll);
                    break;
                case left:
                case right:
                    mScroller.fling(getScrollX(), getScrollY(), -(int) vX, 0, mMinHorizontalScroll, mMaxHorizontalScroll, 0, mMaxVerticalScroll);
                    break;
            }
            redraw();
            return true;
        }


        @Override
        public boolean onDown(MotionEvent e) {
            //   Log.e("MotionEvent","++++++"+mScroller.isFinished());
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
                return true;
            }
            return true;
        }

        /**
         * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
         * returns the direction that an arrow pointing from p1 to p2 would have.
         *
         * @param x1 the x position of the first point
         * @param y1 the y position of the first point
         * @param x2 the x position of the second point
         * @param y2 the y position of the second point
         * @return the direction
         */
        private Direction getDirection(float x1, float y1, float x2, float y2) {
            double angle = getAngle(x1, y1, x2, y2);
            return Direction.get(angle);
        }

        /**
         * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
         * The angle is measured with 0/360 being the X-axis to the right, angles
         * increase counter clockwise.
         *
         * @param x1 the x position of the first point
         * @param y1 the y position of the first point
         * @param x2 the x position of the second point
         * @param y2 the y position of the second point
         * @return the angle between two points
         */
        private double getAngle(float x1, float y1, float x2, float y2) {

            double rad = Math.atan2(y1 - y2, x2 - x1) + Math.PI;
            return (rad * 180 / Math.PI + 180) % 360;
        }
    }

    private enum Direction {
        up,
        down,
        left,
        right;

        /**
         * Returns a direction given an angle.
         * Directions are defined as follows:
         * <p>
         * Up: [45, 135]
         * Right: [0,45] and [315, 360]
         * Down: [225, 315]
         * Left: [135, 225]
         *
         * @param angle an angle from 0 to 360 - e
         * @return the direction of an angle
         */
        public static Direction get(double angle) {
            if (inRange(angle, 45, 135)) {
                return Direction.up;
            } else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
                return Direction.right;
            } else if (inRange(angle, 225, 315)) {
                return Direction.down;
            } else {
                return Direction.left;
            }
        }

        /**
         * @param angle an angle
         * @param init  the initial bound
         * @param end   the final bound
         * @return returns true if the given angle is in the interval [init, end).
         */
        private static boolean inRange(double angle, float init, float end) {
            return (angle >= init) && (angle < end);
        }
    }
    public EPG.EPGChannel getChannelDetailsByPosition(int position){
        if(epgData != null)
            return epgData.getEPGChannel(position);
        else
            return null;
    }
    //to check whether event is finished event
    public boolean isEventFinished(EPG.EPGProgram event){

     /*   long startTime, endTime;
        startTime = getEventStartTime(event);
        endTime = getEventEndTime(event);
        long currentTimeMillis = System.currentTimeMillis();
        if(event != null && event.getTarget().getPageAttributes().getIsLive()!= null && event.getTarget().getPageAttributes().getIsLive().equalsIgnoreCase("true")) {
            if(startTime < currentTimeMillis && endTime < currentTimeMillis)
                return true;
        }*/
        return false;
    }
    public void scrollSet(int y, int dy){
        mScroller.startScroll(0,0,0,0,0);
    }

    public void highlightSelectedEvent(Canvas canvas,EPG.EPGProgram event,int chanelPosition,boolean isClickChannelImage){
        //Log.e("isCurrent","true");

        Rect drawingRect = mDrawingRect;
        //this condition for is channel image click else event click
        if (isClickChannelImage){
            drawingRect.left = getScrollX();
            drawingRect.top = getTopFrom(chanelPosition);
            drawingRect.right = drawingRect.left + mChannelLayoutWidth;
            drawingRect.bottom = drawingRect.top + mChannelLayoutHeight;
        }else {
            //this condition for selected event box outside or on the image of channel
            if((-(mScroller.getCurrX() - getXFrom(getEventStartTime(event)))) >= mChannelLayoutWidth){
                //Log.e("isCurrent","true   "  +getXFrom(getEventStartTime(event)));
                drawingRect.left = getXFrom(getEventStartTime(event));
            }else {
                //Log.e("isCurrent","false"+ "  "+mChannelLayoutWidth);
                drawingRect.left =getScrollX()+mChannelLayoutWidth;
            }
            drawingRect.right = getXFrom(getEventEndTime(event));
            drawingRect.top = getTopFrom(chanelPosition);
            drawingRect.bottom = drawingRect.top + mChannelLayoutHeight;
            mPaint.setColor(mSelectedEPGBackground);
        }
        try {
            canvas.drawRect(drawingRect, mStrokePaintCurrent);
            //   invalidate();
        }catch (Exception e) {
            //Log.e("canvasTemp",e.getMessage());
        }
    }

    // getting crash when canvas is used outside the onDraw() method, so added drawingStatus for access the canvas
    public void setDrawingStatus(boolean drawingStatus,EPG.EPGProgram event,int chanelPosition,boolean isClickChannelImage) {
        this.drawingStatus = drawingStatus;
        this.event = event;
        this.chanelPosition = chanelPosition;
        this.isClickChannelImage = isClickChannelImage;
        invalidate();
    }
    public void viewInvalidate(boolean drawingStatus) {
        this.drawingStatus = drawingStatus;
        invalidate();
    }

}
