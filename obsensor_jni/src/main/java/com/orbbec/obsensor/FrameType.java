package com.orbbec.obsensor;

/**
 * \if English
 * Describe the Frame type enumeration value
 * \else
 * 描述Frame类型枚举值
 * \endif
 */
public enum FrameType {
    /**
	 * \if English
	 * Video frames (infrared, color, depth frames are all video frames)
	 * \else
	 * 视频帧（红外、彩色、深度帧都属于视频帧）
     * \endif
     */
    VIDEO(0),
    /**
	 * \if English
	 * Infrared frame
	 * \else
     * 红外帧
     * \endif
     */
    IR(1),
    /**
	 * \if English
	 * color frame
	 * \else
     * 彩色帧
     * \endif
     */
    COLOR(2),
    /**
	 * \if English
	 * depth frame
	 * \else
     * 深度帧
     * \endif
     */
    DEPTH(3),
    /**
	 * \if English
	 * Accelerometer data frame
	 * \else
     * 加速度计数据帧
     * \endif
     */
    ACCEL(4),
    /**
	 * \if English
	 * Frame collection (internally contains a variety of data frames)
	 * \else
     * 帧集合（内部包含多种数据帧）
     * \endif
     */
    FRAME_SET(5),
    /**
	 * \if English
	 * point cloud frame
	 * \else
     * 点云帧
     * \endif
     */
    POINTS(6),
    /**
	 * \if English
	 * Gyroscope data frame
	 * \else
     * 陀螺仪数据帧
     * \endif
     */
    GYRO(7),

    /**
     * \if English
     * Left IR frame
     * \else
     * 左路红外帧
     * \endif
     */
    IR_LEFT(8),

    /**
     * \if English
     * Right IR frame
     * \else
     * 右路红外帧
     * \endif
     */
    IR_RIGHT(9),
    /**
     * \if English
     * Rawphase frame
     * \else
     * TODO lumiaozi
     * \endif
     */
    RAW_PHASE(10)
    ;

    private final int mValue;

    FrameType(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the data frame type
     *
     * @return index value
	 * \else
     * 获取数据帧类型对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the data frame type corresponding to the specified index
     *
     * @param value index value
     * @return data frame type
	 * \else
     * 获取指定索引对应的数据帧类型
     *
     * @param value 索引值
     * @return 数据帧类型
	 * \endif
     */
    public static FrameType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
