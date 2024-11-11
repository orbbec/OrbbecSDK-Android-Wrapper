package com.orbbec.obsensor;

public class DisparityTransform extends Filter{
    /**
     * \if English
     * Create a disparity transform.
     * \else
     * 创建一个视察转换
     * \endif
     */
    public DisparityTransform(boolean depthToDisparity) {
        super(nCreate(depthToDisparity));
    }

    public DisparityTransform(long handle, boolean depthToDisparity) {
        super(handle);
    }

    private static native long nCreate(boolean depthToDisparity);
}
