package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

public class Uint16PropertyRange extends DataType {
    private int mCur;
    private int mMax;
    private int mMin;
    private int mStep;
    private int mDef;

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
    public int getCur() {
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
    public int getMax() {
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
    public int getMin() {
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
    public int getStep() {
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
    public int getDef() {
        throwInitializeException();
        return mDef;
    }

    @Override
    public int BYTES() {
        // Short.BYTES * 5 (Uint16 * 5)
        return 10;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Short.BYTES;
        mCur = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMax = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMin = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mStep = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mDef = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    public String toString() {
        return "Uint16PropertyRange{" +
                "mCur=" + mCur +
                ", mMax=" + mMax +
                ", mMin=" + mMin +
                ", mStep=" + mStep +
                ", mDef=" + mDef +
                '}';
    }
}
