package com.myplex.myplex.previewSeekBar;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class PicassoTransformation implements Transformation {
    private int x,width;
    private int y,height;
    private String cachekey;
    public PicassoTransformation(int xCo,int yCo,int widthThumbnail,int heightThumbnail,String cachekey) {
        y = yCo/* square / MAX_LINES*/;
        x = xCo /*square % MAX_COLUMNS*/;
        width = widthThumbnail;
        height = heightThumbnail;
        this.cachekey = cachekey;
    }
    @Override
    public Bitmap transform(Bitmap source) {
        if(width>0 && height>0) {
            if (x+width >source.getWidth()){//handle x+width more than source width exception
                return null;
            }
            Bitmap result = Bitmap.createBitmap(source,x,y,width,height);
            if (result != source) {
                source.recycle();
            }
            return result;
        }else{
            return null;
        }
    }

    @Override
    public String key() {
        return cachekey;
    }
}
