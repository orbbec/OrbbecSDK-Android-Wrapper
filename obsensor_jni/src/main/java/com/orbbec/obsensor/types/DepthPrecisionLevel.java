package com.orbbec.obsensor.types;

import com.orbbec.obsensor.DepthFrame;

/**
 * \if English
 * depth precision level
 * <p>
 * The depth precision level does not completely determine the depth unit and real precision, and the influence of the data packaging format needs to be considered.
 * The specific unit can be obtained through getValueScale() of DepthFrame
 *
 * @see DepthFrame#getValueScale()
 * \else
 * 深度精度等级
 * <p>
 * 深度精度等级并不完全决定深度的单位和真实精度，需要考虑数据打包格式的影响，
 * 具体单位可通过DepthFrame的getValueScale()获取
 * @see DepthFrame#getValueScale()
 * \endif
 */
public enum DepthPrecisionLevel {
    /**
     * 1mm
     */
    OB_PRECISION_1MM(0),

    /**
     * 0.8mm
     */
    OB_PRECISION_0MM8(1),

    /**
     * 0.4mm
     */
    OB_PRECISION_0MM4(2),

    /**
     * 0.1mm
     */
    OB_PRECISION_0MM1(3),
    /**
     * 0.2mm
     */
    OB_PRECISION_0MM2(4),
    /**
     * 0.5mm
     */
    OB_PRECISION_0MM5(5),
    /**
     * 0.05mm
     */
    OB_PRECISION_0MM05(6),
    /**
     * unknown
     */
    OB_PRECISION_UNKNOWN(7),
    OB_PRECISION_COUNT(8);

    private final int mValue;

    DepthPrecisionLevel(int value) {
        mValue = value;
    }

    /**
     * \if English
     * Get the index corresponding to the depth precision level
     *
     * @return index value
     * \else
     * 获取深度精度等级对应的索引
     * @return 索引值
     * \endif
     */
    public int value() {
        return mValue;
    }

    /**
     * \if English
     * Get the depth precision level corresponding to the specified index
     *
     * @param value index value
     * @param value 索引值
     * @return depth precision level
     * \else
     * 获取指定索引对应的深度精度等级
     * @return 深度精度等级
     * \endif
     */
    public static DepthPrecisionLevel get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
