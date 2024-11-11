package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.IntPropertyRange;

public class ThresholdFilter extends Filter{
    /**
     * \if English
     * Create a threshold filter.
     * \else
     * 创建一个截断滤波器
     * \endif
     */
    public ThresholdFilter() {
        super(nCreate());
    }

    ThresholdFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Get the threshold filter min range.
     * \else
     * 获取截断滤波器最小值
     * \endif
     */
    public IntPropertyRange getMinRange() {
        throwInitializeException();
        IntPropertyRange minRange = new IntPropertyRange();
        mGetMinRange(mHandle, minRange.getBytes());
        boolean result = minRange.parseBytes();
        if (!result) {
            throw new OBException("getScaleRange parse bytes error!");
        }
        return minRange;
    }

    /**
     * \if English
     * Get the threshold filter max range.
     * \else
     * 获取截断滤波器最大值
     * \endif
     */
    public IntPropertyRange getMaxRange() {
        throwInitializeException();
        IntPropertyRange maxRange = new IntPropertyRange();
        mGetMaxRange(mHandle, maxRange.getBytes());
        boolean result = maxRange.parseBytes();
        if (!result) {
            throw new OBException("getMaxRange parse bytes error!");
        }
        return maxRange;
    }

    /**
     * \if English
     * Set the threshold filter scale range.
     *
     * @param min threshold filter scale min value.
     * @param max threshold filter scale max value.
     * \else
     * 设置截断滤波器缩放范围
     *
     * @param min 截断滤波器缩放最小值
     * @param max 截断滤波器缩放最大值
     * \endif
     */
    public boolean setScaleValue(int min, int max) {
        throwInitializeException();
        return nSetScaleValue(mHandle, min, max);
    }

    private static native long nCreate();

    private native void mGetMinRange(long handle, byte[] minRange);

    private native void mGetMaxRange(long handle, byte[] maxRange);

    private native boolean nSetScaleValue(long handle, int min, int max);
}
