package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TrainingResults;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class DistanceEstimator implements Comparable<DistanceEstimator> {
    public static final double GOOD_ERROR = 0.01;

    public static final double MIN = 0.1;
    public static final double BT_MAX = 15;
    public static final double WIFI_MAX = 110;

    public final double max;
    public final TrainingResults results;

    public DistanceEstimator(TrainingResults results, double max) {
        this.results = results;
        this.max = max;
    }

    public DistanceEstimator(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        max = json.getDouble("max");
        results = new TrainingResults(json.getString("results"));
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

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("max", max);
            json.put("results", results.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
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

    @Override
    public int compareTo(DistanceEstimator another) {
        return results.compareTo(another.results);
    }
}
