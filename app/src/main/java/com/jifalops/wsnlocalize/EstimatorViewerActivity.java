package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    static final String TAG = EstimatorViewerActivity.class.getSimpleName();
    static final int ALL = 0, WIFI = 1, WIFI5G = 2, BT = 3, BTLE = 4;
    static final String[] GROUPS = new String[] {
            "All", "WiFi 2.4GHz", "WiFi 5GHz", "Bluetooth", "Bluetooth LE"};

    TextView countView, goodCountView, goodSamplesCountView;
    View summary;
    ListView estimatorsView;
    EstimatorHelper helper, toSendHelper, bestHelper;
    boolean best = true;
    int group = ALL;

    SharedPreferences prefs;

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
        prefs = getSharedPreferences(TAG, MODE_PRIVATE);
        best = prefs.getBoolean("best", best);
        group = prefs.getInt("group", group);
        toSendHelper = new EstimatorHelper(false, true, new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                if (!best && !showEstimators()) {
                    Toast.makeText(EstimatorViewerActivity.this, "No estimators to show", Toast.LENGTH_SHORT).show();
                }
            }
        });
        bestHelper = new EstimatorHelper(true, true, new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                if (best && !showEstimators()) {
                    Toast.makeText(EstimatorViewerActivity.this, "No estimators to show", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showEstimators();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putBoolean("best", best)
                .putInt("group", group).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_estimatorviewer, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.estimatorsBest).setChecked(best);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.estimatorsBest:
                best = !best;
                break;
            case R.id.estimatorAll:
                group = ALL;
                break;
            case R.id.estimatorWifi:
                group = WIFI;
                break;
            case R.id.estimatorWifi5g:
                group = WIFI5G;
                break;
            case R.id.estimatorBt:
                group = BT;
                break;
            case R.id.estimatorBtle:
                group = BTLE;
                break;
        }
        showEstimators(); // for all above
        return super.onOptionsItemSelected(item);
    }

    boolean showEstimators() {
        if (best) {
            helper = bestHelper;
        } else {
            helper = toSendHelper;
        }
        final List<Estimator> estimators = new ArrayList<>();
        String title;
        switch (group) {
            case WIFI:
                estimators.addAll(helper.getWifi());
                title = GROUPS[WIFI];
                break;
            case WIFI5G:
                estimators.addAll(helper.getWifi5g());
                title = GROUPS[WIFI5G];
                break;
            case BT:
                estimators.addAll(helper.getBt());
                title = GROUPS[BT];
                break;
            case BTLE:
                estimators.addAll(helper.getBtle());
                title = GROUPS[BTLE];
                break;
            default:
                title = GROUPS[ALL];
                estimators.addAll(helper.getAll());
        }

        if (estimators.size() == 0) {
            return false;
        }

        setTitle(title + (best ? " best " : " ") + "estimators");

        countView.setText(estimators.size() + "");

        Collections.sort(estimators);

        int goodCount = 0, goodCountSamples = 0, maxSamples = 0;
        for (Estimator e : estimators) {
            if (e.results.numSamples > maxSamples) maxSamples = e.results.numSamples;
        }
        for (Estimator e : estimators) {
            if (e.results.error < Estimator.GOOD_ERROR) {
                ++goodCount;
                if (e.results.numSamples == maxSamples) ++goodCountSamples;
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
        return true;
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
                samples += e.results.numSamples;
                generations += e.results.numGenerations;
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
            samples = e.results.numSamples;
            generations = e.results.numGenerations;
        }
    }
}
