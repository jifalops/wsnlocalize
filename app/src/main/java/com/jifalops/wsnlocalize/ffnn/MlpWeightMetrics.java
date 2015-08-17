package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public class MlpWeightMetrics extends SampleMetrics {
    final int hidden, weights,
            hiddenBiasesStart, hiddenToOutputStart, outputBiasesStart;
    MlpWeightMetrics(SampleMetrics sm, int hidden) {
        super(sm.inputs, sm.outputs);
        this.hidden = hidden;
        hiddenBiasesStart = inputs * hidden;
        hiddenToOutputStart = hiddenBiasesStart + hidden;
        outputBiasesStart = hiddenToOutputStart + hidden * outputs;
        weights = hidden * (inputs + outputs + 1) + outputs;  // MLP
    }
}
