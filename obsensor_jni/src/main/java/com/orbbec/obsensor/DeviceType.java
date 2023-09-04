package com.orbbec.obsensor;

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
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the device type corresponding to the specified index
     *
     * @param value index value
     * @return device type
	 * \else
     * 获取指定索引对应的设备类型
     *
     * @param value 索引值
     * @return 设备类型
	 * \endif
     */
    public static DeviceType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
