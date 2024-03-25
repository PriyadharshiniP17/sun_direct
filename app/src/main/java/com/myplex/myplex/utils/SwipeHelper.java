package com.myplex.myplex.utils;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public abstract class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    public static final int BUTTON_WIDTH = 200;
    private static Boolean animate;
    private RecyclerView recyclerView;
    private List<UnderlayButton> buttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (UnderlayButton button : buttons) {
                if (button.onClick(e.getX(), e.getY()))
                    break;
            }

            return true;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (swipedPos < 0) return false;
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
            View swipedItem = swipedViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(e);
                else {
                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        }
    };

    public SwipeHelper(Context context, RecyclerView recyclerView, Boolean animate) {
        super(0, ItemTouchHelper.LEFT);
        this.animate = animate;
        this.recyclerView = recyclerView;
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };

        attachSwipe();
    }




    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * BUTTON_WIDTH;
        recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return 20.0f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0) {
            swipedPos = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * BUTTON_WIDTH / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            int pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX) {
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (UnderlayButton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(
                    c,
                    new RectF(
                            left,
                            itemView.getTop(),
                            right,
                            itemView.getBottom()
                    ),
                    pos
            );

            right = left;
        }
    }

    public void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    public interface UnderlayButtonClickListener {
        void onClick(int pos);
    }

    public static class UnderlayButton {
        private String text;
        private Drawable imageResId;
        private int buttonBackgroundcolor;
        private int textColor;
        private int pos;
        private RectF clickRegion;
        private UnderlayButtonClickListener clickListener;

        public UnderlayButton(String text, Drawable imageResId, int buttonBackgroundcolor, int textColor, UnderlayButtonClickListener clickListener) {
            this.text = text;
            this.imageResId = imageResId;
            this.buttonBackgroundcolor = buttonBackgroundcolor;
            this.textColor = textColor;
            this.clickListener = clickListener;
        }

        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                clickListener.onClick(pos);
                return true;
            }

            return false;
        }

        private void onDraw(Canvas canvas, RectF rect, int pos) {

            Paint p = new Paint();
            // Draw background
            p.setColor(buttonBackgroundcolor);
            canvas.drawRect(rect, p);
            Log.d("SwipeHelper", "onDraw: animate " + animate);
            Log.d("SwipeHelper", "onDraw: imageResId " + imageResId);
            if (!animate) {
                // Draw Text
//                p.setColor(Color.BLACK);
                p.setColor(textColor);
                p.setTextSize(30);
                Rect r = new Rect();
                float cHeight = rect.height();
                float cWidth = rect.width();
                Log.d("SwipeHelper", "onDraw: cHeight "+ cHeight + " cWidth " +cWidth);
                p.setTextAlign(Paint.Align.LEFT);
                p.getTextBounds(text, 0, text.length(), r);
                float x = cWidth / 2f - r.width() / 2f - r.bottom + 3;
                float y = cHeight / 2f + r.height() / 2f - r.left + 50;

             /*   float x = cWidth / 2f - r.width() / 2f - r.left;
                float y = cHeight / 2f + r.height() / 2f - r.bottom - 40;*/
                //-10 negative will increase space in bottom
                canvas.drawText(text, rect.left + x-10, rect.top + y-10, p);

                Log.d("SwipeHelper", "onDraw: x "+ x + " y " +y);
                if (imageResId != null) {
//                    imageResId.setBounds((int) (rect.left + 30), (int) (rect.top + (cHeight / 2f)), (int) (rect.right - 30), (int) (rect.bottom - ((cHeight / 10f))));
                    Log.d("SwipeHelper", "onDraw: left "+ (int) (rect.left));
                    Log.d("SwipeHelper", "onDraw: top "+ (int) (rect.top));
                    Log.d("SwipeHelper", "onDraw: right "+ (int) (rect.right));
                    Log.d("SwipeHelper", "onDraw: bottom "+ (int) (rect.bottom));

                    /*float aspectRatio = (float)imageResId.getIntrinsicWidth() / imageResId.getIntrinsicHeight();
                    int desiredWidthInPx = 100;
                    int derivedHeightInPx = (int)(desiredWidthInPx / aspectRatio);
                    imageResId.setBounds(0, 0, desiredWidthInPx, derivedHeightInPx);*/
//                    drawable.draw(canvas)
                    int margin = 45;
//                    imageResId.setBounds((int) (rect.left + 35), (int) (rect.top + (cHeight / 10f)), (int) (rect.right - 35), (int) (rect.bottom - ((cHeight / 2f))));
                    //imageResId.setBounds((int) rect.left+margin, (int) rect.top+margin-5, (int) rect.right-margin, (int) rect.bottom-margin-10); // expanded working
                    //imageResId.setBounds((int) rect.left+margin + 20, (int) rect.top+margin-5-19, (int) rect.left+(3* margin), (int) rect.top+(3* margin)-5-19); // // expanded working
                    //to move icon top increase negative in top and bottom
                    imageResId.setBounds((int) rect.left+margin + 25, (int) rect.top+margin-25, (int) rect.left+(3* margin) -5, (int) rect.top+(3* margin)-40);
//                    imageResId.setBounds((int) (rect.left + (cWidth / 2f)), (int) (rect.top + (cHeight / 10f)), (int) (rect.right - (cWidth / 2f)), (int) (rect.bottom - ((cHeight / 2f))));
//                    imageResId.setBounds((int) (rect.left + 30), (int) (rect.top + (cHeight / 2f)), (int) (rect.right -30), (int) (rect.bottom - ((cHeight / 10f))));
                    imageResId.draw(canvas);

                }

            } else {
                //animate
                // Draw Text
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(30);
                textPaint.setColor(textColor);
                StaticLayout sl = new StaticLayout(text, textPaint, (int) rect.width(),
                        Layout.Alignment.ALIGN_CENTER, 1, 1, false);

                if (imageResId != null) {
                    imageResId.setBounds((int) (rect.left + 50), (int) (rect.top + (rect.height() / 2f)), (int) (rect.right - 50), (int) (rect.bottom - ((rect.height() / 10f))));
                    imageResId.draw(canvas);
                }

                canvas.save();
                Rect r = new Rect();
                float y = (rect.height() / 2f) + (r.height() / 2f) - r.bottom - (sl.getHeight() / 2);

                if (imageResId == null)
                    canvas.translate(rect.left, rect.top + y);
                else
                    canvas.translate(rect.left, rect.top + y - 30);


                sl.draw(canvas);
                canvas.restore();
            }

            clickRegion = rect;
            this.pos = pos;
        }

    }
}