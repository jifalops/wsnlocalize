package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class TerminationConditions {
    int maxGenerations = 200;
    double minError = 1e-3;
    double minStdDev = 1e-4;
    double minImprovement = 1e-4;
    int minImprovementGenerations = 10;
}
