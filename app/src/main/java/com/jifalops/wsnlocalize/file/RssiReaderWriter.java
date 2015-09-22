package com.jifalops.wsnlocalize.file;

import com.jifalops.wsnlocalize.data.RssiRecord;
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

    public boolean readRssi(final TypedReadListener<RssiRecord> callback) {
        return readLines(new ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                int exceptions = 0;
                final List<RssiRecord> records = new ArrayList<>();
                for (String line : lines) {
                    try {
                        records.add(new RssiRecord(line.split(",")));
                    } catch (NumberFormatException e) {
                        ++exceptions;
                    }
                }
                callback.onReadSucceeded(records, exceptions);
            }

            @Override
            public void onReadFailed(IOException e) {
                callback.onReadFailed(e);
            }
        });
    }

    public void writeRecords(List<RssiRecord> records, boolean append, WriteListener callback) {
        writeLines(Lists.toString(records), append, callback);
    }
}
