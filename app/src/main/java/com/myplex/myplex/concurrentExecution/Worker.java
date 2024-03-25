package com.myplex.myplex.concurrentExecution;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker implements Runnable {

    private AtomicBoolean started;
    private Runnable task;
    private Thread thread;
    private CountDownLatch latch;

    public Worker(Runnable task, CountDownLatch latch) {
        this.latch = latch;
        this.task = task;
        started = new AtomicBoolean(false);
        thread = new Thread(this);
    }

    public void start() {
        if (!started.getAndSet(true)) {
            thread.start();
        }
    }

    @Override
    public void run() {
        task.run();
        latch.countDown();
    }
}
