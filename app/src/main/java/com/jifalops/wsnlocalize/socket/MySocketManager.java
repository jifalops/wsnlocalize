package com.jifalops.wsnlocalize.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The socket manager contains a list of connections (clients), and a single listening server socket.
 * It also uses a Handler to transfer server/connection events from their respective threads
 * to the main application thread.
 *
 * @author Jacob Phillips (12/2014, jphilli85 at gmail)
 */
public class MySocketManager {
    private static final String TAG = MySocketManager.class.getSimpleName();

    private final List<MyConnectionSocket> connections = Collections.synchronizedList(
            new ArrayList<MyConnectionSocket>());
    private final MyServerSocket server = new MyServerSocket();

    /**
     * Send a message to all connections.
     * @return the number of connections the message was sent to.
     */
    public int send(String msg) {
        int count = 0;
        for (MyConnectionSocket mcs : connections) {
            if (mcs.send(msg)) ++count;
        }
        return count;
    }

    /**
     * Send a message to a specific connection
     * @return the number of connections the message was sent to (1 or 0).
     */
    public int send(InetAddress address, String msg) {
        int count = 0;
        for (MyConnectionSocket mcs : connections) {
            if (mcs.getAddress().getHostAddress().equals(address.getHostAddress())) {
                if (mcs.send(msg)) ++count;
            }
        }
        return count;
    }

    public List<MyConnectionSocket> getConnections() { return connections; }

    public synchronized boolean hasAddress(InetAddress address) {
        for (MyConnectionSocket mcs : connections) {
            if (mcs.getAddress().getHostAddress().equals(address.getHostAddress())) {
                return true;
            }
        }
        return false;
    }

    public synchronized void startServer() {
        startServer(server.getPort());
    }
    public synchronized void startServer(int port) {
        server.registerListener(serverListener);
        server.start(port);
    }

    public synchronized void stopServer() {
        server.stop();
        server.unregisterListener(serverListener);
    }

    public synchronized boolean startConnection(MyConnectionSocket mcs) {
        if (hasAddress(mcs.getAddress())) {
            Log.d(TAG, "Already have connection to " + mcs.getAddress()+":"+mcs.getPort() + ", ignoring.");
            return false;
        }
        mcs.registerListener(connectionSocketListener);
        connections.add(mcs);
        mcs.start();
        return true;
    }

    public synchronized void stopConnections() {
        for (MyConnectionSocket mcs : connections) {
            mcs.stop();
            mcs.unregisterListener(connectionSocketListener);
        }
        connections.clear();
    }


    private final MyServerSocket.ServerListener serverListener = new MyServerSocket.ServerListener() {
        @Override
        public void onServerSocketListening(final MyServerSocket mss, final ServerSocket ss) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onServerSocketListening(mss, ss);
                    }
                }
            });
        }

        @Override
        public void onServerAcceptedClientSocket(final MyServerSocket mss, final Socket socket) {
            startConnection(new MyConnectionSocket(socket));
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onServerAcceptedClientSocket(mss, socket);
                    }
                }
            });
        }

        @Override
        public void onFinished(final MyServerSocket mss) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onServerFinished(mss);
                    }
                }
            });
        }
    };

    private final MyConnectionSocket.ConnectionListener connectionSocketListener = new MyConnectionSocket.ConnectionListener() {
        @Override
        public void onSocketCreated(final MyConnectionSocket mcs, final Socket socket) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onClientSocketCreated(mcs, socket);
                    }
                }
            });
        }

        @Override
        public void onMessageSent(final MyConnectionSocket mcs, final String s) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onMessageSent(mcs, s);
                    }
                }
            });
        }

        @Override
        public void onMessageReceived(final MyConnectionSocket mcs, final String s) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onMessageReceived(mcs, s);
                    }
                }
            });
        }

        @Override
        public void onFinished(final MyConnectionSocket mcs) {
            connections.remove(mcs);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (SocketListener l : listeners) {
                        l.onClientFinished(mcs);
                    }
                }
            });
        }
    };

    /** Called on main thread. */
    public interface SocketListener {
        void onServerAcceptedClientSocket(MyServerSocket mss, Socket socket);
        void onServerFinished(MyServerSocket mss);
        void onServerSocketListening(MyServerSocket mss, ServerSocket socket);
        void onMessageSent(MyConnectionSocket mcs, String msg);
        void onMessageReceived(MyConnectionSocket mcs, String msg);
        void onClientFinished(MyConnectionSocket mcs);
        void onClientSocketCreated(MyConnectionSocket mcs, Socket socket);
    }
    private final List<SocketListener> listeners = new ArrayList<>(1);
    public boolean registerListener(SocketListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(SocketListener l) {
        return listeners.remove(l);
    }
}
