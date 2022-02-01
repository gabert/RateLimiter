package com.github.gabert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Semaphore;

/**
 * Simple Throttling control library
 */
public class RateLimiter {
    private final int numOfCalls;
    private final RingBuffer<LocalDateTime> ringBuffer;
    private final TemporalUnit temporalUnit;
    private final int timeWindow;
    private final Semaphore mutex = new Semaphore(1);

    /**
     * Parameters control number of calls per time window. Time window unit is controlled by TemporalUnit
     *
     * @param numOfCalls
     * @param timeWindow
     * @param temporalUnit
     */
    public RateLimiter(int numOfCalls, int timeWindow, TemporalUnit temporalUnit) {
        this.numOfCalls = numOfCalls;
        this.ringBuffer = new RingBuffer<>(numOfCalls);
        this.timeWindow = timeWindow;
        this.temporalUnit = temporalUnit;

        init();
    }

    private void init() {
        for (int i = 0; i < numOfCalls; i++) {
            ringBuffer.offer(LocalDateTime.now().minus(timeWindow, temporalUnit));
        }
    }

    /**
     * The release method permits signals possible next execution.
     * If the next execution is not possible, the method will cause the calling thread to sleep for a necessary time
     * so next release is possible.
     *
     * The method is thread safe. When used in multithreaded environment the method tries to acquire lock. If lock
     * was successful the method returns true signalling the release is possible. Otherwise, the method return false.
     *
     * @return boolean
     */
    public boolean release() {
        if ( ! mutex.tryAcquire() ) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = ringBuffer.take().plus(timeWindow, temporalUnit);

        if ( now.isBefore(next) ) {
            try {
                long sleepTime = ChronoUnit.MILLIS.between(now, next);

                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {}
        }

        ringBuffer.offer(LocalDateTime.now());

        mutex.release();

        return true;
    }
}
