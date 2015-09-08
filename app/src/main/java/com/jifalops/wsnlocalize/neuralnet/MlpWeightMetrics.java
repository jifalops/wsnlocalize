package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class MlpWeightMetrics extends SampleMetrics {
    final int numHidden, numWeights,
            hiddenBiasesStart, hiddenToOutputStart, outputBiasesStart;
    public MlpWeightMetrics(int numInputs, int numOutputs) {
        this(numInputs, numOutputs, Math.round((numInputs + numOutputs) / 2));
    }
    public MlpWeightMetrics(int numInputs, int numOutputs, int numHidden) {
        super(numInputs, numOutputs);
        this.numHidden = numHidden;
        hiddenBiasesStart = numInputs * numHidden;
        hiddenToOutputStart = hiddenBiasesStart + numHidden;
        outputBiasesStart = hiddenToOutputStart + numHidden * numOutputs;
        numWeights = numHidden * (numInputs + numOutputs + 1) + numOutputs;  // MLP
    }
}
