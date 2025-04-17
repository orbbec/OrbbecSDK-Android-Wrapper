package com.orbbec.obsensor.types;

/**
 * \if English
 * Device Status Type
 * \else
 * 设备状态类型
 * \endif
 */
public enum DeviceStateType {
    /**
     * \if English
     * The device is normal, the default value
     * \else
     * 设备正常，默认值
     * \endif
     */
    NORMAL,
    /**
     * \if English
     * Device high temperature warning
     * \else
     * 设备高温警告
     * \endif
     */
    WARN,
    /**
     * \if English
     * Device overtemperature abnormality
     * \else
     * 设备超温异常
     * \endif
     */
    FATAL
}
