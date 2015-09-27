package com.jifalops.wsnlocalize.toolbox.debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.jifalops.wsnlocalize.toolbox.util.SimpleLog;

/**
 *
 */
public class DebugLog extends SimpleLog {
    static final String TAG = DebugLog.class.getSimpleName();
    private int logLevel = Log.VERBOSE, toastLevel = Log.VERBOSE;
    Context ctx;
    SharedPreferences prefs;

    public DebugLog(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        prefs = this.ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        logLevel = prefs.getInt("logLevel", logLevel);
        toastLevel = prefs.getInt("toastLevel", toastLevel);
    }

    public int getLogLevel() { return logLevel; }
    public int getToastLevel() { return toastLevel; }

    public void setLogLevel(int level) {
        if (logLevel == level) return;
        logLevel = level;
        prefs.edit().putInt("logLevel", logLevel).apply();
    }

    public void setToastLevel(int level) {
        if (toastLevel == level) return;
        toastLevel = level;
        prefs.edit().putInt("toastLevel", toastLevel).apply();
    }

    public void a(String msg) {
        add(Log.ASSERT, null, msg, null);
    }
    public void a(String tag, String msg) {
        add(Log.ASSERT, tag, msg, null);
    }
    public void a(String tag, String msg, Throwable tr) {
        add(Log.ASSERT, tag, msg, tr);
    }
    public void a(String msg, Throwable tr) {
        add(Log.ASSERT, null, msg, tr);
    }

    public void e(String msg) {
        add(Log.ERROR, null, msg, null);
    }
    public void e(String tag, String msg) {
        add(Log.ERROR, tag, msg, null);
    }
    public void e(String tag, String msg, Throwable tr) {
        add(Log.ERROR, tag, msg, tr);
    }
    public void e(String msg, Throwable tr) {
        add(Log.ERROR, null, msg, tr);
    }

    public void w(String msg) {
        add(Log.WARN, null, msg, null);
    }
    public void w(String tag, String msg) {
        add(Log.WARN, tag, msg, null);
    }
    public void w(String tag, String msg, Throwable tr) {
        add(Log.WARN, tag, msg, tr);
    }
    public void w(String msg, Throwable tr) {
        add(Log.WARN, null, msg, tr);
    }

    public void i(String msg) {
        add(Log.INFO, null, msg, null);
    }
    public void i(String tag, String msg) {
        add(Log.INFO, tag, msg, null);
    }
    public void i(String tag, String msg, Throwable tr) {
        add(Log.INFO, tag, msg, tr);
    }
    public void i(String msg, Throwable tr) {
        add(Log.INFO, null, msg, tr);
    }

    public void d(String msg) {
        add(Log.DEBUG, null, msg, null);
    }
    public void d(String tag, String msg) {
        add(Log.DEBUG, tag, msg, null);
    }
    public void d(String tag, String msg, Throwable tr) {
        add(Log.DEBUG, tag, msg, tr);
    }
    public void d(String msg, Throwable tr) {
        add(Log.DEBUG, null, msg, tr);
    }

    public void v(String msg) {
        add(Log.VERBOSE, null, msg, null);
    }
    public void v(String tag, String msg) {
        add(Log.VERBOSE, tag, msg, null);
    }
    public void v(String tag, String msg, Throwable tr) {
        add(Log.VERBOSE, tag, msg, tr);
    }
    public void v(String msg, Throwable tr) {
        add(Log.VERBOSE, null, msg, tr);
    }

    @Override
    public void add(String msg) {
        add(Log.VERBOSE, null, msg, null);
    }

    @Override
    public void add(int priority, String msg) {
        add(priority, null, msg, null);
    }

    private void add(int priority, String tag, String msg, Throwable tr) {
        String logmsg = null;
        if (priority >= logLevel || priority >= toastLevel) {
            if (tag == null) tag = lookupTag();
            if (tag == null) tag = TAG;
            logmsg = tag + ": " + msg + (tr == null ? "" : '\n' + tr.getMessage());
            super.add(priority, logmsg);
        }
        if (priority >= logLevel) {
            Log.println(priority, tag, msg + '\n' + Log.getStackTraceString(tr));
        }
        if (priority >= toastLevel) {
            Toast.makeText(ctx, logmsg, Toast.LENGTH_SHORT).show();
        }
    }

    private String lookupTag() {
        // Get the class two above current (assuming App calls this log).
        StackTraceElement ste = CallerInfo.getCaller(getClass().getName(), 2);
        if (ste == null) return null;
        String[] parts = ste.getClassName().split("\\.");
        if (parts.length == 0) return ste.getClassName();
        String[] inner = parts[parts.length - 1].split("\\$");
        if (inner.length == 0) return parts[parts.length - 1];
        return inner[inner.length - 1];
    }
}
