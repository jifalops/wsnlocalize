package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    private TextView eventLogView, deviceLogView, collectedCountView, sentCountView, toSendCountView;
    private EditText distanceView, deviceIdView;
    private Switch collectSwitch;
    private Button sendButton;

    private final List<Device> devices = new ArrayList<>();
    private final List<RssiRequest.RssiRecord> rssiRecords = new ArrayList<>();

    private int deviceId, collectedCount, sentCount, toSendCount;
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
        sentCountView = (TextView) findViewById(R.id.sentCount);
        toSendCountView = (TextView) findViewById(R.id.toSendCount);
        deviceIdView = (EditText) findViewById(R.id.deviceId);
        distanceView = (EditText) findViewById(R.id.distanceMeters);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);
        sendButton = (Button) findViewById(R.id.sendButton);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.setSelectAllOnFocus(true);
        distanceView.setSelectAllOnFocus(true);
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
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getInstance().sendRequest(new RssiRequest(rssiRecords,
                        new Response.Listener<AbsRequest.MyResponse>() {
                    @Override
                    public void onResponse(AbsRequest.MyResponse response) {
                        if (response.responseCode != 200) {
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
        collectedCount = prefs.getInt("collected", 0);
        sentCount = prefs.getInt("sent", 0);
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
    protected void onResume() {
        super.onResume();
        addDevice(new Device("name", "asdf", "qwer"));
        addDevice(new Device("name2", "zxcv", "fghj"));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void addDevice(Device d) {
        devices.add(d);
        deviceLogView.append(devices.indexOf(d) + " " + d.toString());
    }
}
