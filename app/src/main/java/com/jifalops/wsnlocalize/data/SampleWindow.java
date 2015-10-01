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

    @Override
    public String toString() {
        return minCount +","+ minTime +","+ maxCount +","+ maxTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SampleWindow) {
            SampleWindow window = (SampleWindow) o;
            return minCount == window.minCount && minTime == window.minTime &&
                    maxCount == window.maxCount && maxTime == window.maxTime;
        } else return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
