package com.jifalops.wsnlocalize;

import android.content.Context;

import com.jifalops.wsnlocalize.util.ResettingList;

import java.io.File;

/**
 *
 */
public class Settings {
    private Settings() {}
    public static final String SIGNAL_BT = "bt";
    public static final String SIGNAL_BTLE = "btle";
    public static final String SIGNAL_WIFI = "wifi";
    public static final String SIGNAL_WIFI5G = "wifi5g";
    public static final String DATA_RSSI = "rssi";
    public static final String DATA_WINDOW = "windows";
    public static final String DATA_ESTIMATOR = "estimator";
    public static final String DATA_SAMPLES = "samples";
    public static final ResettingList.Limits btWindowTrigger   = new ResettingList.Limits(3, 10_000, 5, 120_000);
    public static final ResettingList.Limits btTrainTrigger    = new ResettingList.Limits(2, 30_000, 10, 120_000);
    public static final ResettingList.Limits btleWindowTrigger = new ResettingList.Limits(15, 5_000, 20, 30_000);
    public static final ResettingList.Limits btleTrainTrigger  = new ResettingList.Limits(3, 30_000, 10, 120_000);
    public static final ResettingList.Limits wifiWindowTrigger = new ResettingList.Limits(5, 5_000, 20, 20_000);
    public static final ResettingList.Limits wifiTrainTrigger  = new ResettingList.Limits(3, 30_000, 10, 1200_000);

    public static String getFileName(String signalType, String dataType) {
        String ext = ".csv";
        if (DATA_ESTIMATOR.equals(dataType)) ext = ".json";
        return signalType + "-" + dataType + ext;
    }

    public static File getDataDir(Context ctx) {
        return ctx.getExternalFilesDir(null);
    }
}
