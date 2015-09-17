package com.jifalops.wsnlocalize.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ResettingList} keeps a list of items and the times they were added.
 * When an item is added, it checks if {@link Limits#reached(int, long)} is true and then
 * calls {@link LimitsCallback#onLimitsReached(List, long)}.
 * Note that it clears the internal list after calling the callback.
 */
public class ResettingList<T> {
    public interface LimitsCallback<T> {
        void onLimitsReached(List<T> list, long time);
    }

    private final Limits limits;
    private final LimitsCallback<T> callback;
    private final List<T> list = new ArrayList<>();
    private long start = 0;

    public ResettingList(@NonNull Limits limits, @Nullable LimitsCallback<T> callback) {
        this.limits = limits;
        this.callback = callback;
    }

    /** @return true if the trigger limits were reached. */
    public boolean add(T item) {
        if (start == 0) start = System.nanoTime();
        long elapsed = (System.nanoTime() - start) / 1_000_000; // millis
        list.add(item);
        if (limits.reached(list.size(), elapsed)) {
            if (callback != null) callback.onLimitsReached(list, elapsed);
            reset();
            return true;
        }
        return false;
    }

    /** Revert to a fresh state, like when just created or after triggered. */
    public void reset() {
        list.clear();
        start = 0;
    }

    /**
     * {@link Limits} has minimum and maximum counts and times.
     * {@link #reached} can be used to check if both minimums ({@link #minCount}, {@link #minTime}),
     * or either maximum ({@link #maxCount}, {@link #maxTime}) have been reached.
     */
    public static class Limits {
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
}
