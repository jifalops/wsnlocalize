package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.util.Stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class WindowRecord {
    public static class Rss {
        public final int count, min, max, range;
        public final double mean, median, stdDev;
        public Rss(int count, int min, int max, int range,
                   double mean, double median, double stdDev) {
            this.count = count;
            this.min = min;
            this.max = max;
            this.range = range;
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
        }
        public Rss(String[] csv) {
            count = Integer.valueOf(csv[0]);
            min = Integer.valueOf(csv[1]);
            max = Integer.valueOf(csv[2]);
            range = Integer.valueOf(csv[3]);
            mean = Double.valueOf(csv[4]);
            median = Double.valueOf(csv[5]);
            stdDev = Double.valueOf(csv[6]);
        }
        @Override
        public String toString() {
            return count +","+ min +","+ max +","+ range +","+ mean +","+ median +","+ stdDev;
        }
    }

    public static class Elapsed {
        public final long millis;
        public final int min, max, range;
        public final double mean, median, stdDev;
        public Elapsed(long millis, int min, int max, int range,
                   double mean, double median, double stdDev) {
            this.millis = millis;
            this.min = min;
            this.max = max;
            this.range = range;
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
        }
        public Elapsed(String[] csv) {
            millis = Integer.valueOf(csv[0]);
            min = Integer.valueOf(csv[1]);
            max = Integer.valueOf(csv[2]);
            range = Integer.valueOf(csv[3]);
            mean = Double.valueOf(csv[4]);
            median = Double.valueOf(csv[5]);
            stdDev = Double.valueOf(csv[6]);
        }
        @Override
        public String toString() {
            return millis +","+ min +","+ max +","+ range +","+ mean +","+ median +","+ stdDev;
        }
    }

    public final Rss rss;
    public final Elapsed elapsed;
    public final int numDevices; // that produced this window.
    public final double distance;
    public double estimated = 0;

    public WindowRecord(List<RssiRecord> records) {
        distance = records.get(0).distance; // known or unknown.
        int len = records.size();
        double[] rssi = new double[len];
        double[] el = new double[len-1];
        Set<String> macs = new HashSet<>();
        RssiRecord record;
        for (int i = 0; i < len; ++i) {
            record = records.get(i);
            macs.add(record.mac);
            rssi[i] = record.rssi;
            if (i != 0) {
                el[i-1] = record.time - records.get(i-1).time;
            }
        }
        numDevices = macs.size();
        int min = (int) Stats.min(rssi);
        int max = (int) Stats.max(rssi);
        rss = new Rss(len, min, max, max-min,
                Stats.mean(rssi), Stats.median(rssi), Stats.stdDev(rssi));

        min = (int) Stats.min(el);
        max = (int) Stats.max(el);
        long millis = records.get(len-1).time - records.get(0).time;
        elapsed = new Elapsed(millis, min, max, max-min,
                Stats.mean(el), Stats.median(el), Stats.stdDev(el));
    }

    public WindowRecord(String[] csv) {
        estimated = Double.valueOf(csv[csv.length - 1]);
        distance = Double.valueOf(csv[csv.length - 2]);
        numDevices = Integer.valueOf(csv[csv.length - 3]);

        rss = new Rss(csv);
        System.arraycopy(csv, 7, csv, 0, 7); // shift 7 elements left 7 places
        elapsed = new Elapsed(csv);
    }

    @Override
    public String toString() {
        return rss.toString() +","+ elapsed.toString()
                +","+ numDevices +","+ distance +","+ estimated;
    }

    /** Does not include the estimated distance. */
    public double[] toTrainingArray() {
        return new double[] {
                rss.count, rss.min, rss.max, rss.range,
                rss.mean, rss.median, rss.stdDev,
                elapsed.millis, elapsed.min, elapsed.max, elapsed.range,
                elapsed.mean, elapsed.median, elapsed.stdDev,
                numDevices,
                distance
        };
    }
    public static final int TRAINING_ARRAY_SIZE = 16;

    public static double[][] toSamples(List<WindowRecord> records) {
        int len = records.size();
        double[][] samples = new double[len][TRAINING_ARRAY_SIZE];
        for (int i = 0; i < len; ++i) {
            samples[i] = records.get(i).toTrainingArray();
        }
        return samples;
    }
}
