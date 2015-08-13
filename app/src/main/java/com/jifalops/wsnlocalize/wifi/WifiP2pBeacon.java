package com.jifalops.wsnlocalize.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * As of 2015-08-13 WiFi P2P methods do not include signal strength.
 * Look for changes in a newer version of
 *  http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android-apps/5.1.1_r1/com/android/settings/wifi/p2p/WifiP2pPeer.java#43
 * or possibly
 *  http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/net/wifi/p2p/WifiP2pDevice.java
 */
public class WifiP2pBeacon {

    private static WifiP2pBeacon instance;
    public static WifiP2pBeacon getInstance(Context ctx) {
        if (instance == null) instance = new WifiP2pBeacon(ctx.getApplicationContext());
        return instance;
    }

    private Context ctx;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    private WifiP2pBeacon(Context ctx) {
        this.ctx = ctx;
        manager = (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(ctx, Looper.getMainLooper(), channelListener);
    }

    public void startBeaconing() {
        manager.discoverPeers(channel, discoverListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        ctx.registerReceiver(receiver, filter);
    }

    public void stopBeaconing() {
        ctx.unregisterReceiver(receiver);
    }

    private final WifiP2pManager.ChannelListener channelListener = new WifiP2pManager.ChannelListener() {
        @Override
        public void onChannelDisconnected() {
            for (WifiP2pBeaconListener l : listeners) {
                l.onChannelDisconnected();
            }
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                boolean enabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
                for (WifiP2pBeaconListener l : listeners) {
                    l.onStateChanged(enabled);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                manager.requestPeers(channel, peerListener);
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                for (WifiP2pBeaconListener l : listeners) {
                    l.onThisDeviceChanged(device);
                }
            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
                boolean enabled = state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED;
                for (WifiP2pBeaconListener l : listeners) {
                    l.onDiscoveryChanged(enabled);
                }
            }
        }
    };

    private final WifiP2pManager.ActionListener discoverListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailure(int reason) {
            String msg = "(reason unknown)";
            switch (reason) {
                case WifiP2pManager.BUSY:
                    msg = "busy";
                    break;
                case WifiP2pManager.ERROR:
                    msg = "error";
                    break;
                case WifiP2pManager.P2P_UNSUPPORTED:
                    msg = "P2P is unsupported";
                    break;
            }
            for (WifiP2pBeaconListener l : listeners) {
                l.onDiscoveryFailed(reason, msg);
            }
        }
    };

    private final WifiP2pManager.PeerListListener peerListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            List<WifiP2pDevice> list = new ArrayList<>(peers.getDeviceList());
            for (WifiP2pBeaconListener l : listeners) {
                l.onPeersAvailable(list);
            }
        }
    };

    public interface WifiP2pBeaconListener {
        /** See {@link WifiP2pManager.ChannelListener#onChannelDisconnected()} */
        void onChannelDisconnected();
        /** See {@link WifiP2pManager.ActionListener#onFailure(int)} */
        void onDiscoveryFailed(int code, String msg);
        /** See {@link WifiP2pManager#WIFI_P2P_DISCOVERY_CHANGED_ACTION} */
        void onDiscoveryChanged(boolean enabled);
        /** See {@link WifiP2pManager.PeerListListener#onPeersAvailable(WifiP2pDeviceList)} */
        void onPeersAvailable(List<WifiP2pDevice> peers);
        /** See {@link WifiP2pManager#WIFI_P2P_STATE_CHANGED_ACTION} */
        void onStateChanged(boolean enabled);
        /** See {@link android.net.wifi.p2p.WifiP2pManager#WIFI_P2P_THIS_DEVICE_CHANGED_ACTION */
        void onThisDeviceChanged(WifiP2pDevice device);
    }
    private final List<WifiP2pBeaconListener> listeners = new ArrayList<>(1);
    public boolean registerListener(WifiP2pBeaconListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(WifiP2pBeaconListener l) {
        return listeners.remove(l);
    }
}
