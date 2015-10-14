package com.jifalops.wsnlocalize;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.RssiList;
import com.jifalops.wsnlocalize.data.RssiSampleList;
import com.jifalops.wsnlocalize.data.helper.EstimatesHelper;
import com.jifalops.wsnlocalize.data.helper.EstimatorsHelper;
import com.jifalops.wsnlocalize.data.helper.InfoFileHelper;
import com.jifalops.wsnlocalize.data.helper.RssiHelper;
import com.jifalops.wsnlocalize.data.helper.SamplesHelper;
import com.jifalops.wsnlocalize.toolbox.Display;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.toolbox.util.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 */
public class InfoFileAdapter extends RecyclerView.Adapter<InfoFileAdapter.ViewHolder> {
    RssiHelper rssiHelper;
    InfoFileHelper infoHelper;
    SamplesHelper samplesHelper;
    EstimatorsHelper estimatorsHelper;
    EstimatesHelper estimatesHelper;

    List<DataFileInfo> infos;
    LayoutInflater inflater;

    SparseBooleanArray selected;

    View.OnClickListener listener;
    
    public InfoFileAdapter(Context ctx) {
        rssiHelper = RssiHelper.getInstance();
        infoHelper = InfoFileHelper.getInstance();
        samplesHelper = SamplesHelper.getInstance();
        estimatorsHelper = EstimatorsHelper.getInstance();
        estimatesHelper = EstimatesHelper.getInstance();
        
        infos = infoHelper.getAll();
        inflater = LayoutInflater.from(ctx);

        selected = new SparseBooleanArray(infos.size());
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public int[] getSelectedPositions() {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < selected.size(); i++) {
            int key = selected.keyAt(i);
            if (selected.get(key)) list.add(key);
        }
        return Arrays.toPrimitive(list.toArray(new Integer[list.size()]));
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.listitem_infofile, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, final int position) {
        DataFileInfo info = infos.get(position);
        String signal = infoHelper.getSignal(position);

        RssiList rssi = rssiHelper.getRssi(signal);
        RssiSampleList samples = samplesHelper.getSamples(signal, info);
        EstimatorsHelper.EstimatorInfo estimatorInfo =
                estimatorsHelper.getEstimatorInfo(signal, info);
        Map<Integer, EstimatesHelper.EstimatesInfo> estimatesMap =
                estimatesHelper.getEstimatesInfos(signal, info);

        vh.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = !selected.get(position);
                selected.put(position, b);
                vh.container.setCardElevation(b ? Display.dpToPx(8) : Display.dpToPx(2));
//                vh.container.setCardBackgroundColor(b ? 0xFFF : 0xEEE);
                if (listener != null) listener.onClick(vh.container);
            }
        });

        vh.container.setCardElevation(selected.get(position)
                ? Display.dpToPx(8)
                : Display.dpToPx(2));

        vh.signal.setText(signal + " - " + info.id);
        vh.rssi.setText(rssi.size()+"");

        vh.minCount.setText(info.window.minCount+"");
        vh.minTime.setText(info.window.minTime+"");
        vh.maxCount.setText(info.window.maxCount+"");
        vh.maxTime.setText(info.window.maxTime+"");

        vh.samples.setText(samples.size()+"");
        vh.distances.setText(samples.splitByDistance().size()+"");

        vh.psoTimedCount.setText(estimatorInfo.psoTimed.size()+"");
        vh.psoTimedAvg.setText(format(estimatorsAvgError(estimatorInfo.psoTimed), 3));
        vh.psoUntimedCount.setText(estimatorInfo.psoUntimed.size()+"");
        vh.psoUntimedAvg.setText(format(estimatorsAvgError(estimatorInfo.psoUntimed), 3));

        vh.deTimedCount.setText(estimatorInfo.deTimed.size()+"");
        vh.deTimedAvg.setText(format(estimatorsAvgError(estimatorInfo.deTimed), 3));
        vh.deUntimedCount.setText(estimatorInfo.deUntimed.size()+"");
        vh.deUntimedAvg.setText(format(estimatorsAvgError(estimatorInfo.deUntimed), 3));

        vh.depsoTimedCount.setText(estimatorInfo.depsoTimed.size()+"");
        vh.depsoTimedAvg.setText(format(estimatorsAvgError(estimatorInfo.depsoTimed), 3));
        vh.depsoUntimedCount.setText(estimatorInfo.depsoUntimed.size()+"");
        vh.depsoUntimedAvg.setText(format(estimatorsAvgError(estimatorInfo.depsoUntimed), 3));
        
        vh.psoTimedEstimates.removeAllViews();
        vh.psoUntimedEstimates.removeAllViews();
        vh.deTimedEstimates.removeAllViews();
        vh.deUntimedEstimates.removeAllViews();
        vh.depsoTimedEstimates.removeAllViews();
        vh.depsoUntimedEstimates.removeAllViews();
        for (Map.Entry<Integer, EstimatesHelper.EstimatesInfo> entry : estimatesMap.entrySet()) {
            fillEstimates(vh.psoTimedEstimates, entry.getKey(), entry.getValue().psoTimed);
            fillEstimates(vh.psoUntimedEstimates, entry.getKey(), entry.getValue().psoUntimed);
            fillEstimates(vh.deTimedEstimates, entry.getKey(), entry.getValue().deTimed);
            fillEstimates(vh.deUntimedEstimates, entry.getKey(), entry.getValue().deUntimed);
            fillEstimates(vh.depsoTimedEstimates, entry.getKey(), entry.getValue().depsoTimed);
            fillEstimates(vh.depsoUntimedEstimates, entry.getKey(), entry.getValue().depsoUntimed);
        }
        
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }


    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView container;

        TextView signal, rssi,
            minCount, minTime, maxCount, maxTime,
            samples, distances,
            psoTimedCount, psoTimedAvg, psoUntimedCount, psoUntimedAvg,
            deTimedCount, deTimedAvg, deUntimedCount, deUntimedAvg,
            depsoTimedCount, depsoTimedAvg, depsoUntimedCount, depsoUntimedAvg;

        LinearLayout psoTimedEstimates, psoUntimedEstimates,
                deTimedEstimates, deUntimedEstimates,
                depsoTimedEstimates, depsoUntimedEstimates;
        
        public ViewHolder(View v) {
            super(v);
            container = (CardView) v.findViewById(R.id.cardView);

            signal = (TextView) v.findViewById(R.id.signal);
            rssi = (TextView) v.findViewById(R.id.rssi);
            
            minCount = (TextView) v.findViewById(R.id.minCount);
            minTime = (TextView) v.findViewById(R.id.minTime);
            maxCount = (TextView) v.findViewById(R.id.maxCount);
            maxTime = (TextView) v.findViewById(R.id.maxTime);

            samples = (TextView) v.findViewById(R.id.samples);
            distances = (TextView) v.findViewById(R.id.distances);

            psoTimedCount = (TextView) v.findViewById(R.id.psoTimedCount);
            psoTimedAvg = (TextView) v.findViewById(R.id.psoTimedAvg);
            psoUntimedCount = (TextView) v.findViewById(R.id.psoUntimedCount);
            psoUntimedAvg = (TextView) v.findViewById(R.id.psoUntimedAvg);

            deTimedCount = (TextView) v.findViewById(R.id.deTimedCount);
            deTimedAvg = (TextView) v.findViewById(R.id.deTimedAvg);
            deUntimedCount = (TextView) v.findViewById(R.id.deUntimedCount);
            deUntimedAvg = (TextView) v.findViewById(R.id.deUntimedAvg);

            depsoTimedCount = (TextView) v.findViewById(R.id.depsoTimedCount);
            depsoTimedAvg = (TextView) v.findViewById(R.id.depsoTimedAvg);
            depsoUntimedCount = (TextView) v.findViewById(R.id.depsoUntimedCount);
            depsoUntimedAvg = (TextView) v.findViewById(R.id.depsoUntimedAvg);

            psoTimedEstimates = (LinearLayout) v.findViewById(R.id.psoTimedEstimates);
            psoUntimedEstimates = (LinearLayout) v.findViewById(R.id.psoUntimedEstimates);

            deTimedEstimates = (LinearLayout) v.findViewById(R.id.deTimedEstimates);
            deUntimedEstimates = (LinearLayout) v.findViewById(R.id.deUntimedEstimates);

            depsoTimedEstimates = (LinearLayout) v.findViewById(R.id.depsoTimedEstimates);
            depsoUntimedEstimates = (LinearLayout) v.findViewById(R.id.depsoUntimedEstimates);
        }
    }



    double estimatorsAvgError(List<TrainingResults> results) {
        int len = results.size();
        if (len == 0) return 0;
        double sum = 0;
        for (TrainingResults r : results) {
            sum += r.error;
        }
        return sum / len;
    }

    double estimatesAvgError(List<EstimatesHelper.Estimate> estimates) {
        int len = estimates.size();
        if (len == 0) return 0;
        double sum = 0;
        for (EstimatesHelper.Estimate e : estimates) {
            sum += Math.abs(e.error);
        }
        return sum / len;
    }

    double underestimatePct(List<EstimatesHelper.Estimate> estimates) {
        int len = estimates.size();
        if (len == 0) return 0;
        int count = 0;
        for (EstimatesHelper.Estimate e : estimates) {
            if (e.error < 0) ++count;
        }
        return count / len;
    }
    
    void fillEstimates(LinearLayout layout, int numEstimators, List<EstimatesHelper.Estimate> estimates) {
        View v = inflater.inflate(R.layout.listitem_estimates, layout, false);
        ((TextView) v.findViewById(R.id.numEstimators)).setText(numEstimators+"");
        ((TextView) v.findViewById(R.id.avgError)).setText(
                Math.round(estimatesAvgError(estimates)*100) + "%");
        ((TextView) v.findViewById(R.id.underestimatePct)).setText(
                Math.round(underestimatePct(estimates)*100) + "%");
        layout.addView(v);
    }
    
    String format(double n, int places) {
        return String.format(Locale.US, "%."+places+"f", n);
    }
}
