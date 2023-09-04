package com.orbbec.orbbecsdkexamples.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.LogSeverity;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Sensor;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.LocalUtils;

public class HelloOrbbecActivity extends AppCompatActivity {
    private static final String TAG = "HelloOrbbecActivity";

    private static boolean hasConfigLogFile = false;
    private OBContext mOBContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("HelloOrbbec");
        setContentView(R.layout.activity_hello_orbbec);
        TextView txtInfo = findViewById(R.id.txt_info);
        // Configure log storage (optional). The log file can only be configured once and must be before the OBContext object is created.
        if (!hasConfigLogFile) {
            OBContext.setLoggerToFile(LogSeverity.DEBUG, getExternalCacheDir().getAbsolutePath(), 20, 3);
            hasConfigLogFile = true;
        }

        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                Log.d(TAG, "onDeviceAttach");
                try {
                    StringBuilder builder = new StringBuilder();
                    // 2.Check the SDK version
                    builder.append("SDK VersionName = " + OBContext.getVersionName() + "\n");
                    builder.append("SDK StageVersion = " + OBContext.getStageVersion() + "\n");

                    // 3.Get the number of devices
                    int deviceCount = deviceList.getDeviceCount();

                    for (int i = 0; i < deviceCount; ++i) {
                        // 4.Create device based on device index
                        Device device = deviceList.getDevice(i);

                        // 5.Get device information
                        DeviceInfo info = device.getInfo();

                        // 6.Get device version information
                        builder.append("Name: " + info.getName() + "\n");
                        builder.append("Vid: " + LocalUtils.formatHex04(info.getVid()) + "\n");
                        builder.append("Pid: " + LocalUtils.formatHex04(info.getPid()) + "\n");
                        builder.append("Uid: " + info.getUid() + "\n");
                        builder.append("SN: " + info.getSerialNumber() + "\n");
                        builder.append("connectType: " + info.getConnectionType() + "\n");
                        String firmwareVersion = info.getFirmwareVersion();
                        builder.append("FirmwareVersion: " + firmwareVersion + "\n");

                        // 7.Iterate through the sensors of the current device
                        for (Sensor sensor : device.querySensors()) {
                            // 8.Query sensor type
                            builder.append("Sensor : type = " + sensor.getType() + "\n");
                        }
                        runOnUiThread(() -> txtInfo.setText(builder.toString()));

                        // 9.Release device information
                        info.close();

                        // 10.Release device resources
                        device.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 11.Release device list resources
                    deviceList.close();
                }
            }

            @Override
            public void onDeviceDetach(DeviceList deviceList) {
                Log.d(TAG, "onDeviceDetach");
                try {
                    deviceList.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            // Release SDK Context
            if (null != mOBContext) {
                mOBContext.close();
                mOBContext = null;
            }
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}