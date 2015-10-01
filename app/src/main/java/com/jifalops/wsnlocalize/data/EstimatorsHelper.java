package com.jifalops.wsnlocalize.data;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.toolbox.neuralnet.Scaler;

import java.io.IOException;
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

    static class EstimatorInfo {
        Scaler scaler;

    }

    private final Map<DataFileInfo, SampleList>
        bt = new HashMap<>(),
        btle = new HashMap<>(),
        wifi = new HashMap<>(),
        wifi5g = new HashMap<>();
    private boolean loaded;
    private int numFiles, succeeded, failed;

    private InfoFileHelper helper;

    private EstimatorsHelper() {
        helper = InfoFileHelper.getInstance();
        numFiles = helper.getBt().size() + helper.getBtle().size() +
                helper.getWifi().size() + helper.getWifi5g().size();
        NumberReaderWriter rw;
        for (final DataFileInfo info : helper.getBt()) {
            rw = new NumberReaderWriter(App.Files.getSamplesFile(App.SIGNAL_BT, info.id));
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.put(info, new SampleList(list));
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
            rw = new NumberReaderWriter(App.Files.getSamplesFile(App.SIGNAL_BTLE, info.id));
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    btle.put(info, new SampleList(list));
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
            rw = new NumberReaderWriter(App.Files.getSamplesFile(App.SIGNAL_WIFI, info.id));
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi.put(info, new SampleList(list));
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
            rw = new NumberReaderWriter(App.Files.getSamplesFile(App.SIGNAL_WIFI5G, info.id));
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    wifi5g.put(info, new SampleList(list));
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
            App.log().e("Failed to load " + failed + " samples files.");
        }
    }

    public boolean isLoaded() { return loaded; }

    public void addBt(SampleList list, int numRssi, int numOutputs, SampleWindow window,
                     @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getBt(numRssi, numOutputs, window);
        if (info == null) {
            info = helper.addBt(numRssi, numOutputs, window);
        } else {
            App.log().a("BT samples file with that info already exists.");
        }
        bt.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_BT, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }

    public void addBtle(SampleList list, int numRssi, int numOutputs, SampleWindow window,
                      @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getBtle(numRssi, numOutputs, window);
        if (info == null) {
            info = helper.addBtle(numRssi, numOutputs, window);
        } else {
            App.log().a("BTLE samples file with that info already exists.");
        }
        btle.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_BTLE, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }

    public void addWifi(SampleList list, int numRssi, int numOutputs, SampleWindow window,
                      @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getWifi(numRssi, numOutputs, window);
        if (info == null) {
            info = helper.addWifi(numRssi, numOutputs, window);
        } else {
            App.log().a("WIFI samples file with that info already exists.");
        }
        wifi.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_WIFI, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }

    public void addWifi5g(SampleList list, int numRssi, int numOutputs, SampleWindow window,
                        @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getWifi5g(numRssi, numOutputs, window);
        if (info == null) {
            info = helper.addWifi5g(numRssi, numOutputs, window);
        } else {
            App.log().a("WIFI5G samples file with that info already exists.");
        }
        wifi5g.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_WIFI5G, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }
}
