package com.jifalops.wsnlocalize;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.util.ServiceThreadApplication;

/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
    public static final String NSD_SERVICE_PREFIX = "wsnloco_";
    public static final String WIFI_BEACON_SSID_PREFIX = "wsnloco_";

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
