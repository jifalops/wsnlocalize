package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class TrainingStatus {
    private TerminationConditions term;
    private double[] best;
    private int bestIndex;
    private double[] bestError;

    TrainingStatus(MlpWeightMetrics metrics, TerminationConditions conditions) {
        term = conditions;
        best = new double[metrics.numWeights];
        bestError = new double[conditions.minImprovementGenerations];
        for (int i = 0; i < bestError.length; i++) {
            bestError[i] = 1_000_000;
        }
    }

    double[] getBest() {
        return best;
    }
    int getBestIndex() {
        return bestIndex;
    }
    double getBestError() { return bestError[0]; }

    boolean updateIfBest(double[] weights, int index, double error) {
        if (error < bestError[0]) {
            best = weights;
            bestIndex = index;
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
                bestError[bestError.length - 1] - bestError[0] < term.minImprovement ;
    }
}
