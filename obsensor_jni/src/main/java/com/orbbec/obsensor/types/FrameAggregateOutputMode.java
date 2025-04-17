package com.orbbec.obsensor.types;

/**
 * \if English
 * Frame aggregate output mode
 * \else
 * 帧聚合输出模式
 * \endif
 */
public enum FrameAggregateOutputMode {
    /**
     * \if English
     * Only FrameSet that contains all types of data frames will be output
     * \else
     * 只有包含所有数据帧类型的FrameSet才会输出
     * \endif
     */
    OB_FRAME_AGGREGATE_OUTPUT_ALL_TYPE_FRAME_REQUIRE(0),
    /**
     * \if English
     * Color Frame Require output mode
     * Suitable for Color using H264, H265 and other inter-frame encoding format open stream
     * In this mode, the user may return null when getting a non-Color type data frame from the acquired FrameSet
     * \else
     * 彩色帧输出模式
     * 适用于彩色使用H264，H265和其他帧间编码格式打开流
     * 在此模式下，用户可以从获取到的FrameSet中获取非彩色类型的数据帧时，可能返回null
     * \endif
     */
    OB_FRAME_AGGREGATE_OUTPUT_COLOR_FRAME_REQUIRE(1),
    /**
     * \if English
     * FrameSet for any case will be output
     * In this mode, the user may return null when getting the specified type of data frame from the acquired FrameSet
     * \else
     * FrameSet对于任何情况都会输出
     * 在此模式下，用户可以从获取到的FrameSet中获取指定类型的数据帧时，可能返回null
     */
    OB_FRAME_AGGREGATE_OUTPUT_ANY_SITUATION(2),

    /**
     * \if English
     * Disable Frame Aggreate
     * In this mode, All types of data frames will output independently
     * \else
     * 禁用帧聚合
     * 在此模式下，所有类型的数据帧都会独立输出
     * \endif
     */
    OB_FRAME_AGGREGATE_OUTPUT_DISABLE(3);

    private final int mValue;

    FrameAggregateOutputMode(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static FrameAggregateOutputMode get(int value) {
        for (FrameAggregateOutputMode item : values()) {
            if (item.value() == value) {
                return item;
            }
        }
        return null;
    }
}
