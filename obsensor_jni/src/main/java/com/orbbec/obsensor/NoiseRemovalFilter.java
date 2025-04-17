package com.orbbec.obsensor;

import com.orbbec.obsensor.types.NoiseRemovalFilterParams;
import com.orbbec.obsensor.types.Uint16PropertyRange;

public class NoiseRemovalFilter extends Filter {
    /**
     * \if English
     * Create a noise removal filter.
     * \else
     * 创建一个去噪滤波器
     * \endif
     */
    public NoiseRemovalFilter(String activationKey) {
        super(nCreate(activationKey));
    }

    NoiseRemovalFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Set the noise removal filter params.
     * \else
     * 设置空间滤波器参数
     * \endif
     */
    public void setFilterParams(NoiseRemovalFilterParams params) {
        throwInitializeException();
        setConfigValue("max_size", params.getMaxSize());
        setConfigValue("min_diff", params.getDispDiff());
//        nSetFilterParams(mHandle, params.BYTES());
    }

    /**
     * \if English
     * Get the noise removal filter params.
     *
     * @return noise removal filter params
     * \else
     * 获取空间滤波器参数
     *
     * @return 空间滤波器参数
     * \endif
     */
    public NoiseRemovalFilterParams getFilterParams() {
        throwInitializeException();
        NoiseRemovalFilterParams params = new NoiseRemovalFilterParams();
        params.setMaxSize((int) getConfigValue("max_size"));
        params.setDispDiff((int) getConfigValue("min_diff"));
//        nGetFilterParams(mHandle, params.BYTES());
//        boolean result = params.parseBytes();
//        if (!result) {
//            throw new OBException("getFilterParams parse bytes error!");
//        }
        return params;
    }

    /**
     * \if English
     * Get the noise removal filter disp diff range.
     *
     * @return the disp_diff value of property range.
     * \else
     * 获取空间滤波器dispDiff范围
     *
     * @return dispdiff值
     * \endif
     */
    public Uint16PropertyRange getDispDiffRange() {
        throwInitializeException();
        Uint16PropertyRange dispDiffRange = new Uint16PropertyRange();
        nGetDispDiffRange(mHandle, dispDiffRange.BYTES());
        boolean result = dispDiffRange.parseBytes();
        if (!result) {
            throw new OBException("getDispDiffRange parse bytes error!");
        }
        return dispDiffRange;
    }

    /**
     * \if English
     * Get the noise removal filter max size range.
     *
     * @return the max_size value of property range.
     * \else
     * 获取空间滤波器最大尺寸范围
     *
     * @return maxsize值
     * \endif
     */
    public Uint16PropertyRange getMaxSizeRange() {
        throwInitializeException();
        Uint16PropertyRange maxSizeRange = new Uint16PropertyRange();
        nGetMaxSizeRange(mHandle, maxSizeRange.BYTES());
        boolean result = maxSizeRange.parseBytes();
        if (!result) {
            throw new OBException("getMaxSizeRange parse bytes error!");
        }
        return maxSizeRange;
    }

    private static native long nCreate(String activationKey);

    private static native void nGetDispDiffRange(long handle, byte[] dispDiffRange);

    private static native void nGetMaxSizeRange(long handle, byte[] maxSizeRange);

    private static native void nSetFilterParams(long handle, byte[] params);

    private static native void nGetFilterParams(long handle, byte[] params);
}
