package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.data.DistanceEstimator;
import com.jifalops.wsnlocalize.toolbox.util.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BestDistanceEstimator {
    public interface OnReadyListener {
        void onReady();
    }

    final List<DistanceEstimator>
            bt = new ArrayList<>(),
            btle = new ArrayList<>(),
            wifi = new ArrayList<>(),
            wifi5g = new ArrayList<>();

    final EstimatorHelper helper;

    public BestDistanceEstimator(final OnReadyListener callback) {
        helper = new EstimatorHelper(new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                for (DistanceEstimator e : helper.getBt()) {
                    if (e.results.error < DistanceEstimator.GOOD_ERROR) bt.add(e);
                }
                for (DistanceEstimator e : helper.getBtle()) {
                    if (e.results.error < DistanceEstimator.GOOD_ERROR) btle.add(e);
                }
                for (DistanceEstimator e : helper.getWifi()) {
                    if (e.results.error < DistanceEstimator.GOOD_ERROR) wifi.add(e);
                }
                for (DistanceEstimator e : helper.getWifi5g()) {
                    if (e.results.error < DistanceEstimator.GOOD_ERROR) wifi5g.add(e);
                }
                callback.onReady();
            }
        });
    }

    public int getBtSize() { return bt.size(); }
    public int getBtleSize() { return btle.size(); }
    public int getWifiSize() { return wifi.size(); }
    public int getWifi5gSize() { return wifi5g.size(); }

    public double estimateBt(double[] sample) {
        return estimate(bt, sample);
    }
    public double estimateBtle(double[] sample) {
        return estimate(btle, sample);
    }
    public double estimateWifi(double[] sample) {
        return estimate(wifi, sample);
    }
    public double estimateWifi5g(double[] sample) {
        return estimate(wifi5g, sample);
    }

    private double estimate(List<DistanceEstimator> list, double[] sample) {
        int len = list.size();
        if (len == 0) return 0;
        double[] estimates = new double[len];
        for (int i = 0; i < len; ++i) {
            estimates[i] = list.get(i).estimate(sample);
        }
        return Stats.median(estimates);
    }
}
