package com.orbbec.obsensor;

public enum HoleFillingMode {

    TOP(0),

    /**
     * \if English
     * "max" means farest for depth, and nearest for disparity; FILL_NEAREST
     * \else
     * "max"表示深度最大值， disparity最小值; FILL_NEAREST
     * \endif
     */
    NEAREST(1),

    FAREST(2);

    private final int mValue;

    /**
     * \if English
     * Hole fillig mode
     *
     * @param value index value
     * \else
     * 填洞滤波模式
     *
     * @param value 索引值
     * \endif
     */
    HoleFillingMode(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static HoleFillingMode get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
