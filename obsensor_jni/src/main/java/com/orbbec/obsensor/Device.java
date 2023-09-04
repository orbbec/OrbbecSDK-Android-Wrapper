package com.orbbec.obsensor;

import android.util.Log;
import android.util.SparseArray;

import com.orbbec.internal.OBLocalUtils;
import com.orbbec.obsensor.datatype.DeviceTemperature;

import java.util.ArrayList;
import java.util.List;

/**
 * \if English
 * Expose orbbec device functions
 * \else
 * 公开orbbec设备功能 
 * \endif
 */
public class Device extends LobClass {
    private static final String TAG = "Device";

    private List<Sensor> mSensors = new ArrayList<>();

    private SparseArray<PropertyRangeB> mPropertyRangeMapB = new SparseArray<>();
    private SparseArray<PropertyRangeI> mPropertyRangeMapI = new SparseArray<>();
    private SparseArray<PropertyRangeF> mPropertyRangeMapF = new SparseArray<>();

    protected static class PropertyRangeB {
        public boolean min;
        public boolean max;
        public boolean step;
        public boolean def;
    }

    protected static class PropertyRangeI {
        public int min;
        public int max;
        public int step;
        public int def;
    }

    protected static class PropertyRangeF {
        public float min;
        public float max;
        public float step;
        public float def;
    }

    protected synchronized PropertyRangeB getRangeB(int propertyId) {
        throwInitializeException();
        if (mPropertyRangeMapB.indexOfKey(propertyId) < 0) {
            PropertyRangeB propertyRange = new PropertyRangeB();
            nGetPropertyRangeB(mHandle, propertyId, propertyRange);
            mPropertyRangeMapB.put(propertyId, propertyRange);
        }
        return mPropertyRangeMapB.get(propertyId);
    }

    protected synchronized PropertyRangeI getRangeI(int propertyId) {
        throwInitializeException();
        if (mPropertyRangeMapI.indexOfKey(propertyId) < 0) {
            PropertyRangeI propertyRange = new PropertyRangeI();
            nGetPropertyRangeI(mHandle, propertyId, propertyRange);
            mPropertyRangeMapI.put(propertyId, propertyRange);
        }
        return mPropertyRangeMapI.get(propertyId);
    }

    protected synchronized PropertyRangeF getRangeF(int propertyId) {
        throwInitializeException();
        if (mPropertyRangeMapF.indexOfKey(propertyId) < 0) {
            PropertyRangeF propertyRange = new PropertyRangeF();
            nGetPropertyRangeF(mHandle, propertyId, propertyRange);
            mPropertyRangeMapF.put(propertyId, propertyRange);
        }
        return mPropertyRangeMapF.get(propertyId);
    }

    /**
	 * \if English
	 * Initialize the device object by specifying the device handle
     *
     * @param handle handle of the initialized device
	 * \else
     * 通过指定Device句柄初始化Device对象
     *
     * @param handle 初始化的设备的句柄
	 * \endif
     */
    public Device(long handle) {
        mHandle = handle;
        long[] sensorHandles = nQuerySensor(mHandle);
        for (long h : sensorHandles) {
            mSensors.add(new Sensor(h));
        }
    }

    /**
	 * \if English
	 * Query the list of sensors contained in the device
     *
     * @return Sensor list
	 * \else
     * 查询设备包含的传感器列表
     *
     * @return 传感器列表
	 * \endif
     */
    public List<Sensor> querySensors() {
        return mSensors;
    }

    /**
	 * \if English
	 * Get the list of property information supported by the device
     *
     * @return List of property information supported by the device {@link List<DevicePropertyInfo>}
	 * \else
     * 获取设备支持的属性信息列表
     *
     * @return 设备支持的属性信息列表 {@link List<DevicePropertyInfo>}
	 * \endif
     */
    public List<DevicePropertyInfo> getSupportedPropertyList() {
        throwInitializeException();
        int innerSupportedCount = nGetSupportedPropertyCount(mHandle);
        List<DevicePropertyInfo> list = new ArrayList<>();
        for (int i = 0; i < innerSupportedCount; i++) {
            DevicePropertyInfo cell = nGetSupportedProperty(mHandle, i);
            if (cell.getProperty() != null) {
                list.add(cell);
            }
        }
        return list;
    }

