package com.jifalops.wsnlocalize.data;

/**
 *
 */
public class DataFileInfo {
    public final int id, numRssi;
    public final SampleWindow window;

    public DataFileInfo(int id, int numRssi, SampleWindow window) {
        this.id = id;
        this.numRssi = numRssi;
        this.window = window;
    }

    public DataFileInfo(String csv) {
        String[] parts = csv.split(",");
        id = Integer.valueOf(parts[0]);
        numRssi = Integer.valueOf(parts[1]);
        window = new SampleWindow(
                Integer.valueOf(parts[2]),
                Long.valueOf(parts[3]),
                Integer.valueOf(parts[4]),
                Long.valueOf(parts[5])
        );
    }

    @Override
    public String toString() {
        return id +","+ numRssi +","+ window.toString();
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
