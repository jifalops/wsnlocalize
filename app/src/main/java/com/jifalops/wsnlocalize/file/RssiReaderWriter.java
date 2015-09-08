package com.jifalops.wsnlocalize.file;

import com.jifalops.wsnlocalize.data.RssiRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiReaderWriter extends TextReaderWriter {
    public interface RssiCallbacks {
        /** Called on non-main thread. */
        void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records);
        /** Called on non-main thread. */
        void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(TextReaderWriter rw, List<String> lines) {
            List<RssiRecord> records = new ArrayList<>();
            for (String line : lines) {
                records.add(new RssiRecord(line.split(",")));
            }
            callbacks.onRssiRecordsRead(RssiReaderWriter.this, records);
        }
        @Override
        public void onWriteCompleted(TextReaderWriter rw, int linesWritten) {
            callbacks.onRssiRecordsWritten(RssiReaderWriter.this, linesWritten);
        }
    };

    final RssiCallbacks callbacks;
    public RssiReaderWriter(File file, RssiCallbacks callbacks) {
        super(file);
        this.callbacks = callbacks;
        setIoCallbacks(ioCallbacks);
    }

    public void readRecords() {
        readLines();
    }

    public void writeRecords(List<RssiRecord> records, boolean append) {
        List<String> lines = new ArrayList<>();
        for (RssiRecord r : records) {
            lines.add(r.toString());
        }
        writeLines(lines, append);
    }
}
