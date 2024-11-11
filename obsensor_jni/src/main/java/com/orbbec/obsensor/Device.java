package com.orbbec.obsensor;

import android.util.Log;
import android.util.SparseArray;

import com.orbbec.internal.OBLocalUtils;
import com.orbbec.obsensor.datatype.DeviceTemperature;

import java.nio.ByteBuffer;
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        throwInitializeException();
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
        return nGetDeviceInfo(mHandle);
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
        throwInitializeException();
        List<Sensor> retList = new ArrayList<>();
        synchronized (mSensors) {
            int iSensorTypes[] = nQuerySensorTypes(mHandle);
            for (int itype : iSensorTypes) {
                SensorType type = SensorType.get(itype);
                if (null != type) {
                    boolean find = false;
                    for (Sensor sensor : mSensors) {
                        if (sensor.getType() == type) {
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        long sensorHandle = nGetSensor(mHandle, type.value());
                        mSensors.add(new Sensor(sensorHandle));
                    }
                } else {
                    throw new OBException("Not support sensorType: " + itype);
                }
            }
            retList.addAll(mSensors);
        }
        return retList;
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
        throwInitializeException();
        synchronized (mSensors) {
            for (Sensor sensor : mSensors) {
                if (sensor.getType() == type) {
                    return sensor;
                }
            }

            int iSensorTypes[] = nQuerySensorTypes(mHandle);
            boolean find = false;
            for (int itype : iSensorTypes) {
                if (itype == type.value()) {
                    find = true;
                    break;
                }
            }

            if (find) {
                long sensorHandle = nGetSensor(mHandle, type.value());
                Sensor sensor = new Sensor(sensorHandle);
                mSensors.add(sensor);
                return sensor;
            }
        }
        return null;
    }

    /**
     * \if English
     * Device is support SensorType
     * @param type sensorType
     * @return true: support sensorType, false: not support sensorType
     * \else
     * 设备是否支持传感器类型
     * @param type 传感器类型 {@link SensorType}
     * @return true：支持，false：不支持
     * \endif
     */
    public boolean hasSensor(SensorType type) {
        throwInitializeException();
        return nHasSensor(mHandle, type.value());
    }

    /**
	 * \if English
	 * Upgrade device firmware
     * Cautious: Not supported yet
     *
     * @param fileName firmware path
     * @param callback callback during the upgrade process {@link UpgradeCallback}
     * @throws OBException 	The firmware path is abnormal
	 * \else
     * 升级设备固件
     * 说明：开发中，待完善
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
     * Upgrade device firmware
     *
     * @param buffer Firmware file content
     *               Caution: {@link ByteBuffer#capacity()} is the valid data length.
     * @param callback callback during the upgrade process {@link UpgradeCallback}
     * @throws OBException 	The firmware path is abnormal
     * \else
     * 升级设备固件
     *
     * @param buffer 固件路径的二进制内容，注意{@link ByteBuffer#capacity()}的内容都是有效数据，否则升级会有问题，导致读取冗余脏数据
     * @param callback 升级过程回调 {@link UpgradeCallback}
     * @throws OBException 固件路径异常
     * \endif
     */
    public void upgrade(ByteBuffer buffer, UpgradeCallback callback) {
        if (null == buffer) {
            throw new OBException("Invalid fileData buffer: null");
        }
        throwInitializeException();
        nUpgrade(mHandle, buffer, callback);
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
        throwInitializeException();
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
     * @brief send the capture command to the device.
     * @brief The device will start one time image capture after receiving the capture command when it is in the @ref
     * OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING
     *
     * @attention The frequency of the user call this function multiplied by the number of frames per trigger should be less than the frame rate of the stream.
     * The number of frames per trigger can be set by @ref framesPerTrigger.
     * @attention For some models，receive and execute the capture command will have a certain delay and performance consumption, so the frequency of calling
     * this function should not be too high, please refer to the product manual for the specific supported frequency.
     * @attention If the device is not in the @ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING mode, device will ignore the capture command.
     * \else
     * @brief 将捕获命令发送到设备
     * @brief 当设备处于 @ref OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING 模式时，设备会进行一次图像抓拍
     *
     * @attention 用户调用此函数的频率乘以每个触发器的帧数应该小于流的帧速率。每个触发器的帧数可以由 @ref framesPerTriggerForTriggeringMode 设置
     * @attention 对于某些型号，接收和执行捕获命令会有一定的延迟和性能消耗，因此调用的频率此功能不宜过高，具体支持频率请参阅产品说明书
     * @attention 如果设备未处于 @ref OB_MULTI_device_SYNC_MODE_HARDWARE_TRIGGERING模式，则设备将忽略捕获命令
     * \endif
     */
    public void triggerCapture() {
        throwInitializeException();
        nTriggerCapture(mHandle);
    }

    /**
     * \if English
     * @brief set the timestamp reset configuration of the device.
     * \else
     * @brief 设置设备的时间戳重置配置
     * \endif
     */
    public void setTimestampResetConfig(TimestampResetConfig config) {
        throwInitializeException();
        nSetTimestampResetConfig(mHandle, config);
    }

    /**
     * \if English
     * @brief get the timestamp reset configuration of the device.
     *
     * @return OBDeviceTimestampResetConfig return the timestamp reset configuration of the device.
     * \else
     * @brief 获取设备的时间戳重置配置
     *
     * @return OBDeviceTimestampResetConfig 设备的时间戳重置配置.
     * \endif
     */
    public TimestampResetConfig getTimestampResetConfig() {
        throwInitializeException();
        return nGetTimestampResetConfig(mHandle);
    }

    /**
     * \if English
     * @brief send the timestamp reset command to the device.
     * @brief The device will reset the timer for calculating the timestamp for output frames to 0 after receiving the timestamp reset command when the
     * timestamp reset function is enabled. The timestamp reset function can be enabled by call @ref ob_device_set_timestamp_reset_config.
     * @brief Before calling this function, user should call @ref ob_device_set_timestamp_reset_config to disable the timestamp reset function (It is not
     * required for some models, but it is still recommended to do so for code compatibility).
     *
     * @attention If the stream of the device is started, the timestamp of the continuous frames output by the stream will jump once after the timestamp reset.
     * @attention Due to the timer of device is not high-accuracy, the timestamp of the continuous frames output by the stream will drift after a long time.
     * User can call this function periodically to reset the timer to avoid the timestamp drift, the recommended interval time is 60 minutes.
     * \else
     * @brief 向设备发送时间戳重置命令
     * @brief 当时间戳重置功能已启用。时间戳重置功能可以通过调用 @ref ob_device_set_timestamp_reset_config来启用
     * @brief 在调用此函数之前，用户应该调用 @ref ob_device_set_timestamp_reset_config
     * 来禁用时间戳重置函数（它不是某些型号需要，但为了代码兼容性，仍然建议这样做）
     *
     * @attention 如果设备的流被启动，则流输出的连续帧的时间戳将在时间戳重置后跳一次
     * @attention 由于设备的定时器精度不高，流输出的连续帧的时间戳在长时间后会漂移，用户可以定期调用此功能来重置计时器，以避免时间戳漂移，建议间隔时间为60分钟
     * \endif
     */
    public void timestampReset() {
        throwInitializeException();
        nTimestampReset(mHandle);
    }

    /**
     * \if English
     * @brief synchronize the timer of the device with the host.
     * @brief After calling this function, the timer of the device will be synchronized with the host. User can call this function to multiple devices to
     * synchronize all timers of the devices.
     *
     * @attention If the stream of the device is started, the timestamp of the continuous frames output by the stream will may jump once after the timer
     * sync.
     * @attention Due to the timer of device is not high-accuracy, the timestamp of the continuous frames output by the stream will drift after a long time.
     * User can call this function periodically to synchronize the timer to avoid the timestamp drift, the recommended interval time is 60 minutes.
     * \else
     * @brief 将设备的计时器与主机同步
     * @brief 调用此功能后，设备的计时器将与主机同步。用户可以将此函数调用到多个设备同步设备的所有定时器
     *
     * @attention 如果设备的流被启动，则该流输出的连续帧的时间戳可能在定时器同步之后跳一次
     * @attention 由于设备的定时器精度不高，流输出的连续帧的时间戳在长时间后会漂移。用户可以定期调用此功能来同步定时器，以避免时间戳漂移，建议间隔时间为60分钟
     * \endif
     *
     */
    public void timerSyncWithHost() {
        throwInitializeException();
        nTimerSyncWithHost(mHandle);
    }

    /**
	 * \if English
	 * Get the original parameter list of camera calibration saved in the device.
     *
     * @attention The parameters in the list do not correspond to the current open-current configuration. You need to select the parameters according to the
     * actual situation, and may need to do scaling, mirroring and other processing. Non-professional users are recommended to use the
     * Pipeline#getCameraParam() interface.
     *
     * @return Camera parameter list {@link CameraParamList}
	 * \else
     * 获取设备内保存的相机标定的原始参数列表，
     *
     * @attention 列表内参数不与当前开流配置相对应，需要自行根据实际情况选用参数并可能需要做缩放、镜像等处理。非专业用户建议使用Pipeline#getCameraParam()接口。
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
            try {
                sensor.release();
            } catch (Exception ignore) {
            }
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
     * Get network config
     * Only for some network device, Such as Gemini 2 XL,
     * \else
     * 获取网络配置
     * 说明：仅支持网络功能的设备，例如 Gemini 2 XL
     * \endif
     */
    public OBNetworkConfig getNetworkConfig() {
        throwInitializeException();
        return nGetNetworkConfig(mHandle);
    }

    /**
     * \if English
     * Set network config
     * Only for some network device, Such as Gemini 2 XL,
     * \else
     * 设置网络配置
     * 说明：仅支持网络功能的设备，例如 Gemini 2 XL
     * \endif
     */
    public void setNetworkConfig(OBNetworkConfig config) {
        nSetNetworkConfig(mHandle, config);
    }

    /**
     * \if English
     * @brief get the multi device sync configuration of the device.
     *
     * @return OBMultiDeviceSyncConfig return the multi device sync configuration of the device.
     * \else
     *
     * @brief 获取设备的多设备同步配置
     *
     * @return OBMultiDeviceSyncConfig 设备的多设备同步配置
     * \endif
     *
     */
    public MultiDeviceSyncConfig getMultiDeviceSyncConfig() {
        throwInitializeException();
        return nGetMultiDeviceSyncConfig(mHandle);
    }

    /**
     * \if English
     * @brief set the multi device sync configuration of the device.
     *
     * @param[in] config The multi device sync configuration.
     * \else
     * @brief 设置设备的多设备同步配置
     *
     * @param[in] config 多设备同步配置
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
     * Check if the device supports global timestamp.
     *
     * @return true supports, false does not support
     * \else
     * 检查设备是否支持全局时间戳
     *
     * @return true 支持, false 不支持
     * \endif
     */
    public boolean isGlobalTimestampSupported() {
        throwInitializeException();
        return nIsGlobalTimestampSupported(mHandle);
    }

    /**
     * \if English
     * Load depth filter config from file.
     * \else
     * 重置默认的深度滤波配置
     * \endif
     */
    public void resetDefaultDepthFilterConfig() {
        throwInitializeException();
        nResetDefaultDepthFilterConfig(mHandle);
    }

    /**
     * \if English
     * Get the current preset name.
     *
     * @return The current preset name, it should be one of the preset names returned by @ref ob_device_get_available_preset_list.
     * \else
     * 获取当前预置名称
     *
     * @return 当前预设名称，它应该是@ref ob_device_get_available_preset_list返回的预设名称之一。
     * \endif
     */
    public String getCurrentPresetName() {
        throwInitializeException();
        return nGetCurrentPresetName(mHandle);
    }

    /**
     * \if English
     * @brief Get the available preset list.
     *
     * @param[in] Log error messages. The name should be one of the preset names returned by @ref ob_device_get_available_preset_list.
     * \else
     * @brief 获取可用预置列表
     *
     * @param[in] 记录错误消息。该名称应该是@ref ob_device_get_available_preset_list返回的预设名称之一。
     * \endif
     */
    public void loadPreset(String presetName) {
        throwInitializeException();
        nLoadPreset(mHandle, presetName);
    }

    /**
     * \if English
     * @brief Load preset from json string.
     *
     * @param[in] The json file path.
     * \else
     * @brief 从json字符串加载预置
     *
     * @param[in] json字符串
     * \endif
     */
    public void loadPresetFromJsonFile(String jsonFilePath) {
        throwInitializeException();
        nLoadPresetFromJsonFile(mHandle, jsonFilePath);
    }

    /**
     * \if English
     * @brief Export current settings as a preset json file.
     *
     * @param[in] The json file path.
     * \else
     * @brief 将当前设置导出为预置json文件
     *
     * @param[in] json字符串
     * \endif
     */
    public void exportCurrentSettingsAsPresetJsonFile(String jsonFilePath) {
        throwInitializeException();
        nExportCurrentSettingsAsPresetJsonFile(mHandle, jsonFilePath);
    }

    /**
     * \if English
     * Get the available preset list.
     *
     * @return The available preset list.
     * \else
     * 获取可用预置列表
     *
     * @return 可用预置列表
     * \endif
     */
    public PresetList getAvailablePresetList() {
        throwInitializeException();
        long handle = nGetAvailablePresetList(mHandle);
        return handle != 0 ? new PresetList(handle) : null;
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
            try {
                s.release();
            } catch (Exception ignore) {
            }
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

    protected static native boolean nHasSensor(long handle, int sensorType);

    protected static native int[] nQuerySensorTypes(long handle);

    protected static native long nGetSensor(long handle, int sensorType);

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

//    protected static native byte[] nGetPropertyItem(long handle, int property);

    protected static native OBDataBundle nGetPropertyValueDataTypeExt(long handle, int property);

    protected static native void nGetPropertyRangeB(long handle, int property, PropertyRangeB outParams);

    protected static native void nGetPropertyRangeI(long handle, int property, PropertyRangeI outParams);

    protected static native void nGetPropertyRangeF(long handle, int property, PropertyRangeF outParams);

    protected static native DeviceInfo nGetDeviceInfo(long handle);

    protected static native void nUpgrade(long handle, String fileName, UpgradeCallback callback);

    protected static native void nUpgrade(long handle, ByteBuffer buffer, UpgradeCallback callback);

    protected static native void nSendFileToDestination(long handle, String filePath, String dstFilePath, FileSendCallback callback);

    protected static native boolean nActivateAuthorization(long handle, String authCode);

//    protected static native Version nGetVersion(long handle);

    protected static native void nSetStateChangeListener(long handle, OnStateChangeListenerImpl listener);

    protected static native int nGetSupportedPropertyCount(long handle);

    protected static native DevicePropertyInfo nGetSupportedProperty(long handle, int index);

    protected static native void nTriggerCapture(long handle);

    protected static native void nSetTimestampResetConfig(long handle, TimestampResetConfig config);

    protected static native TimestampResetConfig nGetTimestampResetConfig(long handle);

    protected static native void nTimestampReset(long handle);

    protected static native void nTimerSyncWithHost(long handle);

    protected static native MultiDeviceSyncConfig nGetMultiDeviceSyncConfig(long handle);

    protected static native void nSetMultiDeviceSyncConfig(long handle, MultiDeviceSyncConfig deviceSyncConfig);

    protected static native OBNetworkConfig nGetNetworkConfig(long handle);

    protected static native void nSetNetworkConfig(long handle, OBNetworkConfig config);

    protected static native long nGetCalibrationCameraParamList(long handle);

    protected static native boolean nIsGlobalTimestampSupported(long handle);

    protected static native void nResetDefaultDepthFilterConfig(long handle);

    protected static native String nGetCurrentPresetName(long handle);

    protected static native void nLoadPreset(long handle, String presetName);

    protected static native void nLoadPresetFromJsonFile(long handle, String jsonFilePath);

    protected static native void nExportCurrentSettingsAsPresetJsonFile(long handle, String jsonFilePath);

    protected static native long nGetAvailablePresetList(long handle);

    protected static native void nReboot(long handle);

    protected static native void nRebootDelayMode(long handle, int delayMs);
}
