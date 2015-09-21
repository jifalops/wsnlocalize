package com.jifalops.toolbox.file;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NumberReaderWriter extends AbsTextReaderWriter {
    public interface NumbersReadListener {
        void onReadSucceeded(List<double[]> lines, int numberFormatExceptions);
        void onReadFailed(IOException e);
    }

    public NumberReaderWriter(File file) {
        super(file);
    }

    public boolean readNumbers(@NonNull final NumbersReadListener callback) {
        return readLines(new ReadListener<String>() {
            int numExceptions = 0;
            @Override
            public void onReadSucceeded(List<String> lines) {
                List<double[]> numbers = new ArrayList<>(lines.size());
                if (lines.size() > 0) {
                    int cols = lines.get(0).split(",").length;
                    String[] parts;
                    double[] nums;
                    for (String line : lines) {
                        nums = new double[cols];
                        parts = line.split(",");
                        for (int j = 0; j < cols; ++j) {
                            try {
                                nums[j] = Double.valueOf(parts[j]);
                            } catch (NumberFormatException e) {
                                ++numExceptions;
                            }
                        }
                        numbers.add(nums);
                    }
                }
                callback.onReadSucceeded(numbers, numExceptions);
            }

            @Override
            public void onReadFailed(IOException e) {
                callback.onReadFailed(e);
            }
        });
    }

    public void writeNumbers(@NonNull double[][] numbers, boolean append, @NonNull WriteListener callback) {
        List<String> lines = new ArrayList<>();
        StringBuilder sb;
        int cols = numbers[0].length;
        for (double[] row : numbers) {
            sb = new StringBuilder(cols * 2 - 1);
            for (int i = 0; i < cols; ++i) {
                if (i != 0) sb.append(",");
                sb.append(row[i]);
            }
            lines.add(sb.toString());
        }
        writeLines(lines, append, callback);
    }
}
