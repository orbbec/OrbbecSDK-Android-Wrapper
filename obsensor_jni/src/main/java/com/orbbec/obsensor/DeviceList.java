package com.orbbec.obsensor;

import com.orbbec.internal.BetaDevice;

/**
 * \if English
 * Device list
 * \else
 * 设备列表
 * \endif
 */
public class DeviceList extends LobClass {

    DeviceList(long handle) {
        mHandle = handle;
    }

    /**
	 * \if English
	 * Get the number of devices
     *
     * @return the number of devices
	 * \else
     * 获取设备数量
     *
     * @return 设备数量
	 * \endif
     */
    public int getDeviceCount() {
        throwInitializeException();
        return nGetDeviceCount(mHandle);
    }

    /**
	 * \if English
	 * Get the device serial number according to the specified index in the device list
     *
     * @param index index value
     * @return devise serial number
	 * \else
     * 根据设备列表中指定索引，获取设备产品名
     *
     * @param index 索引值
     * @return 产品名
	 * \endif
     */
    @Deprecated
    public String getName(int index) {
        throwInitializeException();
        return nGetName(mHandle, index);
    }

    /**
	 * \if English
	 * Obtain the product identification number of the device according to the specified index in the device list
     *
     * @param index index value
     * @return product identification number
	 * \else
     * 根据设备列表中指定索引，获取设备产品标识号
     *
     * @param index 索引值
     * @return 产品标识号
	 * \endif
     */
    public int getPid(int index) {
        throwInitializeException();
        return nGetPid(mHandle, index);
    }

    /**
	 * \if English
	 * Obtain the identification number of the company which the device belongs according to the specified index in the device list
     *
     * @param index index value
     * @return company identification number
	 * \else
     * 根据设备列表中指定索引，获取设备所属公司标识号
     *
     * @param index 索引值
     * @return 公司标识号
	 * \endif
     */
    public int getVid(int index) {
        throwInitializeException();
        return nGetVid(mHandle, index);
    }

    /**
	 * \if English
	 * Obtain the unique ID of the device according to the specified index in the device list
     *
     * @param index index value
     * @return Device Unique ID
	 * \else
     * 根据设备列表中指定索引，获取设备唯一标识
     *
     * @param index 索引值
     * @return 设备唯一标识
	 * \endif
     */
    public String getUid(int index) {
        throwInitializeException();
        return nGetUid(mHandle, index);
    }

    /**
	 * \if English
	 * Get the device serial number according to the specified index in the device list
     *
     * @param index index value
     * @return devise serial number
	 * \else
     * 根据设备列表中指定索引，获取设备序列号
     *
     * @param index 索引值
     * @return 设备序列号
	 * \endif
     */
    public String getDeviceSerialNumber(int index) {
        throwInitializeException();
        return nGetDeviceSerialNumber(mHandle, index);
    }

    /**
     * \if English
     * @brief Get device connection type
     *
     * @param index device index
     * @return const char* returns connection type，currently supports："USB", "USB1.0", "USB1.1", "USB2.0", "USB2.1", "USB3.0", "USB3.1", "USB3.2", "Ethernet"
     * \else
     * @brief 获取设备连接类型
     *
     * @param index 设备索引
     * @return const char* 返回连接类型，当前支持的类型有："USB", "USB1.0", "USB1.1", "USB2.0", "USB2.1", "USB3.0", "USB3.1", "USB3.2", "Ethernet"
     * \endif
     */
    public String getConnectionType(int index) {
        throwInitializeException();
        return nGetConnectionType(mHandle, index);
    }

    /**
     * \if English
     * @brief get the ip address of the device at the specified index
     *
     * @attention Only valid for network devices, otherwise it will return "0.0.0.0".
     *
     * @param index the index of the device
     * @return the ip address of the device
     * \else
     * @brief 获取指定索引处设备的 IP 地址
     *
     * @attention 仅适用于网络设备，否则将返回 "0.0.0.0"
     *
     * @param index 设备索引
     * @return 设备的 IP 地址，例如：“192.168.1.10”
     * \endif
     */
    public String getIpAddress(int index) {
        throwInitializeException();
        return nGetIpAddress(mHandle, index);
    }

    /**
     * \if English
     * @brief Get the device extension information.
     *
     * @param[in] info Device Information
     * @param[in] index Device index
     * @param[out] error Log error messages
     * @return const char* The device extension information
     * \else
     * @brief 获取SDK支持的设备的拓展信息
     * @param index 设备索引
     * @return 设备扩展信息
     * \endif
     */
    public String getExtensionInfo(int index) {
        throwInitializeException();
        return nGetExtensionInfo(mHandle, index);
    }

    /**
	 * \if English
	 * Get the device object according to the specified index in the device list
     *
     * @param index index value
     * @return designated device
	 * \else
     * 根据设备列表中指定索引，获取设备对象
     *
     * @param index 索引值
     * @return 指定设备
	 * \endif
     */
    public Device getDevice(int index) {
        throwInitializeException();
        long handle = nGetDevice(mHandle, index);
        if (0 == handle) {
            return null;
        }
        return new BetaDevice(handle);
    }

    /**
	 * \if English
     * Create a device, if the device has been acquired and created elsewhere, repeated acquisition will return an error
     *
     * @param serialNum	The serial number of the device to be created
     * @return Returns the created device {@link Device}
	 * \else
     * 创建设备,如果设备有在其他地方被获取创建,重复获取将会返回错误
     *
     * @param serialNum 要创建设备的序列号
     * @return 返回创建的设备 {@link Device}
	 * \endif
     */
    public Device getDeviceBySerialNumber(String serialNum) {
        throwInitializeException();
        long handle = nGetDeviceBySerialNumber(mHandle, serialNum);
        if (0 == handle) {
            return null;
        }
        return new BetaDevice(handle);
    }

    /**
	 * \if English
	 * Create a device, if the device has been acquired and created elsewhere, repeated acquisition will return an error
     *
     * @param uid The uid of the device to create
     * @return Returns the created device {@link Device}
	 * \else
     * 创建设备,如果设备有在其他地方被获取创建，重复获取将会返回错误
     *
     * @param uid 要创建设备的uid
     * @return 返回创建的设备 {@link Device}
	 * \endif
     */
    public Device getDeviceByUid(String uid) {
        throwInitializeException();
        long handle = nGetDeviceByUid(mHandle, uid);
        if (0 == handle) {
            return null;
        }
        return new BetaDevice(handle);
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

    private static native int nGetDeviceCount(long handle);

    private static native void nDelete(long handle);

    private static native String nGetName(long handle, int index);

    private static native int nGetPid(long handle, int index);

    private static native int nGetVid(long handle, int index);

    private static native String nGetUid(long handle, int index);

    private static native String nGetDeviceSerialNumber(long handle, int index);

    private static native String nGetConnectionType(long handle, int index);

    private static native String nGetIpAddress(long handle, int index);

    private static native String nGetExtensionInfo(long handle, int index);

    private static native long nGetDevice(long handle, int index);

    private static native long nGetDeviceBySerialNumber(long handle, String serialNum);

    private static native long nGetDeviceByUid(long handle, String uid);
}
