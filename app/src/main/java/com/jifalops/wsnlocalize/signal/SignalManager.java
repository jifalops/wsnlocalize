package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.data.ResettingList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SignalManager {
    final SignalStuff ss;
    final Map<String, String> devices = new HashMap<>();
    final Map<Long, String> events = new HashMap<>();

    protected SignalManager(String signalType, File dir, ResettingList.Limits rssiWindowLimits,
                         ResettingList.Limits windowTrainingLimits,
                         SignalStuff.SignalCallbacks callbacks) {
        ss = new SignalStuff(signalType, dir, rssiWindowLimits, windowTrainingLimits, callbacks);
    }
}
