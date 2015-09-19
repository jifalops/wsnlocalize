package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.util.Arrays;
import com.jifalops.wsnlocalize.util.Stats;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class SampleViewerActivity extends Activity {
    static final int ALL = 0, WIFI = 1, WIFI5G = 2, BT = 3, BTLE = 4;
    static final String[] GROUPS = new String[] {
            "All", "WiFi 2.4GHz", "WiFi 5GHz", "Bluetooth", "Bluetooth LE"};

    double[][] bt = null, btle = null, wifi = null, wifi5g = null;
    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samplesview);
        new NumberReaderWriter(new File(Settings.getDataDir(SampleViewerActivity.this),
                Settings.getFileName(Settings.SIGNAL_BT, Settings.DATA_SAMPLES)),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, double[][] numbers) {
                        bt = numbers;
                        btLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                });
        new NumberReaderWriter(new File(Settings.getDataDir(SampleViewerActivity.this),
                Settings.getFileName(Settings.SIGNAL_BTLE, Settings.DATA_SAMPLES)),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, double[][] numbers) {
                        btle = numbers;
                        btleLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                });
        new NumberReaderWriter(new File(Settings.getDataDir(SampleViewerActivity.this),
                Settings.getFileName(Settings.SIGNAL_WIFI, Settings.DATA_SAMPLES)),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, double[][] numbers) {
                        wifi = numbers;
                        wifiLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                });
        new NumberReaderWriter(new File(Settings.getDataDir(SampleViewerActivity.this),
                Settings.getFileName(Settings.SIGNAL_WIFI5G, Settings.DATA_SAMPLES)),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, double[][] numbers) {
                        wifi5g = numbers;
                        wifi5gLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                });
    }

    void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            showSamples(ALL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sampleviewer, menu);
        Spinner groups = (Spinner) menu.findItem(R.id.action_samplesspinner).getActionView();
        groups.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                GROUPS) {
        });
        groups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showSamples(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_samplesspinner:
//
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    void showSamples(int index) {
        double[][] samples = null;
        switch (index) {
            case WIFI:
                samples = wifi;
                break;
            case WIFI5G:
                samples = wifi5g;
                break;
            case BT:
                samples = bt;
                break;
            case BTLE:
                samples = btle;
                break;
            default:
                if (wifi != null) samples = wifi;
                if (wifi5g != null) {
                    samples = samples == null ? wifi5g : Arrays.concat(samples, wifi5g);
                }
                if (bt != null) {
                    samples = samples == null ? bt : Arrays.concat(samples, bt);
                }
                if (btle != null) {
                    samples = samples == null ? btle : Arrays.concat(samples, btle);
                }
        }

        if (samples == null) {
            Toast.makeText(this, "No samples to show", Toast.LENGTH_SHORT).show();
            return;
        }

        final double[][] finalSamples = samples;

        TextView summary = (TextView) findViewById(R.id.overallSummary);
        ListView distSummariesView = (ListView) findViewById(R.id.distanceSummary);
        ListView samplesView = (ListView) findViewById(R.id.samples);

        summary.setText(new SamplesSummary(samples).toString());

        final List<SamplesSummary> distanceSummaries = makeDistanceSummaries(samples);
        distSummariesView.setAdapter(new ArrayAdapter<SamplesSummary>(this,
                R.layout.listitem_sample, distanceSummaries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample, parent, false);
                }
                ((TextView) convertView).setText(distanceSummaries.get(position).toString());
                return convertView;
            }
        });


        samplesView.setAdapter(new ArrayAdapter<double[]>(this, R.layout.listitem_sample, finalSamples) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample, parent, false);
                }
                ((TextView) convertView).setText(makeSampleString(finalSamples[position]));
                return convertView;
            }
        });
    }

    String makeSampleString(double[] s) {
        return String.format(Locale.US,
                "%.1fm. med:%.1f avg:%.1f std:%.1f\n" +
                        "#rss:%d #dev:%d tavg:%.1f.\n" +
                        "tmed:%.1f tstd:%.1f ttot:%.1f rmin:%d rmax:%d rrng:%d tmin:%d tmax:%d trng:%d",
                s[15], s[5], s[4], s[6],
                (int) s[0], (int) s[14], s[11] / 1000,
                s[12] / 1000, s[13] / 1000, s[7] / 1000, (int) s[1], (int) s[2], (int) s[3],
                (int) s[8], (int) s[9], (int) s[10]);
    }

    static class SamplesSummary {
        final double dist, avgrssicount, avgdevices,
                rmean, rmedian, rstddev, millis, tmean, tmedian, tstddev;
        final int count, rmin, rmax, rrange, tmin, tmax, trange;
        SamplesSummary(double[][] samples) {
            count = samples.length;
            double[][] cols = Arrays.transpose(samples);
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

        @Override
        public String toString() {
            return String.format(Locale.US,
                    "%.1fm (%d). med:%.1f avg:%.1f std:%.1f\n" +
                            "#rss:%.1f #dev:%.1f tavg:%.1f\n" +
                            "tmed:%.1f tstd:%.1f ttot:%.1f rmin:%d rmax:%d rrng:%d tmin:%d tmax:%d trng:%d",
                    dist, count, rmedian, rmean, rstddev,
                    avgrssicount, avgdevices, tmean/1000,
                    tmedian/1000, tstddev/1000, millis/1000, rmin, rmax, rrange, tmin, tmax, trange);
        }
    }

    List<SamplesSummary> makeDistanceSummaries(double[][] samples) {
        Map<Double, List<double[]>> distances = new HashMap<>();
        List<SamplesSummary> summaries;
        int rows = samples.length;
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
            summaries.add(new SamplesSummary(s.toArray(new double[s.size()][])));
        }
        return summaries;
    }
}
