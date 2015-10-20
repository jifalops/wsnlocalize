package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.toolbox.util.ResettingList;

/**
 * {@link SampleWindow} has minimum and maximum counts and times.
 * {@link #reached} can be used to check if both minimums ({@link #minCount}, {@link #minTime}),
 * or either maximum ({@link #maxCount}, {@link #maxTime}) have been reached.
 */
public class SampleWindow extends ResettingList.Trigger {
    public SampleWindow(int minCount, long minTimeMillis, int maxCount, long maxTimeMillis) {
        super(minCount, minTimeMillis, maxCount, maxTimeMillis);
    }
}
