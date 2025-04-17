package com.orbbec.obsensor.types;

/**
 * \if English
 * Enumeration value describing the pixel type of frame (usually used for depth frame)
 * \else
 * 描述帧像素类型的枚举值（通常用于深度帧）
 * \endif
 */
public enum PixelType {
    /**
     * \if English
     * Unknown pixel type, or undefined pixel type for current frame
     * \else
     * 未知像素类型，或当前帧未定义的像素类型
     * \endif
     */
    UNKNOWN(-1),
    /**
     * \if English
     * Depth pixel type, the value of the pixel is the distance from the camera to the object
     * \else
     * 深度像素类型，像素的值是相机到物体的距离
     * \endif
     */
    DEPTH(0),
    /**
     * \if English
     * Disparity for structured light camera
     * \else
     * 结构光相机的视差
     * \endif
     */
    DISPARITY(2),
    /**
     * \if English
     * Raw phase for tof camera
     * \else
     * tof相机的原始阶段
     * \endif
     */
    RAW_PHASE(3);

    private final int mValue;

    PixelType(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static PixelType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return UNKNOWN;
    }
}
