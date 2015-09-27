package com.jifalops.wsnlocalize.data;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EstimatorsHelper {
    private static EstimatorsHelper instance;
    public static EstimatorsHelper getInstance() {
        if (instance == null) {
            instance = new EstimatorsHelper();
        }
        return instance;
    }

    private final Map<SampleListSourceInfo, List<DistanceEstimator>> estimators = new HashMap<>();
    private boolean loaded;
    private int numFiles, succeeded, failed;

    private EstimatorsHelper() {
        loadEstimators();
    }

    private void loadEstimators() {
        File dir = App.Files.getEstimatorsDir();
        if (dir.isDirectory()) {
            numFiles = dir.listFiles().length;
            estimators.clear();
            EstimatorReaderWriter rw;
            for (final File f : dir.listFiles()) {
                final SampleListSourceInfo info = new SampleListSourceInfo(f.getName());
                rw = new EstimatorReaderWriter(f);
                rw.readEstimators(new AbsTextReaderWriter.TypedReadListener<DistanceEstimator>() {
                    @Override
                    public void onReadSucceeded(List<DistanceEstimator> list, int typingExceptions) {
                        estimators.put(info, list);
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                });
            }
        } else {
            App.log().e("estimators directory invalid");
        }
    }

    private void checkLoaded() {
        loaded = (succeeded + failed) == numFiles;
        if (loaded && failed > 0) {
            App.log().e("Failed to load " + failed + " estimator files.");
        }
    }

    public boolean isLoaded() { return loaded; }

    public void addEstimator(SampleListSourceInfo info, DistanceEstimator estimator,
                     @Nullable AbsTextReaderWriter.WriteListener callback) {
        List<DistanceEstimator> list = estimators.get(info);
        if (list == null) {
            list = new ArrayList<>();
            estimators.put(info, list);
        }
        list.add(estimator);

        EstimatorReaderWriter rw = new EstimatorReaderWriter(
                new File(App.Files.getEstimatorsDir(), info.getFileName()));
        rw.writeEstimators(Collections.singletonList(estimator), true, callback);
    }
}
