package com.jifalops.wsnlocalize.data;

/**
 *
 */
public class DataFileInfo {
    public final int id, numRssi, numOutputs;
    public final SampleWindow window;

    public DataFileInfo(int id, int numRssi, int numOutputs, SampleWindow window) {
        this.id = id;
        this.numRssi = numRssi;
        this.numOutputs = numOutputs;
        this.window = window;
    }

    public DataFileInfo(String csv) {
        String[] parts = csv.split(",");
        id = Integer.valueOf(parts[0]);
        numRssi = Integer.valueOf(parts[1]);
        numOutputs = Integer.valueOf(parts[2]);
        window = new SampleWindow(
                Integer.valueOf(parts[3]),
                Long.valueOf(parts[4]),
                Integer.valueOf(parts[5]),
                Long.valueOf(parts[6])
        );
    }

    @Override
    public String toString() {
        return id +","+ numRssi +","+ numOutputs +","+ window.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DataFileInfo && ((DataFileInfo)o).id == id;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
