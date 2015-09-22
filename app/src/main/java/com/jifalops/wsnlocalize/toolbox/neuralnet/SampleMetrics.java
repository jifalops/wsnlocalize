package com.jifalops.wsnlocalize.toolbox.neuralnet;

/**
 *
 */
class SampleMetrics {
    final int numInputs, numOutputs;
    SampleMetrics(int numInputs, int numOutputs) {
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
    }

    SampleMetrics(String csv) {
        String[] p = csv.split(",");
        numInputs = Integer.valueOf(p[0]);
        numOutputs = Integer.valueOf(p[1]);
    }

    @Override
    public String toString() {
        return numInputs +","+ numOutputs;
    }
}
