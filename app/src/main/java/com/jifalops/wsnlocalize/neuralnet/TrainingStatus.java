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
            shiftErrorsRight();
            bestError[0] = error;
            return true;
        } else {
            return false;
        }
    }

    private void shiftErrorsRight() {
        for(int i = bestError.length - 1; i > 0; i--) {
            bestError[i] = bestError[i-1];
        }
    }


    boolean isComplete(int generation, double errorStdDev) {
        return generation >= term.maxGenerations ||
                bestError[0] <= term.minError ||
                errorStdDev <= term.minStdDev ||
                bestError[bestError.length - 1] + term.minImprovement >= bestError[0];
    }
}
