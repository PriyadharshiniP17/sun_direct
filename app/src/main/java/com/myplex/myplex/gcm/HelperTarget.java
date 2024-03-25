package com.myplex.myplex.gcm;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.myplex.myplex.model.CacheManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;

/**
 * Created by Apalya on 15-Mar-16.
 */

public class HelperTarget implements Target {
    interface TargetListener{

        void onTarget(Bitmap bitmap);
    }

    private static final String TAG = HelperTarget.class.getSimpleName();
    private WeakReference<TargetListener> onTargetListener;
    private static HelperTarget sTarget;

    private HelperTarget(){
        if (sTarget != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }
    public static HelperTarget getInstance(){
        synchronized (CacheManager.class) {
            if(sTarget == null) {
                sTarget = new HelperTarget();
            }
        }
        return sTarget;
    }

    void setOnTargetListener(WeakReference<TargetListener> onTargetListener) {
        this.onTargetListener = onTargetListener;
    }


    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        //Log.d(TAG, "HelperTarget: onBitmapLoaded - successfull");
        if(onTargetListener == null || onTargetListener.get() == null){
            return;
        }
        onTargetListener.get().onTarget(bitmap);
    }

    /**
     * Callback indicating the image could not be successfully loaded.
     * <p/>
     * <strong>Note:</strong> The passed {@link Drawable} may be {@code null} if none has been
     * specified via {@link RequestCreator#error(Drawable)}
     * or {@link RequestCreator#error(int)}.
     *
     * @param errorDrawable
     */
    @Override
    public void onBitmapFailed(Exception e,Drawable errorDrawable) {
        //Log.d(TAG, "HelperTarge: onBitmapFailed-> error");
        if(onTargetListener == null || onTargetListener.get() == null){
            return;
        }
        onTargetListener.get().onTarget(null);
    }

    /**
     * Callback invoked right before your request is submitted.
     * <p/>
     * <strong>Note:</strong> The passed {@link Drawable} may be {@code null} if none has been
     * specified via {@link RequestCreator#placeholder(Drawable)}
     * or {@link RequestCreator#placeholder(int)}.
     *
     * @param placeHolderDrawable
     */
    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        //Log.d(TAG, "HelperTarget: onPrepareLoad-> preparing to load");
    }

    @Override
    public int hashCode() {
        //Log.d(TAG, "HelperTarget: hashCode- ");
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        //Log.d(TAG, "HelperTarget: equals - ");
        return super.equals(o);
    }

    @Override
    protected void finalize() throws Throwable {
        //Log.d(TAG, "HelperTarget: finalize- ");
        super.finalize();
    }
}