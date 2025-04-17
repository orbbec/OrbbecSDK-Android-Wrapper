package com.orbbec.obsensor;

import com.orbbec.obsensor.types.FloatPropertyRange;
import com.orbbec.obsensor.types.IntPropertyRange;
import com.orbbec.obsensor.types.SpatialAdvancedFilterParams;
import com.orbbec.obsensor.types.Uint16PropertyRange;

public class SpatialAdvancedFilter extends Filter {
    /**
     * \if English
     * Create a spatial advanced filter.
     * \else
     * 创建一个空间滤波器
     * \endif
     */
    public SpatialAdvancedFilter(String activationKey) {
        super(nCreate(activationKey));
    }

    SpatialAdvancedFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Get the spatial advanced filter alpha range.
     *
     * @return the alpha value of property range.
     * \else
     * 获取空间滤波器alpha范围
     *
     * @return 属性范围的alpha值
     * \endif
     */
    public FloatPropertyRange getAlphaRange() {
        throwInitializeException();
        FloatPropertyRange alphaRange = new FloatPropertyRange();
        nGetAlphaRange(mHandle, alphaRange.BYTES());
        boolean result = alphaRange.parseBytes();
        if (!result) {
            throw new OBException("getAlphaRange parse bytes error!");
        }
        return alphaRange;
    }

    /**
     * \if English
     * Get the spatial advanced filter disp diff range.
     *
     * @return the dispdiff value of property range.
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
     * Get the spatial advanced filter radius range.
     *
     * @return the radius value of property range.
     * \else
     * 获取空间滤波器半径范围
     *
     * @return 半径范围值
     * \endif
     */
    public Uint16PropertyRange getRadiusRange() {
        throwInitializeException();
        Uint16PropertyRange radiusRange = new Uint16PropertyRange();
        nGetRadiusRange(mHandle, radiusRange.BYTES());
        boolean result = radiusRange.parseBytes();
        if (!result) {
            throw new OBException("getRadiusRange parse bytes error!");
        }
        return radiusRange;
    }

    /**
     * \if English
     * Get the spatial advanced filter magnitude range.
     *
     * @return the magnitude value of property range.
     * \else
     * 获取空间滤波器幅度范围
     *
     * @return 幅度范围值
     * \endif
     */
    public IntPropertyRange getMagnitudeRange() {
        throwInitializeException();
        IntPropertyRange magnitudeRange = new IntPropertyRange();
        nGetMagnitudeRange(mHandle, magnitudeRange.BYTES());
        boolean result = magnitudeRange.parseBytes();
        if (!result) {
            throw new OBException("getMagnitudeRange parse bytes error!");
        }
        return magnitudeRange;
    }

    /**
     * \if English
     * Get the spatial advanced filter params.
     * \else
     *  获取空间滤波器参数
     * \endif
     */
    public SpatialAdvancedFilterParams getFilterParams() {
        throwInitializeException();
        SpatialAdvancedFilterParams params = new SpatialAdvancedFilterParams();
        params.setAlpha((float) getConfigValue("alpha"));
        params.setDispDiff((int) getConfigValue("disp_diff"));
        params.setMagnitude((short) getConfigValue("magnitude"));
        params.setRadius((int) getConfigValue("radius"));
//        nGetFilterParams(mHandle, params.BYTES());
//        boolean result = params.parseBytes();
//        if (!result) {
//            throw new OBException("getFilterParams parse bytes error!");
//        }
        return params;
    }

    /**
     * \if English
     * Set the spatial advanced filter params.
     *
     * @param params spatial advanced filter params
     * \else
     * 设置空间滤波器参数
     *
     * @param params 空间滤波器参数
     * \endif
     */
    public void setFilterParams(SpatialAdvancedFilterParams params) {
        throwInitializeException();
        setConfigValue("alpha", params.getAlpha());
        setConfigValue("disp_diff", params.getDispDiff());
        setConfigValue("magnitude", params.getMagnitude());
        setConfigValue("radius", params.getRadius());
//        nSetFilterParams(mHandle, params.BYTES());
    }

    private static native long nCreate(String activationKey);

    private static native void nGetAlphaRange(long handle, byte[] alphaRange);

    private static native void nGetDispDiffRange(long handle, byte[] dispDiffRange);

    private static native void nGetRadiusRange(long handle, byte[] radiusRange);

    private static native void nGetMagnitudeRange(long handle, byte[] magnitudeRange);

    private static native void nGetFilterParams(long handle, byte[] params);

    private static native void nSetFilterParams(long handle, byte[] params);
}
