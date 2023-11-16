package com.orbbec.obsensor;

/**
 * \if English
 * Enumeration value describing the type of data stream
 * \else
 * 描述数据流类型的枚举值
 * \endif
 */
public enum StreamType {
    /**
	 * \if English
	 * Video stream (infrared, color, depth streams are all video streams)
	 * \else
     * 视频流(红外、彩色、深度流都属于视频流)
     * \endif
     */
    VIDEO(0),
    /**
	 * \if English
	 * infrared stream
	 * \else
     * 红外流
     * \endif
     */
    IR(1),
    /**
	 * \if English
	 * color stream
	 * \else
     * 彩色流
     * \endif
     */
    COLOR(2),
    /**
	 * \if English
	 * depth stream
	 * \else
     * 深度流
     * \endif
     */
    DEPTH(3),
    /**
	 * \if English
	 * Accelerometer data stream
	 * \else
     * 加速度计数据流
     * \endif
     */
    ACCEL(4),
    /**
	 * \if English
	 * Gyroscope data stream
	 * \else
     * 陀螺仪数据流
     * \endif
     */
    GYRO(5),

    /**
     * \if English
     * Left IR stream
     * \else
     * 左路红外流
     * \endif
     */
    IR_LEFT(6),

    /**
     * \if English
     * Right IR stream
     * \else
     * 右路红外流
     * \endif
     */
    IR_RIGHT(7),

    /**
     * \if English
     * RawPhase Stream
     * \else
     * TODO lumiaozi
     * \endif
     */
    RAW_PHASE(8),
    ;

    private final int mValue;

    StreamType(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the data stream type
     *
     * @return index value
	 * \else
     * 获取数据流类型对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the data stream type corresponding to the specified index
     *
     * @param value index value
     * @return data stream type
	 * \else
     * 获取指定索引对应的数据流类型
     *
     * @param value 索引值
     * @return 数据流类型
	 * \endif
     */
    public static StreamType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
