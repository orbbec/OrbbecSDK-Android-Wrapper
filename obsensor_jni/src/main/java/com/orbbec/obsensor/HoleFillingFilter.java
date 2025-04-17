package com.orbbec.obsensor;

import com.orbbec.obsensor.types.HoleFillingMode;

public class HoleFillingFilter extends Filter {
    /**
     * \if English
     * Create a HoleFilling Filter.
     * \else
     * 创建一个填洞滤波器
     * \endif
     */
    public HoleFillingFilter(String activationKey) {
        super(nCreate(activationKey));
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
    public void setFilterMode(HoleFillingMode mode) {
        throwInitializeException();
        setConfigValue("hole_filling_mode", mode.value());
//        nSetMode(mHandle, mode.value());
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
    public HoleFillingMode getFilterMode() {
        throwInitializeException();
        int value = (int) getConfigValue("hole_filling_mode");
        return HoleFillingMode.get(value);
    }

    private static native long nCreate(String activationKey);

    private static native void nSetMode(long handle, int mode);

    private static native int nGetMode(long handle);
}
