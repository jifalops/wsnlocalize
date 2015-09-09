package com.jifalops.wsnlocalize.file;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TextReaderWriter {
    public interface IoCallbacks {
        /** Called on non-main thread. */
        void onReadCompleted(TextReaderWriter rw, List<String> lines);
        /** Called on non-main thread. */
        void onWriteCompleted(TextReaderWriter rw, int linesWritten);
    }

    final File file;
    final HandlerThread thread;
    final Handler handler;
    protected final Handler creationThreadHandler = new Handler();
    IoCallbacks callbacks;

    protected TextReaderWriter(File file) {
        this(file, null);
    }
    public TextReaderWriter(File file, IoCallbacks callbacks) {
        this.file = file;
        this.callbacks = callbacks;
        thread = new HandlerThread(getClass().getName());
        handler = new Handler(thread.getLooper());
    }

    protected void setIoCallbacks(IoCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public boolean readLines() {
        if (file.exists()) {
            post(new Runnable() {
                @Override
                public void run() {
                    List<String> lines = new ArrayList<>();
                    BufferedReader r = null;
                    try {
                        r = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = r.readLine()) != null && line.length() > 0) {
                            lines.add(line);
                        }
                    } catch (FileNotFoundException e) {
                        // ignored
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (r != null) r.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    callbacks.onReadCompleted(TextReaderWriter.this, lines);
                }
            });
            return true;
        }
        return false;
    }

    public void writeLines(final List<String> lines, final boolean append) {
        post(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                BufferedWriter w = null;
                try {
                    w = new BufferedWriter(new FileWriter(file, append));
                    for (String line : lines) {
                        w.write(line);
                        w.newLine();
                        ++count;
                    }
                } catch (FileNotFoundException e) {
                    // ignored
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (w != null) w.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                callbacks.onWriteCompleted(TextReaderWriter.this, count);
            }
        });
    }

    public void close() {
        handler.removeCallbacksAndMessages(null);
        if (thread.getState() != Thread.State.NEW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                thread.quitSafely();
            } else {
                thread.quit();
            }
        }
    }

    private boolean post(Runnable r) {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
        return handler.post(r);
    }
}
