package com.jifalops.wsnlocalize.toolbox.neuralnet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Scaler {
    // When scaled
    final int numInputs, numOutputs,
            inputMin = -1, inputMax = 1, outputMin = 0, outputMax = 1;

    // Before scaling
    final double[] min, max;


    /** @param data used to set the min and max for each column. */
    public Scaler(double[][] data, int numOutputs) {
        this.numOutputs = numOutputs;
        int rows = data.length;
        int cols = data[0].length;
        numInputs = cols - numOutputs;
        min = new double[cols];
        max = new double[cols];
        for (int col = 0; col < cols; ++col) {
            min[col] = Double.MAX_VALUE;
            max[col] = -Double.MAX_VALUE;
            for (int row = 0; row < rows; ++row) {
                if (data[row][col] < min[col]) min[col] = data[row][col];
                if (data[row][col] > max[col]) max[col] = data[row][col];
            }
        }
    }

    public Scaler(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        numOutputs = json.getInt("numOutputs");
        JSONArray minj = json.getJSONArray("min");
        JSONArray maxj = json.getJSONArray("max");
        int len = minj.length();
        numInputs = len - numOutputs;
        min = new double[len];
        max = new double[len];
        for (int i = 0; i < len; ++i) {
            min[i] = minj.getDouble(i);
            max[i] = maxj.getDouble(i);
        }
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("numOutputs", numOutputs);
            JSONArray minj = new JSONArray();
            JSONArray maxj = new JSONArray();
            for (int i = 0; i < min.length; ++i) {
                minj.put(i, min[i]);
                maxj.put(i, max[i]);
            }
            json.put("min", minj);
            json.put("max", maxj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public int getNumOutputs() {
        return numOutputs;
    }

    public int getNumColumns() {
        return min.length;
    }

    public double[][] scale(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[rows][cols];
        double unscaledDiff, scaledMin, scaledDiff,
                inDiff = inputMax - inputMin,
                outDiff = outputMax - outputMin;
        for (int i = 0; i < cols; i++) {
            unscaledDiff = max[i] - min[i];
            if (i < numInputs) {
                scaledMin = inputMin;
                scaledDiff = inDiff;
            } else {
                scaledMin = outputMin;
                scaledDiff = outDiff;
            }
            for (int j = 0; j < rows; j++) {
                scaled[j][i] = unscaledDiff == 0
                        ? scaledMin
                        : scaledMin + (data[j][i] - min[i]) * scaledDiff / unscaledDiff;
            }
        }
        return scaled;
    }

    public double[] unscale(double[] outputs) {
        int len = outputs.length;
        double[] unscaled = new double[len];
        for (int i = 0; i < len; ++i) {
            unscaled[i] = (outputs[i] - outputMin) *
                    (max[numInputs + i] - min[numInputs + i]) /
                    (outputMax - outputMin) +
                    min[numInputs + i];
        }
        return unscaled;
    }

    public static double[][] randomize(double[][] data) {
        List<double[]> rand = Arrays.asList(data);
        Collections.shuffle(rand);
        return rand.toArray(new double[data.length][data[0].length]);
    }

    public double[][] scaleAndRandomize(double[][] data) {
        return randomize(scale(data));
    }
}
