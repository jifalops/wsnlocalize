package com.jifalops.wsnlocalize.data;

/**
 *
 */
public class Limits {
    private final int minCount, maxCount;
    private final long minTime, maxTime;

    public Limits(int minCount, long minTime, int maxCount, long maxTime) {
        this.minCount = minCount;
        this.minTime = minTime;
        this.maxCount = maxCount;
        this.maxTime = maxTime;
    }

    public boolean reached(int count, long time) {
        return (count >= minCount && time >= minTime)
                || count >= maxCount
                || time >= maxTime;
    }
}
