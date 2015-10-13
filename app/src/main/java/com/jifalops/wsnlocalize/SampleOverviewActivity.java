//package com.jifalops.wsnlocalize;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.jifalops.wsnlocalize.data.RssiSampleList;
//import com.jifalops.wsnlocalize.data.helper.SamplesHelper;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.TreeMap;
//
///**
// *
// */
//public class SampleOverviewActivity extends Activity {
//    static final String TAG = SampleOverviewActivity.class.getSimpleName();
//
//    TextView btRssi, btleRssi, wifiRssi, wifi5gRssi,
//        btSamples, btleSamples, wifiSamples, wifi5gSamples;
//    Button trainButton;
//    ListView samplesList;
//
//    boolean[] checkedSamples;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_samples_detail);
//
//        btRssi = (TextView) findViewById(R.id.btRssi);
//        btleRssi = (TextView) findViewById(R.id.btleRssi);
//        wifiRssi = (TextView) findViewById(R.id.wifiRssi);
//        wifi5gRssi = (TextView) findViewById(R.id.wifi5gRssi);
//
//        btSamples = (TextView) findViewById(R.id.btSamples);
//        btleSamples = (TextView) findViewById(R.id.btleSamples);
//        wifiSamples = (TextView) findViewById(R.id.wifiSamples);
//        wifi5gSamples = (TextView) findViewById(R.id.wifi5gSamples);
//
//        samplesList = (ListView) findViewById(R.id.samplesList);
//
//        trainButton = (Button) findViewById(R.id.trainButton);
//
//        trainButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<Integer> indexes = new ArrayList<>();
//                for (int i = 0; i < checkedSamples.length; ++i) {
//                    if (checkedSamples[i]) indexes.add(i);
//                }
//                Intent intent = new Intent(SampleOverviewActivity.this,
//                        EstimatorTrainingActivity.class);
//                intent.putIntegerArrayListExtra(
//                        EstimatorTrainingActivity.EXTRA_SAMPLEINFO_INDEXES, indexes);
//                startActivity(intent);
//            }
//        });
//
//        final List<SamplesHelper> allSamples = App.getSamplesInfo();
//        checkedSamples = new boolean[allSamples.size()];
//
////        KeyedList<String, SamplesInfo> signalSamples = new KeyedList<>(allSamples,
////                new KeyedList.KeyLookup<String, SamplesInfo>() {
////            @Override
////            public String getKey(SamplesInfo item) {
////                return item.signal;
////            }
////        });
//
//
//        samplesList.setAdapter(new ArrayAdapter<SamplesHelper>(this,
//                R.layout.listitem_sample_overview, allSamples) {
//            @Override
//            public View getView(final int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
//                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample_overview, parent, false);
//                }
//
//                convertView.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        Intent i = new Intent(SampleOverviewActivity.this,
//                                SampleDetailViewerActivity.class);
//                        i.putExtra(SampleDetailViewerActivity.EXTRA_SAMPLELIST_INDEX, position);
//                        startActivity(i);
//                        return true;
//                    }
//                });
//
//                Holder holder = (Holder) convertView.getTag();
//                if (holder == null) {
//                    holder = new Holder();
//
//                    holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
//
//                    holder.signal = (TextView) convertView.findViewById(R.id.signal);
//                    holder.rssi = (TextView) convertView.findViewById(R.id.rssi);
//                    holder.samples = (TextView) convertView.findViewById(R.id.samples);
//                    holder.distances = (TextView) convertView.findViewById(R.id.distances);
//
//                    holder.minCount = (TextView) convertView.findViewById(R.id.minCount);
//                    holder.minTime = (TextView) convertView.findViewById(R.id.minTime);
//                    holder.maxCount = (TextView) convertView.findViewById(R.id.maxCount);
//                    holder.maxTime = (TextView) convertView.findViewById(R.id.maxTime);
//
//                    convertView.setTag(holder);
//                }
//
//                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        checkedSamples[position] = isChecked;
//                        setTrainButtonVisibility();
//                    }
//                });
//
//                SamplesHelper info = allSamples.get(position);
//
//                holder.signal.setText(info.signal);
//                holder.rssi.setText(info.numRssi+"");
//                holder.samples.setText(info.samples.size());
//                TreeMap<Double, RssiSampleList> map = new TreeMap<>(info.samples.splitByDistance());
//                holder.distances.setText(String.format(Locale.US, "%d (%.1f - %.1fm)",
//                        map.size(), map.firstKey(), map.lastKey()));
//
//                return convertView;
//            }
//        });
//
//    }
//
//    static class Holder {
//        CheckBox checkBox;
//        TextView signal, rssi, samples, distances,
//                minCount, minTime, maxCount, maxTime;
//    }
//
//    void setTrainButtonVisibility() {
//        for (boolean b : checkedSamples) {
//            if (b) {
//                trainButton.setVisibility(View.VISIBLE);
//                return;
//            }
//        }
//        trainButton.setVisibility(View.GONE);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//
//        return true;
//    }
//}
