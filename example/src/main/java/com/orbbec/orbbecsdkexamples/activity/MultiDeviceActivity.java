package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.adapter.DeviceControllerAdapter;
import com.orbbec.orbbecsdkexamples.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-Device
 */
public class MultiDeviceActivity extends BaseActivity {
    private static final String TAG = "MultiDeviceCopyActivity";

    private ListView mListView;
    private DeviceControllerAdapter mDeviceControllerAdapter;
    private List<DeviceBean> mDeviceBeanList = new ArrayList<>();

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            Log.d(TAG, "onDeviceAttach: " + deviceList.getDeviceCount());
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
                    devInfo.close();
                    runOnUiThread(() -> {
                        mDeviceControllerAdapter.addItem(new DeviceBean(name, uid, connectionType, device));
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Release DeviceList resources
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            Log.d(TAG, "onDeviceDetach: " + deviceList.getDeviceCount());
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
        setTitle("MultiDevice");
        setContentView(R.layout.activity_multi_device);

        mListView = (ListView) findViewById(R.id.lv_multi_device);
        // Create a device control adapter
        mDeviceControllerAdapter = new DeviceControllerAdapter(mDeviceBeanList, this);

        mListView.setAdapter(mDeviceControllerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            // Release resources
            for (DeviceBean deviceBean : mDeviceBeanList) {
                try {
                    // Release device resources
                    deviceBean.getDevice().close();
                } catch (Exception e) {
                    Log.w(TAG, "onDestroy: " + e.getMessage());
                }
            }
            mDeviceBeanList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        releaseSDK();
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }
}