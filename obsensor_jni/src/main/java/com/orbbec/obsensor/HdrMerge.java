package com.orbbec.obsensor;

public class HdrMerge extends Filter {

    /**
     * \if English
     * Create a hdr merge filter.
     * \else
     * 创建一个hdr合并滤波器
     * \endif
     */
    public HdrMerge() {
        super(nCreate());
    }

    HdrMerge(long handle) {
        super(handle);
    }

    private static native long nCreate();
}
