package com.orbbec.orbbecsdkexamples.activity;

import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.orbbecsdkexamples.R;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Hot plugin Viewer
 */
public class DeviceHotPluginActivity extends BaseActivity {
    private static final String TAG = "DeviceHotPluginActivity";

    private DeviceList mCurrentList;
    private TextView mDeviceChangeStatusTv;
    private Button mRebootDeviceBtn;

    private final Locale locale = Locale.getDefault();
    private final int sdkVersion = Build.VERSION.SDK_INT;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            setText(mDeviceChangeStatusTv, printDeviceList("added", deviceList));
            mCurrentList = mOBContext.queryDevices();
            setText(mDeviceChangeStatusTv, printDeviceList("connected", mCurrentList));
            runOnUiThread(() -> mRebootDeviceBtn.setEnabled(true));
            // Release deviceList
            deviceList.close();
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            setText(mDeviceChangeStatusTv, printDeviceList("removed", deviceList));
            mCurrentList = mOBContext.queryDevices();
            setText(mDeviceChangeStatusTv, printDeviceList("connected", mCurrentList));
            // Release deviceList
            deviceList.close();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Device-Hot Plugin");
        setContentView(R.layout.activity_device_hot_plugin);
        mDeviceChangeStatusTv = findViewById(R.id.tv_device_change_status);
        mRebootDeviceBtn = findViewById(R.id.btn_reboot_devices);
        mRebootDeviceBtn.setOnClickListener(v -> {
            rebootDevices(mCurrentList);
            mRebootDeviceBtn.setEnabled(false);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
        if (mCurrentList == null) {
            mDeviceChangeStatusTv.append("Waiting for device connection...\n\n");
        }
    }

    @Override
    protected void onStop() {
        try {
            if (mCurrentList != null) {
                mCurrentList.close();
                mCurrentList = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
        releaseSDK();
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void setText(TextView tv, String text) {
        runOnUiThread(() -> tv.append(text));
    }

    private String printDeviceList(String prompt, DeviceList deviceList) {
        int count = deviceList.getDeviceCount();
        if (count == 0) {
            return "The device is empty.\n\n";
        }
        StringBuilder output = new StringBuilder();

        if (sdkVersion >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            output.append(String.format(locale, "Time: %s\n", LocalDateTime.now().format(formatter)));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
            output.append(String.format(locale, "Time: %s\n", sdf.format(new Date())));
        }
        output.append(String.format(locale, "%d device(s) %s:\n", count, prompt));
        try {
            for (int i = 0; i < count; ++i) {
                String name = deviceList.getName(i);
                String uid = deviceList.getUid(i);
                int vid = deviceList.getVid(i);
                int pid = deviceList.getPid(i);
                String serialNumber = deviceList.getDeviceSerialNumber(i);
                String connection = deviceList.getConnectionType(i);

                output.append(String.format("  - device name: %s, uid: %s, vid: 0x%04X, pid: 0x%04X, serial number: %s, connection: %s\n",
                        name, uid, vid, pid, serialNumber, connection));
            }
            String divider = createDivider();
            output.append(divider).append("\n");
        } catch (Exception e) {
            Log.e(TAG, "printDeviceList: " + e.getMessage());
            return "";
        }
        return output.toString();
    }

    private String createDivider() {
        int width = mDeviceChangeStatusTv.getWidth();
        Paint paint = new Paint();
        paint.setTextSize(mDeviceChangeStatusTv.getTextSize());
        float cw = paint.measureText("-");
        int cc = (int) (width / cw);
        return new String(new char[cc]).replace('\0', '-');
    }

    private void rebootDevices(DeviceList deviceList) {
        try {
            if (deviceList != null) {
                for (int i = 0; i < deviceList.getDeviceCount(); ++i) {
                    Device device = deviceList.getDevice(i);
                    device.reboot();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "rebootDevices: " + e.getMessage());
        }
    }
}