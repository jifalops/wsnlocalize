
package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.toolbox.neuralnet.Sample;
import com.jifalops.wsnlocalize.toolbox.util.Stats;

import java.util.List;

/**
 *
 */
public class RssiSample implements Sample {
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
    public final double distance;

    public RssiSample(List<Rssi> records) {
        distance = records.get(0).distance; // ok if known or unknown.

        int len = records.size();
        double[] rssi = new double[len];
        double[] el = new double[len-1];
        Rssi r;
        for (int i = 0; i < len; ++i) {
            r = records.get(i);
            rssi[i] = r.rssi;
            if (i != 0) el[i-1] = r.time - records.get(i-1).time;
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

    public RssiSample(String[] csv) {
        distance = Double.valueOf(csv[csv.length - 1]);
        rss = new Rss(csv);
        System.arraycopy(csv, 7, csv, 0, 7); // shift 7 elements left 7 places
        elapsed = new Elapsed(csv);
    }

    public RssiSample(double[] a) {
        rss = new Rss((int)a[0], (int)a[1], (int)a[2], (int)a[3], a[4], a[5], a[6]);
        elapsed = new Elapsed((long)a[7], (int)a[8], (int)a[9], (int)a[10], a[11], a[12], a[13]);
        distance = a[14];
    }

    @Override
    public String toString() {
        return rss.toString() +","+ elapsed.toString()
                +","+ distance;
    }

    @Override
    public double[] toArray() {
        return new double[] {
                rss.count, rss.min, rss.max, rss.range,
                rss.mean, rss.median, rss.stdDev,
                elapsed.millis, elapsed.min, elapsed.max, elapsed.range,
                elapsed.mean, elapsed.median, elapsed.stdDev,
                distance // output is last
        };
    }

    public double[] toUntimedArray() {
        return new double[] {
                rss.count, rss.min, rss.max, rss.range,
                rss.mean, rss.median, rss.stdDev,
                distance // output is last
        };
    }

    @Override
    public int getNumOutputs() {
        return 1;
    }

}