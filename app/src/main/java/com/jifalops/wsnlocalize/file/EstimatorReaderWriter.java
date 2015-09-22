package com.jifalops.wsnlocalize.file;

import com.jifalops.wsnlocalize.data.DistanceEstimator;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.util.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EstimatorReaderWriter extends AbsTextReaderWriter {

    public EstimatorReaderWriter(File file) {
        super(file);
    }

    public boolean readEstimators(final TypedReadListener<DistanceEstimator> callback) {
        return readLines(new ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                int exceptions = 0;
                List<DistanceEstimator> estimators = new ArrayList<>();
                for (String line : lines) {
                    try {
                        estimators.add(new DistanceEstimator(line));
                    } catch (Exception e) {
                        ++exceptions;
                    }
                }
                callback.onReadSucceeded(estimators, exceptions);
            }

            @Override
            public void onReadFailed(IOException e) {
                callback.onReadFailed(e);
            }
        });
    }

    public void writeEstimators(List<DistanceEstimator> estimators, boolean append, WriteListener callback) {
        writeLines(Lists.toString(estimators), append, callback);
    }
}
