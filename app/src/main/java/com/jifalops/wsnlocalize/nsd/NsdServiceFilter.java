package com.jifalops.wsnlocalize.nsd;

import android.net.nsd.NsdServiceInfo;

/**
* Created by Jacob Phillips.
*/
public interface NsdServiceFilter {
    boolean isAcceptableService(NsdServiceInfo info);
}
