package com.jifalops.wsnlocalize.file;

import android.os.AsyncTask;

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
    IoCallbacks callbacks;

    protected TextReaderWriter(File file) {
        this(file, null);
    }
    public TextReaderWriter(File file, IoCallbacks callbacks) {
        this.file = file;
        this.callbacks = callbacks;
    }

    protected void setIoCallbacks(IoCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public boolean readLines() {
        if (file.exists()) {
            new AsyncTask<Void, Void, List<String>>() {
                @Override
                protected List<String> doInBackground(Void... params) {
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
                    return lines;
                }

                @Override
                protected void onPostExecute(List<String> lines) {
                    callbacks.onReadCompleted(TextReaderWriter.this, lines);
                }
            }.execute();
            return true;
        }
        return false;
    }

    public void writeLines(final List<String> lines, final boolean append) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int count = 0;
                BufferedWriter w = null;
                try {
                    w = new BufferedWriter(new FileWriter(file, append));
                    for (String line : lines) {
                        w.write(line);
                        w.newLine();
                        ++count;
                    }
                    w.flush();
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
                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                callbacks.onWriteCompleted(TextReaderWriter.this, count);
            }
        }.execute();
    }

    public void truncate() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BufferedWriter w = null;
                try {
                    w = new BufferedWriter(new FileWriter(file, false));
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
                return null;
            }
        }.execute();
    }
}
