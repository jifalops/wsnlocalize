package com.jifalops.toolbox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@link ResettingList} keeps a list of items and the times they were added.
 * When an item is added, it checks if {@link Trigger#reached(int, long)} is true and then
 * calls {@link LimitsCallback#onLimitsReached(List, long)}.<br />
 * Note: The timer does not start until the first item is added to the list,
 * and that the internal list is cleared after calling the callback.
 */
public class ResettingList<T> {
    public interface LimitsCallback<T> {
        void onLimitsReached(List<T> list, long time);
    }

    /**
     * {@link Trigger} has minimum and maximum counts and times.
     * {@link #reached} can be used to check if both minimums ({@link #minCount}, {@link #minTime}),
     * or either maximum ({@link #maxCount}, {@link #maxTime}) have been reached.
     */
    public static class Trigger {
        private final int minCount, maxCount;
        private final long minTime, maxTime;

        public Trigger(int minCount, long minTimeMillis, int maxCount, long maxTimeMillis) {
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

    private final Trigger trigger;
    private final LimitsCallback<T> callback;
    private final List<T> list = new ArrayList<>();
    private long start = 0;
    private Timer timer;

    public ResettingList(@NonNull Trigger trigger, @Nullable LimitsCallback<T> callback) {
        this.trigger = trigger;
        this.callback = callback;
    }

    /** @return true if the trigger limits were reached. */
    public boolean add(T item) {
        list.add(item);
        if (list.size() == 0) resetTimer();
        if (start == 0) start = System.nanoTime();
        long elapsed = (System.nanoTime() - start) / 1_000_000; // millis
        if (trigger.reached(list.size(), elapsed)) {
            callCallback(elapsed);
            return true;
        }
        return false;
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callCallback(trigger.maxTime);
            }
        }, trigger.maxTime);
    }

    private void callCallback(long elapsed) {
        if (callback != null) callback.onLimitsReached(list, elapsed);
        reset();
    }

    /** Revert to a fresh state, like when just created or after triggered. */
    public void reset() {
        list.clear();
        start = 0;
    }
}
