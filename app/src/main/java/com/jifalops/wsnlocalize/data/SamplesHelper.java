package com.jifalops.wsnlocalize.data;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.NumberReaderWriter;

import java.io.File;
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

    private final Map<SampleListSourceInfo, SampleList> samples = new HashMap<>();
    private boolean loaded;
    private int numFiles, succeeded, failed;

    private SamplesHelper() {
        loadSamples();
    }

    private void loadSamples() {
        File dir = App.Files.getSamplesDir();
        if (dir.isDirectory()) {
            numFiles = dir.listFiles().length;
            samples.clear();
            NumberReaderWriter rw;
            for (final File f : dir.listFiles()) {
                final SampleListSourceInfo info = new SampleListSourceInfo(f.getName());
                rw = new NumberReaderWriter(f);
                rw.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
                    @Override
                    public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                        samples.put(info, new SampleList(list));
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
            App.log().e("samples directory invalid");
        }
    }

    private void checkLoaded() {
        loaded = (succeeded + failed) == numFiles;
        if (loaded && failed > 0) {
            App.log().e("Failed to load " + failed + " samples files.");
        }
    }

    public boolean isLoaded() { return loaded; }

    public void save(SampleListSourceInfo info, SampleList list,
                     @Nullable AbsTextReaderWriter.WriteListener callback) {
        samples.put(info, list);
        NumberReaderWriter rw = new NumberReaderWriter(new File(App.Files.getSamplesDir(), info.getFileName()));
        rw.writeNumbers(list.toDoubleList(), false, callback);
    }
}
