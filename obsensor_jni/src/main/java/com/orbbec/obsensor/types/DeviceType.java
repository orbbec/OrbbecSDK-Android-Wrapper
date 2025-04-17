package com.orbbec.obsensor.types;

/**
 * \if English
 * Device Type
 * \else
 * 设备类型
 * \endif
 */
public enum DeviceType {
    /**
     * \if English
     * Unknown device type
     * \else
     * 未知设备类型
     * \endif
     */
    OB_UNKNOWN_DEVICE_TYPE(-1),
    /**
     * \if English
     * monocular structured light camera
     * \else
     * 单目结构光相机
     * \endif
     */
    OB_STRUCTURED_LIGHT_MONOCULAR_CAMERA(0),

    /**
     * \if English
     * Binocular structured light camera
     * \else
     * 双目结构光相机
     * \endif
     */
    OB_STRUCTURED_LIGHT_BINOCULAR_CAMERA(1),

    /**
     * \if English
     * tof camera
     * \else
     * TOF相机
     * \endif
     */
    OB_TOF_CAMERA(2);

    private final int mValue;

    DeviceType(int value) {
        mValue = value;
    }

    /**
     * \if English
     * The index corresponding to the device type
     *
     * @return index value
     * \else
     * 设备类型对应的索引
     * @return 索引值
     * \endif
     */
    public int value() {
        return mValue;
    }

    public static DeviceType get(int value) {
        for (DeviceType type : values()) {
            if (type.mValue == value) {
                return type;
            }
        }
        return null;
    }
}
