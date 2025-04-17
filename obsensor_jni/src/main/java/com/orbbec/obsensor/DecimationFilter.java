package com.orbbec.obsensor;

import com.orbbec.obsensor.types.Uint8PropertyRange;

public class DecimationFilter extends Filter {

    /**
     * \if English
     * Create a decimation filter.
     * \else
     * 创建一个降采样滤波器
     * \endif
     */
    public DecimationFilter() {
        super(nCreate());
    }

    DecimationFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Set the decimation filter scale value.
     *
     * @param value decimation filter scale value.
     * \else
     * 设置降采样滤波器缩放值
     *
     * @param value 降采样滤波器缩放值
     * \endif
     */
    public void setScaleValue(short value) {
        throwInitializeException();
//        nSetScaleValue(mHandle, value);
        setConfigValue("decimate", value);
    }

    /**
     * \if English
     * Get the decimation filter scale value.
     *
     * @return decimation filter scale value.
     * \else
     * 获取降采样滤波器缩放值
     *
     * @return 降采样滤波器缩放值
     * \endif
     */
    public short getScaleValue() {
        throwInitializeException();
        return (short) getConfigValue("decimate");
    }

    /**
     * \if English
     * Get the decimation filter scale range.
     * \else
     * 获取降采样滤波器缩放范围
     * \endif
     */
    public Uint8PropertyRange getScaleRange() {
        throwInitializeException();
        Uint8PropertyRange scaleRange = new Uint8PropertyRange();
        nGetScaleRange(mHandle, scaleRange.BYTES());
        boolean result = scaleRange.parseBytes();
        if (!result) {
            throw new OBException("getScaleRange parse bytes error!");
        }
        return scaleRange;
    }

    private static native long nCreate();

    private static native void nGetScaleRange(long handle, byte[] scaleRange);

    private static native void nSetScaleValue(long handle, short value);

    private static native short nGetScaleValue(long handle);
}
