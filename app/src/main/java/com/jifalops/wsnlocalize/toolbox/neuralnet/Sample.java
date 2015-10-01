package com.jifalops.wsnlocalize.toolbox.neuralnet;

/**
 *
 */
public interface Sample {
    /** The returned array should have inputs before outputs and should not be scaled */
    double[] toArray();
    int getNumOutputs();
}
