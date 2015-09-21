package com.jifalops.wsnlocalize.data;

import com.jifalops.toolbox.neuralnet.NeuralNetwork;
import com.jifalops.toolbox.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.App;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class Estimator implements Comparable<Estimator> {
    public static final double MIN = 0.1;
    public static final double BT_MAX = 15;
    public static final double WIFI_MAX = 110;

    public final double max;
    public final TrainingResults results;

    public Estimator(TrainingResults results, double max) {
        this.results = results;
        this.max = max;
    }

    public Estimator(String jsonObject) throws JSONException {
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
        double[][] toEstimate = new double[][] {sample};
        double[][] scaled = results.scaler.scale(toEstimate);
        double[] outputs = NeuralNetwork.calcOutputs(results.weights, scaled[0], results.metrics);
        double estimate = results.scaler.unscale(outputs)[0];
        if (estimate < MIN) {
            estimate = MIN;
        } else if (estimate > max) {
            estimate = max;
        }
        return estimate;
    }

    @Override
    public int compareTo(Estimator another) {
        return results.compareTo(another.results);
    }
}
