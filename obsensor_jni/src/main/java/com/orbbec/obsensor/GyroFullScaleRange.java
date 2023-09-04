package com.orbbec.obsensor;

/**
 * \if English
 * Enumeration of gyroscope ranges
 * \else
 * 陀螺仪量程的枚举
 * \endif
 */
public enum GyroFullScaleRange {
    /**
	 * \if English
	 * 16 degrees per second
	 * \else
     * 16度每秒
     * \endif
     */
    FS_16dps(1),
    /**
	 * \if English
	 * 31 degrees per second
	 * \else
     * 31度每秒
     * \endif
     */
    FS_31dps(2),
    /**
	 * \if English
	 * 62 degrees per second
	 * \else
     * 62度每秒
     * \endif
     */
    FS_62dps(3),
    /**
	 * \if English
	 * 125 degrees per second
	 * \else
     * 125度每秒
     * \endif
     */
    FS_125dps(4),
    /**
	 * \if English
	 * 250 degrees per second
	 * \else
     * 250度每秒
     * \endif
     */
    FS_250dps(5),
    /**
	 * \if English
	 * 500 degrees per second
	 * \else
     * 500度每秒
     * \endif
     */
    FS_500dps(6),
    /**
	 * \if English
	 * 1000 degrees per second
	 * \else
     * 1000度每秒
     * \endif
     */
    FS_1000dps(7),
    /**
	 * \if English
	 * 2000 degrees per second
	 * \else
     * 2000度每秒
     * \endif
     */
    FS_2000dps(8);
    private final int mValue;

    GyroFullScaleRange(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the gyro range
     *
     * @return index value
	 * \else
     * 获取陀螺仪量程对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the range enumeration value corresponding to the specified index of the gyroscope
     *
     * @param value index value
     * @return Gyroscope range enumeration
	 * \else
     * 获取陀螺仪指定索引对应的量程枚举值
     *
     * @param value 索引值
     * @return 陀螺仪量程枚举
	 * \endif
     */
    public static GyroFullScaleRange get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
