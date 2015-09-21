package com.jifalops.toolbox.neuralnet;

/**
 *
 */
public class TerminationConditions {
    int maxGenerations = 200;
    double minError = 1e-3;
    double minStdDev = minError / 10;
    double minImprovement = minError / 10;
    int minImprovementGenerations = 10;
}
