package com.jifalops.wsnlocalize.file;

import com.jifalops.toolbox.file.AbsTextReaderWriter;
import com.jifalops.toolbox.util.Lists;
import com.jifalops.wsnlocalize.data.WindowRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WindowReaderWriter extends AbsTextReaderWriter {
    public WindowReaderWriter(File file) {
        super(file);
    }

    public boolean readRecords(final TypedReadListener<WindowRecord> callback) {
        return readLines(new ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                int exceptions = 0;
                List<WindowRecord> records = new ArrayList<>();
                for (String line : lines) {
                    try {
                        records.add(new WindowRecord(line.split(",")));
                    } catch (Exception e) {
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

    public void writeRecords(List<WindowRecord> records, boolean append, WriteListener callback) {
        writeLines(Lists.toString(records), append, callback);
    }
}
