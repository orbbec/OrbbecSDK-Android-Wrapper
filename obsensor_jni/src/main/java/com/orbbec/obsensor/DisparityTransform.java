package com.orbbec.obsensor;

public class DisparityTransform extends Filter {
    /**
     * \if English
     * Create a disparity transform.
     * \else
     * 创建一个视察转换
     * \endif
     */
    public DisparityTransform(String activationKey) {
        super(nCreate(activationKey));
    }

    public DisparityTransform(long handle) {
        super(handle);
    }

    private static native long nCreate(String activationKey);
}
