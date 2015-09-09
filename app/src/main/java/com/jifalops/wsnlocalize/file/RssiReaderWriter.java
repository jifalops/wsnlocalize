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
        void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records);
        void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(TextReaderWriter rw, List<String> lines) {
            if (lines.size() == 0) return;
            final List<RssiRecord> records = new ArrayList<>();
            for (String line : lines) {
                records.add(new RssiRecord(line.split(",")));
            }
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onRssiRecordsRead(RssiReaderWriter.this, records);
                }
            });
        }
        @Override
        public void onWriteCompleted(TextReaderWriter rw, final int linesWritten) {
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onRssiRecordsWritten(RssiReaderWriter.this, linesWritten);
                }
            });
        }
    };

    final RssiCallbacks callbacks;
    public RssiReaderWriter(File file, RssiCallbacks callbacks) {
        super(file);
        this.callbacks = callbacks;
        setIoCallbacks(ioCallbacks);
    }

    public boolean readRecords() {
        return readLines();
    }

    public void writeRecords(List<RssiRecord> records, boolean append) {
        List<String> lines = new ArrayList<>();
        for (RssiRecord r : records) {
            lines.add(r.toString());
        }
        writeLines(lines, append);
    }
}
