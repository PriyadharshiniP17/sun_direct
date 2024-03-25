package viewpagerindicator;

import android.graphics.drawable.StateListDrawable;

public interface IconPagerAdapter {
    /**
     * Get icon representing the page at {@code index} in the com.myplex.sundirect.ui.adapter.
     */
    int getIconResId(int index);

    // From PagerAdapter
    int getCount();
    StateListDrawable getDynamicIcons(int index);
}
