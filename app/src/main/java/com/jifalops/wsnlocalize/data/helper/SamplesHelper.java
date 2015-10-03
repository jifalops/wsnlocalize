package com.jifalops.wsnlocalize.data.helper;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.RssiSampleList;
import com.jifalops.wsnlocalize.data.SampleWindow;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.NumberReaderWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SamplesHelper {
    private static SamplesHelper instance;
    public static SamplesHelper getInstance() {
        if (instance == null) {
            instance = new SamplesHelper();
        }
        return instance;
    }

    private final Map<DataFileInfo, RssiSampleList>
        bt = new HashMap<>(),
        btle = new HashMap<>(),
        wifi = new HashMap<>(),
        wifi5g = new HashMap<>();


    private boolean loaded;
    private int numFiles, succeeded, failed;

    private InfoFileHelper helper;

    private SamplesHelper() {
        helper = InfoFileHelper.getInstance();
        numFiles = helper.getBt().size() + helper.getBtle().size() +
                helper.getWifi().size() + helper.getWifi5g().size();
        
        NumberReaderWriter rw;
        for (final DataFileInfo info : helper.getBt()) {
            rw = new NumberReaderWriter(App.Files.getSamplesFile(App.SIGNAL_BT, info.id));
            if (!rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                @Override
                public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                    bt.put(info, new RssiSampleList(list));
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
                    btle.put(info, new RssiSampleList(list));
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
                    wifi.put(info, new RssiSampleList(list));
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
                    wifi5g.put(info, new RssiSampleList(list));
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

    public void addBt(RssiSampleList list, int numRssi, SampleWindow window,
                     @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getBt(numRssi, window);
        if (info == null) {
            info = helper.addBt(numRssi, window);
        } else {
            App.log().a("BT samples file with that info already exists.");
        }
        bt.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_BT, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }

    public void addBtle(RssiSampleList list, int numRssi, SampleWindow window,
                      @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getBtle(numRssi, window);
        if (info == null) {
            info = helper.addBtle(numRssi, window);
        } else {
            App.log().a("BTLE samples file with that info already exists.");
        }
        btle.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_BTLE, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }

    public void addWifi(RssiSampleList list, int numRssi, SampleWindow window,
                      @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getWifi(numRssi, window);
        if (info == null) {
            info = helper.addWifi(numRssi, window);
        } else {
            App.log().a("WIFI samples file with that info already exists.");
        }
        wifi.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_WIFI, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }

    public void addWifi5g(RssiSampleList list, int numRssi, SampleWindow window,
                        @Nullable AbsTextReaderWriter.WriteListener callback) {
        DataFileInfo info = helper.getWifi5g(numRssi, window);
        if (info == null) {
            info = helper.addWifi5g(numRssi, window);
        } else {
            App.log().a("WIFI5G samples file with that info already exists.");
        }
        wifi5g.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(
                App.Files.getSamplesFile(App.SIGNAL_WIFI5G, info.id));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }
}
