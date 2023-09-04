package com.orbbec.obsensor;

/**
 * \if English
 * Callback interface for registering listening device status
 * \else
 * 用于注册监听设备状态的回调接口
 * \endif
 */
public interface DeviceChangedCallback {

    /**
	 *\if English
	 * Device addition notification and list
     *
     * @param deviceList Add device list {@link DeviceList}
	 * \else
     * 设备添加通知及列表
     *
     * @param deviceList 新增设备列表 {@link DeviceList}
	 * \endif
     */
    void onDeviceAttach(DeviceList deviceList);

    /**
	 * \if English
     * Device removal notices and lists
     *
     * @param deviceList Remove device list {@link DeviceList}
	 * \else
     * 设备移除通知及列表
     *
     * @param deviceList 移除设备列表 {@link DeviceList}
	 * \endif
     */
    void onDeviceDetach(DeviceList deviceList);
}
