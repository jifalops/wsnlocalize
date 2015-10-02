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

    public final Rss rss;
    public final double distance;

    public RssiSample(List<Rssi> records) {
        distance = records.get(0).distance; // ok if known or unknown.

        int len = records.size();
        double[] rssi = new double[len];
        for (int i = 0; i < len; ++i) {
            rssi[i] = records.get(i).rssi;
        }
        int min = (int) Stats.min(rssi);
        int max = (int) Stats.max(rssi);
        rss = new Rss(len, min, max, max-min,
                Stats.mean(rssi), Stats.median(rssi), Stats.stdDev(rssi));
    }

    public RssiSample(String[] csv) {
        distance = Double.valueOf(csv[csv.length - 1]);
        rss = new Rss(csv);
    }

    public RssiSample(double[] a) {
        rss = new Rss((int)a[0], (int)a[1], (int)a[2], (int)a[3], a[4], a[5], a[6]);
        distance = a[a.length - 1];
    }

    @Override
    public String toString() {
        return rss.toString() +","+ distance;
    }

    @Override
    public double[] toArray() {
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
