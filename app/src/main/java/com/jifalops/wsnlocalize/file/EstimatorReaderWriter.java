package com.jifalops.wsnlocalize.file;

import com.jifalops.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.data.Estimator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EstimatorReaderWriter extends AbsTextReaderWriter {
    public interface EstimatorCallbacks {
        void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records);
        void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(AbsTextReaderWriter rw, List<String> lines) {
            final List<Estimator> records = new ArrayList<>();
            for (String line : lines) {
                try {
                    records.add(new Estimator(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            callbacks.onEstimatorRecordsRead(EstimatorReaderWriter.this, records);
        }
        @Override
        public void onWriteCompleted(AbsTextReaderWriter rw, final int linesWritten) {
            callbacks.onEstimatorRecordsWritten(EstimatorReaderWriter.this, linesWritten);
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
