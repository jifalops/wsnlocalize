package com.jifalops.wsnlocalize.file;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.util.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated
 */
public class WindowReaderWriter extends AbsTextReaderWriter {
    public WindowReaderWriter(File file) {
        super(file);
    }

    public boolean readRecords(@Nullable final TypedReadListener<WindowRecord> callback) {
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
                if (callback != null) callback.onReadSucceeded(records, exceptions);
            }

            @Override
            public void onReadFailed(IOException e) {
                if (callback != null) callback.onReadFailed(e);
            }
        });
    }

    public void writeRecords(List<WindowRecord> records, boolean append, @Nullable WriteListener callback) {
        writeLines(Lists.toStrings(records), append, callback);
    }
}
