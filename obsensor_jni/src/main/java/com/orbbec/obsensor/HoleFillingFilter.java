package com.orbbec.obsensor;

public class HoleFillingFilter extends Filter{
    /**
     * \if English
     * Create a HoleFilling Filter.
     * \else
     * 创建一个填洞滤波器
     * \endif
     */
    public HoleFillingFilter() {
        super(nCreate());
    }

    HoleFillingFilter(long handle) {
        super(handle);
    }

    /**
     * \if English
     * Set the HoleFillingFilter mode.
     *
     * @param mode holefilling mode OB_HOLE_FILL_TOP,OB_HOLE_FILL_NEAREST or OB_HOLE_FILL_FAREST.
     * \else
     * 设置填洞滤波模式
     *
     * @param mode 填洞滤波模式 OB_HOLE_FILL_TOP,OB_HOLE_FILL_NEAREST or OB_HOLE_FILL_FAREST。
     * \endif
     */
    public void setMode(HoleFillingMode mode) {
        throwInitializeException();
        nSetMode(mHandle, mode.value());
    }

    /**
     * \if English
     * Get the HoleFillingFilter mode.
     *
     * @return the HoleFillingFilter mode
     * \else
     * 获取填洞滤波模式
     *
     * @return 填洞滤波模式
     * \endif
     */
    public HoleFillingMode getMode() {
        throwInitializeException();
        return HoleFillingMode.get(nGetMode(mHandle));
    }

    private static native long nCreate();

    private static native void nSetMode(long handle, int mode);

    private static native int nGetMode(long handle);
}
