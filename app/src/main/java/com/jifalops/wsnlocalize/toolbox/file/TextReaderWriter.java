package com.jifalops.wsnlocalize.toolbox.file;

import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

/**
 * Class for typical read-write operations on a plain text file.
 */
public class TextReaderWriter extends AbsTextReaderWriter {

    public TextReaderWriter(File file) {
        super(file);
    }

    public boolean readLines(@Nullable ReadListener callback) {
        return super.readLines(callback);
    }

    @Override
    public void writeLines(List<String> lines, boolean append, @Nullable WriteListener callback) {
        super.writeLines(lines, append, callback);
    }
}
