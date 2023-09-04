package com.orbbec.orbbecsdkexamples.bean;

import com.orbbec.obsensor.Device;

public class DeviceBean {
    private String mDeviceName;
    private String mDeviceUid;
    private String mDeviceConnectionType;
    private Device mDevice;
    public boolean isDepthRunning;
    public boolean isColorRunning;
    public boolean isIrRunning;

    public DeviceBean(String name, String uid, String connectionType, Device device) {
        mDeviceName = name;
        mDeviceUid = uid;
        mDeviceConnectionType = connectionType;
        mDevice = device;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceUid() {
        return mDeviceUid;
    }

    public String getDeviceConnectionType() {
        return mDeviceConnectionType;
    }

    public Device getDevice() {
        return mDevice;
    }
}
