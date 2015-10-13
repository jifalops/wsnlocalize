package com.jifalops.wsnlocalize.data.helper;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TrainingResults;

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

    public static class EstimatorInfo {
        public final List<TrainingResults>
                psoTimed = new ArrayList<>(),
                psoUntimed = new ArrayList<>(),
                deTimed = new ArrayList<>(),
                deUntimed = new ArrayList<>(),
                depsoTimed = new ArrayList<>(),
                depsoUntimed = new ArrayList<>();
    }

    private final Map<DataFileInfo, EstimatorInfo>
        bt = new HashMap<>(),
        btle = new HashMap<>(),
        wifi = new HashMap<>(),
        wifi5g = new HashMap<>();


    private boolean loaded;
    private int numFiles, succeeded, failed;

    private InfoFileHelper helper;

    private EstimatorsHelper() {
        helper = InfoFileHelper.getInstance();
        numFiles = (helper.getBt().size() + helper.getBtle().size() +
                helper.getWifi().size() + helper.getWifi5g().size()) * 6;

        for (DataFileInfo info : helper.getBt()) {
            bt.put(info, new EstimatorInfo());
        }
        for (DataFileInfo info : helper.getBtle()) {
            btle.put(info, new EstimatorInfo());
        }
        for (DataFileInfo info : helper.getWifi()) {
            wifi.put(info, new EstimatorInfo());
        }
        for (DataFileInfo info : helper.getWifi5g()) {
            wifi5g.put(info, new EstimatorInfo());
        }
        
        NumberReaderWriter rw;
        
        for (final DataFileInfo info : helper.getBt()) {
            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BT, App.NN_PSO, info.id, false));
            
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.get(info).psoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BT, App.NN_PSO, info.id, true));
            
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.get(info).psoTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;

            
            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BT, App.NN_DE, info.id, false));
            
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.get(info).deUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BT, App.NN_DE, info.id, true));
            
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.get(info).deTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BT, App.NN_DEPSO, info.id, false));
            
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.get(info).depsoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BT, App.NN_DEPSO, info.id, true));
            
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.get(info).depsoTimed.addAll(TrainingResults.fromDoubleList(list));
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


        
        for (final DataFileInfo info : helper.getBtle()) {
            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BTLE, App.NN_PSO, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.get(info).psoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BTLE, App.NN_PSO, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.get(info).psoTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BTLE, App.NN_DE, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.get(info).deUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BTLE, App.NN_DE, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.get(info).deTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BTLE, App.NN_DEPSO, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.get(info).depsoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_BTLE, App.NN_DEPSO, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.get(info).depsoTimed.addAll(TrainingResults.fromDoubleList(list));
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



        for (final DataFileInfo info : helper.getWifi()) {
            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI, App.NN_PSO, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.get(info).psoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI, App.NN_PSO, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.get(info).psoTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI, App.NN_DE, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.get(info).deUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI, App.NN_DE, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.get(info).deTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI, App.NN_DEPSO, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.get(info).depsoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI, App.NN_DEPSO, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.get(info).depsoTimed.addAll(TrainingResults.fromDoubleList(list));
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



        for (final DataFileInfo info : helper.getWifi5g()) {
            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI5G, App.NN_PSO, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.get(info).psoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI5G, App.NN_PSO, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.get(info).psoTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI5G, App.NN_DE, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.get(info).deUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI5G, App.NN_DE, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.get(info).deTimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI5G, App.NN_DEPSO, info.id, false));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.get(info).depsoUntimed.addAll(TrainingResults.fromDoubleList(list));
                    ++succeeded;
                    checkLoaded();
                }

                @Override
                public void onReadFailed(IOException e) {
                    ++failed;
                    checkLoaded();
                }
            })) ++failed;


            rw = new NumberReaderWriter(App.Files.getEstimatorsFile(
                    App.SIGNAL_WIFI5G, App.NN_DEPSO, info.id, true));

            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.get(info).depsoTimed.addAll(TrainingResults.fromDoubleList(list));
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
        

        checkLoaded();
    }

    private void checkLoaded() {
        loaded = (succeeded + failed) == numFiles;
        if (loaded && failed > 0) {
            App.log().e("Failed to load " + failed + " estimator files.");
        }
    }

    public boolean isLoaded() { return loaded; }

    public void add(TrainingResults results,
                    DataFileInfo info, String signalType, String nnType, boolean timed,
                     @Nullable AbsTextReaderWriter.WriteListener callback) {
        List<TrainingResults> list = null;
        switch (signalType) {
            case App.SIGNAL_BT:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? bt.get(info).psoTimed : bt.get(info).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? bt.get(info).deTimed : bt.get(info).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? bt.get(info).depsoTimed : bt.get(info).depsoUntimed; break;
                }
                break;
            case App.SIGNAL_BTLE:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? btle.get(info).psoTimed : btle.get(info).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? btle.get(info).deTimed : btle.get(info).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? btle.get(info).depsoTimed : btle.get(info).depsoUntimed; break;
                }
                break;
            case App.SIGNAL_WIFI:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? wifi.get(info).psoTimed : wifi.get(info).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? wifi.get(info).deTimed : wifi.get(info).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? wifi.get(info).depsoTimed : wifi.get(info).depsoUntimed; break;
                }
                break;
            case App.SIGNAL_WIFI5G:
                switch (nnType) {
                    case App.NN_PSO:
                        list = timed ? wifi5g.get(info).psoTimed : wifi5g.get(info).psoUntimed; break;
                    case App.NN_DE:
                        list = timed ? wifi5g.get(info).deTimed : wifi5g.get(info).deUntimed; break;
                    case App.NN_DEPSO:
                        list = timed ? wifi5g.get(info).depsoTimed : wifi5g.get(info).depsoUntimed; break;
                }
                break;
        }
       
        if (list != null) {
            list.add(results);
        } else {
            App.log().e("Unknown list for training results");
        }
        
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getEstimatorsFile(signalType, nnType, info.id, timed));
        rw.writeNumbers(Collections.singletonList(results.toArray()), true, callback);
    }


    public EstimatorInfo getEstimatorInfo(String signal, DataFileInfo info) {
        switch (signal) {
            case App.SIGNAL_BT:
                return bt.get(info);
            case App.SIGNAL_BTLE:
                return btle.get(info);
            case App.SIGNAL_WIFI:
                return wifi.get(info);
            default:
                return wifi5g.get(info);
        }
    }
}
