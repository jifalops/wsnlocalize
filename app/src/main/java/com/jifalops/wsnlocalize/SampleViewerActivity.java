package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.signal.SampleHelper;
import com.jifalops.wsnlocalize.toolbox.util.Arrays;
import com.jifalops.wsnlocalize.toolbox.util.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 *
 */
public class SampleViewerActivity extends Activity {
    static final int ALL = 0, WIFI = 1, WIFI5G = 2, BT = 3, BTLE = 4;
    static final String[] GROUPS = new String[] {
            "All", "WiFi 2.4GHz", "WiFi 5GHz", "Bluetooth", "Bluetooth LE"};

    final List<double[]> bt = new ArrayList<>(), btle = new ArrayList<>(),
            wifi = new ArrayList<>(), wifi5g = new ArrayList<>();

    GridLayout summary;
    ListView distSummariesView, samplesView;
    SampleHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samplesview);

        summary = (GridLayout) findViewById(R.id.overallSummary);
        distSummariesView = (ListView) findViewById(R.id.distanceSummary);
        samplesView = (ListView) findViewById(R.id.samples);

        helper = new SampleHelper(new SampleHelper.SamplesCallback() {
            @Override
            public void onSamplesLoaded() {
                bt.addAll(helper.getBt());
                btle.addAll(helper.getBtle());
                wifi.addAll(helper.getWifi());
                wifi5g.addAll(helper.getWifi5g());
                showSamples(ALL);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sampleviewer, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sampleAll:
                showSamples(ALL);
                return true;
            case R.id.sampleWifi:
                showSamples(WIFI);
                return true;
            case R.id.sampleWifi5g:
                showSamples(WIFI5G);
                return true;
            case R.id.sampleBt:
                showSamples(BT);
                return true;
            case R.id.sampleBtle:
                showSamples(BTLE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showSamples(int index) {
        final List<double[]> samples = new ArrayList<>();
        String title;
        switch (index) {
            case WIFI:
                samples.addAll(wifi);
                title = GROUPS[WIFI];
                break;
            case WIFI5G:
                samples.addAll(wifi5g);
                title = GROUPS[WIFI5G];
                break;
            case BT:
                samples.addAll(bt);
                title = GROUPS[BT];
                break;
            case BTLE:
                samples.addAll(btle);
                title = GROUPS[BTLE];
                break;
            default:
                title = GROUPS[ALL];
                samples.addAll(bt);
                samples.addAll(btle);
                samples.addAll(wifi);
                samples.addAll(wifi5g);
        }

        if (samples.size() == 0) {
            Toast.makeText(this, "No samples to show", Toast.LENGTH_SHORT).show();
            return;
        }

        setTitle(title + " Samples");

        fillSampleView(summary, new SamplesSummary(samples));

        final List<SamplesSummary> distanceSummaries = makeDistanceSummaries(samples);
        distSummariesView.setAdapter(new ArrayAdapter<SamplesSummary>(this,
                R.layout.listitem_sample, distanceSummaries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample, parent, false);
                }
                fillSampleView(convertView, distanceSummaries.get(position));
                return convertView;
            }
        });


        samplesView.setAdapter(new ArrayAdapter<double[]>(this, R.layout.listitem_sample, samples) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample, parent, false);
                }
                fillSampleView(convertView, samples.get(position));
                return convertView;
            }
        });
    }

    public void onOverallSummaryClicked(View view) {
        summary.setVisibility(summary.getVisibility() == View.VISIBLE
                ? View.GONE : View.VISIBLE);
    }

    public void onDistanceSummariesClicked(View view) {
        distSummariesView.setVisibility(distSummariesView.getVisibility() == View.VISIBLE
                ? View.GONE : View.VISIBLE);
    }

    public void onSamplesClicked(View view) {
        samplesView.setVisibility(samplesView.getVisibility() == View.VISIBLE
                ? View.GONE : View.VISIBLE);
    }

    static class Holder {
        TextView dist, rmin, rmax, rrange,
                 rcount, rmean, rmed, rstd,
                 time, tmin, tmax, trange,
                 dcount, tmean, tmed, tstd;
    }
    
    void fillSampleView(View v, SamplesSummary s) {
        Holder holder = (Holder) v.getTag();
        if (holder == null) {
            holder = new Holder();
            holder.dist = (TextView) v.findViewById(R.id.dist);
            holder.rmin = (TextView) v.findViewById(R.id.rmin);
            holder.rmax = (TextView) v.findViewById(R.id.rmax);
            holder.rrange = (TextView) v.findViewById(R.id.rrange);
            holder.rcount = (TextView) v.findViewById(R.id.rcount);
            holder.rmean = (TextView) v.findViewById(R.id.rmean);
            holder.rmed = (TextView) v.findViewById(R.id.rmedian);
            holder.rstd = (TextView) v.findViewById(R.id.rstddev);
            holder.time = (TextView) v.findViewById(R.id.time);
            holder.tmin = (TextView) v.findViewById(R.id.tmin);
            holder.tmax = (TextView) v.findViewById(R.id.tmax);
            holder.trange = (TextView) v.findViewById(R.id.trange);
            holder.dcount = (TextView) v.findViewById(R.id.numDevices);
            holder.tmean = (TextView) v.findViewById(R.id.tmean);
            holder.tmed = (TextView) v.findViewById(R.id.tmedian);
            holder.tstd = (TextView) v.findViewById(R.id.tstddev);
        }
        holder.dist.setText(String.format(Locale.US, "%.1fm", s.dist));
        holder.rmin.setText(String.format(Locale.US, "%d", s.rmin));
        holder.rmax.setText(String.format(Locale.US, "%d", s.rmax));
        holder.rrange.setText(String.format(Locale.US, "%d", s.rrange));
        holder.rcount.setText(String.format(Locale.US, "%d (%.1f)", s.count, s.avgrssicount));
        holder.rmean.setText(String.format(Locale.US, "%.1f", s.rmean));
        holder.rmed.setText(String.format(Locale.US, "%.1f", s.rmedian));
        holder.rstd.setText(String.format(Locale.US, "%.1f", s.rstddev));
        holder.time.setText(String.format(Locale.US, "%.1fs", s.millis / 1000));
        holder.tmin.setText(String.format(Locale.US, "%.1fs", s.tmin / 1000f));
        holder.tmax.setText(String.format(Locale.US, "%.1fs", s.tmax / 1000f));
        holder.trange.setText(String.format(Locale.US, "%.1fs", s.trange / 1000f));
        holder.dcount.setText(String.format(Locale.US, "%.1f", s.avgdevices));
        holder.tmean.setText(String.format(Locale.US, "%.1fs", s.tmean / 1000));
        holder.tmed.setText(String.format(Locale.US, "%.1fs", s.tmedian / 1000));
        holder.tstd.setText(String.format(Locale.US, "%.1fs", s.tstddev / 1000));
        v.setTag(holder);
    }

    void fillSampleView(View v, double[] s) {
        Holder holder = (Holder) v.getTag();
        if (holder == null) {
            holder = new Holder();
            holder.dist = (TextView) v.findViewById(R.id.dist);
            holder.rmin = (TextView) v.findViewById(R.id.rmin);
            holder.rmax = (TextView) v.findViewById(R.id.rmax);
            holder.rrange = (TextView) v.findViewById(R.id.rrange);
            holder.rcount = (TextView) v.findViewById(R.id.rcount);
            holder.rmean = (TextView) v.findViewById(R.id.rmean);
            holder.rmed = (TextView) v.findViewById(R.id.rmedian);
            holder.rstd = (TextView) v.findViewById(R.id.rstddev);
            holder.time = (TextView) v.findViewById(R.id.time);
            holder.tmin = (TextView) v.findViewById(R.id.tmin);
            holder.tmax = (TextView) v.findViewById(R.id.tmax);
            holder.trange = (TextView) v.findViewById(R.id.trange);
            holder.dcount = (TextView) v.findViewById(R.id.numDevices);
            holder.tmean = (TextView) v.findViewById(R.id.tmean);
            holder.tmed = (TextView) v.findViewById(R.id.tmedian);
            holder.tstd = (TextView) v.findViewById(R.id.tstddev);
        }
        holder.dist.setText(String.format(Locale.US, "%.1fm", s[15]));
        holder.rmin.setText(String.format(Locale.US, "%d", (int) s[1]));
        holder.rmax.setText(String.format(Locale.US, "%d", (int) s[2]));
        holder.rrange.setText(String.format(Locale.US, "%d", (int) s[3]));
        holder.rcount.setText(String.format(Locale.US, "%d", (int) s[0]));
        holder.rmean.setText(String.format(Locale.US, "%.1f", s[4]));
        holder.rmed.setText(String.format(Locale.US, "%.1f", s[5]));
        holder.rstd.setText(String.format(Locale.US, "%.1f", s[6]));
        holder.time.setText(String.format(Locale.US, "%.1fs", s[7] / 1000));
        holder.tmin.setText(String.format(Locale.US, "%.1fs", s[8] / 1000f));
        holder.tmax.setText(String.format(Locale.US, "%.1fs", s[9] / 1000f));
        holder.trange.setText(String.format(Locale.US, "%.1fs", s[10] / 1000f));
        holder.dcount.setText(String.format(Locale.US, "%.1f", s[14]));
        holder.tmean.setText(String.format(Locale.US, "%.1fs", s[11] / 1000));
        holder.tmed.setText(String.format(Locale.US, "%.1fs", s[12] / 1000));
        holder.tstd.setText(String.format(Locale.US, "%.1fs", s[13] / 1000));
        v.setTag(holder);
    }

    static class SamplesSummary {
        final double dist, avgrssicount, avgdevices,
                rmean, rmedian, rstddev, millis, tmean, tmedian, tstddev;
        final int count, rmin, rmax, rrange, tmin, tmax, trange;
        SamplesSummary(List<double[]> samples) {
            count = samples.size();
            double[][] cols = Arrays.transpose(samples.toArray(new double[count][]));
            avgrssicount = Stats.mean(cols[0]);
            rmin = (int) Stats.min(cols[1]);
            rmax = (int) Stats.max(cols[2]);
            rrange = rmax - rmin; // skip cols[3]
            rmean = Stats.mean(cols[4]);
            rmedian = Stats.mean(cols[5]);
            rstddev = Stats.mean(cols[6]);
            millis = Stats.mean(cols[7]);
            tmin = (int) Stats.min(cols[8]);
            tmax = (int) Stats.max(cols[9]);
            trange = tmax - tmin; // skip cols[10]
            tmean = Stats.mean(cols[11]);
            tmedian = Stats.mean(cols[12]);
            tstddev = Stats.mean(cols[13]);
            avgdevices = Stats.mean(cols[14]);
            dist = Stats.mean(cols[15]);
        }
    }


    List<SamplesSummary> makeDistanceSummaries(List<double[]> samples) {
        TreeMap<Double, List<double[]>> distances = new TreeMap<>();
        List<SamplesSummary> summaries;
        List<double[]> list;
        double dist;
        for (double[] sample : samples) {
            dist = sample[WindowRecord.ACTUAL_DISTANCE_INDEX];
            list = distances.get(dist);
            if (list == null) {
                list = new ArrayList<>();
                distances.put(dist, list);
            }
            list.add(sample);
        }
        summaries = new ArrayList<>(distances.size());
        for (List<double[]> s : distances.values()) {
            summaries.add(new SamplesSummary(s));
        }
        return summaries;
    }
}
