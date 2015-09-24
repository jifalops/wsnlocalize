package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.data.DistanceEstimator;
import com.jifalops.wsnlocalize.toolbox.util.Stats;

import java.util.List;

/**
 *
 */
public class BestDistanceEstimator {
    public interface OnReadyListener {
        void onReady();
    }

    public static class Estimate {
        public final double mean, median;
        public Estimate(double mean, double median) {
            this.mean = mean;
            this.median = median;
        }
    }

    final EstimatorHelper helper;

    public BestDistanceEstimator(boolean best, boolean maxSamplesOnly, final OnReadyListener callback) {
        helper = new EstimatorHelper(best, maxSamplesOnly, new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                callback.onReady();
            }
        });
    }

    public int getBtSize() { return helper.bt.size(); }
    public int getBtleSize() { return helper.btle.size(); }
    public int getWifiSize() { return helper.wifi.size(); }
    public int getWifi5gSize() { return helper.wifi5g.size(); }

    public Estimate estimateBt(double[] sample) {
        return estimate(helper.bt, sample);
    }
    public Estimate estimateBtle(double[] sample) {
        return estimate(helper.btle, sample);
    }
    public Estimate estimateWifi(double[] sample) {
        return estimate(helper.wifi, sample);
    }
    public Estimate estimateWifi5g(double[] sample) {
        return estimate(helper.wifi5g, sample);
    }

    private Estimate estimate(List<DistanceEstimator> list, double[] sample) {
        int len = list.size();
        if (len == 0) return new Estimate(0,0);
        double[] estimates = new double[len];
        for (int i = 0; i < len; ++i) {
            estimates[i] = list.get(i).estimate(sample);
        }

        return new Estimate(Stats.mean(estimates), Stats.median(estimates));
    }
}
