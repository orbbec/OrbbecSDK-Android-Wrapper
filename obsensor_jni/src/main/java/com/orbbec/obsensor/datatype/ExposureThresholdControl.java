package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * TOF Exposure Threshold
 * \else
 * TOF曝光阈值
 * \endif
 */
public class ExposureThresholdControl extends DataType {
    private int mUpper;
    private int mLower;

    /**
	 * \if English
	 * Get the upper limit of the threshold, unit: ms
     *
     * @return upper threshold
	 * \else
     * 获取阈值上限，单位：ms
     *
     * @return 阈值上限
	 * \endif
     */
    public int getUpper() {
        throwInitializeException();
        return mUpper;
    }

    /**
	 * \if English
	 * Get the lower limit of the threshold, unit: ms
     *
     * @return lower threshold
	 * \else
     * 获取阈值下限，单位：ms
     *
     * @return 阈值下限
	 * \endif
     */
    public int getLower() {
        throwInitializeException();
        return mLower;
    }

    /**
	 * \if English
	 * Set the upper limit of the threshold, unit: ms
     *
     * @param upper upper threshold value
	 * \else
     * 设置阈值上限，单位：ms
     *
     * @param upper 阈值上限值
	 * \endif
     */
    public void setUpper(int upper) {
        this.mUpper = upper;
    }

    /**
	 * \if English
	 * Set the lower limit of the threshold, unit: ms
     *
     * @param lower Lower threshold value
	 * \else
     * 设置阈值下限，单位：ms
     *
     * @param lower 阈值下限值
	 * \endif
     */
    public void setLower(int lower) {
        this.mLower = lower;
    }

    @Override
    public int BYTES() {
        // Integer.BYTES * 2
        return 8;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        mUpper = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mLower = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        DataUtilities.appendBytes(DataUtilities.intToBytes(mUpper), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.intToBytes(mLower), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "ExposureThresholdControl{mUpper:" + mUpper + ", mLower:" + mLower + "}";
    }
}
