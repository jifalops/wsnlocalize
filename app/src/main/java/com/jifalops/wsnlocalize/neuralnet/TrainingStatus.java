package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class TrainingStatus {
    private TerminationConditions term;
    private double[] best;
    private double[] bestError;

    TrainingStatus(MlpWeightMetrics metrics, TerminationConditions conditions) {
        term = conditions;
        best = new double[metrics.numWeights];
        bestError = new double[conditions.minImprovementGenerations];
        for (int i = 0; i < bestError.length; i++) {
            bestError[i] = Double.MAX_VALUE;
        }
    }

    double[] getBest() {
        return best;
    }

    boolean updateIfBest(double[] weights, double error) {
        if (error < bestError[0]) {
            best = weights;
            System.arraycopy(bestError, 0, bestError, 1, bestError.length - 1); // shift right
            bestError[0] = error;
            return true;
        } else {
            return false;
        }
    }

    boolean isComplete(int generation, double errorStdDev) {
        return generation >= term.maxGenerations ||
                bestError[0] <= term.minError ||
                errorStdDev <= term.minStdDev ||
                bestError[bestError.length - 1] + term.minImprovement >= bestError[0];
    }
}
