package com.jifalops.wsnlocalize.toolbox.file;

import com.jifalops.wsnlocalize.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class Files {
    private Files() {}

    public static void copy(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            App.log().e("file copy error: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                App.log().e("file copy error: " + e.getMessage());
                throw e;
            }
        }



    }
}
