package com.jifalops.wsnlocalize.toolbox.file;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NumberReaderWriter extends AbsTextReaderWriter {

    public NumberReaderWriter(File file) {
        super(file);
    }

    public boolean readNumbers(@Nullable final TypedReadListener<double[]> callback) {
        return readLines(new ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                int numExceptions = 0;
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
                            } catch (Exception e) {
                                ++numExceptions;
                            }
                        }
                        numbers.add(nums);
                    }
                }
                if (callback != null) callback.onReadSucceeded(numbers, numExceptions);
            }

            @Override
            public void onReadFailed(IOException e) {
                if (callback != null) callback.onReadFailed(e);
            }
        });
    }

    public void writeNumbers(List<double[]> numbers, boolean append, @Nullable WriteListener callback) {
        List<String> lines = new ArrayList<>();
        StringBuilder sb;
        int cols = numbers.get(0).length;
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
