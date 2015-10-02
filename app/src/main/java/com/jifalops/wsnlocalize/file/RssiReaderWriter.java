package com.jifalops.wsnlocalize.file;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.data.Rssi;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.util.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiReaderWriter extends AbsTextReaderWriter {

    public RssiReaderWriter(File file) {
        super(file);
    }

    public boolean readRssi(@Nullable final TypedReadListener<Rssi> callback) {
        return readLines(new ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                int exceptions = 0;
                final List<Rssi> records = new ArrayList<>();
                for (String line : lines) {
                    try {
                        records.add(new Rssi(line.split(",")));
                    } catch (NumberFormatException e) {
                        ++exceptions;
                    }
                }
                if (callback != null) callback.onReadSucceeded(records, exceptions);
            }

            @Override
            public void onReadFailed(IOException e) {
                if (callback != null) callback.onReadFailed(e);
            }
        });
    }

    public void writeRecords(List<Rssi> records, boolean append, @Nullable WriteListener callback) {
        writeLines(Lists.toStrings(records), append, callback);
    }
}
