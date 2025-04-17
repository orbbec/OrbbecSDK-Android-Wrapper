package com.orbbec.obsensor;

import com.orbbec.obsensor.types.FloatPropertyRange;

public class TemporalFilter extends Filter {
    /**
     * \if English
     * Create a Temporal Filter.
     * \else
     * 创建一个时域滤波器
     * \endif
     */
    public TemporalFilter(String activationKey) {
        super(nCreate(activationKey));
    }

    TemporalFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Get the TemporalFilter diffscale range.
     *
     * @return the diffscale value of property range
     * \else
     * 获取时域滤波器diffscale范围
     *
     * @return 属性范围的diffscale值
     * \endif
     */
    public FloatPropertyRange getDiffscaleRange() {
        throwInitializeException();
        FloatPropertyRange diffscaleRange = new FloatPropertyRange();
        nGetDiffscaleRange(mHandle, diffscaleRange.BYTES());
        boolean result = diffscaleRange.parseBytes();
        if (!result) {
            throw new OBException("getDiffscaleRange parse bytes error!");
        }
        return diffscaleRange;
    }

    /**
     * \if English
     * Set the TemporalFilter diffscale value.
     *
     * @param value diffscale value
     * \else
     * 设置时域滤波器diffscale值
     *
     * @param value diffscale值
     * \endif
     */
    public void setDiffscaleValue(float value) {
        throwInitializeException();
        setConfigValue("diff_scale", value);
//        nSetDiffscaleValue(mHandle, value);
    }

    /**
     * \if English
     * Get the TemporalFilter weight range.
     * \else
     * 获取时域滤波器权重范围
     * \endif
     */
    public FloatPropertyRange getWeightRange() {
        throwInitializeException();
        FloatPropertyRange weightRange = new FloatPropertyRange();
        nGetWeightRange(mHandle, weightRange.BYTES());
        boolean result = weightRange.parseBytes();
        if (!result) {
            throw new OBException("getWeightRange parse bytes error!");
        }
        return weightRange;
    }

    /**
     * \if English
     * Set the TemporalFilter weight value.
     *
     * @param value weight value
     * \else
     * 设置时域滤波器权重
     *
     * @param value 权重
     * \endif
     */
    public void setWeightValue(float value) {
        throwInitializeException();
        setConfigValue("weight", value);
//        nSetWeightValue(mHandle, value);
    }

    private static native long nCreate(String activationKey);

    private static native void nGetDiffscaleRange(long handle, byte[] diffscaleRange);

    private static native void nSetDiffscaleValue(long handle, double value);

    private static native void nGetWeightRange(long handle, byte[] weightRange);

    private static native void nSetWeightValue(long handle, float value);
}
