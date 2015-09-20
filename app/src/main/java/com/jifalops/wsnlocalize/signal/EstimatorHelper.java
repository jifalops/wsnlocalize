package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EstimatorHelper {
    public interface EstimatorsCallback {
        void onEstimatorsLoaded();
    }

    final List<Estimator>
            bt = new ArrayList<>(),
            btle = new ArrayList<>(),
            wifi = new ArrayList<>(),
            wifi5g = new ArrayList<>();
    final EstimatorReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
    EstimatorsCallback callback;

    public EstimatorHelper(EstimatorsCallback onLoadFinished) {
        callback = onLoadFinished;
        btRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_BT, App.DATA_ESTIMATOR),
                new EstimatorReaderWriter.EstimatorCallbacks() {
                    @Override
                    public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                        bt.addAll(records);
                        btLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

                    }
                });
        btleRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_BTLE, App.DATA_ESTIMATOR),
                new EstimatorReaderWriter.EstimatorCallbacks() {
                    @Override
                    public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                        btle.addAll(records);
                        btleLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

                    }
                });
        wifiRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_WIFI, App.DATA_ESTIMATOR),
                new EstimatorReaderWriter.EstimatorCallbacks() {
                    @Override
                    public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                        wifi.addAll(records);
                        wifiLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

                    }
                });
        wifi5gRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_WIFI5G, App.DATA_ESTIMATOR),
                new EstimatorReaderWriter.EstimatorCallbacks() {
                    @Override
                    public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                        wifi5g.addAll(records);
                        wifi5gLoaded = true;
                        checkIfAllLoaded();
                    }

                    @Override
                    public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

                    }
                });

        btRW.readRecords();
        btleRW.readRecords();
        wifiRW.readRecords();
        wifi5gRW.readRecords();
    }

    void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            callback.onEstimatorsLoaded();
        }
    }

    public List<Estimator> getBt() { return bt; }
    public List<Estimator> getBtle() { return btle; }
    public List<Estimator> getWifi() { return wifi; }
    public List<Estimator> getWifi5g() { return wifi5g; }
    public List<Estimator> getAll() {
        List<Estimator> list = new ArrayList<>();
        list.addAll(bt);
        list.addAll(btle);
        list.addAll(wifi);
        list.addAll(wifi5g);
        return list;
    }

    public void addBt(Estimator e) {
        btRW.writeRecords(makeList(e), true);
        bt.add(e);
    }
    public void addBtle(Estimator e) {
        btleRW.writeRecords(makeList(e), true);
        btle.add(e);
    }
    public void addWifi(Estimator e) {
        wifiRW.writeRecords(makeList(e), true);
        wifi.add(e);
    }
    public void addWifi5g(Estimator e) {
        wifi5gRW.writeRecords(makeList(e), true);
        wifi5g.add(e);
    }

    List<Estimator> makeList(Estimator e) {
        List<Estimator> list = new ArrayList<>();
        list.add(e);
        return list;
    }
}
