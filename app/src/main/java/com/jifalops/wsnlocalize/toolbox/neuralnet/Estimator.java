package com.jifalops.wsnlocalize.toolbox.neuralnet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class Estimator implements Comparable<Estimator> {
    public final double[] weights;
    public final double error, mean, stddev;
    public final int samples, generations;
    public final Scaler scaler;
    public final MlpWeightMetrics metrics;
    Estimator(double[] weights, MlpWeightMetrics metrics, double error, double mean,
              double stddev, int samples, int generations, Scaler scaler) {
        this.weights = weights;
        this.metrics = metrics;
        this.error = error;
        this.mean = mean;
        this.stddev = stddev;
        this.samples = samples;
        this.generations = generations;
        this.scaler = scaler;
    }

    public Estimator(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        error = json.getDouble("error");
        mean = json.getDouble("mean");
        stddev = json.getDouble("stddev");
        samples = json.getInt("samples");
        generations = json.getInt("generations");
        metrics = new MlpWeightMetrics(json.getString("metrics"));
        scaler = new Scaler(json.getString("scaler"));
        JSONArray weightsj = json.getJSONArray("weights");
        int len = weightsj.length();
        weights = new double[len];
        for (int i = 0; i < len; ++i) {
            weights[i] = weightsj.getDouble(i);
        }
    }

    public double[] estimate(double[] sample) {
        double[] scaled = scaler.scale(new double[][] {sample})[0];
        double[] outputs = NeuralNetwork.calcOutputs(weights, scaled, metrics);
        return scaler.unscale(outputs);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("error", error);
            json.put("mean", mean);
            json.put("stddev", stddev);
            json.put("samples", samples);
            json.put("generations", generations);
            json.put("metrics", metrics.toString());
            json.put("scaler", scaler.toString());
            JSONArray weightsj = new JSONArray();
            for (int i = 0; i < weights.length; ++i) {
                weightsj.put(i, weights[i]);
            }
            json.put("weights", weightsj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @Override
    public int compareTo(Estimator another) {
//        if (samples < another.samples) return -1;
//        if (samples > another.samples) return 1;
        if (error < another.error) return -1;
        if (error > another.error) return 1;
        return 0;
    }
}
