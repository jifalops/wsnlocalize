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

    public MlpWeightMetrics(String csv) {
        super(csv);
        String[] p = csv.split(",");
        numHidden = Integer.valueOf(p[2]);
        hiddenBiasesStart = Integer.valueOf(p[3]);
        hiddenToOutputStart = Integer.valueOf(p[4]);
        outputBiasesStart = Integer.valueOf(p[5]);
        numWeights = Integer.valueOf(p[6]);
    }

    @Override
    public String toString() {
        return super.toString() +","+ numHidden +","+ hiddenBiasesStart +","+
                hiddenToOutputStart +","+ outputBiasesStart +","+ numWeights;
    }
}
