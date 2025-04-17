package com.orbbec.obsensor.types;

/**
 * \if English
 * Accelerometer range enumeration
 * \else
 * 加速度计量程枚举
 * \endif
 */
public enum AccelFullScaleRange {
    /**
     * \if English
     * Unknown
     * \else
     * 未知
     * \endif
     */
    FS_UNKNOWN(-1),
    /**
     * \if English
     * 1x the acceleration of gravity
     * \else
     * 1倍重力加速度
     * \endif
     */
    FS_2g(1),
    /**
     * \if English
     * 4 times the acceleration of gravity
     * \else
     * 4倍重力加速度
     * \endif
     */
    FS_4g(2),
    /**
     * \if English
     * 8 times the acceleration of gravity
     * \else
     * 8倍重力加速度
     * \endif
     */
    FS_8g(3),
    /**
     * \if English
     * 16 times the acceleration of gravity
     * \else
     * 16倍重力加速度
     * \endif
     */
    FS_16g(4);

    private final int mValue;

    AccelFullScaleRange(int value) {
        mValue = value;
    }

    /**
     * \if English
     * Get the index corresponding to the accelerometer range
     *
     * @return index value
     * \else
     * 获取加速度计量程对应的索引
     * @return 索引值
     * \endif
     */
    public int value() {
        return mValue;
    }

    /**
     * \if English
     * Get the range enumeration value corresponding to the specified index
     *
     * @param value index value
     * @param value 索引值
     * @return Accelerometer range enumeration value
     * \else
     * 获取指定索引对应的量程枚举值
     * @return 加速度计量程枚举值
     * \endif
     */
    public static AccelFullScaleRange get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
