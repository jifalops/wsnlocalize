/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jifalops.wsnlocalize.nsd;

import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.R;
import com.jifalops.wsnlocalize.socket.MyConnectionSocket;
import com.jifalops.wsnlocalize.socket.MyServerSocket;
import com.jifalops.wsnlocalize.wifi.WifiHelper;

import java.net.ServerSocket;
import java.net.Socket;

public class NsdDemoActivity extends Activity {
    private NsdController nsdController;
    private TextView statusView;
    private EditText sendView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsd);
        statusView = (TextView) findViewById(R.id.status);
        sendView = (EditText) findViewById(R.id.chatInput);
        autoScrollTextView(statusView, (ScrollView) findViewById(R.id.scrollView));

        WifiHelper wifi = WifiHelper.getInstance(this);
        wifi.setWifiEnabled(true);
        String service = App.NSD_SERVICE_PREFIX + wifi.getMacAddress();

        nsdController = new NsdController(this, service, new NsdServiceFilter() {
            @Override
            public boolean isAcceptableService(NsdServiceInfo info) {
                return info.getServiceName().startsWith(App.NSD_SERVICE_PREFIX);
            }
        });
    }

    private void autoScrollTextView(TextView tv, final ScrollView sv) {
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nsdController.registerListener(nsdListener);
        nsdController.startNsd();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nsdController.stopNsd();
        nsdController.unregisterListener(nsdListener);
    }

    public void clickSend(View v) {
        String msg = sendView.getText().toString();
        if (!msg.isEmpty()) {
            nsdController.send(msg);
        }
        sendView.setText("");
    }

    public void addChatLine(final String line) {
        statusView.append("\n" + line);
    }


    private final NsdController.NsdListener nsdListener = new NsdController.NsdListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo info) {
            addChatLine("Service registered.");
        }

        @Override
        public void onAcceptableServiceResolved(NsdServiceInfo info) {
            addChatLine("Service resolved for " + info.getHost().getHostAddress());
        }

        @Override
        public void onServerAcceptedClientSocket(MyServerSocket mss, Socket socket) {
            addChatLine("Accepted connection to " + socket.getInetAddress().getHostAddress());
        }

        @Override
        public void onServerFinished(MyServerSocket mss) {
            addChatLine("Server closed. Accepted " + mss.getAcceptCount() + " connections.");
        }

        @Override
        public void onServerSocketListening(MyServerSocket mss, ServerSocket ss) {
            addChatLine("Server started on port " + ss.getLocalPort());
        }

        @Override
        public void onMessageSent(MyConnectionSocket mcs, String msg) {
            addChatLine("'" + msg + "' sent to " + mcs.getSocket().getInetAddress().getHostAddress());
        }

        @Override
        public void onMessageReceived(MyConnectionSocket mcs, String msg) {
            addChatLine("'" + msg + "' received from " + mcs.getSocket().getInetAddress().getHostAddress());
        }

        @Override
        public void onClientFinished(MyConnectionSocket mcs) {
            addChatLine(mcs.getAddress().getHostAddress() + " closed.");
        }

        @Override
        public void onClientSocketCreated(MyConnectionSocket mcs, Socket socket) {
            addChatLine("Created connection to " + socket.getInetAddress().getHostAddress());
        }
    };
}
