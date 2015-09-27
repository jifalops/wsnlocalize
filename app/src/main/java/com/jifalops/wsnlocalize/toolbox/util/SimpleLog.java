package com.jifalops.wsnlocalize.toolbox.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains a list of messages. Each message is automatically timestamped with
 * {@link System#currentTimeMillis()} and can be tagged with an integer for the
 * priority of that message.
 */
public class SimpleLog {
    public static class LogItem implements Comparable<LogItem> {
        public final long time = System.currentTimeMillis();
        public final int priority;
        public final String msg;
        private LogItem(int priority, String msg) {
            this.priority = priority;
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

    public void add(String msg) { add(0, msg); }
    public void add(int priority, String msg) {
        log.add(new LogItem(priority, msg));
    }

    public LogItem get(int index) {
        return log.get(index);
    }

    public Iterator<LogItem> iterator() {
        return log.iterator();
    }

    public List<LogItem> getBypriority(int priority, boolean includeGreaterpriority) {
        List<LogItem> list = new ArrayList<>();
        if (includeGreaterpriority) {
            for (LogItem li : log) {
                if (li.priority >= priority) list.add(li);
            }
        } else {
            for (LogItem li : log) {
                if (li.priority == priority) list.add(li);
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