    /**
     * \if English
     * Get list of DevicePrecisionLevel
     *
     * @return list of DevicePrecisionLevel
     *
     * \else
     * 查询设备支持的相机深度精度列表
     * @return 设备支持的相机深度精度列表
     * \endif
     */
    public List<DepthPrecisionLevel> getSupportedDepthPrecisionLevelList() {
        throwInitializeException();
        List<DepthPrecisionLevel> listPrecisionLevelList = new ArrayList<>();
        int levelArray[] = nGetSupportDepthPrecisionLevelList(mHandle);
        if (null != levelArray) {
            Log.d(TAG, "getSupportedDepthPrecisionLevelList() level array size: " + levelArray.length);
            for (int value : levelArray) {
                Log.d(TAG, "getSupportedDepthPrecisionLevelList levelArray: " + value);
                if (value < DepthPrecisionLevel.OB_PRECISION_COUNT.value()) {
                    listPrecisionLevelList.add(DepthPrecisionLevel.get(value));
                } else {
                    Log.e(TAG, "nGetSupportDepthPrecisionLevelList unsupported value: " + value);
                }
            }
        }
        return listPrecisionLevelList;
    }

    /**
	 * \if English
	 * Query whether the device supports setting the specified device property
     *
     * @param property       Device property {@link DeviceProperty}
     * @param permissionType Required permission type {@link PermissionType}
     * @return true supports, false does not support
	 * \else
     * 查询设备是否支持设置指定的设备属性
     *
     * @param property       设备属性 {@link DeviceProperty}
     * @param permissionType 需要支持的权限类型 {@link PermissionType}
     * @return true 支持, false 不支持
	 * \endif
     */
    public boolean isPropertySupported(DeviceProperty property, PermissionType permissionType) {
        throwInitializeException();
        return nIsPropertySupported(mHandle, property.value(), permissionType.value());
    }

    /**
	 * \if English
	 * Set the value of the boolean type device property
     *
     * @param property device property {@link DeviceProperty}
     * @param value    	Set the value of the device property, boolean type
	 * \else
     * 设置boolean类型设备属性的值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @param value    设置设备属性的值，boolean类型
	 * \endif
     */
    public void setPropertyValueB(DeviceProperty property, boolean value) {
        throwInitializeException();
        nSetPropertyValueB(mHandle, property.value(), value);
    }

    /**
	 * \if English
	 * Set the value of the int type device property
     *
     * @param property device property {@link DeviceProperty}
     * @param value   Set the value of the device property, int type
	 * \else
     * 设置int类型设备属性的值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @param value    设置设备属性的值，int类型
	 * \endif
     */
    public void setPropertyValueI(DeviceProperty property, int value) {
        throwInitializeException();
        nSetPropertyValueI(mHandle, property.value(), value);
    }

    /**
	 * \if English
	 * Set the value of the float type device property
     *
     * @param property device property {@link DeviceProperty}
     * @param value    set the value of the device property, float type
	 * \else
     * 设置float类型设备属性的值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @param value    设置设备属性的值，float类型
	 * \endif
     */
    public void setPropertyValueF(DeviceProperty property, float value) {
        throwInitializeException();
        nSetPropertyValueF(mHandle, property.value(), value);
    }

    /**
	 * \if English
	 * Set the data structure of the corresponding device properties
     *
     * @param property device property {@link DeviceProperty}
     * @param dataType device property data structure {@link DataType}
     * @throws OBException When there is an error in the encapsulation of the data structure to be set
	 * \else
     * 设置对应的设备属性的数据结构
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @param dataType 设备属性的数据结构 {@link DataType}
     * @throws OBException 当待设置的数据结构封装出错时
	 * \endif
     */
    public void setPropertyValueDataType(DeviceProperty property, DataType dataType) {
        throwInitializeException();
        boolean result = dataType.wrapBytes();
        if (result) {
            nSetPropertyValueDataType(mHandle, property.value(), dataType.getBytes());
        } else {
            throw new OBException(property + " wrap bytes error!");
        }
    }

    /**
	 * \if English
	 * Get the value of the boolean type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The value of the device property, boolean type
	 * \else
     * 获取boolean类型设备属性的值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性的值，boolean类型
	 * \endif
     */
    public boolean getPropertyValueB(DeviceProperty property) {
        throwInitializeException();
        return nGetPropertyValueB(mHandle, property.value());
    }

