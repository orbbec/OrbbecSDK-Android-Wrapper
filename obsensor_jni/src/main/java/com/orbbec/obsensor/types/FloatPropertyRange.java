package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Structure for float range
 * \else
 * 浮点范围结构
 * \endif
 */
public class FloatPropertyRange implements ByteConversion {
    /**
     * \if English
     * Current value
     * \else
     * 当前值
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private float mCur;
    /**
     * \if English
     * Maximum value
     * \else
     * 最大值
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private float mMax;
    /**
     * \if English
     * Minimum value
     * \else
     * 最小值
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private float mMin;
    /**
     * \if English
     * Step Value
     * \else
     * 步长
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private float mStep;
    /**
     * \if English
     * Default value
     * \else
     * 默认值
     * \endif
     */
    @StructField(offset = 16, size = 4)
    private float mDef;

    public float getCur() {
        return mCur;
    }

    public float getMax() {
        return mMax;
    }

    public float getMin() {
        return mMin;
    }

    public float getStep() {
        return mStep;
    }

    public float getDef() {
        return mDef;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
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
