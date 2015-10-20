package com.jifalops.wsnlocalize;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.RssiSample;
import com.jifalops.wsnlocalize.data.RssiSampleList;
import com.jifalops.wsnlocalize.data.SampleWindow;
import com.jifalops.wsnlocalize.data.helper.EstimatesHelper;
import com.jifalops.wsnlocalize.data.helper.EstimatorsHelper;
import com.jifalops.wsnlocalize.data.helper.InfoFileHelper;
import com.jifalops.wsnlocalize.data.helper.RssiHelper;
import com.jifalops.wsnlocalize.data.helper.SamplesHelper;
import com.jifalops.wsnlocalize.toolbox.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.toolbox.neuralnet.Scaler;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.toolbox.util.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 */
public class DeviceEstimatorAdapter extends RecyclerView.Adapter<DeviceEstimatorAdapter.ViewHolder> {
    static final int MIN_ESTIMATORS = 5;

    static class Device {
        final String mac, name, signal;

        final Map<DataFileInfo, DeviceInfo> infos = new HashMap<>();

        int bestEstimateDataFileId = 0;
        BestEstimate estimate;

        Device(String mac, String name, String signal) {
            this.mac = mac;
            this.name = name;
            this.signal = signal;

            List<DataFileInfo> list = InfoFileHelper.getInstance().get(signal);
            for (DataFileInfo info : list) {
                EstimatorsHelper.EstimatorInfo estimatorInfo =
                        EstimatorsHelper.getInstance().getEstimatorInfo(signal, info);
                int maxEstimatorsTimed = findMaxEstimators(estimatorInfo, true);
                int maxEstimatorsUntimed = findMaxEstimators(estimatorInfo, false);

                if (maxEstimatorsTimed > 10) maxEstimatorsTimed = floor10(maxEstimatorsTimed);
                if (maxEstimatorsUntimed > 10) maxEstimatorsUntimed = floor10(maxEstimatorsUntimed);

                int minEstimatorsTimed = maxEstimatorsTimed > MIN_ESTIMATORS ? MIN_ESTIMATORS : maxEstimatorsTimed;
                int minEstimatorsUntimed = maxEstimatorsUntimed > MIN_ESTIMATORS ? MIN_ESTIMATORS : maxEstimatorsUntimed;

                infos.put(info, new DeviceInfo(new MinMaxEstimators(RssiSample.getMaxEstimate(signal),
                        SamplesHelper.getInstance().getSamples(signal, info),
                        estimatorInfo,
                        minEstimatorsTimed, maxEstimatorsTimed, minEstimatorsUntimed, maxEstimatorsUntimed)));
            }
        }

        void estimate(RssiSample sample, SampleWindow window) {
            for (Map.Entry<DataFileInfo, DeviceInfo> e : infos.entrySet()) {
                if (e.getKey().window.equals(window)) {
                    e.getValue().sample = sample;
                    e.getValue().estimate = e.getValue().estimators.estimate(sample);
                    break;
                }
            }
            for (Map.Entry<DataFileInfo, DeviceInfo> e : infos.entrySet()) {
                if (estimate == null) estimate = e.getValue().estimate;
                else if (Math.abs(e.getValue().estimate.error) < Math.abs(estimate.error)) {
                    estimate = e.getValue().estimate;
                    bestEstimateDataFileId = e.getKey().id;
                }
            }
        }

        static int floor10(int n) {
            return (int) Math.floor(n/10);
        }

        static int findMaxEstimators(EstimatorsHelper.EstimatorInfo info, boolean timed) {
            List<List<TrainingResults>> results = new ArrayList<>(3);
            if (timed) {
                results.add(info.deTimed);
                results.add(info.psoTimed);
                results.add(info.depsoTimed);
            } else {
                results.add(info.deUntimed);
                results.add(info.psoUntimed);
                results.add(info.depsoUntimed);
            }
            int min = Integer.MAX_VALUE;
            for (List<TrainingResults> list : results) {
                if (list.size() < min) min = list.size();
            }
            return min;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Device && mac.equals(((Device) o).mac);
        }