    /**
	 * \if English
	 * Get the value of the int type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The value of the device property, int type
	 * \else
     * 获取int类型设备属性的值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性的值，int类型
	 * \endif
     */
    public int getPropertyValueI(DeviceProperty property) {
        throwInitializeException();
        return nGetPropertyValueI(mHandle, property.value());
    }

    /**
	 * \if English
	 * Get the value of the float type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The value of the device property, float type
	 * \else
     * 获取float类型设备属性的值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性的值，float类型
	 * \endif
     */
    public float getPropertyValueF(DeviceProperty property) {
        throwInitializeException();
        return nGetPropertyValueF(mHandle, property.value());
    }

    /**
	 * \if English
	 * Get the data structure of the corresponding device properties
     *
     * @param property device property {@link DeviceProperty}
     * @param dataType device property data structure {@link DataType}
     * @throws OBException When there is an error in parsing the obtained data structure
	 * \else
     * 获取对应的设备属性的数据结构
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @param dataType 设备属性的数据结构 {@link DataType}
     * @throws OBException 当获取到的数据结构解析出错时
	 * \endif
     */
    public void getPropertyValueDataType(DeviceProperty property, DataType dataType) {
        throwInitializeException();
        nGetPropertyValueDataType(mHandle, property.value(), dataType.getBytes());
        boolean result = dataType.parseBytes();
        if (!result) {
            throw new OBException(property + " parse bytes error!");
        }
    }

    /**
	 * \if English
	 * Get the minimum value supported by the int type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The minimum value supported by the device property, int type
	 * \else
     * 获取int类型设备属性支持的最小值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的最小值，int类型
	 * \endif
     */
    public int getMinRangeI(DeviceProperty property) {
        return getRangeI(property.value()).min;
    }

    /**
	 * \if English
	 * Get the minimum value supported by the float type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The minimum value supported by the device property, float type
	 * \else
     * 获取float类型设备属性支持的最小值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的最小值，float类型
	 * \endif
     */
    public float getMinRangeF(DeviceProperty property) {
        return getRangeF(property.value()).min;
    }

    /**
	 * \if English
	 * Get the maximum value supported by the int type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The maximum value supported by the device property, int type
	 * \else
     * 获取int类型设备属性支持的最大值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的最大值，int类型
	 * \endif
     */
    public int getMaxRangeI(DeviceProperty property) {
        return getRangeI(property.value()).max;
    }

    /**
	 * \if English
	 * Get the maximum value supported by the float type device property
     *
     * @param property device {@link DeviceProperty}
     * @return The maximum value supported by the device property, float type
	 * \else
     * 获取float类型设备属性支持的最大值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的最大值，float类型
	 * \endif
     */
    public float getMaxRangeF(DeviceProperty property) {
        return getRangeF(property.value()).max;
    }

    /**
	 * \if English
	 * Get the step value at which the int type device property changes within the supported range
     *
     * @param property device property {@link DeviceProperty}
     * @return The step value that changes within the range supported by the device property, int type
	 * \else
     * 获取int类型设备属性在支持的范围内变化的步值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的范围内变化的步值，int类型
	 * \endif
     */
    public int getStepI(DeviceProperty property) {
        return getRangeI(property.value()).step;
    }

    /**
	 * \if English
	 * Get the step value at which the float type device property changes within the supported range
     *
     * @param property device property {@link DeviceProperty}
     * @return The step value that changes within the range supported by the device property, float type
	 * \else
     * 获取float类型设备属性在支持的范围内变化的步值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的范围内变化的步值，float类型
	 * \endif
     */
    public float getStepF(DeviceProperty property) {
        return getRangeF(property.value()).step;
    }

    /**
	 * \if English
	 * Get the default value of the boolean type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The default value supported by the device property, boolean type
	 * \else
     * 获取boolean类型设备属性的默认值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的默认值，boolean类型
	 * \endif
     */
    public boolean getDefaultB(DeviceProperty property) {
        return getRangeB(property.value()).def;
    }

    /**
	 * \if English
	 * Get the default value of the int type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The default value supported by the device property, int type
	 * \else
     * 获取int类型设备属性的默认值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的默认值，int类型
	 * \endif
     */
    public int getDefaultI(DeviceProperty property) {
        return getRangeI(property.value()).def;
    }

