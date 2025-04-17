package com.orbbec.obsensor.types;

import android.text.TextUtils;

/**
 * \if English
 * Device information
 * \else
 * 设备信息
 * \endif
 */
public class DeviceInfo {
    private String name;
    private int vid;
    private int pid;
    private String uid;
    private String serialNumber;
    private String connectionType;
    private String ipAddress;
    private String firmwareVersion;
    private String hardwareVersion;
    private String supportedMinSdkVersion;
    private String asicName;
    private int deviceTypeValue;

    /**
     * \if English
     * Get device name
     *
     * @return device name
     * \else
     * 获取设备产品名
     * @return 设备产品名
     * \endif
     */
    public String getName() {
        return name;
    }

    /**
     * \if English
     * Get device identification number
     *
     * @return device identification number
     * \else
     * 获取设备产品标识号
     * @return 产品标识号
     * \endif
     */
    public int getPid() {
        return pid;
    }

    /**
     * \if English
     * Get the company identification number of the device
     *
     * @return company identification number
     * \else
     * 获取设备所属公司标识号
     * @return 公司标识号
     * \endif
     */
    public int getVid() {
        return vid;
    }

    /**
     * \if English
     * Get device uid
     *
     * @return Device Unique ID
     * \else
     * 获取设备唯一标识
     * @return 设备唯一标识
     * \endif
     */
    public String getUid() {
        return uid;
    }

    /**
     * \if English
     * Get device serial number
     *
     * @return devise serial number
     * \else
     * 获取设备序列号
     * @return 设备序列号
     * \endif
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * \if English
     * Get the connection type of device
     *
     * @return The device connection type
     * \else
     * 获取设备连接类型
     * @return 设备连接类型
     * \endif
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * \if English
     *
     * @return the IP address of the device, such as "192.168.1.10"
     * \else
     * @return 设备的IP地址，例如："192.168.1.10"
     * \endif
     * @brief Get the IP address of the device
     * @attention Only valid for network devices, otherwise it will return "0.0.0.0".
     * @brief 获取设备的IP地址
     * @attention 仅适用于网络设备，否则将返回“0.0.0.0”。
     */
    public String getIpAddress() {
        if (TextUtils.isEmpty(ipAddress)) {
            return "0.0.0.0";
        }
        return ipAddress;
    }

    /**
     * \if English
     * Get firmware version number
     *
     * @return Returns the firmware version number
     * \else
     * 获取固件版本号
     * @return 返回固件版本号
     * \endif
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * \if English
     * Get the version number of the hardware
     *
     * @return Returns the version number of the hardware
     * \else
     * 获取硬件的版本号
     * @return 返回硬件的版本号
     * \endif
     */
    public String getHardwareVersion() {
        return hardwareVersion;
    }

    /**
     * \if English
     * Get the minimum version number of the SDK supported by the device
     *
     * @return Returns the minimum SDK version number supported by the device
     * \else
     * 获取设备支持的SDK最小版本号
     * @return 返回设备支持的SDK最小版本号
     * \endif
     */
    public String getSupportedMinSdkVersion() {
        return supportedMinSdkVersion;
    }

    /**
     * \if English
     * Get chip type name
     *
     * @return Returns the chip type name
     * \else
     * 获取芯片类型名称
     * @return 返回芯片类型名称
     * \endif
     */
    public String getAsicName() {
        return asicName;
    }

    /**
     * \if English
     * Get device type
     *
     * @return Returns the device type {@link DeviceType}
     * \else
     * 获取设备类型
     * @return 返回设备类型 {@link DeviceType}
     * \endif
     */
    public DeviceType getDeviceType() {
        return DeviceType.get(deviceTypeValue);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", vid=" + String.format("0x%04x", vid) +
                ", pid=" + String.format("0x%04x", pid) +
                ", uid='" + uid + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", connectionType='" + connectionType + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", supportedMinSdkVersion='" + supportedMinSdkVersion + '\'' +
                ", asicName='" + asicName + '\'' +
                ", deviceType=" + getDeviceType() +
                '}';
    }
}
