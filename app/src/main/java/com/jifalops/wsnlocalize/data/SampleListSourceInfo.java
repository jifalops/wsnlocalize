package com.jifalops.wsnlocalize.data;

/**
 * signal-numRssi-minCount-minTime-maxCount-maxTime.csv
 */
public class SampleListSourceInfo {
    public final String signal, extension;
    public final int numRssi;
    public final SampleWindow window;
    public SampleListSourceInfo(String signal, int numRssi, SampleWindow window, String fileExtension) {
        this.signal = signal;
        this.numRssi = numRssi;
        this.window = window;
        extension = fileExtension;
    }
    public SampleListSourceInfo(String fileName) {
        String[] parts = fileName.split("-");
        String[] last = parts[5].split("\\.");
        signal = parts[0];
        numRssi = Integer.valueOf(parts[1]);
        window = new SampleWindow(
                Integer.valueOf(parts[2]), Long.valueOf(parts[3]),
                Integer.valueOf(parts[4]), Long.valueOf(last[0]));
        extension = last[1];
    }
    public String getFileName() {
        return signal +"-"+ numRssi +"-"+
                window.minCount +"-"+ window.minTime +"-"+
                window.maxCount +"-"+ window.maxTime +"."+ extension;

    }

    @Override
    public boolean equals(Object o) {
        try {
            return ((SampleListSourceInfo) o).getFileName().equals(getFileName());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getFileName().hashCode();
    }
}
