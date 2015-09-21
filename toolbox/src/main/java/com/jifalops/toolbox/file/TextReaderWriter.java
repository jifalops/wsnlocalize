package com.jifalops.toolbox.file;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

/**
 * Class for typical read-write operations on a plain text file.
 */
public class TextReaderWriter extends AbsTextReaderWriter {

    public TextReaderWriter(File file) {
        super(file);
    }

    public boolean readLines(@NonNull ReadListener<String> callback) {
        return super.readLines(callback);
    }

    @Override
    public void writeLine(String line, boolean append, @NonNull WriteListener callback) {
        super.writeLine(line, append, callback);
    }

    @Override
    public void writeLines(List<String> lines, boolean append, @NonNull WriteListener callback) {
        super.writeLines(lines, append, callback);
    }
}
