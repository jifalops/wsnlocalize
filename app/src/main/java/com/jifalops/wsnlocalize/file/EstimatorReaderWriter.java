//package com.jifalops.wsnlocalize.file;
//
//import android.support.annotation.Nullable;
//
//import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
//import com.jifalops.wsnlocalize.toolbox.util.Lists;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// */
//public class EstimatorReaderWriter extends AbsTextReaderWriter {
//
//    public EstimatorReaderWriter(File file) {
//        super(file);
//    }
//
//    public boolean readEstimators(@Nullable final TypedReadListener<Estimator> callback) {
//        return readLines(new ReadListener() {
//            @Override
//            public void onReadSucceeded(List<String> lines) {
//                int exceptions = 0;
//                List<Estimator> estimators = new ArrayList<>();
//                for (String line : lines) {
//                    try {
//                        estimators.add(new Estimator(line));
//                    } catch (Exception e) {
//                        ++exceptions;
//                    }
//                }
//                if (callback != null) callback.onReadSucceeded(estimators, exceptions);
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                if (callback != null) callback.onReadFailed(e);
//            }
//        });
//    }
//
//    public void writeEstimators(List<Estimator> estimators, boolean append,
//                                @Nullable WriteListener callback) {
//        writeLines(Lists.toStrings(estimators), append, callback);
//    }
//}
