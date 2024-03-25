package com.myplex.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CardImageView extends ImageView {

	public int mCardId;
	public String mImageUrl;
	
	public void setDownloadedBitmap(Bitmap bm){
		if(bm == null){return;}
		setImageBitmap(bm);
	}
	public CardImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CardImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CardImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDetachedFromWindow() {		
//		setImageBitmap(null);
//		setImageDrawable(null);
		super.onDetachedFromWindow();
	}

}
