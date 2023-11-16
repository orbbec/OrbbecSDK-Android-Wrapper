package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Sensor;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.LocalUtils;

import java.util.Locale;
import java.util.Map;

public class HelloOrbbecActivity extends BaseActivity {
    private static final String TAG = "HelloOrbbecActivity";

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            Log.d(TAG, "onDeviceAttach");
            try {
                deviceList.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dumpDevices();
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            Log.d(TAG, "onDeviceDetach");
            try {
                deviceList.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dumpDevices();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("HelloOrbbec");
        setContentView(R.layout.activity_hello_orbbec);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        releaseSDK();
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void dumpDevices() {
        try (DeviceList deviceList = mOBContext.queryDevices()) {
            try {
                StringBuilder builder = new StringBuilder();
                // 2.Check the SDK version
                builder.append("SDK VersionName = " + OBContext.getVersionName() + "\n");
                builder.append("SDK StageVersion = " + OBContext.getStageVersion() + "\n");

                // 3.Get the number of devices
                int deviceCount = deviceList.getDeviceCount();
                if (deviceCount <= 0) {
                    builder.append("Not device found.");
                }

                for (int i = 0; i < deviceCount; ++i) {
                    // 4.Create device based on device index
                    Device device = deviceList.getDevice(i);

                    // 5.Get device information
                    DeviceInfo info = device.getInfo();

                    // 6.Get device version information
                    if (deviceCount > 1) {
                        if (i > 0) {
                            builder.append("\n");
                        }
                        builder.append("Device[" + i + "]: \n");
                    }
                    builder.append("Name: " + info.getName() + "\n");
                    builder.append("Vid: " + LocalUtils.formatHex04(info.getVid()) + "\n");
                    builder.append("Pid: " + LocalUtils.formatHex04(info.getPid()) + "\n");
                    builder.append("Uid: " + info.getUid() + "\n");
                    builder.append("SN: " + info.getSerialNumber() + "\n");
                    builder.append("connectType: " + info.getConnectionType() + "\n");
                    String firmwareVersion = info.getFirmwareVersion();
                    builder.append("FirmwareVersion: " + firmwareVersion + "\n");
                    builder.append(dumpExtensionInfo(info.getExtensionInfo()));

                    // 7.Iterate through the sensors of the current device
                    for (Sensor sensor : device.querySensors()) {
                        // 8.Query sensor type
                        builder.append("Sensor:    " + sensor.getType() + "\n");
                    }

                    // 8.Release device information
                    info.close();

                    // 9.Release device resources
                    device.close();
                } // for deviceList

                runOnUiThread(() -> {
                    TextView txtInfo = findViewById(R.id.txt_info);
                    txtInfo.setText(builder.toString());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String dumpExtensionInfo(String extensionInfo) {
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(extensionInfo)) {
            builder.append("extensionInfo: ").append(extensionInfo).append("\n");
            return builder.toString();
        }

        builder.append("extensionInfo: ");
        try {
            JsonElement jsonParser = JsonParser.parseString(extensionInfo);
            JsonObject rootObject = jsonParser.getAsJsonObject();
            builder.append("\n");
            for (Map.Entry<String, JsonElement> e : rootObject.entrySet()) {
                if (TextUtils.equals("extensioninfo", e.getKey().toLowerCase(Locale.US))) {
                    for (Map.Entry<String, JsonElement> e2 : e.getValue().getAsJsonObject().entrySet()) {
                        builder.append("        ").append(e2.getKey()).append(": ").append(e2.getValue().toString()).append("\n");
                    }
                } else {
                    builder.append("    ").append(e.getKey()).append(": ").append(e.getValue().toString()).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            builder.append(extensionInfo);
        }
        return builder.toString();
    }

}