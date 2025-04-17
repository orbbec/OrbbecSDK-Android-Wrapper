package com.orbbec.obsensor.types;

/**
 * \if English
 * Playback data types
 * \else
 * 回放数据类型
 * \endif
 */
public enum MediaType {
    /**
     * \if English
     * color stream
     * \else
     * 彩色流
     * \endif
     */
    OB_MEDIA_COLOR_STREAM(1),

    /**
     * \if English
     * depth stream
     * \else
     * 深度流
     * \endif
     */
    OB_MEDIA_DEPTH_STREAM(2),

    /**
     * \if English
     * IR stream
     * \else
     * 红外流
     * \endif
     */
    OB_MEDIA_IR_STREAM(4),

    /**
     * \if English
     * gyro stream
     * \else
     * 陀螺仪数据流
     * \endif
     */
    OB_MEDIA_GYRO_STREAM(8),

    /**
     * \if English
     * accel stream
     * \else
     * 加速度及数据流
     * \endif
     */
    OB_MEDIA_ACCEL_STREAM(16),

    /**
     * \if English
     * camera parameter
     * \else
     * 相机参数
     * \endif
     */
    OB_MEDIA_CAMERA_PARAM(32),

    /**
     * \if English
     * device information
     * \else
     * 设备信息
     * \endif
     */
    OB_MEDIA_DEVICE_INFO(64),

    /**
     * \if English
     * stream information
     * \else
     * 流信息
     * \endif
     */
    OB_MEDIA_STREAM_INFO(128),

    /**
     * \if English
     * Left infrared stream
     * \else
     * 左侧红外流
     * \endif
     */
    OB_MEDIA_IR_LEFT_STREAM(256),

    /**
     * \if English
     * Right infrared stream
     * \else
     * 右侧红外流
     * \endif
     */
    OB_MEDIA_IR_RIGHT_STREAM(512),

    /**
     * \if English
     * all types
     * \else
     * 所有类型
     * \endif
     */
    OB_MEDIA_ALL(OB_MEDIA_COLOR_STREAM.value()
            | OB_MEDIA_DEPTH_STREAM.value()
            | OB_MEDIA_IR_STREAM.value()
            | OB_MEDIA_GYRO_STREAM.value()
            | OB_MEDIA_ACCEL_STREAM.value()
            | OB_MEDIA_CAMERA_PARAM.value()
            | OB_MEDIA_DEVICE_INFO.value()
            | OB_MEDIA_STREAM_INFO.value()
            | OB_MEDIA_IR_LEFT_STREAM.value()
            | OB_MEDIA_IR_RIGHT_STREAM.value());

    private final int mValue;

    MediaType(int value) {
        mValue = value;
    }

    /**
     * \if English
     * Get the index corresponding to the data stream format
     *
     * @return index value
     * \else
     * 获取数据流格式对应的索引
     * @return 索引值
     * \endif
     */
    public int value() {
        return mValue;
    }

    /**
     * \if English
     * Get the data stream format corresponding to the specified index
     *
     * @param value index value
     * @param value 索引值
     * @return data stream format
     * \else
     * 获取指定索引对应的数据流格式
     * @return 数据流格式
     * \endif
     */
    public static MediaType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
