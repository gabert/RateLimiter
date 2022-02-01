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
     * If release method returns true the lock acquire was successful. The false means lock was not successful
     * in a multithreaded environment. I release is not possible due to number of calls exceeded number of calls
     * within a given timeframe, the RateLimiter will pause the thread until release is possible.
     *
     * @return
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
