package com.orbbec.obsensor;

/**
 * \if English
 * Enumeration value describing the sensor type
 * \else
 * 描述传感器类型的枚举值
 * \endif
 */
public enum SensorType {
    /**
	 * \if English
	 * Unknown type sensor
	 * \else
     * 未知类型传感器
     * \endif
     */
    UNKNOWN(0),
    /**
	 * \if English
	 * infrared
	 * \else
     * 红外
     * \endif
     */
    IR(1),
    /**
	 * \if English
	 * color
	 * \else
     * 彩色
     * \endif
     */
    COLOR(2),
    /**
	 * \if English
	 * depth
	 * \else
     * 深度
     * \endif
     */
    DEPTH(3),
    /**
	 * \if English
	 * accelerometer
	 * \else
     * 加速度计
     * \endif
     */
    ACCEL(4),
    /**
	 * \if English
	 * gyro
	 * \else
     * 陀螺仪
     * \endif
     */
    GYRO(5),

    /**
     * \if English
     * Left IR
     * \else
     * 左红外
     * \endif
     */
    IR_LEFT(6),

    /**
     * \if English
     * Right IR
     * \else
     * 右红外
     * \endif
     */
    IR_RIGHT(7)
    ;

    private final int mValue;

    SensorType(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the sensor type
     *
     * @return index value
	 * \else
     * 获取传感器类型对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the sensor type corresponding to the specified index
     *
     * @param value index value
     * @return sensor type
	 * \else
     * 获取指定索引对应的传感器类型
     *
     * @param value 索引值
     * @return 传感器类型
	 * \endif
     */
    public static SensorType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
