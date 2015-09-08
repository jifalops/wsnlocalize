package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.util.Stats;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 *
 */
public class WindowRecord {
    private static class Statistics {
        public final int min, max, range;
        public final double mean, median, stdDev;
        public Statistics(int min, int max, int range,
                   double mean, double median, double stdDev) {
            this.min = min;
            this.max = max;
            this.range = range;
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
        }
        public Statistics(String jsonObject) throws JSONException {
            JSONObject json = new JSONObject(jsonObject);
            min = json.getInt("min");
            max = json.getInt("max");
            range = json.getInt("range");
            mean = json.getDouble("mean");
            median = json.getDouble("median");
            stdDev = json.getDouble("stdDev");
        }
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json.put("min", min);
                json.put("max", max);
                json.put("range", range);
                json.put("mean", mean);
                json.put("median", median);
                json.put("stdDev", stdDev);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
    }

    public static class Rss extends Statistics {
        public final int count;
        public Rss(int count, int min, int max, int range,
                   double mean, double median, double stdDev) {
            super(min, max, range, mean, median, stdDev);
            this.count = count;
        }
        public Rss(String jsonObject) throws JSONException {
            super(jsonObject);
            JSONObject json = new JSONObject(jsonObject);
            count = json.getInt("count");
        }
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json = new JSONObject(super.toString());
                json.put("count", count);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
    }

    public static class Elapsed extends Statistics {
        public final int millis;
        public Elapsed(int millis, int min, int max, int range,
                   double mean, double median, double stdDev) {
            super(min, max, range, mean, median, stdDev);
            this.millis = millis;
        }
        public Elapsed(String jsonObject) throws JSONException {
            super(jsonObject);
            JSONObject json = new JSONObject(jsonObject);
            millis = json.getInt("millis");
        }
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json = new JSONObject(super.toString());
                json.put("millis", millis);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
    }

    public final Rss rss;
    public final Elapsed elapsed;

    public WindowRecord(List<RssiRecord> records) {
        int len = records.size();
        double[] rssi = new double[len];
        double[] el = new double[len-1];
        long last = 0;
        for (int i = 0; i < len; ++i) {
            rssi[i] = records.get(i).rssi;
            if (i != 0) {
                el[i-1] = records.get(i).time - last;
            }
            last = records.get(i).time;
        }
        int min = (int) Stats.min(rssi);
        int max = (int) Stats.max(rssi);
        rss = new Rss(len, min, max, max-min,
                Stats.mean(rssi), Stats.median(rssi), Stats.stdDev(rssi));

        min = (int) Stats.min(el);
        max = (int) Stats.max(el);
        int millis = (int) (el[el.length-1] - el[0]);
        elapsed = new Elapsed(millis, min, max, max-min,
                Stats.mean(el), Stats.median(el), Stats.stdDev(el));
    }

    public WindowRecord(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        rss = new Rss(json.getString("rss"));
        elapsed = new Elapsed(json.getString("elapsed"));
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("rss", rss.toString());
            json.put("elapsed", elapsed.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
