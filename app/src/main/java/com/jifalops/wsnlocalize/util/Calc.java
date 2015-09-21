package com.jifalops.wsnlocalize.util;

/**
 * @deprecated
 */
public final class Calc {
    private Calc() {}

    /** @return The distance in meters. */
    public static float freeSpacePathLoss(float levelInDb, float freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return (float) Math.pow(10.0, exp);
    }


}
