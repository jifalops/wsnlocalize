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
import com.jifalops.wsnlocalize.data.RssiList;
import com.jifalops.wsnlocalize.data.RssiSampleList;
import com.jifalops.wsnlocalize.data.helper.EstimatesHelper;
import com.jifalops.wsnlocalize.data.helper.EstimatorsHelper;
import com.jifalops.wsnlocalize.data.helper.InfoFileHelper;
import com.jifalops.wsnlocalize.data.helper.RssiHelper;
import com.jifalops.wsnlocalize.data.helper.SamplesHelper;
import com.jifalops.wsnlocalize.toolbox.Display;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 */
public class DeviceEstimatorAdapter extends RecyclerView.Adapter<DeviceEstimatorAdapter.ViewHolder> {
    RssiHelper rssiHelper;
    InfoFileHelper infoHelper;
    SamplesHelper samplesHelper;
    EstimatorsHelper estimatorsHelper;
    EstimatesHelper estimatesHelper;

    List<DataFileInfo> infos;
    LayoutInflater inflater;

    SparseBooleanArray collapsed;

    public DeviceEstimatorAdapter(Context ctx) {
        rssiHelper = RssiHelper.getInstance();
        infoHelper = InfoFileHelper.getInstance();
        samplesHelper = SamplesHelper.getInstance();
        estimatorsHelper = EstimatorsHelper.getInstance();
        estimatesHelper = EstimatesHelper.getInstance();

        collapsed = new SparseBooleanArray();

        infos = infoHelper.getAll();
        inflater = LayoutInflater.from(ctx);
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.listitem_deviceestimate, parent, false));
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

        EditText distance;

        ViewGroup estimatesContainer;

        TextView signal, name, mac, expandCollapse,
            minUntimedEstimators, minTimedEstimators, maxUntimedEstimators, maxTimedEstimators,
            
            psoMinUntimed, psoMinTimed, psoMaxUntimed, psoMaxTimed,
            psoMinUntimedChange, psoMinTimedChange, psoMaxUntimedChange, psoMaxTimedChange,
            
            deMinUntimed, deMinTimed, deMaxUntimed, deMaxTimed,
            deMinUntimedChange, deMinTimedChange, deMaxUntimedChange, deMaxTimedChange,
            
            depsoMinUntimed, depsoMinTimed, depsoMaxUntimed, depsoMaxTimed,
            depsoMinUntimedChange, depsoMinTimedChange, depsoMaxUntimedChange, depsoMaxTimedChange,

            avgMinUntimed, avgMinTimed, avgMaxUntimed, avgMaxTimed,
            avgMinUntimedChange, avgMinTimedChange, avgMaxUntimedChange, avgMaxTimedChange;
        
        public ViewHolder(View v) {
            super(v);
            container = (CardView) v.findViewById(R.id.cardView);

            distance = (EditText) v.findViewById(R.id.actualDistance);

            signal = (TextView) v.findViewById(R.id.signal);
            name = (TextView) v.findViewById(R.id.name);
            mac = (TextView) v.findViewById(R.id.mac);

            expandCollapse = (TextView) v.findViewById(R.id.expandCollapse);
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

    void setupCollapsible(final int position, final TextView button, final ViewGroup collapsible) {
        collapsible.setVisibility(collapsed.get(position) ? View.GONE : View.VISIBLE);
        button.setText(collapsed.get(position) ? "+" : "-");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = !collapsed.get(position);
                collapsed.put(position, b);
                collapsible.setVisibility(b ? View.GONE : View.VISIBLE);
                button.setText(b ? "+" : "-");
            }
        });
    }



    
    String format(double n, int places) {
        return String.format(Locale.US, "%."+places+"f", n);
    }
}
