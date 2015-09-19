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
            final List<RssiRecord> records = new ArrayList<>();
            for (String line : lines) {
                try {
                    records.add(new RssiRecord(line.split(",")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            callbacks.onRssiRecordsRead(RssiReaderWriter.this, records);
        }
        @Override
        public void onWriteCompleted(TextReaderWriter rw, final int linesWritten) {
            callbacks.onRssiRecordsWritten(RssiReaderWriter.this, linesWritten);
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
