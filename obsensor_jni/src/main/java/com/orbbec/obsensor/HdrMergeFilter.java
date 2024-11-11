package com.orbbec.obsensor;

public class HdrMergeFilter extends Filter {

    /**
     * \if English
     * Create a hdr merge filter.
     * \else
     * 创建一个hdr合并滤波器
     * \endif
     */
    public HdrMergeFilter() {
        super(nCreate());
    }

    HdrMergeFilter(long handle) {
        super(handle);
    }

    private static native long nCreate();
}
