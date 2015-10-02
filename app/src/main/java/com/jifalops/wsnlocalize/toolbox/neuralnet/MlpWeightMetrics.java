package com.jifalops.wsnlocalize.toolbox.neuralnet;

/**
 *
 */
public class MlpWeightMetrics {
    final int numInputs, numOutputs, numHidden, numWeights,
            hiddenBiasesStart, hiddenToOutputStart, outputBiasesStart;

    public MlpWeightMetrics(int numInputs, int numOutputs) {
        this(numInputs, numOutputs, Math.round((numInputs + numOutputs) / 2));
    }

    public MlpWeightMetrics(int numInputs, int numOutputs, int numHidden) {
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numHidden = numHidden;
        hiddenBiasesStart = numInputs * numHidden;
        hiddenToOutputStart = hiddenBiasesStart + numHidden;
        outputBiasesStart = hiddenToOutputStart + numHidden * numOutputs;
        numWeights = numHidden * (numInputs + numOutputs + 1) + numOutputs;  // MLP
    }

    public MlpWeightMetrics(String csv) {
        String[] p = csv.split(",");
        numInputs = Integer.valueOf(p[0]);
        numOutputs = Integer.valueOf(p[1]);
        numHidden = Integer.valueOf(p[2]);
        hiddenBiasesStart = Integer.valueOf(p[3]);
        hiddenToOutputStart = Integer.valueOf(p[4]);
        outputBiasesStart = Integer.valueOf(p[5]);
        numWeights = Integer.valueOf(p[6]);
    }

    @Override
    public String toString() {
        return numInputs +","+ numOutputs +","+ numHidden +","+ hiddenBiasesStart +","+
                hiddenToOutputStart +","+ outputBiasesStart +","+ numWeights;
    }
}
