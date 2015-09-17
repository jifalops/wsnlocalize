package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.neuralnet.NeuralNetwork;
import com.jifalops.wsnlocalize.neuralnet.Scaler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class Estimator {
    public static final double MIN = 0.1;
    public static final double BT_MAX = 15;
    public static final double WIFI_MAX = 110;

    final double min = MIN, max;
    final double[] weights;
    final Scaler scaler;
    final MlpWeightMetrics metrics;

    public Estimator(double[] weights, Scaler scaler, double max) {
        this.weights = weights;
        this.scaler = scaler;
        this.max = max;
        metrics = new MlpWeightMetrics(scaler.getNumInputs(),
                scaler.getNumColumns() - scaler.getNumInputs());
    }

    public Estimator(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        max = json.getDouble("max");
        JSONArray weightsj = json.getJSONArray("weights");
        int len = weightsj.length();
        weights = new double[len];
        for (int i = 0; i < len; ++i) {
            weights[i] = weightsj.getDouble(i);
        }
        scaler = new Scaler(json.getString("scaler"));
        metrics = new MlpWeightMetrics(scaler.getNumInputs(),
                scaler.getNumColumns() - scaler.getNumInputs());
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("max", max);
            JSONArray weightsj = new JSONArray();
            for (int i = 0; i < weights.length; ++i) {
                weightsj.put(i, weights[i]);
            }
            json.put("weights", weightsj);
            json.put("scaler", scaler.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /** Fills in the {@link WindowRecord#estimated} field. */
    public double estimate(WindowRecord record) {
        double[][] sample = new double[][] {record.toTrainingArray()};
        double[][] scaled = scaler.scale(sample);
        double[] outputs = NeuralNetwork.calcOutputs(weights, scaled[0], metrics);
        double estimate = scaler.unscale(outputs)[0];
        if (estimate < min) {
            estimate = min;
        } else if (estimate > max) {
            estimate = max;
        }
        record.estimated = estimate;
        return estimate;
    }
}
