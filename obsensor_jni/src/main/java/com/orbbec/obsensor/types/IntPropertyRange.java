package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Structure for integer range
 * \else
 * 整数范围结构
 * \endif
 */
public class IntPropertyRange implements ByteConversion {
    /**
     * \if English
     * Current value
     * \else
     * 当前值
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private int mCur;
    /**
     * \if English
     * Maximum value
     * \else
     * 最大值
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private int mMax;
    /**
     * \if English
     * Minimum value
     * \else
     * 最小值
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private int mMin;
    /**
     * \if English
     * Step Value
     * \else
     * 步长
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private int mStep;
    /**
     * \if English
     * Default value
     * \else
     * 默认值
     * \endif
     */
    @StructField(offset = 16, size = 4)
    private int mDef;

    public int getCur() {
        return mCur;
    }

    public int getMax() {
        return mMax;
    }

    public int getMin() {
        return mMin;
    }

    public int getStep() {
        return mStep;
    }

    public int getDef() {
        return mDef;
    }

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[20];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
