package com.jifalops.wsnlocalize.data;

/**
 *
 */
public class Rssi {
    public final String mac;
    public final int rssi, freq;
    public final double distance;
    public final long time;

    public Rssi(String mac, int rssi, int freq, long time, double distance) {
        this.mac = mac;
        this.rssi = rssi;
        this.freq = freq;
        this.distance = distance;
        this.time = time;
    }

    public Rssi(String[] csv) {
        mac = csv[0];
        rssi = Integer.valueOf(csv[1]);
        freq = Integer.valueOf(csv[2]);
        distance = Double.valueOf(csv[3]);
        time = Long.valueOf(csv[4]);
    }

    @Override
    public String toString() {
        return mac +","+ rssi +","+ freq +","+ distance +","+ time;
    }
}
