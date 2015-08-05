package com.jifalops.wsnlocalize;

import android.support.annotation.Nullable;

import com.jifalops.wsnlocalize.util.ServiceThreadApplication;

/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
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
