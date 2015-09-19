package com.jifalops.wsnlocalize.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NumberReaderWriter extends TextReaderWriter {
    public interface NumberCallbacks {
        void onNumbersRead(NumberReaderWriter rw, double[][] numbers);
        void onNumbersWritten(NumberReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(TextReaderWriter rw, List<String> lines) {
            int rows = lines.size();
//            if (rows == 0) { return; }
            int cols = lines.get(0).split(",").length;
            final double[][] numbers = new double[rows][cols];
            for (int i = 0; i < rows; ++i) {
                String[] p = lines.get(i).split(",");
                for (int j = 0; j < cols; ++j) {
                    try {
                        numbers[i][j] = Double.valueOf(p[j]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onNumbersRead(NumberReaderWriter.this, numbers);
                }
            });
        }
        @Override
        public void onWriteCompleted(TextReaderWriter rw, final int linesWritten) {
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onNumbersWritten(NumberReaderWriter.this, linesWritten);
                }
            });
        }
    };

    final NumberCallbacks callbacks;
    public NumberReaderWriter(File file, NumberCallbacks callbacks) {
        super(file);
        this.callbacks = callbacks;
        setIoCallbacks(ioCallbacks);
    }

    public boolean readNumbers() {
        return readLines();
    }

    public void writeNumbers(double[][] numbers, boolean append) {
        List<String> lines = new ArrayList<>();
        StringBuilder sb;
        int cols = numbers[0].length;
        for (double[] row : numbers) {
            sb = new StringBuilder(cols*2-1);
            for (int i = 0; i < cols; ++i) {
                if (i != 0) sb.append(",");
                sb.append(row[i]);
            }
            lines.add(sb.toString());
        }
        writeLines(lines, append);
    }
}
