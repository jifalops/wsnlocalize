package com.jifalops.wsnlocalize.data;

/**
 * {@link SampleWindow} has minimum and maximum counts and times.
 * {@link #reached} can be used to check if both minimums ({@link #minCount}, {@link #minTime}),
 * or either maximum ({@link #maxCount}, {@link #maxTime}) have been reached.
 */
public class SampleWindow {
    public final int minCount, maxCount;
    public final long minTime, maxTime;

    public SampleWindow(int minCount, long minTimeMillis, int maxCount, long maxTimeMillis) {
        this.minCount = minCount;
        this.minTime = minTimeMillis;
        this.maxCount = maxCount;
        this.maxTime = maxTimeMillis;
    }

    /**
     * @return true if both minimums have been met, or either maximum has been met.
     */
    public boolean reached(int count, long time) {
        return (count >= minCount && time >= minTime)
                || count >= maxCount
                || time >= maxTime;
    }
}
