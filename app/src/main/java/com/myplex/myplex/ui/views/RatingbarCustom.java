package com.myplex.myplex.ui.views;

import android.content.Context;
import android.content.res.TypedArray;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.myplex.myplex.R;

/**
 * Created by Apparao on 20/02/2019.<br>
 */

public class RatingbarCustom extends LinearLayout {

    int count;
    int rating = 0;
    int innerMargin;
    int starSize;
    int selectedImageResource;
    int unselectedImageResource;
    private OnRatingChangedListener onRatingChangedListener;

    public RatingbarCustom(Context context) {
        this(context, null);
    }

    public RatingbarCustom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingbarCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        count = 5;
        clickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((int) view.getId() >= 1000) {
                    rating = (int) view.getId() - 1000 + 1;
                    fillStars();
                    if (onRatingChangedListener != null)
                        onRatingChangedListener.onRatingChanged(rating);
                }
            }
        };
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.Ratingbar);
        count = ta.getInt(R.styleable.Ratingbar_numStarts, 5);
        rating = ta.getInt(R.styleable.Ratingbar_rating, 0);
        innerMargin = (int) ta.getDimension(R.styleable.Ratingbar_innerMargin, convertDpToPixel(getContext(), 50));
        starSize = (int) ta.getDimension(R.styleable.Ratingbar_starSize, convertDpToPixel(getContext(), 50));
        selectedImageResource = ta.getResourceId(R.styleable.Ratingbar_selected, R.drawable.star_selected);
        unselectedImageResource = ta.getResourceId(R.styleable.Ratingbar_unselected, R.drawable.star_unselected);

        rating = (getRating() > 0 && getRating() <= getCount()) ? getRating() : 0;
        ta.recycle();
        init(attrs);
        if (rating > 0) {
            fillStars();
        }
        invalidate();
    }

    private void fillStars() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) != null && getChildAt(i) instanceof ImageView) {
                if (i < getRating())
                    ((ImageView) getChildAt(i)).setImageResource(selectedImageResource);
                else
                    ((ImageView) getChildAt(i)).setImageResource(unselectedImageResource);
            }
        }
    }


    private void init(AttributeSet attrs) {
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(starSize, starSize);
            layoutParams.setMargins(0, 0, innerMargin, 0);
            imageView.setLayoutParams(layoutParams);
            imageView.setId(1000 + i);
            imageView.setClickable(true);
            imageView.setOnClickListener(clickListener);
            addView(imageView);
            imageView.setImageResource(unselectedImageResource);
        }
    }

    OnClickListener clickListener;

    public static int convertDpToPixel(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setRating(int rating) {
        this.rating = rating;
        fillStars();
    }

    public int getRating() {
        return rating;
    }

    public int getInnerMargin() {
        return innerMargin;
    }

    public void setInnerMargin(int innerMargin) {
        this.innerMargin = innerMargin;
    }

    public int getStarSize() {
        return starSize;
    }

    public void setStarSize(int starSize) {
        this.starSize = starSize;
    }

    public void setOnRatingChangedListener(OnRatingChangedListener onRatingChangedListener) {
        this.onRatingChangedListener = onRatingChangedListener;
    }

    public interface OnRatingChangedListener {
        void onRatingChanged(int rating);
    }

    public int getSelectedImageResource() {
        return selectedImageResource;
    }

    public void setSelectedImageResource(int selectedImageResource) {
        this.selectedImageResource = selectedImageResource;
    }

    public int getUnselectedImageResource() {
        return unselectedImageResource;
    }

    public void setUnselectedImageResource(int unselectedImageResource) {
        this.unselectedImageResource = unselectedImageResource;
    }
}
