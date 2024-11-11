package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

public class FloatPropertyRange extends DataType {

    private float mCur;
    private float mMax;
    private float mMin;
    private float mStep;
    private float mDef;

    /**
     * \if English
     * Get current value
     *
     * @return Current value
     * \else
     * 获取当前值
     *
     * @return 当前值
     * \endif
     */
    public float getCur() {
        throwInitializeException();
        return mCur;
    }

    /**
     * \if English
     * Get maximum value
     *
     * @return Maximum value
     * \else
     * 获取最大值
     *
     * @return 最大值
     * \endif
     */
    public float getMax() {
        throwInitializeException();
        return mMax;
    }

    /**
     * \if English
     * Get minimum value
     *
     * @return Minimum value
     * \else
     * 获取最小值
     *
     * @return 最小值
     * \endif
     */
    public float getMin() {
        throwInitializeException();
        return mMin;
    }

    /**
     * \if English
     * Get step value
     *
     * @return Step value
     * \else
     * 获取步长值
     *
     * @return 步长值
     * \endif
     */
    public float getStep() {
        throwInitializeException();
        return mStep;
    }

    /**
     * \if English
     * Get default value
     *
     * @return Default value
     * \else
     * 获取默认值
     *
     * @return 默认值
     * \endif
     */
    public float getDef() {
        throwInitializeException();
        return mDef;
    }

    @Override
    public int BYTES() {
        // Float.BYTES * 5
        return 20;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        mCur = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMax = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMin = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mStep = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mDef = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    public String toString() {
        return "FloatPropertyRange{" +
                "mCur=" + mCur +
                ", mMax=" + mMax +
                ", mMin=" + mMin +
                ", mStep=" + mStep +
                ", mDef=" + mDef +
                '}';
    }
}
