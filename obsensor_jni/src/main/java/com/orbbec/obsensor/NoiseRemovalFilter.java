package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.IntPropertyRange;
import com.orbbec.obsensor.datatype.NoiseRemovalParams;
import com.orbbec.obsensor.datatype.Uint16PropertyRange;

public class NoiseRemovalFilter extends Filter{
    /**
     * \if English
     * Create a noise removal filter.
     * \else
     * 创建一个去噪滤波器
     * \endif
     */
    public NoiseRemovalFilter() {
        super(nCreate());
    }

    NoiseRemovalFilter(long handle) {
        super(handle);
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
        nGetDispDiffRange(mHandle, dispDiffRange.getBytes());
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
        nGetMaxSizeRange(mHandle, maxSizeRange.getBytes());
        boolean result = maxSizeRange.parseBytes();
        if (!result) {
            throw new OBException("getMaxSizeRange parse bytes error!");
        }
        return maxSizeRange;
    }

    /**
     * \if English
     * Set the noise removal filter params.
     * \else
     * 设置空间滤波器参数
     * \endif
     */
    public void setFilterParams(NoiseRemovalParams params) {
        throwInitializeException();
        if (!params.wrapBytes()) {
            throw new OBException("setFilterParams wrap bytes error!");
        }
        nSetFilterParams(mHandle, params.getBytes());
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
    public NoiseRemovalParams getFilterParams() {
        throwInitializeException();
        NoiseRemovalParams params = new NoiseRemovalParams();
        nGetFilterParams(mHandle, params.getBytes());
        boolean result = params.parseBytes();
        if (!result) {
            throw new OBException("getFilterParams parse bytes error!");
        }
        return params;
    }

    private static native long nCreate();

    private static native void nGetDispDiffRange(long handle, byte[] dispDiffRange);

    private static native void nGetMaxSizeRange(long handle, byte[] maxSizeRange);

    private static native void nSetFilterParams(long handle, byte[] params);

    private static native void nGetFilterParams(long handle, byte[] params);
}
