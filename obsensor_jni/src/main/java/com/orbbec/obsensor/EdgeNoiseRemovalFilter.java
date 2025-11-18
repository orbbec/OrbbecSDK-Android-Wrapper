package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.EdgeNoiseRemovalParams;
import com.orbbec.obsensor.datatype.Uint16PropertyRange;

public class EdgeNoiseRemovalFilter extends Filter{
    /**
     * \if English
     * Create a edge noise removal filter.
     * \else
     * 创建一个边缘去噪滤波器
     * \endif
     */
    public EdgeNoiseRemovalFilter() {
        super(nCreate());
    }

    EdgeNoiseRemovalFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Set the edge noise removal filter params.
     * \else
     * 设置边缘去噪滤波器参数
     * \endif
     */
    public void setFilterParams(EdgeNoiseRemovalParams params) {
        throwInitializeException();
        if (!params.wrapBytes()) {
            throw new OBException("setFilterParams wrap bytes error!");
        }
        nSetFilterParams(mHandle, params.getBytes());
    }

    /**
     * \if English
     * Get the edge noise removal filter params.
     * \else
     * 获取边缘去噪滤波器参数
     * \endif
     */
    public EdgeNoiseRemovalParams getFilterParams() {
        throwInitializeException();
        EdgeNoiseRemovalParams params = new EdgeNoiseRemovalParams();
        nGetFilterParams(mHandle, params.getBytes());
        boolean result = params.parseBytes();
        if (!result) {
            throw new OBException("getFilterParams parse bytes error!");
        }
        return params;
    }

    /**
     * \if English
     * Get the noise removal filter margin left th range.
     *
     * @return the margin_left_th value of property range.
     * \else
     * 获取去噪滤波器边缘左边缘值范围
     *
     * @return 属性范围的左边缘值
     * \endif
     */
    public Uint16PropertyRange getMarginLeftThRange() {
        throwInitializeException();
        Uint16PropertyRange marginLeftThRange = new Uint16PropertyRange();
        nGetMarginLeftThRange(mHandle, marginLeftThRange.getBytes());
        boolean result = marginLeftThRange.parseBytes();
        if (!result) {
            throw new OBException("getMarginLeftThRange parse bytes error!");
        }
        return marginLeftThRange;
    }

    /**
     * \if English
     * Get the noise removal filter margin right th range.
     *
     * @return the margin_right_th value of property range.
     * \else
     * 获取去噪滤波器边缘右边缘值范围
     *
     * @return 属性范围的右边缘值
     * \endif
     */
    public Uint16PropertyRange getMarginRightThRange() {
        throwInitializeException();
        Uint16PropertyRange marginRightThRange = new Uint16PropertyRange();
        nGetMarginRightThRange(mHandle, marginRightThRange.getBytes());
        boolean result = marginRightThRange.parseBytes();
        if (!result) {
            throw new OBException("getMarginRightThRange parse bytes error!");
        }
        return marginRightThRange;
    }

    /**
     * \if English
     * Get the noise removal filter margin top th range.
     *
     * @return the margin_top_th value of property range.
     * \else
     * 获取去噪滤波器边缘上边缘值范围
     *
     * @return 属性范围的上边缘值
     * \endif
     */
    public Uint16PropertyRange getMarginTopThRange() {
        throwInitializeException();
        Uint16PropertyRange marginTopThRange = new Uint16PropertyRange();
        nGetMarginTopThRange(mHandle, marginTopThRange.getBytes());
        boolean result = marginTopThRange.parseBytes();
        if (!result) {
            throw new OBException("getMarginTopThRange parse bytes error!");
        }
        return marginTopThRange;
    }

    /**
     * \if English
     * Get the noise removal filter margin bottom th range.
     *
     * @return the margin_bottom_th value of property range.
     * \else
     * 获取去噪滤波器边缘下边缘值范围
     *
     * @return 属性范围的下边缘值
     * \endif
     */
    public Uint16PropertyRange getMarginBottomThRange() {
        throwInitializeException();
        Uint16PropertyRange marginBottomThRange = new Uint16PropertyRange();
        nGetMarginBottomThRange(mHandle, marginBottomThRange.getBytes());
        boolean result = marginBottomThRange.parseBytes();
        if (!result) {
            throw new OBException("getMarginBottomThRange parse bytes error!");
        }
        return marginBottomThRange;
    }

    private static native long nCreate();

    private static native void nSetFilterParams(long handle, byte[] params);

    private static native void nGetFilterParams(long handle, byte[] params);

    private static native void nGetMarginLeftThRange(long handle, byte[] marginLeftThRange);

    private static native void nGetMarginRightThRange(long handle, byte[] marginRightThRange);

    private static native void nGetMarginTopThRange(long handle, byte[] marginTopThRange);

    private static native void nGetMarginBottomThRange(long handle, byte[] marginBottomThRange);
}
