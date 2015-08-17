package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public class DataSet {
    /** double[sample][inputs and outputs] */
    final double[][] data;
    final SampleMetrics metrics;

    DataSet(double[][] data, SampleMetrics metrics) {
        this.data = data;
        this.metrics = metrics;
    }


//    double rmsError(Individual p, ) {
//
//    }
}
