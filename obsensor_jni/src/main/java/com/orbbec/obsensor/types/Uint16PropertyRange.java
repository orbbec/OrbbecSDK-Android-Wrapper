package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Structure for float range
 * \else
 * 浮点范围结构
 * \endif
 */
public class Uint16PropertyRange implements ByteConversion {
    /**
     * \if English
     * Current value
     * \else
     * 当前值
     * \endif
     */
    @StructField(offset = 0, size = 2)
    private int mCur;
    /**
     * \if English
     * Maximum value
     * \else
     * 最大值
     * \endif
     */
    @StructField(offset = 2, size = 2)
    private int mMax;
    /**
     * \if English
     * Minimum value
     * \else
     * 最小值
     * \endif
     */
    @StructField(offset = 4, size = 2)
    private int mMin;
    /**
     * \if English
     * Step Value
     * \else
     * 步长
     * \endif
     */
    @StructField(offset = 6, size = 2)
    private int mStep;
    /**
     * \if English
     * Default value
     * \else
     * 默认值
     * \endif
     */
    @StructField(offset = 8, size = 2)
    private int mDef;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[10];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
