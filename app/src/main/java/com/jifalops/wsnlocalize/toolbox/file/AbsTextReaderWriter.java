package com.jifalops.wsnlocalize.toolbox.file;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

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
 * Class for typical read-write operations on a plain text file.
 * Internally uses a {@link BufferedReader}, {@link BufferedWriter}, and {@link AsyncTask}s
 * along with synchronization on {@link #file}. This means you can read and write from the same
 * instance of {@link #file} without worry. However, it does not use java
 * {@link java.nio.channels.FileLock}s.
 *
 * Tip: you can write to a temp file and then rename for general async reads and writes on the same
 * file. The rename operation is atomic on most OSes. (http://stackoverflow.com/a/697436/884522)
 */
public abstract class AbsTextReaderWriter {
    public interface ReadListener {
        void onReadSucceeded(List<String> lines);
        void onReadFailed(IOException e);
    }
    public interface TypedReadListener<T> {
        void onReadSucceeded(List<T> list, int typingExceptions);
        void onReadFailed(IOException e);
    }
    public interface WriteListener {
        void onWriteSucceed(int linesWritten);
        void onWriteFailed(IOException e);
    }

    protected final File file;

    public AbsTextReaderWriter(@NonNull File file) {
        this.file = file;
    }

    /**
     * Read all lines from the file in a temporary thread.
     * @return true if a read is attempted (file exists).
     */
    protected boolean readLines(@NonNull final ReadListener callback) {
        if (file.exists()) {
            new AsyncTask<Void, Void, List<String>>() {
                IOException ioe = null;
                @Override
                protected List<String> doInBackground(Void... params) {
                    List<String> lines = new ArrayList<>();
                    BufferedReader r = null;
                    synchronized (file) {
                        try {
                            r = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = r.readLine()) != null && line.length() > 0) {
                                lines.add(line);
                            }
                        } catch (FileNotFoundException e) {
                            // ignored
                        } catch (IOException e) {
                            ioe = e;
                        } finally {
                            try {
                                if (r != null) r.close();
                            } catch (IOException e) {
                                ioe = e;
                            }
                        }
                    }
                    return lines;
                }

                @Override
                protected void onPostExecute(List<String> lines) {
                    if (ioe != null) {
                        callback.onReadFailed(ioe);
                    } else {
                        callback.onReadSucceeded(lines);
                    }
                }
            }.execute();
            return true;
        }
        return false;
    }

    /**
     * Write several lines to the file in a temporary thread.
     */
    protected void writeLines(final List<String> lines, final boolean append,
                           @NonNull final WriteListener callback) {
        new AsyncTask<Void, Void, Integer>() {
            IOException ioe = null;
            @Override
            protected Integer doInBackground(Void... params) {
                int count = 0;
                BufferedWriter w = null;
                synchronized (file) {
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
                        ioe = e;
                    } finally {
                        try {
                            if (w != null) w.close();
                        } catch (IOException e) {
                            ioe = e;
                        }
                    }
                }
                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                if (ioe == null) {
                    callback.onWriteSucceed(count);
                } else {
                    callback.onWriteFailed(ioe);
                }
            }
        }.execute();
    }

    /** Note: {@link IOException}s are not propagated. */
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
