package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.data.ResettingList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class BtMan extends SignalManager {
    BtBeacon beacon;

    public BtMan(File file, ResettingList.Limits rssiWindowLimits,
                 ResettingList.Limits windowTrainingLimits, SignalStuff.SignalCallbacks callbacks) {
        super("bt", file, rssiWindowLimits, windowTrainingLimits, callbacks);
        beacon = BtBeacon.getInstance(App.getInstance());
    }
}
