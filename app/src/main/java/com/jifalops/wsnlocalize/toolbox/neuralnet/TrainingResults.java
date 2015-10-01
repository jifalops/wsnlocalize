package com.jifalops.wsnlocalize.toolbox.neuralnet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class TrainingResults implements Comparable<TrainingResults> {
    public final double[] weights;
    public final double error, mean, stddev;
    public final int numHidden, numGenerations;

    TrainingResults(double[] weights, double error, double mean, double stddev,
                    int numHidden, int numGenerations, SampleList samples) {
        this.weights = weights;
        this.error = error;
        this.mean = mean;
        this.stddev = stddev;
        this.numHidden = numHidden;
        this.numGenerations = numGenerations;
        this.samples = samples;
    }

    public TrainingResults(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        error = json.getDouble("error");
        mean = json.getDouble("mean");
        stddev = json.getDouble("stddev");
        numSamples = json.getInt("samples");
        numGenerations = json.getInt("generations");
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
            json.put("samples", numSamples);
            json.put("generations", numGenerations);
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
