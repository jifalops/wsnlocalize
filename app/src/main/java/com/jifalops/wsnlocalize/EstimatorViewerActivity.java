package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.toolbox.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.signal.EstimatorHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class EstimatorViewerActivity extends Activity {
    static final int ALL = 0, WIFI = 1, WIFI5G = 2, BT = 3, BTLE = 4;
    static final String[] GROUPS = new String[] {
            "All", "WiFi 2.4GHz", "WiFi 5GHz", "Bluetooth", "Bluetooth LE"};

    List<Estimator> bt = null, btle = null, wifi = null, wifi5g = null;

    TextView countView, goodCountView, goodSamplesCountView;
    View summary;
    ListView estimatorsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimatorsview);
        View v = findViewById(R.id.estimatorSummary);
        countView = (TextView) v.findViewById(R.id.count);
        goodCountView = (TextView) v.findViewById(R.id.countGood);
        goodSamplesCountView = (TextView) v.findViewById(R.id.countGoodMaxSamples);
        summary = findViewById(R.id.estimatorAverages);
        estimatorsView = (ListView) findViewById(R.id.estimators);
        EstimatorHelper.loadEstimators(new EstimatorHelper.EstimatorsLoadedCallback() {
            @Override
            public void onEstimatorsLoaded(List<Estimator> bt, List<Estimator> btle,
                                           List<Estimator> wifi, List<Estimator> wifi5g) {
                EstimatorViewerActivity.this.bt = bt;
                EstimatorViewerActivity.this.btle = btle;
                EstimatorViewerActivity.this.wifi = wifi;
                EstimatorViewerActivity.this.wifi5g = wifi5g;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_estimatorviewer, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.estimatorAll:
                showEstimators(ALL);
                return true;
            case R.id.estimatorWifi:
                showEstimators(WIFI);
                return true;
            case R.id.estimatorWifi5g:
                showEstimators(WIFI5G);
                return true;
            case R.id.estimatorBt:
                showEstimators(BT);
                return true;
            case R.id.estimatorBtle:
                showEstimators(BTLE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showEstimators(int index) {
        final List<Estimator> estimators = new ArrayList<>();
        String title;
        switch (index) {
            case WIFI:
                estimators.addAll(wifi);
                title = GROUPS[WIFI];
                break;
            case WIFI5G:
                estimators.addAll(wifi5g);
                title = GROUPS[WIFI5G];
                break;
            case BT:
                estimators.addAll(bt);
                title = GROUPS[BT];
                break;
            case BTLE:
                estimators.addAll(btle);
                title = GROUPS[BTLE];
                break;
            default:
                title = GROUPS[ALL];
                if (wifi != null) estimators.addAll(wifi);
                if (wifi5g != null) estimators.addAll(wifi5g);
                if (bt != null) estimators.addAll(bt);
                if (btle != null) estimators.addAll(btle);
        }

        if (estimators.size() == 0) {
            Toast.makeText(this, "No estimators to show", Toast.LENGTH_SHORT).show();
            return;
        }

        setTitle(title + " Estimators");

        countView.setText(estimators.size() + "");

        Collections.sort(estimators);

        int goodCount = 0, goodCountSamples = 0, maxSamples = 0;
        for (Estimator e : estimators) {
            if (e.results.samples > maxSamples) maxSamples = e.results.samples;
        }
        for (Estimator e : estimators) {
            if (e.results.error < TrainingResults.GOOD_ERROR) {
                ++goodCount;
                if (e.results.samples == maxSamples) ++goodCountSamples;
            }
        }
        goodCountView.setText(goodCount + "");
        goodSamplesCountView.setText(goodCountSamples + "");

        fillEstimatorView(summary, new EstimatorSummary(estimators));

        estimatorsView.setAdapter(new ArrayAdapter<Estimator>(this, R.layout.listitem_estimator, estimators) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_estimator, parent, false);
                }
                fillEstimatorView(convertView, new EstimatorSummary(estimators.get(position)));
                return convertView;
            }
        });
    }

    static class Holder {
        TextView error, mean, stddev, samples, generations;
    }
    
    void fillEstimatorView(View v, EstimatorSummary es) {
        Holder holder = (Holder) v.getTag();
        if (holder == null) {
            holder = new Holder();
            holder.error = (TextView) v.findViewById(R.id.error);
            holder.mean = (TextView) v.findViewById(R.id.mean);
            holder.stddev = (TextView) v.findViewById(R.id.stddev);
            holder.samples = (TextView) v.findViewById(R.id.samples);
            holder.generations = (TextView) v.findViewById(R.id.generations);
        }
        holder.error.setText(String.format(Locale.US, "%.4f", es.error));
        holder.mean.setText(String.format(Locale.US, "%.4f", es.mean));
        holder.stddev.setText(String.format(Locale.US, "%.4f", es.stddev));
        holder.samples.setText(es.samples+"");
        holder.generations.setText(es.generations+"");
        v.setTag(holder);
    }

    static class EstimatorSummary {
        double error = 0, mean = 0, stddev = 0;
        int samples = 0, generations = 0;
        EstimatorSummary(List<Estimator> estimators) {
            for (Estimator e : estimators) {
                error += e.results.error;
                mean += e.results.mean;
                stddev += e.results.stddev;
                samples += e.results.samples;
                generations += e.results.generations;
            }
            int size = estimators.size();
            error /= size;
            mean /= size;
            stddev /= size;
            samples /= size;
            generations /= size;
        }
        EstimatorSummary(Estimator e) {
            error = e.results.error;
            mean = e.results.mean;
            stddev = e.results.stddev;
            samples = e.results.samples;
            generations = e.results.generations;
        }
    }
}
