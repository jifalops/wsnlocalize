package com.jifalops.wsnlocalize;

/**
 *
 */
public class Calc {
    private Calc() {}

    /** @return The distance in meters. */
    public static double freeSpacePathLoss(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    public static double logCurve(double rssi, int maxDistMeters) {
        double divisor = 100 / Math.log10(maxDistMeters);
        return Math.pow(10, Math.abs(rssi) / divisor);
    }
}
