package com.orbbec.obsensor.types;

/**
 * \if English
 * Preset tag
 * \else
 * 预设标签
 * \endif
 */
public enum DepthWorkModeTag {
    OB_DEVICE_DEPTH_WORK_MODE(0),
    OB_CUSTOM_DEPTH_WORK_MODE(1);

    private final int mValue;

    DepthWorkModeTag(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static DepthWorkModeTag get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
