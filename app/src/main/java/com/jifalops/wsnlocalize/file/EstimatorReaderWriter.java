package com.jifalops.wsnlocalize.file;

import com.jifalops.wsnlocalize.data.Estimator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EstimatorReaderWriter extends TextReaderWriter {
    public interface EstimatorCallbacks {
        void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records);
        void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(TextReaderWriter rw, List<String> lines) {
            if (lines.size() == 0) return;
            final List<Estimator> records = new ArrayList<>();
            for (String line : lines) {
                try {
                    records.add(new Estimator(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onEstimatorRecordsRead(EstimatorReaderWriter.this, records);
                }
            });
        }
        @Override
        public void onWriteCompleted(TextReaderWriter rw, final int linesWritten) {
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onEstimatorRecordsWritten(EstimatorReaderWriter.this, linesWritten);
                }
            });
        }
    };

    final EstimatorCallbacks callbacks;
    public EstimatorReaderWriter(File file, EstimatorCallbacks callbacks) {
        super(file);
        this.callbacks = callbacks;
        setIoCallbacks(ioCallbacks);
    }

    public boolean readRecords() {
        return readLines();
    }

    public void writeRecords(List<Estimator> records, boolean append) {
        List<String> lines = new ArrayList<>();
        for (Estimator r : records) {
            lines.add(r.toString());
        }
        writeLines(lines, append);
    }
}
