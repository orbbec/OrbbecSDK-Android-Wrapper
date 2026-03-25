package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.adapter.DeviceControllerAdapter;
import com.orbbec.orbbecsdkexamples.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-Devices
 */
public class AdvancedMultiDevicesActivity extends BaseActivity {
    private static final String TAG = "AdvancedMultiDevicesActivity";

    private DeviceControllerAdapter mDeviceControllerAdapter;
    private final List<DeviceBean> mDeviceBeanList = new ArrayList<>();

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                int count = deviceList.getDeviceCount();
                for (int i = 0; i < count; i++) {
                    // Create Device from deviceList with index
                    Device device = deviceList.getDevice(i);
                    // Get device information
                    DeviceInfo devInfo = device.getInfo();
                    // Get device name
                    String name = devInfo.getName();
                    // Get device uid
                    String uid = devInfo.getUid();
                    // Get device connection type
                    String connectionType = devInfo.getConnectionType();
                    // Release DeviceInfo resources
                    //devInfo.close();
                    // Skip already-connected devices (prevents duplicates on SDK re-register)
                    boolean alreadyExists = false;
                    for (DeviceBean bean : mDeviceBeanList) {
                        if (bean.getDeviceUid().equals(uid)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        runOnUiThread(() -> {
                            mDeviceControllerAdapter.addItem(new DeviceBean(name, uid, connectionType, device));
                        });
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                // Release DeviceList resources
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                for (DeviceBean deviceBean : mDeviceBeanList) {
                    // Determine disconnection devices by uid
                    if (deviceBean.getDeviceUid().equals(deviceList.getUid(0))) {
                        // Release disconnection equipment resources
                        deviceBean.getDevice().close();
                        runOnUiThread(() -> {
                            mDeviceControllerAdapter.deleteItem(deviceBean);
                        });
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "onDeviceDetach: " + e.getMessage());
            } finally {
                // Release DeviceList resources
                deviceList.close();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Advanced-Multi Devices");
        setContentView(R.layout.activity_advanced_multi_devices);

        ListView deviceListViews = findViewById(R.id.lv_multi_devices);
        // Create a device control adapter
        mDeviceControllerAdapter = new DeviceControllerAdapter(mDeviceBeanList, this);

        deviceListViews.setAdapter(mDeviceControllerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台回到前台时，通知 adapter 重绘，触发 getView() 按 isRunning 标志恢复各设备流
        if (mDeviceControllerAdapter != null) {
            mDeviceControllerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        // 退到后台时，停止所有正在运行的 sensor 流，但保留 isRunning 标志以便恢复
        for (DeviceBean deviceBean : mDeviceBeanList) {
            try {
                Device device = deviceBean.getDevice();
                if (deviceBean.isDepthRunning) {
                    Sensor depthSensor = device.getSensor(SensorType.DEPTH);
                    if (depthSensor != null) {
                        depthSensor.stop();
                    }
                }
                if (deviceBean.isColorRunning) {
                    Sensor colorSensor = device.getSensor(SensorType.COLOR);
                    if (colorSensor != null) {
                        colorSensor.stop();
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "onPause stop stream: " + e.getMessage());
            }
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        releaseSDK();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 退出 Activity 时释放全部设备资源
        try {
            for (DeviceBean deviceBean : mDeviceBeanList) {
                try {
                    deviceBean.getDevice().close();
                } catch (Exception e) {
                    Log.w(TAG, "onDestroy close device: " + e.getMessage());
                }
            }
            mDeviceBeanList.clear();
        } catch (Exception e) {
            Log.w(TAG, "onDestroy: " + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }
}