package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.RssiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiActivity extends Activity {
    private static final int REQUEST_BT_DISCOVERABLE = 1;

    private static class Device {
        String wifiMac, btMac, name;
        public Device(String name, String wifiMac, String btMac) {
            this.wifiMac = wifiMac;
            this.btMac = btMac;
            this.name = name;
        }
        @Override
        public String toString() {
            return name + " " + wifiMac + " " + btMac;
        }
    }

    private TextView eventLogView, deviceLogView, collectedCountView, toSendCountView;

    private final List<Device> devices = new ArrayList<>();
    private final List<RssiRequest.RssiRecord> rssiRecords = new ArrayList<>();

    private int deviceId, collectedCount, toSendCount;
    private float distance;
    private boolean collectEnabled;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi);
        eventLogView = (TextView) findViewById(R.id.eventLog);
        deviceLogView = (TextView) findViewById(R.id.deviceLog);
        collectedCountView = (TextView) findViewById(R.id.collectedCount);
        toSendCountView = (TextView) findViewById(R.id.toSendCount);

        final EditText deviceIdView = (EditText) findViewById(R.id.deviceId);
        final EditText distanceView = (EditText) findViewById(R.id.distanceMeters);
        Switch collectSwitch = (Switch) findViewById(R.id.collectSwitch);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        ((TextView) findViewById(R.id.wifiMacView)).setText(App.getInstance().getWifiMac());
        ((TextView) findViewById(R.id.btMacView)).setText(App.getInstance().getBtMac());

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int id = Integer.valueOf(s.toString());
                    Device d = devices.get(id);
                    if (d != null) deviceId = id;
                } catch (Exception e) {
                    deviceIdView.setText(deviceId + "");
                }
            }
        });
        distanceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    distance = Float.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    distanceView.setText(distance + "");
                }
            }
        });
        collectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                collectEnabled = isChecked;
                if (isChecked) {
                    startCollection();
                } else {
                    stopCollection();
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<RssiRequest.RssiRecord> records = new ArrayList<>(rssiRecords);
                rssiRecords.clear();
                App.getInstance().sendRequest(new RssiRequest(records,
                        new Response.Listener<AbsRequest.MyResponse>() {
                            @Override
                            public void onResponse(AbsRequest.MyResponse response) {
                                if (response.responseCode == 200) {

                                } else {
                                    Toast.makeText(RssiActivity.this,
                                            response.responseCode + ": " + response.responseMessage +
                                                    " Result: " + response.queryResult,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(RssiActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }));
            }
        });

        prefs = getSharedPreferences("rssi", MODE_PRIVATE);
    }

    private void autoScroll(final ScrollView sv, final TextView tv) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless device is discoverable.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) {
            return;
        }
        collectedCount = prefs.getInt("collected", 0);
        rssiRecords.addAll(deserializeRssiRecords(prefs.getString("toSend", "")));
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit().putInt("collected", collectedCount)
                .putString("toSend", serializeRssiRecords(rssiRecords))
                .apply();
    }

    private void addDevice(Device device) {
        boolean found = false;
        for (Device d : devices) {
            if (d.wifiMac.equals(device.wifiMac) || d.btMac.equals(device.btMac)) {
                found = true;
                break;
            }
        }
        if (!found) {
            devices.add(device);
            deviceLogView.append(devices.indexOf(device) + " " + device.toString());
        }
    }

    private void addRecord(RssiRequest.RssiRecord record) {
        rssiRecords.add(record);
        collectedCount++;
        toSendCount = rssiRecords.size();
        updateTextViews();
    }

    private void updateTextViews() {
        collectedCountView.setText(collectedCount+"");
        toSendCountView.setText(toSendCount + "");
    }

    public void startCollection() {
        BtBeacon btBeacon = BtBeacon.getInstance(this);
        btBeacon.registerListener(btBeaconListener);
        btBeacon.startBeaconing(this, REQUEST_BT_DISCOVERABLE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            BtLeBeacon btLeBeacon = BtLeBeacon.getInstance(this);
//        }
    }

    public void stopCollection() {
        BtBeacon btBeacon = BtBeacon.getInstance(this);
        btBeacon.stopBeaconing(this, REQUEST_BT_DISCOVERABLE);
        btBeacon.unregisterListener(btBeaconListener);
    }

    private String serializeRssiRecords(List<RssiRequest.RssiRecord> records) {
        JSONArray array = new JSONArray();
        for (RssiRequest.RssiRecord r : records) {
            array.put(r.toString());
        }
        return array.toString();
    }

    private List<RssiRequest.RssiRecord> deserializeRssiRecords(String records) {
        List<RssiRequest.RssiRecord> list = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONArray(records);
            for (int i = 0, len = array.length(); i < len; i++) {
                list.add(new RssiRequest.RssiRecord(array.getString(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addDevice(new Device(device.getName(), "", device.getAddress()));
            addRecord(new RssiRequest.RssiRecord(
                    App.getInstance().getWifiMac(), App.getInstance().getBtMac(),
                    "", device.getAddress(), device.getName(), "BtBeacon", rssi, distance));
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {

        }

        @Override
        public void onDiscoveryRestarting() {

        }
    };
}
