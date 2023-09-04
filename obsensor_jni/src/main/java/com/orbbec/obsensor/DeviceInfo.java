package com.orbbec.obsensor;

/**
 * \if English
 * Device information
 * \else
 * 设备信息
 * \endif
 */
public class DeviceInfo extends LobClass {

    DeviceInfo(long handle) {
        mHandle = handle;
    }

    /**
	 * \if English
	 * Get device name
     *
     * @return device name
	 * \else
     * 获取设备产品名
     *
     * @return 设备产品名
	 * \endif
     */
    public String getName() {
        throwInitializeException();
        return nGetName(mHandle);
    }

    /**
	 * \if English
	 * Get device identification number
     *
     * @return device identification number
	 * \else
     * 获取设备产品标识号
     *
     * @return 产品标识号
	 * \endif
     */
    public int getPid() {
        throwInitializeException();
        return nGetPid(mHandle);
    }

    /**
	 * \if English
	 * Get the company identification number of the device
     *
     * @return company identification number
	 * \else
     * 获取设备所属公司标识号
     *
     * @return 公司标识号
	 * \endif
     */
    public int getVid() {
        throwInitializeException();
        return nGetVid(mHandle);
    }

    /**
	 * \if English
	 * Get device uid
     *
     * @return Device Unique ID
	 * \else
     * 获取设备唯一标识
     *
     * @return 设备唯一标识
	 * \endif
     */
    public String getUid() {
        throwInitializeException();
        return nGetUid(mHandle);
    }

    /**
	 * \if English
	 * Get device serial number
     *
     * @return devise serial number
	 * \else
     * 获取设备序列号
     *
     * @return 设备序列号
	 * \endif
     */
    public String getSerialNumber() {
        throwInitializeException();
        return nGetSerialNumber(mHandle);
    }

    /**
	 * \if English
	 * Get the USB type the device is connected to
     *
     * @return The type of USB the device is connected to
	 * \else
     * 获取设备连接的USB类型
     * 废弃说明：当前支持的类型不止usb，还能够支持network；最新请用 {@link #getConnectionType()} 更直观。
     *
     * @return 设备连接的USB类型
	 * \endif
     */
    @Deprecated
    public String getUsbType() {
        throwInitializeException();
        return nGetUsbType(mHandle);
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
        throwInitializeException();
        return nGetConnectionType(mHandle);
    }

    /**
	 * \if English
	 * Get firmware version number
     *
     * @return Returns the firmware version number
	 * \else
     * 获取固件版本号
     *
     * @return 返回固件版本号
	 * \endif
     */
    public String getFirmwareVersion() {
        throwInitializeException();
        return nGetFirmwareVersion(mHandle);
    }

    /**
	 * \if English
	 * Get the version number of the hardware
     *
     * @return Returns the version number of the hardware
	 * \else
     * 获取硬件的版本号
     *
     * @return 返回硬件的版本号
	 * \endif
     */
    public String getHardwareVersion() {
        throwInitializeException();
        return nGetHardwareVersion(mHandle);
    }

    /**
	 * \if English
	 * Get the minimum version number of the SDK supported by the device
     *
     * @return Returns the minimum SDK version number supported by the device
	 * \else
     * 获取设备支持的SDK最小版本号
     *
     * @return 返回设备支持的SDK最小版本号
	 * \endif
     */
    public String getSupportedMinSdkVersion() {
        throwInitializeException();
        return nGetSupportedMinSdkVersion(mHandle);
    }

    /**
	 * \if English
	 * Get chip type name
     *
     * @return Returns the chip type name
	 * \else
     * 获取芯片类型名称
     *
     * @return 返回芯片类型名称
	 * \endif
     */
    public String getAsicName() {
        throwInitializeException();
        return nGetAsicName(mHandle);
    }

    /**
	 * \if English
	 * Get device type
     *
     * @return Returns the device type {@link DeviceType}
	 * \else
     * 获取设备类型
     *
     * @return 返回设备类型 {@link DeviceType}
	 * \endif
     */
    public DeviceType getDeviceType() {
        throwInitializeException();
        return DeviceType.get(nGetDeviceType(mHandle));
    }

    /**
     * \if English
     * resource release
     * \else
     * 资源释放
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native void nDelete(long handle);

    private static native String nGetName(long handle);

    private static native int nGetPid(long handle);

    private static native int nGetVid(long handle);

    private static native String nGetUid(long handle);

    private static native String nGetSerialNumber(long handle);

    private static native String nGetUsbType(long handle);

    private static native String nGetConnectionType(long handle);

    private static native String nGetFirmwareVersion(long handle);

    private static native String nGetHardwareVersion(long handle);

    private static native String nGetSupportedMinSdkVersion(long handle);

    private static native String nGetAsicName(long handle);

    private static native int nGetDeviceType(long handle);
}
