package com.myplex.myplex.utils;

import java.lang.ref.WeakReference;

public abstract class WeakRunnable<T> implements Runnable {
    private WeakReference<T> weakReference;

    protected WeakRunnable(WeakReference<T> reference) {
        this.weakReference = reference;
    }

    protected WeakRunnable(T reference) {
        this.weakReference = new WeakReference<>(reference);
    }

    protected abstract void safeRun(T var1);

    @Override
    public void run() {
//        if (weakReference == null || weakReference.get() == null) {
//            SDKLogger.debug("weakreference is garbage collected");
//            return;
//        }
        safeRun(weakReference.get());
    }
}
