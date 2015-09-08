package com.jifalops.wsnlocalize.file;

import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WindowReaderWriter extends TextReaderWriter {
    public interface WindowCallbacks {
        /** Called on non-main thread. */
        void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records);
        /** Called on non-main thread. */
        void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(TextReaderWriter rw, List<String> lines) {
            List<WindowRecord> records = new ArrayList<>();
            for (String line : lines) {
                records.add(new WindowRecord(line.split(",")));
            }
            callbacks.onWindowRecordsRead(WindowReaderWriter.this, records);
        }
        @Override
        public void onWriteCompleted(TextReaderWriter rw, int linesWritten) {
            callbacks.onWindowRecordsWritten(WindowReaderWriter.this, linesWritten);
        }
    };

    final WindowCallbacks callbacks;
    public WindowReaderWriter(File file, WindowCallbacks callbacks) {
        super(file);
        this.callbacks = callbacks;
        setIoCallbacks(ioCallbacks);
    }

    public void readRecords() {
        readLines();
    }

    public void writeRecords(List<WindowRecord> records, boolean append) {
        List<String> lines = new ArrayList<>();
        for (WindowRecord r : records) {
            lines.add(r.toString());
        }
        writeLines(lines, append);
    }
}