    /**
	 * \if English
	 * Get the default value of the float type device property
     *
     * @param property device property {@link DeviceProperty}
     * @return The default value supported by the device property, float type
	 * \else
     * 获取float类型设备属性的默认值
     *
     * @param property 设备属性 {@link DeviceProperty}
     * @return 设备属性支持的默认值，float类型
	 * \endif
     */
    public float getDefaultF(DeviceProperty property) {
        return getRangeF(property.value()).def;
    }

    /**
	 * \if English
	 * Get device information
     *
     * @return device information {@link DeviceInfo}
	 * \else
     * 获取设备信息
     *
     * @return 设备信息 {@link DeviceInfo}
	 * \endif
     */
    public DeviceInfo getInfo() {
        throwInitializeException();
        return new DeviceInfo(nGetDeviceInfo(mHandle));
    }

    /**
	 * \if English
	 * Get the sensor of the specified type
     *
     * @param type 	sensor type {@link SensorType}
     * @return sensor {@link Sensor}
	 * \else
     * 获取指定类型的传感器
     *
     * @param type 传感器类型 {@link SensorType}
     * @return 传感器 {@link Sensor}
	 * \endif
     */
    public Sensor getSensor(SensorType type) {
        for (Sensor sensor : mSensors) {
            if (sensor.getType() == type) {
                return sensor;
            }
        }
        return null;
    }

    /**
	 * \if English
	 * Upgrade device firmware
     *
     * @param fileName firmware path
     * @param callback callback during the upgrade process {@link UpgradeCallback}
     * @throws OBException 	The firmware path is abnormal
	 * \else
     * 升级设备固件
     *
     * @param fileName 固件路径
     * @param callback 升级过程回调 {@link UpgradeCallback}
     * @throws OBException 固件路径异常
	 * \endif
     */
    public void upgrade(String fileName, UpgradeCallback callback) {
        throwInitializeException();
        OBLocalUtils.checkFileAndThrow(fileName, "Upgrade failed");
        nUpgrade(mHandle, fileName, callback);
    }

    /**
	 * \if English
	 * Transfer files to the specified path
     *
     * @param filePath source file path
     * @param dstPath  	Target path
     * @param callback File transfer callback{@link FileSendCallback}
	 * \else
     * 传输文件到指定路径
     *
     * @param filePath 源文件路径
     * @param dstPath  目标路径
     * @param callback 文件传输回调 {@link FileSendCallback}
     * @throws OBException 源文件路径异常
	 * \endif
     */
    public void sendFileToDestination(String filePath, String dstPath, FileSendCallback callback) {
        throwInitializeException();
        OBLocalUtils.checkFileAndThrow(filePath, "Send file failed");
        nSendFileToDestination(mHandle, filePath, dstPath, callback);
    }

    /**
	 * \if English
	 * Get device temperature information
     *
     * @return temperature information {@link DeviceTemperature}
	 * \else
     * 获取设备温度信息
     *
     * @return 温度信息 {@link DeviceTemperature}
	 * \endif
     */
    public DeviceTemperature getDeviceTemperature() {
        DeviceTemperature temperature = new DeviceTemperature();
        if (isPropertySupported(DeviceProperty.OB_STRUCT_DEVICE_TEMPERATURE, PermissionType.OB_PERMISSION_READ)) {
            getPropertyValueDataType(DeviceProperty.OB_STRUCT_DEVICE_TEMPERATURE, temperature);
        } else {
            throw new OBException("Getting device temperature is unsupported!");
        }
        return temperature;
    }

    /**
	 * \if English
	 * Activate the device with an authorization code
     *
     * @param authCode Authorization code
     * @return Whether the activation is successful, the activation succeeds: true, the activation fails: false
	 * \else
     * 使用授权码激活设备
     *
     * @param authCode 授权码
     * @return 激活是否成功，激活成功：true，激活失败：false
	 * \endif
     */
    public boolean activateAuthorization(String authCode) {
        throwInitializeException();
        return nActivateAuthorization(mHandle, authCode);
    }

    /**
	 * \if English
	 * Set device status monitoring
     *
     * @param listener callback when device status changes {@link OnStateChangeListener}
	 * \else
     * 设置设备状态监听
     *
     * @param listener 设备状态发生改变时的回调 {@link OnStateChangeListener}
	 * \endif
     */
    public void setStateChangeListener(OnStateChangeListener listener) {
        throwInitializeException();
        nSetStateChangeListener(mHandle, new OnStateChangeListenerImpl() {
            @Override
            public void onStateChange(int stateType, String msg) {
                listener.onStateChange(DeviceStateType.values()[stateType], msg);
            }
        });
    }


