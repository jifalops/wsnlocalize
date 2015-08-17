package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class MlpWeightMetrics extends SampleMetrics {
    final int hidden, weights,
            hiddenBiasesStart, hiddenToOutputStart, outputBiasesStart;
    MlpWeightMetrics(int inputs, int outputs) {
        this(inputs, outputs, Math.round((inputs + outputs) / 2));
    }
    MlpWeightMetrics(int inputs, int outputs, int hidden) {
        super(inputs, outputs);
        this.hidden = hidden;
        hiddenBiasesStart = inputs * hidden;
        hiddenToOutputStart = hiddenBiasesStart + hidden;
        outputBiasesStart = hiddenToOutputStart + hidden * outputs;
        weights = hidden * (inputs + outputs + 1) + outputs;  // MLP
    }
}
