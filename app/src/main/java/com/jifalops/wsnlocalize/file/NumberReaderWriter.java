package com.jifalops.wsnlocalize.file;

import android.text.TextUtils;

import com.jifalops.wsnlocalize.data.RssiRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NumberReaderWriter extends TextReaderWriter {
    public interface NumberCallbacks {
        void onNumbersRead(NumberReaderWriter rw, List<double[]> records);
        void onNumbersWritten(NumberReaderWriter rw, int recordsWritten);
    }

    final IoCallbacks ioCallbacks = new IoCallbacks() {
        @Override
        public void onReadCompleted(TextReaderWriter rw, List<String> lines) {
            final List<double[]> list = new ArrayList<>();
            double[] nums;
            int len;
            for (String line : lines) {
                String[] p = line.split(",");
                len = p.length;
                nums = new double[len];
                for (int i = 0; i < len; ++i) {
                    nums[i] = Double.valueOf(p[i]);
                }
                list.add(nums);
            }
            creationThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onNumbersRead(NumberReaderWriter.this, list);
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

    public void readNumbers() {
        readLines();
    }

    public void writeNumbers(List<double[]> numbers, boolean append) {
        List<String> lines = new ArrayList<>();
        int len = numbers.get(0).length;
        StringBuilder sb;
        for (double[] a : numbers) {
            sb = new StringBuilder(len);
            for (int i = 0; i < len; ++i) {
                if (i != 0) sb.append(",");
                sb.append(a[i]);
            }
            lines.add(sb.toString());
        }
        writeLines(lines, append);
    }
}
