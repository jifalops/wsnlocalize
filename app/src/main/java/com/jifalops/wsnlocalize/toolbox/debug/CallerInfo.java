package com.jifalops.wsnlocalize.toolbox.debug;

/**
 * Find the _class_ in the caller list above the one specified.
 */
public class CallerInfo {
    private CallerInfo() {}

    public static StackTraceElement getCaller(String className) {
        return getCaller(className, 1);
    }
    public static StackTraceElement getCaller(String className, int classesAbove) {
        return getCaller(Thread.currentThread().getStackTrace(), className, classesAbove);
    }
    public static StackTraceElement getCaller(StackTraceElement[] strackTrace,
                                              String className, int classesAbove) {
        return getCaller(strackTrace, className, classesAbove, 0);
    }
    private static StackTraceElement getCaller(StackTraceElement[] stackTrace,
                                               String className, int classesAbove, int startIndex) {
        boolean found = false;

        for (int i = startIndex, len = stackTrace.length; i < len; ++i) {
            if (!found && stackTrace[i].getClassName().equals(className)) {
                // We found the specified class
                if (classesAbove <= 0) return stackTrace[i];
                found = true;
            } else if (!stackTrace[i].getClassName().equals(className)) {
                // We found the class above the specified one.
                if (classesAbove == 1) return stackTrace[i];
                else return getCaller(stackTrace, stackTrace[i].getClassName(), classesAbove - 1, i);
            }
        }
        return null;
    }
}