    /**
	 * \if English
	 * Synchronize the device time (time to the device, synchronize the local system time to the device)
     *
     * @return command round trip time delay（round trip time， rtt）
	 * \else
     * 同步设备时间（向设备授时，同步本地系统时间到设备）
     *
     * @return 命令往返时间延时（round trip time， rtt）
	 * \endif
     */
    public long syncDeviceTime() {
        throwInitializeException();
        return nSyncDeviceTime(mHandle);
    }

    /**
	 * \if English
	 * Get a list of calibrated camera parameters
     *
     * @return Camera parameter list {@link CameraParamList}
	 * \else
     * 获取标定的相机参数列表
     *
     * @return 相机参数列表 {@link CameraParamList}
	 * \endif
     */
    public CameraParamList getCalibrationCameraParamList() {
        throwInitializeException();
        long handle = nGetCalibrationCameraParamList(mHandle);
        if (0 == handle) {
            return null;
        }
        return new CameraParamList(handle);
    }

    /**
     * \if English
     * @brief Get current depth work mode
     *
     * @return ob_depth_work_mode Current depth work mode
     * \else
     * @brief 查询当前的相机深度模式
     *
     * @return 返回当前的相机深度模式
     * \endif
     */
    public DepthWorkMode getCurrentDepthWorkMode() {
        throwInitializeException();
        return nGetCurrentDepthWorkMode(mHandle);
    }

    /**
     * \if English
     * @brief Switch depth work mode by work mode name.
     *
     * @param modeName Depth work mode name which equals to DepthWorkMode.name
     * \else
     * @brief 切换相机深度模式（根据深度工作模式名称）
     *
     * @param modeName 相机深度工作模式的名称，模式名称必须与DepthWorkMode.name一致
     *
     * @return ob_status 设置设置结果，OB_STATUS_OK成功，其他：设置失败
     * \endif
     */
    public void switchDepthWorkMode(String modeName) {
        throwInitializeException();
        nSwitchDepthWorkMode(mHandle, modeName);

        // 重置sensor
        for (Sensor sensor : mSensors) {
            sensor.mOwner = true;
            sensor.close();
        }
        mSensors.clear();

        long[] sensorHandles = nQuerySensor(mHandle);
        for (long h : sensorHandles) {
            mSensors.add(new Sensor(h));
        }

        // 清空Property缓存
        mPropertyRangeMapB.clear();
        mPropertyRangeMapI.clear();
        mPropertyRangeMapF.clear();
    }

    /**
     * \if English
     * @brief Request support depth work mode list
     * @return OBDepthWorkModeList list of ob_depth_work_mode
     * \else
     * @brief 查询相机深度模式列表
     *
     * @return 相机深度模式列表
     * \endif
     */
    public List<DepthWorkMode> getDepthWorkModeList() {
        throwInitializeException();
        return nGetDepthWorkModeList(mHandle);
    }

    /**
     * \if English
     * @brief Gets the current device synchronization configuration
     * @brief Device synchronization: including exposure synchronization function and multi-camera synchronization function of different sensors within a single
     * machine
     *
     * @return OBDeviceSyncConfig returns the device synchronization configuration
     * \else
     * @brief 获取当前设备同步配置
     * @brief 设备同步：包括单机内的不同 Sensor 的曝光同步功能 和 多机同步功能
     *
     * @return OBDeviceSyncConfig 返回设备同步配置
     * \endif
     *
     */
    public MultiDeviceSyncConfig getMultiDeviceSyncConfig() {
        throwInitializeException();
        return nGetMultiDeviceSyncConfig(mHandle);
    }

    /**
     * \if English
     * @brief Set the device synchronization configuration
     * @brief Used to configure the exposure synchronization function and multi-camera synchronization function of different sensors in a single machine
     *
     * @attention Calling this function will directly write the configuration to the device Flash, and it will still take effect after the device restarts. To
     * avoid affecting the Flash lifespan, do not update the configuration frequently.
     *
     * @param deviceSyncConfig Device synchronization configuration
     * \else
     * @brief 设置设备同步配置
     * @brief 用于配置 单机内的不同 Sensor 的曝光同步功能 和 多机同步功能
     *
     * @attention 调用本函数会直接将配置写入设备Flash，设备重启后依然会生效。为了避免影响Flash寿命，不要频繁更新配置。
     *
     * @param deviceSyncConfig 设备同步配置
     * \endif
     *
     */
    public void setMultiDeviceSyncConfig(MultiDeviceSyncConfig deviceSyncConfig) {
        throwInitializeException();
        deviceSyncConfig.getSyncMode(); // Check NullPointerExcetpion
        nSetMultiDeviceSyncConfig(mHandle, deviceSyncConfig);
    }

