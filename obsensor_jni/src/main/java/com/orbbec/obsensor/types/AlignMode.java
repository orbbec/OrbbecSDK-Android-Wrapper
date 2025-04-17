package com.orbbec.obsensor.types;

/**
 * \if English
 * D2C Status
 * \else
 * D2C状态
 * \endif
 */
public enum AlignMode {
    /**
     * \if English
     * Close D2C
     * \else
     * 关闭D2C
     * \endif
     */
    ALIGN_DISABLE(0),

    /**
     * \if English
     * Enable hardware D2C
     * \else
     * 使能硬件D2C
     * \endif
     */
    ALIGN_D2C_HW_MODE(1),

    /**
     * \if English
     * Enable software D2C
     * \else
     * 使能软件D2C
     * \endif
     */
    ALIGN_D2C_SW_MODE(2);

    private final int mValue;

    AlignMode(int value) {
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
    public static AlignMode get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
