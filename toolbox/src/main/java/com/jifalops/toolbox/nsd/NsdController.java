package com.jifalops.toolbox.nsd;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.jifalops.toolbox.socket.MyConnectionSocket;
import com.jifalops.toolbox.socket.MyServerSocket;
import com.jifalops.toolbox.socket.MySocketManager;
import com.jifalops.toolbox.socket.Sockets;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The NSD controller registers a discoverable service on the device's local network using sockets.
 * See {@link MySocketManager}.
 * It also initiates reconnecting.
 *
 * @author Jacob Phillips (01/2015, jifalops at gmail)
 */
public class NsdController {
    private static final String TAG = NsdController.class.getSimpleName();

    private final NsdHelper nsdHelper;
    private final MySocketManager socketManager;

    public NsdController(Context ctx, String serviceName, NsdServiceFilter filter) {
        nsdHelper = new NsdHelper(ctx, serviceName, filter);
        socketManager = new MySocketManager();
    }

    public NsdHelper getNsdHelper() {
        return nsdHelper;
    }
    public MySocketManager getSocketManager() {
        return socketManager;
    }

    public void startNsd() {
        nsdHelper.registerListener(nsdListener);
        nsdHelper.initializeNsd();

        socketManager.registerListener(socketListener);
        socketManager.startServer();

        nsdHelper.discoverServices();
    }

    public void stopNsd() {
        nsdHelper.stopDiscovery();
        socketManager.stopServer();
        socketManager.stopConnections();
        nsdHelper.unregisterService();
        socketManager.unregisterListener(socketListener);
        nsdHelper.unregisterListener(nsdListener);
    }

    /** Convenience method for {@link MySocketManager#send(String)} */
    public int send(String msg) {
        return socketManager.send(msg);
    }

    /** Convenience method for {@link MySocketManager#send(InetAddress, String)} */
    public int send(InetAddress address, String msg) {
        return socketManager.send(address, msg);
    }

    private final MySocketManager.SocketListener socketListener = new MySocketManager.SocketListener() {

        @Override
        public void onServerAcceptedClientSocket(MyServerSocket mss, Socket socket) {
            Log.i(TAG, "Server accepted socket to " + Sockets.toString(socket));
            for (NsdListener l : listeners) {
                l.onServerAcceptedClientSocket(mss, socket);
            }
        }

        @Override
        public void onServerFinished(MyServerSocket mss) {
            Log.v(TAG, "Server on port " + mss.getPort() + " closed. It had accepted " +
                    mss.getAcceptCount() + " sockets total.");
            for (NsdListener l : listeners) {
                l.onServerFinished(mss);
            }
        }

        @Override
        public void onServerSocketListening(MyServerSocket mss, ServerSocket ss) {
            Log.v(TAG, "Server now listening on port " + ss.getLocalPort());
            for (NsdListener l : listeners) {
                l.onServerSocketListening(mss, ss);
            }
            nsdHelper.registerService(ss.getLocalPort());
        }

        @Override
        public void onMessageSent(MyConnectionSocket mcs, String msg) {
            Log.v(TAG, "Sent message to " + Sockets.toString(mcs.getSocket()));
            for (NsdListener l : listeners) {
                l.onMessageSent(mcs, msg);
            }
        }

        @Override
        public void onMessageReceived(MyConnectionSocket mcs, String msg) {
            Log.v(TAG, "Received message from " + Sockets.toString(mcs.getSocket()));
            for (NsdListener l : listeners) {
                l.onMessageReceived(mcs, msg);
            }
        }

        @Override
        public void onClientFinished(MyConnectionSocket mcs) {
            Log.v(TAG, "Client finished: " + Sockets.toString(mcs.getAddress(), mcs.getPort()));
            for (NsdListener l : listeners) {
                l.onClientFinished(mcs);
            }
        }

        @Override
        public void onClientSocketCreated(MyConnectionSocket mcs, Socket socket) {
            Log.v(TAG, "Client socket created for " + Sockets.toString(socket));
            for (NsdListener l : listeners) {
                l.onClientSocketCreated(mcs, socket);
            }
        }
    };

    private final NsdHelper.NsdHelperListener nsdListener = new NsdHelper.NsdHelperListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo info) {
            for (NsdListener l : listeners) {
                l.onServiceRegistered(info);
            }
        }

        @Override
        public void onAcceptableServiceResolved(NsdServiceInfo info) {
            for (NsdListener l : listeners) {
                l.onAcceptableServiceResolved(info);
            }
            socketManager.startConnection(new MyConnectionSocket(info.getHost(), info.getPort()));
        }
    };

    public static abstract class NsdListener implements
            NsdHelper.NsdHelperListener, MySocketManager.SocketListener {}
    private final List<NsdListener> listeners = new ArrayList<>(1);
    public boolean registerListener(NsdListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(NsdListener l) {
        return listeners.remove(l);
    }
}