    /**
	 * \if English
	 * The device restarts. After the device restarts, the original device resources need to be released, and can be re-acquired after the device is reconnected.
	 * \else
     * 设备重启,设备重启后需要将原来的设备资源进行释放,待设备重新连接后可重新获取
     * \endif
     */
    public void reboot() {
        throwInitializeException();
        nReboot(mHandle);
    }

    /** 
	 * \if English
	 * release device resources
	 * \else
     * 释放设备资源
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        Log.d(TAG, "close: ");
        for (Sensor s : mSensors) {
            s.mOwner = true;
            s.close();
        }
        nDelete(mHandle);
        mHandle = 0;
    }

    /**
	 * \if English
	 * Device Status Monitoring
	 * \else
     * 设备状态监听
     * \endif
     */
    public interface OnStateChangeListener {
        /**
		 * \if English
	     * Callback when device status changes
         *
         * @param stateType state type
         * @param msg       state message
	     * \else
         * 设备状态发生改变时的回调
         *
         * @param stateType 状态类型
         * @param msg       状态信息
		 * \endif
         */
        void onStateChange(DeviceStateType stateType, String msg);
    }

    private interface OnStateChangeListenerImpl {
        void onStateChange(int stateType, String msg);
    }

    protected static native void nDelete(long handle);

    protected static native long[] nQuerySensor(long handle);

    protected static native DepthWorkMode nGetCurrentDepthWorkMode(long handle);

    protected static native void nSwitchDepthWorkMode(long handle, String modeName);

    protected static native List<DepthWorkMode> nGetDepthWorkModeList(long handle);

    protected static native int[] nGetSupportDepthPrecisionLevelList(long handle);

    protected static native boolean nIsPropertySupported(long handle, int property, int permission);

    protected static native void nSetPropertyValueB(long handle, int property, boolean value);

    protected static native void nSetPropertyValueI(long handle, int property, int value);

    protected static native void nSetPropertyValueF(long handle, int property, float value);

    protected static native void nSetPropertyValueDataType(long handle, int property, byte[] dataType);

//    protected static native void nSetPropertyValueDataTypeExt(long handle, int property, OBDataBundle dataBundle);

    protected static native boolean nGetPropertyValueB(long handle, int property);

    protected static native int nGetPropertyValueI(long handle, int property);

    protected static native float nGetPropertyValueF(long handle, int property);

    protected static native void nGetPropertyValueDataType(long handle, int property, byte[] dataType);

    protected static native OBDataBundle nGetPropertyValueDataTypeExt(long handle, int property);

    protected static native void nGetPropertyRangeB(long handle, int property, PropertyRangeB outParams);

    protected static native void nGetPropertyRangeI(long handle, int property, PropertyRangeI outParams);

    protected static native void nGetPropertyRangeF(long handle, int property, PropertyRangeF outParams);

    protected static native long nGetDeviceInfo(long handle);

    protected static native void nUpgrade(long handle, String fileName, UpgradeCallback callback);

    protected static native void nSendFileToDestination(long handle, String filePath, String dstFilePath, FileSendCallback callback);

    protected static native boolean nActivateAuthorization(long handle, String authCode);

//    protected static native Version nGetVersion(long handle);

    protected static native void nSetStateChangeListener(long handle, OnStateChangeListenerImpl listener);

    protected static native int nGetSupportedPropertyCount(long handle);

    protected static native DevicePropertyInfo nGetSupportedProperty(long handle, int index);

    protected static native long nSyncDeviceTime(long handle);

    protected static native MultiDeviceSyncConfig nGetMultiDeviceSyncConfig(long handle);

    protected static native void nSetMultiDeviceSyncConfig(long handle, MultiDeviceSyncConfig deviceSyncConfig);

    protected static native long nGetCalibrationCameraParamList(long handle);

    protected static native void nReboot(long handle);

    protected static native void nRebootDelayMode(long handle, int delayMs);
}