        @Override
        public int hashCode() {
            return mac.hashCode();
        }
    }
    
    static class DeviceInfo {
        RssiSample sample;
        BestEstimate estimate;
        final MinMaxEstimators estimators;
        DeviceInfo(MinMaxEstimators estimators) {
            this.estimators = estimators;
        }
    }

    RssiHelper rssiHelper;
    InfoFileHelper infoHelper;
    SamplesHelper samplesHelper;
    EstimatorsHelper estimatorsHelper;
    EstimatesHelper estimatesHelper;

    LayoutInflater inflater;

    SparseBooleanArray expandedWindows;
    Map<Integer, SparseBooleanArray> expandedEstimates;

    List<Device> devices = new ArrayList<>();

    public DeviceEstimatorAdapter(Context ctx) {
        inflater = LayoutInflater.from(ctx);

        rssiHelper = RssiHelper.getInstance();
        infoHelper = InfoFileHelper.getInstance();
        samplesHelper = SamplesHelper.getInstance();
        estimatorsHelper = EstimatorsHelper.getInstance();
        estimatesHelper = EstimatesHelper.getInstance();

        expandedWindows = new SparseBooleanArray();
        expandedEstimates = new HashMap<>();
    }

    public void addSample(Device device, SampleWindow window, RssiSample sample) {
        if (!devices.contains(device)) {
            devices.add(device);
        }

        device.estimate(sample, window);

        notifyDataSetChanged();
    }

    

