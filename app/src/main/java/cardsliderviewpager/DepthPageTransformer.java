package cardsliderviewpager;

import android.view.View;

import androidx.core.view.ViewCompat;

import cardsliderviewpager.viewpager2.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 0.6f;
    private static final float MIN_FADE = 0.8f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) {
            view.setAlpha(MIN_FADE);
        } else if (position < 0) {
            view.setAlpha(1 + position * (1 - MIN_FADE));
            view.setTranslationX(-pageWidth * MAX_SCALE * position);
            ViewCompat.setTranslationZ(view, position);
            float scaleFactor = MIN_SCALE
                    + (MAX_SCALE - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else if (position == 0) {
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(MAX_SCALE);
            ViewCompat.setTranslationZ(view, 0);
            view.setScaleY(MAX_SCALE);
        } else if (position <= 1) {
            ViewCompat.setTranslationZ(view, -position);
            view.setAlpha(1 - position * (1 - MIN_FADE));
            view.setTranslationX(pageWidth * MAX_SCALE * -position);

            float scaleFactor = MIN_SCALE
                    + (MAX_SCALE - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {
            view.setAlpha(MIN_FADE);
        }
    }
}
