package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.util.Arrays;

/**
 *
 */
public class SampleHelper {
    public interface SamplesCallback {
        void onSamplesLoaded();
    }

    double[][] bt = null, btle = null, wifi = null, wifi5g = null;
    final NumberReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
    SamplesCallback callback;

    public SampleHelper(SamplesCallback onLoadFinished) {
        callback = onLoadFinished;
        btRW = new NumberReaderWriter(App.getFile(App.SIGNAL_BT, App.DATA_SAMPLES),
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
        btleRW = new NumberReaderWriter(App.getFile(App.SIGNAL_BTLE, App.DATA_SAMPLES),
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
        wifiRW = new NumberReaderWriter(App.getFile(App.SIGNAL_WIFI, App.DATA_SAMPLES),
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
        wifi5gRW = new NumberReaderWriter(App.getFile(App.SIGNAL_WIFI5G, App.DATA_SAMPLES),
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
        
        btRW.readNumbers();
        btleRW.readNumbers();
        wifiRW.readNumbers();
        wifi5gRW.readNumbers();
    }

    void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            callback.onSamplesLoaded();
        }
    }

    public double[][] getBt() { return bt; }
    public double[][] getBtle() { return btle; }
    public double[][] getWifi() { return wifi; }
    public double[][] getWifi5g() { return wifi5g; }
    public double[][] getAll() {
        double[][] samples = bt;
        if (btle != null) {
            samples = samples == null ? btle : Arrays.concat(samples, btle);
        }
        if (wifi != null) {
            samples = samples == null ? wifi : Arrays.concat(samples, wifi);
        }
        if (wifi5g != null) {
            samples = samples == null ? wifi5g : Arrays.concat(samples, wifi5g);
        }
        return samples;
    }

    public void addBt(double[] s) {
        double[][] tmp = new double[][] {s};
        btRW.writeNumbers(tmp, true);
        if (bt == null) bt = tmp;
        else bt = Arrays.concat(bt, tmp);
    }
    public void addBtle(double[] s) {
        double[][] tmp = new double[][] {s};
        btleRW.writeNumbers(tmp, true);
        if (btle == null) btle = tmp;
        else btle = Arrays.concat(btle, tmp);
    }
    public void addWifi(double[] s) {
        double[][] tmp = new double[][] {s};
        wifiRW.writeNumbers(tmp, true);
        if (wifi == null) wifi = tmp;
        else wifi = Arrays.concat(wifi, tmp);
    }
    public void addWifi5g(double[] s) {
        double[][] tmp = new double[][] {s};
        wifi5gRW.writeNumbers(tmp, true);
        if (wifi5g == null) wifi5g = tmp;
        else wifi5g = Arrays.concat(wifi5g, tmp);
    }
}
