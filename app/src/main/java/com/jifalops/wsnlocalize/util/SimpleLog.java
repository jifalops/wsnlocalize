package com.jifalops.wsnlocalize.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SimpleLog {
    public static class LogItem implements Comparable<LogItem> {
        public final long time = System.currentTimeMillis();
        public final int importance;
        public final String msg;
        private LogItem(int importance, String msg) {
            this.importance = importance;
            this.msg = msg;
        }

        @Override
        public int compareTo(LogItem another) {
            if (time < another.time) return -1;
            if (time > another.time) return 1;
            return 0;
        }
    }

    private final List<LogItem> log = new ArrayList<>();

    public int size() {
        return log.size();
    }

    public void add(int importance, String msg) {
        log.add(new LogItem(importance, msg));
    }

    public LogItem get(int index) {
        return log.get(index);
    }

    public List<LogItem> getAll() {
        return log;
    }

    public List<LogItem> getByImportance(int importance, boolean includeGreaterImportance) {
        List<LogItem> list = new ArrayList<>();
        if (includeGreaterImportance) {
            for (LogItem li : log) {
                if (li.importance >= importance) list.add(li);
            }
        } else {
            for (LogItem li : log) {
                if (li.importance == importance) list.add(li);
            }
        }
        return list;
    }

    /** @return all items before or after the specified time */
    public List<LogItem> getByTime(long systemTimeMillis, boolean before) {
        List<LogItem> list = new ArrayList<>();
        if (before) {
            for (LogItem li : log) {
                if (li.time < systemTimeMillis) list.add(li);
//                else break; // log is always chronological.
            }
        } else {
            for (LogItem li : log) {
                if (li.time > systemTimeMillis) list.add(li);
            }
        }
        return list;
    }

    /** Combines logs in chronological order. */
    public static SimpleLog combine(SimpleLog... logs) {
        SimpleLog combined = new SimpleLog();
        for (SimpleLog log : logs) {
            combined.log.addAll(log.log);
        }
        Collections.sort(combined.log);
        return combined;
    }
}
