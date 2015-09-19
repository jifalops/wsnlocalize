package com.jifalops.wsnlocalize.neuralnet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class TrainingResults implements Comparable<TrainingResults> {
    public static final double GOOD_ERROR = 0.01;
    public final double[] weights;
    public final double error, mean, stddev;
    public final int samples, generations;
    public final Scaler scaler;
    public final MlpWeightMetrics metrics;
    TrainingResults(double[] weights, MlpWeightMetrics metrics, double error, double mean,
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

    public TrainingResults(String jsonObject) throws JSONException {
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
    public int compareTo(TrainingResults another) {
//        if (samples < another.samples) return -1;
//        if (samples > another.samples) return 1;
        if (error < another.error) return -1;
        if (error > another.error) return 1;
        return 0;
    }
}
