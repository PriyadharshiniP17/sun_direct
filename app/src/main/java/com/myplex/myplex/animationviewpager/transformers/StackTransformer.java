package com.myplex.myplex.animationviewpager.transformers;

import androidx.core.view.ViewCompat;
import android.view.View;

public class StackTransformer extends BaseTransformer {

	@Override
	protected void onTransform(View view, float position) {
		ViewCompat.setTranslationX(view,position < 0 ? 0f : -view.getWidth() * position);

	}

}
