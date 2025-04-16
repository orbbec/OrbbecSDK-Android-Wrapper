package com.orbbec.orbbecsdkexamples.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.property.DevicePropertyInfo;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.property.DeviceProperty;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.types.PermissionType;
import com.orbbec.obsensor.property.PropertyType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.adapter.InformationAdapter;
import com.orbbec.orbbecsdkexamples.adapter.SpinnerContentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceControlActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "DeviceControlActivity";
    private List<Device> mDeviceList = new ArrayList<>();
    private Map<String, DevicePropertyInfo> mDevicePropertyMap = new HashMap<>();
    private List<String> mDeviceNameList = new ArrayList<>();
    private List<String> mPropertyNameList = new ArrayList<>();
    private SpinnerContentAdapter<String> mDeviceNameAdapter;
    private SpinnerContentAdapter<String> mPropertyAdapter;
    private Button mSetBtn;
    private EditText mSetEt;
    private Button mGetBtn;
    private TextView mGetTv;
    private Spinner mDeviceSp;
    private Spinner mPropertySp;
    private RecyclerView mInformationSectionRV;
    private InformationAdapter mInformationAdapter;
    private Device mSelectDevice;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                // 3.Add the obtained device to the device list
                mDeviceList.add(deviceList.getDevice(0));

                // 4.Update device list
                updateDeviceSpinnerList();
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                // 5.Release device list resources
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                // A device is disconnected, and the device list resource is released
                for (Device device : mDeviceList) {
                    device.close();
                }
                mDeviceList.clear();

                // Reacquire devices and update device list
                DeviceList curDeviceList = mOBContext.queryDevices();
                for (int i = 0; i < curDeviceList.getDeviceCount(); i++) {
                    mDeviceList.add(curDeviceList.getDevice(i));
                }
                curDeviceList.close();
                updateDeviceSpinnerList();

                // No device connection, clear sensor list and attribute list
                if (mDeviceList.isEmpty()) {
                    clearPropertySpinnerList();
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceDetach: " + e.getMessage());
            } finally {
                deviceList.close();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Device-Control");
        setContentView(R.layout.activity_device_control);
        // 1. Initialize views
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        release();
        releaseSDK();
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    /**
     * Resource release, release all devices in the device list (sensors will be released when the device
     * is released, there is no need to release sensor resources separately)
     * Release SDK Context
     */
    private void release() {
        try {
            // 设备列表资源释放
            for (Device device : mDeviceList) {
                device.close();
            }
            mDeviceList.clear();
        } catch (Exception e) {
            Log.e(TAG, "release: " + e.getMessage());
        }
    }

    private void initViews() {
        mSetBtn = findViewById(R.id.btn_set);
        mSetEt = findViewById(R.id.et_set);
        mGetBtn = findViewById(R.id.btn_get);
        mGetTv = findViewById(R.id.tv_get);

        // 1.1.Device list initialization
        mDeviceSp = findViewById(R.id.spi_device);
        mDeviceNameAdapter = new SpinnerContentAdapter<String>(this, android.R.layout.simple_spinner_item);
        mDeviceNameAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mDeviceSp.setAdapter(mDeviceNameAdapter);
        mDeviceSp.setOnItemSelectedListener(this);

        // 1.2.Property list initialization
        mPropertySp = findViewById(R.id.spi_instructions);
        mPropertyAdapter = new SpinnerContentAdapter<String>(this, android.R.layout.simple_spinner_item);
        mPropertyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mPropertySp.setAdapter(mPropertyAdapter);
        mPropertySp.setOnItemSelectedListener(this);

        mSetBtn.setOnClickListener(this);
        mGetBtn.setOnClickListener(this);

        // 1.3.Operation log column initialization
        mInformationSectionRV = (RecyclerView) findViewById(R.id.rv_information);
        mInformationAdapter = new InformationAdapter(this);
        mInformationSectionRV.setAdapter(mInformationAdapter);
        mInformationSectionRV.setLayoutManager(new LinearLayoutManager(this));
    }

    private void addNewMessage(String message) {
        mInformationAdapter.newMessage(message);
        mInformationSectionRV.scrollToPosition(mInformationAdapter.getItemCount() - 1);
    }

    private void updateDeviceSpinnerList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceNameList.clear();
                try {
                    for (Device device : mDeviceList) {
                        DeviceInfo devInfo = device.getInfo();
                        String deviceName = devInfo.getName();
                        //devInfo.close();
                        mDeviceNameList.add(deviceName);
                    }
                    mDeviceNameAdapter.clear();
                    mDeviceNameAdapter.addAll(mDeviceNameList);
                    mDeviceNameAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "updateDeviceSpinnerList run: " + e.getMessage());
                }
            }
        });
    }

    private void updatePropertySpinnerList() {
        runOnUiThread(() -> {
            mDevicePropertyMap.clear();
            mPropertyNameList.clear();
            try {
                List<DevicePropertyInfo> devicePropertyList = mSelectDevice.getSupportedPropertyList();
                for (DevicePropertyInfo deviceProperty : devicePropertyList) {
                    if (deviceProperty.getPropertyType() != PropertyType.STRUCT_PROPERTY
                            && deviceProperty.getPermissionType() != PermissionType.OB_PERMISSION_DENY) {
                        mPropertyNameList.add(deviceProperty.getPropertyName());
                        mDevicePropertyMap.put(deviceProperty.getPropertyName(), deviceProperty);
                    }
                }
                addNewMessage("Select device command");
                mPropertyAdapter.clear();
                mPropertyAdapter.addAll(mPropertyNameList);
                mPropertyAdapter.notifyDataSetChanged();

                mPropertySp.setSelection(0, true);
            } catch (Exception e) {
                Log.e(TAG, "updatePropertySpinnerList run: " + e.getMessage());
            }
        });
    }

    private void clearPropertySpinnerList() {
        mPropertyNameList.clear();
        mPropertyAdapter.clear();
        mPropertyAdapter.addAll(mPropertyNameList);
        mPropertyAdapter.notifyDataSetChanged();
    }

    //region View.OnClickListener
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_set) {
            setProperty();
        } else if (id == R.id.btn_get) {
            getProperty();
        }
    }
    //endregion

    /**
     * Set selected properties
     */
    private void setProperty() {
        try {
            if (null == mDeviceNameList || mDeviceNameList.isEmpty()) {
                showToast(getString(R.string.device_list_is_empty));
                return;
            }
            if (null == mPropertyNameList || mPropertyNameList.isEmpty()) {
                showToast(getString(R.string.command_list_is_empty));
                return;
            }

            if (TextUtils.isEmpty(mSetEt.getText())) {
                showToast(getString(R.string.please_input_right_value));
                return;
            }

            String setValue = mSetEt.getText().toString();
            // Set device instructions
            DevicePropertyInfo devProperty = mDevicePropertyMap.get(mPropertySp.getSelectedItem().toString());
            switch (devProperty.getPropertyType()) {
                case INT_PROPERTY:
                    //Exposure and gain need to be turned off to adjust AE, brightness needs to be turned on
                    // to adjust AE, white balance (color temperature) needs to be turned off automatic white
                    // balance to be adjusted.
                    try {
                        if (devProperty.getProperty() == DeviceProperty.OB_PROP_COLOR_EXPOSURE_INT
                                || devProperty.getProperty() == DeviceProperty.OB_PROP_COLOR_GAIN_INT) { //曝光或增益
                            // Get auto exposure status
                            boolean propertyExposureBool = propertyExposureBool = mSelectDevice.getPropertyValueB(DeviceProperty.OB_PROP_COLOR_AUTO_EXPOSURE_BOOL);
                            Log.d(TAG, "propertyExposureBool:" + propertyExposureBool);
                            if (propertyExposureBool) {
                                showToast(getString(R.string.cannot_set_exposure_and_gain_when_ae_on));
                                return;
                            }
                        }

                        if (devProperty.getProperty() == DeviceProperty.OB_PROP_COLOR_BRIGHTNESS_INT) { //亮度
                            // Get auto exposure status
                            boolean propertyExposureBool = mSelectDevice.getPropertyValueB(DeviceProperty.OB_PROP_COLOR_AUTO_EXPOSURE_BOOL);
                            Log.d(TAG, "propertyExposureBool:" + propertyExposureBool);
                            if (!propertyExposureBool) {
                                showToast(getString(R.string.cannot_set_brightness_when_ae_off));
                                return;
                            }
                        }

                        if (devProperty.getProperty() == DeviceProperty.OB_PROP_COLOR_WHITE_BALANCE_INT) { //白平衡
                            // Get auto white balance status
                            boolean propertyWhiteBool = mSelectDevice.getPropertyValueB(DeviceProperty.OB_PROP_COLOR_AUTO_WHITE_BALANCE_BOOL);
                            Log.d(TAG, "propertyWhiteBool:" + propertyWhiteBool);
                            if (propertyWhiteBool) {
                                showToast(getString(R.string.cannot_set_white_balance_when_auto_white_balance_on));
                                return;
                            }
                        }
                        mSelectDevice.setPropertyValueI(devProperty.getProperty(), Integer.parseInt(setValue));
                    } catch (Exception e) {
                        Log.e(TAG, "setProperty: " + e.getMessage());
                    }
                    break;
                case BOOL_PROPERTY:
                    // 0:false 1:true
                    mSelectDevice.setPropertyValueB(devProperty.getProperty(), ("1".equals(setValue)));
                    break;
                case FLOAT_PROPERTY:
                    mSelectDevice.setPropertyValueF(devProperty.getProperty(), Float.parseFloat(setValue));
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "setProperty: " + e.getMessage());
        }
    }

    /**
     * Get selected properties
     */
    private void getProperty() {
        try {
            if (null == mDeviceNameList || mDeviceNameList.size() <= 0) {
                showToast(getString(R.string.device_list_is_empty));
                return;
            }
            if (null == mPropertyNameList || mPropertyNameList.size() <= 0) {
                showToast(getString(R.string.command_list_is_empty));
                return;
            }
            // Get device instructions
            DevicePropertyInfo devProperty = mDevicePropertyMap.get(mPropertySp.getSelectedItem().toString());
            switch (devProperty.getPropertyType()) {
                case INT_PROPERTY:
                    int valueI = mSelectDevice.getPropertyValueI(devProperty.getProperty());
                    mGetTv.setText(String.valueOf(valueI));
                    break;
                case BOOL_PROPERTY:
                    boolean valueB = mSelectDevice.getPropertyValueB(devProperty.getProperty());
                    mGetTv.setText(String.valueOf(valueB));
                    break;
                case FLOAT_PROPERTY:
                    float valueF = mSelectDevice.getPropertyValueF(devProperty.getProperty());
                    mGetTv.setText(String.valueOf(valueF));
                    break;
            }
        } catch (Exception e) {
            addNewMessage("[error]:"+e.getMessage());
            Log.e(TAG, "getProperty: " + e.getMessage());
        }
    }

    /**
     * Get device information
     *
     * @param device Target device
     * @return Text of device information
     */
    private String getVersionInfo(Device device) {
        StringBuilder sb = new StringBuilder();
        try {
            DeviceInfo deviceInfo = device.getInfo();
            sb.append(getString(R.string.sensor_control_firmware_version)).
                    append(deviceInfo.getFirmwareVersion()).append("\n").
                    append(getString(R.string.sensor_control_hardware_version)).
                    append(deviceInfo.getHardwareVersion()).append("\n").
                    append(getString(R.string.sensor_control_sdk_version)).
                    append(OBContext.getVersionName()).append("\n").
                    append(getString(R.string.sensor_control_serial_number)).
                    append(deviceInfo.getSerialNumber()).append("\n").
                    append("---------------------------------------------------");
        } catch (Exception e) {
            Log.e(TAG, "getVersionInfo: " + e.getMessage());
        }
        return sb.toString();
    }

    private void showToast(String msg) {
        Log.d(TAG, "msg:" + msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update settings and get hints based on selected commands
     *
     * @param instructionTypeName Selected attribute name
     */
    private void updateSetEditTextHint(String instructionTypeName) {
        mSetEt.setText("");
        mGetTv.setText("");
        try {
            DevicePropertyInfo devicePropertyInfo = mDevicePropertyMap.get(instructionTypeName);
            PropertyType propertyType = devicePropertyInfo.getPropertyType();
            DeviceProperty property = devicePropertyInfo.getProperty();
            Log.i(TAG, "updateSetEditTextHint: instructionTypeName="+instructionTypeName);
            switch (propertyType) {
                case INT_PROPERTY:
                    int minI = mSelectDevice.getMinRangeI(property);
                    int maxI = mSelectDevice.getMaxRangeI(property);
                    mSetEt.setHint("[" + minI + "-" + maxI + "]");
                    break;
                case BOOL_PROPERTY:
                    mSetEt.setHint("[" + 0 + "-" + 1 + "]");
                    break;
                case FLOAT_PROPERTY:
                    float minF = mSelectDevice.getMinRangeF(property);
                    float maxF = mSelectDevice.getMaxRangeF(property);
                    mSetEt.setHint("[" + minF + "-" + maxF + "]");
                    break;
            }
        } catch (Exception e) {
            addNewMessage("[error]:"+e.getMessage());
            Log.e(TAG, "updateSetEditTextHint: " + e.getMessage());
        }
    }

    private void setPanelVisibleControl(boolean visible) {
        mSetBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        mSetEt.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void getPanelVisibleControl(boolean visible) {
        mGetBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        mGetTv.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateControlPanel(PermissionType permission) {
        switch (permission) {
            case OB_PERMISSION_READ:
                setPanelVisibleControl(false);
                getPanelVisibleControl(true);
                break;
            case OB_PERMISSION_WRITE:
                setPanelVisibleControl(true);
                getPanelVisibleControl(false);
                break;
            case OB_PERMISSION_READ_WRITE:
                setPanelVisibleControl(true);
                getPanelVisibleControl(true);
                break;
        }
    }

    //region AdapterView.OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.spi_device) {// Device switching, updating sensor list and attribute list
            mSelectDevice = mDeviceList.get(position);
            updatePropertySpinnerList();

            String deviceName = mDeviceSp.getSelectedItem().toString();
            addNewMessage(getString(R.string.sensor_control_select_device) + deviceName);
            addNewMessage(getVersionInfo(mDeviceList.get(position)));
        } else if (parentId == R.id.spi_instructions) {// command switching
            String instructionTypeName = mPropertySp.getSelectedItem().toString();
            PermissionType permissionType = mDevicePropertyMap.get(instructionTypeName).getPermissionType();
            String message = String.format("%s %s   %s %s",
                    getString(R.string.sensor_control_select_command), instructionTypeName,
                    getString(R.string.sensor_control_read_write_permission), permissionType);
            addNewMessage(message);
            updateControlPanel(permissionType);
            updateSetEditTextHint(instructionTypeName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //endregion
}