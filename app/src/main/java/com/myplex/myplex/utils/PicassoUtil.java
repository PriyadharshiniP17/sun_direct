package com.myplex.myplex.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.widget.ImageView;

import com.myplex.myplex.R;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Apalya on 10/27/2015.
 */
public class PicassoUtil {
    private static final String PICASSO_LOAD_TAG = "picasso_load_image";
    private WeakReference<Context> mWeakReference;
    private Picasso mPicasso;
    private static PicassoUtil sPicassoUtil;

    private PicassoUtil(Context context) {
        mWeakReference = new WeakReference<>(context);
        try {
            mPicasso = new Picasso.Builder(mWeakReference.get()).executor(Executors.newCachedThreadPool())
                    .memoryCache(new LruCache(getBytesForMemCache(60)))
                    .listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            exception.printStackTrace();
                        }
                    }).build();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //returns the given percentage of available memory in bytes
    private int getBytesForMemCache(int percent){
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        Activity activity =(Activity) (mWeakReference.get());
        ActivityManager activityManager = (ActivityManager)activity.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        double availableMemory= mi.availMem;

        return (int)(percent*availableMemory/100);
    }

    public static PicassoUtil with(Context context) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(context);
        }
        return sPicassoUtil;
    }

    public void load(String url, ImageView imageView, int defaultRes) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .fit()
                .noFade()
                .error(defaultRes)
                .config(Bitmap.Config.RGB_565)
                .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                .placeholder(defaultRes)
                .into(imageView);
    }
    public void load(String url, ImageView imageView, int defaultRes,int width,int height) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .resize(width,height)
                .noFade()
                .error(defaultRes)
                .config(Bitmap.Config.RGB_565)
                .placeholder(defaultRes)
                .into(imageView);
    }

    public void load(File url, ImageView imageView, Transformation transformation) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        if(imageView != null) {
            mPicasso.load(url)
                    .tag(PICASSO_LOAD_TAG)
                    .transform(transformation)
                    .placeholder(R.drawable.black)
                    .into(imageView);
        }else{
            mPicasso.load(url)
                    .tag(PICASSO_LOAD_TAG)
                    .transform(transformation)
                    ;
        }
    }

    public void loadCenterInside(String url, ImageView imageView, int defaultRes,int width,int height) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .resize(width,height)
                .noFade()
                .centerInside()
                .error(defaultRes)
                .config(Bitmap.Config.RGB_565)
                .placeholder(defaultRes)
                .into(imageView);
    }


    public void loadCenterCrop(String url, ImageView imageView, int defaultRes,int width,int height) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .resize(width,height)
                .noFade()
                .centerCrop()
                .error(defaultRes)
                .config(Bitmap.Config.RGB_565)
                .placeholder(defaultRes)
                .into(imageView);
    }

    public void loadBanner(String url, ImageView imageView, int defaultRes) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .noFade()
                .error(defaultRes)
                .placeholder(defaultRes)
                .into(imageView);
    }

    public void load(String url, ImageView imageView) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .noFade()
                .config(Bitmap.Config.RGB_565)
                .into(imageView);
    }


    public void loadWithMemoryPolicy(String url, Target imageView) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .noFade()
                .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                .config(Bitmap.Config.RGB_565)
                .priority(Picasso.Priority.HIGH)
                .into(imageView);
    }

    public void load(String url, Target imageView) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .noFade()
                .config(Bitmap.Config.RGB_565)
                .priority(Picasso.Priority.HIGH)
                .into(imageView);
    }


    public void load(int image, ImageView imageView,int placeholderImage) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(image)
                .tag(PICASSO_LOAD_TAG)
                .fit()
                .noFade()
                .placeholder(placeholderImage)
                .config(Bitmap.Config.RGB_565)
                .into(imageView);
    }
    public void loadOnlyScaleDown(int image, ImageView imageView,int placeholderImage) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(image)
                .tag(PICASSO_LOAD_TAG)

                .onlyScaleDown()
                .noFade()
                .placeholder(placeholderImage)
                .config(Bitmap.Config.RGB_565)
                .into(imageView);
    }
    public void loadOnlyScaleDown(String image, ImageView imageView,int placeholderImage,int width,int height) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(image)
                .tag(PICASSO_LOAD_TAG)

                .resize(width,height)
                .onlyScaleDown()

                .noFade()
                .placeholder(placeholderImage)
                .config(Bitmap.Config.RGB_565)
                .into(imageView);
    }
    public void loadOnlyScaleDown(int image, ImageView imageView,int placeholderImage,int width,int height) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(image)
                .tag(PICASSO_LOAD_TAG)
                .resize(width,height)
                .onlyScaleDown()
                .noFade()
                .placeholder(placeholderImage)
                .config(Bitmap.Config.RGB_565)
                .into(imageView);
    }
    public void load(int image, ImageView imageView, int defaultRes,int width,int height) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(image)
                .tag(PICASSO_LOAD_TAG)
                .resize(width,height)
                .noFade()
                .error(defaultRes)
                .config(Bitmap.Config.RGB_565)
                .placeholder(defaultRes)
                .into(imageView);
    }

    public void download(String url, @NonNull final Target target) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url)
                .tag(PICASSO_LOAD_TAG)
                .config(Bitmap.Config.RGB_565)
                .into(target);
    }

    public void pause() {
        pauseTag(PICASSO_LOAD_TAG);
    }

    public void resume() {
        resumeTag(PICASSO_LOAD_TAG);
    }

    private void pauseTag(String tag) {
        mPicasso.pauseTag(tag);
    }

    private void resumeTag(String tag) {
        mPicasso.resumeTag(tag);
    }

    public void fetch(String url) {
        if (sPicassoUtil == null) {
            sPicassoUtil = new PicassoUtil(mWeakReference.get());
        }
        mPicasso.load(url).fetch();
    }
}
