package com.orbbec.obsensor;

import com.orbbec.obsensor.types.SensorType;

public class SensorList extends LobClass {

    SensorList(long handle) {
        mHandle = handle;
    }

    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    /**
     * \if English
     * Get the number of sensors.
     *
     * @return The number of sensors.
     * \else
     * 获取传感器的数量
     *
     * @return 传感器的数量
     */
    public int getCount() {
        throwInitializeException();
        return nGetCount(mHandle);
    }

    /**
     * \if English
     * Get the type of the specified sensor.
     *
     * @param index The sensor index.
     * @return The sensor type.
     * \else
     * 获取指定索引的传感器类型
     *
     * @param index 传感器的索引
     * @return 传感器的类型
     */
    public SensorType getSensorType(int index) {
        throwInitializeException();
        return SensorType.get(nGetSensorType(mHandle, index));
    }

    /**
     * \if English
     * Get a sensor by index number.
     *
     * @param index The sensor index. The range is [0, count-1]. If the index exceeds the range, an exception will be thrown.
     * @return The sensor object.
     * \else
     * 通过索引获取传感器对象
     *
     * @param index 传感器的索引
     * @return 传感器对象
     */
    public Sensor getSensor(int index) {
        throwInitializeException();
        long handle = nGetSensor(mHandle, index);
        return handle != 0 ? new Sensor(handle) : null;
    }

    /**
     * \if English
     * Get a sensor by sensor type.
     *
     * @param type The sensor type to obtain.
     * @return A sensor object. If the specified sensor type does not exist, it will return empty.
     * \else
     * 通过传感器类型获取传感器对象
     *
     * @param type 需要获取的传感器类型
     * @return 传感器对象。如果指定的传感器类型不存在，则返回空。
     */
    public Sensor getSensor(SensorType type) {
        throwInitializeException();
        long handle = nGetSensorByType(mHandle, type.value());
        return handle != 0 ? new Sensor(handle) : null;
    }

    private native void nDelete(long handle);

    private native int nGetCount(long handle);

    private native int nGetSensorType(long handle, int index);

    private native long nGetSensor(long handle, int index);

    private native long nGetSensorByType(long handle, int type);
}
