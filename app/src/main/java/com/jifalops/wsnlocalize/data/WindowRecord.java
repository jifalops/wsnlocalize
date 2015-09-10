package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.util.Stats;

import java.util.List;

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
    public final float distance;
    public float estimated;

    public WindowRecord(List<RssiRecord> records) {
        distance = records.get(0).distance; // known or unknown.
        int len = records.size();
        double[] rssi = new double[len];
        double[] el = new double[len-1];
        for (int i = 0; i < len; ++i) {
            rssi[i] = records.get(i).rssi;
            if (i != 0) {
                el[i-1] = records.get(i).time - records.get(i-1).time;
            }
        }
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
        distance = Float.valueOf(csv[14]);
        estimated = Float.valueOf(csv[15]);
        rss = new Rss(csv);
        System.arraycopy(csv, 7, csv, 0, csv.length-7); // shift left 7
        elapsed = new Elapsed(csv);
    }

    @Override
    public String toString() {
        return rss.toString() +","+ elapsed.toString()
                +","+ distance +","+ estimated;
    }

    /** Does not include the estimated distance. */
    public double[] toTrainingArray() {
        return new double[] {
                rss.count, rss.min, rss.max, rss.range,
                rss.mean, rss.median, rss.stdDev,
                elapsed.millis, elapsed.min, elapsed.max, elapsed.range,
                elapsed.mean, elapsed.median, elapsed.stdDev,
                distance
        };
    }
    public static final int TRAINING_ARRAY_SIZE = 15;
}
