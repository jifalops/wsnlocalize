package com.jifalops.wsnlocalize.wifi;

import android.net.wifi.ScanResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jacob Phillips.
 */
public class ScanAggregator {
    final Map<String, AggregateScanResult> aggResults = new HashMap<String, AggregateScanResult>();
    public AggregateScanResult getResult(String bssid) { return aggResults.get(bssid); }
    public Collection<AggregateScanResult> getResults() {
        return aggResults.values();
    }

    public void processScanResults(List<ScanResult> scanResults) {
        for (ScanResult sr : scanResults) {
            AggregateScanResult agg = aggResults.get(sr.BSSID);
            if (agg == null) {
                agg = new AggregateScanResult();
                aggResults.put(sr.BSSID, agg);
            }
            agg.add(sr);
        }
    }
}
