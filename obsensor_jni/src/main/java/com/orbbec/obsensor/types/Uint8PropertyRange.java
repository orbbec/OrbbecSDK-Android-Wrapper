package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Structure for float range
 * \else
 * 浮点范围结构
 * \endif
 */
public class Uint8PropertyRange implements ByteConversion {
    /**
     * \if English
     * Current value
     * \else
     * 当前值
     * \endif
     */
    @StructField(offset = 0, size = 1)
    private short mCur;
    /**
     * \if English
     * Maximum value
     * \else
     * 最大值
     * \endif
     */
    @StructField(offset = 1, size = 1)
    private short mMax;
    /**
     * \if English
     * Minimum value
     * \else
     * 最小值
     * \endif
     */
    @StructField(offset = 2, size = 1)
    private short mMin;
    /**
     * \if English
     * Step Value
     * \else
     * 步长
     * \endif
     */
    @StructField(offset = 3, size = 1)
    private short mStep;
    /**
     * \if English
     * Default value
     * \else
     * 默认值
     * \endif
     */
    @StructField(offset = 4, size = 1)
    private short mDef;

    public short getCur() {
        return mCur;
    }

    public short getMax() {
        return mMax;
    }

    public short getMin() {
        return mMin;
    }

    public short getStep() {
        return mStep;
    }

    public short getDef() {
        return mDef;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[5];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