    DataFileInfo getInfo(List<DataFileInfo> infos, SampleWindow window) {
        for (DataFileInfo info : infos) {
            if (info.window.equals(window)) return info;
        }
        return null;
    }

    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.listitem_deviceestimate, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, final int position) {
        Device device = devices.get(position);

        vh.signal.setText(device.signal);
        vh.name.setText(device.name);
        vh.mac.setText(device.mac);

        vh.bestEstimator.setText(device.bestEstimateDataFileId + ": " + device.estimate.name
            + " (" + device.estimate.estimators + ")");
        vh.bestEstimate.setText(device.estimate.estimate+"");

        setupCollapsible(position, vh);
        setupWindows(device, vh);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }


    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView container;

        EditText distance;

        ViewGroup windowsContainer;

        TextView signal, name, mac, windowsExpandCollapse, bestEstimator, bestEstimate;

        List<WindowView> windows = new ArrayList<>();
        
        public ViewHolder(View v) {
            super(v);
            container = (CardView) v.findViewById(R.id.cardView);

            distance = (EditText) v.findViewById(R.id.actualDistance);

            signal = (TextView) v.findViewById(R.id.signal);
            name = (TextView) v.findViewById(R.id.name);
            mac = (TextView) v.findViewById(R.id.mac);

            windowsExpandCollapse = (TextView) v.findViewById(R.id.windowsExpandCollapse);
            windowsContainer = (ViewGroup) v.findViewById(R.id.windowsContainer);

            bestEstimator = (TextView) v.findViewById(R.id.bestEstimator);
            bestEstimate = (TextView) v.findViewById(R.id.bestEstimate);
        }

        static class WindowView {
            ViewGroup estimatesContainer;
            TextView expandCollapse, windowLimits,
                    minUntimedEstimators, minTimedEstimators, maxUntimedEstimators, maxTimedEstimators,

            psoMinUntimed, psoMinTimed, psoMaxUntimed, psoMaxTimed,
                    psoMinUntimedChange, psoMinTimedChange, psoMaxUntimedChange, psoMaxTimedChange,

            deMinUntimed, deMinTimed, deMaxUntimed, deMaxTimed,
                    deMinUntimedChange, deMinTimedChange, deMaxUntimedChange, deMaxTimedChange,

            depsoMinUntimed, depsoMinTimed, depsoMaxUntimed, depsoMaxTimed,
                    depsoMinUntimedChange, depsoMinTimedChange, depsoMaxUntimedChange, depsoMaxTimedChange,

            avgMinUntimed, avgMinTimed, avgMaxUntimed, avgMaxTimed,
                    avgMinUntimedChange, avgMinTimedChange, avgMaxUntimedChange, avgMaxTimedChange;

            WindowView(View v) {
                expandCollapse = (TextView) v.findViewById(R.id.expandCollapse);
                windowLimits = (TextView) v.findViewById(R.id.windowLimits);
                estimatesContainer = (ViewGroup) v.findViewById(R.id.estimatesContainer);

                minUntimedEstimators = (TextView) v.findViewById(R.id.untimedMinEstimatorsCount);
                minTimedEstimators = (TextView) v.findViewById(R.id.timedMinEstimatorsCount);
                maxUntimedEstimators = (TextView) v.findViewById(R.id.untimedMaxEstimatorsCount);
                maxTimedEstimators = (TextView) v.findViewById(R.id.timedMaxEstimatorsCount);

                psoMinUntimed = (TextView) v.findViewById(R.id.untimedMinPsoEstimatedDistance);
                psoMinUntimedChange = (TextView) v.findViewById(R.id.untimedMinPsoEstimateChange);
                psoMinTimed = (TextView) v.findViewById(R.id.timedMinPsoEstimatedDistance);
                psoMinTimedChange = (TextView) v.findViewById(R.id.timedMinPsoEstimateChange);

                psoMaxUntimed = (TextView) v.findViewById(R.id.untimedMaxPsoEstimatedDistance);
                psoMaxUntimedChange = (TextView) v.findViewById(R.id.untimedMaxPsoEstimateChange);
                psoMaxTimed = (TextView) v.findViewById(R.id.timedMaxPsoEstimatedDistance);
                psoMaxTimedChange = (TextView) v.findViewById(R.id.timedMaxPsoEstimateChange);

                deMinUntimed = (TextView) v.findViewById(R.id.untimedMinDeEstimatedDistance);
                deMinUntimedChange = (TextView) v.findViewById(R.id.untimedMinDeEstimateChange);
                deMinTimed = (TextView) v.findViewById(R.id.timedMinDeEstimatedDistance);
                deMinTimedChange = (TextView) v.findViewById(R.id.timedMinDeEstimateChange);

                deMaxUntimed = (TextView) v.findViewById(R.id.untimedMaxDeEstimatedDistance);
                deMaxUntimedChange = (TextView) v.findViewById(R.id.untimedMaxDeEstimateChange);
                deMaxTimed = (TextView) v.findViewById(R.id.timedMaxDeEstimatedDistance);
                deMaxTimedChange = (TextView) v.findViewById(R.id.timedMaxDeEstimateChange);

                depsoMinUntimed = (TextView) v.findViewById(R.id.untimedMinDepsoEstimatedDistance);
                depsoMinUntimedChange = (TextView) v.findViewById(R.id.untimedMinDepsoEstimateChange);
                depsoMinTimed = (TextView) v.findViewById(R.id.timedMinDepsoEstimatedDistance);
                depsoMinTimedChange = (TextView) v.findViewById(R.id.timedMinDepsoEstimateChange);

                depsoMaxUntimed = (TextView) v.findViewById(R.id.untimedMaxDepsoEstimatedDistance);
                depsoMaxUntimedChange = (TextView) v.findViewById(R.id.untimedMaxDepsoEstimateChange);
                depsoMaxTimed = (TextView) v.findViewById(R.id.timedMaxDepsoEstimatedDistance);
                depsoMaxTimedChange = (TextView) v.findViewById(R.id.timedMaxDepsoEstimateChange);

                avgMinUntimed = (TextView) v.findViewById(R.id.untimedMinAvgEstimatedDistance);
                avgMinUntimedChange = (TextView) v.findViewById(R.id.untimedMinAvgEstimateChange);
                avgMinTimed = (TextView) v.findViewById(R.id.timedMinAvgEstimatedDistance);
                avgMinTimedChange = (TextView) v.findViewById(R.id.timedMinAvgEstimateChange);

                avgMaxUntimed = (TextView) v.findViewById(R.id.untimedMaxAvgEstimatedDistance);
                avgMaxUntimedChange = (TextView) v.findViewById(R.id.untimedMaxAvgEstimateChange);
                avgMaxTimed = (TextView) v.findViewById(R.id.timedMaxAvgEstimatedDistance);
                avgMaxTimedChange = (TextView) v.findViewById(R.id.timedMaxAvgEstimateChange);
            }
        }
    }

    void setupWindows(Device device, ViewHolder vh) {
        List<DataFileInfo> infos = infoHelper.get(device.signal);
        ViewHolder.WindowView w;
        DataFileInfo info;

        vh.windows.clear(); // can be commented out (but may show stale data)
        for (int i = infos.size(); i < vh.windows.size(); i++) {
            vh.windows.remove(i);
        }
        for (int i = 0; i < infos.size(); i++) {
            if (vh.windows.size() <= i) {
                vh.windows.add(new ViewHolder.WindowView(inflater.inflate(R.layout.listitem_deviceestimate_window,
                        vh.windowsContainer, false)));
            }

            w = vh.windows.get(i);
            info = infos.get(i);

            EstimatorsHelper.EstimatorInfo estimatorInfo =
                    estimatorsHelper.getEstimatorInfo(device.signal, info);

            int maxEstimatorsTimed = Device.findMaxEstimators(estimatorInfo, true);
            int maxEstimatorsUntimed = Device.findMaxEstimators(estimatorInfo, false);

            if (maxEstimatorsTimed > 10) maxEstimatorsTimed = Device.floor10(maxEstimatorsTimed);
            if (maxEstimatorsUntimed > 10) maxEstimatorsUntimed = Device.floor10(maxEstimatorsUntimed);

            int minEstimatorsTimed = maxEstimatorsTimed > MIN_ESTIMATORS ? MIN_ESTIMATORS : maxEstimatorsTimed;
            int minEstimatorsUntimed = maxEstimatorsUntimed > MIN_ESTIMATORS ? MIN_ESTIMATORS : maxEstimatorsUntimed;

            w.windowLimits.setText(info.id + ": " + info.window.toString());
            w.minTimedEstimators.setText(minEstimatorsTimed+"");
            w.minUntimedEstimators.setText(minEstimatorsUntimed+"");
            w.maxTimedEstimators.setText(maxEstimatorsTimed+"");
            w.maxUntimedEstimators.setText(maxEstimatorsUntimed+"");

            w.deMinTimed.setText(device.infos.get(info).estimators.minDeTimed+"");
            w.deMinUntimed.setText(device.infos.get(info).estimators.minDeUntimed+"");
            w.psoMinTimed.setText(device.infos.get(info).estimators.minPsoTimed+"");
            w.psoMinUntimed.setText(device.infos.get(info).estimators.minPsoUntimed+"");
            w.depsoMinTimed.setText(device.infos.get(info).estimators.minDepsoTimed+"");
            w.depsoMinUntimed.setText(device.infos.get(info).estimators.minDepsoUntimed+"");
            w.avgMinTimed.setText(device.infos.get(info).estimators.minAvgTimed+"");
            w.avgMinUntimed.setText(device.infos.get(info).estimators.minAvgUntimed+"");

            w.deMaxTimed.setText(device.infos.get(info).estimators.maxDeTimed+"");
            w.deMaxUntimed.setText(device.infos.get(info).estimators.maxDeUntimed+"");
            w.psoMaxTimed.setText(device.infos.get(info).estimators.maxPsoTimed+"");
            w.psoMaxUntimed.setText(device.infos.get(info).estimators.maxPsoUntimed+"");
            w.depsoMaxTimed.setText(device.infos.get(info).estimators.maxDepsoTimed+"");
            w.depsoMaxUntimed.setText(device.infos.get(info).estimators.maxDepsoUntimed+"");
            w.avgMaxTimed.setText(device.infos.get(info).estimators.maxAvgTimed+"");
            w.avgMaxUntimed.setText(device.infos.get(info).estimators.maxAvgUntimed+"");


            w.deMinTimedChange.setText((device.infos.get(info).estimators.minDeTimed - device.infos.get(info).estimators.minDeTimedPrevious)+"");
            w.deMinUntimedChange.setText((device.infos.get(info).estimators.minDeUntimed - device.infos.get(info).estimators.minDeUntimedPrevious)+"");
            w.psoMinTimedChange.setText((device.infos.get(info).estimators.minPsoTimed - device.infos.get(info).estimators.minPsoTimedPrevious)+"");
            w.psoMinUntimedChange.setText((device.infos.get(info).estimators.minPsoUntimed - device.infos.get(info).estimators.minPsoUntimedPrevious)+"");
            w.depsoMinTimedChange.setText((device.infos.get(info).estimators.minDepsoTimed - device.infos.get(info).estimators.minDepsoTimedPrevious)+"");
            w.depsoMinUntimedChange.setText((device.infos.get(info).estimators.minDepsoUntimed - device.infos.get(info).estimators.minDepsoUntimedPrevious)+"");
            w.avgMinTimedChange.setText((device.infos.get(info).estimators.minAvgTimed - device.infos.get(info).estimators.minAvgTimedPrevious)+"");
            w.avgMinUntimedChange.setText((device.infos.get(info).estimators.minAvgUntimed - device.infos.get(info).estimators.minAvgUntimedPrevious)+"");

            w.deMaxTimedChange.setText((device.infos.get(info).estimators.maxDeTimed - device.infos.get(info).estimators.maxDeTimedPrevious)+"");
            w.deMaxUntimedChange.setText((device.infos.get(info).estimators.maxDeUntimed - device.infos.get(info).estimators.maxDeUntimedPrevious)+"");
            w.psoMaxTimedChange.setText((device.infos.get(info).estimators.maxPsoTimed - device.infos.get(info).estimators.maxPsoTimedPrevious)+"");
            w.psoMaxUntimedChange.setText((device.infos.get(info).estimators.maxPsoUntimed - device.infos.get(info).estimators.maxPsoUntimedPrevious)+"");
            w.depsoMaxTimedChange.setText((device.infos.get(info).estimators.maxDepsoTimed - device.infos.get(info).estimators.maxDepsoTimedPrevious)+"");
            w.depsoMaxUntimedChange.setText((device.infos.get(info).estimators.maxDepsoUntimed - device.infos.get(info).estimators.maxDepsoUntimedPrevious)+"");
            w.avgMaxTimedChange.setText((device.infos.get(info).estimators.maxAvgTimed - device.infos.get(info).estimators.maxAvgTimedPrevious)+"");
            w.avgMaxUntimedChange.setText((device.infos.get(info).estimators.maxAvgUntimed - device.infos.get(info).estimators.maxAvgUntimedPrevious)+"");
        }
    }
    
    static class MinMaxEstimators {
        final EstimatorsHelper.EstimatorInfo 
                min = new EstimatorsHelper.EstimatorInfo(), 
                max = new EstimatorsHelper.EstimatorInfo();
        double
                minDeTimed, minDeUntimed, maxDeTimed, maxDeUntimed,
                minPsoTimed, minPsoUntimed, maxPsoTimed, maxPsoUntimed,
                minDepsoTimed, minDepsoUntimed, maxDepsoTimed, maxDepsoUntimed,
                minAvgTimed, minAvgUntimed, maxAvgTimed, maxAvgUntimed,

                minDeTimedPrevious, minDeUntimedPrevious, maxDeTimedPrevious, maxDeUntimedPrevious,
                minPsoTimedPrevious, minPsoUntimedPrevious, maxPsoTimedPrevious, maxPsoUntimedPrevious,
                minDepsoTimedPrevious, minDepsoUntimedPrevious, maxDepsoTimedPrevious, maxDepsoUntimedPrevious,
                minAvgTimedPrevious, minAvgUntimedPrevious, maxAvgTimedPrevious, maxAvgUntimedPrevious;

        final Scaler timedScaler, untimedScaler;
        final MlpWeightMetrics timedMetrics, untimedMetrics;
        final double maxEstimate;
        
        MinMaxEstimators(double maxEstimate, RssiSampleList samples, EstimatorsHelper.EstimatorInfo info,
                         int minTimed, int maxTimed, int minUntimed, int maxUntimed) {
            this.maxEstimate = maxEstimate;
            RssiSampleList.Untimed untimed = samples.toUntimed();
            timedScaler = samples.getScaler();
            untimedScaler = untimed.getScaler();
            timedMetrics = new MlpWeightMetrics(samples.getNumInputs(), samples.getNumOutputs());
            untimedMetrics = new MlpWeightMetrics(untimed.getNumInputs(), untimed.getNumOutputs());
            for (int i = 0; i < minTimed; i++) {
                if (info.deTimed.size() > i) this.min.deTimed.add(info.deTimed.get(i));
                if (info.psoTimed.size() > i) this.min.psoTimed.add(info.psoTimed.get(i));
                if (info.depsoTimed.size() > i) this.min.depsoTimed.add(info.depsoTimed.get(i));
            }
            for (int i = 0; i < maxTimed; i++) {
                if (info.deTimed.size() > i) this.max.deTimed.add(info.deTimed.get(i));
                if (info.psoTimed.size() > i) this.max.psoTimed.add(info.psoTimed.get(i));
                if (info.depsoTimed.size() > i) this.max.depsoTimed.add(info.depsoTimed.get(i));
            }

            for (int i = 0; i < minUntimed; i++) {
                if (info.deUntimed.size() > i) this.min.deUntimed.add(info.deUntimed.get(i));
                if (info.psoUntimed.size() > i) this.min.psoUntimed.add(info.psoUntimed.get(i));
                if (info.depsoUntimed.size() > i) this.min.depsoUntimed.add(info.depsoUntimed.get(i));
            }
            for (int i = 0; i < maxUntimed; i++) {
                if (info.deUntimed.size() > i) this.max.deUntimed.add(info.deUntimed.get(i));
                if (info.psoUntimed.size() > i) this.max.psoUntimed.add(info.psoUntimed.get(i));
                if (info.depsoUntimed.size() > i) this.max.depsoUntimed.add(info.depsoUntimed.get(i));
            }
        }
        
        BestEstimate estimate(RssiSample sample) {
            minDeTimedPrevious = minDeTimed;
            minDeUntimedPrevious = minDeUntimed;
            minPsoTimedPrevious = minPsoTimed;
            minPsoUntimedPrevious = minPsoUntimed;
            minDepsoTimedPrevious = minDepsoTimed;
            minDepsoUntimedPrevious = minDepsoUntimed;
            minAvgTimedPrevious = minAvgTimed;
            minAvgUntimedPrevious = minAvgUntimed;

            maxDeTimedPrevious = maxDeTimed;
            maxDeUntimedPrevious = maxDeUntimed;
            maxPsoTimedPrevious = maxPsoTimed;
            maxPsoUntimedPrevious = maxPsoUntimed;
            maxDepsoTimedPrevious = maxDepsoTimed;
            maxDepsoUntimedPrevious = maxDepsoUntimed;
            maxAvgTimedPrevious = maxAvgTimed;
            maxAvgUntimedPrevious = maxAvgUntimed;
            
            
            minDeTimed = mean(sample.estimate(maxEstimate, true, min.deTimed, timedScaler, timedMetrics));
            minDeUntimed = mean(sample.estimate(maxEstimate, false, min.deUntimed, untimedScaler, untimedMetrics));
            minPsoTimed = mean(sample.estimate(maxEstimate, true, min.psoTimed, timedScaler, timedMetrics));
            minPsoUntimed = mean(sample.estimate(maxEstimate, false, min.psoUntimed, untimedScaler, untimedMetrics));
            minDepsoTimed = mean(sample.estimate(maxEstimate, true, min.depsoTimed, timedScaler, timedMetrics));
            minDepsoUntimed = mean(sample.estimate(maxEstimate, false, min.depsoUntimed, untimedScaler, untimedMetrics));

            maxDeTimed = mean(sample.estimate(maxEstimate, true, max.deTimed, timedScaler, timedMetrics));
            maxDeUntimed = mean(sample.estimate(maxEstimate, false, max.deUntimed, untimedScaler, untimedMetrics));
            maxPsoTimed = mean(sample.estimate(maxEstimate, true, max.psoTimed, timedScaler, timedMetrics));
            maxPsoUntimed = mean(sample.estimate(maxEstimate, false, max.psoUntimed, untimedScaler, untimedMetrics));
            maxDepsoTimed = mean(sample.estimate(maxEstimate, true, max.depsoTimed, timedScaler, timedMetrics));
            maxDepsoUntimed = mean(sample.estimate(maxEstimate, false, max.depsoUntimed, untimedScaler, untimedMetrics));

            minAvgTimed = (minDeTimed + minPsoTimed + minDepsoTimed) / 3;
            minAvgUntimed = (minDeUntimed + minPsoUntimed + minDepsoUntimed) / 3;
            maxAvgTimed = (maxDeTimed + maxPsoTimed + maxDepsoTimed) / 3;
            maxAvgUntimed = (maxDeUntimed + maxPsoUntimed + maxDepsoUntimed) / 3;

            if (sample.distance == 0) return new BestEstimate("unavailable", 0, 0, 0);

            double minDeTimedError = Math.abs(minDeTimed - sample.distance);
            double minDeUntimedError = Math.abs(minDeUntimed - sample.distance);
            double minPsoTimedError = Math.abs(minPsoTimed - sample.distance);
            double minPsoUntimedError = Math.abs(minPsoUntimed - sample.distance);
            double minDepsoTimedError = Math.abs(minDepsoTimed - sample.distance);
            double minDepsoUntimedError = Math.abs(minDepsoUntimed - sample.distance);

            double maxDeTimedError = Math.abs(maxDeTimed - sample.distance);
            double maxDeUntimedError = Math.abs(maxDeUntimed - sample.distance);
            double maxPsoTimedError = Math.abs(maxPsoTimed - sample.distance);
            double maxPsoUntimedError = Math.abs(maxPsoUntimed - sample.distance);
            double maxDepsoTimedError = Math.abs(maxDepsoTimed - sample.distance);
            double maxDepsoUntimedError = Math.abs(maxDepsoUntimed - sample.distance);

            double minError = Stats.min(new double[]{
                    minDeTimedError, minDeUntimedError,
                    minPsoTimedError, minPsoUntimedError,
                    minDepsoTimedError, minDepsoUntimedError,
                    maxDeTimedError, maxDeUntimedError,
                    maxPsoTimedError, maxPsoUntimedError,
                    maxDepsoTimedError, maxDepsoUntimedError,
            });

            String name = "unknown";
            double estimate = 0;
            int estimators = 0;
            if (minError == minDeTimedError) {
                name = "DE Timed";
                estimate = minDeTimed;
                estimators = min.deTimed.size(); }
            else if (minError == minDeUntimedError) {
                name = "DE Untimed";
                estimate = minDeUntimed;
                estimators = min.deUntimed.size(); }
            else if (minError == minPsoTimedError) {
                name = "PSO Timed";
                estimate = minPsoTimed;
                estimators = min.psoTimed.size(); }
            else if (minError == minPsoUntimedError) {
                name = "PSO Untimed";
                estimate = minPsoUntimed;
                estimators = min.psoUntimed.size(); }
            else if (minError == minDepsoTimedError) {
                name = "DEPSO Timed";
                estimate = minDepsoTimed;
                estimators = min.depsoTimed.size(); }
            else if (minError == minDepsoUntimedError) {
                name = "DEPSO Untimed";
                estimate = minDepsoUntimed;
                estimators = min.depsoUntimed.size(); }
            else if (minError == maxDeTimedError) {
                name = "DE Timed";
                estimate = maxDeTimed;
                estimators = max.deTimed.size(); }
            else if (minError == maxDeUntimedError) {
                name = "DE Untimed";
                estimate = maxDeUntimed;
                estimators = max.deUntimed.size(); }
            else if (minError == maxPsoTimedError) {
                name = "PSO Timed";
                estimate = maxPsoTimed;
                estimators = max.psoTimed.size(); }
            else if (minError == maxPsoUntimedError) {
                name = "PSO Untimed";
                estimate = maxPsoUntimed;
                estimators = max.psoUntimed.size(); }
            else if (minError == maxDepsoTimedError) {
                name = "DEPSO Timed";
                estimate = maxDepsoTimed;
                estimators = max.depsoTimed.size(); }
            else if (minError == maxDepsoUntimedError) {
                name = "DEPSO Untimed";
                estimate = maxDepsoUntimed;
                estimators = max.depsoUntimed.size(); }
            return new BestEstimate(name, sample.distance, estimate, estimators);
        }

        double mean(double[] estimates) {
            return Stats.mean(Stats.trim(estimates, 1));
        }
    }

    static class BestEstimate {
        final String name;
        final double estimate, error;
        final int estimators;
        BestEstimate(String name, double distance, double estimate, int estimators) {
            this.name = name;
            this.estimate = estimate;
            error = (estimate - distance) / distance;
            this.estimators = estimators;
        }
    }


    void setupCollapsible(final int position, final ViewHolder vh) {
        vh.windowsContainer.setVisibility(expandedWindows.get(position) ? View.VISIBLE : View.GONE);
        vh.windowsExpandCollapse.setText(expandedWindows.get(position) ? "-" : "+");

        vh.windowsExpandCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = !expandedWindows.get(position);
                expandedWindows.put(position, b);
                vh.windowsContainer.setVisibility(b ? View.VISIBLE : View.GONE);
                vh.windowsExpandCollapse.setText(b ? "-" : "+");
            }
        });

        SparseBooleanArray tmp = expandedEstimates.get(position);
        if (tmp == null) tmp = new SparseBooleanArray(vh.windows.size());
        final SparseBooleanArray expanded = tmp;

        for (int i = 0, len = vh.windows.size(); i < len; ++i) {
            final ViewHolder.WindowView w = vh.windows.get(i);
            w.estimatesContainer.setVisibility(expanded.get(i) ? View.VISIBLE : View.GONE);
            w.expandCollapse.setText(expanded.get(i) ? "-" : "+");

            final int finalI = i;
            w.expandCollapse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean b = !expanded.get(finalI);
                    expanded.put(position, b);
                    w.estimatesContainer.setVisibility(b ? View.VISIBLE : View.GONE);
                    w.expandCollapse.setText(b ? "-" : "+");
                }
            });
        }
    }




    
    String format(double n, int places) {
        return String.format(Locale.US, "%."+places+"f", n);
    }
}
