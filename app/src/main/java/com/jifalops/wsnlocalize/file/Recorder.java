package com.jifalops.wsnlocalize.file;

import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;

import java.util.List;

/**
 *
 */
public class Recorder {
    final RssiReaderWriter rssi;
    final WindowReaderWriter window;
    public Recorder(RssiReaderWriter rssi, WindowReaderWriter window) {
        this.rssi = rssi;
        this.window = window;
    }

    public void readRssi() {
        rssi.readRecords();
    }

    public void writeRssi(List<RssiRecord> records, boolean append) {
        rssi.writeRecords(records, append);
    }

    public void readWindows() {
        window.readRecords();
    }

    public void writeWindows(List<WindowRecord> records, boolean append) {
        window.writeRecords(records, append);
    }

    public void close() {
        rssi.close();
        window.close();
    }
}
