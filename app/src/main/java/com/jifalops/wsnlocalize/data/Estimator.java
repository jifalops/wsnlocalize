package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TrainingResults;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Estimator extends ArrayList<TrainingResults> {
    public static final double MIN = 0.1;
    public static final double BT_MAX = 15;
    public static final double WIFI_MAX = 110;

    public final double max;
    public final RssiSampleList samples;
    public final List<TrainingResults> results = new ArrayList<>();
    public final boolean timed;

    public Estimator(RssiSampleList samples, double max) {
        this.samples = samples;
        this.max = max;
        timed = true;
    }

    public Estimator(RssiSampleList.Untimed samples, double max) {
        this.samples = samples;
        this.max = max;
        timed = false;
    }

    public static double getMax(String signalType) {
        switch (signalType) {
            case App.SIGNAL_BT:
                // fall through
            case App.SIGNAL_BTLE:
                return BT_MAX;
            default:
                return WIFI_MAX;
        }
    }

    public double estimate(double[] sample) {
        double estimate = results.estimate(sample)[0];
        if (estimate < MIN) {
            estimate = MIN;
        } else if (estimate > max) {
            estimate = max;
        }
        return estimate;
    }
}
