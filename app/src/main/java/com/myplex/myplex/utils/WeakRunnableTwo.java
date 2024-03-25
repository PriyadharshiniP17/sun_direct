package com.myplex.myplex.utils;

import java.lang.ref.WeakReference;

public abstract class WeakRunnableTwo<T, U> implements Runnable {
    private WeakReference<T> weakReferenceOne;
    private WeakReference<U> weakReferenceTwo;
    protected WeakRunnableTwo(WeakReference<T> reference, WeakReference<U> referencetwo) {
        this.weakReferenceOne = reference;
        this.weakReferenceTwo = referencetwo;
    }

    protected WeakRunnableTwo(T reference, U referenceTwo) {
        this.weakReferenceOne = new WeakReference<>(reference);
        this.weakReferenceTwo = new WeakReference<>(referenceTwo);
    }

    protected abstract void safeRun(T referenceOne, U referenceTwo);

    @Override
    public void run() {
//        if (weakReferenceOne == null || weakReferenceOne.get() == null || weakReferenceTwo == null || weakReferenceTwo.get() == null) {
//            SDKLogger.debug("weakreference is garbage collected");
//            return;
//        }
        safeRun(weakReferenceOne.get(), weakReferenceTwo.get());
    }
}
