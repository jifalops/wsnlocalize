package com.jifalops.wsnlocalize.data.helper;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.RssiSample;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.toolbox.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EstimatesHelper {
    private static EstimatesHelper instance;
    public static EstimatesHelper getInstance() {
        if (instance == null) {
            instance = new EstimatesHelper();
        }
        return instance;
    }
    
    public static class Estimate {
        RssiSample sample;
        double estimate;
        double error;

        public Estimate(RssiSample sample, double estimate) {
            this.sample = sample;
            this.estimate = estimate;
            error = (estimate - sample.distance) / sample.distance;
        }

        public Estimate(double[] a) {
            int len = a.length;
            error = a[len - 1];
            estimate = a[len - 2];
            sample = new RssiSample(a);
        }

        double[] toArray() {
            return Arrays.concat(sample.toArray(), new double[] {estimate, error});
        }

        static List<Estimate> fromDoubleList(List<double[]> list) {
            List<Estimate> estimates = new ArrayList<>();
            for (double[] a : list) {
                estimates.add(new Estimate(a));
            }
            return estimates;
        }
    }

    public static class EstimatesInfo {
        public final List<Estimate>
                psoTimed = new ArrayList<>(),
                psoUntimed = new ArrayList<>(),
                deTimed = new ArrayList<>(),
                deUntimed = new ArrayList<>(),
                depsoTimed = new ArrayList<>(),
                depsoUntimed = new ArrayList<>();
    }

    private final Map<DataFileInfo, Map<Integer, EstimatesInfo>>
        bt = new HashMap<>(),
        btle = new HashMap<>(),
        wifi = new HashMap<>(),
        wifi5g = new HashMap<>();


    private boolean loaded;
    private int numFiles, succeeded, failed;

    private InfoFileHelper helper;

    private EstimatesHelper() {
        helper = InfoFileHelper.getInstance();

        numFiles = helper.getBt().size() + helper.getBtle().size() +
                helper.getWifi().size() + helper.getWifi5g().size();

        int[] numEstimators;
        Map<Integer, EstimatesInfo> estimateInfoMap;
        
        for (DataFileInfo info : helper.getBt()) {
            numEstimators = App.Files.getEstimatesFilesNumEstimators(App.SIGNAL_BT, info.id);
            numFiles += numEstimators.length * 6;
            estimateInfoMap = new HashMap<>();
            for (int n : numEstimators) {
                estimateInfoMap.put(n, new EstimatesInfo());
            }
            bt.put(info, estimateInfoMap);
        }
        for (DataFileInfo info : helper.getBtle()) {
            numEstimators = App.Files.getEstimatesFilesNumEstimators(App.SIGNAL_BTLE, info.id);
            numFiles += numEstimators.length * 6;
            estimateInfoMap = new HashMap<>();
            for (int n : numEstimators) {
                estimateInfoMap.put(n, new EstimatesInfo());
            }
            btle.put(info, estimateInfoMap);
        }
        for (DataFileInfo info : helper.getWifi()) {
            numEstimators = App.Files.getEstimatesFilesNumEstimators(App.SIGNAL_WIFI, info.id);
            numFiles += numEstimators.length * 6;
            estimateInfoMap = new HashMap<>();
            for (int n : numEstimators) {
                estimateInfoMap.put(n, new EstimatesInfo());
            }
            wifi.put(info, estimateInfoMap);
        }
        for (DataFileInfo info : helper.getWifi5g()) {
            numEstimators = App.Files.getEstimatesFilesNumEstimators(App.SIGNAL_WIFI5G, info.id);
            numFiles += numEstimators.length * 6;
            estimateInfoMap = new HashMap<>();
            for (int n : numEstimators) {
                estimateInfoMap.put(n, new EstimatesInfo());
            }
            wifi5g.put(info, estimateInfoMap);
        }
        
        NumberReaderWriter rw;
        
        for (final DataFileInfo info : bt.keySet()) {
            for (final int estimators : bt.get(info).keySet()) {
                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BT, App.NN_PSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        bt.get(info).get(estimators).psoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BT, App.NN_PSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        bt.get(info).get(estimators).psoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BT, App.NN_DE, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        bt.get(info).get(estimators).deUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BT, App.NN_DE, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        bt.get(info).get(estimators).deTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BT, App.NN_DEPSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        bt.get(info).get(estimators).depsoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BT, App.NN_DEPSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        bt.get(info).get(estimators).depsoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;
            }
        }



        for (final DataFileInfo info : btle.keySet()) {
            for (final int estimators : btle.get(info).keySet()) {
                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BTLE, App.NN_PSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        btle.get(info).get(estimators).psoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BTLE, App.NN_PSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        btle.get(info).get(estimators).psoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BTLE, App.NN_DE, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        btle.get(info).get(estimators).deUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BTLE, App.NN_DE, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        btle.get(info).get(estimators).deTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BTLE, App.NN_DEPSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        btle.get(info).get(estimators).depsoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_BTLE, App.NN_DEPSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        btle.get(info).get(estimators).depsoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;
            }
        }



        for (final DataFileInfo info : wifi.keySet()) {
            for (final int estimators : wifi.get(info).keySet()) {
                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI, App.NN_PSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi.get(info).get(estimators).psoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI, App.NN_PSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi.get(info).get(estimators).psoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI, App.NN_DE, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi.get(info).get(estimators).deUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI, App.NN_DE, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi.get(info).get(estimators).deTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI, App.NN_DEPSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi.get(info).get(estimators).depsoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI, App.NN_DEPSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi.get(info).get(estimators).depsoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;
            }
        }



        for (final DataFileInfo info : wifi5g.keySet()) {
            for (final int estimators : wifi5g.get(info).keySet()) {
                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI5G, App.NN_PSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi5g.get(info).get(estimators).psoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI5G, App.NN_PSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi5g.get(info).get(estimators).psoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI5G, App.NN_DE, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi5g.get(info).get(estimators).deUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI5G, App.NN_DE, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi5g.get(info).get(estimators).deTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI5G, App.NN_DEPSO, info.id, estimators, false));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi5g.get(info).get(estimators).depsoUntimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;


                rw = new NumberReaderWriter(App.Files.getEstimatesFile(
                        App.SIGNAL_WIFI5G, App.NN_DEPSO, info.id, estimators, true));

                if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        wifi5g.get(info).get(estimators).depsoTimed.addAll(Estimate.fromDoubleList(list));
                        ++succeeded;
                        checkLoaded();
                    }

                    @Override
                    public void onReadFailed(IOException e) {
                        ++failed;
                        checkLoaded();
                    }
                })) ++failed;
            }
        }
        

        checkLoaded();
    }

    private void checkLoaded() {
        loaded = (succeeded + failed) == numFiles;
        if (loaded && failed > 0) {
            App.log().e("Failed to load " + failed + " estimator files.");
        }
    }

    public boolean isLoaded() { return loaded; }

    public void add(Estimate estimate,
                    DataFileInfo info, String signalType, String nnType, int estimators, boolean timed,
                     @Nullable AbsTextReaderWriter.WriteListener callback) {
        List<Estimate> list = null;
        switch (signalType) {
            case App.SIGNAL_BT:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? bt.get(info).get(estimators).psoTimed : bt.get(info).get(estimators).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? bt.get(info).get(estimators).deTimed : bt.get(info).get(estimators).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? bt.get(info).get(estimators).depsoTimed : bt.get(info).get(estimators).depsoUntimed; break;
                }
                break;
            case App.SIGNAL_BTLE:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? btle.get(info).get(estimators).psoTimed : btle.get(info).get(estimators).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? btle.get(info).get(estimators).deTimed : btle.get(info).get(estimators).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? btle.get(info).get(estimators).depsoTimed : btle.get(info).get(estimators).depsoUntimed; break;
                }
                break;
            case App.SIGNAL_WIFI:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? wifi.get(info).get(estimators).psoTimed : wifi.get(info).get(estimators).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? wifi.get(info).get(estimators).deTimed : wifi.get(info).get(estimators).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? wifi.get(info).get(estimators).depsoTimed : wifi.get(info).get(estimators).depsoUntimed; break;
                }
                break;
            case App.SIGNAL_WIFI5G:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? wifi5g.get(info).get(estimators).psoTimed : wifi5g.get(info).get(estimators).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? wifi5g.get(info).get(estimators).deTimed : wifi5g.get(info).get(estimators).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? wifi5g.get(info).get(estimators).depsoTimed : wifi5g.get(info).get(estimators).depsoUntimed; break;
                }
                break;
        }
       
        if (list != null) {
            list.add(estimate);
        } else {
            App.log().e("Unknown list for estimate");
        }
        
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getEstimatesFile(signalType, nnType, info.id, estimators, timed));
        rw.writeNumbers(Collections.singletonList(estimate.toArray()), true, callback);
    }

}
