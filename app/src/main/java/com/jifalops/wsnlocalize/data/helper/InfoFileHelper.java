package com.jifalops.wsnlocalize.data.helper;

import android.util.Log;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.SampleWindow;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.TextReaderWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class InfoFileHelper {
    static final String TAG = InfoFileHelper.class.getSimpleName();
    public static final String ACTION_LOADED = TAG + ".loaded";
    
    private static InfoFileHelper instance;
    public static InfoFileHelper getInstance() {
        if (instance == null) {
            instance = new InfoFileHelper();
        }
        return instance;
    }

    private final List<DataFileInfo>
        bt = new ArrayList<>(),
        btle = new ArrayList<>(),
        wifi = new ArrayList<>(),
        wifi5g = new ArrayList<>();
    private final TextReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    private boolean allLoaded, btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;

    private InfoFileHelper() {
        btRW = new TextReaderWriter(App.Files.getInfoFile(App.SIGNAL_BT));
        btleRW = new TextReaderWriter(App.Files.getInfoFile(App.SIGNAL_BTLE));
        wifiRW = new TextReaderWriter(App.Files.getInfoFile(App.SIGNAL_WIFI));
        wifi5gRW = new TextReaderWriter(App.Files.getInfoFile(App.SIGNAL_WIFI5G));
        
        if (!btRW.readLines(new AbsTextReaderWriter.ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                for (String line : lines) {
                    bt.add(new DataFileInfo(line));
                }
                btLoaded = true;
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BT info.", e);
            }
        })) btLoaded = true;

        if (!btleRW.readLines(new AbsTextReaderWriter.ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                for (String line : lines) {
                    btle.add(new DataFileInfo(line));
                }
                btleLoaded = true;
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BTLE info.", e);
            }
        })) btleLoaded = true;

        if(!wifiRW.readLines(new AbsTextReaderWriter.ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                for (String line : lines) {
                    wifi.add(new DataFileInfo(line));
                }
                wifiLoaded = true;
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi info.", e);
            }
        })) wifiLoaded = true;

        if(!wifi5gRW.readLines(new AbsTextReaderWriter.ReadListener() {
            @Override
            public void onReadSucceeded(List<String> lines) {
                for (String line : lines) {
                    wifi5g.add(new DataFileInfo(line));
                }
                wifi5gLoaded = true;
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi5G info.", e);
            }
        })) wifi5gLoaded = true;

        checkIfAllLoaded();
    }

    private void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            allLoaded = true;
            App.broadcast(ACTION_LOADED);
        }
    }

    public boolean isLoaded() { return allLoaded; }

    public List<DataFileInfo> getBt() { return bt; }
    public List<DataFileInfo> getBtle() { return btle; }
    public List<DataFileInfo> getWifi() { return wifi; }
    public List<DataFileInfo> getWifi5g() { return wifi5g; }
    public List<DataFileInfo> getAll() {
        List<DataFileInfo> list = new ArrayList<>();
        list.addAll(bt);
        list.addAll(btle);
        list.addAll(wifi);
        list.addAll(wifi5g);
        return list;
    }


    private int getNextId(List<DataFileInfo> list) {
        int max = 0;
        for (DataFileInfo info : list) {
            if (info.id > max) max = info.id;
        }
        return max + 1;
    }

    private DataFileInfo get(List<DataFileInfo> list, int numRssi, SampleWindow window) {
        for (DataFileInfo info : list) {
            if (info.numRssi == numRssi && info.window.equals(window)) {
                return info;
            }
        }
        return null;
    }

    public DataFileInfo getBt(int numRssi, SampleWindow window) {
        return get(bt, numRssi, window);
    }
    public DataFileInfo getBtle(int numRssi, SampleWindow window) {
        return get(btle, numRssi, window);
    }
    public DataFileInfo getWifi(int numRssi, SampleWindow window) {
        return get(wifi, numRssi, window);
    }
    public DataFileInfo getWifi5g(int numRssi, SampleWindow window) {
        return get(wifi5g, numRssi, window);
    }

    /** May return an existing {@link DataFileInfo} if a matching one already exists */
    public DataFileInfo addBt(int numRssi, SampleWindow window) {
        DataFileInfo info = get(bt, numRssi, window);
        if (info != null) return info;

        info = new DataFileInfo(getNextId(bt), numRssi, window);
        bt.add(info);

        btRW.writeLines(Collections.singletonList(info.toString()), true,
                new AbsTextReaderWriter.WriteListener() {
                    @Override
                    public void onWriteSucceed(int linesWritten) {

                    }

                    @Override
                    public void onWriteFailed(IOException e) {
                        Log.e(TAG, "Failed writing BT info file.", e);
                    }
                });
        return info;
    }

    /** May return an existing {@link DataFileInfo} if a matching one already exists */
    public DataFileInfo addBtle(int numRssi, SampleWindow window) {
        DataFileInfo info = get(btle, numRssi, window);
        if (info != null) return info;

        info = new DataFileInfo(getNextId(btle), numRssi, window);
        btle.add(info);

        btleRW.writeLines(Collections.singletonList(info.toString()), true,
                new AbsTextReaderWriter.WriteListener() {
                    @Override
                    public void onWriteSucceed(int linesWritten) {

                    }

                    @Override
                    public void onWriteFailed(IOException e) {
                        Log.e(TAG, "Failed writing BTLE info file.", e);
                    }
                });
        return info;
    }

    /** May return an existing {@link DataFileInfo} if a matching one already exists */
    public DataFileInfo addWifi(int numRssi, SampleWindow window) {
        DataFileInfo info = get(wifi, numRssi, window);
        if (info != null) return info;

        info = new DataFileInfo(getNextId(wifi), numRssi, window);
        wifi.add(info);

        wifiRW.writeLines(Collections.singletonList(info.toString()), true,
                new AbsTextReaderWriter.WriteListener() {
                    @Override
                    public void onWriteSucceed(int linesWritten) {

                    }

                    @Override
                    public void onWriteFailed(IOException e) {
                        Log.e(TAG, "Failed writing WIFI info file.", e);
                    }
                });
        return info;
    }

    /** May return an existing {@link DataFileInfo} if a matching one already exists */
    public DataFileInfo addWifi5g(int numRssi, SampleWindow window) {
        DataFileInfo info = get(wifi5g, numRssi, window);
        if (info != null) return info;

        info = new DataFileInfo(getNextId(wifi5g), numRssi, window);
        wifi5g.add(info);

        wifi5gRW.writeLines(Collections.singletonList(info.toString()), true,
                new AbsTextReaderWriter.WriteListener() {
                    @Override
                    public void onWriteSucceed(int linesWritten) {

                    }

                    @Override
                    public void onWriteFailed(IOException e) {
                        Log.e(TAG, "Failed writing WIFI5G info file.", e);
                    }
                });
        return info;
    }
}
