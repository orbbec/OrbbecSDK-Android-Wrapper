package com.orbbec.orbbecsdkexamples.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
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
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.DeviceProperty;
import com.orbbec.obsensor.DevicePropertyInfo;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.PermissionType;
import com.orbbec.obsensor.PropertyType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.adapter.InformationAdapter;
import com.orbbec.orbbecsdkexamples.adapter.SpinnerContentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorControlActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "SensorControlActivity";
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
    private OBContext mOBContext;
    private Device mSelectDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("SensorControl");
        setContentView(R.layout.activity_sensor_control);
        // 1. Initialize views
        initViews();

        // 2.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                try {
                    // 3.Add the obtained device to the device list
                    mDeviceList.add(deviceList.getDevice(0));

                    // 4.Update device list
                    updateDeviceSpinnerList();
                } catch (Exception e) {
                    e.printStackTrace();
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
                    if (mDeviceList.size() <= 0) {
                        clearPropertySpinnerList();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    deviceList.close();
                }
            }
        });
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
                        devInfo.close();
                        mDeviceNameList.add(deviceName);
                    }
                    mDeviceNameAdapter.clear();
                    mDeviceNameAdapter.addAll(mDeviceNameList);
                    mDeviceNameAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updatePropertySpinnerList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearPropertySpinnerList() {
        mPropertyNameList.clear();
        mPropertyAdapter.clear();
        mPropertyAdapter.addAll(mPropertyNameList);
        mPropertyAdapter.notifyDataSetChanged();
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

            // OBContext资源释放
            if (null != mOBContext) {
                mOBContext.close();
                mOBContext = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        release();
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
            if (null == mDeviceNameList || mDeviceNameList.size() <= 0) {
                showToast(getString(R.string.device_list_is_empty));
                return;
            }
            if (null == mPropertyNameList || mPropertyNameList.size() <= 0) {
                showToast(getString(R.string.command_list_is_empty));
                return;
            }

            if (null == mSetEt.getText() || "".equals(mSetEt.getText()) || mSetEt.getText().toString().isEmpty()) {
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
                    if (devProperty.getProperty() == DeviceProperty.OB_PROP_COLOR_EXPOSURE_INT
                            || devProperty.getProperty() == DeviceProperty.OB_PROP_COLOR_GAIN_INT) { //曝光或增益
                        // Get auto exposure status
                        boolean propertyExposureBool = false;
                        try {
                            propertyExposureBool = mSelectDevice.getPropertyValueB(DeviceProperty.OB_PROP_COLOR_AUTO_EXPOSURE_BOOL);
                        } catch (Exception e) {

                        }
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
                    break;
                case BOOL_PROPERTY:
                    // 0:false 1:true
                    mSelectDevice.setPropertyValueB(devProperty.getProperty(), ("1".equals(setValue) ? true : false));
                    break;
                case FLOAT_PROPERTY:
                    mSelectDevice.setPropertyValueF(devProperty.getProperty(), Float.parseFloat(setValue));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    mGetTv.setText(Integer.toString(valueI));
                    break;
                case BOOL_PROPERTY:
                    boolean valueB = mSelectDevice.getPropertyValueB(devProperty.getProperty());
                    mGetTv.setText(Boolean.toString(valueB));
                    break;
                case FLOAT_PROPERTY:
                    float valueF = mSelectDevice.getPropertyValueF(devProperty.getProperty());
                    mGetTv.setText(Float.toString(valueF));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            addNewMessage("[error]:"+e.getMessage());
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
        try (DeviceInfo deviceInfo = device.getInfo()) {
            sb.append(getString(R.string.sensor_control_firmware_version) + deviceInfo.getFirmwareVersion() + "\n"
                    + getString(R.string.sensor_control_hardware_version) + deviceInfo.getHardwareVersion() + "\n"
                    + getString(R.string.sensor_control_sdk_version) + OBContext.getVersionName() + "\n"
                    + getString(R.string.sensor_control_serial_number) + deviceInfo.getSerialNumber()
            );
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            addNewMessage("[error]:"+e.getMessage());
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
            addNewMessage(getString(R.string.sensor_control_select_command) + instructionTypeName
                    + getString(R.string.sensor_control_read_write_permission) + permissionType);
            updateControlPanel(permissionType);
            updateSetEditTextHint(instructionTypeName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //endregion
}