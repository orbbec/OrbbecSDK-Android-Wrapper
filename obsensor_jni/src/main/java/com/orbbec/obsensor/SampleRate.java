package com.orbbec.obsensor;

/**
 * \if English
 * Enumeration of IMU sample rate values ​​(gyroscope or accelerometer)
 * \else
 * IMU采样率值的枚举(陀螺仪或加速度计)
 * \endif
 */
public enum SampleRate {
    /**
     * \if English
     * Unknown sample rate
     * \else
     * 未知采样率
     * \endif
     */
    UNKNOWN(0),
    /**
     * 1.5625Hz
     */
    ODR_1_5625_HZ(1),
    /**
     * 3.125Hz
     */
    ODR_3_125_HZ(2),
    /**
     * 6.25Hz
     */
    ODR_6_25_HZ(3),
    /**
     * 12.5Hz
     */
    ODR_12_5_HZ(4),
    /**
     * 25Hz
     */
    ODR_25_HZ(5),
    /**
     * 50Hz
     */
    ODR_50_HZ(6),
    /**
     * 100Hz
     */
    ODR_100_HZ(7),
    /**
     * 200Hz
     */
    ODR_200_HZ(8),
    /**
     * 500Hz
     */
    ODR_500_HZ(9),
    /**
     * 1KHz
     */
    ODR_1_KHZ(10),
    /**
     * 2KHz
     */
    ODR_2_KHZ(11),
    /**
     * 4KHz
     */
    ODR_4_KHZ(12),
    /**
     * 8KHz
     */
    ODR_8_KHZ(13),
    /**
     * 16KHz
     */
    ODR_16_KHZ(14),
    /**
     * 32Hz
     */
    ODR_32_KHZ(15);

    private final int mValue;

    SampleRate(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the accelerometer or gyroscope sampling frequency enumeration value
     *
     * @return index value
	 * \else
     * 获取加速度计或陀螺仪采样频率枚举值
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get accelerometer or gyroscope sampling frequency enumeration
     *
     * @param value index value
     * @return Accelerometer or gyroscope sampling frequency enumeration
	 * \else
     * 获取加速度计或陀螺仪采样频率枚举
     *
     * @param value 索引值
     * @return 加速度计或陀螺仪采样频率枚举
	 * \endif
     */
    public static SampleRate get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
